package com.antlerslabs.kindergarten;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;

public class TeacherName extends Activity {
	private EditText firstname;
	private EditText lastname;
	private Network mNetwork;
	private PreferenceWrapper mPrefsWrapper;
	private WebServiceRequest mWebServiceRequest;
	private Parser mParser;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teacher_name);
		
		firstname = (EditText) findViewById(R.id.firstname);
		lastname = (EditText) findViewById(R.id.lastname);
		
		mNetwork = Network.getInstance(this);
		mPrefsWrapper = PreferenceWrapper.getInstance(this);
		mWebServiceRequest = WebServiceRequest.newInstance();
		
		findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		findViewById(R.id.send_name_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstname.getText().toString().equals("")) {
					Message.make(TeacherName.this, R.string.empty_string, R.string.enter_firstname).toast();
					return;
				}
				if(lastname.getText().toString().equals("")) {
					Message.make(TeacherName.this, R.string.empty_string, R.string.enter_lastname).toast();
					return;
				}
				if(!mNetwork.isAvailable()) {
					Message.make(TeacherName.this, R.string.empty_string, R.string.check_network_connection).alert();
					return;
				}
				new Thread(new Runnable() {
					public void run() {
						int userId = mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID);
						final String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
															.setApiName("user")
															.addParameter("update")
															.putPostValues("userid", String.valueOf(userId))
															.putPostValues("firstname", firstname.getText().toString())
															.putPostValues("lastname", lastname.getText().toString())
															.setResultFormat(WebServiceRequest.DataFormat.XML)
															.setRequestMethod(WebServiceRequest.POST)
															.getRawResponse();
						
						runOnUiThread(new Runnable() {
							public void run() {
								mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
								if(mParser.getSpecificValue("status") != null && mParser.getSpecificValue("status").equals("success")) {
									mPrefsWrapper.setPreferenceBooleanValue(Constants.TEACHER_HAS_NAME, true);
									finish();
								} else {
									Message.make(TeacherName.this, R.string.empty_string, R.string.unable_to_update_user).alert();
								}
							}
						});
					}
				}).start();
			}
		});
	}
}
