package edu.junyao.usar;

import edu.fiveglabs.diorama.Models.DIORAMA.FemaMarking;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FEMAActivity extends Activity implements OnClickListener{
	private static final String TAG = "FEMAActivity";
	public static final String FUNC = "function";
	public static final String LON = "longitude";
	public static final String LAT = "latitude";
	public static final String CUR_LAT = "current latitude";
	public static final String CUR_LON = "current longitude";
	public static final String STAMP = "time stamp";
	public static final String RESPONDER_ID = "responder id";
	public static final String LIVE_NUM = "live number";
	public static final String DEAD_NUM = "dead number";
	public static final String COMMENT = "comment";
	
	public TextView lat_tv, lon_tv, id_tv, time_tv;
	public EditText detail_et, live_et, dead_et;
	public Button cncl_btn, cnfm_btn, cur_loc_btn;

	private double lat, lon;
	private String timeStamp;
	private long responderId;
	private int func;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fema);
		
		// register some ui components
		lat_tv = (TextView) findViewById(R.id.fema_lat);
		lon_tv = (TextView) findViewById(R.id.fema_long);
		id_tv = (TextView) findViewById(R.id.identifier);
		time_tv = (TextView) findViewById(R.id.time_date);
		detail_et = (EditText) findViewById(R.id.hazard);
		live_et = (EditText) findViewById(R.id.live);
		dead_et = (EditText) findViewById(R.id.dead);
		cur_loc_btn = (Button) findViewById(R.id.Cur_fema_Loc);
		cncl_btn = (Button) findViewById(R.id.fema_cancel);
		cnfm_btn = (Button) findViewById(R.id.fema_confirm);
		
		// retrieve location information from intent
		Bundle bundle = getIntent().getExtras();
		lat = bundle.getDouble(LAT);
		lon = bundle.getDouble(LON);
		lat_tv.setText(lat+"");
		lon_tv.setText(lon+"");
		timeStamp = bundle.getString(STAMP);
		responderId = bundle.getInt(RESPONDER_ID);
		time_tv.setText(timeStamp);
		id_tv.setText(responderId+"");
		cncl_btn.setOnClickListener(this);
		cnfm_btn.setOnClickListener(this);
		func = bundle.getInt(FUNC);
		
		if(func == MainActivity.ADD_FUNCTION){
			cur_loc_btn.setVisibility(Button.VISIBLE);
			cur_loc_btn.setOnClickListener(this);
		}else if(func == MainActivity.CHECK_FUNCTION){
			cur_loc_btn.setVisibility(Button.INVISIBLE);
			detail_et.setText(bundle.getString(COMMENT));
			live_et.setText(bundle.getInt(LIVE_NUM)+"");
			dead_et.setText(bundle.getInt(DEAD_NUM)+"");
		}
		
		// reset flags
		MainActivity.anim_ended = false;
		MainActivity.map_loaded = false;
	}

	@Override
	public void onClick(View v) {
		Intent returnIntent = new Intent();
		Bundle bundle;
		switch(v.getId()){
		case R.id.fema_confirm:
			String comment = detail_et.getText().toString();
			int liveNum = Integer.parseInt(live_et.getText().toString());
			int deadNum = Integer.parseInt(dead_et.getText().toString());
			bundle = new Bundle();
			bundle.putInt(FEMAActivity.FUNC, func);
			bundle.putDouble(LAT, lat);
			bundle.putDouble(LON, lon);
			bundle.putInt(LIVE_NUM, liveNum);
			bundle.putInt(DEAD_NUM, deadNum);
			bundle.putString(STAMP, timeStamp);
			bundle.putString(COMMENT, comment);
			returnIntent.putExtras(bundle);
			setResult(RESULT_OK, returnIntent);
			finish();
			break;
		case R.id.fema_cancel:
			setResult(RESULT_CANCELED, returnIntent);
			finish();
			break;
		case R.id.Cur_fema_Loc:
			bundle = getIntent().getExtras();
			double curLat = bundle.getDouble(CUR_LAT);
			double curLon = bundle.getDouble(CUR_LON);
			if(curLat == Double.NaN || curLon == Double.NaN){
				Toast.makeText(getApplicationContext(), "current location not available", Toast.LENGTH_SHORT).show();
			}else{
				lat = curLat;
				lon = curLon;
				lat_tv.setText(lat+"");
				lon_tv.setText(lon+"");
			}
			break;
		}		
	}

}
