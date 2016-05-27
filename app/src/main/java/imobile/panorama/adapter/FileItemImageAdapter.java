package imobile.panorama.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;

import imobile.panorama.R;

public class FileItemImageAdapter extends ArrayAdapter<File> {

	Context mContext;
	
	int resource;
	
	int mWdith;
	int mHeight;

	public FileItemImageAdapter(Context context, int _resource,
			List<File> objects) {
		super(context, _resource, objects);
		mContext = context;
		resource = _resource;
		
		WindowManager mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(metrics);
		mWdith = metrics.widthPixels;
		mHeight = metrics.heightPixels;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LinearLayout rl;

		File file = getItem(position);

		if (convertView == null) {
			rl = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater) getContext()
					.getSystemService(inflater);
			vi.inflate(resource, rl, true);
		} else {

			rl = (LinearLayout) convertView;
		}
		
		

		ImageView iv = (ImageView)rl.findViewById(R.id.iv_vps_image_image);
		Bitmap bmp;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 30;
        opts.inPreferredConfig = Bitmap.Config.ARGB_4444;    // 默认是Bitmap.Config.ARGB_8888
        //opts.inPurgeable = true;
        //opts.inInputShareable = true;


        bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
		
		int width = mWdith;
		int height = (int)((float)mHeight/5f);
		// 读入参数
		ExifInterface exif;
		try {
			exif = new ExifInterface(file.getAbsolutePath());
			String str = exif.getAttribute(ExifInterface.TAG_MAKE);

			if (str != null && !str.equals("")) {
				try {
					String[] arrStr = str.split("\t");

					Float coverPercent = Float.parseFloat(arrStr[1]);
					if(coverPercent>0 && coverPercent<1){
						width = (int) (coverPercent * width);
					}
				} catch (Exception e) {
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
		
		iv.setImageBitmap(Bitmap.createScaledBitmap(bmp, width, height, true));
		bmp.recycle();
		bmp = null;
		return rl;
	}
}
