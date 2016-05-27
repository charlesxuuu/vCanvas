package imobile.panorama.vseditor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import imobile.panorama.R;
import imobile.panorama.adapter.FileItemTextAdapter;
import imobile.panorama.util.Constants;
import imobile.panorama.util.VPSFile;

public class ShowVPSFileActivity extends Activity {

	private ListView lvVpsfile = null;
	private List<File> lstVPSFile = null;
	
	private FileItemTextAdapter faa = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.lstvpsfile);
		lvVpsfile = (ListView)findViewById(R.id.lv_vps_lst);

		lstVPSFile = new ArrayList<File>();
		
		File file = new File(Constants.VPSFILEDIR);
		
		if(file.exists()){
			File[] files = file.listFiles();
			
			for(int i=0;i<files.length;i++){
				if(files[i].isFile() && files[i].getName().endsWith(".xml")){
					lstVPSFile.add(files[i]);
				}
			}
		}
		
		//将lstVPSFile适配到ListView中
		faa = new FileItemTextAdapter(this, R.layout.lstfiletextitem, lstVPSFile);
		lvVpsfile.setAdapter(faa);
		
		lvVpsfile.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				File file = lstVPSFile.get(position);
				VPSFile.importFromFile(file.getName());
				setResult(RESULT_OK);
				finish();
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
