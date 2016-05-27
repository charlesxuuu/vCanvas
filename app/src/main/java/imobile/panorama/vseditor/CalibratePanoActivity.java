package imobile.panorama.vseditor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import imobile.panorama.R;
import imobile.panorama.util.Constants;
import imobile.panorama.util.HotSpot;
import imobile.panorama.util.Scene;
import imobile.panorama.util.VPSFile;
import imobile.panorama.vsbrowser.ShowPanoSfView;

public class CalibratePanoActivity extends Activity{
	// 当前场景
	private Scene mSc = null;
	// UI控件
	private ShowPanoSfView sfvPano = null;
	private int leftAngle = 0;
	private ImageButton btnNext = null;
	private ImageButton btnLeft = null;
	private ImageButton btnRight = null;
	// TimerTask
	private TimerTask tt = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mAngle = 0;
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.calibratepano);
		sfvPano = (ShowPanoSfView)findViewById(R.id.sfv_cali_pano);
		sfvPano.requestFocus();
		sfvPano.setFocusableInTouchMode(true);
		btnNext = (ImageButton)findViewById(R.id.btn_cali_nextpic);
		btnLeft = (ImageButton)findViewById(R.id.btn_cali_moveleft);
		btnLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mHS.hsPanoOrien = (mHS.hsPanoOrien+1)%360;
				mAngle = (mAngle-1)%360;
				sfvPano.setAngleX(-(int)mAngle);
			}
		});
		btnRight = (ImageButton)findViewById(R.id.btn_cali_moveright);
		btnRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mHS.hsPanoOrien = (mHS.hsPanoOrien-1)%360;
				mAngle = (mAngle+1)%360;
				sfvPano.setAngleX(-(int)mAngle);
			}
		});
		
		Intent in = getIntent();
		int id = in.getIntExtra(Constants.SCENEID, -1);
		if(id!=-1){
			mSc = VPSFile.getSceneByID(id);
			leftAngle = sfvPano.setNewScene(mSc);
			
			handler = new ShowPanoHandler();
			handler.sendEmptyMessage(0);
		}else{
			Toast.makeText(CalibratePanoActivity.this, "全景图读取失败", Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	@Override
    protected void onResume() {
		Log.d("myDebug", "ShowPanoActivity onResume()");
        super.onResume();
        sfvPano.onResume();
        
        Timer updateTimer = new Timer("UPDATEVIEW");
        tt = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(Math.abs(mRateX)>=1){
					mAngle += mRateX;
					mAngle = (mAngle+360)%360;
					
					mRateX *= mReduceX;
					
					if(Math.abs(mRateX)<1){
						mRateX=0;
					}
					sfvPano.setAngleX(-(int)mAngle);
					handler.sendEmptyMessage(0);
				}
			}
		};
		updateTimer.scheduleAtFixedRate(tt, 0, 100);
    }
 
    @Override
    protected void onPause() {
        super.onPause();
        sfvPano.onPause();
        if(tt!=null){
        	tt.cancel();
        }
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	// 旋转角速度
    private float mAngle = 0;
    private float mRateX = 0;
    private final float mReduceX = 0.6f;
    // 前一个xy轴的
 	private float mPreviousX;
 	private float mPreviousY;
 	// 触控变量
 	private int mode = 0;
 	// 触控类型
 	private static final int NONE = 0;
 	private static final int MOVE = 1;
 	private static final int ZOOM = 2;
 	// 滑动速度到圆柱转速的转换因子
 	private final float TOUCH_SCALE_FACTOR = 100.0f / 360;
 	@Override
 	public boolean onTouchEvent(MotionEvent e)
 		{
 			switch (e.getAction() & MotionEvent.ACTION_MASK)
 			{
 			case MotionEvent.ACTION_DOWN:
 				if(Math.abs(mRateX)>2){
 					mRateX = (mRateX/Math.abs(mRateX))*2;
 				}
 				mPreviousX = e.getX();
 				mPreviousY = e.getY();
 				mode = MOVE;
 				break;
 			case MotionEvent.ACTION_POINTER_DOWN:
 				break;
 			case MotionEvent.ACTION_POINTER_UP:
 			case MotionEvent.ACTION_UP:
 				mode = NONE;
 				break;
 			case MotionEvent.ACTION_MOVE:
 				if(mode == MOVE){
 					float x = e.getX();
 					float y = e.getY();
 					mRateX += ((x - mPreviousX)*TOUCH_SCALE_FACTOR);
 					mPreviousX = x;
 					mPreviousY = y;
 				}else if(mode == ZOOM){
 				}
 			}
 			return true;
 		}
    
 	private ShowPanoHandler handler = null;
	class ShowPanoHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0://触控模式
				nearButton((360-mAngle)%360);
				break;
			}
			super.handleMessage(msg);
		}
	}
    private HotSpot mHS = null;
	// 判断是否邻近Button
	private void nearButton(float angle){
		float temp = (angle+leftAngle)%360;
		for (int i = 0; i < mSc.lstHotS.size(); i++) {
			HotSpot hs = mSc.lstHotS.get(i);
			if (Math.abs(hs.hsPanoOrien - temp) < 10) {
				btnNext.setVisibility(View.VISIBLE);
				btnLeft.setVisibility(View.VISIBLE);
				btnRight.setVisibility(View.VISIBLE);
				mHS = hs;
				break;
			} else {
				btnNext.setVisibility(View.INVISIBLE);
				btnLeft.setVisibility(View.INVISIBLE);
				btnRight.setVisibility(View.INVISIBLE);
				mHS = null;
			}
		}
	}
	
	// ****************************************
	// Menu选项
	// ****************************************
	private final int MENU_DELETEPANO = Menu.FIRST;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_DELETEPANO, 0, "删除全景图").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case MENU_DELETEPANO:
			mSc.isRelevance = false;
			mSc.sPanoFile = "";
			finish();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
}
