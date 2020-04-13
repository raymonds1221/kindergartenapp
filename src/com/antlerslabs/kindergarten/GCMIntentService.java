package com.antlerslabs.kindergarten;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.widget.Toast;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.pojo.CalendarSync;
import com.antlerslabs.kindergarten.pojo.Kindergarden;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onError(Context context, String errorId) {
		Toast.makeText(this, "Unable to register from google cloud: " + errorId, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onMessage(final Context context, Intent intent) {
		final DataSource dataSource = DataSourceFactory.getDataSourceFactory(context);
		final PreferenceWrapper prefsWrapper = PreferenceWrapper.getInstance(context);
		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int push_by = 0;
		
		final int profileUid = Integer.parseInt(intent.getStringExtra("profile_uid"));
		String type = intent.getStringExtra("type");
		String title = "Kindergarden";
		String message = "";
		
		prefsWrapper.setPreferenceIntValue(Constants.DEFAULT_KG_ID, profileUid);
		
		if(type.equals("messagewall")) {
			push_by = Constants.PUSH_BY_MESSAGEBOARD;
			title = "Kindergarden - Messageboard";
			message = intent.getStringExtra("message");
		} else if(type.equals("calendar")) {
			push_by = Constants.PUSH_BY_CALENDAR;
			title = "Kindergarden - Calendar Event";
			final int uid = Integer.parseInt(intent.getStringExtra("uid"));
			final String tit = intent.getStringExtra("title");
			final String description = intent.getStringExtra("description");
			final String event_date = intent.getStringExtra("event_date");
			message = event_date + " (" + tit + ") " + description;
			
			if(prefsWrapper.getPreferenceBooleanValue(Constants.SYNC_CALENDAR_ENABLED)) {
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							try {
								Date date = dateFormat.parse(event_date);
								
								Calendar dateStart = Calendar.getInstance();
								Calendar dateEnd = Calendar.getInstance();
								
								dateStart.setTime(date);
								dateEnd.setTime(date);
								dateEnd.add(Calendar.DAY_OF_MONTH, 1);
								
								ContentValues contentValues = new ContentValues();
								contentValues.put("calendar_id", prefsWrapper.getPreferenceIntValue(Constants.DEFAULT_CALENDAR_ID));
								contentValues.put("title", tit);
								contentValues.put("description", description);
								contentValues.put("dtstart", dateStart.getTimeInMillis() + DateUtils.DAY_IN_MILLIS);
								contentValues.put("dtend", dateEnd.getTimeInMillis() + DateUtils.DAY_IN_MILLIS);
								contentValues.put("allDay", 1);
								contentValues.put("hasAlarm", 1);
								contentValues.put("eventTimezone", TimeZone.getDefault().getID());
								
								CalendarSync calSync = dataSource.where("calendar_detail_id", uid)
											.getForObject(CalendarSync.class);
								
								if(dataSource.getRecordCount() > 0) {
									getContentResolver().update(Uri.parse("content://com.android.calendar/events"), contentValues, "_id=" + calSync.getEventId(), null);
								} else {
									Uri calendar_event = getContentResolver().insert(Uri.parse("content://com.android.calendar/events"), contentValues);
									CalendarSync calendarSync = new CalendarSync();
									int eventId = (int) ContentUris.parseId(calendar_event);
									calendarSync.setEventId(eventId);
									calendarSync.setCalendarDetailId(uid);
									dataSource.insert(calendarSync);
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
			}
		}
		
		Kindergarden profile = dataSource.where("uid", profileUid)
											.getForObject(Kindergarden.class);
		prefsWrapper.setPreferenceIntValue(Constants.DEFAULT_KG_ID, profileUid);
		prefsWrapper.setPreferenceBooleanValue(Constants.USER_IS_TEACHER, profile.getType().equals("TEACHER"));
		
		if(profile != null) {
			profile.setProfileViewed(false);
			profile.setNewMessage(profile.getNewMessage() + 1);
			dataSource.where("uid", profileUid)
						.update(profile);
			Intent i = new Intent(MainActivity.KINDERGARDEN_RECEIVER);
			context.sendBroadcast(i);
		}
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_statusbar, title, System.currentTimeMillis());
		Intent notificationIntent = new Intent(context, LauncherActivity.class);
		notificationIntent.removeExtra(Constants.PUSH_BY);
		notificationIntent.putExtra(Constants.PUSH_BY, push_by);
		notificationIntent.setData(Uri.parse("kg://" + SystemClock.elapsedRealtime()));
		Log.i("KG", "push by gcm service: " + push_by);
		
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, title, message, pendingIntent);
		notification.defaults |= Notification.DEFAULT_ALL;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		PreferenceWrapper prefsWrapper = PreferenceWrapper.getInstance(context);
		prefsWrapper.setPreferenceBooleanValue(Constants.REGISTERED_TO_CLOUD, true);
		prefsWrapper.setPreferenceStringValue(Constants.CLOUD_REGISTRATION_ID, regId);
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		PreferenceWrapper prefsWrapper = PreferenceWrapper.getInstance(context);
		prefsWrapper.setPreferenceBooleanValue(Constants.REGISTERED_TO_CLOUD, false);
		prefsWrapper.setPreferenceStringValue(Constants.CLOUD_REGISTRATION_ID, null);
	}

}
