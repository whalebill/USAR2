package edu.junyao.usar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnClickListener{
	private static final String TAG = "LoginActivity";
	public static final String TEAM_NAME = "team name";
	public static final String RESPONDER_ID = "responder id";
	
	private EditText team_name_ed, responder_id_ed;
	private Button cncl_btn, cnfm_btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// register some ui components
		team_name_ed = (EditText) findViewById(R.id.team_name);
		responder_id_ed = (EditText) findViewById(R.id.responder_id);
		cncl_btn = (Button) findViewById(R.id.login_cancel);
		cnfm_btn = (Button) findViewById(R.id.login_confirm);
		cncl_btn.setOnClickListener(this);
		cnfm_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent returnIntent = new Intent();
		Bundle bundle;
		switch(v.getId()){
		case R.id.login_cancel:
			setResult(RESULT_CANCELED, returnIntent);
			finish();
			break;
		case R.id.login_confirm:
			String teamName = team_name_ed.getText().toString();
			int responderId = Integer.parseInt(responder_id_ed.getText().toString());
			bundle = new Bundle();
			bundle.putString(TEAM_NAME, teamName);
			bundle.putInt(RESPONDER_ID, responderId);
			returnIntent.putExtras(bundle);
			setResult(RESULT_OK, returnIntent);
			finish();
			break;
		}
	}
	
	
}
