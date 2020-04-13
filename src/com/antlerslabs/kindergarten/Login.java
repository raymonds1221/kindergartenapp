package com.antlerslabs.kindergarten;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.pojo.Profile;

public class Login extends Activity {
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
		setContentView(R.layout.login);
		
		email = (EditText) findViewById(R.id.email);
		password = (EditText) findViewById(R.id.password);
		mNetwork = Network.getInstance(this);
		mPrefsWrapper = PreferenceWrapper.getInstance(this);
		mWebServiceRequest = WebServiceRequest.newInstance();
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("Loading...");
		mProgress.setCancelable(false);
		
		findViewById(R.id.forgot_password).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Login.this, ForgotPassword.class);
				startActivity(intent);
			}
		});
		
		findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Login.this, Registration.class);
				startActivityForResult(intent, Constants.REQUEST_CODE_REGISTRATION);
			}
		});
		
		findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(email.getText().toString().equals("")) {
					Message.make(Login.this, R.string.empty_string, R.string.enter_email).toast();
					return;
				}
				if(password.getText().toString().equals("")) {
					Message.make(Login.this, R.string.empty_string, R.string.enter_password).toast();
					return;
				}
				
				if(mNetwork.isAvailable()) {
					mProgress.show();
					if(mPrefsWrapper.getPreferenceBooleanValue(Constants.REGISTERED_TO_CLOUD)) {
						new Thread(new Runnable() {
							public void run() {
								final String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																		.setApiName("user")
																		.addParameter("login")
																		.putPostValues("username", email.getText().toString())
																		.putPostValues("password", password.getText().toString())
																		.setResultFormat(WebServiceRequest.DataFormat.XML)
																		.setRequestMethod(WebServiceRequest.POST)
																		.getRawResponse();
								runOnUiThread(new Runnable() {
									public void run() {
										mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
										
										if(!mParser.getSpecificValue("status").toString().equals("failure")) {
											int loginStatus = Integer.parseInt(mParser.getSpecificValue("login_status").toString());
											
											if(loginStatus == 1) {
												int userId = Integer.parseInt(mParser.getSpecificValue("user_id").toString());
												mPrefsWrapper.setPreferenceBooleanValue(Constants.USER_IS_REGISTERED, true);
												mPrefsWrapper.setPreferenceIntValue(Constants.USER_ID, userId);
												checkProfiles(userId);
											} else {
												mProgress.dismiss();
												Message.make(Login.this, "", "Ugyldig brukernavn og passord").alert();
											}
										} else {
											mProgress.dismiss();
											Message.make(Login.this, "", mParser.getSpecificValue("log").toString()).alert();
										}
									}
								});
							}
						}).start();
					} else {
						Message.make(Login.this, R.string.empty_string, R.string.no_registration_id).toast();
					}
				} else {
					Message.make(Login.this, R.string.empty_string, R.string.check_network_connection).alert();
				}
			}
		});
	}
	
	private void checkProfiles(final int userId) {
		new Thread(new Runnable() {
			public void run() {
				final List<Profile> profiles = mWebServiceRequest.setApiUri(getString(R.string.api_url))
															.setApiName("profile")
															.addParameter("profilesalt")
															.addParameter("userid")
															.addParameter(String.valueOf(userId))
															.setResultFormat(WebServiceRequest.DataFormat.XML)
															.setRequestMethod(WebServiceRequest.GET)
															.getForObjects(Profile.class);
				runOnUiThread(new Runnable() {
					public void run() {
						mProgress.dismiss();
						Intent intent = new Intent();
						if(profiles != null && profiles.size() > 0) {
							mPrefsWrapper.setPreferenceIntValue(Constants.DEFAULT_KG_ID, profiles.get(0).getUid());
							mPrefsWrapper.setPreferenceBooleanValue(Constants.USER_IS_TEACHER, profiles.get(0).getType().equals("TEACHER"));
							intent.setClass(Login.this, MainActivity.class);
						} else {
							intent.setClass(Login.this, CodeEntry.class);
						}
						startActivity(intent);
						finish();
					}
				});
			}
		}).start();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Constants.REQUEST_CODE_REGISTRATION && resultCode == Constants.RESULT_REGISTRATION) {
			Intent intent = new Intent(this, CodeEntry.class);
			startActivity(intent);
			finish();
		}
	}
}
