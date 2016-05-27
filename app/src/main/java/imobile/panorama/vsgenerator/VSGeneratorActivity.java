package imobile.panorama.vsgenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import imobile.panorama.R;
import imobile.panorama.util.Constants;

//import android.annotation.TargetApi;
//import android.annotation.TargetApi;

//@TargetApi(14)
public class VSGeneratorActivity extends Activity implements
		SurfaceTexture.OnFrameAvailableListener,
		MosaicRendererSurfaceViewRenderer.MosaicSurfaceCreateListener,
		SurfaceHolder.Callback,
		View.OnClickListener
{
	
	private static final String TAG = "myDebug";
	/* 使用到的常量 */
	public static final int DEFAULT_SWEEP_ANGLE = 326; 
	public static final int DEFAULT_BLEND_MODE = Mosaic.BLENDTYPE_HORIZONTAL;
	public static final int DEFAULT_CAPTURE_PIXELS = 960 * 720;

	/* 控制最后合成全景图时常量 */
	private static final int MSG_GENERATE_FINAL_MOSAIC_ERROR = 1;
	private static final int MSG_RESET_TO_PREVIEW = 2;
	private static final int MSG_CLEAR_SCREEN_DELAY = 3;

	private static final int SCREEN_DELAY = 2 * 60 * 1000;

	/* 相机的状态 */
	private static final int PREVIEW_STOPPED = 0;
	private static final int PREVIEW_ACTIVE = 1;
	/* 合成全景图时的工作状态 */ 
	private int mCaptureState;
	private static final int CAPTURE_STATE_VIEWFINDER = 0;					// 初始状态
	private static final int CAPTURE_STATE_MOSAIC     = 1;					// 合成全景图状态
	/* 临界值 */
	private static final float PANNING_SPEED_THRESHOLD = 20f;					// 平移速率的最大临界值
	/* 对话框---设置GPS */
	private final int DIALOG_SETGPS = 1;										// 给全景图命名的对话框
	private final int RC_SETTINGGPS = 0;										// REQUEST_CODE
	/* Handler Code */ 
	private VSGHandler handler = null;
	private final int HANDLER_OPEN_LOCSERVICE = 10;
	private final int HANDLER_CLOSE_LOCSERVICE = 11;
	// 存储Pano的文件夹名称
	private String strPanoDir = "";
	private String filePanoPath = "";
	// 获取SharedPreference存储的参数
	private SharedPreferences sp = null;

	/* UI 控件 */
	private Button btnStitch;													// 显示控件
	private PostureView pv;														// 姿态数据的显示
	private ImageView mReview;													// 显示左侧图片
	/* 传感器 */
	private SensorManager mSensorManager;
	private Sensor mAcce = null;
	private Sensor mMagn = null;
	private float[] acceData = new float[3];									// 加速度
	private float[] magnData = new float[3];									// 磁值
	private float[] orienData = new float[3];									// 方向角度
	private final SensorEventListener mSensorListener = new SensorEventListener()
	{
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy){}

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
				acceData[0] = event.values[0];
				acceData[1] = event.values[1];
				acceData[2] = event.values[2];
			}else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
				magnData[0] = event.values[0];
				magnData[1] = event.values[1];
				magnData[2] = event.values[2];
			}
			
		}
	};
	
	private TimerTask tt = null;												// 读取传感器任务
	private LocationManager lm = null;											// 定位管理器

	/** 判断是单独生成一个全景图文件，还是在虚拟空间中生成一个全景图文件 */
	private boolean isSinglePano = true;										// 单独生成全景图还是拼接全景图
	private boolean mPausing;													// Boolean标记值，用来判断Activity是否暂停

	private MosaicRendererSurfaceView mMosaicView;								// 显示控件

	private int mPreviewWidth;
	private int mPreviewHeight;
	private int mCameraState;
	
	private MosaicFrameProcessor mMosaicFrameProcessor;							// 照相帧处理方法
	private long mTimeTaken;
	private SurfaceTexture mSurfaceTexture;										// 显示摄像头图片
	private boolean mThreadRunning;											// 后台线程标志位
	private float[] mTransformMatrix;											// 转移矩阵，TextureView上的数据显示在mMosaicView上
	private float mHorizontalViewAngle;
	private float mVerticalViewAngle;
	private float mAccumulatePercent = 0;

	private String mTargetFocusMode = Parameters.FOCUS_MODE_AUTO ;				//Parameters.FOCUS_MODE_INFINITY

	// 相机拍摄时屏幕的角度
	private float mLeftAngle;
	private int mDeviceOrientationAtCapture;
	private int mCameraOrientation;

	/* 相机对象 */ 
	private Camera mCameraDevice;
	private Camera.PreviewCallback previewCallback;
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		isSinglePano = getIntent().getBooleanExtra(
				Constants.INTENT_SINGLE_PANO, false);

		//getWindow().requestFeature(Window.FEATURE_NO_TITLE);					// 程序无Title
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,		// 全屏显示
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mCaptureState = CAPTURE_STATE_VIEWFINDER;								// 初始化状态
		
		setContentView(R.layout.vsgenerator);
		mMosaicView = (MosaicRendererSurfaceView)findViewById(R.id.sfv_vsg_camera);
		mMosaicView.getRenderer().setMosaicSurfaceCreateListener(this);
		btnStitch = (Button) findViewById(R.id.btn_vsg_stitch);
		btnStitch.setOnClickListener(this);
		pv = (PostureView)findViewById(R.id.pv_posture);
		mReview = (ImageView)findViewById(R.id.iv_first_bmp);
		
		handler = new VSGHandler();
		
		sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		// 判断是否需要给全景图设置GPS标签
		if (sp.getBoolean(Constants.PREF_CB_GEOTAG, false))
		{
			lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				showDialog(DIALOG_SETGPS);
			} else
			{
				handler.sendEmptyMessage(HANDLER_OPEN_LOCSERVICE);
			}
		}

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAcce = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagn = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mSensorManager.registerListener(mSensorListener, mAcce,
				SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(mSensorListener, mMagn,
				SensorManager.SENSOR_DELAY_UI);

		mTransformMatrix = new float[16];
		
      	previewCallback = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] _data, Camera arg1) {
                if (_data != null)
                {
                    Camera.Parameters parameters = mCameraDevice.getParameters();
                    int imageFormat = parameters.getPreviewFormat();

                    if (imageFormat == ImageFormat.NV21)//貌似必须为NV21格式，这也导致无法转为2.3以下版本?????????
                    {
                        // get full picture
                        Bitmap image = null;
                        int w = parameters.getPreviewSize().width;
                        int h = parameters.getPreviewSize().height;
                         
                        Rect rect = new Rect(0, 0, w, h);
                        YuvImage img = new YuvImage(_data, ImageFormat.NV21, w, h, null);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        if (img.compressToJpeg(rect, 100, baos))
                        {
                            image =  BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
                			image = Bitmap.createBitmap(image, 0, 0, (int) (image.getWidth() * 0.125), image.getHeight());
                			if(sp.getBoolean("PREF_CB_FIRST_PICTURE", true)){
                				mReview.setImageBitmap(image);
                                mReview.setVisibility(View.VISIBLE);
                			}
                        }
                    }
                }
            }
        };
	
        wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        registerReceiver(brWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		Timer update = new Timer("UPDATE");
		tt = new TimerTask()
		{
			@Override
			public void run()
			{
				if (acceData != null && magnData != null)
				{
					float[] values = new float[3];
					float[] valueR = new float[9];
					SensorManager.getRotationMatrix(valueR, null, acceData,
							magnData);
					SensorManager.getOrientation(valueR, values);

					values[0] = (float) Math.toDegrees(values[0]);
					values[1] = (float) Math.toDegrees(values[1]);
					values[2] = (float) Math.toDegrees(values[2]);

					orienData[0] = (values[0] + 360) % 360;
					orienData[1] = values[1];
					orienData[2] = values[2];

					pv.setData(orienData);
				}
			}
		};
		
		update.scheduleAtFixedRate(tt, 2000, 100);
		
		if (mCameraDevice == null){
			doOnResume();
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();

		if (tt != null){
			tt.cancel();
		}

		mPausing = true;
		
		if (mCaptureState == CAPTURE_STATE_MOSAIC)
		{
			stopCapture(true);
			reset();
		}


		releaseCamera();
		mMosaicView.onPause();
		clearMosaicFrameProcessorIfNeeded();
		resetScreenOn();
		System.gc();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mSensorManager.unregisterListener(mSensorListener);
		handler.sendEmptyMessage(HANDLER_CLOSE_LOCSERVICE);

		unregisterReceiver(brWifi);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RC_SETTINGGPS)
		{
			// 如果GPS已经打开
			if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				handler.sendEmptyMessage(HANDLER_OPEN_LOCSERVICE);
			}
		}
	}
	
	public void doOnResume()
	{
		mPausing = false;

		mCaptureState = CAPTURE_STATE_VIEWFINDER;
		
		try
		{
			setupCamera(); 														// 包含打开相机
			initMosaicFrameProcessorIfNeeded(); 								// 包含初始化mosaic
			mMosaicView.onResume();

			// initThumbnailButton();
			keepScreenOnAwhile();
		} catch (Exception e)
		{
			return;
		}
	}

//	@OnClickAttr
//	public void onCancelButtonClicked(View v)
//	{
//		if (mPausing || mSurfaceTexture == null)
//			return;
//		cancelHighResComputation();
//	}
	
	// *************************************************
	// Create and Prepare Dialog
	// *************************************************
	private EditText etPanoName = null;
	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog = null;
		LayoutInflater factory = null;
		View view = null;
		AlertDialog.Builder builder = null;
		switch (id)
		{
		case DIALOG_SETGPS:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.open_gps));
			builder.setMessage(getResources().getString(
					R.string.use_gps_panorama));
			builder.setPositiveButton(getResources().getString(R.string.yes),
					new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							startActivityForResult(new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS),
									RC_SETTINGGPS);
						}
					});
			builder.setNegativeButton(getResources().getString(R.string.no),
					new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
						}
					});
			dialog = builder.create();
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		// TODO Auto-generated method stub
		super.onPrepareDialog(id, dialog);
	}

	// ****************************************************
	// ServiceConnection for LocationService
	// ****************************************************
	private LocationService srsBinder = null;
	private boolean bindornot = false;
	private ServiceConnection sc = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			srsBinder = ((LocationService.LocationBinder) service).getService();
			bindornot = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
		}
	};



	// *************************************
	// 内部类---Handler
	// *************************************
	class VSGHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			switch (msg.what)
			{
			case HANDLER_OPEN_LOCSERVICE: 										// 打开LocationService
				Intent in = new Intent(VSGeneratorActivity.this,
						LocationService.class);
				bindService(in, sc, Context.BIND_AUTO_CREATE);
				break;
			case HANDLER_CLOSE_LOCSERVICE: 										// 关闭LocationService
				if (bindornot)
				{
					unbindService(sc);
				}
				break;
			case MSG_GENERATE_FINAL_MOSAIC_ERROR:
				onBackgroundThreadFinished();
				if(mPausing) {
					resetToPreview();
				} else {

				}
				break;
			case MSG_RESET_TO_PREVIEW:
				onBackgroundThreadFinished();
				resetToPreview();
				break;
			case MSG_CLEAR_SCREEN_DELAY:
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				break;
			}
			clearMosaicFrameProcessorIfNeeded();
		}
	}

	
	// *************************************
	// Button OnClickListener
	// *************************************
	@Override
	public void onClick(View v) {
		if (mPausing || mThreadRunning || mSurfaceTexture == null)
			return;
		switch (mCaptureState) {
		case CAPTURE_STATE_VIEWFINDER:											// 如果是初始状态,
			mCaptureState = CAPTURE_STATE_MOSAIC;								// 则点击按钮后进入拍摄全景图状态
			startCapture();
			mLeftAngle = orienData[0];											// 获取当前手机指向的角度值
			mCameraDevice.setOneShotPreviewCallback(previewCallback);			// 相机回调，执行一次（One Shot)
			btnStitch.setBackgroundResource(R.drawable.shutter_btn_focus);		// 改变按钮的背景图
			
//			wm.startScan();
			break;
		case CAPTURE_STATE_MOSAIC:												// 如果是正在拍摄全景图，
			mCaptureState = CAPTURE_STATE_VIEWFINDER;							// 则点击按钮停止拍摄
			stopCapture(false);
			btnStitch.setBackgroundResource(R.drawable.shutter_btn_normal);

			finish();
		}
	}
		

	// ***************************************
	// Camera 操作
	// ***************************************
	private void setupCamera() throws Exception
	{
		openCamera();
		Parameters parameters = mCameraDevice.getParameters();
		setupCaptureParams(parameters);
		configureCamera(parameters);
	}
	
	private void openCamera() throws Exception {
		int backCameraId = CameraHolder.instance().getBackCameraId(); 			// 后置摄像头
		mCameraDevice = Util.openCamera(this, backCameraId); 					// 初始化mCameraDevice对象
		mCameraOrientation = Util.getCameraOrientation(backCameraId);
	}


	private void releaseCamera()												// 释放mCameraDevice对象
	{
		if (mCameraDevice != null)
		{
			mCameraDevice.setPreviewCallbackWithBuffer(null);
			CameraHolder.instance().release();
			mCameraDevice = null;
			mCameraState = PREVIEW_STOPPED;
		}
	}

	
	private boolean findBestPreviewSize(List<Size> supportedSizes,
			boolean need4To3, boolean needSmaller)								// 最好是4:3格式的
	{
		int pixelsDiff = DEFAULT_CAPTURE_PIXELS;
		boolean hasFound = false;
		for (Size size : supportedSizes)
		{
			int h = size.height;
			int w = size.width;

			int d = DEFAULT_CAPTURE_PIXELS - h * w;
			if (needSmaller && d < 0)
			{ 
				continue;
			}
			if (need4To3 && (h * 4 != w * 3))
			{
				continue;
			}
			d = Math.abs(d);
			if (d < pixelsDiff)
			{
				mPreviewWidth = w;
				mPreviewHeight = h;
				pixelsDiff = d;
				hasFound = true;
			}
		}
		return hasFound;
	}

	private void setupCaptureParams(Parameters parameters)
	{
		List<Size> supportedSizes = parameters.getSupportedPreviewSizes();
		if (!findBestPreviewSize(supportedSizes, true, true))
		{
			Log.w(TAG, "No 4:3 ratio preview size supported.");
			if (!findBestPreviewSize(supportedSizes, false, true))
			{
				Log.w(TAG,
						"Can't find a supported preview size smaller than 960x720.");
				findBestPreviewSize(supportedSizes, false, false);
			}
		}
		Log.v(TAG, "preview h = " + mPreviewHeight + " , w = " + mPreviewWidth);
		parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);

		List<int[]> frameRates = parameters.getSupportedPreviewFpsRange();
		int last = frameRates.size() - 1;
		int minFps = (frameRates.get(last))[Parameters.PREVIEW_FPS_MIN_INDEX];
		int maxFps = (frameRates.get(last))[Parameters.PREVIEW_FPS_MAX_INDEX];
		parameters.setPreviewFpsRange(minFps, maxFps);
		Log.v(TAG, "preview fps: " + minFps + ", " + maxFps);

		List<String> supportedFocusModes = parameters.getSupportedFocusModes();
		if (supportedFocusModes.indexOf(mTargetFocusMode) >= 0)
		{
			parameters.setFocusMode(mTargetFocusMode);
		} else
		{
		}

		parameters.setRecordingHint(false);

		mHorizontalViewAngle = parameters.getHorizontalViewAngle();
		mVerticalViewAngle = parameters.getVerticalViewAngle();
	}

	public int getPreviewBufSize()
	{
		PixelFormat pixelInfo = new PixelFormat();
		PixelFormat.getPixelFormatInfo(mCameraDevice.getParameters()
				.getPreviewFormat(), pixelInfo);
		return (mPreviewWidth * mPreviewHeight * pixelInfo.bitsPerPixel / 8) + 32;
	}

	private void configureCamera(Parameters parameters)
	{
		mCameraDevice.setParameters(parameters);
	}

	// ******************************************************************************
	// MosaicRendererSurfaceViewRenderer.MosaicSurfaceCreateListener
	// ******************************************************************************
	@Override
	public void onMosaicSurfaceCreated(final int textureID)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (mSurfaceTexture != null)
				{
					mSurfaceTexture.release();
				}
				mSurfaceTexture = new SurfaceTexture(textureID);
				if (!mPausing)
				{
					mSurfaceTexture
							.setOnFrameAvailableListener(VSGeneratorActivity.this); // ////////////////////////////////
				}
			}
		});
	}
	
	@Override
	public void onMosaicSurfaceChanged()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (!mPausing)
				{
					startCameraPreview();
				}
			}
		});
	}
	

	public void runViewFinder()													// CAPTURE_STATE_VIEWFINDER
	{
		mMosaicView.setWarping(false);
		mMosaicView.preprocess(mTransformMatrix);
		mMosaicView.setReady();
		mMosaicView.requestRender();
	}

	public void runMosaicCapture()													// CAPTURE_STATE_MOSAIC
	{
		mMosaicView.setWarping(true);
		mMosaicView.preprocess(mTransformMatrix);

		mMosaicView.lockPreviewReadyFlag();
		// 将GPU中的Textures转换到CPU内存中，做进一步处理
		mMosaicView.transferGPUtoCPU();
		// 阻塞当前进程 (当 GPU->CPU 转移完成的时候，该进程被再次开启）
		mMosaicView.waitUntilPreviewReady();
		mMosaicFrameProcessor.processFrame();
		
		// 在updateProgress函数中执行以下两句话
		mMosaicView.setReady();
		mMosaicView.requestRender();
	}

	
	// ***************************************
	// SurfaceTexture.OnFrameAvailableListener
	// ***************************************
	public synchronized void onFrameAvailable(SurfaceTexture surface)
	{
		// Frames might still be available after the activity is paused. If we
		// call onFrameAvailable
		// after pausing, the GL thread will crash.
		if (mPausing)
			return;

		// 更新纹理(texture)应当在GL线程中完成
		// queueEvent转到GL线程
		mMosaicView.queueEvent(new Runnable()
		{
			@Override
			public void run()
			{
				if (mPausing)
					return;
				mSurfaceTexture.updateTexImage();
				mSurfaceTexture.getTransformMatrix(mTransformMatrix);
			}
		});
		
		// *****************在这里将照相机获取的的图片信息转给mosaic做进一步处理*****************
		// Update the transformation matrix for mosaic pre-process.
		if (mCaptureState == CAPTURE_STATE_VIEWFINDER)
		{
			runViewFinder();
		} else
		{
			// ######################################################
			// 可以再这里做略微的优化（传感器辅助从而减少找帧的时间）
			runMosaicCapture();
			
		}
	}


	// ***************************************
	// 合成全景图周期控制
	// ***************************************
	public void startCapture()													// 开始---捕获图片，用于合成全景图
	{
		// 重新设置参数
		mTimeTaken = System.currentTimeMillis();    							// 获得当前时间

		mMosaicFrameProcessor													// 设置Mosaic处理的进度信息的监听器
				.setProgressListener(new MosaicFrameProcessor.ProgressListener()
		{
			@Override
			public void onProgress(boolean isFinished,
					float panningRateX, float panningRateY,
					float progressX, float progressY)
			{
				
				float accumulatedHorizontalAngle = progressX
								* mHorizontalViewAngle;
				float accumulatedVerticalAngle = progressY
								* mVerticalViewAngle;
						
				// 应当在这里计算完成的百分比
//				int angleInMajorDirection = (Math.abs(accumulatedHorizontalAngle) > Math
//								.abs(accumulatedVerticalAngle)) ? (int) accumulatedHorizontalAngle
//								: (int) accumulatedVerticalAngle;
				int angleInMajorDirection = (int)accumulatedHorizontalAngle;
				float tempPercent = (float)(angleInMajorDirection)/DEFAULT_SWEEP_ANGLE;
				if(mAccumulatePercent<tempPercent){
					
					mAccumulatePercent = tempPercent;
					
					if(mAccumulatePercent>1 || isFinished){
						mAccumulatePercent = 1;
					}
					pv.setPercnet(mAccumulatePercent);
				}
				

				if (isFinished || Math.abs(accumulatedHorizontalAngle) >= DEFAULT_SWEEP_ANGLE)
				{
					mCaptureState = CAPTURE_STATE_VIEWFINDER;
					stopCapture(false);
				} else {
					float panningRateXInDegree = panningRateX
									* mHorizontalViewAngle;
					float panningRateYInDegree = panningRateY
									* mVerticalViewAngle;
					updateProgress(panningRateXInDegree,
									panningRateYInDegree,
									accumulatedHorizontalAngle,
									accumulatedVerticalAngle);
				}
			}
		});
		keepScreenOn();
	}

	private void stopCapture(boolean aborted)									// 停止----收集完所需的合成全景图所需图片
																				// 其中布尔值aborted表示是否被意外终止
	{
		stopCameraPreview();

		mSurfaceTexture.setOnFrameAvailableListener(null);

		if (!aborted && !mThreadRunning)
		{
			Intent intent = new Intent(VSGeneratorActivity.this, StitchActivity.class);
			intent.putExtra(Constants.INTENT_SINGLE_PANO, isSinglePano);
			startActivity(intent);
			runBackgroundThread(new Thread()
			{
				@Override
				public void run()
				{
					boolean result = false;
					
					MosaicJpeg jpeg = generateFinalMosaic(sp.getBoolean("PREF_CB_HIGH_VALUE_PICTURE", true));  

					if(jpeg == null)
					{
						handler.sendEmptyMessage(MSG_RESET_TO_PREVIEW);				// 被用户取消
					}else if (!jpeg.isValid)
					{ 
						handler.sendEmptyMessage(MSG_GENERATE_FINAL_MOSAIC_ERROR);	// 合成时出错
					} else if (jpeg != null && jpeg.isValid)
					{
						
						// 存储该到这里
						int orientation = (mDeviceOrientationAtCapture + mCameraOrientation) % 360;
						result = savePanorama(jpeg.data, jpeg.width,
								jpeg.height, orientation);
;

						Intent in = new Intent(StitchActivity.BR_STITCH_FINISH);
						in.putExtra("SAVE_RESULT", result);
						in.putExtra(Constants.INTENT_FILEDIR, filePanoPath);
						sendBroadcast(in);
						
						// 如果是VSEditor中打开的，则需返回路径名
						if (!isSinglePano) {
							Intent in2 = new Intent(VSGeneratorActivity.this,
									StitchActivity.class);
							in2.putExtra(Constants.INTENT_FILEDIR, filePanoPath);
							setResult(RESULT_OK, in2);
						}
					} 
					
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							finish();
						}
					});
				}
			});
		}
		keepScreenOnAwhile();
	}


	
	private void updateProgress(float panningRateXInDegree,						// 进度条更新
			float panninRateYInDegree, float progressHorizontalAngle,
			float progressVerticalAngle)
	{

		// 这里设置速度警告
		if ((Math.abs(panningRateXInDegree) > PANNING_SPEED_THRESHOLD)
				|| (Math.abs(panninRateYInDegree) > PANNING_SPEED_THRESHOLD)) {
			 pv.setIsFast(true);
		} else {
			 pv.setIsFast(false);
		}
		
	}

	/* 管理后台线程---该线程用于合成全景图，并存储 */
	private void runBackgroundThread(Thread thread)								// 开启后台线程
	{
		mThreadRunning = true;
		thread.start();
	}

	private void onBackgroundThreadFinished()										// 关闭后台线程
	{
		mThreadRunning = false;
	}

//	private void cancelHighResComputation()
//	{
//		synchronized (mWaitObject)
//		{
//			mWaitObject.notify();
//		}
//	}

	

	private void reset()
	{
		mCaptureState = CAPTURE_STATE_VIEWFINDER;
		
		mMosaicFrameProcessor.reset();

		mSurfaceTexture.setOnFrameAvailableListener(this);
	}

	private void resetToPreview()
	{
		reset();
		if (!mPausing)
			startCameraPreview();
	}

	private boolean savePanorama(byte[] jpegData, int width, int height,
			int orientation)													// 保存全景图
	{
		if (jpegData != null)
		{
			String fileName = Util.createName(									// 产生名字的地方,全景图名字
					getResources().getString(R.string.pano_file_name_format),
					mTimeTaken);
			
			
			boolean result = Storage.saveImage(fileName, jpegData);
			if (result && orientation != 0)
			{
				filePanoPath = Storage.generateFilepath(fileName);
				try
				{
					// EXIF
					ExifInterface exif = new ExifInterface(filePanoPath);

					mLeftAngle = (mLeftAngle+90-20)%360;						// 估测全景图的起始角度值
					
					mAccumulatePercent += (float)mHorizontalViewAngle/360;
					if(mAccumulatePercent>=1){
						mAccumulatePercent = 1;
					}
					
					String str = "";
					
					str += (int)mLeftAngle+"\t"+mAccumulatePercent;
					
					
					Location lc = null;
					if (bindornot) {
						lc = srsBinder.getLocation();
						if(lc!=null){
							exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, lc.getLatitude()+"");//
							exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lc.getLongitude()+"");//
						}
					}
					exif.setAttribute(ExifInterface.TAG_MAKE, str);
					exif.saveAttributes();
				} catch (IOException e)
				{
					Log.e(TAG, "cannot set exif data: " + filePanoPath);
				}
			}
			return true;
		}
		return false;
	}

	private void clearMosaicFrameProcessorIfNeeded()								// 清空，是否mMosaicFrameProcessor申请的内存空间
	{
		if (!mPausing || mThreadRunning)
			return;
		mMosaicFrameProcessor.clear();
	}

	private void initMosaicFrameProcessorIfNeeded()								// mMosaicFrameProcessor的初始化
	{
		if (mPausing || mThreadRunning)
			return;
		if (mMosaicFrameProcessor == null)
		{
			mMosaicFrameProcessor = new MosaicFrameProcessor(mPreviewWidth,
					mPreviewHeight, getPreviewBufSize());
		}
		mMosaicFrameProcessor.initialize();
	}



	/**
	 * 合成最后拼接成的图像
	 * @param highRes  标记位，判断是否生成高质量的全景图
	 */
	public MosaicJpeg generateFinalMosaic(boolean highRes)
	{
		int mosaicReturnCode = mMosaicFrameProcessor.createMosaic(highRes);		// 执行合成函数
		if (mosaicReturnCode == Mosaic.MOSAIC_RET_CANCELLED)
		{
			return null;
		} else if (mosaicReturnCode == Mosaic.MOSAIC_RET_ERROR)
		{
			return new MosaicJpeg();
		}

		byte[] imageData = mMosaicFrameProcessor.getFinalMosaicNV21();			// 获取图像信息
		if (imageData == null)
		{
			Log.e(TAG, "getFinalMosaicNV21() returned null.");
			return new MosaicJpeg();
		}

		int len = imageData.length - 8;
		int width = (imageData[len + 0] << 24)
				+ ((imageData[len + 1] & 0xFF) << 16)
				+ ((imageData[len + 2] & 0xFF) << 8)
				+ (imageData[len + 3] & 0xFF);
		int height = (imageData[len + 4] << 24)
				+ ((imageData[len + 5] & 0xFF) << 16)
				+ ((imageData[len + 6] & 0xFF) << 8)
				+ (imageData[len + 7] & 0xFF);

		if (width <= 0 || height <= 0)
		{
			Log.e(TAG, "width|height <= 0!!, len = " + (len) + ", W = " + width
					+ ", H = " + height);
			return new MosaicJpeg();
		}

		/* 将YuvImage格式存储为Jpeg格式 */
		YuvImage yuvimage = new YuvImage(imageData, ImageFormat.NV21, width,
				height, null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, out);
		try
		{
			out.close();
		} catch (Exception e)
		{
			Log.e(TAG, "Exception in storing final mosaic", e);
			return new MosaicJpeg();
		}
		return new MosaicJpeg(out.toByteArray(), width, height);
	}

	
	
	private void setPreviewTexture(SurfaceTexture surface)						// SurfaceTexture 与 CameraDevice关联起来
	{
		try
		{
			mCameraDevice.setPreviewTexture(surface);
		} catch (Throwable ex)
		{
			releaseCamera();
			throw new RuntimeException("setPreviewTexture failed", ex);
		}
	}
	
	
	
	private void startCameraPreview()											// 开启CameraPreview
	{
		if (mCameraState != PREVIEW_STOPPED)
			stopCameraPreview();

		mCameraDevice.setDisplayOrientation(0);									// 0: landscape

		setPreviewTexture(mSurfaceTexture);

		try{
			mCameraDevice.startPreview();
		} catch (Throwable ex) {
			releaseCamera();
			throw new RuntimeException("startPreview failed", ex);
		}
		mCameraState = PREVIEW_ACTIVE;
	}

	private void stopCameraPreview()											// 关闭CameraPreview
	{
		if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED){
			mCameraDevice.stopPreview();
		}
		mCameraState = PREVIEW_STOPPED;
	}

	@Override
	public void onUserInteraction()
	{
		super.onUserInteraction();
		if (mCaptureState != CAPTURE_STATE_MOSAIC)
			keepScreenOnAwhile();
	}

	private void resetScreenOn()
	{
		handler.removeMessages(MSG_CLEAR_SCREEN_DELAY);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void keepScreenOnAwhile()
	{
		handler.removeMessages(MSG_CLEAR_SCREEN_DELAY);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		handler.sendEmptyMessageDelayed(MSG_CLEAR_SCREEN_DELAY, SCREEN_DELAY);
	}

	private void keepScreenOn()													// 保持屏幕开启
	{
		handler.removeMessages(MSG_CLEAR_SCREEN_DELAY);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	// ***************************************
	// 内部类---MosaicJpeg
	// ***************************************
	private class MosaicJpeg {
		public MosaicJpeg(byte[] data, int width, int height) {
			this.data = data;													// data为最后合成全景图时的数据
			this.width = width;
			this.height = height;
			this.isValid = true;
		}

		public MosaicJpeg() {
			this.data = null;													// null 表示合成全景图过程被用户取消
			this.width = 0;
			this.height = 0;
			this.isValid = false;
		}

		public final byte[] data;
		public final int width;
		public final int height;
		public final boolean isValid;											// 标记位：true表示成功，false表示合成全景图时出错
	}

	
	
	// ***************************************
	// SurfaceHolder的回调函数
	// ***************************************
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0)
	{
		// TODO Auto-generated method stub
		
	}
	
	private WifiManager wm = null;
	private WifiBR      brWifi = new WifiBR();
	private FileOutputStream fosWiFi = null;
	class WifiBR extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(wm!=null){
				
//				String strWiFi = "";
//				
//				fosWiFi = Storage.createOutputFile("wifi.txt");
//				
//				String fileName =Util.createName(									// 产生名字的地方,全景图名字
//						getResources().getString(R.string.pano_file_name_format),
//						mTimeTaken);
//				fileName+="\n";
//				try {
//					fosWiFi.write(fileName.getBytes());
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
//				List<ScanResult> results = wm.getScanResults();
//				for (ScanResult result : results) {
//					strWiFi += result.BSSID + "\t" + result.SSID + "\t"
//							+ result.level + "\n";
//				}
//				
//				try {
//					fosWiFi.write(strWiFi.getBytes());
//					fosWiFi.flush();
//					fosWiFi.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}
	}
}
