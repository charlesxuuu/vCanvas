package imobile.panorama.adapter;

import imobile.panorama.R;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StringItemTextAdapter extends ArrayAdapter<String> {

	int resource;

	public StringItemTextAdapter(Context context, int _resource,
			List<String> objects) {
		super(context, _resource, objects);
		resource = _resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		RelativeLayout rl;

		String name = getItem(position);

		if (convertView == null) {
			rl = new RelativeLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater) getContext()
					.getSystemService(inflater);
			vi.inflate(resource, rl, true);
		} else {

			rl = (RelativeLayout) convertView;
		}

		TextView tv = (TextView) rl.findViewById(R.id.tv_vps_filename_text);
		tv.setText(name);

		return rl;
	}
}