package imobile.panorama.vsgenerator;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.ConditionVariable;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class MosaicRendererSurfaceView extends GLSurfaceView {
    private static final boolean DEBUG = false;
    private MosaicRendererSurfaceViewRenderer mRenderer;							// Renderer
    private ConditionVariable mPreviewFrameReadyForProcessing;
    private boolean mIsLandscapeOrientation = true;								// 是否为Landscape（横屏）方向

    /* 以下三个是构造函数 */
    public MosaicRendererSurfaceView(Context context) {
        super(context);
        initialize(context, false, 0, 0);
    }

    public MosaicRendererSurfaceView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
        initialize(context, false, 0, 0);
    }

    public MosaicRendererSurfaceView(Context context, boolean translucent,
            int depth, int stencil) {
        super(context);
        initialize(context, translucent, depth, stencil);
    }

    
    private void initialize(Context context, boolean translucent, int depth, int stencil) {
        getDisplayOrientation(context);
        init(translucent, depth, stencil);
        setZOrderMediaOverlay(true);
    }

    private void getDisplayOrientation(Context context) {						// 获取屏幕的显示方向
        Activity activity = (VSGeneratorActivity) context;  
        mIsLandscapeOrientation = (activity.getRequestedOrientation()
                == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
    }

    private void init(boolean translucent, int depth, int stencil) {

        /* 默认的GLSurfaceView()构建一个RGB_565不透明的表层
         * 如果想要一个透明的表层，则使用PixelFormat.TRANSLUCENT来改变GL Surfaces
         */
        if (translucent) {
            this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }

        setEGLContextFactory(new ContextFactory());

        setEGLConfigChooser(
            translucent ? new ConfigChooser(8, 8, 8, 8, depth, stencil) :
            new ConfigChooser(5, 6, 5, 0, depth, stencil));

        
        mRenderer = new MosaicRendererSurfaceViewRenderer(mIsLandscapeOrientation);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);	// RENDERMODE_WHEN_DIRTY: mRenderer只有在Surface被创建的时候或者requestRenderer()的时候被调用
        mPreviewFrameReadyForProcessing = new ConditionVariable();
    }

    private static class ContextFactory implements GLSurfaceView.EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            checkEglError("Before eglCreateContext", egl);
            int[] attribList = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            EGLContext context = egl.eglCreateContext(
                display, eglConfig, EGL10.EGL_NO_CONTEXT, attribList);
            checkEglError("After eglCreateContext", egl);
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    private static void checkEglError(String prompt, EGL10 egl) {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            Log.e("myDebug", String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser {

        public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
            mRedSize = r;
            mGreenSize = g;
            mBlueSize = b;
            mAlphaSize = a;
            mDepthSize = depth;
            mStencilSize = stencil;
        }

        private static final int EGL_OPENGL_ES2_BIT = 4;
        private static final int[] CONFIG_ATTRIBUTES =
        {
            EGL10.EGL_RED_SIZE, 4,
            EGL10.EGL_GREEN_SIZE, 4,
            EGL10.EGL_BLUE_SIZE, 4,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_NONE
        };

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

            int[] numConfig = new int[1];
            egl.eglChooseConfig(display, CONFIG_ATTRIBUTES, null, 0, numConfig);

            int numConfigs = numConfig[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }

            /* 最低限度的匹配 EGL configs */
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, CONFIG_ATTRIBUTES, configs, numConfigs, numConfig);

            if (DEBUG) {
                 printConfigs(egl, display, configs);
            }
            
            // 返回最好的结果
            return chooseConfig(egl, display, configs);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);

                if (d < mDepthSize || s < mStencilSize)
                    continue;

                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                            EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                            EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);

                if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                    return config;
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }

        private void printConfigs(EGL10 egl, EGLDisplay display,
            EGLConfig[] configs) {
            int numConfigs = configs.length;
            for (int i = 0; i < numConfigs; i++) {
                printConfig(egl, display, configs[i]);
            }
        }

        private void printConfig(EGL10 egl, EGLDisplay display,
                EGLConfig config) {
            int[] attributes = {
                    EGL10.EGL_BUFFER_SIZE,
                    EGL10.EGL_ALPHA_SIZE,
                    EGL10.EGL_BLUE_SIZE,
                    EGL10.EGL_GREEN_SIZE,
                    EGL10.EGL_RED_SIZE,
                    EGL10.EGL_DEPTH_SIZE,
                    EGL10.EGL_STENCIL_SIZE,
                    EGL10.EGL_CONFIG_CAVEAT,
                    EGL10.EGL_CONFIG_ID,
                    EGL10.EGL_LEVEL,
                    EGL10.EGL_MAX_PBUFFER_HEIGHT,
                    EGL10.EGL_MAX_PBUFFER_PIXELS,
                    EGL10.EGL_MAX_PBUFFER_WIDTH,
                    EGL10.EGL_NATIVE_RENDERABLE,
                    EGL10.EGL_NATIVE_VISUAL_ID,
                    EGL10.EGL_NATIVE_VISUAL_TYPE,
                    0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                    EGL10.EGL_SAMPLES,
                    EGL10.EGL_SAMPLE_BUFFERS,
                    EGL10.EGL_SURFACE_TYPE,
                    EGL10.EGL_TRANSPARENT_TYPE,
                    EGL10.EGL_TRANSPARENT_RED_VALUE,
                    EGL10.EGL_TRANSPARENT_GREEN_VALUE,
                    EGL10.EGL_TRANSPARENT_BLUE_VALUE,
                    0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                    0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                    0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                    0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                    EGL10.EGL_LUMINANCE_SIZE,
                    EGL10.EGL_ALPHA_MASK_SIZE,
                    EGL10.EGL_COLOR_BUFFER_TYPE,
                    EGL10.EGL_RENDERABLE_TYPE,
                    0x3042 // EGL10.EGL_CONFORMANT
            };
            String[] names = {
                    "EGL_BUFFER_SIZE",
                    "EGL_ALPHA_SIZE",
                    "EGL_BLUE_SIZE",
                    "EGL_GREEN_SIZE",
                    "EGL_RED_SIZE",
                    "EGL_DEPTH_SIZE",
                    "EGL_STENCIL_SIZE",
                    "EGL_CONFIG_CAVEAT",
                    "EGL_CONFIG_ID",
                    "EGL_LEVEL",
                    "EGL_MAX_PBUFFER_HEIGHT",
                    "EGL_MAX_PBUFFER_PIXELS",
                    "EGL_MAX_PBUFFER_WIDTH",
                    "EGL_NATIVE_RENDERABLE",
                    "EGL_NATIVE_VISUAL_ID",
                    "EGL_NATIVE_VISUAL_TYPE",
                    "EGL_PRESERVED_RESOURCES",
                    "EGL_SAMPLES",
                    "EGL_SAMPLE_BUFFERS",
                    "EGL_SURFACE_TYPE",
                    "EGL_TRANSPARENT_TYPE",
                    "EGL_TRANSPARENT_RED_VALUE",
                    "EGL_TRANSPARENT_GREEN_VALUE",
                    "EGL_TRANSPARENT_BLUE_VALUE",
                    "EGL_BIND_TO_TEXTURE_RGB",
                    "EGL_BIND_TO_TEXTURE_RGBA",
                    "EGL_MIN_SWAP_INTERVAL",
                    "EGL_MAX_SWAP_INTERVAL",
                    "EGL_LUMINANCE_SIZE",
                    "EGL_ALPHA_MASK_SIZE",
                    "EGL_COLOR_BUFFER_TYPE",
                    "EGL_RENDERABLE_TYPE",
                    "EGL_CONFORMANT"
            };
            int[] value = new int[1];
            for (int i = 0; i < attributes.length; i++) {
                int attribute = attributes[i];
                String name = names[i];
                if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
                	
                } else {
                    while (egl.eglGetError() != EGL10.EGL_SUCCESS);
                }
            }
        }

        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];
    }

    public void lockPreviewReadyFlag() {
        mPreviewFrameReadyForProcessing.close();
    }

    private void unlockPreviewReadyFlag() {
        mPreviewFrameReadyForProcessing.open();
    }

    public void waitUntilPreviewReady() {
        mPreviewFrameReadyForProcessing.block();
    }

    public void setReady() {
        queueEvent(new Runnable() {

            @Override
            public void run() {
                mRenderer.setReady();
            }
        });
    }

    public void preprocess(final float[] transformMatrix) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.preprocess(transformMatrix);
            }
        });
    }

    public void transferGPUtoCPU() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.transferGPUtoCPU();
                unlockPreviewReadyFlag();
            }
        });
    }

    public void setWarping(final boolean flag) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.setWarping(flag);
            }
        });
    }

    public MosaicRendererSurfaceViewRenderer getRenderer() {
        return mRenderer;
    }

}
