package imobile.panorama.navigation;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class NaviService extends Service {
	private final IBinder binder = new MyBinder();			// 在onBind()中返回binder对象
	
	// 统计步数
	private CountSteps cs = null;
	// 步长
	private float stepLength = 0.4f;// 默认单位是米
	// 统计方向数据
	private CountOrientation co = null;
	
	/* Sensor数据 */ 
//	private float[] arrOrien = new float[3];				// 方向数据来自加速度和磁方向传感器
	/* Sensor对象 */
	private SensorManager sm = null;
	private Sensor acce;
	private Sensor magne;

    private Sensor stepdetector; //chix

    private float[] acceData = new float[3];
	private float[] magnData = new float[3];

    private int stepData; //chix

	private final SensorEventListener sensorListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {		
				cs.identifySteps(event.values);
				acceData[0] = event.values[0];
				acceData[1] = event.values[1];
				acceData[2] = event.values[2];
			} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magnData[0] = event.values[0];
				magnData[1] = event.values[1];
				magnData[2] = event.values[2];
			} else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {//chix
                stepData++;
/*                Context context = getApplicationContext();
                CharSequence text = "step:" + stepData;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();*/
            }
		}
	};

	/* 用于计步数和统计方向值的任务 */
	private TimerTask tt = null;

	/* 屏幕锁接收器对象, 屏幕关闭后，传感器停止工作 */ 
	private BroadcastReceiver b_r = new Screen_Off_BroadcaseReceiver();
	
	private SharedPreferences sp = null;

	/**
	 * @ SensorRecordService onCreate()
	 */
	@Override
	public void onCreate() {
		Log.d("myDebug", "SensorRecordService onCreate()");
		
		super.onCreate();
		
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String str = sp.getString("PREF_LST_SENSITIVITY", "1");
		int which = Integer.parseInt(str);
		int min = 300, max = 800;
		switch(which){
		case 0:
			min = 200;
			max = 1000;
			break;
		case 1:
			min = 300;
			max = 800;
			break;
		case 2:
			min = 400;
			max = 600;
			break;
		}
		cs = new CountSteps(min,max);
		co = new CountOrientation();
		
		stepLength = Float.valueOf(sp.getString("PREF_ET_STEP_LENGTH", "0.4"));

		/*初始化传感器管理对象*/ 
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		acce = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sm.registerListener(sensorListener, acce, SensorManager.SENSOR_DELAY_UI);
		magne = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		sm.registerListener(sensorListener, magne,
				SensorManager.SENSOR_DELAY_UI);
        stepdetector = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);//chix
        sm.registerListener(sensorListener, stepdetector,
                SensorManager.SENSOR_DELAY_UI);//chix

		/* 注册监听器 */ 
		registerReceiver(b_r, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	/**
	 * SensorRecordService onBind() bind服务时会调用该方法
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("myDebug", "SensorRecordService onBind()");

		/* 执行定期任务---定期的获取传感器数据，用于推测当前轨迹信息 */ 
		Timer updatetimer = new Timer("COLLECTDATA");
		tt = new TimerTask() {
			@Override
			public void run() {

				if (acceData != null && magnData != null) {
					float[] values = new float[3];
					float[] valueR = new float[9];
					SensorManager.getRotationMatrix(valueR, null, acceData,
							magnData);
					SensorManager.getOrientation(valueR, values);

					values[0] = (float) Math.toDegrees(values[0]);
					values[1] = (float) Math.toDegrees(values[1]);
					values[2] = (float) Math.toDegrees(values[2]);

					co.setData((values[0] + 360) % 360);
				}
			}
		};
		updatetimer.scheduleAtFixedRate(tt, 1000, 100);

		return binder;
	}

	/**
	 * SensorRecordService onUnbind(Intent intent);
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("myDebug", "SensorRecordService onUnbind()");
		if (tt != null) {
			tt.cancel();
		}
		return super.onUnbind(intent);
	}
	
	/**
	 * SensorRecordService onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		// 注销广播接收器
		unregisterReceiver(b_r);
		// 注销传感器监听器
		sm.unregisterListener(sensorListener);
	}

	/** 获取移动的步数 */
	//public float getLength(){	return cs.getSteps()*stepLength;}
    public float getLength(){	return stepData;}//chix

	
	/** 获取移动的方向 */
	public float getOrien(){
		return co.getOrien();
	}

	
	/**
	 * @ 绑定服务时，通过该类返回SensorRecordService对象
	 * 
	 * @author Administrator
	 */
	public class MyBinder extends Binder {
		public NaviService getService() {
			return NaviService.this;
		}
	}

	/**
	 * @ 屏幕关的广播接收器
	 */
	class Screen_Off_BroadcaseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			sm.unregisterListener(sensorListener);
			sm.registerListener(sensorListener, acce,
					SensorManager.SENSOR_DELAY_UI);
		}
	}
	
}
