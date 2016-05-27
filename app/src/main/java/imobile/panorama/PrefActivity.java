package imobile.panorama;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.WindowManager;

public class PrefActivity extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
