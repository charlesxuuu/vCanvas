package imobile.panorama.vseditor;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import imobile.panorama.util.*;

public class VSPathView extends View{
	
	private Context mContext;
	
	private Paint   mPaintPoint;
	private RadialGradient mRadiGradient = null;
	private Paint   mPaintLine;
	private Paint   mPaintText;
	private Paint   mPaintBGLine;
	private Paint   mPaintAnnulus;
	// 是否绘画背景网格
	private boolean isDrawBGLine = false;
	
	private float dValueX = 0;
	private float dValueY = 0;
	public VSPathView(Context context) {
		super(context);
		mContext = context;
		
		mPaintPoint = new Paint();
		mPaintPoint.setAntiAlias(true);
		mPaintPoint.setColor(Color.GREEN);
		mPaintPoint.setStrokeWidth(3);
		
		mPaintLine = new Paint();
		mPaintLine.setAntiAlias(true);
		mPaintLine.setStyle(Paint.Style.FILL);
		mPaintLine.setStrokeWidth(5);
		mPaintLine.setColor(Color.RED);
		
		mPaintText = new Paint();
		mPaintText.setAntiAlias(true);
		mPaintText.setColor(Color.BLACK);
		mPaintText.setStrokeWidth(3);
		mPaintText.setTextSize(Constants.R);
		mPaintText.setTextAlign(Align.CENTER);
		
		mPaintBGLine = new Paint();
		mPaintBGLine.setAntiAlias(true);
		mPaintBGLine.setColor(Color.BLACK);
		mPaintBGLine.setAlpha(50);
		mPaintBGLine.setStrokeWidth(1);
		
		mPaintAnnulus = new Paint();
		mPaintAnnulus.setAntiAlias(true);
		mPaintAnnulus.setStrokeWidth(5);
		mPaintAnnulus.setStyle(Paint.Style.STROKE);
		mPaintAnnulus.setColor(Color.GREEN);
	}
	
	public VSPathView(Context context,  AttributeSet attrs){
		super(context ,attrs);
		mContext = context;
		mPaintPoint = new Paint();
		mPaintPoint.setAntiAlias(true);
		mPaintPoint.setColor(Color.GREEN);
		mPaintPoint.setStrokeWidth(3);
		
		mPaintLine = new Paint();
		mPaintLine.setAntiAlias(true);
		mPaintLine.setStyle(Paint.Style.FILL);
		mPaintLine.setStrokeWidth(5);
		mPaintLine.setColor(Color.RED);
		
		mPaintText = new Paint();
		mPaintText.setAntiAlias(true);
		mPaintText.setColor(Color.BLACK);
		mPaintText.setStrokeWidth(3);
		mPaintText.setTextSize(Constants.R);
		mPaintText.setTextAlign(Align.CENTER);
		
		mPaintBGLine = new Paint();
		mPaintBGLine.setAntiAlias(true);
		mPaintBGLine.setColor(Color.BLACK);
		mPaintBGLine.setAlpha(50);
		mPaintBGLine.setStrokeWidth(1);
		
		mPaintAnnulus = new Paint();
		mPaintAnnulus.setAntiAlias(true);
		mPaintAnnulus.setStrokeWidth(5);
		mPaintAnnulus.setStyle(Paint.Style.STROKE);
		mPaintAnnulus.setColor(Color.GREEN);
	}
	
	// 当前选择的ID
	private int mTouchID = -1;
	public void setTouchID(int _id){
		mTouchID = _id;
		invalidate();
	}
	
	// 判断是否绘画背景图
	public void setIsDrawBGLine(boolean _b){
		isDrawBGLine = _b;
		invalidate();
	}
	
	/** 横纵坐标系X和Y位移数据变量 */
	public void initDValue(float _x, float _y){		//初始化坐标，中心坐标
		dValueX = _x;
		dValueY = _y;
		invalidate();
	}
	public void setDValue(float _x, float _y){    	//坐标位移累加
		dValueX += _x;
		dValueY += _y;
		invalidate();
	}
	public float getDValueX(){						//获取X轴的坐标位移值
		return dValueX;
	}
	public float getDValueY(){						//获取Y轴的坐标位移值
		return dValueY;
	}
	
	// 放大缩小操作
	public void scaleBig(){
		for(int i=0;i<VPSFile.getSceneNum();i++){
			Scene sc = VPSFile.getSceneByI(i);
			sc.sX = 2*sc.sX;
			sc.sY = 2*sc.sY;
		}
		postInvalidate();
	}
	public void scaleSmall(){
		for(int i=0;i<VPSFile.getSceneNum();i++){
			Scene sc = VPSFile.getSceneByI(i);
			sc.sX = 0.5f*sc.sX;
			sc.sY = 0.5f*sc.sY;
		}
		postInvalidate();
	}
	
	
	private float width;
	private float height;
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawColor(Color.TRANSPARENT);
		
		if(isDrawBGLine){
			float interval = width/20;
		
			for(int i=0;i<width;i+=interval){
				canvas.drawLine(i, 0, i, height, mPaintBGLine);
			}
			for(int j=0;j<height;j+=interval){
				canvas.drawLine(0, j, width, j, mPaintBGLine);
			}
		}
		
		for(int i=0;i<VPSFile.getSceneNum();i++){
			Scene sc = VPSFile.getSceneByI(i);
			if(sc.isRelevance){
				mPaintPoint.setStyle(Paint.Style.FILL);
			}else{
				mPaintPoint.setStyle(Paint.Style.STROKE);
			}

			mRadiGradient = new RadialGradient(sc.sX+dValueX, sc.sY+dValueY, Constants.R, 
					new int[]{Color.argb(255, 0, 139, 149) ,Color.argb(255, 0, 245, 255)},
					null, Shader.TileMode.REPEAT);
			mPaintPoint.setShader(mRadiGradient);
			
			canvas.drawCircle(sc.sX+dValueX, sc.sY+dValueY, Constants.R, mPaintPoint);
			
			//画ID值
			canvas.drawText(sc.sID+"", sc.sX+dValueX,//(float)(pp.x-AutoPanoApp.R/Math.sqrt(2))
					sc.sY+(Constants.R*0.5f)+dValueY, mPaintText);//(float)( pp.y+AutoPanoApp.R/Math.sqrt(2))
			
			if(sc.sID == mTouchID){
				canvas.drawCircle(sc.sX+dValueX, sc.sY+dValueY, Constants.R+6, mPaintAnnulus);
			}
		}

		for(int i=0;i<VPSFile.getSceneNum();i++){
			Scene psc = VPSFile.getSceneByI(i);
			for(int j=0;j<psc.lstHotS.size();j++){
				if(psc.lstHotS.get(j).hsNextSceneID>i){
					Scene nsc = VPSFile.getSceneByID(psc.lstHotS.get(j).hsNextSceneID);
					canvas.drawLine(psc.sX+dValueX, psc.sY+dValueY, nsc.sX+dValueX, nsc.sY+dValueY, mPaintLine);
				}
			}
		}
	}
}
