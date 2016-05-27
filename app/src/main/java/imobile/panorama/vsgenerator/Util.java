package imobile.panorama.vsgenerator;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.hardware.Camera;


public class Util {
    private static final int OPEN_RETRY_COUNT = 2;								// 打开相机时尝试的次数

    // Orientation hysteresis amount used in rounding, in degrees
    public static final int ORIENTATION_HYSTERESIS = 5;

    public static final String REVIEW_ACTION = "com.android.camera.action.REVIEW";

    //
    private Util() {
    }

    public static void Assert(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static android.hardware.Camera openCamera(Activity activity, int cameraId)// 打开照相机
            throws Exception {
        DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        if (dpm.getCameraDisabled(null)) {										// null 检查所有可能的admins
            throw new Exception();
        }
        for (int i = 0; i < OPEN_RETRY_COUNT; i++) {
	        try {
	            return CameraHolder.instance().open(cameraId);
	        } catch (Exception e) {
	        	if (i == 0) {
	        		try {
	        			Thread.sleep(1000);										// 等待一秒钟时间
	        		} catch(InterruptedException ie) {
	        			
	        		}
	        		continue;
	        	} else {
	        		throw e;
	        	}
	        }
        }
        throw new Exception(new RuntimeException("Should never get here"));// 若未获取，直接抛出异常
    }


    public static int getDisplayOrientation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static int getCameraOrientation(int cameraId) {					// 应该是获得的照片相对于屏幕的角度值
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info.orientation;
    }
    
    
    
    public static String createName(String format, long dateTaken) {			// 根据时间命名全景图
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }
}
