package com.antlerslabs.kindergarten.service;

import java.util.List;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Service;
import android.content.Intent;
import android.content.ContentValues;
import android.content.ContentUris;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.RemoteCallbackList;
import android.net.Uri;
import android.text.format.DateUtils;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.WebServiceRequest;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.pojo.LatestNews;
import com.antlerslabs.kindergarten.pojo.Message;
import com.antlerslabs.kindergarten.pojo.CalendarDetail;
import com.antlerslabs.kindergarten.pojo.CalendarSync;
import com.antlerslabs.kindergarten.pojo.AboutDetails;
import com.antlerslabs.kindergarten.pojo.AboutDocument;
import com.antlerslabs.kindergarten.pojo.Kindergarden;
import com.antlerslabs.kindergarten.service.IKGService;
import com.antlerslabs.kindergarten.service.IKGServiceCallback;

public class KGService extends Service {
	private final RemoteCallbackList<IKGServiceCallback> callbacks = new RemoteCallbackList<IKGServiceCallback>();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private IKGService.Stub mBinder = new IKGService.Stub() {
		@Override
		public void requestProfiles(final int userId) throws RemoteException {
			final WebServiceRequest mWebServiceRequest = WebServiceRequest.newInstance();
			final DataSource mDataSource = DataSourceFactory.getDataSourceFactory(KGService.this);
			final Network mNetwork = Network.getInstance(KGService.this);
			
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						if(mNetwork.isAvailable()) {
							List<Kindergarden> profiles = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																			.setApiName("profile")
																			.addParameter("profilesalt")
																			.addParameter("userid")
																			.addParameter(String.valueOf(userId))
																			.addParameter("format")
																			.setResultFormat(WebServiceRequest.DataFormat.XML)
																			.addParameter(String.valueOf(System.currentTimeMillis()))
																			.setRequestMethod(WebServiceRequest.GET)
																			.getForObjects(Kindergarden.class);
							//mDataSource.delete(Kindergarden.class);
							//mDataSource.insertAll(profiles);
							mDataSource.delete(LatestNews.class);
							mDataSource.delete(Message.class);
							mDataSource.delete(CalendarDetail.class);
							mDataSource.delete(AboutDetails.class);
							mDataSource.delete(AboutDocument.class);
							
							if(profiles != null) {
								for(Kindergarden profile: profiles) {
									Kindergarden prof = mDataSource.where("uid", profile.getUid())
																	.getForObject(Kindergarden.class);
									
									if(mDataSource.getRecordCount() > 0) {
										profile.setProfileViewed(prof.isProfileViewed());
										int newMessage = 0;
										if(profile.getTotalMessage() > prof.getTotalMessage()) {
											if(!profile.isProfileViewed()) {
												newMessage = profile.getTotalMessage();
												profile.setProfileViewed(false);
											} else {
												newMessage = profile.getTotalMessage() - prof.getTotalMessage() + prof.getNewMessage();
											}
										} else if(profile.getTotalMessage() < prof.getTotalMessage()) {
											newMessage = profile.getTotalMessage();
											profile.setProfileViewed(false);
										} else {
											if(!profile.isProfileViewed()) {
												newMessage = profile.getTotalMessage();
												profile.setProfileViewed(false);
											}
										}
										profile.setNewMessage(newMessage);
										mDataSource.where("uid", prof.getUid())
													.update(profile);
									} else {
										profile.setNewMessage(profile.getTotalMessage());
										mDataSource.insert(profile);
									}
								}
							}
							
							int n = callbacks.beginBroadcast();
							
							for(int i=0;i<n;i++) {
								try {
									callbacks.getBroadcastItem(i).onFinishRequestingProfiles();
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
							
							callbacks.finishBroadcast();
						} else {
							int n = callbacks.beginBroadcast();
							
							for(int i=0;i<n;i++) {
								try {
									callbacks.getBroadcastItem(i).onFinishRequestingProfiles();
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
							
							callbacks.finishBroadcast();
						}
					}
				}
			}).start();
		}
		
		@Override
		public void requestSyncCalendar() throws RemoteException {
			final WebServiceRequest mWebServiceRequest = WebServiceRequest.newInstance();
			final DataSource mDataSource = DataSourceFactory.getDataSourceFactory(KGService.this);
			final Network mNetwork = Network.getInstance(KGService.this);
			final PreferenceWrapper mPrefsWrapper = PreferenceWrapper.getInstance(KGService.this);
			
			if(mNetwork.isAvailable()) {
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																.setApiName("calendar")
																.addParameter("synevents")
																.addParameter("kgid")
																.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.setRequestMethod(WebServiceRequest.GET)
																.getRawResponse();
							if(result != null) {
								final Parser mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
								final List<CalendarDetail> calendarDetails = mParser.getSpecificXPathList("/xml/item/item", CalendarDetail.class);
								
								for(CalendarDetail calendarDetail: calendarDetails) {
									Calendar dateStart = Calendar.getInstance();
									Calendar dateEnd = Calendar.getInstance();
									
									dateStart.setTime(calendarDetail.getDateCreated());
									dateEnd.setTime(calendarDetail.getDateCreated());
									dateEnd.add(Calendar.DAY_OF_MONTH, 1);
									
									ContentValues contentValues = new ContentValues();
									contentValues.put("calendar_id", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_CALENDAR_ID));
									contentValues.put("title", calendarDetail.getTitle());
									contentValues.put("description", calendarDetail.getDescription());
									contentValues.put("dtstart", dateStart.getTimeInMillis() + DateUtils.DAY_IN_MILLIS);
									contentValues.put("dtend", dateEnd.getTimeInMillis() + DateUtils.DAY_IN_MILLIS);
									contentValues.put("allDay", 1);
									contentValues.put("hasAlarm", 1);
									contentValues.put("eventTimezone", TimeZone.getDefault().getID());
									
									CalendarSync calSync = mDataSource.where("calendar_detail_id", calendarDetail.getUid())
												.getForObject(CalendarSync.class);
									
									if(mDataSource.getRecordCount() > 0) {
										getContentResolver().update(Uri.parse("content://com.android.calendar/events"), contentValues, "_id=" + calSync.getEventId(), null);
									} else {
										Uri calendar_event = getContentResolver().insert(Uri.parse("content://com.android.calendar/events"), contentValues);
										CalendarSync calendarSync = new CalendarSync();
										int eventId = (int) ContentUris.parseId(calendar_event);
										calendarSync.setEventId(eventId);
										calendarSync.setCalendarDetailId(calendarDetail.getUid());
										mDataSource.insert(calendarSync);
									}
								}
							}
						}
					}
				}).start();
			}
		}

		@Override
		public void registerCallback(IKGServiceCallback callback)
				throws RemoteException {
			callbacks.register(callback);
		}

		@Override
		public void unregisterCallback(IKGServiceCallback callback)
				throws RemoteException {
			callbacks.unregister(callback);
		}
	};
}
