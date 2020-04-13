package com.antlerslabs.kindergarten;

import java.util.List;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.adapter.CalendarDocumentAdapter;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.pojo.CalendarDetail;
import com.antlerslabs.kindergarten.pojo.CalendarDocument;

public class CalendarDetails extends Activity {
	private TextView calendar_date;
	private TextView calendar_description;
	private ListView list_attachment;
	private CalendarDetail mCalendarDetail;
	private final DateFormat mDateFormat = new SimpleDateFormat("dd MMMM");
	private final DateFormat mDateFormat02 = new SimpleDateFormat("dd MMMM");
	private DataSource mDataSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_details);
		
		calendar_date = (TextView) findViewById(R.id.calendar_date);
		calendar_description = (TextView) findViewById(R.id.calendar_description);
		list_attachment = (ListView) findViewById(R.id.list_attachment);
		mDataSource = DataSourceFactory.getDataSourceFactory(this);
		
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null) {
			mCalendarDetail = (CalendarDetail) bundle.getParcelable(Constants.CALENDAR_DETAIL);
			
			calendar_date.setText(mDateFormat.format(mCalendarDetail.getDateCreated()).toUpperCase());
			calendar_description.setText(mCalendarDetail.getDescription());
			
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						final List<CalendarDocument> calendarDocuments = mDataSource.where("calendar_uid", mCalendarDetail.getUid())
																				.getForObjects(CalendarDocument.class);
						runOnUiThread(new Runnable() {
							public void run() {
								if(calendarDocuments != null) {
									CalendarDocumentAdapter adapter = new CalendarDocumentAdapter(CalendarDetails.this, R.layout.list_attachment, calendarDocuments);
									list_attachment.setAdapter(adapter);
									
									list_attachment.setOnItemClickListener(new AdapterView.OnItemClickListener() {

										@Override
										public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
											CalendarDocument calendarDocument = calendarDocuments.get(position);
											Intent intent = new Intent(CalendarDetails.this, DocumentViewer.class);
											intent.putExtra(Constants.DOCUMENT_NAME, calendarDocument.getFilename());
											startActivity(intent);
										}
									});
								}
							}
						});
					}
				}
			}).start();
		}
		
		findViewById(R.id.share_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog alert = new AlertDialog.Builder(CalendarDetails.this)
													.setItems(R.array.share_menu, new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															Intent intent = new Intent();
															
															final List<CalendarDocument> calendarDocuments = mDataSource.where("calendar_uid", mCalendarDetail.getUid())
																	.getForObjects(CalendarDocument.class);
															StringBuffer sb = new StringBuffer();
															
															sb.append("Kalender hendelse fra barnehagen:\n\n");
															sb.append(mCalendarDetail.getTitle().toUpperCase() + "\n");
															sb.append(mDateFormat02.format(mCalendarDetail.getDateCreated()) + "\n");
															sb.append(mCalendarDetail.getDescription() + "\n\n");
															
															if(calendarDocuments != null && calendarDocuments.size() > 0) {
																for(CalendarDocument calendarDocument: calendarDocuments) {
																	sb.append(calendarDocument.getFilename() + "\n");
																}
															}
															
															switch(which) {
																case 0:
																	intent.setAction(Intent.ACTION_VIEW);
																	intent.putExtra("sms_body", sb.toString());
																	intent.setType("vnd.android-dir/mms-sms");
																	startActivity(intent);
																	break;
																case 1:
																	intent = new Intent(Intent.ACTION_SEND);
																	intent.setType("text/plain");
																	intent.putExtra(Intent.EXTRA_SUBJECT, "Kalender - Barnehage");
																	intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
																	startActivity(Intent.createChooser(intent, "Send Email"));
																	break;
															}
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
		
		findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
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
