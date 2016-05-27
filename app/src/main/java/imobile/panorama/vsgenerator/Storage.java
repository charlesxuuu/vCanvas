package imobile.panorama.vsgenerator;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Storage {
    private static final String TAG = "CameraPictureStorage";

    public static String MOUNT_POINT = "/mnt/sdcard";

    public static String DIRECTORY = "/sdcard/PanoSpace";							// PanoSpace目录
	
    
    public static boolean saveImage(String title, byte[] jpeg){					// 文件名称，jpeg数据
        String path = generateFilepath(title);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(jpeg);
        } catch (Exception e) {
            Log.e(TAG, "Failed to write image", e);
            return false;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
        
        return true;
    }

    public static String generateFilepath(String title) {						// 生成存储图片的路径
        return DIRECTORY + '/' + title + ".jpg";
    }
    
    public static FileOutputStream createOutputFile(String fileName) {
		File dir = new File(DIRECTORY);
		if(!dir.exists() && !dir.isDirectory()){
			dir.mkdirs();
		}
		
		FileOutputStream fos = null;
		File file = new File(DIRECTORY, fileName);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fos = new FileOutputStream(file, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fos;
	}
}
