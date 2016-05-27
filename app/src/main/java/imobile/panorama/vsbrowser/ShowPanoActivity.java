package imobile.panorama.vsbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import imobile.panorama.R;
import imobile.panorama.util.HotSpot;
import imobile.panorama.util.Scene;
import imobile.panorama.util.VPSFile;

public class ShowPanoActivity extends Activity{
	private Context context;
	
	// 用于显示的控件，跳转到下一个场景
	private ShowPanoSfView view;
	private int leftAngle;
	private Button   btnNextPic;	// 导航到下一个场景中
	private ImageView ivImage;
    private TextView tvAngle;

	// 传感器
//	private float[] orienData;	// 方向角度值
	private SensorManager sm = null;
	private Sensor mAcce  = null;
	private Sensor mMagn = null;
	private float[] acceData = new float[3];
	private float[] magnData = new float[3];
	private final SensorEventListener mAcceListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			if(event.sensor.getType()!=Sensor.TYPE_ACCELEROMETER)
				return;
			acceData[0] = event.values[0];
			acceData[1] = event.values[1];
			acceData[2] = event.values[2];
		}
	};
	private final SensorEventListener mMagnListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
		@Override
		public void onSensorChanged(SensorEvent event) {
			if(event.sensor.getType()!=Sensor.TYPE_MAGNETIC_FIELD)
				return;
			magnData[0] = event.values[0];
			magnData[1] = event.values[1];
			magnData[2] = event.values[2];
		}
	};
	// TimerTask（根据方向传感器和磁传感器合成偏北角度值以及姿态仪数据）
	private TimerTask tt = null;
	
	// 当前场景（*保留参数）
	private Scene mSc = null;
	// 按钮指向的场景ID
	private int  nextID = -1;
	
	
	// 长和高
	private int width;
	private int height;
	private Bitmap bmpV;
	private AlphaAnimation alpha;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d("myDebug", "ShowPanoActivity onCreate()");
		super.onCreate(savedInstanceState);
		
		context = this;
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// 进入PanoSpace的场景
		if(VPSFile.getSceneNum()>0){
			mSc = VPSFile.getSceneByI(0);
		}
		
		WindowManager wm = getWindowManager();
		Display dis = wm.getDefaultDisplay();
		width = dis.getWidth();
		height = dis.getHeight();
		bmpV = Bitmap.createBitmap( width,height,Config.ARGB_8888 ); 
		
		setContentView(R.layout.showpano);
		view = (ShowPanoSfView)findViewById(R.id.sfv_pano);
		view.requestFocus();
		view.setFocusableInTouchMode(true);

        tvAngle = (TextView)findViewById(R.id.tv_angle);
        tvAngle.setText(""+leftAngle);
        tvAngle.setVisibility(View.VISIBLE);

		btnNextPic = (Button)findViewById(R.id.btn_nextpic);
		btnNextPic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				
//				view.getRootView().getDecorView();
				
//				bmpV = PanoRender.SavePixels(0, 0, 800, 400);
				
				
				
//				Canvas cv = new Canvas(bmpV);
//				view.draw(cv);
//				cv.save(Canvas.ALL_SAVE_FLAG);
//				cv.restore();
				
//				View decV = v.getWindow().getDecorView();
//				view.setDrawingCacheEnabled(true);
//				view.buildDrawingCache();
//				bmpV = view.getDrawingCache();
				
//				saveCurrentImage();
				
//				View decV = v.getWindow().getDecorView();
//				decV.setDrawingCacheEnabled(true);
//				decV.buildDrawingCache();
//				bmpV = decV.getDrawingCache();
//				
//				 FileOutputStream fos = null;    
//			        try {    
//			            fos = new FileOutputStream("/sdcard/pano.jpg");    
//			            if (null != fos) {    
//			                bmpV.compress(Bitmap.CompressFormat.PNG, 90, fos);    
//			                fos.flush();    
//			                fos.close();    
//			            }    
//			        } catch (FileNotFoundException e) {    
//			            e.printStackTrace();    
//			        } catch (IOException e) {    
//			            e.printStackTrace();    
//			        }
//				
//				ivImage.setImageBitmap(bmpV);
//				ivImage.setImageResource(R.drawable.panorama);
//				ivImage.setVisibility(View.VISIBLE);
				
				btnNextPic.setVisibility(View.INVISIBLE);
//				
//				if(bmpV!=null){
//					System.out.println("bmpV"+bmpV.getWidth()+"\t"+bmpV.getHeight());
//				}else{
//					System.out.println("bmpV null");
//				}
				
				nextScene(nextID);
//				
//				ivImage.setVisibility(View.VISIBLE);
//				btnNextPic.setVisibility(View.VISIBLE);
				
//				alpha = new AlphaAnimation(1.0f, 0.0f);
//				alpha.setFillAfter(true);
//				alpha.setDuration(1000);
//				alpha.setAnimationListener(new AnimationListener() {
//					
//					@Override
//					public void onAnimationStart(Animation animation) {
//						// TODO Auto-generated method stub
//					}
//					
//					@Override
//					public void onAnimationRepeat(Animation animation) {
//						// TODO Auto-generated method stub
//					}
//					
//					@Override
//					public void onAnimationEnd(Animation animation) {
//						ivImage.setVisibility(View.INVISIBLE);
//					}
//				});
//				ivImage.setAnimation(alpha);
//				alpha.start();
				
			}
		});
		
		ivImage = (ImageView)findViewById(R.id.iv_prev_image);
		
		handler = new ShowPanoHandler();
		handler.sendEmptyMessage(1);
	}

	@Override
    protected void onResume() {
		Log.d("myDebug", "ShowPanoActivity onResume()");
        super.onResume();
        view.onResume();
        
        leftAngle = view.setNewScene(mSc);
        
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAcce  = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagn  = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener(mAcceListener, mAcce, SensorManager.SENSOR_DELAY_UI);
        sm.registerListener(mMagnListener, mMagn, SensorManager.SENSOR_DELAY_UI);
        
        
        mRotateAngle = 0;
        Timer updateTimer = new Timer("UPDATEVIEW");
        tt = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				view.showAnimation();
				
				if(isCompassMode){//感应模式
					if(acceData!=null && magnData!=null){
						float[] values = new float[3];
						float[] valueR = new float[9];
						SensorManager.getRotationMatrix(valueR, null, acceData,
								magnData);
						SensorManager.getOrientation(valueR, values);

						values[0] = (float) Math.toDegrees(values[0]);
						values[0] = (values[0]+360)%360;
						values[1] = (float) Math.toDegrees(values[1]);// 滚动角
						values[2] = (float) Math.toDegrees(values[2]);// 俯仰角
							
						view.setAngleXYZ((int)(values[0]+90-leftAngle)%360 , -(int) values[1],
								(int) (values[2] + 90));

						mRotateAngle = values[0]-leftAngle;
						handler.sendEmptyMessage(0);
					} 
				}else{//触控模式
					if(Math.abs(mRateX)>=1){
						
						if(switchAnimation){
							if(Math.abs(mRateX)>20){
								mRotateAngle+=20*(mRateX/Math.abs(mRateX));
								mReduceX = 0.8f;
							}else{
								mReduceX = 0.9f;
								mRotateAngle += mRateX;
							}

							mRateX *= mReduceX;
							if(Math.abs(mRateX)<1){
								mRateX=0;
							}
						}else{
							mRotateAngle += mRateX;
							mRateX = 0;
						}
	
						mRotateAngle = (360+mRotateAngle)%360;
						view.setAngleX(-(int)mRotateAngle);

						handler.sendEmptyMessage(1);
					}
				}
			}
		};
		updateTimer.scheduleAtFixedRate(tt, 0, 100);
    }
 
    @Override
    protected void onPause() {
        super.onPause();
        view.onPause();
        if(tt!=null){
        	tt.cancel();
        }
        
        sm.unregisterListener(mAcceListener);
		sm.unregisterListener(mMagnListener);
    }

    // 旋转角速度
    private float mRotateAngle = 0;
    private float mRateX = 0;
    private float mReduceX = 0.9f;
    // 前一个xy轴的
 	private float mPreviousX;
 	private float mPreviousY;
 	// 触控变量
 	private int mode = 0;
 	private boolean switchAnimation = false;
 	// 触控类型
 	private static final int NONE = 0;
 	private static final int MOVE = 1;
 	private static final int ZOOM = 2;
 	// 滑动速度到圆柱转速的转换因子
 	private final float TOUCH_SCALE_FACTOR = 60.0f / 360;
 	@Override
 	public boolean onTouchEvent(MotionEvent e){
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (Math.abs(mRateX) > 2) {
				mRateX = (mRateX / Math.abs(mRateX)) * 2;
			}
			mPreviousX = e.getX();
			mPreviousY = e.getY();
			mode = MOVE;
			
			switchAnimation = false;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			switchAnimation = false;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			switchAnimation = false;
			mode = NONE;
			break;
		case MotionEvent.ACTION_UP:
			switchAnimation = true;
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == MOVE) {
				float x = e.getX();
				float y = e.getY();
				mRateX += ((x - mPreviousX) * TOUCH_SCALE_FACTOR);
				mPreviousX = x;
				mPreviousY = y;
			} else if (mode == ZOOM) {
			}
		}
		return true;
	}

 	private ShowPanoHandler handler = null;
	class ShowPanoHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0://感应模式下
				nearButton(mRotateAngle);
				break;
			case 1://触控模式下
				nearButton(360-mRotateAngle-90);
			}
            tvAngle.setText(""+mRotateAngle);
			super.handleMessage(msg);
		}
	}
	// 判断是否邻近Button
	private void nearButton(float angle){
		float temp = (angle + 90 + leftAngle) % 360;
		for (int i = 0; i < mSc.lstHotS.size(); i++) {
			HotSpot hs = mSc.lstHotS.get(i);
			if (Math.abs(hs.hsPanoOrien - temp) < 10) {
				btnNextPic.setVisibility(View.VISIBLE);
				nextID = hs.hsNextSceneID;
				break;
			} else {
				btnNextPic.setVisibility(View.INVISIBLE);
				nextID = -1;
			}
		}
	}
	
	// 获取下一个场景
	private void nextScene(int _id) {
		if (_id > -1) {
			mSc = VPSFile.getSceneByID(_id);
			int oldAngle = leftAngle;
			leftAngle = view.setNewScene(mSc);
			mRotateAngle = (mRotateAngle-oldAngle+leftAngle+360)%360;
			view.setAngleX(-(int)mRotateAngle);
            tvAngle.setText(""+mRotateAngle);
		}
	}
	
	private boolean isCompassMode = false;
	private final int MENU_COMPASS_MODE = Menu.FIRST;
	private final int MENU_TOUCH_MODE   = Menu.FIRST+1;
	private final int MENU_CARD_MODE    = Menu.FIRST+2;
	private final int MENU_SHOW_IN_MAP  = Menu.FIRST+3;
	private final int MENU_BACK         = Menu.FIRST+4;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true; 
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case MENU_COMPASS_MODE:
			isCompassMode = true;
			view.requestRender();
			break;
		case MENU_TOUCH_MODE:
			isCompassMode = false;
			view.requestRender();
			break;
		case MENU_CARD_MODE:
			Intent in = new Intent(ShowPanoActivity.this, CardBoardActivity.class);
			startActivity(in);
			break;
		case MENU_SHOW_IN_MAP:
//			Intent in = new Intent(ShowPanoActivity.this, LocMapActivity.class);
//			in.putExtra("LAT", lat);
//			in.putExtra("LON", lon);
//			startActivity(in);
			break;
		case MENU_BACK:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private double lat = -999;
	private double lon = -999;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		
		try {
			ExifInterface exif = new ExifInterface(mSc.sPanoFile);
			String str = exif.getAttribute(ExifInterface.TAG_MAKE);
			if(str!=null && !str.equals("")){
				try{
					String[] arrStr = str.split("\t");
					
					if(arrStr.length == 4){
						lat = Double.parseDouble(arrStr[2]);
						lon = Double.parseDouble(arrStr[3]);
					}
				}catch(Exception e){
				}
			}


			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(isCompassMode){
			menu.add(0, MENU_TOUCH_MODE,   0, "Touch Mode");
			menu.add(0, MENU_CARD_MODE,    0, "Cardboard Mode");
			if(lat!=-999 && lon!=-999){
				menu.add(0, MENU_SHOW_IN_MAP,  1, "Show in Map");
				menu.add(0, MENU_BACK,         2, "Back");
			}else{
				menu.add(0, MENU_BACK,         1, "Back");
			}
			
			
		}else{
			mRotateAngle = 0;
			menu.add(0, MENU_COMPASS_MODE, 0, "Sensor Mode");
			menu.add(0, MENU_CARD_MODE,    0, "Cardboard Mode");
			if(lat!=-999 && lon!=-999){
				menu.add(0, MENU_SHOW_IN_MAP,  1, "Show in Map");
				menu.add(0, MENU_BACK,         2, "Back");
			}else{
				menu.add(0, MENU_BACK,         1, "Back");
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
//	private void saveCurrentImage(){
////		View decV = view.getRootView();
////		decV.setDrawingCacheEnabled(true);
////		decV.buildDrawingCache();
////		bmpV = decV.getDrawingCache();
//		
//		FileOutputStream fos = null;
//		try {
//			fos = new FileOutputStream("/sdcard/pano02.png");
//			if (null != fos) {
//				bmpV.compress(Bitmap.CompressFormat.PNG, 90, fos);
//				fos.flush();
//				fos.close();
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
