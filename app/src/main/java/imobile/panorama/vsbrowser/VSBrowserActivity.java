package imobile.panorama.vsbrowser;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import imobile.panorama.R;
import imobile.panorama.adapter.FileItemImageAdapter;
import imobile.panorama.adapter.FileItemTextAdapter;
import imobile.panorama.adapter.StringItemTextAdapter;
import imobile.panorama.net.Communicat;
import imobile.panorama.util.Constants;
import imobile.panorama.util.Scene;
import imobile.panorama.util.VPSFile;

/**
 * 浏览可用于展示的全景图，并生成虚拟空间，在ShowPanoActivity中展示
 * @author chix
 */

public class VSBrowserActivity extends TabActivity{
	
	private static final String TASK_WEBFIELS = "WEBFILES";
	private static final String TASK_DL_XML   = "DL_XML";
	private static final String TASK_DL_PIC   = "DL_PIC";
	private static final String TASK_UL_XML   = "UL_XML";
	private static final String TASK_UL_PIC   = "UL_PIC";
	
	private static final String TH_PANORAMA = "PANORAMA";
	private static final String TH_PANOSPACE = "PANOSPACE";
	private static final String TH_WEB = "WEB";
	
	private Communicat comm = null;

	// UI控件，及其数据
	private TabHost browserTabHost;
	private ListView lvPanos;
	private List<File> lstPanos = null;
	private FileItemImageAdapter faaPanos = null;
	private ListView lvVPSFiles;
	private List<File> lstVPSFile = null;
	private FileItemTextAdapter faaVPS = null;
	private ListView lvWebFiles;
	private List<String> lstWebFiles = new ArrayList<String>();
	private StringItemTextAdapter saaWeb = null;
	
	// 删除文件对话框
	private final int DIALOG_SET = 0;
	private File  dFile = null;
	private int dId  = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		browserTabHost = this.getTabHost();
		LayoutInflater.from(this).inflate(R.layout.vsbrowser, browserTabHost.getTabContentView(),true);
        browserTabHost.addTab(
                browserTabHost.newTabSpec(TH_PANOSPACE)
                        .setIndicator("Virtual Tour", null)//getResources().getDrawable(R.drawable.tab_title)
                        .setContent(R.id.rl_vsb_vpsfile));
		browserTabHost.addTab(
				browserTabHost.newTabSpec(TH_PANORAMA)
				.setIndicator("Panorama", null)//getResources().getDrawable(R.drawable.tab_title)
				.setContent(R.id.rl_vsb_singlepano)
				);
		browserTabHost.addTab(
				browserTabHost.newTabSpec(TH_WEB)
				.setIndicator("Network Content", null)//getResources().getDrawable(R.drawable.tab_title)
				.setContent(R.id.rl_vsb_web));
		
		browserTabHost.setOnTabChangedListener(new OnTabChangeListener(){
			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				if(tabId.equals(TH_WEB)){
					new NetAsyncTask().execute(TASK_WEBFIELS);
				}
			}
		});
		
		comm = new Communicat();

		lvPanos = (ListView)findViewById(R.id.lv_vsb_panos);
		lvVPSFiles = (ListView)findViewById(R.id.lv_vsb_vpsfiles);
		lvWebFiles = (ListView)findViewById(R.id.lv_vsb_webfiles);
		
		/* 全景图的适配器 */
		File filePanos = new File(Constants.PANODIR);
		if(filePanos.exists()){
			File[] files = filePanos.listFiles(filter("\\D.*\\.jpg"));
			lstPanos = Arrays.asList(files);
		}
		faaPanos = new FileItemImageAdapter(this, R.layout.lstfileimageitem, lstPanos);
		lvPanos.setAdapter(faaPanos);
		lvPanos.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				File file = lstPanos.get(position);
				VPSFile.clear();
				VPSFile.setVPSName(file.getName());
				Scene sc = new Scene();
				sc.sID = 0;
				sc.sName = file.getName();
				sc.sX = sc.sY = 0;
				sc.lstHotS.clear();
				sc.isRelevance = true;
				sc.sPanoFile = file.getAbsolutePath();
				VPSFile.addNewScene(sc, null);
				
				//导向展示页面
				Intent in = new Intent(VSBrowserActivity.this, ShowPanoActivity.class);
				startActivity(in);
			}
		});
		lvPanos.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long arg3) {
				dFile = lstPanos.get(position);

				dId = 0;
				showDialog(DIALOG_SET);
				return true;
			}
		});
		
		
		/* 虚拟空间文件的适配器 */
		File fileVPSFile = new File(Constants.VPSFILEDIR);
		if(fileVPSFile.exists()){
			File[] files = fileVPSFile.listFiles(filter("\\D.*\\.xml"));
			lstVPSFile = Arrays.asList(files);
		}
		//将lstVPSFile适配到ListView中
		faaVPS = new FileItemTextAdapter(this, R.layout.lstfiletextitem, lstVPSFile);
		lvVPSFiles.setAdapter(faaVPS);
		lvVPSFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				File file = lstVPSFile.get(position);
				VPSFile.importFromFile(file.getName());
				
				//导向展示页面
				Intent in = new Intent(VSBrowserActivity.this, ShowPanoActivity.class);
				startActivity(in);
			}
		});
		lvVPSFiles.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long arg3) {
				dFile = lstVPSFile.get(position);
				dId = 1;
				showDialog(DIALOG_SET);
				return true;
			}
		});
		
		
		/* 来自于网络上的文件 */
		saaWeb = new StringItemTextAdapter(this, R.layout.lstfiletextitem, lstWebFiles);
		lvWebFiles.setAdapter(saaWeb);
		lvWebFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				String xmlName = lstWebFiles.get(position);
				new NetAsyncTask().execute(TASK_DL_XML, xmlName);
				
				// 点击之后，从网络中下载具体的文字
			}
		});
		lvWebFiles.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long arg3) {
				return true;
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		VPSFile.clear();
	}
	
	// ********************************************
	// Activity Dialog
	// ********************************************
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		super.onCreateDialog(id);
		Dialog dialog = null;
		LayoutInflater factory = null;
	    View view = null;
	    Builder builder = null;
		switch(id){
		case DIALOG_SET:
			builder = new AlertDialog.Builder(VSBrowserActivity.this);
			builder.setTitle("Setting");
			builder.setIcon(android.R.drawable.ic_menu_set_as);
			factory = LayoutInflater.from(this);
			view = factory.inflate( R.layout.setpano,null);
			Button btn = (Button)view.findViewById(R.id.btn_set_delete);
			btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {	
					switch (dId) {
					case 0:
						if (dFile.exists()){
							dFile.delete();
						}
						lstPanos.remove(dFile);
						faaPanos.notifyDataSetChanged();
						break;
					case 1:
						if (dFile.exists()) {
							dFile.delete();
						}
						lstVPSFile.remove(dFile);
						faaVPS.notifyDataSetChanged();
						break;
					}
					dismissDialog(DIALOG_SET);
				}
			});
			Button btnUpload = (Button)view.findViewById(R.id.btn_set_upload);
			btnUpload.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {	
					// 将dFile上传到服务端
					// dFile;
					if(dFile.getName().endsWith(".xml")){
						System.out.println("sdfk:\t"+dFile.getAbsolutePath());
						new NetAsyncTask().execute(TASK_UL_XML, dFile.getAbsolutePath());
					}else{
						
					}
						
					dismissDialog(DIALOG_SET);
				}
			});
			builder.setView(view);
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

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
		super.onPrepareDialog(id, dialog);
	}
	
	
	private String currentVPSFile = "";
	
	private class NetAsyncTask extends AsyncTask<String, Integer, Integer>{

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected Integer doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			int result = 0;
			if(arg0[0].equals(TASK_WEBFIELS)){
				if(comm!=null){
					
					List<String> temp = comm.getXmlList();
					
					if(temp.size()>0){
						lstWebFiles.clear();
						
						for(int i=0;i<temp.size();i++){
							lstWebFiles.add(temp.get(i));
						}
					}
				}
				result = 1;
			}else if(arg0[0].equals(TASK_DL_XML)){
				String xmlName = arg0[1];
				
				System.out.println(TASK_DL_XML);
				
				if(comm!=null){
					try {
						comm.downFile(Communicat.ip+"xml/"+xmlName, "/sdcard/", "web.xml");

						VPSFile.importWebFile("/sdcard/web.xml");

						for(int i=0;i<VPSFile.getSceneNum();i++){
							Scene sc = VPSFile.getSceneByI(i);
	
							
							String picName = sc.sPanoFile.substring(sc.sPanoFile.lastIndexOf("/") + 1);
							
							comm.downFile(Communicat.ip+"pic/"+picName, "/sdcard/", picName);
							
							sc.sPanoFile = "/sdcard/"+picName;
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				result = 2;
			}else if(arg0[0].endsWith(TASK_DL_PIC)){
				
				result = 3;
			}else if(arg0[0].endsWith(TASK_UL_XML)){
				comm.uploadXmlFile(Communicat.ip + "upload/uploadXml.php", dFile.getAbsolutePath());
				
				VPSFile.importFromFile(dFile.getName());
				

				Map<String, File> files = new HashMap<String, File>();   
				for(int i=0;i<VPSFile.getSceneNum();i++)
				{
					Scene sc = VPSFile.getSceneByI(i);
					files.put(sc.sPanoFile.substring(sc.sPanoFile.lastIndexOf("/")), 
							new File(sc.sPanoFile));  
				}
				try {
					comm.uploadFiles(Communicat.ip + "upload/uploadPic.php", files);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				result = 4;
			}else if(arg0[0].endsWith(TASK_UL_PIC)){
				
				result = 5;
			}

			return result;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			switch(result){
			case 1:
				break;
			case 2:
				Intent in = new Intent(VSBrowserActivity.this, ShowPanoActivity.class);
				startActivity(in);
				break;
			case 3:
				break;
			case 4:
				Toast.makeText(VSBrowserActivity.this, "Upload "+dFile.getName()+" successfully!", Toast.LENGTH_SHORT).show();
				break;
			case 5:
				break;
			}
			
			saaWeb.notifyDataSetChanged();
			lvWebFiles.invalidate();
		}
		
	}

	// 匿名内部类---文件目录筛选器
	// 传递的参数必须是final的，这样匿名内部类才能使用来自该类范围之外的对象
	private FilenameFilter filter(final String regex){
		return new FilenameFilter(){
			
			private Pattern pattern = Pattern.compile(regex);
			
			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				return pattern.matcher(filename).matches();
			}
		};
	}
}
