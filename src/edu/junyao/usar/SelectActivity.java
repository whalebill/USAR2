package edu.junyao.usar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SelectActivity extends Activity implements OnClickListener{
	private static final String TAG = "SelectActivity";
	public static final String RANGE = "range";
	public static final String LEVEL = "level";
	
	private Spinner filter_lv_sp;
	private EditText range_ed;
	private Button cnfm_btn, cncl_btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selection);
		
		// register some ui components
		filter_lv_sp = (Spinner) findViewById(R.id.filter_level);
		range_ed = (EditText) findViewById(R.id.distance);
		cnfm_btn = (Button) findViewById(R.id.select_confirm);
		cncl_btn = (Button) findViewById(R.id.select_cancel);
		cnfm_btn.setOnClickListener(this);
		cncl_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent returnIntent = new Intent();
		Bundle bundle;
		switch(v.getId()){
		case R.id.select_cancel:
			setResult(RESULT_CANCELED, returnIntent);
			finish();
			break;
		case R.id.select_confirm:
			double range = 0;
			if(range_ed.getText().toString().length() > 0){
				range = Double.parseDouble(range_ed.getText().toString());
			}
			int level = filter_lv_sp.getSelectedItemPosition();
			bundle = new Bundle();
			bundle.putDouble(RANGE, range);
			bundle.putInt(LEVEL, level);
			returnIntent.putExtras(bundle);
			setResult(RESULT_OK, returnIntent);
			finish();
			break;
		}
	}
}
