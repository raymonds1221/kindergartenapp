package com.antlerslabs.kindergarten.widget;

import com.antlerslabs.kindergarten.CalendarDetails;
import com.antlerslabs.kindergarten.DocumentViewer;
import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.adapter.CalendarAdapter;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.pojo.CalendarDetail;
import com.antlerslabs.kindergarten.pojo.CalendarDocument;

import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import android.app.AlertDialog;
import android.os.Environment;
import android.os.Handler;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.net.Uri;


public class Calendar extends LinearLayout {
	private Context mContext;
	private final java.util.Calendar mCalendar = java.util.Calendar.getInstance();
	private int mMonth = mCalendar.get(java.util.Calendar.MONTH);
	private int mYear = mCalendar.get(java.util.Calendar.YEAR);
	private DateFormat mDateFormat = new SimpleDateFormat("MMMM yyyy");
	private DateFormat mDateFormat02 = new SimpleDateFormat("dd MMMM");
	private ViewFlipper calendar_switcher;
	private TextView calendar_date;
	private ListView list_calendar;
	private RelativeLayout calendar_month_container;
	private WebServiceRequest mWebServiceRequest;
	private PreferenceWrapper mPrefsWrapper;
	private Network mNetwork;
	private Parser mParser;
	private DataSource mDataSource;
	private final Handler mHandler = new Handler();
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 200;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	public Calendar(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.calendar, this);
		
		mContext = context;
		mWebServiceRequest = WebServiceRequest.newInstance();
		mPrefsWrapper = PreferenceWrapper.getInstance(context);
		mNetwork = Network.getInstance(context);
		mDataSource = DataSourceFactory.getDataSourceFactory(context);
	}
	
	@Override
	public void onFinishInflate() {
		calendar_switcher = (ViewFlipper) findViewById(R.id.calendar_switcher);
		list_calendar = (ListView) findViewById(R.id.list_calendar);
		calendar_month_container = (RelativeLayout) findViewById(R.id.calendar_month_container);
		calendar_date = (TextView) findViewById(R.id.calendar_date); 
		
		calendar_date.setText(mDateFormat.format(mCalendar.getTime()).toUpperCase());
		
		final GestureDetector gestureDetector = new GestureDetector(new SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
				float diffAbs = Math.abs(event1.getY() - event2.getY());
				float diff = event1.getX() - event2.getX();
				
				if(diffAbs > SWIPE_MAX_OFF_PATH)
					return false;
				
				if(diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if(mMonth == 12) {
						mMonth = 1;
						mYear++;
					} else {
						mMonth++;
					}
					
					mCalendar.set(java.util.Calendar.MONTH, mMonth);
					mCalendar.set(java.util.Calendar.YEAR, mYear);
					
					calendar_date.setText(mDateFormat.format(mCalendar.getTime()).toUpperCase());
					displayCalendar();
				} else if(-diff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					if(mMonth == 1) {
						mMonth = 12;
						mYear--;
					} else {
						mMonth--;
					}
					
					mCalendar.set(java.util.Calendar.MONTH, mMonth);
					mCalendar.set(java.util.Calendar.YEAR, mYear);
					
					calendar_date.setText(mDateFormat.format(mCalendar.getTime()).toUpperCase());
					displayCalendar();
				}
				
				return true;
			}
		});
		
		calendar_month_container.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		
		calendar_month_container.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
		
		findViewById(R.id.calendar_prev).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mMonth == 1) {
					mMonth = 12;
					mYear--;
				} else {
					mMonth--;
				}
				
				mCalendar.set(java.util.Calendar.MONTH, mMonth);
				mCalendar.set(java.util.Calendar.YEAR, mYear);
				
				calendar_date.setText(mDateFormat.format(mCalendar.getTime()).toUpperCase());
				displayCalendar();
			}
		});
		
		findViewById(R.id.calendar_next).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mMonth == 12) {
					mMonth = 1;
					mYear++;
				} else {
					mMonth++;
				}
				
				mCalendar.set(java.util.Calendar.MONTH, mMonth);
				mCalendar.set(java.util.Calendar.YEAR, mYear);
				
				calendar_date.setText(mDateFormat.format(mCalendar.getTime()).toUpperCase());
				displayCalendar();
			}
		});
		
		findViewById(R.id.refresh_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				java.util.Calendar calendar = java.util.Calendar.getInstance();
				mMonth = calendar.get(java.util.Calendar.MONTH);
				mYear = calendar.get(java.util.Calendar.YEAR);
				
				mCalendar.set(java.util.Calendar.MONTH, mMonth);
				mCalendar.set(java.util.Calendar.YEAR, mYear);
				
				calendar_date.setText(mDateFormat.format(mCalendar.getTime()).toUpperCase());
				displayCalendar();
			}
		});
	}
	
	public void displayCalendar() {
		calendar_switcher.setDisplayedChild(0);
		
		if(mNetwork.isAvailable()) {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						String result = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																				.setApiName("calendar")
																				.addParameter("events")
																				.addParameter("kgid")
																				.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																				.addParameter("month")
																				.addParameter(String.valueOf(mCalendar.get(java.util.Calendar.MONTH) + 1))
																				.addParameter("year")
																				.addParameter(String.valueOf(mCalendar.get(java.util.Calendar.YEAR)))
																				.setResultFormat(WebServiceRequest.DataFormat.XML)
																				.setRequestMethod(WebServiceRequest.GET)
																				.getRawResponse();
						if(result != null) {
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							final List<CalendarDetail> calendarDetails = mParser.getSpecificXPathList("/xml/item/events", CalendarDetail.class);
							final List<CalendarDocument> calendarDocuments = mParser.getSpecificXPathList("/xml/item/doc/item", CalendarDocument.class);
							
							for(CalendarDetail calendarDetail: calendarDetails) {
								calendarDetail.setMonth(mCalendar.get(java.util.Calendar.MONTH));
								calendarDetail.setYear(mCalendar.get(java.util.Calendar.YEAR));
								mDataSource.where("uid", calendarDetail.getUid())
											.getForObject(CalendarDetail.class);
								calendarDetail.setProfileUid(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID));
								if(mDataSource.getRecordCount() > 0)
									mDataSource.where("uid", calendarDetail.getUid())
												.delete(CalendarDetail.class);
								mDataSource.insert(calendarDetail);
							}
							
							for(CalendarDocument calendarDocument: calendarDocuments) {
								mDataSource.where("uid", calendarDocument.getUid())
											.getForObject(CalendarDocument.class);
								
								if(mDataSource.getRecordCount() > 0)
									mDataSource.where("uid", calendarDocument.getUid())
												.delete(CalendarDocument.class);
								mDataSource.insert(calendarDocument);
							}
							
							mHandler.post(new Runnable() {
								public void run() {
									calendar_switcher.setDisplayedChild(1);
									
									if(calendarDetails != null) {
										CalendarAdapter adapter = new CalendarAdapter(mContext, R.layout.list_calendar, calendarDetails);
										list_calendar.setAdapter(adapter);
										
										adapter.setOnItemClickListener(new CalendarAdapter.OnItemClickListener() {
											@Override
											public void onItemClick(CalendarDetail calendarDetail) {
												if(calendarDetail.getDescription() != null && !calendarDetail.getDescription().equals("")) {
													Intent intent = new Intent(mContext, CalendarDetails.class);
													intent.putExtra(Constants.CALENDAR_DETAIL, calendarDetail);
													mContext.startActivity(intent);
												}
											}

											@Override
											public void onAttachmentClick(CalendarDetail calendarDetail) {
												Intent intent = new Intent(mContext, DocumentViewer.class);
												CalendarDocument calendarDocument = mDataSource.where("calendar_uid", calendarDetail.getUid())
																								.getForObject(CalendarDocument.class);
												intent.putExtra(Constants.DOCUMENT_NAME, calendarDocument.getFilename());
												mContext.startActivity(intent);
											}

											@Override
											public void onShareClick(final CalendarDetail calendarDetail) {
												AlertDialog alert = new AlertDialog.Builder(mContext)
																					.setItems(R.array.share_menu, new DialogInterface.OnClickListener() {
																						@Override
																						public void onClick(DialogInterface dialog, final int which) {
																							new Thread(new Runnable() {
																								public void run() {
																									synchronized(Constants.mLockObject) {
																										final List<CalendarDocument> calendarDocuments = mDataSource.where("calendar_uid", calendarDetail.getUid())
																																									.getForObjects(CalendarDocument.class);
																										final StringBuffer sb = new StringBuffer();
																										
																										sb.append("Kalender hendelse fra barnehagen:\n\n");
																										sb.append(calendarDetail.getTitle().toUpperCase() + "\n");
																										sb.append(mDateFormat02.format(calendarDetail.getDateCreated()) + "\n");
																										sb.append(calendarDetail.getDescription() + "\n\n");
																										
																										if(calendarDocuments != null && calendarDocuments.size() > 0) {
																											for(CalendarDocument calendarDocument: calendarDocuments) {
																												sb.append(calendarDocument.getFilename() + "\n");
																											}
																										}
																										
																										mHandler.post(new Runnable() {
																											public void run() {
																												Intent intent = new Intent();
																												switch(which) {
																													case 0:
																														intent.setAction(Intent.ACTION_VIEW);
																														intent.putExtra("sms_body", sb.toString());
																														intent.setType("vnd.android-dir/mms-sms");
																														mContext.startActivity(intent);
																														break;
																													case 1:
																														intent = new Intent(Intent.ACTION_SEND);
																														intent.setType("text/plain");
																														intent.putExtra(Intent.EXTRA_SUBJECT, "Kalender - Barnehage");
																														intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
																														mContext.startActivity(Intent.createChooser(intent, "Send Email"));
																														break;
																												}
																											}
																										});
																									}
																								}
																							}).start();
																							
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
											}
										});
									}
								}
							});
						}
					}
				}
			}).start();
		} else {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						final List<CalendarDetail> calendarDetails = mDataSource.where("month", mCalendar.get(java.util.Calendar.MONTH))
																				.andWhere("year", String.valueOf(mCalendar.get(java.util.Calendar.YEAR)))
																				.andWhere("profile_uid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																				.getForObjects(CalendarDetail.class);
						
						mHandler.post(new Runnable() {
							public void run() {
								CalendarAdapter adapter = new CalendarAdapter(mContext, R.layout.list_calendar, calendarDetails);
								list_calendar.setAdapter(adapter);
								calendar_switcher.setDisplayedChild(1);
								
								adapter.setOnItemClickListener(new CalendarAdapter.OnItemClickListener() {
									@Override
									public void onItemClick(CalendarDetail calendarDetail) {
										if(calendarDetail.getDescription() != null && !calendarDetail.getDescription().equals("")) {
											Intent intent = new Intent(mContext, CalendarDetails.class);
											intent.putExtra(Constants.CALENDAR_DETAIL, calendarDetail);
											mContext.startActivity(intent);
										}
									}

									@Override
									public void onAttachmentClick(CalendarDetail calendarDetail) {
										Intent intent = new Intent(mContext, DocumentViewer.class);
										CalendarDocument calendarDocument = mDataSource.where("calendar_uid", calendarDetail.getUid())
																						.getForObject(CalendarDocument.class);
										intent.putExtra(Constants.DOCUMENT_NAME, calendarDocument.getFilename());
										mContext.startActivity(intent);
									}

									@Override
									public void onShareClick(final CalendarDetail calendarDetail) {
										AlertDialog alert = new AlertDialog.Builder(mContext)
																			.setItems(R.array.share_menu, new DialogInterface.OnClickListener() {
																				@Override
																				public void onClick(DialogInterface dialog, final int which) {
																					new Thread(new Runnable() {
																						public void run() {
																							synchronized(Constants.mLockObject) {
																								final List<CalendarDocument> calendarDocuments = mDataSource.where("calendar_uid", calendarDetail.getUid())
																										.getForObjects(CalendarDocument.class);
																								final StringBuffer sb = new StringBuffer();
																								
																								sb.append("Kalender hendelse fra barnehagen:\n\n");
																								sb.append(calendarDetail.getTitle().toUpperCase() + "\n");
																								sb.append(mDateFormat02.format(calendarDetail.getDateCreated()) + "\n");
																								sb.append(calendarDetail.getDescription() + "\n\n");
																								
																								if(calendarDocuments != null && calendarDocuments.size() > 0) {
																									for(CalendarDocument calendarDocument: calendarDocuments) {
																										sb.append(calendarDocument.getFilename() + "\n");
																									}
																								}
																								
																								mHandler.post(new Runnable() {
																									public void run() {
																										Intent intent = new Intent();
																										switch(which) {
																											case 0:
																												intent.setAction(Intent.ACTION_VIEW);
																												intent.putExtra("sms_body", sb.toString());
																												intent.setType("vnd.android-dir/mms-sms");
																												mContext.startActivity(intent);
																												break;
																											case 1:
																												intent = new Intent(Intent.ACTION_SEND);
																												intent.setType("text/plain");
																												intent.putExtra(Intent.EXTRA_SUBJECT, "Kalender - Barnehage");
																												intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
																												mContext.startActivity(Intent.createChooser(intent, "Send Email"));
																												break;
																										}
																									}
																								});
																							}
																						}
																					}).start();
																					
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
									}
								});
							}
						});
					}
				}
			}).start();
		}
	}
	
	private Uri createVCS(CalendarDetail calendarDetail) {
		File file = new File(Environment.getExternalStorageDirectory(), "kindergarden/calendar_event.vcs");
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			
			OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
			StringBuffer sb = new StringBuffer();
			sb.append("BEGIN:VCALENDAR\n");
			sb.append("PRODID:-//AT Content Types//AT Event//EN\n");
			sb.append("VERSION:2.0\n");
			sb.append("METHOD:PUBLISH\n");
			sb.append("BEGIN:VEVENT\n");
			sb.append("DTSTAMP:" + dateFormat.format(calendarDetail.getDateCreated()) + "\n");
			sb.append("CREATED:" + dateFormat.format(calendarDetail.getDateCreated()) + "\n");
			sb.append("LAST-MODIFIED:" + dateFormat.format(calendarDetail.getDateCreated()) + "\n");
			sb.append("SUMMARY:" + calendarDetail.getTitle() + "\n");
			sb.append("DESCRIPTION;ENCODING=QUOTED-PRINTABLE:" + calendarDetail.getDescription() + "\n");
			sb.append("DTSTART:" + dateFormat.format(calendarDetail.getDateCreated()) + "\n");
			sb.append("DTEND:" + dateFormat.format(calendarDetail.getDateCreated()) + "\n");
			sb.append("CLASS:PUBLIC\n");
			sb.append("END:VEVENT\n");
			sb.append("END:VCALENDAR\n");
			
			byte[] buffer = sb.toString().getBytes();
			output.write(buffer, 0, buffer.length);
			output.close();
			
			return Uri.fromFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
