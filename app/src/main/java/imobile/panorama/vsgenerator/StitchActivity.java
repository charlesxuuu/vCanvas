package imobile.panorama.vsgenerator;

import imobile.panorama.R;
import imobile.panorama.util.Constants;
import imobile.panorama.util.Scene;
import imobile.panorama.util.VPSFile;
import imobile.panorama.vsbrowser.ShowPanoActivity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class StitchActivity extends Activity {
	public static final String BR_STITCH_FINISH = "STITCH_FINISH";
	
	private boolean isSinglePano = true;										// 判断是单独生成一个全景图文件，还是在虚拟空间中生成一个全景图文件
	private int seconds = 0;													// 拼接所消耗时间
	private TimerTask tt = null;
	/*UI控件*/ 
	private String strElapsedTime;
	private TextView tvSec = null;
	private ProgressBar pbar = null;
	
	private StitchBR br = new StitchBR();										// BroadcastReceiver
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.stitch);
		tvSec = (TextView)findViewById(R.id.tv_stitch_time);
		strElapsedTime = getResources().getString(R.string.elapsed_time)+":";
		tvSec.setText(strElapsedTime+0+"(s)");
		pbar  = (ProgressBar)findViewById(R.id.pbar_stitch);
		
		Intent in = getIntent();
		isSinglePano = in.getBooleanExtra(Constants.INTENT_SINGLE_PANO, false);
		
		registerReceiver(br, new IntentFilter(BR_STITCH_FINISH));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Timer timer = new Timer("SECOND");
		tt = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tvSec.setText(strElapsedTime + seconds + "(s)");
					}
				});
				seconds++;
			}
		};
		timer.scheduleAtFixedRate(tt, 0, 1000);
	}


	@Override
	protected void onPause() {
		super.onPause();
		if (tt != null) {
			tt.cancel();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(br);
	}
	
	
	class StitchBR extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			boolean result = intent.getBooleanExtra("SAVE_RESULT", false);
			if (result) {
				String str = intent.getStringExtra(Constants.INTENT_FILEDIR);
				if (str != null && !str.equals("")) {
					if (isSinglePano) {
						VPSFile.clear();										// 打开Activity展示全景图
						VPSFile.setVPSName("");
						Scene sc = new Scene();
						sc.sID = 0;
						sc.sName = "SHOW";
						sc.sX = sc.sY = 0;
						sc.lstHotS.clear();
						sc.isRelevance = true;
						sc.sPanoFile = str;
						VPSFile.addNewScene(sc, null);

						Intent in = new Intent(StitchActivity.this,
								ShowPanoActivity.class);						// 导向展示页面
						startActivity(in);
					} 
				}
			}else{
				Toast.makeText(StitchActivity.this, "合成或者存储图片失败，请重试", Toast.LENGTH_LONG).show();
			}
			finish();
		}
	}	
}
	