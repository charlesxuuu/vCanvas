                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          package imobile.panorama.vseditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import imobile.panorama.R;
import imobile.panorama.adapter.FileItemImageAdapter;
import imobile.panorama.navigation.NaviService;
import imobile.panorama.util.Constants;
import imobile.panorama.util.Path;
import imobile.panorama.util.Scene;
import imobile.panorama.util.VPSFile;
import imobile.panorama.vsgenerator.VSGeneratorActivity;


public class VSEditorActivity extends Activity implements Handler.Callback, OnSharedPreferenceChangeListener, OnClickListener{
	/* 系统使用的常量 */
	private final int DIALOG_SAVE_VPS    = 0;
	private final int DIALOG_RESAVE_VPS  = 1;
	private final int DIALOG_NAVISTART   = 2;
	private final int DIALOG_NAVIGATING  = 3;
	private final int DIALOG_NAVIEND     = 4;
	private final int DIALOG_CHOOSESCENE = 5;
	private final int DIALOG_CHOOSEPANO  = 6;
	private final int DIALOG_EXISTPANOS  = 7;
	
	
	private int mId = -1;														// 场景计数器（即场景ID）
	
	
	private VSPathView vspv = null;												// 控件对象
	private LinearLayout llVse = null;
	private Button  btnPano = null;
	private Button  btnNavi = null;
	private Button  btnDele = null;
	private Button  btnSave = null;
	private LinearLayout llMoveSc = null;
	private ImageButton  btnTop = null;
	private ImageButton  btnLeft = null;
	private ImageButton  btnRight = null;
	private ImageButton  btnBottom = null;
	private ImageButton  btnScaleBig = null;
	private ImageButton  btnScaleSmall = null;
	
	/* 惯性导航数据 */
	private final float stepSize = 5f;											// 步长
	private int  stepNumber = 0;												// 惯性导航---步数
	private float pathDirection;												// 惯性导航---移动方向
	private int  currentID;														//
	private int  nextID;														//
	
	
	private DisplayMetrics dMetrics = null;										// 界面展示的度量标准
	private float displayWidth;													// 界面宽度
	private float displayHeight;												// 界面高度
	
	/* 用于绑定服务的对象 */
	private NaviService srsBinder = null;
	private boolean  unbindornot  = false;
	private ServiceConnection sc = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			srsBinder = ((NaviService.MyBinder) service).getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) { }
	};
	
	
	private Handler myHandler = null;
	
	private SharedPreferences sp = null;										// 本地共享首选项
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("myDebug", "VSEditorActivity onCreate()");
		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		dMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
		displayWidth = dMetrics.widthPixels;
		displayHeight = dMetrics.heightPixels;
		
		
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());// 获取系统的默认属性值
		sp.registerOnSharedPreferenceChangeListener(this);
		
		
		setContentView(R.layout.vseditor);										// 获取控件对象
		vspv  = (VSPathView)findViewById(R.id.vspv_draw);
		vspv.setBackgroundResource(R.drawable.staticmap);
		//https://maps.googleapis.com/maps/api/staticmap?center=49.2767968,-122.9146341&zoom=19&size=600x300

		llVse = (LinearLayout)findViewById(R.id.ll_vse_oper);
		btnPano = (Button)findViewById(R.id.btn_vse_pano);
		btnPano.setOnClickListener(this);
		btnNavi = (Button)findViewById(R.id.btn_vse_navi);
		btnNavi.setOnClickListener(this);
		btnDele = (Button)findViewById(R.id.btn_vse_delete);
		btnDele.setOnClickListener(this);
		btnSave = (Button)findViewById(R.id.btn_vse_save);
		btnSave.setOnClickListener(this);
		llMoveSc = (LinearLayout)findViewById(R.id.ll_vse_movescene);
		btnLeft = (ImageButton)findViewById(R.id.btn_vse_moveleft);
		btnLeft.setOnClickListener(this);
		btnRight = (ImageButton)findViewById(R.id.btn_vse_moveright);
		btnRight.setOnClickListener(this);
		btnTop = (ImageButton)findViewById(R.id.btn_vse_movetop);
		btnTop.setOnClickListener(this);
		btnBottom = (ImageButton)findViewById(R.id.btn_vse_movebottom);
		btnBottom.setOnClickListener(this);
		btnScaleBig = (ImageButton)findViewById(R.id.btn_vse_scale_big);
		btnScaleBig.setOnClickListener(this);
		btnScaleSmall = (ImageButton)findViewById(R.id.btn_vse_scale_small);
		btnScaleSmall.setOnClickListener(this);
		
		// 设置是否画背景图
		vspv.setIsDrawBGLine(sp.getBoolean(Constants.PREF_CB_DRAWGRID, false));
		VPSFile.vspv = vspv;
		vspv.initDValue(displayWidth*0.5f, displayHeight*0.5f);
		
		makeNewScense();
		
		myHandler = new Handler(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		clear();
	}
	// 清除函数
	private void clear(){
		vspv.initDValue(displayWidth*0.5f, displayHeight*0.5f);					// 设置坐标原点位置为屏幕中心点
		VPSFile.clear();														// 清空VPSFile，装在新的数据
		mId = -1;																// 初始化场景编号
		setTouchID(-1);															// 当前屏幕中没有被选择的场景
	}
	
	/* REQUESTCODE */ 
	private final int RCODE_IMPORTVPSF = 22;									// 导入新的VPSFile
	private final int RCODE_CPANO = 21;											// 给已生成的场景重新赋全景图
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case RCODE_CPANO:
			if(resultCode == RESULT_OK){
				VPSFile.setScenePano(touchScene.sID, 							// VSGeneratorActivity返回当前场景拼接的全景图的路径
						data.getStringExtra(Constants.INTENT_FILEDIR));	
			}
			break;
		case RCODE_IMPORTVPSF:
			if(resultCode == RESULT_OK){
				setTouchID(-1);
				mId = VPSFile.getMaxSceneId();
			}
			break;
		}
	}

	/*
	 * Activity's Dialog 
	 */
	private EditText etVPSName  = null;
	private EditText etOrien    = null;
	private EditText etStepNum  = null;
	private EditText etNameSc   = null;
	private List<File> lstPanos  = new ArrayList<File>();
	private List<Scene> lstTempSc = new ArrayList<Scene>();
	private FileItemImageAdapter faa = null;
	private ChooseScArrayAdapter scadapter = null;
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		LayoutInflater factory = null;
	    View view = null;
	    Builder builder = null;
		switch(id){
		case DIALOG_SAVE_VPS:													// 文件已经存在
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Save VPS");
			builder.setIcon(R.drawable.vpsicon);
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.namevps, null);
			etVPSName = (EditText)view.findViewById(R.id.et_d_namevps);
			builder.setView(view);
			builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String strVPSName = etVPSName.getText().toString();
					VPSFile.setVPSName(strVPSName);
					File file = new File(Constants.VPSFILEDIR+strVPSName+".xml");
					if(!file.exists()){
						//执行存储函数
						VPSFile.exportToFile();
					}else{
						showDialog(DIALOG_RESAVE_VPS);
					}
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			dialog = builder.create();
			break;
		case DIALOG_RESAVE_VPS:													// 
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Save VPS");
			builder.setIcon(R.drawable.vpsicon);
			factory = LayoutInflater.from(this);
			view = factory.inflate(android.R.layout.simple_list_item_1, null);
			builder.setView(view);
			builder.setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//执行存储函数
					VPSFile.exportToFile();
				}
			});
			builder.setNeutralButton("Save as", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showDialog(DIALOG_SAVE_VPS);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			dialog = builder.create();
			break;
		
		case DIALOG_NAVISTART:
			builder = new AlertDialog.Builder(this);
			//builder.setTitle("准备导航");
            builder.setTitle("Prepare to Navigate..");
            builder.setIcon(android.R.drawable.ic_menu_more);
            //builder.setMessage("若导航至下一个场景，请将手机头指向正前方，然后点击开始；否则点击取消按钮。");
			builder.setMessage("If you move to the next scene, please start and move forward..");
			builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 绑定服务 
					Intent in = new Intent(VSEditorActivity.this,
							NaviService.class);
					bindService(in, sc, Context.BIND_AUTO_CREATE);
					unbindornot = true;
					showDialog(DIALOG_NAVIGATING);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
	
				}
			});
			dialog = builder.create();
			break;
		case DIALOG_NAVIGATING:
			ProgressDialog pd = new ProgressDialog(this);
			//pd.setTitle("正在惯导定位！");
			pd.setTitle("dead reckoning...");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setCancelable(false);
			pd.setButton("Finished", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					/* 获取数据 */
					stepNumber = (int)srsBinder.getLength();
					pathDirection  = srsBinder.getOrien();
					
					/* 解除绑定 */ 
					if(unbindornot){
						unbindService(sc);
						unbindornot = false;
					}
					
					showDialog(DIALOG_NAVIEND);
				}
			});
			dialog = pd;
			break;
		case DIALOG_NAVIEND:
			builder = new AlertDialog.Builder(this);
            //builder.setTitle("导航结束");
            builder.setTitle("Finished");
            builder.setIcon(android.R.drawable.ic_menu_more);
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.nextpoint, null);
			etOrien = (EditText)view.findViewById(R.id.et_np_orien);
			etStepNum = (EditText)view.findViewById(R.id.et_np_stepnum);
			builder.setView(view);
			builder.setPositiveButton("New Scene", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String strSN = etStepNum.getText().toString().replace(" ", "");
					String strOr = etOrien.getText().toString().replace(" ", "");
					if((strSN != null && !strSN.equals("")) && (strOr!=null && !strOr.equals(""))){
						stepNumber = Integer.parseInt(strSN);
						pathDirection = Float.parseFloat(strOr);
						makeNewScense();
					}else{
						Toast.makeText(VSEditorActivity.this, "步数或者方向值为空值，请重新输入！", Toast.LENGTH_SHORT).show();
						myHandler.sendEmptyMessage(1);  // 若输入的数据不合法，则重新输入
					}
				}
			});
			builder.setNegativeButton("Select Scene", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showDialog(DIALOG_CHOOSESCENE);
				}
			});
			dialog = builder.create();
			
			break;
		case DIALOG_CHOOSESCENE:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Scene ID");
			builder.setIcon(android.R.drawable.ic_menu_more);
			scadapter = new ChooseScArrayAdapter(this, R.layout.lstsceneiditem, lstTempSc);
			builder.setSingleChoiceItems(scadapter, 0, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Scene sc = lstTempSc.get(which);
					nextID = sc.sID;
					Path path = makeNewPath(false);
					VPSFile.addNewPathBetweenTwoSc(path);
					currentID = nextID;
					setTouchID(currentID);
					
					dismissDialog(DIALOG_CHOOSESCENE);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			dialog = builder.create();
			break;
		case DIALOG_CHOOSEPANO:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Set Scene");
			builder.setMessage("Please Set Scene\nNew Scene or Select an existing one.");
			builder.setPositiveButton("New", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(VSEditorActivity.this, VSGeneratorActivity.class);
					intent.putExtra(Constants.INTENT_SINGLE_PANO, false);
					startActivityForResult(intent, RCODE_CPANO);
				}
			});
			builder.setNegativeButton("Select", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					showDialog(DIALOG_EXISTPANOS);
				}
			});
			dialog = builder.create();
			break;
		case DIALOG_EXISTPANOS:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Scene");
			builder.setIcon(android.R.drawable.ic_menu_more);
			faa = new FileItemImageAdapter(this, R.layout.lstfileimageitem, lstPanos);
			builder.setSingleChoiceItems(faa, 0, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					File tempfile = lstPanos.get(which);
					VPSFile.setScenePano(touchScene.sID, tempfile.getAbsolutePath());
					dismissDialog(DIALOG_EXISTPANOS);
				}
			});
			dialog = builder.create();
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		// TODO Auto-generated method stub
		switch(id){
		case DIALOG_SAVE_VPS:
			String strName;
			if(VPSFile.getVPSName()!=""){
				strName = VPSFile.getVPSName();
			}else{
				SimpleDateFormat sdfvps = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
				Date currentTimeVPS = new Date(java.lang.System.currentTimeMillis());
				strName = "VPS"+sdfvps.format(currentTimeVPS);
			}
			AlertDialog dialogTimeVPS = (AlertDialog)dialog;
			EditText etVPS = (EditText)dialogTimeVPS.findViewById(R.id.et_d_namevps);
			etVPS.setText(strName);
			etVPS.setSelection( 0, strName.length() );
			break;
		case DIALOG_NAVIEND:
			AlertDialog dialogNP = (AlertDialog)dialog;
			EditText etOrien = (EditText)dialogNP.findViewById(R.id.et_np_orien);
			etOrien.setText(pathDirection+"");
			EditText etStepNum = (EditText)dialogNP.findViewById(R.id.et_np_stepnum);
			etStepNum.setText(stepNumber+"");
			break;
		case DIALOG_CHOOSESCENE:
			lstTempSc.clear();
			for(int i=0;i<VPSFile.getSceneNum();i++){
				if(VPSFile.getSceneByI(i).sID!=touchScene.sID){
					lstTempSc.add(VPSFile.getSceneByI(i));
				}
			}
			scadapter.notifyDataSetChanged();
			break;
		case DIALOG_RESAVE_VPS:
			AlertDialog dialogRE = (AlertDialog)dialog;
			TextView etReSave = (TextView)dialogRE.findViewById( android.R.id.text1);
			etReSave.setText("*文件 "+VPSFile.getVPSName()+" 已经存在，是否覆盖？");
			break;
		case DIALOG_EXISTPANOS:
			File filePanos = new File(Constants.PANODIR);
			if(filePanos.exists() && filePanos.isDirectory()){
				File[] files = filePanos.listFiles();
				for(int i=0;i<files.length;i++){
					if(files[i].getName().endsWith(".jpg")){
						lstPanos.add(files[i]);
					}
				}
			}
			faa.notifyDataSetChanged();
			break;
		}
	}

	private Path makeNewPath(boolean isNewPoint){								// 两个场景之间的矢量线段（方向 + 距离）
		Path newPath = new Path();
		newPath.prevID = currentID;
		newPath.nextID = nextID;
		newPath.direction = pathDirection;
		newPath.distance  = stepNumber*stepSize+2*Constants.R;
		return newPath;
	}
	
	private void makeNewScense(){												// 生成新的场景
		mId++;
		if(mId == 0){
			Scene csc = new Scene();
			csc.sID = mId;
			csc.sName= "S"+mId;
			csc.sX = 0;
			csc.sY = 0;
			VPSFile.addNewScene(csc, null);
			currentID = mId;
		}else{
			nextID = mId; 
			Path path = makeNewPath(true);
			Scene  ps  = VPSFile.getSceneByID(currentID);
			Scene  ns = new Scene();
			ns.sID = mId;
			ns.sName = "S"+mId;
			ns.sX = (float) (ps.sX + path.distance*Math.sin((Math.PI/180f)*path.direction));
			ns.sY = (float) (ps.sY - path.distance*Math.cos((Math.PI/180f)*path.direction));
			VPSFile.addNewScene(ns, path);
			currentID = nextID;
		}
		setTouchID(mId);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what){
		case 0:
			break;
		case 1://因为对话框中数字不正确，所以需重新打开NEXTPOINT DIALOG
			showDialog(DIALOG_NAVIEND);
			break;
		case 2:
			break;
		}
		return false;
	}


	// 选择的ID
//	private int touchID = -1;
	private Scene touchScene = null;
	private void setTouchID(int _id) {
		if (_id == -1) {
			touchScene = null;
			llVse.setVisibility(View.GONE); 
			llMoveSc.setVisibility(View.GONE);
		} else {
			llVse.setVisibility(View.VISIBLE);
			llMoveSc.setVisibility(View.VISIBLE);
			touchScene = VPSFile.getSceneByID(_id);
			currentID = _id;
		}
		vspv.setTouchID(_id);
	}
	
	/* 触控反馈 */
	private boolean isMove = false;
	private float mPreviousX;
	private float mPreviousY;
	private float mFirstX;
	private float mFirstY;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			isMove = true;
			mFirstX = mPreviousX = event.getX();
			mFirstY = mPreviousY = event.getY();
			break;
		case MotionEvent.ACTION_POINTER_1_DOWN:
			isMove = false;
			break;
		case MotionEvent.ACTION_UP:
			float x = event.getX();
			float y = event.getY();
			
			int i;
			for(i=0;i<VPSFile.getSceneNum();i++){
				Scene tempSc = VPSFile.getSceneByI(i);
				float dist = (float) Math.sqrt(Math.pow((tempSc.sX+vspv.getDValueX())-x,2)
						+Math.pow((tempSc.sY+vspv.getDValueY())-y, 2));
				if(dist<Constants.R){
					setTouchID(tempSc.sID);
					break;
				}
			}
			
			if(Math.abs(x-mFirstX)<1 && Math.abs(y-mFirstY)<1){
				if(i>=VPSFile.getSceneNum()){
					setTouchID(-1);
				}
			}
			
			break;
		case MotionEvent.ACTION_MOVE:
			if(isMove){
				float xMove  = event.getX();
				float dx = xMove - mPreviousX;
				float yMove  = event.getY();
				float dy = yMove - mPreviousY;
				vspv.setDValue(dx, dy);
				mPreviousX = xMove;
				mPreviousY = yMove;
			}
			break;
		}
		return true;
	}
	
	
	// MENU的编号
	private final int MENU_RECREATE  = Menu.FIRST;
	private final int MENU_IMPORTVPS = Menu.FIRST + 1;
	private final int MENU_EXPORTVPS = Menu.FIRST + 2;
	private final int MENU_RESET     = Menu.FIRST + 3;
//	private final int MENU_SETTING   = Menu.FIRST+3;
	/*
	 * Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_RECREATE,  0, "New VPS");
		menu.add(0, MENU_IMPORTVPS, 1, "Import VPS");
		menu.add(0, MENU_EXPORTVPS, 2, "Save VPS");
		menu.add(0, MENU_RESET,     3, "Reset");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case MENU_RECREATE:
			clear();
			makeNewScense();
			break;
		case MENU_IMPORTVPS:
			Intent inshow = new Intent(VSEditorActivity.this, ShowVPSFileActivity.class);
			startActivityForResult(inshow, RCODE_IMPORTVPSF);
			break;
		case MENU_EXPORTVPS:
			File file = new File(Constants.VPSFILEDIR
					+ VPSFile.getVPSName() + ".xml");
			if (!file.exists()) {
				showDialog(DIALOG_SAVE_VPS);
			} else {
				showDialog(DIALOG_RESAVE_VPS);
			}
			
//			int i;
//			for (i = 0; i < VPSFile.getSceneNum(); i++) {
//				Scene temp = VPSFile.getSceneByI(i);
//				if (!temp.isRelevance) {
//					break;
//				}
//			}
//			if (i < VPSFile.getSceneNum()) {
//				Toast.makeText(VSEditorActivity.this, "有场景没有设置全景图，无法导出文件！",
//						Toast.LENGTH_SHORT).show();
//			} else {
//				
//			}
			break;
		case MENU_RESET:
			vspv.initDValue(displayWidth*0.5f, displayHeight*0.5f);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp,
			String key) {
		if(key.equals(Constants.PREF_CB_DRAWGRID)){
			vspv.setIsDrawBGLine(sp.getBoolean(key, false));
		}
	}

	// *********************************************
	// 按钮监听函数
	// *********************************************
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btn_vse_pano:
			if(touchScene.isRelevance){
				Intent in = new Intent(VSEditorActivity.this, CalibratePanoActivity.class);
				in.putExtra(Constants.SCENEID, touchScene.sID);
				startActivity(in);
			}else{
				showDialog(DIALOG_CHOOSEPANO);
			}
			break;
		case R.id.btn_vse_navi:
			showDialog(DIALOG_NAVISTART);
			break;
		case R.id.btn_vse_delete:
			if(touchScene.sID!=-1){
				VPSFile.deleteScene(touchScene.sID);
	        	setTouchID(-1);
        	}
			break;
		case R.id.btn_vse_save:
			File file = new File(Constants.VPSFILEDIR
					+ VPSFile.getVPSName() + ".xml");
			if (!file.exists()) {
				showDialog(DIALOG_SAVE_VPS);
			} else {
				showDialog(DIALOG_RESAVE_VPS);
			}
			
//			int i;
//			for (i = 0; i < VPSFile.getSceneNum(); i++) {
//				Scene temp = VPSFile.getSceneByI(i);
//				if (!temp.isRelevance) {
//					break;
//				}
//			}
//			if (i < VPSFile.getSceneNum()) {
//				Toast.makeText(VSEditorActivity.this, "有场景没有设置全景图，无法导出文件！",
//						Toast.LENGTH_SHORT).show();
//			} else {
//				
//			}
			break;
		case R.id.btn_vse_moveleft:
			touchScene.moveLeft();
			vspv.invalidate();
			break;
		case R.id.btn_vse_moveright:
			touchScene.moveRight();
			vspv.invalidate();
			break;
		case R.id.btn_vse_movebottom:
			touchScene.moveBottom();
			vspv.invalidate();
			break;
		case R.id.btn_vse_movetop:
			touchScene.moveTop();
			vspv.invalidate();
			break;
		case R.id.btn_vse_scale_big:
			vspv.scaleBig();
			break;
		case R.id.btn_vse_scale_small:
			vspv.scaleSmall();
			break;
		}
	}
	
	// *********************************************
	// 适配器
	// *********************************************
	// 场景
	class ChooseScArrayAdapter extends ArrayAdapter<Scene>{
		public ChooseScArrayAdapter(Context context, int textViewResourceId,
				List<Scene> objects) {
			super(context, textViewResourceId, objects);
			resource = textViewResourceId;
		}

		int resource;
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RelativeLayout rl;
			
			Scene sc = getItem(position);
			
			if (convertView == null) {
				rl = new RelativeLayout(getContext());
				String inflater = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater vi = (LayoutInflater) getContext()
						.getSystemService(inflater);
				vi.inflate(resource, rl, true);
			} else {
				rl = (RelativeLayout) convertView;
			}
			
			TextView name = (TextView) rl.findViewById(R.id.tv_sceneitem_name);
			name.setText(sc.sID+"");

			return rl;
		}
	}

}