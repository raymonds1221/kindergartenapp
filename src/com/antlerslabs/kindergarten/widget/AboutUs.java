package com.antlerslabs.kindergarten.widget;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.adapter.AboutDocumentAdapter;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.pojo.AboutDetails;
import com.antlerslabs.kindergarten.pojo.AboutDocument;
import com.antlerslabs.kindergarten.pojo.Kindergarden;

import java.util.List;

import android.os.Handler;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ListView;
import android.text.method.ScrollingMovementMethod;


public class AboutUs extends LinearLayout {
	private Context mContext;
	private TextView aboutus_title;
	private TextView aboutus_details;
	public ViewFlipper aboutus_switcher;
	private ListView list_aboutus_documents;
	private PreferenceWrapper mPrefsWrapper;
	private Network mNetwork;
	private WebServiceRequest mWebServiceRequest;
	private DataSource mDataSource;
	private final Handler mHandler = new Handler();

	public AboutUs(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.about_us, this);
		
		mContext = context;
		mPrefsWrapper = PreferenceWrapper.getInstance(context);
		mNetwork = Network.getInstance(context);
		mWebServiceRequest = WebServiceRequest.newInstance();
		mDataSource = DataSourceFactory.getDataSourceFactory(context);
	}
	
	@Override
	public void onFinishInflate() {
		aboutus_title = (TextView) findViewById(R.id.aboutus_title);
		aboutus_details = (TextView) findViewById(R.id.aboutus_details);
		aboutus_switcher = (ViewFlipper) findViewById(R.id.aboutus_switcher);
		list_aboutus_documents = (ListView) findViewById(R.id.list_aboutus_documents);
		
		aboutus_details.setMovementMethod(new ScrollingMovementMethod());
	}
	
	public void displayAboutUs() {
		
		if(mNetwork.isAvailable()) {
			long lastUpdated = mPrefsWrapper.getPreferenceLongValue(Constants.ABOUT_DETAILS_LAST_UPDATED);
			
			if(lastUpdated > 0) {
				long elapsedTime = (System.currentTimeMillis() - lastUpdated) / 1000 / 60 / 60 / 24;
				
				if(elapsedTime >= 2 && mPrefsWrapper.getPreferenceBooleanValue(Constants.NEED_REQUEST_DOCUMENTS)) {
					mPrefsWrapper.setPreferenceBooleanValue(Constants.NEED_REQUEST_DOCUMENTS, false);
					requestAboutDetails();
				} else {
					new Thread(new Runnable() {
						public void run() {
							synchronized(Constants.mLockObject) {
								final Kindergarden kindergarden = mDataSource.where("uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																				.getForObject(Kindergarden.class);
								final AboutDetails aboutDetails = mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																				.getForObject(AboutDetails.class);
								final List<AboutDocument> aboutDocuments = mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																				.getForObjects(AboutDocument.class);
								
								mHandler.post(new Runnable() {
									public void run() {
										aboutus_switcher.setDisplayedChild(1);
										
										if(aboutDetails != null) {
											aboutus_title.setText(kindergarden.getName().toUpperCase());
											aboutus_details.setText(aboutDetails.getDetails());
										}
										
										if(aboutDocuments != null) {
											AboutDocumentAdapter adapter = new AboutDocumentAdapter(mContext, R.layout.list_aboutus_document, aboutDocuments);
											list_aboutus_documents.setAdapter(adapter);
										}
									}
								});
							}
						}
					}).start();
				}
			} else {
				requestAboutDetails();
			}
		} else {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						final Kindergarden kindergarden = mDataSource.where("uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																		.getForObject(Kindergarden.class);
						final AboutDetails aboutDetails = mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																		.getForObject(AboutDetails.class);
						final List<AboutDocument> aboutDocuments = mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																		.getForObjects(AboutDocument.class);
						
						mHandler.post(new Runnable() {
							public void run() {
								aboutus_switcher.setDisplayedChild(1);
								
								if(aboutDetails != null) {
									aboutus_title.setText(kindergarden.getName().toUpperCase());
									aboutus_details.setText(aboutDetails.getDetails());
								}
								
								if(aboutDocuments != null) {
									AboutDocumentAdapter adapter = new AboutDocumentAdapter(mContext, R.layout.list_aboutus_document, aboutDocuments);
									list_aboutus_documents.setAdapter(adapter);
								}
							}
						});
					}
				}
			}).start();
		}
	}
	
	private void requestAboutDetails() {
		new Thread(new Runnable() {
			public void run() {
				synchronized(Constants.mLockObject) {
					final Kindergarden kindergarden = mDataSource.where("uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																	.getForObject(Kindergarden.class);
					final AboutDetails aboutDetails = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																	.setApiName("about")
																	.addParameter("aboutus")
																	.addParameter("kgid")
																	.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																	.setResultFormat(WebServiceRequest.DataFormat.XML)
																	.setRequestMethod(WebServiceRequest.GET)
																	.getForObject(AboutDetails.class);
					final List<AboutDocument> aboutDocuments = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																	.setApiName("about")
																	.addParameter("aboutus")
																	.addParameter("kgid")
																	.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																	.setResultFormat(WebServiceRequest.DataFormat.XML)
																	.setRequestMethod(WebServiceRequest.GET)
																	.getForObjects(AboutDocument.class);
					if(aboutDetails != null) {
						mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
									.getForObject(AboutDetails.class);
						if(mDataSource.getRecordCount() > 0) {
							mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
										.delete(AboutDetails.class);
						}
						aboutDetails.setProfileUid(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID));
						mDataSource.insert(aboutDetails);
					}
					if(aboutDocuments != null) {
						mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
									.getForObjects(AboutDocument.class);
						
						if(mDataSource.getRecordCount() > 0) {
							mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
										.delete(AboutDocument.class);
						}
						
						for(AboutDocument aboutDocument: aboutDocuments) {
							aboutDocument.setProfileUid(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID));
							mDataSource.insert(aboutDocument);
						}
					}
					
					mHandler.post(new Runnable() {
						public void run() {
							if(aboutDetails != null) {
								aboutus_switcher.setDisplayedChild(1);
								aboutus_title.setText(kindergarden.getName().toUpperCase());
								aboutus_details.setText(aboutDetails.getDetails());
							} else {
								aboutus_switcher.setDisplayedChild(1);
							}
							
							if(aboutDocuments != null) {
								AboutDocumentAdapter adapter = new AboutDocumentAdapter(mContext, R.layout.list_aboutus_document, aboutDocuments);
								list_aboutus_documents.setAdapter(adapter);
							}
							
							mPrefsWrapper.setPreferenceLongValue(Constants.ABOUT_DETAILS_LAST_UPDATED, System.currentTimeMillis());
						}
					});
				}
			}
		}).start();
	}
}
