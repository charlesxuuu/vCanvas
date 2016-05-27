package imobile.panorama.vsbrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import imobile.panorama.util.Constants;
import imobile.panorama.util.Scene;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ShowPanoSfView extends GLSurfaceView{
	// Context
	private Context mContext;
	// SurfaceView的画笔 
	private PanoRender mRenderer;
	// View所展示的Scene
	private Scene mSc;
	// 初始角度值
//	private int leftDAngle = -99;
//	private int dAngle;

	public ShowPanoSfView(Context context){
		super(context);
		mContext = context;
		mRenderer = new PanoRender(context, Constants.CYLINDER_R, Constants.CYLINDER_L, Constants.CYLINDER_N);
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public ShowPanoSfView(Context context,  AttributeSet attrs){
		super(context, attrs);
		mContext = context;
		mRenderer = new PanoRender(context, Constants.CYLINDER_R, Constants.CYLINDER_L, Constants.CYLINDER_N);
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	// 给视图设置新的场景
	public int setNewScene(Scene sc){
		mSc = sc;
		Bitmap bmp = null;
		int leftAngle = 0;
    	if(sc.isRelevance){
			try {
				// 先设置默认参数，若无法从配置文件中读取设置参数，则使用默认参数
				int width = Constants.BMPWIDTH;
				int height = Constants.BMPHEIGHT;
				
				// 读入参数
				ExifInterface exif = new ExifInterface(sc.sPanoFile);
				String str = exif.getAttribute(ExifInterface.TAG_MAKE);
				
				if(str!=null && !str.equals("")){
					try{
						String[] arrStr = str.split("\t");
						
						leftAngle = Integer.parseInt(arrStr[0]);
						
						System.out.println(sc.sName+"\tangle:\t"+leftAngle);
						
						Float coverPercent = Float.parseFloat(arrStr[1]);
						width = (int)(coverPercent*width);
					}catch(Exception e){
					}
				}
				//xcch
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 3;
				bmp = BitmapFactory.decodeFile(sc.sPanoFile, opts);
				bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
				int[] bmpPix = new int[width*height];
				bmp.getPixels(bmpPix, 0, width, 0, 0, width, height);
				
				mRenderer.bmp = Bitmap.createBitmap(Constants.BMPWIDTH, Constants.BMPHEIGHT, Bitmap.Config.ARGB_4444);
				mRenderer.bmp.setPixels(bmpPix, 0, width, 0, 0, width, height);
				mRenderer.isBmp = true;
//				isAnimation = true;
				requestRender();
				
				bmpPix = null;
			}catch(Exception e){
			}finally{
				
				bmp = null;
			}
		}
    	
//    	if(leftDAngle<0){
//    		dAngle = 0;
//    	}else{
//    		dAngle = leftDAngle-leftAngle;
//    	}
//    	leftDAngle = leftAngle;
    	
    	
    	return leftAngle;
	}
	
	// 触控模式下，只设一个值
	public void setAngleX(int _angle){
		mRenderer.mAngleX = _angle;
		mRenderer.mAngleY = 0;
		mRenderer.mAngleZ = 0;
		requestRender();
	}
	// 感应模式下，设置三个值
	public void setAngleXYZ(int x, int y, int z){
		mRenderer.mAngleX = x;
		mRenderer.mAngleY = y;
		mRenderer.mAngleZ = z;
		requestRender();
	}
	
//	private boolean isAnimation = false;
//	private Bitmap  prev = null;
//	private Bitmap  next = null;
//	private int n = 0;
//	private int count = 0;
//	public void showAnimation(){
//		count ++;
//		if(count>=5){
//			if(isAnimation){
//				Bitmap temp = null;
//				if(prev == null){
//					prev = next;
//					temp = prev;
//					isAnimation = false;
//				}else{
//					
//					Paint p = new Paint();
//					int num = 255 - n*30;
//					if(num<0){
//						num = 0;
//					}
//					p.setAlpha(num);
//					temp = Bitmap.createBitmap(next);
//					Canvas cv = new Canvas(temp);
//					
//					int length = (int) ((((360-dAngle)%360)/360f)*Constants.BMPWIDTH);
//					
//					Rect src = new Rect(length, 0, Constants.BMPWIDTH, Constants.BMPHEIGHT);
//					Rect det = new Rect(0, 0, Constants.BMPWIDTH-length, Constants.BMPHEIGHT);
//					cv.drawBitmap(prev, src, det, p);
//					
//					src.left = 0;
//					src.right = length;
//					det.left = det.right;
//					det.right = det.right+length;
//					cv.drawBitmap(prev, src, det, p);
//					
//					cv.save(Canvas.ALL_SAVE_FLAG);
//					cv.restore();
//				}
//				
//				mRenderer.bmp = temp;
//				mRenderer.isBmp = true;
//				requestRender();
//				
//				n++;
//				if(n>=10){
//					isAnimation = false;
//					prev = next;
//					n=0;
//				}
//			}
//			
//			count = 0;
//		}
//		
//	}
	
//	private int calcPosi(){
//		int width = Constants.BMPWIDTH*()
//	}
	
//	public int getInitAngle(){
//		return leftAngle;
//	}
}
