package com.antlerslabs.kindergarten;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.widget.EditText;

import com.google.android.gcm.GCMRegistrar;

import com.antlerslabs.kindergarten.service.IKGService;
import com.antlerslabs.kindergarten.service.IKGServiceCallback;
import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.Constants;
import com.antlerslabs.kindergarten.helpers.Message;
import com.antlerslabs.kindergarten.helpers.Network;
import com.antlerslabs.kindergarten.helpers.PreferenceWrapper;
import com.antlerslabs.kindergarten.net.WebServiceRequest;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.pojo.Code;
import com.antlerslabs.kindergarten.service.*;

import com.google.zxing.client.android.CaptureActivity;

public class CodeEntry extends Activity {
	private EditText code;
	private EditText firstname;
	private EditText lastname;
	private WebServiceRequest mWebServiceRequest;
	private DataSource mDataSource;
	private PreferenceWrapper mPrefsWrapper;
	private Network mNetwork;
	private Parser mParser;
	private IKGService mIKGService;
	private boolean hasRequestedProfiles;
	private ProgressDialog mProgressDialog;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.code_entry);
		
		code = (EditText) findViewById(R.id.code);
		firstname = (EditText) findViewById(R.id.firstname);
		lastname = (EditText) findViewById(R.id.lastname);
		mWebServiceRequest = WebServiceRequest.newInstance();
		mDataSource = DataSourceFactory.getDataSourceFactory(this);
		mPrefsWrapper = PreferenceWrapper.getInstance(this);
		mNetwork = Network.getInstance(this);
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setMessage("Loading...");
		
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null && bundle.getBoolean(Constants.SHOW_TEACHER_NAME)) {
			findViewById(R.id.teacher_name).setVisibility(View.VISIBLE);
		}
		
		findViewById(R.id.qr_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CodeEntry.this, CaptureActivity.class);
				startActivityForResult(intent, Constants.REQUEST_CODE_QR_CODE);
			}
		});
		
		findViewById(R.id.send_code_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(findViewById(R.id.teacher_name).getVisibility() == View.VISIBLE) {
					return;
				}
				if(code.getText().toString().equals("")) {
					Message.make(CodeEntry.this, R.string.empty_string, R.string.enter_code).toast();
					return;
				}
				if(!mNetwork.isAvailable()) {
					Message.make(CodeEntry.this,  R.string.empty_string, R.string.check_network_connection).alert();
					return;
				}
				
				mProgressDialog.show();
				new Thread(new Runnable() {
					public void run() {
						final String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																.setApiName("profile")
																.addParameter("add")
																.putPostValues("code", code.getText().toString())
																.putPostValues("userid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID)))
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.setRequestMethod(WebServiceRequest.POST)
																.getRawResponse();
						mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
						
						runOnUiThread(new Runnable() {
							public void run() {
								mProgressDialog.dismiss();
								if(mParser.getSpecificValue("status").toString().equals("success")) {
									int kgId = Integer.parseInt(mParser.getSpecificValue("KG_id").toString());
									boolean isTeacher = mParser.getSpecificValue("type").toString().equals("TEACHER");
									mPrefsWrapper.setPreferenceIntValue(Constants.DEFAULT_KG_ID, kgId);
									mPrefsWrapper.setPreferenceBooleanValue(Constants.USER_IS_TEACHER, isTeacher);
									
									Intent intent = new Intent();
									
									if(isTeacher) {
										intent.setClass(CodeEntry.this, AdminApproval.class);
									} else {
										intent.setClass(CodeEntry.this, MainActivity.class);
									}
									startActivityForResult(intent, Constants.REQUEST_CODE_ADMIN_APPROVAL);
								} else {
									Message.make(CodeEntry.this, "", mParser.getSpecificValue("log").toString()).toast();
								}
							}
						});
					}
				}).start();
				
				/*if(mPrefsWrapper.getPreferenceBooleanValue(Constants.REGISTERED_TO_CLOUD)) {
					mProgressDialog.show();
					new Thread(new Runnable() {
						public void run() {
							mPrefsWrapper.setPreferenceStringValue(Constants.LAST_ENTERED_CODE, code.getText().toString());
							final String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																.setApiName("user")
																.addParameter("add")
																.putPostValues("cloud_registration_id", mPrefsWrapper.getPreferenceStringValue(Constants.CLOUD_REGISTRATION_ID))
																.putPostValues("code", code.getText().toString())
																.putPostValues("source", "ANDROID")
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.setRequestMethod(WebServiceRequest.POST)
																.getRawResponse();
							runOnUiThread(new Runnable() {
								public void run() {
									mProgressDialog.dismiss();
									mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
									if(mParser.getSpecificValue("status").equals("success")) {
										mPrefsWrapper.setPreferenceBooleanValue(Constants.USER_IS_REGISTERED, true);
										int userId = Integer.parseInt(mParser.getSpecificValue("user_id").toString());
										mPrefsWrapper.setPreferenceIntValue(Constants.USER_ID, userId);
										int kgId = Integer.parseInt(mParser.getSpecificValue("KG_id").toString());
										mPrefsWrapper.setPreferenceIntValue(Constants.DEFAULT_KG_ID, kgId);
										
										final boolean is_teacher = mParser.getSpecificValue("type").toString().equals("TEACHER");
										mPrefsWrapper.setPreferenceBooleanValue(Constants.USER_IS_TEACHER, is_teacher);
										
										Code c = new Code();
										c.setKgId(kgId);
										c.setValue(code.getText().toString());
										mDataSource.insert(c);
										
										Intent intent = new Intent(CodeEntry.this, MainActivity.class);
										intent.putExtra(Constants.HAS_REQUESTED_PROFILES, hasRequestedProfiles);
										startActivity(intent);
										finish();
									} else {
										Message.make(CodeEntry.this, "", mParser.getSpecificValue("log").toString()).alert();
									}
								}
							});
						}
					}).start();
				} else {
					Message.make(CodeEntry.this, R.string.empty_string, R.string.no_registration_id).toast();
				}*/
			}
		});
		
		findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.teacher_name).setVisibility(View.GONE);
			}
		});
		
		findViewById(R.id.send_name_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstname.getText().toString().equals("")) {
					Message.make(CodeEntry.this, R.string.empty_string, R.string.enter_firstname).toast();
					return;
				}
				if(lastname.getText().toString().equals("")) {
					Message.make(CodeEntry.this, R.string.empty_string, R.string.enter_lastname).toast();
					return;
				}
				if(!mNetwork.isAvailable()) {
					Message.make(CodeEntry.this, R.string.empty_string, R.string.check_network_connection).alert();
					return;
				}
				mProgressDialog.show();
				new Thread(new Runnable() {
					public void run() {
						int userId = mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID);
						final String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
															.setApiName("user")
															.addParameter("update")
															.putPostValues("userid", String.valueOf(userId))
															.putPostValues("firstname", firstname.getText().toString())
															.putPostValues("lastname", lastname.getText().toString())
															.setRequestMethod(WebServiceRequest.POST)
															.getRawResponse();
						
						runOnUiThread(new Runnable() {
							public void run() {
								mProgressDialog.dismiss();
								mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
								if(mParser.getSpecificValue("status").equals("success")) {
									mPrefsWrapper.setPreferenceBooleanValue(Constants.TEACHER_HAS_NAME, true);
									Intent intent = new Intent(CodeEntry.this, MainActivity.class);
									intent.putExtra(Constants.HAS_REQUESTED_PROFILES, hasRequestedProfiles);
									startActivity(intent);
									finish();
								} else {
									Message.make(CodeEntry.this, R.string.empty_string, R.string.unable_to_update_user).alert();
								}
							}
						});
					}
				}).start();
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Intent intent = new Intent(this, KGService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Constants.REQUEST_CODE_QR_CODE && resultCode == RESULT_OK) {
			code.setText(data.getExtras().getString("code"));
		} else if(requestCode == Constants.REQUEST_CODE_ADMIN_APPROVAL && resultCode == Constants.RESULT_ADMIN_APPROVAL) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unbindService(mServiceConnection);
		
		if(!mPrefsWrapper.getPreferenceBooleanValue(Constants.USER_IS_REGISTERED)) {
			GCMRegistrar.unregister(this);
		}
	}
	
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mIKGService = IKGService.Stub.asInterface(service);
			
			try {
				mIKGService.registerCallback(mKGServiceCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mIKGService = null;
			
			try {
				mIKGService.unregisterCallback(mKGServiceCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};
	
	private final IKGServiceCallback.Stub mKGServiceCallback = new IKGServiceCallback.Stub() {
		@Override
		public void onFinishRequestingProfiles() throws RemoteException {
			hasRequestedProfiles = true;
		}
	};
}
