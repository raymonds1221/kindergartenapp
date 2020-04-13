package com.antlerslabs.kindergarten.adapter;

import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageButton;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.pojo.CalendarDetail;
import com.antlerslabs.kindergarten.pojo.CalendarDocument;

public class CalendarAdapter extends ArrayAdapter<CalendarDetail> {
	private Context mContext;
	private int mLayoutResourceId;
	private List<CalendarDetail> mCalendarDetails;
	private LayoutInflater mLayoutInflater;
	private DataSource mDataSource;
	private DateFormat mDateFormat = new SimpleDateFormat("dd");
	private OnItemClickListener onItemClickListener;

	public CalendarAdapter(Context context, int layoutResourceId, List<CalendarDetail> calendarDetails) {
		super(context, layoutResourceId, calendarDetails);
		mContext = context;
		mLayoutResourceId = layoutResourceId;
		mCalendarDetails = calendarDetails;
		mDataSource = DataSourceFactory.getDataSourceFactory(context);
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		final CalendarDetail calendarDetail = mCalendarDetails.get(position);
		
		if(view == null) {
			view = mLayoutInflater.inflate(mLayoutResourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.calendar_day = (TextView) view.findViewById(R.id.calendar_day);
			viewHolder.calendar_title = (TextView) view.findViewById(R.id.calendar_title);
			viewHolder.calendar_comment = (ImageButton) view.findViewById(R.id.calendar_comment);
			viewHolder.calendar_attachment = (ImageButton) view.findViewById(R.id.calendar_attachment);
			viewHolder.calendar_share = (ImageButton) view.findViewById(R.id.calendar_share);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		if(calendarDetail.getDateCreated().before(new Date())) {
			viewHolder.calendar_day.setTextColor(mContext.getResources().getColor(R.color.inactive_calendar));
			viewHolder.calendar_title.setTextColor(mContext.getResources().getColor(R.color.inactive_calendar));
			viewHolder.calendar_comment.setImageResource(R.drawable.comment_icon_inactive);
			viewHolder.calendar_attachment.setImageResource(R.drawable.attachment_icon_inactive);
			viewHolder.calendar_share.setImageResource(R.drawable.share_btn_inactive);
		}
		
		viewHolder.calendar_day.setText(mDateFormat.format(calendarDetail.getDateCreated()));
		viewHolder.calendar_title.setText(calendarDetail.getTitle().toUpperCase());
		
		if(calendarDetail.getDescription() != null && !calendarDetail.getDescription().trim().equals("")) {
			viewHolder.calendar_comment.setVisibility(View.VISIBLE);
		} else {
			viewHolder.calendar_comment.setVisibility(View.GONE);
		}
		
		mDataSource.where("calendar_uid", calendarDetail.getUid())
					.getForObjects(CalendarDocument.class);
		if(mDataSource.getRecordCount() == 0) {
			viewHolder.calendar_attachment.setVisibility(View.GONE);
		} else {
			viewHolder.calendar_attachment.setVisibility(View.VISIBLE);
		}
		
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(onItemClickListener != null)
					onItemClickListener.onItemClick(calendarDetail);
			}
		});
		
		viewHolder.calendar_attachment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(onItemClickListener != null)
					onItemClickListener.onAttachmentClick(calendarDetail);
			}
		});
		
		viewHolder.calendar_share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(onItemClickListener != null)
					onItemClickListener.onShareClick(calendarDetail);
			}
		});
		
		return view;
	}
	
	private class ViewHolder {
		public TextView calendar_day;
		public TextView calendar_title;
		public ImageButton calendar_comment;
		public ImageButton calendar_attachment;
		public ImageButton calendar_share;
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		onItemClickListener = listener;
	}
	
	public static interface OnItemClickListener {
		public void onItemClick(CalendarDetail calendarDetail);
		public void onAttachmentClick(CalendarDetail calendarDetail);
		public void onShareClick(CalendarDetail calendarDetail);
	}
}
