package imobile.panorama.vsbrowser;

import imobile.panorama.util.Constants;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class PanoRender implements Renderer {
	//
	private Context mContext = null;
	
	private static GL10 glA;
	
	Cylinder myCyli;
    // mAngleX用于控制图像绕Y轴旋转
	public int mAngleX;
	public int mAngleY;
	public int mAngleZ;
	// mAngleY用于控制图像绕X轴旋转
    public float mMoveY;
    // mZoom缩放参数
    public float mZoom = 1.0f;
    
    public float mHeight = 0f;
    
    public boolean isBmp = true;
    public Bitmap bmp = null;
    

	/**
	 * 构造函数
	 * @param r 确定圆柱的粗细
	 * @param l 确定圆柱的长短
	 * @param n 确定圆柱的精度
	 */
	public PanoRender(Context _context, float r, float l, int n)
	{
		mContext = _context;
		myCyli = new Cylinder(r, l, n);
		
	}

	public void onDrawFrame(GL10 gl)
	{
		Log.d("myDebug", "PanoRender onDrawFrame()");
		
		glA = gl;
		
		if(isBmp){
			createTexture(gl);
			isBmp = false;
		}
		
		// 清除屏幕和深度缓存
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// 近似于重置功能，它将所选的矩阵状态恢复成原始状态，简单说就是将当前点移动到屏幕中心
		gl.glLoadIdentity();

		//
		myCyli.setArgument(mAngleX, mAngleY, mAngleZ, -mMoveY, mZoom);
		// 绘制圆柱面
		myCyli.drawFace(gl);
		
		
//		Bitmap bmp = SavePixels(0,0,800,400, gl);
//		FileOutputStream fos = null;
//		try {
//			fos = new FileOutputStream("/sdcard/pano05.png");
//			if (null != fos) {
//				bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
//				fos.flush();
//				fos.close();
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
		
	}

	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		Log.d("myDebug", "PanoRender onSurfaceChanged()");
		
		mHeight = height;
		
		float ratio = (float) width / height;
		// 设置opengl场景的大小
		gl.glViewport(0, 0, width, height);
		// 设置投影矩阵
		gl.glMatrixMode(GL10.GL_PROJECTION);
		// 重置当前的模型观察矩阵
		gl.glLoadIdentity();
		// 设置视口的大小
		gl.glFrustumf( -ratio,  ratio, -1, 1, 1.5f, 300f);
		// 选择模型观察矩阵
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		// 重置当前的模型观察矩阵
		gl.glLoadIdentity();
	}

	/**
	 * 创建时调用
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		Log.d("myDebug", "PanoRender onSurfaceCreated()");
		
		// 关闭图形抖动，否则灯光照射后没有光斑效果
		gl.glDisable(GL10.GL_DITHER);
		// 启用阴影平滑
		gl.glShadeModel(GL10.GL_SMOOTH);
		// 设置深度缓存
		gl.glClearDepthf(1.0f);
		// 启用深度缓存
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// 所作深度测试类型
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glEnable(GL10.GL_CULL_FACE);
		// 告诉系统对透视进行修正
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		gl.glLoadIdentity();
		// 环境的背景颜色：黑色
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	}

	static int mTextureID01;
	
	private void createTexture(GL10 gl)
	{
		
		IntBuffer intBuffer = IntBuffer.allocate(1);
		//创建纹理
		gl.glGenTextures(1, intBuffer);
		mTextureID01 = intBuffer.get();
		// 绑定纹理1
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID01);
		// 设置过滤滤波
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);//
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);//

		// 设置平铺与拉伸效果 GL10.GL_CLAMP_TO_EDGE拉伸，GL10.GL_REPEAT平铺
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_CLAMP_TO_EDGE);

		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
				GL10.GL_REPLACE);//
		
		
		if(bmp!=null){
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);
			bmp.recycle();
		}
		
		
	}
	
	
//	 public static Bitmap SavePixels(int x, int y, int w, int h)
//	    { 
//	        int b[]=new int[w*h];
//	        int bt[]=new int[w*h];
//	        IntBuffer ib=IntBuffer.wrap(b);
//	        ib.position(0);
//	        glA.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
//	        for(int i=0; i<h; i++)
//	        { 
//	              for(int j=0; j<w; j++)
//	              {
//	                  int pix=b[i*w+j];
//	                  int pb=(pix>>16)&0xff;
//	                  int pr=(pix<<16)&0x00ff0000;
//	                  int pix1=(pix&0xff00ff00) | pr | pb;
//	                  bt[(h-i-1)*w+j]=pix1;
//	              }
//	        }
//	        Bitmap sb=Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
//	        return sb;
//	    }
}
