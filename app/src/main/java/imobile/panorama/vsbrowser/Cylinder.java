package imobile.panorama.vsbrowser;

import imobile.panorama.util.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Cylinder {
	private FloatBuffer mVertexsBuffer;//顶点buffer
	private ShortBuffer mFaceOrderBuffer;//柱面顶点buffer
	private FloatBuffer mTexcoordsBuffer;//纹理坐标

	private int n;// 圆柱面的精细成都
	private float rotX = 90;
	private float rotY = 0;
	private float rotZ = 0;
	private float moveY = 0;
	private float zoomZ = 1.0f;

	public Cylinder(float r, float l, int n)
	{
		super();
		this.n = n;
		getVers(r, l);
		getOrder();
		getTeccoords();
	}
	
	public void setArgument(float x0, float x1, float x2, float y, float _zoom)
	{
		rotX = x0;
		rotY = x1;
		rotZ = x2;
		moveY = y;
		zoomZ = _zoom;
	}

	/**
	 * @see 计算出顶点坐标
	 * @see 假设圆柱的中心点为（0，0，0）
	 * @param r确定圆柱的粗细
	 * @param l确定圆柱的长短
	 * @param n确定圆柱的精度(圆柱面分成等分)
	 */
	private void getVers(float r, float l)
	{
		//n = 6;
		float[] mVertexsArray = new float[n * 3 * 2 + 6];
		float angle = (float) ((1.0f / n) * 2 * Math.PI);// 单位圆心角度

		// 圆柱各顶点坐标
		for (int i = 0; i < n; i++)
		{
			mVertexsArray[i * 3] = (float) Math.cos(angle * i) * r;
			mVertexsArray[i * 3 + 1] = l;
			mVertexsArray[i * 3 + 2] = (float) Math.sin(angle * i) * r;
			
			mVertexsArray[i * 3 + 3 * n] = (float) Math.cos(angle * i) * r;
			mVertexsArray[i * 3 + 1 + 3 * n] = -l;
			mVertexsArray[i * 3 + 2 + 3 * n] = (float) Math.sin(angle * i) * r;
		}
		// 上底面圆心 第2n+1个点
		mVertexsArray[2 * n * 3] = 0;
		mVertexsArray[2 * n * 3 + 1] = l;
		mVertexsArray[2 * n * 3 + 2] = 0;
		// 下底面圆心 第2n+2个点
		mVertexsArray[2 * n * 3 + 3] = 0;
		mVertexsArray[2 * n * 3 + 4] = -l;
		mVertexsArray[2 * n * 3 + 5] = 0;
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(mVertexsArray.length*8);
		vbb.order(ByteOrder.nativeOrder());
		mVertexsBuffer = vbb.asFloatBuffer();
		mVertexsBuffer.put(mVertexsArray);
		mVertexsBuffer.position(0);
	}

	/**
	 * @see 顶点绘制顺序
	 * @param n
	 */
	private void getOrder()
	{

		short[] stripArray = new short[n * 4];

		// 生成圆柱表面顶点绘制顺序
		for (short i = 0; i < n; i++)
		{
			stripArray[i * 4] = i;
			stripArray[i * 4 + 1] = (short) (i + n);
			if (i == n - 1)
			{
				stripArray[i * 4 + 2] = (short) (i + 1 - n);
				stripArray[i * 4 + 3] = (short) (i + 1);
			} else
			{

				stripArray[i * 4 + 2] = (short) (i + 1);
				stripArray[i * 4 + 3] = (short) (i + n +  1);
			}
		}

		ByteBuffer fbb = ByteBuffer.allocateDirect(stripArray.length*2);
		fbb.order(ByteOrder.nativeOrder());
		mFaceOrderBuffer = fbb.asShortBuffer();
		mFaceOrderBuffer.put(stripArray);
		mFaceOrderBuffer.position(0);
		
		
	}

	/**
	 * 纹理映射数据
	 */
	public void getTeccoords()
	{

		float[] texcoords = new float[n * 2 * 2];
		float temp = 1.0f / n;

		for (int i = 0; i < n; i++)
		{
			texcoords[i * 2] = i * temp;
			texcoords[i * 2 + 1] = 0;

			texcoords[i * 2 + n * 2] = i * temp;
			texcoords[i * 2 + n * 2 + 1] = 1;
		}

		ByteBuffer tbb = ByteBuffer.allocateDirect(texcoords.length*8);
		tbb.order(ByteOrder.nativeOrder());
		mTexcoordsBuffer = tbb.asFloatBuffer();
		mTexcoordsBuffer.put(texcoords);
		mTexcoordsBuffer.position(0);
	}

	/**
	 * @see 绘制圆柱面
	 * @param gl
	 */
	public void drawFace(GL10 gl)
	{
		//开启2D纹理贴图功能
		gl.glEnable(GL10.GL_TEXTURE_2D);
		//重置
		gl.glLoadIdentity();

		//设置要使用的纹理
		gl.glBindTexture(GL10.GL_TEXTURE_2D, PanoRender.mTextureID01);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);//开启顶点设置功能
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);//开启纹理     
		
		//放大做小
		gl.glScalef(1f, 1f, zoomZ);
		//旋转
		gl.glRotatef(rotZ, 1, 0, 0);
		gl.glRotatef(rotY, 0, 0, 1);
		gl.glRotatef(rotX+Constants.ROTOTE_PLUS, 0, 1, 0);
		//上下移动
        gl.glTranslatef(0, moveY, 0);
        
        
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexsBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexcoordsBuffer);

		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP,
				4*n, GL10.GL_UNSIGNED_SHORT,
				mFaceOrderBuffer);
		

		gl.glFinish();
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

		gl.glRotatef(-90, 0, 1, 0);
		
		//关闭2D纹理贴图功能
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
}
