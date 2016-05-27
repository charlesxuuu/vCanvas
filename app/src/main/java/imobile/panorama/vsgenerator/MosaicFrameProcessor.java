package imobile.panorama.vsgenerator;

import android.util.Log;

public class MosaicFrameProcessor {
    private static final boolean LOGV = true;
    private static final String TAG = "MosaicFrameProcessor";
    private static final int NUM_FRAMES_IN_BUFFER = 2;						// 缓存区中帧的个数，即每两张做一次对比
    private static final int MAX_NUMBER_OF_FRAMES = 200;
    private static final int MOSAIC_RET_CODE_INDEX = 10;						// MOSAIC RETURN RESULT:OK/ERROR/CANCELLED/LOW_TEXTURE/FEW_INLIERS
    private static final int FRAME_COUNT_INDEX = 9;							// 合成帧的个数
    private static final int X_COORD_INDEX = 2;
    private static final int Y_COORD_INDEX = 5;
    private static final int HR_TO_LR_DOWNSAMPLE_FACTOR = 4;
    private static final int WINDOW_SIZE = 3;

    private Mosaic mMosaicer;													// Mosaic 对象
    private boolean mIsMosaicMemoryAllocated = false;							// 是否分配内存空间
    private final long[] mFrameTimestamp = new long[NUM_FRAMES_IN_BUFFER];	// 每帧的时间
    private float mTranslationLastX;
    private float mTranslationLastY;

    private int mFillIn = 0;													// 数组mFrameTimestamp的指针
    private int mTotalFrameCount = 0;											// 总计处理的帧数
    private long mLastProcessedFrameTimestamp = 0;
    private int mLastProcessFrameIdx = -1;
    private int mCurrProcessFrameIdx = -1;

    // Panning Rate: 平移速率，定义为每秒图像内容转换的百分比
    // 使用移动平均值来求平移速率
    private float mPanningRateX;
    private float mPanningRateY;

    private float[] mDeltaX = new float[WINDOW_SIZE];
    private float[] mDeltaY = new float[WINDOW_SIZE];
    private float[] mDeltaTime = new float[WINDOW_SIZE];
    private int mOldestIdx = 0;
    private float mTotalTranslationX = 0f;
    private float mTotalTranslationY = 0f;
    private float mTotalDeltaTime = 0f;

    private ProgressListener mProgressListener;

    private int mPreviewWidth;													// 预览窗口的宽度
    private int mPreviewHeight;												// 预览窗口的高度
    private int mPreviewBufferSize;											// 每张图片的缓存区（字节数）

    public interface ProgressListener {										// 进度监听器
        public void onProgress(boolean isFinished, float panningRateX, float panningRateY,
                float progressX, float progressY);
    }

    public MosaicFrameProcessor(int previewWidth, int previewHeight, int bufSize) {
        mMosaicer = new Mosaic();
        mPreviewWidth = previewWidth;
        mPreviewHeight = previewHeight;
        mPreviewBufferSize = bufSize;
    }

    public void setProgressListener(ProgressListener listener) {				// 设置进度监听器
        mProgressListener = listener;
    }

    public int reportProgress(boolean hires, boolean cancel) {				// 报告进度信息（0-100）
        return mMosaicer.reportProgress(hires, cancel);
    }

    public void initialize() {													// 初始化
        setupMosaicer(mPreviewWidth, mPreviewHeight, mPreviewBufferSize);		
        setStripType(Mosaic.STRIPTYPE_WIDE);									// WIDE 和 THIN 两种模式
        reset();
    }

    public void clear() {														// 释放由allocateMosaicMemory()函数生成的内存空间
        if (mIsMosaicMemoryAllocated) {
            mIsMosaicMemoryAllocated = false;
            mMosaicer.freeMosaicMemory();
        }
    }

    public void setStripType(int type) {										// 设置条状的类型
        mMosaicer.setStripType(type);
    }

    private void setupMosaicer(int previewWidth, int previewHeight, int bufSize) {
        Log.v(TAG, "setupMosaicer w, h=" + previewWidth + ',' + previewHeight + ',' + bufSize);
        mMosaicer.allocateMosaicMemory(previewWidth, previewHeight);			// 申请内存，用于Mosaic
        mIsMosaicMemoryAllocated = true;

        mFillIn = 0;
        if  (mMosaicer != null) {
            mMosaicer.reset();
        }
    }

    public void reset() {
        mTotalFrameCount = 0;
        mFillIn = 0;
        mLastProcessedFrameTimestamp = 0;
        mTotalTranslationX = 0;
        mTranslationLastX = 0;
        mTotalTranslationY = 0;
        mTranslationLastY = 0;
        mTotalDeltaTime = 0;
        mPanningRateX = 0;
        mPanningRateY = 0;
        mLastProcessFrameIdx = -1;
        mCurrProcessFrameIdx = -1;
        for (int i = 0; i < WINDOW_SIZE; ++i) {
            mDeltaX[i] = 0f;
            mDeltaY[i] = 0f;
            mDeltaTime[i] = 0f;
        }
        mMosaicer.reset();
    }

    public int createMosaic(boolean highRes) {										// 创建全景图，在输入完所需的帧之后
        return mMosaicer.createMosaic(highRes);
    }

    public byte[] getFinalMosaicNV21() {											// 获取合成好的数据（全景图）
        return mMosaicer.getFinalMosaicNV21();
    }
    
    public float[] setSourceImage(byte[] pixels) {									// 应用层给底层输入图片信息
        return mMosaicer.setSourceImage(pixels);
    }
    
    public void setBlendingType(int type) {										// 设置弯曲的类型
        mMosaicer.setBlendingType(type);
    }

    /**
     * 处理最近输入的图像帧，并且更新界面的进度条
     * 当完成之后，处理并显示最后的合成图
     */
    public void processFrame() {
        if (!mIsMosaicMemoryAllocated) {
            return;
        }
        long t1 = System.currentTimeMillis();
        mFrameTimestamp[mFillIn] = t1;

        mCurrProcessFrameIdx = mFillIn;
        mFillIn = ((mFillIn + 1) % NUM_FRAMES_IN_BUFFER);

        if (mCurrProcessFrameIdx != mLastProcessFrameIdx) {						// 始终保证是两个不同的帧在做检测
            mLastProcessFrameIdx = mCurrProcessFrameIdx;

            long timestamp = mFrameTimestamp[mCurrProcessFrameIdx];

            if (mTotalFrameCount < MAX_NUMBER_OF_FRAMES) {
                // 处理帧
                calculateTranslationRate(timestamp);

                // 输出进度信息
                if (mProgressListener != null) {
                    mProgressListener.onProgress(false, mPanningRateX, mPanningRateY,// false 表示未完成
                            mTranslationLastX * HR_TO_LR_DOWNSAMPLE_FACTOR / mPreviewWidth,
                            mTranslationLastY * HR_TO_LR_DOWNSAMPLE_FACTOR / mPreviewHeight);
                }
            } else {
                if (mProgressListener != null) {
                    mProgressListener.onProgress(true, mPanningRateX, mPanningRateY,// true 表示已经完成
                            mTranslationLastX * HR_TO_LR_DOWNSAMPLE_FACTOR / mPreviewWidth,
                            mTranslationLastY * HR_TO_LR_DOWNSAMPLE_FACTOR / mPreviewHeight);
                }
            }
        }
    }

    public void calculateTranslationRate(long now) {        					// 处理帧						
        float[] frameData = mMosaicer.setSourceImageFromGPU();					// 处理的函数
        
        int ret_code = (int) frameData[MOSAIC_RET_CODE_INDEX];
        
        if(ret_code>0){
        	 mTotalFrameCount  = (int) frameData[FRAME_COUNT_INDEX];
             float translationCurrX = frameData[X_COORD_INDEX];
             float translationCurrY = frameData[Y_COORD_INDEX];

             if (mLastProcessedFrameTimestamp == 0f) {
                 mTranslationLastX = translationCurrX;
                 mTranslationLastY = translationCurrY;
                 mLastProcessedFrameTimestamp = now;
                 return;
             }

             int idx = mOldestIdx;
             mTotalTranslationX -= mDeltaX[idx];
             mTotalTranslationY -= mDeltaY[idx];
             mTotalDeltaTime -= mDeltaTime[idx];
             mDeltaX[idx] = Math.abs(translationCurrX - mTranslationLastX);
             mDeltaY[idx] = Math.abs(translationCurrY - mTranslationLastY);
             mDeltaTime[idx] = (now - mLastProcessedFrameTimestamp) / 1000.0f;
             mTotalTranslationX += mDeltaX[idx];
             mTotalTranslationY += mDeltaY[idx];
             mTotalDeltaTime += mDeltaTime[idx];


             mPanningRateX = mTotalTranslationX /
                     (mPreviewWidth / HR_TO_LR_DOWNSAMPLE_FACTOR) / mTotalDeltaTime;
             mPanningRateY = mTotalTranslationY /
                     (mPreviewHeight / HR_TO_LR_DOWNSAMPLE_FACTOR) / mTotalDeltaTime;

             mTranslationLastX = translationCurrX;
             mTranslationLastY = translationCurrY;
             mLastProcessedFrameTimestamp = now;
             mOldestIdx = (mOldestIdx + 1) % WINDOW_SIZE;
        }
    }
}
