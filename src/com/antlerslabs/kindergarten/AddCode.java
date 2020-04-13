package com.antlerslabs.kindergarten;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import com.antlerslabs.kindergarten.helpers.Constants;
import com.antlerslabs.kindergarten.helpers.Message;

import com.google.zxing.client.android.CaptureActivity;

public class AddCode extends Activity {
	private EditText code;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.code_entry);
		
		code = (EditText) findViewById(R.id.code);
		
		findViewById(R.id.qr_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AddCode.this, CaptureActivity.class);
				startActivityForResult(intent, Constants.REQUEST_CODE_QR_CODE);
			}
		});
		
		findViewById(R.id.send_code_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(code.getText().toString().equals("")) {
					Message.make(AddCode.this, R.string.empty_string, R.string.enter_code).toast();
					return;
				}
				
				Intent intent = new Intent();
				intent.putExtra("code", code.getText().toString());
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Constants.REQUEST_CODE_QR_CODE && resultCode == RESULT_OK) {
			code.setText(data.getExtras().getString("code"));
		}
	}
}
