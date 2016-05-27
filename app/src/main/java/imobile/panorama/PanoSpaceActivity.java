package imobile.panorama;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import imobile.panorama.util.Constants;
import imobile.panorama.vsbrowser.VSBrowserActivity;
import imobile.panorama.vseditor.VSEditorActivity;
import imobile.panorama.vsgenerator.VSGeneratorActivity;

public class PanoSpaceActivity extends Activity {
	private final int MENU_SETTING = Menu.FIRST;
	private final int MENU_HELP = Menu.FIRST+1;
	private final int MENU_ABOUT = Menu.FIRST+2;
	private final int MENU_EXIT = Menu.FIRST+3;
	
	// 共享首选项设置
	private final int PREF_SETTING = 0;
	
	// UI 控件
	private Button btnCreatePano = null;
	private Button btnVSEditor = null;
	private Button btnBrowser = null;
	private ImageView ivPano  = null;
	private TimerTask tt = null;
	private int        curPosi = 0;
	private List<File> lstPanos = null;
	private Drawable[] layers = new Drawable[2];

	// boolean值，判断SDcard是否挂载
	// 如果没有SDcard卡，则无法读取和存储全景图信息
	private boolean isMediaMounted = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.main);
        
        ivPano = (ImageView)findViewById(R.id.iv_panoramas);
        layers[1] = (Drawable)getResources().getDrawable(R.drawable.showcase);
        ivPano.setImageDrawable(layers[1]);
        btnCreatePano = (Button)findViewById(R.id.main_btn_camera);
        btnCreatePano.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isMediaMounted){
					Intent in = new Intent(PanoSpaceActivity.this, VSGeneratorActivity.class);
					in.putExtra(Constants.INTENT_SINGLE_PANO, true);
					startActivity(in);
				}
			}
		});
        btnVSEditor = (Button)findViewById(R.id.main_btn_vseditor);
        btnVSEditor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isMediaMounted){
					Intent in = new Intent(PanoSpaceActivity.this, VSEditorActivity.class);
					startActivity(in);
				}
				
			}
		});
        btnBrowser = (Button)findViewById(R.id.main_btn_browser);
        btnBrowser.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent in = new Intent(PanoSpaceActivity.this, VSBrowserActivity.class);
				startActivity(in);
			}
		});
       
        //判断SDcard是否挂载成功
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
        	File file = new File(Constants.PANODIR);
        	if(!file.exists() && !file.isDirectory()){
        		file.mkdir();
        	}
        	file = new File(Constants.VPSFILEDIR);
        	if(!file.exists() && !file.isDirectory()){
        		file.mkdir();
        	}
        	isMediaMounted = true;
        }else{
        	isMediaMounted = false;
        }
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        registerReceiver(brSDcardListener, filter);
    }
    
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		lstPanos = new ArrayList<File>();
		File filePanos = new File(Constants.PANODIR);
		if(filePanos.exists()){
			File[] files = filePanos.listFiles();	
			for(int i=0;i<files.length;i++){
				if(files[i].getName().endsWith(".jpg")){
					lstPanos.add(files[i]);
				}
			}
		}
		
		
		if(lstPanos.size()>0){
			Timer updateTimer = new Timer("UpdatePano");
			tt = new TimerTask(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(layers[0]!=null){
								layers[0].setCallback(null);
								layers[0] = null;
							}
							layers[0] = layers[1];
							layers[1] = scaleImage(Drawable.createFromPath(lstPanos.get(curPosi).getAbsolutePath()), (float)0.5);
							TransitionDrawable td = new TransitionDrawable(layers);
							ivPano.setImageDrawable(td);
							td.startTransition(1000);
							curPosi = (curPosi+1)%lstPanos.size();
						}
						
					});
				}
			};
			updateTimer.scheduleAtFixedRate(tt, 5000, 5000);
		}
	}
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(tt!=null){
			tt.cancel();
		}
	}

	@Override
   	protected void onDestroy() {
   		// TODO Auto-generated method stub
   		super.onDestroy();
   		unregisterReceiver(brSDcardListener);
   	}
       
   	private final BroadcastReceiver brSDcardListener = new BroadcastReceiver() {
   		@Override
   		public void onReceive(Context context, Intent intent) {
   			// TODO Auto-generated method stub
   			String str = intent.getAction();
   			if(str.equals(Intent.ACTION_MEDIA_MOUNTED)){
   				isMediaMounted = true;
   			}else{
   				isMediaMounted = false;
   			}
   		}
   	};

   	@Override
   	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
   		// TODO Auto-generated method stub
   		super.onActivityResult(requestCode, resultCode, data);
   		switch(requestCode){
   		case PREF_SETTING:
   			break;
   		}
   	}

   	@Override
   	public boolean onCreateOptionsMenu(Menu menu) {
   		// TODO Auto-generated method stub
   		super.onCreateOptionsMenu(menu);
   		menu.add(0, MENU_SETTING, 0, R.string.setting).setIcon(android.R.drawable.ic_menu_preferences);
   		menu.add(0, MENU_HELP,    1, R.string.help).setIcon(android.R.drawable.ic_menu_help);
   		menu.add(0, MENU_ABOUT,   2, R.string.about).setIcon(android.R.drawable.ic_menu_info_details);
   		menu.add(0, MENU_EXIT,    3, R.string.exit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
   		return true;
   	}

   	@Override
   	public boolean onOptionsItemSelected(MenuItem item) {
   		// TODO Auto-generated method stub
   		switch(item.getItemId()){
   		case MENU_SETTING:
   			Intent in = new Intent(PanoSpaceActivity.this, PrefActivity.class);
   			startActivityForResult(in, PREF_SETTING);
   			break;
   		case MENU_HELP:
   			break;
   		case MENU_ABOUT:
   			break;
   		case MENU_EXIT:
   			finish();
   			break;
   		}
   		return super.onOptionsItemSelected(item);
   	}


    private Drawable scaleImage (Drawable image, float scaleFactor) {

        if ((image == null) || !(image instanceof BitmapDrawable)) {
            return image;
        }

        Bitmap b = ((BitmapDrawable)image).getBitmap();

        int sizeX = Math.round(image.getIntrinsicWidth() * scaleFactor);
        int sizeY = Math.round(image.getIntrinsicHeight() * scaleFactor);

        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false);

        image = new BitmapDrawable(getResources(), bitmapResized);

        return image;

    }
}