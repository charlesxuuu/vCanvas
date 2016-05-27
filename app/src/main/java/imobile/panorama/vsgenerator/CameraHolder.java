package imobile.panorama.vsgenerator;

import static imobile.panorama.vsgenerator.Util.Assert;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

public class CameraHolder {
    private Camera mCameraDevice;
    private long mKeepBeforeTime = 0;
    private final Handler mHandler;											// 先判断mUsers是否为0，若是则释放Camera对象
    private int mUsers = 0;  													// open()的个数减去release()的个数
    private int mCameraId = -1;  												// 当前的Camera的ID
    private int mBackCameraId = -1, mFrontCameraId = -1;						// 后置/前置Camera的ID

    private Parameters mParameters;												// 存储Camera的参数值，当程序确实打开Camera之后

    private static CameraHolder sHolder;										// 这里我们使用单例模式,使得当前只能打开一个摄像头
    public static synchronized CameraHolder instance() {						// 实例化sHolder对象
        if (sHolder == null) {
            sHolder = new CameraHolder();
        }
        return sHolder;
    }

    private static final int RELEASE_CAMERA = 1;								// HANDLER_ID
    private class ReleaseHandler extends Handler {							// ReleaseHandler
        ReleaseHandler(Looper looper) {
            super(looper);
        }

        @Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RELEASE_CAMERA:
				synchronized (CameraHolder.this) {
					if (CameraHolder.this.mUsers == 0)
						releaseCamera();
				}
				break;
			}
		}
    }

    private CameraHolder() {
        HandlerThread ht = new HandlerThread("CameraHolder");
        ht.start();
        mHandler = new ReleaseHandler(ht.getLooper());
        int mNumberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < mNumberOfCameras; i++) {
            CameraInfo mInfo = new CameraInfo();
            Camera.getCameraInfo(i, mInfo);
            if (mBackCameraId == -1 && mInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraId = i;
            }
            if (mFrontCameraId == -1 && mInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
            }
        }
    }


    public synchronized Camera open(int cameraId) throws Exception {			// open
        Assert(mUsers == 0);
        if (mCameraDevice != null && mCameraId != cameraId) {
            mCameraDevice.release();
            mCameraDevice = null;
            mCameraId = -1;
        }
        if (mCameraDevice == null) {
            try {
                mCameraDevice = Camera.open(cameraId);
                mCameraId = cameraId;
            } catch (RuntimeException e) {
                throw new Exception(e);
            }
            mParameters = mCameraDevice.getParameters();
        } else {
            try {
                mCameraDevice.reconnect();
            } catch (IOException e) {;
                throw new Exception(e);
            }
            mCameraDevice.setParameters(mParameters);
        }
        ++mUsers;
        mKeepBeforeTime = 0;
        return mCameraDevice;
    }


    public synchronized void release() {										// release
        Assert(mUsers == 1);
        --mUsers;
        mCameraDevice.stopPreview();
        releaseCamera();
    }
    private synchronized void releaseCamera() {
        Assert(mUsers == 0);
        Assert(mCameraDevice != null);
        long now = System.currentTimeMillis();
        if (now < mKeepBeforeTime) {
            mHandler.sendEmptyMessageDelayed(RELEASE_CAMERA,
                    mKeepBeforeTime - now);
            return;
        }
        mCameraDevice.release();
        mCameraDevice = null;
        mParameters = null;														// 这里也需要设置为空，因为它对Camera有个引用对象
        mCameraId = -1;
    }

    public int getBackCameraId() {
        return mBackCameraId;
    }

    public int getFrontCameraId() {
        return mFrontCameraId;
    }
}
