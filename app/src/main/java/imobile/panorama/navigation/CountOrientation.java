package imobile.panorama.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;


public class CountOrientation {
	
	private final int n = 10;
	private int countNumber = 0;
	private float[] arrNumber = new float[10];
	
	/* 惯性导航数据 */
	private List<Integer> lstOrien = new ArrayList<Integer>();	// 方向
	

	public CountOrientation(){
		lstOrien.clear();
	}
	
	
	public void setData(float orien){
		countNumber++;
		
		arrNumber[countNumber-1]=orien;
		
		if(countNumber%n==0){
			lstOrien.add(calcMiddleNumber(arrNumber));
			countNumber = 0;
		}
	}
	
	public float getOrien(){
		int n = lstOrien.size();
		float[] arr = new float[n];
		for(int i=0;i<n;i++){
			arr[i] = lstOrien.get(i);
		}
		
		/* 求平均值和方差 */ 
		float average = 0;
		average = calcMiddleNumber(arr);
		lstOrien.clear();
		
		return average;
	}
	
	private int calcMiddleNumber(float[] arr_num){
		float result = 99999999999f;
		int   position = -1;
		float temp = 0f;
		for(int i=0;i<360;i++){
			temp = 0f;
			for(int j=0;j<arr_num.length;j++){
				float tempDiff = Math.abs(i-arr_num[j]);
				if(tempDiff>180){
					tempDiff=360-tempDiff;
				}
				temp+=tempDiff;
			}
			if(result>temp){
				position = i;
				result = temp;
			}
		}
		return position;
	}
}
