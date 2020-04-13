package com.antlerslabs.kindergarten;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.pojo.Kindergarden;

public class ContactForm extends Activity {
	private EditText contact_title;
	private EditText contact_email;
	private EditText contact_body;
	private TextView kindergarden_name;
	private Network mNetwork;
	private PreferenceWrapper mPrefsWrapper;
	private WebServiceRequest mWebServiceRequest;
	private Parser mParser;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_form);
		
		contact_title = (EditText) findViewById(R.id.contact_title);
		contact_email = (EditText) findViewById(R.id.contact_email);
		contact_body = (EditText) findViewById(R.id.contact_body);
		kindergarden_name = (TextView) findViewById(R.id.kindergarden_name);
		
		mNetwork = Network.getInstance(this);
		mPrefsWrapper = PreferenceWrapper.getInstance(this);
		mWebServiceRequest = WebServiceRequest.newInstance();
		
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null) {
			Kindergarden profile = (Kindergarden) bundle.getParcelable(Constants.DEFAULT_PROFILE);
			kindergarden_name.setText("TIL " + profile.getName().toUpperCase());
		}
		
		findViewById(R.id.abort_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		findViewById(R.id.send_message).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(contact_title.getText().toString().equals("") &&
						contact_email.getText().toString().equals("") &&
						contact_body.getText().toString().equals("")) {
					Message.make(ContactForm.this, R.string.empty_string, R.string.no_content).toast();
				}
				if(contact_title.getText().toString().equals("")) {
					Message.make(ContactForm.this, R.string.empty_string, R.string.enter_title).toast();
					return;
				}
				if(contact_email.getText().toString().equals("")) {
					Message.make(ContactForm.this, R.string.empty_string, R.string.enter_email).toast();
					return;
				}
				if(contact_body.getText().toString().equals("")) {
					Message.make(ContactForm.this, R.string.empty_string, R.string.enter_body).toast();
					return;
				}
				if(!mNetwork.isAvailable()) {
					Message.make(ContactForm.this, R.string.empty_string, R.string.check_network_connection).toast();
					return;
				}
				
				final ProgressDialog progressDialog = new ProgressDialog(ContactForm.this);
				progressDialog.setMessage("Please wait...");
				progressDialog.show();
				
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																.setApiName("sendmail")
																.addParameter("send")
																.putPostValues("kgid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																.putPostValues("userid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID)))
																.putPostValues("email", contact_email.getText().toString())
																.putPostValues("subject", contact_title.getText().toString())
																.putPostValues("message", contact_body.getText().toString())
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.setRequestMethod(WebServiceRequest.POST)
																.getRawResponse();
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							
							runOnUiThread(new Runnable() {
								public void run() {
									progressDialog.dismiss();
									if(mParser.getSpecificValue("status") != null && mParser.getSpecificValue("status").toString().equals("success")) {
										finish();
									} else {
										Message.make(ContactForm.this, "", mParser.getSpecificValue("log").toString()).toast();
									}
								}
							});
						}
					}
				}).start();
			}
		});
	}
}
