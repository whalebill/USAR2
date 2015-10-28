package edu.junyao.usar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class RequestActivity extends Activity implements OnClickListener{
	private static final String TAG = "RequestActivtiy";
	public static final String SWITCH = "switch";
	
	private RadioButton enable_btn, disable_btn;
	private Button cncl_btn, cnfm_btn;
	private RadioGroup switch_group;
	
	private boolean enableRequest = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request);
		
		// register some ui components
		enable_btn = (RadioButton) findViewById(R.id.enable_request);
		disable_btn = (RadioButton) findViewById(R.id.disable_request);
		switch_group = (RadioGroup) findViewById(R.id.switch_group);
		cncl_btn = (Button) findViewById(R.id.request_cancel);
		cnfm_btn = (Button) findViewById(R.id.request_confirm);
		
		disable_btn.setChecked(true);
		
		enable_btn.setOnClickListener(this);
		disable_btn.setOnClickListener(this);
		cncl_btn.setOnClickListener(this);
		cnfm_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent returnIntent = new Intent();
		Bundle bundle;
		switch(v.getId()){
		case R.id.enable_request:
			enableRequest = true;
			break;
		case R.id.disable_request:
			enableRequest = false;
			break;
		case R.id.request_cancel:
			setResult(RESULT_CANCELED, returnIntent);
			finish();
			break;
		case R.id.request_confirm:
			bundle = new Bundle();
			bundle.putBoolean(SWITCH, enableRequest);
			returnIntent.putExtras(bundle);
			setResult(RESULT_OK, returnIntent);
			finish();
			break;
		}
	}
	
	
}
