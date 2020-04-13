package com.antlerslabs.kindergarten.widget;

import com.antlerslabs.kindergarten.ContactForm;
import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.adapter.ContactAdapter;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.pojo.Admin;
import com.antlerslabs.kindergarten.pojo.Kindergarden;
import com.antlerslabs.kindergarten.pojo.Teacher;

import java.util.List;

import android.app.AlertDialog;
import android.os.Handler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ListView;
import android.net.Uri;


public class ContactUs extends LinearLayout {
	private Context mContext;
	private PreferenceWrapper mPrefsWrapper;
	private Network mNetwork;
	private WebServiceRequest mWebServiceRequest;
	private DataSource mDataSource;
	private ViewFlipper contactus_switcher;
	private TextView contactus_title;
	private TextView contactus_address;
	private TextView contactus_email;
	private TextView admin_name;
	private ListView list_contacts;
	private Kindergarden mKindergarden;
	private Admin mAdmin;
	private final Handler mHandler = new Handler();

	public ContactUs(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.contact_us, this);
		
		mContext = context;
		mPrefsWrapper = PreferenceWrapper.getInstance(context);
		mNetwork = Network.getInstance(context);
		mWebServiceRequest = WebServiceRequest.newInstance();
		mDataSource = DataSourceFactory.getDataSourceFactory(context);
	}
	
	public void onFinishInflate() {
		contactus_switcher = (ViewFlipper) findViewById(R.id.contactus_switcher);
		contactus_title = (TextView) findViewById(R.id.contactus_title);
		contactus_address = (TextView) findViewById(R.id.contactus_address);
		contactus_email = (TextView) findViewById(R.id.contactus_email);
		admin_name = (TextView) findViewById(R.id.admin_name);
		list_contacts = (ListView) findViewById(R.id.list_contacts);
		
		contactus_email.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, ContactForm.class);
				intent.putExtra(Constants.DEFAULT_PROFILE, mKindergarden);
				mContext.startActivity(intent);
			}
		});
		
		admin_name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mAdmin != null && mAdmin.getPhoneNumber() != null && !mAdmin.getPhoneNumber().equals("")) {
					AlertDialog alert = new AlertDialog.Builder(mContext)
														.setMessage(R.string.call_prompt)
														.setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																Intent intent = new Intent(Intent.ACTION_CALL);
																intent.setData(Uri.parse("tel:" + mAdmin.getPhoneNumber()));
																mContext.startActivity(intent);
																dialog.dismiss();
															}
														})
														.setNegativeButton(R.string.no_btn, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																dialog.dismiss();
															}
														})
														.create();
					alert.show();
				} else {
					Message.make(mContext, R.string.empty_string, R.string.unable_to_call).toast();
				}
			}
		});
		
		findViewById(R.id.call_btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mAdmin != null && mAdmin.getPhoneNumber() != null && !mAdmin.getPhoneNumber().equals("")) {
					AlertDialog alert = new AlertDialog.Builder(mContext)
														.setMessage(R.string.call_prompt)
														.setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																Intent intent = new Intent(Intent.ACTION_CALL);
																intent.setData(Uri.parse("tel:" + mAdmin.getPhoneNumber()));
																mContext.startActivity(intent);
																dialog.dismiss();
															}
														})
														.setNegativeButton(R.string.no_btn, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																dialog.dismiss();
															}
														})
														.create();
					alert.show();
				} else {
					Message.make(mContext, R.string.empty_string, R.string.unable_to_call).toast();
				}
			}
		});
	}
	
	public void displayContactUs() {
		contactus_switcher.setDisplayedChild(0);
		if(mNetwork.isAvailable()) {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						mKindergarden = mDataSource.where("uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																.getForObject(Kindergarden.class);
						mAdmin = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																.setApiName("profile")
																.addParameter("admininfo")
																.addParameter("kgid")
																.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.addParameter(String.valueOf(System.currentTimeMillis()))
																.setRequestMethod(WebServiceRequest.GET)
																.getForObject(Admin.class);
						final List<Teacher> teachers = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																.setApiName("user")
																.addParameter("teacherslist")
																.addParameter("kgid")
																.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.setRequestMethod(WebServiceRequest.GET)
																.getForObjects(Teacher.class);
						mDataSource.where("uid", mAdmin.getUid())
									.getForObject(Admin.class);
						if(mDataSource.getRecordCount() > 0)
							mDataSource.where("uid", mAdmin.getUid())
										.delete(Admin.class);
						mAdmin.setProfileUid(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID));
						mDataSource.insert(mAdmin);
						
						if(teachers != null) {
							for(Teacher teacher: teachers) {
								mDataSource.where("uid", teacher.getUid())
											.getForObject(Teacher.class);
								if(mDataSource.getRecordCount() > 0)
									mDataSource.where("uid", teacher.getUid())
												.delete(Teacher.class);
								teacher.setProfileUid(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID));
								mDataSource.insert(teacher);
							}
						}
						
						mHandler.post(new Runnable() {
							public void run() {
								contactus_title.setText(mKindergarden.getName().toUpperCase());
								contactus_address.setText(mKindergarden.getAddress());
								contactus_email.setText(mKindergarden.getEmail());
								
								if(mAdmin != null && mAdmin.getName() != null) {
									admin_name.setText(mAdmin.getName().toUpperCase());
								}
								
								contactus_switcher.setDisplayedChild(1);
								
								if(teachers != null) {
									ContactAdapter adapter = new ContactAdapter(mContext, R.layout.list_contact, teachers);
									list_contacts.setAdapter(adapter);
								}
								
							}
						});
					}
				}
			}).start();
		} else {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						mKindergarden = mDataSource.where("uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
													.getForObject(Kindergarden.class);
						mAdmin = mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
											.getForObject(Admin.class);
						final List<Teacher> teachers = mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																	.getForObjects(Teacher.class);
						mHandler.post(new Runnable() {
							public void run() {
								contactus_title.setText(mKindergarden.getName().toUpperCase());
								contactus_address.setText(mKindergarden.getAddress());
								contactus_email.setText(mKindergarden.getEmail());
								
								if(mAdmin != null && mAdmin.getName() != null) {
									admin_name.setText(mAdmin.getName().toUpperCase());
								}
								
								contactus_switcher.setDisplayedChild(1);
								
								if(teachers != null) {
									ContactAdapter adapter = new ContactAdapter(mContext, R.layout.list_contact, teachers);
									list_contacts.setAdapter(adapter);
								}
								
							}
						});
					}
				}
			}).start();
		}
	}
}
