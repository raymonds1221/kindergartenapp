package com.antlerslabs.kindergarten.widget;

import com.antlerslabs.kindergarten.AboutApp;
import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;

import android.app.AlertDialog;
import android.os.Handler;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.database.Cursor;
import android.net.Uri;


public class Settings extends LinearLayout {
	private Context mContext;
	private ImageButton push_notification_enabled;
	private ImageButton sync_calendar_enabled;
	private PreferenceWrapper mPrefsWrapper;
	private Network mNetwork;
	private WebServiceRequest mWebServiceRequest;
	private Parser mParser;
	private final Handler mHandler = new Handler();

	public Settings(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.settings, this);
		
		mContext = context;
		mPrefsWrapper = PreferenceWrapper.getInstance(context);
		mNetwork = Network.getInstance(context);
		mWebServiceRequest = WebServiceRequest.newInstance();
	}
	
	@Override
	public void onFinishInflate() {
		push_notification_enabled = (ImageButton) findViewById(R.id.push_notification_enabled);
		sync_calendar_enabled = (ImageButton) findViewById(R.id.sync_calendar_enabled);
		
		if(mPrefsWrapper.getPreferenceBooleanValue(Constants.PUSH_NOTIFICATION_ENABLED)) {
			push_notification_enabled.setImageResource(R.drawable.switch_on);
		} else {
			push_notification_enabled.setImageResource(R.drawable.switch_off);
		}
		
		if(mPrefsWrapper.getPreferenceBooleanValue(Constants.SYNC_CALENDAR_ENABLED)) {
			sync_calendar_enabled.setImageResource(R.drawable.switch_on);
		} else {
			sync_calendar_enabled.setImageResource(R.drawable.switch_off);
		}
		
		push_notification_enabled.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mNetwork.isAvailable())
					return;
				
				boolean enabled = mPrefsWrapper.getPreferenceBooleanValue(Constants.PUSH_NOTIFICATION_ENABLED);
				mPrefsWrapper.setPreferenceBooleanValue(Constants.PUSH_NOTIFICATION_ENABLED, !enabled);
				enabled = !enabled;
				push_notification_enabled.setImageResource((enabled) ? R.drawable.switch_on: R.drawable.switch_off);
				
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							String result = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																.setApiName("settings")
																.addParameter("pushset")
																.putPostValues("userid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID)))
																.putPostValues("kgid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																.putPostValues("switch", (mPrefsWrapper.getPreferenceBooleanValue(Constants.PUSH_NOTIFICATION_ENABLED)) ? "1" : "0")
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.setRequestMethod(WebServiceRequest.POST)
																.getRawResponse();
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							
							if(mParser.getSpecificValue("status") != null && !mParser.getSpecificValue("status").toString().equals("success")) {
								mHandler.post(new Runnable() {
									public void run() {
										Message.make(mContext, R.string.empty_string, R.string.cannot_change_settings).toast();
									}
								});
							}
						}
					}
				}).start();
			}
		});
		
		sync_calendar_enabled.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mNetwork.isAvailable())
					return;
				
				boolean enabled = mPrefsWrapper.getPreferenceBooleanValue(Constants.SYNC_CALENDAR_ENABLED);
				mPrefsWrapper.setPreferenceBooleanValue(Constants.SYNC_CALENDAR_ENABLED, !enabled);
				enabled = !enabled;
				sync_calendar_enabled.setImageResource((enabled) ? R.drawable.switch_on: R.drawable.switch_off);
				
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							String result = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																.setApiName("settings")
																.addParameter("syncalset")
																.putPostValues("userid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID)))
																.putPostValues("kgid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																.putPostValues("switch", (mPrefsWrapper.getPreferenceBooleanValue(Constants.SYNC_CALENDAR_ENABLED)) ? "1" : "0")
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.setRequestMethod(WebServiceRequest.POST)
																.getRawResponse();
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							
							if(mParser.getSpecificValue("status") != null && !mParser.getSpecificValue("status").toString().equals("success")) {
								mHandler.post(new Runnable() {
									public void run() {
										Message.make(mContext, R.string.empty_string, R.string.cannot_change_settings).toast();
									}
								});
							}
						}
					}
				}).start();
				
				if(enabled) {
					Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"), new String[] {"_id", "calendar_displayName"}, "visible=1", null, null);
					
					if(cursor != null && cursor.moveToNext()) {
						final int[] calIds = new int[cursor.getCount()];
						final String[] calNames = new String[cursor.getCount()];
						
						if(calIds.length > 0) {
							for(int i=0;i<calIds.length;i++) {
								calIds[i] = cursor.getInt(0);
								calNames[i] = cursor.getString(1);
								cursor.moveToNext();
							}
							if(calIds.length > 1) {
								AlertDialog alert = new AlertDialog.Builder(mContext)
																	.setTitle(R.string.calendar_to_sync)
																	.setItems(calNames, new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(DialogInterface dialog, int which) {
																			mPrefsWrapper.setPreferenceIntValue(Constants.DEFAULT_CALENDAR_ID, calIds[which]);
																			((OnSettingsListener) mContext).onSyncCalendar();
																			dialog.dismiss();
																		}
																	})
																	.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(DialogInterface dialog, int which) {
																			dialog.dismiss();
																		}
																	})
																	.create();
								alert.show();
							} else {
								mPrefsWrapper.setPreferenceIntValue(Constants.DEFAULT_CALENDAR_ID, calIds[0]);
							}
						} else {
							Message.make(mContext, R.string.empty_string, R.string.cannot_sync_calendar).toast();
							sync_calendar_enabled.setImageResource(R.drawable.switch_off);
							mPrefsWrapper.setPreferenceBooleanValue(Constants.SYNC_CALENDAR_ENABLED, false);
						}
					} else {
						Message.make(mContext, R.string.empty_string, R.string.cannot_sync_calendar).toast();
						sync_calendar_enabled.setImageResource(R.drawable.switch_off);
						mPrefsWrapper.setPreferenceBooleanValue(Constants.SYNC_CALENDAR_ENABLED, false);
					}
				}
			}
		});
		
		findViewById(R.id.about_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, AboutApp.class);
				mContext.startActivity(intent);
			}
		});
	}
	
	public void displayPushNotification() {
		if(mNetwork.isAvailable()) {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						String result = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
															.setApiName("settings")
															.addParameter("pushget")
															.addParameter("userid")
															.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID)))
															.addParameter("kgid")
															.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
															.setResultFormat(WebServiceRequest.DataFormat.XML)
															.setRequestMethod(WebServiceRequest.GET)
															.getRawResponse();
						mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
						if(mParser.getSpecificValue("push_notification") != null && mParser.getSpecificValue("push_notification").toString().equals("1")) {
							mPrefsWrapper.setPreferenceBooleanValue(Constants.PUSH_NOTIFICATION_ENABLED, true);
						} else {
							mPrefsWrapper.setPreferenceBooleanValue(Constants.PUSH_NOTIFICATION_ENABLED, false);
						}
						
						mHandler.post(new Runnable() {
							public void run() {
								boolean enabled = mPrefsWrapper.getPreferenceBooleanValue(Constants.PUSH_NOTIFICATION_ENABLED);
								push_notification_enabled.setImageResource((enabled) ? R.drawable.switch_on: R.drawable.switch_off);
							}
						});
					}
				}
			}).start();
		}
	}
	
	public static interface OnSettingsListener {
		void onSyncCalendar();
	}
}
