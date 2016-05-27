package imobile.panorama.vsgenerator;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MosaicRendererSurfaceViewRenderer implements GLSurfaceView.Renderer
{
    private static final String TAG = "MosaicRendererSurfaceViewRenderer";
    // boolean变量，用于判断屏幕是否为肖像模式
    private boolean mIsLandscapeOrientation;
    // 监听器
    private MosaicSurfaceCreateListener mSurfaceCreateListener;

    public MosaicRendererSurfaceViewRenderer(boolean isLandscapeOrientation) {
        mIsLandscapeOrientation = isLandscapeOrientation;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        MosaicRenderer.step();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        if (mSurfaceCreateListener != null) {
            mSurfaceCreateListener.onMosaicSurfaceCreated(MosaicRenderer.init());
        }
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        MosaicRenderer.reset(width, height, mIsLandscapeOrientation);
        Log.i(TAG, "Renderer: onSurfaceChanged");
        if (mSurfaceCreateListener != null) {
            mSurfaceCreateListener.onMosaicSurfaceChanged();
        }
    }

    // 给MosaicSurfaceCreateListener对象赋值
    public void setMosaicSurfaceCreateListener(MosaicSurfaceCreateListener listener) {
        mSurfaceCreateListener = listener;
    }

    public void setReady() {
        MosaicRenderer.ready();
    }

    public void preprocess(float[] transformMatrix) {
        MosaicRenderer.preprocess(transformMatrix);
    }

    public void transferGPUtoCPU() {
        MosaicRenderer.transferGPUtoCPU();
    }

    public void setWarping(boolean flag) {
        MosaicRenderer.setWarping(flag);
    }
    
    public interface MosaicSurfaceCreateListener {
        public void onMosaicSurfaceCreated(final int surface);
        public void onMosaicSurfaceChanged();
    }
}
