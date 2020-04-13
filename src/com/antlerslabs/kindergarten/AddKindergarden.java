package com.antlerslabs.kindergarten;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ImageButton;
import android.widget.ViewSwitcher;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.adapter.Kindergarden02Adapter;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.pojo.Code;
import com.antlerslabs.kindergarten.pojo.Kindergarden;

public class AddKindergarden extends Activity {
	private ListView list_kindergarden;
	private ViewSwitcher kindergarden_switcher;
	private WebServiceRequest mWebServiceRequest;
	private DataSource mDataSource;
	private Network mNetwork;
	private PreferenceWrapper mPrefsWrapper;
	private Parser mParser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_kindergarden);
		
		list_kindergarden = (ListView) findViewById(R.id.list_kindergarden);
		kindergarden_switcher = (ViewSwitcher) findViewById(R.id.kindergarden_switcher);
		mWebServiceRequest = WebServiceRequest.newInstance();
		mDataSource = DataSourceFactory.getDataSourceFactory(this);
		mNetwork = Network.getInstance(this);
		mPrefsWrapper = PreferenceWrapper.getInstance(this);
		
		displayKindergarden();
		
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, AbsListView.LayoutParams.FILL_PARENT);
		LinearLayout linearLayout = new LinearLayout(this);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.RIGHT;
		ImageButton add_btn = new ImageButton(this);
		add_btn.setLayoutParams(layoutParams);
		add_btn.setImageResource(R.drawable.add_btn);
		add_btn.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		linearLayout.addView(add_btn);
		linearLayout.setLayoutParams(params);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		list_kindergarden.addFooterView(linearLayout);
		
		add_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AddKindergarden.this, AddCode.class);
				startActivityForResult(intent, Constants.REQUEST_CODE_ADD_CODE);
			}
		});
		
		findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void displayKindergarden() {
		
		new Thread(new Runnable() {
			public void run() {
				synchronized(Constants.mLockObject) {
					final List<Kindergarden> profiles = mDataSource.getForObjects(Kindergarden.class);
					
					runOnUiThread(new Runnable() {
						public void run() {
							Kindergarden02Adapter adapter = new Kindergarden02Adapter(AddKindergarden.this, R.layout.list_kindergarden_02, profiles);
							list_kindergarden.setAdapter(adapter);
							kindergarden_switcher.setDisplayedChild(0);
						}
					});
				}
			}
		}).start();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Constants.REQUEST_CODE_ADD_CODE && resultCode == RESULT_OK) {
			final String code = data.getExtras().getString("code");
			
			mDataSource.where("value", code)
						.getForObject(Code.class);
			
			if(mDataSource.getRecordCount() > 0) {
				Message.make(this, R.string.empty_string, R.string.code_exist).toast();
				return;
			}
			
			if(mNetwork.isAvailable()) {
				kindergarden_switcher.setDisplayedChild(1);
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																.setApiName("profile")
																.addParameter("addalt")
																.putPostValues("code", code)
																.putPostValues("userid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID)))
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.setRequestMethod(WebServiceRequest.POST)
																.getRawResponse();
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							
							if(mParser.getSpecificValue("status") != null && mParser.getSpecificValue("status").toString().equals("success")) {
								Kindergarden profile = new Kindergarden();
								profile.setUid(Integer.parseInt(mParser.getSpecificValue("profile_uid").toString()));
								profile.setName(mParser.getSpecificValue("name").toString());
								profile.setDescription(mParser.getSpecificValue("description").toString());
								profile.setEmail(mParser.getSpecificValue("email").toString());
								profile.setPhoneNumber(mParser.getSpecificValue("phone_number").toString());
								profile.setAddress(mParser.getSpecificValue("address").toString());
								profile.setType(mParser.getSpecificValue("type").toString());
								profile.setTotalMessage(Integer.parseInt(mParser.getSpecificValue("total_msg").toString()));
								profile.setNewMessage(profile.getTotalMessage());
								profile.setWithPermission(0);
								mDataSource.insert(profile);
								
								Intent intentBroadcast = new Intent(MainActivity.KINDERGARDEN_RECEIVER);
								sendBroadcast(intentBroadcast);
								
								if(profile.getType() != null && profile.getType().equals("TEACHER") &&
										!mPrefsWrapper.getPreferenceBooleanValue(Constants.TEACHER_HAS_NAME)) {
									Intent intent = new Intent(AddKindergarden.this, TeacherName.class);
									startActivity(intent);
								}
								
								runOnUiThread(new Runnable() {
									public void run() {
										displayKindergarden();
									}
								});
							} else {
								runOnUiThread(new Runnable() {
									public void run() {
										if(mParser.getSpecificValue("log") != null) {
											Message.make(AddKindergarden.this, "", mParser.getSpecificValue("log").toString()).toast();
											kindergarden_switcher.setDisplayedChild(0);
										}
									}
								});
							}
						}
					}
				}).start();
			}
		}
	}
}
