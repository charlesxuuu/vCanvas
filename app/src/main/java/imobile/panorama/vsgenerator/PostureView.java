package imobile.panorama.vsgenerator;

import imobile.panorama.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class PostureView extends View{
	
	private Context mContext;
	
	// 左下角矩形以及对应所使用的画笔
	private Paint mPaintRectBlue;
	private Rect  rectBlue;
	private Paint mPaintRectGreen;
	private Rect  rectGreen;
//	private boolean isDrawRG = false;
//	private boolean isDrawGreen  = false;
//	private float prevDirec;
//	private float direction;
	private float eRectLength;
	// 图片
	Paint mPaint = null;
	// 水平仪图片资源的声明
	private float  middleTopBound;
	private boolean isTopOk = true;
	private Bitmap topBitmapRect;
	private Bitmap topBmpRectOrange;
	private float  middleLeftBound;
	private boolean isLeftOk = true;
	private Bitmap leftBitmapRect;
	private Bitmap leftBitmapRectOrange;
	private Bitmap bmpBall;
	// 移动速度控制
	private boolean isMoveFast = false;
	private Paint   mPaintFast  = null;
	private Paint   mPaintFastText = null;
	private Rect    mRectFast   = null;
	private String  strMovingFast = "";

	public PostureView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		
		mPaintRectBlue = new Paint();
		mPaintRectBlue.setAntiAlias(true);
		mPaintRectBlue.setStrokeWidth(3);
		mPaintRectBlue.setColor(Color.BLUE);
		mPaintRectBlue.setStyle(Paint.Style.STROKE);
		
		mPaintRectGreen = new Paint();
		mPaintRectGreen.setAntiAlias(true);
		mPaintRectGreen.setStrokeWidth(3);
		mPaintRectGreen.setColor(Color.GREEN);
		mPaintRectGreen.setStyle(Paint.Style.FILL);
		
		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);//消除锯齿
        
        mPaintFast = new Paint();
        mPaintFast.setAntiAlias(true);
        mPaintFast.setStrokeWidth(10);
        mPaintFast.setColor(Color.RED);
        mPaintFast.setStyle(Paint.Style.STROKE);
        mPaintFast.setTextAlign(Align.CENTER);
        
        mPaintFastText = new Paint();
        mPaintFastText.setAntiAlias(true);
        mPaintFastText.setStrokeWidth(10);
        mPaintFastText.setColor(Color.RED);
        mPaintFastText.setTextSize(50);
        mPaintFastText.setTextAlign(Align.CENTER);
        
        
        strMovingFast = mContext.getResources().getString(R.string.moving_fast);
        
	}

	// View的高度和宽度
	private int mWidth;
	private int mHeight;
	private int leftWidth;
	private int leftHeight;
	private int topWidth;
	private int topHeight;
	private int ballR;
	
	// 移动/每度
	private float eLeftLength;
	private float eTopLength;
	
	// 矩形的长度和高度
	private int rectWidth = 0;
	private int rectHeight = 0;
	
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		
		mWidth = w;
		mHeight = h;
		
		rectWidth = (int)(mHeight*0.8);
		rectHeight = (int)(mHeight*0.3);
		rectBlue = new Rect(0, rectWidth, rectHeight, mHeight);
		
		mRectFast = new Rect(0, 0, mWidth, mHeight);
		
		
		topWidth = (int)(0.4*mHeight);
		topHeight = (int)(0.125*mHeight);
		
		leftWidth = (int)(0.125*mHeight);
		leftHeight = (int)(0.4*mHeight);
		
		ballR = (int)(0.04*mHeight);
		
		eLeftLength = (leftHeight-2*ballR)/90f;
		eTopLength  = (topWidth-2*ballR)/90f;
		
		eRectLength = (mHeight*0.3f*0.7f)/40;
		
		middleTopBound = ballR/eTopLength;
		middleLeftBound = ballR/eLeftLength;
		
		initBitmap();
		initLocation();
	}
	
	private void initBitmap(){
		topBitmapRect = BitmapFactory.decodeResource(getResources(), R.drawable.orien_up_pipe);
		topBitmapRect = Bitmap.createScaledBitmap(topBitmapRect, topWidth, topHeight, true);
		topBmpRectOrange = BitmapFactory.decodeResource(getResources(), R.drawable.orien_up_pipe_orange);
		topBmpRectOrange = Bitmap.createScaledBitmap(topBmpRectOrange, topWidth, topHeight, true);
		leftBitmapRect = BitmapFactory.decodeResource(getResources(), R.drawable.orien_left_pipe);
		leftBitmapRect = Bitmap.createScaledBitmap(leftBitmapRect, leftWidth, leftHeight, true);
		leftBitmapRectOrange = BitmapFactory.decodeResource(getResources(), R.drawable.orien_left_pipe_orange);
		leftBitmapRectOrange = Bitmap.createScaledBitmap(leftBitmapRectOrange, leftWidth, leftHeight, true);
		bmpBall = BitmapFactory.decodeResource(getResources(), R.drawable.orien_middle_ball);
		bmpBall = Bitmap.createScaledBitmap(bmpBall, 2*ballR, 2*ballR, true);
	}
	
	//背景矩形的位置声明
	int top_x ;
	int top_y ;
	int left_x ;
	int left_y ;
	//水泡的位置声明
	int top_ball_x;
	float topDx  = 0;
	int top_ball_y;
	float topDy  = 0;
	int left_ball_x;
	float leftDx = 0;
	int left_ball_y;
	float leftDy = 0;
	
	private void initLocation(){
		top_x = (int)((mWidth-topWidth)*0.5);
		top_y = 10;
		left_x = 10;
		left_y = (int) ((mHeight-leftHeight)*0.5);
		
		top_ball_x = top_x+(int)((topWidth*0.5f-ballR));
		top_ball_y = top_y+(int)(topHeight*0.5f-ballR);
		left_ball_x = left_x+(int)(leftWidth*0.5f-ballR);
		left_ball_y = left_y+(int)(leftHeight*0.5f-ballR);
	}
	
	// 设置全景图完成百分比
	public void setPercnet(float p){
		if((int)(rectHeight*p)>0){
			rectGreen = new Rect(0, rectWidth, (int)(rectHeight*p), mHeight);
		}
		postInvalidate();
	}
	
	// 设置移动是否过快的boolean值
	public void setIsFast(boolean b){
		isMoveFast = b;	
	}
	
	// 设置姿态数据
	public void setData(float[] data) {
		float roll = data[1];
		float pitch = (data[2]+90)%360;
		
		// 计算水平仪中小球偏移的距离
		isTopOk = false;
		if (Math.abs(roll) < 45) {
			topDx = roll * eTopLength;
			if(Math.abs(roll)<middleTopBound){
				isTopOk = true;
			}
		} else if (roll >= 45) {
			topDx = 44.5f * eTopLength;
		} else {
			topDx = -44.5f * eTopLength;
		}
		isLeftOk = false;
		if (Math.abs(pitch) < 45) {
			leftDy = pitch * eLeftLength;
			if(Math.abs(pitch)<middleLeftBound){
				isLeftOk = true;
			}
		} else if (pitch >= 45) {
			leftDy = 44.5f * eLeftLength;
		} else {
			leftDy = -44.5f * eLeftLength;
		}
		
		postInvalidate();
	}
	

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		
		// 代表整幅全景图
		canvas.drawRect(rectBlue, mPaintRectBlue);
		
		// 画实心的绿色矩形图，代表拼接的范围
		if(rectGreen!=null){
			canvas.drawRect(rectGreen, mPaintRectGreen);
		}
		
		//画柱的背景图
		if(isTopOk){
			canvas.drawBitmap(topBitmapRect,top_x,top_y, mPaint);
		}else{
			canvas.drawBitmap(topBmpRectOrange,top_x,top_y, mPaint);
		}
		
		if(isLeftOk){
			canvas.drawBitmap(leftBitmapRect, left_x,left_y,mPaint);
		}else{
			canvas.drawBitmap(leftBitmapRectOrange, left_x,left_y,mPaint);
		}
				
		//画气泡
		canvas.drawBitmap(bmpBall, top_ball_x+topDx, top_ball_y+topDy, mPaint);
		canvas.drawBitmap(bmpBall, left_ball_x+leftDx, left_ball_y+leftDy, mPaint);
			
				
		//画标尺
		canvas.drawLine((int)(top_x+(topWidth*0.5f-1.5*ballR)), top_y, (int)(top_x+(topWidth*0.5f-1.5*ballR)), top_y+topHeight, mPaint);
		canvas.drawLine((int)(top_x+(topWidth*0.5f+1.5*ballR)), top_y, (int)(top_x+(topWidth*0.5f+1.5*ballR)), top_y+topHeight, mPaint);
		canvas.drawLine(left_x, (int)(left_y+(leftHeight*0.5f-1.5*ballR)), left_x+leftWidth, (int)(left_y+(leftHeight*0.5f-1.5*ballR)), mPaint);
		canvas.drawLine(left_x, (int)(left_y+(leftHeight*0.5f+1.5*ballR)), left_x+leftWidth, (int)(left_y+(leftHeight*0.5f+1.5*ballR)), mPaint);
	
		if(isMoveFast){
			canvas.drawRect(mRectFast, mPaintFast);
			canvas.drawText(strMovingFast, mWidth*0.5f, mHeight*0.5f, mPaintFastText);
		}
		
	}
}
