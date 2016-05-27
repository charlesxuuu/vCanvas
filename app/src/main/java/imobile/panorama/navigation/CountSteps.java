package imobile.panorama.navigation;

import android.hardware.Sensor;
import android.hardware.SensorManager;

public class CountSteps {
	private int MinThreshold = 0;
	private int MaxThreshold = 99999;
	
	// 时间
	private long lastTimeWave = 0;
	private long currentTimeWave = 0;
	// 在一个波周期内，判断是否有加速度向量大于一定的临界值
	private boolean isBigAngle = false;
	float[] preCoordinate;
	double currentTime = 0, lastTime = 0;
	float WALKING_THRESHOLD = 20;
	/* 步数计步器变量 */
	private boolean isFirstAcce = true;
	private boolean flag = true;
	private boolean isPreValley = false;
	// 步数
	private int countSteps = 0;
	// 加速度数据
	private float mAcceValue1;
	private float mAcceValue2;
	private float mAcceValue3;



	public CountSteps(int min, int max){
		MinThreshold = min;
		MaxThreshold = max;
	}
	
	
	public void initSteps(){
		countSteps = 0;
	}
	public int getSteps(){
		return countSteps;
	}
	
	public void identifySteps(float[] values){
		analyseData(values);
		
		float x = values[0];
		float y = values[1];
		float z = values[2];
		
		if(isFirstAcce){
			mAcceValue1 = mAcceValue2 = mAcceValue3 = (float)Math.sqrt(x*x+y*y+z*z);
			isFirstAcce = false;
		}else{
			mAcceValue3 = mAcceValue2;
			mAcceValue2 = mAcceValue1;
			mAcceValue1 = (float)Math.sqrt(x*x+y*y+z*z);
			
			if(mAcceValue2-9.8>1){
				isPreValley = true;
			}
			
			if(isPreValley && mAcceValue2-9.8<-1){//
				if(mAcceValue2 < mAcceValue1 && mAcceValue2 < mAcceValue3){
					if(flag){
						currentTimeWave = System.currentTimeMillis();
			 			isPreValley = false;
			 			flag = false;
			 		}else{
			 			currentTimeWave = System.currentTimeMillis();
			 			isPreValley = false;
			 			if(currentTimeWave - lastTimeWave > MinThreshold && currentTimeWave - lastTimeWave < MaxThreshold){
			 				countSteps++;
//							Intent intent = new Intent("COUNT_STEP");
//							intent.putExtra("ISSTEP", true);
//							sendBroadcast(intent);				 			
			 			}
			 		}
					lastTimeWave = currentTimeWave;
				}
			}
		}
	}

	
	/**
	 * 计步器使用的函数
	 */
	private void analyseData(float[] values){
		//获取当前时间
		currentTime = System.currentTimeMillis();
		//每隔200MS取加速度力和前一个进行比较
		if(currentTime-lastTime>200){
			if(preCoordinate == null){//还为存过数据
				preCoordinate = new float[3];
				for(int i=0;i<3;i++){
					preCoordinate[i] = values[i];
				}
			}else{//记录了原始坐标的话，就进行比较
				int angle = calculateAngle(values, preCoordinate);
				if(angle>WALKING_THRESHOLD){
					isBigAngle = true;
				}
				for(int i=0;i<3;i++){
					preCoordinate[i] = values[i];
				}
			}
			lastTime = currentTime;//重新计时
		}
	}
	
	private int calculateAngle(float[] newPoints, float[] oldPoints){
		int angle = 0;
		float vectorProduct = 0;		//向量积
		float newMold       = 0;        //新向量的模
		float oldMold       = 0; 		//旧向量的模
		for(int i=0;i<3;i++){
			vectorProduct +=
					newPoints[i]*oldPoints[i];
			newMold += newPoints[i]*newPoints[i];
			oldMold += oldPoints[i]*oldPoints[i];
		}
		newMold = (float)Math.sqrt(newMold);
		oldMold = (float)Math.sqrt(oldMold);
		// 计算夹角的余弦
		float cosAngle = (float)(vectorProduct/(newMold*oldMold));
		// 通过余弦值求角度值
		float fangle = (float)Math.toDegrees(Math.acos(cosAngle));
		angle = (int)fangle;
		return angle;// 返回向量的夹角
	}
}
