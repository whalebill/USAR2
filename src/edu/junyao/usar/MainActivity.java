package edu.junyao.usar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.fiveglabs.diorama.HTTP.Global;
import edu.fiveglabs.diorama.HTTP.RestClient;
import edu.fiveglabs.diorama.Models.DIORAMA.EmergencyResponderUpdate;
import edu.fiveglabs.diorama.Models.DIORAMA.FemaMarking;
import edu.fiveglabs.diorama.Models.DIORAMA.PatientAssessment;

public class MainActivity extends Activity implements OnMapLongClickListener, OnMarkerClickListener{
	public enum Mode{
		VICTIM, FEMA, SELECT, REQUEST, VIEW
	}
	
	private static final long GROUP_ID = 1028452;
	private static final String TAG = "MainActivity";
	private static final String REQUEST_MARK = "@@@";
	private static final int LOGIN_REQUEST = 0;
	private static final int VICTIM_REQUEST = 1;
	private static final int FEMA_REQUEST = 2;	
	public static final int SELECT_REQUEST = 3;
	public static final int REQUEST_REQUEST = 4;
	
	public static final int ADD_FUNCTION = 1;
	public static final int CHECK_FUNCTION = 2;

    private GoogleMap map;
	private ImageButton victimMenuBtn;
	private ImageButton femaMenuBtn;
	private ImageButton selectMenuBtn;
	private ImageButton requestMenuBtn;
	private TextView requestText, modeText;

	private RestClient rc;
    private GPSReceiver gpsReceiver;
    private Timer updateTimer = new Timer();
    private UpdateFromServ updateFromServ= new UpdateFromServ();
    private SharedPreferences pref;
    private NotificationManager notificationManager;		
    
    private OnClickListener menuClickListener;
    public static boolean map_loaded, anim_ended;
    
    public Mode mode = Mode.VIEW;
    private String teamName = "";
    private int responderId = 1;
    private int patientId = 1;
    private double range = Double.MAX_VALUE;
    private int selectLevel = 0;
    private boolean needHelp = false, canBeUpdated = true;
    private ArrayList<PatientAssessment> vicToAdd, vicToUpdate;
    private ArrayList<FemaMarking> femaToAdd, femaToUpdate;
    private EmergencyResponderUpdate emsToAdd, emsToUpdate;
    private HashMap<PatientAssessment, Marker> shownVicMarker;
    private HashMap<FemaMarking, Marker> shownFemaMarker;
    private HashMap<EmergencyResponderUpdate, Marker> shownEmsMarker;
    private LatLng currentLoc;
    private PatientAssessment currentPA;
    private FemaMarking currentFM;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);
    	
    	// ui register
    	map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    	map.setMyLocationEnabled(true);
    	map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		victimMenuBtn = (ImageButton) findViewById(R.id.victim_menu);
		femaMenuBtn = (ImageButton) findViewById(R.id.fema_menu);
		selectMenuBtn = (ImageButton) findViewById(R.id.select_menu);
		requestMenuBtn = (ImageButton) findViewById(R.id.request_menu);
		requestText = (TextView) findViewById(R.id.request_tv);
		modeText = (TextView) findViewById(R.id.mode_tv);
    	
		menuClickListener = new MenuClickListener();
		
		victimMenuBtn.setOnClickListener(menuClickListener);
		femaMenuBtn.setOnClickListener(menuClickListener);
		selectMenuBtn.setOnClickListener(menuClickListener);
		requestMenuBtn.setOnClickListener(menuClickListener);
		
    	// intent register
    	IntentFilter filter = new IntentFilter(GPSService.NEW_LOCATION_UPDATE);
    	
    	// register notification
    	notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	
    	// result from different ui activity should use intent for result
    	gpsReceiver = new GPSReceiver();
    	registerReceiver(gpsReceiver, filter);
    	startService(new Intent(getApplicationContext(), GPSService.class));

    	map.setOnMapLongClickListener(this);
    	map.setOnMarkerClickListener(this);

    	init();
    	centerMapOnMyLocation();
    }
    //initial LoginActivity
    private void init(){
    	rc = new RestClient();
    	
    	Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		Bundle bundle = new Bundle();
		intent.putExtras(bundle);
		startActivityForResult(intent, LOGIN_REQUEST);
    	
		pref = getSharedPreferences("info", MODE_PRIVATE);
    	this.patientId = pref.getInt(teamName+"///"+responderId, 0);
    	shownVicMarker = new HashMap<PatientAssessment, Marker>();
    	shownFemaMarker = new HashMap<FemaMarking, Marker>();
    	shownEmsMarker = new HashMap<EmergencyResponderUpdate, Marker>();
    	vicToAdd = new ArrayList<PatientAssessment>();
    	vicToUpdate = new ArrayList<PatientAssessment>();
    	femaToAdd = new ArrayList<FemaMarking>();
    	femaToUpdate = new ArrayList<FemaMarking>();
    	this.anim_ended = false;
    	this.map_loaded = false;
    	
    }
  
    @Override
    protected void onResume(){
	    super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.main, menu);
    	return true;
    }

    @Override
    public void onMapLongClick(final LatLng theLocation) {
    	map_loaded = anim_ended = false;
    	
    	switch(mode){
    	case VICTIM:
    		// move camera to clicked position
        	map.animateCamera(CameraUpdateFactory.newLatLng(theLocation));

        	map.setOnCameraChangeListener(new OnCameraChangeListener(){
        		// called when camera change finished
    			@Override
    			public void onCameraChange(CameraPosition arg0) {
    				if (map_loaded){
    					fireActivityForResult(mode, theLocation);
    				}
    				anim_ended = true;
    			}
        	});
        	map.setOnMapLoadedCallback(new OnMapLoadedCallback(){
        		// called when map view loaded
    			@Override
    			public void onMapLoaded() {
    				if (anim_ended){
    					fireActivityForResult(mode, theLocation);
    				}
    				map_loaded = true;
    			}
        	});
    		break;
    	case FEMA:
    		// move camera to clicked position
        	map.animateCamera(CameraUpdateFactory.newLatLng(theLocation));

        	map.setOnCameraChangeListener(new OnCameraChangeListener(){
        		// called when camera change finished
    			@Override
    			public void onCameraChange(CameraPosition arg0) {
    				if (map_loaded){
    					fireActivityForResult(mode, theLocation);
    				}
    				anim_ended = true;
    			}
        	});
        	map.setOnMapLoadedCallback(new OnMapLoadedCallback(){
        		// called when map view loaded
    			@Override
    			public void onMapLoaded() {
    				if (anim_ended){    					
    					fireActivityForResult(mode, theLocation);
    				}
    				map_loaded = true;
    			}
        	});
    		break;
    	}
    }
    // when 
    private void fireActivityForResult(Mode mode, LatLng theLocation){
    	Intent intent;
    	Bundle bundle;
    	switch(mode){
    	case VICTIM:
    		intent = new Intent(getApplicationContext(), VictimActivity.class);
			bundle = new Bundle();
			bundle.putInt(VictimActivity.FUNC, ADD_FUNCTION);
			bundle.putDouble(VictimActivity.LAT, theLocation.latitude);
			bundle.putDouble(VictimActivity.LON, theLocation.longitude);
			if(currentLoc != null){
				bundle.putDouble(VictimActivity.CUR_LAT, currentLoc.latitude);
				bundle.putDouble(VictimActivity.CUR_LON, currentLoc.longitude);
			}else{
				bundle.putDouble(VictimActivity.CUR_LAT, Double.NaN);
				bundle.putDouble(VictimActivity.CUR_LON, Double.NaN);				
			}
			intent.putExtras(bundle);
			// this request code will return to the OnActivityResult();
			startActivityForResult(intent, VICTIM_REQUEST);
		
    		break;
    	case FEMA:
    		intent = new Intent(getApplicationContext(), FEMAActivity.class);
    		bundle = new Bundle();
    		bundle.putInt(FEMAActivity.FUNC, ADD_FUNCTION);
			bundle.putDouble(FEMAActivity.LAT, theLocation.latitude);
			bundle.putDouble(FEMAActivity.LON, theLocation.longitude);
			if(currentLoc != null){
				bundle.putDouble(FEMAActivity.CUR_LAT, currentLoc.latitude);
				bundle.putDouble(FEMAActivity.CUR_LON, currentLoc.longitude);
			}else{
				Toast.makeText(getApplicationContext(), "current location not available", Toast.LENGTH_SHORT).show();
				bundle.putDouble(FEMAActivity.CUR_LAT, Double.NaN);
				bundle.putDouble(FEMAActivity.CUR_LON, Double.NaN);
			}
			bundle.putInt(FEMAActivity.RESPONDER_ID, responderId);
			bundle.putString(FEMAActivity.STAMP, rc.getTimeStamp());
			intent.putExtras(bundle);
			startActivityForResult(intent, FEMA_REQUEST);
    		break;
    	}
    }
    //processing the new information;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Bundle bundle;
    	switch(requestCode){
    	//update information for login user; 
    	case LOGIN_REQUEST:
    		Log.d(TAG, "result from LoginActivity");
    		if(resultCode == RESULT_OK){
    			bundle = data.getExtras();
    			teamName = bundle.getString(LoginActivity.TEAM_NAME);
    			responderId = bundle.getInt(LoginActivity.RESPONDER_ID);
    			// start update from server
    	    	updateTimer..scheduleAtFixedRate(updateFromServ, 0, 1000);
    			Toast.makeText(getApplicationContext(), "welcome responder from "+teamName, Toast.LENGTH_SHORT).show();
    		}else if(resultCode == RESULT_CANCELED){
    			finish();
    		}
    		break;
    		//
    	case VICTIM_REQUEST:
    		Log.d(TAG, "result from VictimAcitivity");
    		if(resultCode == RESULT_OK){
	    		bundle = data.getExtras();
	    		if(bundle.getInt(VictimActivity.FUNC) == MainActivity.ADD_FUNCTION){
		    		double vic_lat = bundle.getDouble(VictimActivity.LAT);
		    		double vic_lon = bundle.getDouble(VictimActivity.LON);
		    		int vic_triage = bundle.getInt(VictimActivity.TRIAGE);
		    		String comment = bundle.getString(VictimActivity.COMMENT);
		    		int vic_type = bundle.getInt(VictimActivity.TYPE);
		    		// new a victim object to add to hashmap;(server thread visit this hashmap periodically ;)
		    		vicToAdd.add(new PatientAssessment().packageObject(0, responderId, patientId, vic_type, RestClient.getTimeStamp(), vic_lat+","+vic_lon, Global.TRIAL_ID, vic_triage, comment, Global.GROUP_ID));
		    		Toast.makeText(getApplicationContext(), "New victim added, synchronizing to server", Toast.LENGTH_SHORT).show();
	    		}else if(bundle.getInt(VictimActivity.FUNC) == MainActivity.CHECK_FUNCTION){
	    			
	    			double vic_lat = bundle.getDouble(VictimActivity.LAT);
		    		double vic_lon = bundle.getDouble(VictimActivity.LON);
		    		int vic_triage = bundle.getInt(VictimActivity.TRIAGE);
		    		String comment = bundle.getString(VictimActivity.COMMENT);
		    		int vic_type = bundle.getInt(VictimActivity.TYPE);
		    		
		    		currentPA.setLocationString(vic_lat+","+vic_lon);
		    		currentPA.setPriorityType(vic_triage);
		    		currentPA.setDetails(comment);
		    		currentPA.setAssessmentType(vic_type);
		    		// add a victim object to update
		    		vicToUpdate.add(currentPA);
		    		currentPA = null;
		    		Toast.makeText(getApplicationContext(), "New victim updated, synchronizing to server", Toast.LENGTH_SHORT).show();
	    		}
    		}
    		break;
    	case FEMA_REQUEST:
    		Log.d(TAG, "result from FEMAAcitivity");
    		if(resultCode == RESULT_OK){
    			bundle = data.getExtras();
    			if(bundle.getInt(FEMAActivity.FUNC) == MainActivity.ADD_FUNCTION){
    				double fema_lat = bundle.getDouble(FEMAActivity.LAT);
        			double fema_lon = bundle.getDouble(FEMAActivity.LON);
        			int live_num = bundle.getInt(FEMAActivity.LIVE_NUM);
        			int dead_num = bundle.getInt(FEMAActivity.DEAD_NUM);
        			String timeStamp = bundle.getString(FEMAActivity.STAMP);
        			String comment = bundle.getString(FEMAActivity.COMMENT);
        			// new a FEMA marking to add
        			femaToAdd.add(new FemaMarking().packageObject(0, responderId, timeStamp, fema_lat+","+fema_lon, Global.TRIAL_ID, comment, Global.GROUP_ID, live_num, dead_num, comment));
        			Toast.makeText(getApplicationContext(), "New FEMA marking added, synchronizing to server", Toast.LENGTH_SHORT).show();
    			}else if(bundle.getInt(FEMAActivity.FUNC) == MainActivity.CHECK_FUNCTION){
    				double fema_lat = bundle.getDouble(FEMAActivity.LAT);
        			double fema_lon = bundle.getDouble(FEMAActivity.LON);
        			int live_num = bundle.getInt(FEMAActivity.LIVE_NUM);
        			int dead_num = bundle.getInt(FEMAActivity.DEAD_NUM);
        			String timeStamp = bundle.getString(FEMAActivity.STAMP);
        			String comment = bundle.getString(FEMAActivity.COMMENT);
        			
        			currentFM.setLocationString(fema_lat+","+fema_lon);
        			currentFM.setLivingVictims(live_num);
        			currentFM.setDeadVictims(dead_num);
        			currentFM.setTimestamp(timeStamp);
        			currentFM.setDetails(comment);
        			currentFM.setHazardsPresent(comment);
        			// add a FEMA marking to update
        			femaToUpdate.add(currentFM);
        			currentFM = null;
        			Toast.makeText(getApplicationContext(), "New FEMA marking added, synchronizing to server", Toast.LENGTH_SHORT).show();
    			}
    		}
    		break;
    	case SELECT_REQUEST:
    		Log.d(TAG, "result from SelectAcitivity");
    		if(resultCode == RESULT_OK){
    			bundle = data.getExtras();
    			if(bundle.getDouble(SelectActivity.RANGE) > 0){
    				range = bundle.getDouble(SelectActivity.RANGE);
    			}else{
    				range = Double.MAX_VALUE;
    			}
    			selectLevel = bundle.getInt(SelectActivity.LEVEL);
    			mode = Mode.SELECT;
    		}
    		break;
    	case REQUEST_REQUEST:
    		Log.d(TAG, "result from RequestAcitivity");
    		if(resultCode == RESULT_OK){
    			bundle = data.getExtras();
    			needHelp = bundle.getBoolean(RequestActivity.SWITCH);
    			if(needHelp){
    				requestText.setBackgroundColor(Color.BLACK);
    				requestText.setTextColor(Color.WHITE);
    				requestText.setText("Requesting help from other responders");
    			}else{
    				requestText.setBackgroundColor(Color.TRANSPARENT);
    				requestText.setTextColor(Color.TRANSPARENT);
    				requestText.setText("");
    			}
    			mode = Mode.REQUEST;
    			canBeUpdated = false;
    		}
    		break;
    	}
    }
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		for(PatientAssessment pa : shownVicMarker.keySet()){
			if(marker.getPosition().equals(shownVicMarker.get(pa).getPosition())){
				Log.d(TAG, "marker found");
				checkMarkerForResult(mode, pa);
				break;
			}
		}
		for(FemaMarking fm : shownFemaMarker.keySet()){
			if(marker.getPosition().equals(shownFemaMarker.get(fm).getPosition())){
				Log.d(TAG, "marker found");
				checkMarkerForResult(mode, fm);
				break;
			}
		}
		Log.d(TAG, "marker not found");
		return false;
	};
	
	private void checkMarkerForResult(Mode mode, Object obj){
		Intent intent;
    	Bundle bundle;
    	if(obj instanceof PatientAssessment){
    		PatientAssessment pa = (PatientAssessment) obj;
    		currentPA = pa;
    		intent = new Intent(getApplicationContext(), VictimActivity.class);
			bundle = new Bundle();
			bundle.putInt(VictimActivity.FUNC, CHECK_FUNCTION);
			bundle.putDouble(VictimActivity.LAT, Double.parseDouble(pa.getLocationString().split(",")[0]));
			bundle.putDouble(VictimActivity.LON, Double.parseDouble(pa.getLocationString().split(",")[1]));
			bundle.putInt(VictimActivity.TRIAGE, pa.getPriorityType());
			bundle.putString(VictimActivity.COMMENT, pa.getDetails());
			intent.putExtras(bundle);
			startActivityForResult(intent, VICTIM_REQUEST);
    	}else if(obj instanceof FemaMarking){
    		FemaMarking fm = (FemaMarking) obj;
    		currentFM = fm;
    		intent = new Intent(getApplicationContext(), FEMAActivity.class);
    		bundle = new Bundle();
    		bundle.putInt(FEMAActivity.FUNC, CHECK_FUNCTION);
    		bundle.putDouble(FEMAActivity.LAT, Double.parseDouble(fm.getLocationString().split(",")[0]));
			bundle.putDouble(FEMAActivity.LON, Double.parseDouble(fm.getLocationString().split(",")[1]));
			bundle.putInt(FEMAActivity.RESPONDER_ID, responderId);
			bundle.putString(FEMAActivity.STAMP, fm.getTimestamp());
			bundle.putInt(FEMAActivity.LIVE_NUM, fm.getLivingVictims());
			bundle.putInt(FEMAActivity.DEAD_NUM, fm.getDeadVictims());
			bundle.putString(FEMAActivity.COMMENT, fm.getDetails());
			intent.putExtras(bundle);
			startActivityForResult(intent, FEMA_REQUEST);
    	}
	}
	
	private Marker showVicMarker(PatientAssessment pa){
		double lat = Double.parseDouble(pa.getLocationString().split(",")[0]);
		double lon = Double.parseDouble(pa.getLocationString().split(",")[1]);
		switch(pa.getPriorityType()){
		case PatientAssessment.PRIORITY_RED:
			return map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
		            .icon(BitmapDescriptorFactory.fromResource(R.drawable.google_marker_red)));
		case PatientAssessment.PRIORITY_YELLOW:
			return map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.google_marker_yellow)));
		case PatientAssessment.PRIORITY_GREEN:
			return map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.google_marker_green)));
		case PatientAssessment.PRIORITY_BLACK:
			return map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.google_marker_black)));
		default:
			return null;
		}
	}
	
	private Marker showFemaMarker(FemaMarking fm){
		double lat = Double.parseDouble(fm.getLocationString().split(",")[0]);
		double lon = Double.parseDouble(fm.getLocationString().split(",")[1]);
		
		return map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.cross)));
	}
	
	private Marker showEmsMarker(EmergencyResponderUpdate ems){
		double lat = Double.parseDouble(ems.getLocationString().split(",")[0]);
		double lon = Double.parseDouble(ems.getLocationString().split(",")[1]);
		
		if(ems.getDetails().contains(REQUEST_MARK)){
			return map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ems2)));
		}else{
			return map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ems)));
		}
	}

	public class GPSReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();	
			Double lat = bundle.getDouble(GPSService.GPS_LAT);
			Double lon = bundle.getDouble(GPSService.GPS_LNG);
			currentLoc = new LatLng(lat, lon);
			if(emsToUpdate != null){
				emsToUpdate.setLocationString(lat+","+lon);
			}
			//Toast.makeText(getApplicationContext(), "location updated", 100).show();
		}
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(gpsReceiver);
		stopService(new Intent(getApplicationContext(), GPSService.class));
		
		// stop update from server
		updateTimer.cancel();
		
		Editor editor = pref.edit();
		editor.putInt(teamName+"///"+responderId, patientId);
		editor.commit();
	}
	
    private void centerMapOnMyLocation() {
    	map.setMyLocationEnabled(true);
    	LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location myLocation = locationManager.getLastKnownLocation(provider);

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
        
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10), 2000, null);
    }
    
    public double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius=6371;//radius of earth in Km         
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        
        return Radius * c;
    }
    
    public LatLng stringToLatlng(String latLngStr){
    	double lat = Double.parseDouble(latLngStr.split(",")[0]);
    	double lng = Double.parseDouble(latLngStr.split(",")[1]);
    	return new LatLng(lat, lng);
    }
    
    private void sendFemaNotification(FemaMarking fm){
    	Log.d(TAG, "send notification");
    	
    	Intent intent = new Intent(this, FEMAActivity.class);
    	Bundle bundle = new Bundle();
    	bundle.putInt(FEMAActivity.FUNC, CHECK_FUNCTION);
		bundle.putDouble(FEMAActivity.LAT, Double.parseDouble(fm.getLocationString().split(",")[0]));
		bundle.putDouble(FEMAActivity.LON, Double.parseDouble(fm.getLocationString().split(",")[1]));
		bundle.putInt(FEMAActivity.RESPONDER_ID, responderId);
		bundle.putString(FEMAActivity.STAMP, fm.getTimestamp());
		bundle.putInt(FEMAActivity.LIVE_NUM, fm.getLivingVictims());
		bundle.putInt(FEMAActivity.DEAD_NUM, fm.getDeadVictims());
		bundle.putString(FEMAActivity.COMMENT, fm.getDetails());
		intent.putExtras(bundle);
    	PendingIntent pIntent = PendingIntent.getActivity(this, FEMA_REQUEST, intent, 0);
    	
    	Notification note=new Notification(R.drawable.cross,
                "New FEMA marking found!",
                System.currentTimeMillis());
    	note.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
    	
    	note.setLatestEventInfo(this, "New FEMA marking", fm.getDetails(), pIntent);
    	
    	notificationManager.notify(1111, note); 
    }
    
    class MenuClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(v == victimMenuBtn){
				Toast.makeText(getApplicationContext(), "Victim menu clicked", Toast.LENGTH_SHORT).show();
				mode = Mode.VICTIM;
				modeText.setBackgroundColor(Color.BLACK);
				modeText.setTextColor(Color.WHITE);
				modeText.setText("Mode: Victim");
				range = Double.MAX_VALUE;
				selectLevel = 0;
			}else if(v == femaMenuBtn){
				Toast.makeText(getApplicationContext(), "FEMA menu clicked", Toast.LENGTH_SHORT).show();
				mode = Mode.FEMA;
				modeText.setBackgroundColor(Color.BLACK);
				modeText.setTextColor(Color.WHITE);
				modeText.setText("Mode: FEMA Marking");
				range = Double.MAX_VALUE;
				selectLevel = 0;
			}else if(v == selectMenuBtn){
				Toast.makeText(getApplicationContext(), "Search menu clicked", Toast.LENGTH_SHORT).show();
				modeText.setBackgroundColor(Color.BLACK);
				modeText.setTextColor(Color.WHITE);
				modeText.setText("Mode: Victim search");
				Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
				startActivityForResult(intent, SELECT_REQUEST);
			}else if(v == requestMenuBtn){
				Toast.makeText(getApplicationContext(), "Request menu clicked", Toast.LENGTH_SHORT).show();
				modeText.setBackgroundColor(Color.BLACK);
				modeText.setTextColor(Color.WHITE);
				modeText.setText("Mode: Help request");
				Intent intent = new Intent(getApplicationContext(), RequestActivity.class);
				startActivityForResult(intent, REQUEST_REQUEST);
				range = Double.MAX_VALUE;
				selectLevel = 0;
			}
		}
	}
	
	private class UpdateFromServ extends TimerTask{
		public void run(){
			// add new victim to server
			for(PatientAssessment pa : vicToAdd){
				Log.d(TAG, "send victim");
				rc.sendPatientAssessment(pa);
			}
			vicToAdd.clear();
			// update victim to server
			for(PatientAssessment pa : vicToUpdate){
				Log.d(TAG, "update victim");
				rc.updatePatientAssessment(pa);
			}
			vicToUpdate.clear();
			// synchronize victim from server
			final HashMap<PatientAssessment, Marker> newShownVicMarker = new HashMap<PatientAssessment, Marker>();
			List<PatientAssessment> patientAssessment = rc.getPatientAssessments();
			Log.d(TAG, "receive victim: "+patientAssessment.size());
			
			Log.d(TAG, "shownVicMarker length "+shownVicMarker.keySet().size());
			Log.d(TAG, "newShownVicMarker length "+newShownVicMarker.keySet().size());
			
			// filter results
			if(mode == Mode.SELECT){
				List<PatientAssessment> newPatientAssessment = new ArrayList<PatientAssessment>();
				for(PatientAssessment pa : patientAssessment){
					
					LatLng paLoc = stringToLatlng(pa.getLocationString());
					Log.d(TAG, "selected level: "+selectLevel);
					Log.d(TAG, "pa priority type: "+pa.getPriorityType());
					Log.d(TAG, "pa assessment: "+pa.getAssessmentType());
					if(currentLoc != null){
						Log.d(TAG, "distance: "+calculationByDistance(paLoc, currentLoc));
						if((selectLevel == 0 || pa.getPriorityType() == selectLevel) &&
								calculationByDistance(paLoc, currentLoc) < range){
							newPatientAssessment.add(pa);
						}
					}
				}
				patientAssessment.clear();
				patientAssessment.addAll(newPatientAssessment);
			}

			Log.d(TAG, "new size: "+patientAssessment.size());
			Log.d(TAG, "shownVicMarker length "+shownVicMarker.keySet().size());
			Log.d(TAG, "newShownVicMarker length "+newShownVicMarker.keySet().size());
			
			for(PatientAssessment pa : patientAssessment){
				final PatientAssessment finalPA = pa;
				boolean newVic = true;
				Iterator<PatientAssessment> paIterator = shownVicMarker.keySet().iterator();
				while(paIterator.hasNext()){
					PatientAssessment pas = paIterator.next();
					
					if(pa.getEmergencyResponseId() == pas.getEmergencyResponseId() && 
							pa.getLocationString().equals(pas.getLocationString()) &&
							pa.getAssessmentType() == PatientAssessment.ASSESSMENT_TYPE_TRIAGE){
						newShownVicMarker.put(pas, shownVicMarker.get(pas));
						paIterator.remove();
						newVic = false;
						break;
					}
				}
				if(newVic && pa.getAssessmentType() == PatientAssessment.ASSESSMENT_TYPE_TRIAGE){
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							newShownVicMarker.put(finalPA, showVicMarker(finalPA));							
						}
					});
				}
				Log.d(TAG, "PatientAssessmentType: "+pa.getAssessmentType());
				Log.d(TAG, "shownVicMarker length "+shownVicMarker.keySet().size());
				Log.d(TAG, "newShownVicMarker length "+newShownVicMarker.keySet().size());
			}
			if(shownVicMarker.size() > 0){
				for(PatientAssessment pas : shownVicMarker.keySet()){
					final PatientAssessment finalPAS = pas;
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							if(shownVicMarker.get(finalPAS) != null){
								shownVicMarker.get(finalPAS).remove();
							}
						}
					});
				}
			}
			shownVicMarker.clear();
			shownVicMarker.putAll(newShownVicMarker);

			// add new fema marking to server
			for(FemaMarking fm : femaToAdd){
				Log.d(TAG, "send FEMA marking");
				rc.sendFemaMarking(fm);
			}
			femaToAdd.clear();
			// update fema marking to server
			for(FemaMarking fm : femaToUpdate){
				Log.d(TAG, "update FEMA marking");
				rc.updateFemaMarking(fm);
			}
			femaToUpdate.clear();
			// synchronize fema marking to server
			final HashMap<FemaMarking, Marker> newShownFemaMarker = new HashMap<FemaMarking, Marker>();			
			List<FemaMarking> femaMarking = rc.getFemaMarking();
			Log.d(TAG, "receive FEMA marking: "+femaMarking.size());
			for(FemaMarking fm : femaMarking){
				final FemaMarking finalFM = fm;
				boolean newFema = true;
				Iterator<FemaMarking> fmIterator = shownFemaMarker.keySet().iterator();
				while(fmIterator.hasNext()){
					FemaMarking fms = fmIterator.next();
					Log.d(TAG, "FEMA on server: "+fm.getTimestamp());
					Log.d(TAG, "FEMA here: "+fms.getTimestamp());
					if(fm.getEmergencyResponseId() == fms.getEmergencyResponseId() &&
							fm.getLocationString().equals(fms.getLocationString()) &&
							fm.getTimestamp().equals(fms.getTimestamp())){
						newShownFemaMarker.put(fms, shownFemaMarker.get(fms));
						fmIterator.remove();
						newFema = false;
						break;
					}
				}
				Log.d(TAG, "newShownFemaMarker: "+newShownFemaMarker.size());
				Log.d(TAG, "shownFemaMarker: "+shownFemaMarker.size());
				if(newFema){
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							newShownFemaMarker.put(finalFM, showFemaMarker(finalFM));
							sendFemaNotification(finalFM);
						}
					});
				}
				Log.d(TAG, "newShownFemaMarker: "+newShownFemaMarker.size());
				Log.d(TAG, "shownFemaMarker: "+shownFemaMarker.size());
			}
			if(shownFemaMarker.size() > 0){
				for(final FemaMarking fms : shownFemaMarker.keySet()){
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							if(shownFemaMarker.get(fms) != null)
								shownFemaMarker.get(fms).remove();
						}
					});
				}
			}
			shownFemaMarker.clear();
			shownFemaMarker.putAll(newShownFemaMarker);
			Log.d(TAG, "shownFemaMarker: "+shownFemaMarker.size());
			
			// update self location
			Log.d(TAG, "send ems update");
			// change request mode
			if(emsToAdd != null){
				if(needHelp){
					emsToAdd.setDetails(REQUEST_MARK+teamName);
				}else{
					emsToAdd.setDetails(teamName);
				}
				rc.sendEmergencyResponderUpdate(emsToAdd);
			}
			if(emsToUpdate != null){
				if(needHelp){
					emsToUpdate.setDetails(REQUEST_MARK+teamName);
				}else{
					emsToUpdate.setDetails(teamName);
				}
				rc.updateEmergencyResponderUpdate(emsToUpdate);
				canBeUpdated = true;
			}
			// synchronize ems location to server
			final HashMap<EmergencyResponderUpdate, Marker> newShownEmsMarker = new HashMap<EmergencyResponderUpdate, Marker>();			
			List<EmergencyResponderUpdate> emsList = rc.getEmergencyResponseUpdates();
			Log.d(TAG, "receive EMS marking: "+emsList.size());
			for(EmergencyResponderUpdate ems1 : emsList){
				final EmergencyResponderUpdate finalEMS = ems1;
				Log.d(TAG, "EMS: "+ems1.getDetails()+" Responder: "+ems1.getEmergencyResponderId());
				boolean newEms = true;
				Iterator<EmergencyResponderUpdate> emsIterator = shownEmsMarker.keySet().iterator();
				while(emsIterator.hasNext()){
					EmergencyResponderUpdate ems2 = emsIterator.next();
					if(ems1.getEmergencyResponderId() == ems2.getEmergencyResponderId() &&
							ems1.getDetails().equals(ems2.getDetails()) &&
							ems1.getEmergencyResponderId() != responderId &&
							!ems1.getDetails().replace(REQUEST_MARK, "").equals(teamName)){
						newShownEmsMarker.put(ems2, shownEmsMarker.get(ems2));
						emsIterator.remove();
						newEms = false;
						break;
					}
				}
				if(newEms){
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							Log.d(TAG, "final EMS id: "+finalEMS.getEmergencyResponderId()+" to "+responderId);
							Log.d(TAG, "final EMS name: "+finalEMS.getDetails()+" to "+finalEMS.getDetails().replace(REQUEST_MARK, ""));
							if(finalEMS.getEmergencyResponderId() != responderId ||
									!finalEMS.getDetails().replace(REQUEST_MARK, "").equals(teamName))
								newShownEmsMarker.put(finalEMS, showEmsMarker(finalEMS));
						}
					});
				}
				// retrieve self ems for update
				if(canBeUpdated && ems1.getEmergencyResponderId() == responderId &&
						ems1.getDetails().replace(REQUEST_MARK, "").equals(teamName)){
					emsToUpdate = ems1;
					emsToAdd = null;
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							if(emsToUpdate.getDetails().contains(REQUEST_MARK)){
			    				requestText.setBackgroundColor(Color.BLACK);
			    				requestText.setTextColor(Color.WHITE);
			    				requestText.setText("Requesting help from other responders");
								needHelp = true;
							}else{
			    				requestText.setBackgroundColor(Color.TRANSPARENT);
			    				requestText.setTextColor(Color.TRANSPARENT);
			    				requestText.setText("");
								needHelp = false;
							}
						}
					});
				}
			}
			if(shownEmsMarker.size() > 0){
				for(final EmergencyResponderUpdate ems3 : shownEmsMarker.keySet()){
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							if(shownFemaMarker.get(ems3) != null)
								shownFemaMarker.get(ems3).remove();
						}
					});
				}
			}
			if(emsToUpdate == null && emsToAdd == null && currentLoc != null){
				emsToAdd = new EmergencyResponderUpdate();
				if(needHelp){
					emsToAdd.packageObject(0, responderId, EmergencyResponderUpdate.UPDATE_TYPE_LOCATION, RestClient.getTimeStamp(), currentLoc.latitude+","+currentLoc.longitude, Global.TRIAL_ID, REQUEST_MARK+teamName, Global.GROUP_ID);
				}else{
					emsToAdd.packageObject(0, responderId, EmergencyResponderUpdate.UPDATE_TYPE_LOCATION, RestClient.getTimeStamp(), currentLoc.latitude+","+currentLoc.longitude, Global.TRIAL_ID, teamName, Global.GROUP_ID);
				}
			}
			shownFemaMarker.clear();
			shownFemaMarker = newShownFemaMarker;
			// update all ems location on map
			for(EmergencyResponderUpdate ems4 : shownEmsMarker.keySet()){
				shownEmsMarker.get(ems4).setPosition(
						new LatLng(Double.parseDouble(ems4.getLocationString().split(",")[0]),
								Double.parseDouble(ems4.getLocationString().split(",")[1])));
			}
		}
	}

	
} 