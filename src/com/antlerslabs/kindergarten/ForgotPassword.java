package com.antlerslabs.kindergarten;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.helpers.Network;
import com.antlerslabs.kindergarten.helpers.Message;

public class ForgotPassword extends Activity {
	private EditText email;
	private Network mNetwork;
	private WebServiceRequest mWebServiceRequest;
	private Parser mParser;
	private ProgressDialog mProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forgot_password);
		
		email = (EditText) findViewById(R.id.email);
		mNetwork = Network.getInstance(this);
		mWebServiceRequest = WebServiceRequest.newInstance();
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("Please wait...");
		mProgress.setCancelable(false);
		
		findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(email.getText().toString().equals("")) {
					Message.make(ForgotPassword.this, R.string.empty_string, R.string.enter_email).toast();
					return;
				}
				
				if(mNetwork.isAvailable()) {
					mProgress.show();
					new Thread(new Runnable() {
						public void run() {
							final String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																	.setApiName("user")
																	.addParameter("forgotpassword")
																	.putPostValues("email", email.getText().toString())
																	.setResultFormat(WebServiceRequest.DataFormat.XML)
																	.setRequestMethod(WebServiceRequest.POST)
																	.getRawResponse();
							runOnUiThread(new Runnable() {
								public void run() {
									mProgress.dismiss();
									mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
									Message.make(ForgotPassword.this, "", mParser.getSpecificValue("log").toString()).toast();
									
									if(mParser.getSpecificValue("status").toString().equals("success")) {
										finish();
									}
								}
							});
						}
					}).start();
				} else {
					Message.make(ForgotPassword.this, R.string.empty_string, R.string.check_network_connection);
				}
			}
		});
	}
}
