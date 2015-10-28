package edu.junyao.usar;

import edu.fiveglabs.diorama.Models.DIORAMA.PatientAssessment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class VictimActivity extends Activity implements OnClickListener{
	private static final String TAG = "VictimActivity";
	public static final String FUNC = "function";
	public static final String LON = "longitude";
	public static final String LAT = "latitude";
	public static final String CUR_LAT = "current latitude";
	public static final String CUR_LON = "current longitude";
	public static final String TRIAGE = "triage";
	public static final String COMMENT = "comment";
	public static final String TYPE = "type";
	
	public TextView lat_tv, lon_tv;
	public Spinner pri_spin;
	public EditText detail_et;
	public Button cncl_btn, cnfm_btn, cur_loc_btn, evac_btn;
	
	private double lat, lon;
	private int func;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_victim);
		
		// register some ui components
		lat_tv = (TextView) findViewById(R.id.vic_lat);
		lon_tv = (TextView) findViewById(R.id.vic_long);
		pri_spin = (Spinner)findViewById(R.id.triage);
		detail_et = (EditText)findViewById(R.id.vic_detail);
		cur_loc_btn = (Button) findViewById(R.id.vic_cur_loc);
		cncl_btn = (Button)findViewById(R.id.vic_cancel);
		cnfm_btn = (Button)findViewById(R.id.vic_confirm);
		evac_btn = (Button)findViewById(R.id.vic_evac);
		 
		// retrieve location information from intent
		Bundle bundle = getIntent().getExtras();
		lat = bundle.getDouble(LAT);
		lon = bundle.getDouble(LON);
		lat_tv.setText(lat+"");
		lon_tv.setText(lon+"");
		cncl_btn.setOnClickListener(this);
		cnfm_btn.setOnClickListener(this);
		func = bundle.getInt(FUNC);
		
		if(func == MainActivity.ADD_FUNCTION){
			evac_btn.setVisibility(Button.INVISIBLE);
			cur_loc_btn.setVisibility(Button.VISIBLE);
			cur_loc_btn.setOnClickListener(this);
		}else{
			evac_btn.setVisibility(Button.VISIBLE);
			evac_btn.setOnClickListener(this);
			cur_loc_btn.setVisibility(Button.INVISIBLE);
			detail_et.setText(bundle.getString(COMMENT));
			pri_spin.setSelection(bundle.getInt(TRIAGE)-1);
		}
		
		// reset flags
		MainActivity.map_loaded = false;
		MainActivity.anim_ended = false;
	}

	@Override
	public void onClick(View v) {
		Intent returnIntent = new Intent();
		Bundle bundle;
		switch(v.getId()){
		case R.id.vic_confirm:
			String comment = detail_et.getText().toString();
			int priorityId = priorityToInt((String) pri_spin.getSelectedItem());
			bundle = new Bundle();
			bundle.putInt(VictimActivity.FUNC, func);
			bundle.putDouble(LAT, lat);
			bundle.putDouble(LON, lon);
			bundle.putInt(TRIAGE, priorityId);
			bundle.putString(COMMENT, comment);
			bundle.putInt(TYPE, PatientAssessment.ASSESSMENT_TYPE_TRIAGE);
			returnIntent.putExtras(bundle);
			setResult(RESULT_OK, returnIntent);
			finish();
			break;
		case R.id.vic_cancel:
			setResult(RESULT_CANCELED, returnIntent);
			finish();
			break;
		case R.id.vic_cur_loc:
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
		case R.id.vic_evac:
			String newComment = detail_et.getText().toString();
			int newPriorityId = priorityToInt((String) pri_spin.getSelectedItem());
			bundle = new Bundle();
			bundle.putInt(VictimActivity.FUNC, func);
			bundle.putDouble(LAT, lat);
			bundle.putDouble(LON, lon);
			bundle.putInt(TRIAGE, newPriorityId);
			bundle.putString(COMMENT, newComment);
			bundle.putInt(TYPE, PatientAssessment.ASSESSMENT_TYPE_IS_EVACUATED);
			returnIntent.putExtras(bundle);
			setResult(RESULT_OK, returnIntent);
			finish();
			break;
		}
	}
	
	public static int priorityToInt(String priority){
		if(priority.equals("RED"))
			return PatientAssessment.PRIORITY_RED;
		else if(priority.equals("GREEN"))
			return PatientAssessment.PRIORITY_GREEN;
		else if(priority.equals("BLACK"))
			return PatientAssessment.PRIORITY_BLACK;
		else if(priority.equals("YELLOW"))
			return PatientAssessment.PRIORITY_YELLOW;
		else
			return 0;
	}
	
	public static String intToPriority(int intNum){
		switch(intNum){
		case PatientAssessment.PRIORITY_BLACK:
			return "BLACK";
		case PatientAssessment.PRIORITY_GREEN:
			return "GREEN";
		case PatientAssessment.PRIORITY_RED:
			return "RED";
		case PatientAssessment.PRIORITY_YELLOW:
			return "YELLOW";
		default:
			return null;
		}
	}
}
