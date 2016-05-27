package imobile.panorama.vsgenerator;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service{

	private LocationBinder binder = new LocationBinder();
	// 定位管理
	private LocationManager lm = null;
	private LocationListener mLocListener = new LocationListener(){
		@Override
		public void onLocationChanged(Location location) {
			lstLoc.add(location);
		}

		@Override
		public void onProviderDisabled(String provider) { }

		@Override
		public void onProviderEnabled(String provider) { }

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { }
	};
	// 位置列表
	private List<Location> lstLoc = new ArrayList<Location>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, mLocListener);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		lm.removeUpdates(mLocListener);
	}

	class LocationBinder extends Binder{
		public LocationService getService(){
			return LocationService.this;
		}
	}
	
	public Location getLocation(){
		Location lc = null;
		
		if(lstLoc.size()<1){
			return lc;
		}
		
		double sumLat = 0;
		double sumLon = 0;
		for(int i=0;i<lstLoc.size();i++){
			sumLat += lstLoc.get(i).getLatitude();
			sumLon += lstLoc.get(i).getLongitude();
		}
		
		lc = new Location(LocationManager.GPS_PROVIDER);
		lc.setLatitude(sumLat/lstLoc.size());
		lc.setLongitude(sumLon/lstLoc.size());
		return lc;
	}
}
