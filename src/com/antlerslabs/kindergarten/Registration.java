package com.antlerslabs.kindergarten;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

import com.antlerslabs.kindergarten.helpers.Message;
import com.antlerslabs.kindergarten.helpers.PreferenceWrapper;
import com.antlerslabs.kindergarten.helpers.Network;
import com.antlerslabs.kindergarten.helpers.Constants;
import com.antlerslabs.kindergarten.net.WebServiceRequest;
import com.antlerslabs.kindergarten.parser.*;

public class Registration extends Activity {
	private EditText firstname;
	private EditText lastname;
	private EditText email;
	private EditText password;
	private Network mNetwork;
	private PreferenceWrapper mPrefsWrapper;
	private WebServiceRequest mWebServiceRequest;
	private Parser mParser;
	private ProgressDialog mProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		
		firstname = (EditText) findViewById(R.id.firstname);
		lastname = (EditText) findViewById(R.id.lastname);
		email = (EditText) findViewById(R.id.email);
		password = (EditText) findViewById(R.id.password);
		
		mNetwork = Network.getInstance(this);
		mPrefsWrapper = PreferenceWrapper.getInstance(this);
		mWebServiceRequest = WebServiceRequest.newInstance();
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("Please wait...");
		mProgress.setCancelable(false);
		
		findViewById(R.id.create_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstname.getText().toString().equals("")) {
					Message.make(Registration.this, R.string.empty_string, R.string.enter_firstname).toast();
					firstname.requestFocus();
					return;
				}
				if(lastname.getText().toString().equals("")) {
					Message.make(Registration.this, R.string.empty_string, R.string.enter_lastname).toast();
					lastname.requestFocus();
					return;
				}
				if(email.getText().toString().equals("")) {
					Message.make(Registration.this, R.string.empty_string, R.string.enter_email).toast();
					email.requestFocus();
					return;
				}
				if(password.getText().toString().equals("")) {
					Message.make(Registration.this, R.string.empty_string, R.string.enter_password).toast();
					password.requestFocus();
					return;
				}
				
				mProgress.show();
				if(mNetwork.isAvailable()) {
					if(mPrefsWrapper.getPreferenceBooleanValue(Constants.REGISTERED_TO_CLOUD)) {
						new Thread(new Runnable() {
							public void run() {
								final String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																		.setApiName("user")
																		.addParameter("add")
																		.putPostValues("firstname", firstname.getText().toString())
																		.putPostValues("lastname", lastname.getText().toString())
																		.putPostValues("email", email.getText().toString())
																		.putPostValues("password", password.getText().toString())
																		.putPostValues("cloud_registration_id", mPrefsWrapper.getPreferenceStringValue(Constants.CLOUD_REGISTRATION_ID))
																		.putPostValues("source", "ANDROID")
																		.setResultFormat(WebServiceRequest.DataFormat.XML)
																		.setRequestMethod(WebServiceRequest.POST)
																		.getRawResponse();
								runOnUiThread(new Runnable() {
									public void run() {
										mProgress.dismiss();
										mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
										
										if(mParser.getSpecificValue("status").toString().equals("success")) {
											mPrefsWrapper.setPreferenceBooleanValue(Constants.USER_IS_REGISTERED, true);
											int userId = Integer.parseInt(mParser.getSpecificValue("user_id").toString());
											mPrefsWrapper.setPreferenceIntValue(Constants.USER_ID, userId);
											setResult(Constants.RESULT_REGISTRATION);
											finish();
										} else {
											Message.make(Registration.this, "", mParser.getSpecificValue("log").toString()).alert();
										}
									}
								});
							}
						}).start();
					} else {
						Message.make(Registration.this, R.string.empty_string, R.string.no_registration_id).toast();
					}
				} else {
					Message.make(Registration.this, R.string.empty_string, R.string.check_network_connection);
				}
			}
		});
		
		findViewById(R.id.forgot_password).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Registration.this, ForgotPassword.class);
				startActivity(intent);
			}
		});
		
		findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
