package edu.junyao.usar;

import com.google.android.gms.maps.model.LatLng;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GPSService extends Service{
	public static final String NEW_LOCATION_UPDATE = "New_Loc_update";
	public static final String BUNDLE_LAT_LONG = "New_Lat_long";
	public static final String GPS_LAT = "latitude";
	public static final String GPS_LNG = "longitude";
	
	LocationManager locationManager;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider = LocationManager.GPS_PROVIDER;
		locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
		
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private final LocationListener locationListener = new LocationListener(){

		@Override
		public void onLocationChanged(Location locationUpdate) {			
			if(locationUpdate != null) {
				Double lat = locationUpdate.getLatitude();
				Double lon = locationUpdate.getLongitude();

				Intent intent = new Intent(NEW_LOCATION_UPDATE);
				Bundle bundle = new Bundle();
				bundle.putDouble(GPS_LAT, lat);
				bundle.putDouble(GPS_LNG, lon);
				intent.putExtras(bundle);
				sendBroadcast(intent);
			}	
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	};
	@Override
	public void onDestroy(){
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
		Log.d("ServiceonDestroy", "onDestroy Called in Service");
	}
	
	
}

