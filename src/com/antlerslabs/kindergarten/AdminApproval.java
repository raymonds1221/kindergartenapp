package com.antlerslabs.kindergarten;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.KeyEvent;

import com.antlerslabs.kindergarten.helpers.Constants;

public class AdminApproval extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin_approval);
		
		findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(Constants.RESULT_ADMIN_APPROVAL);
				finish();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}
}
