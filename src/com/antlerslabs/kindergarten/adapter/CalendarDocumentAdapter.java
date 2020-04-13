package com.antlerslabs.kindergarten.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antlerslabs.kindergarten.pojo.CalendarDocument;

public class CalendarDocumentAdapter extends ArrayAdapter<CalendarDocument> {
	private int mLayoutResourceId;
	private List<CalendarDocument> mCalendarDocuments;
	private LayoutInflater mLayoutInflater;

	public CalendarDocumentAdapter(Context context, int layoutResourceId, List<CalendarDocument> calendarDocuments) {
		super(context, layoutResourceId, calendarDocuments);
		
		mLayoutResourceId = layoutResourceId;
		mCalendarDocuments = calendarDocuments;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		TextView attachment_name = null;
		CalendarDocument calendarDocument = mCalendarDocuments.get(position);
		
		if(view == null) {
			view = mLayoutInflater.inflate(mLayoutResourceId, null);
		}
		
		attachment_name = (TextView) view;
		attachment_name.setText(calendarDocument.getTitle());
		
		return attachment_name;
	}
}
