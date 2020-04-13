package com.antlerslabs.kindergarten.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.pojo.Kindergarden;

public class KindergardenAdapter extends ArrayAdapter<Kindergarden> {
	private int mLayoutResourceId;
	private List<Kindergarden> mProfiles;
	private LayoutInflater mLayoutInflater;

	public KindergardenAdapter(Context context, int layoutResourceId, List<Kindergarden> profiles) {
		super(context, layoutResourceId, profiles);
		mLayoutResourceId = layoutResourceId;
		mProfiles = profiles;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		Kindergarden profile = mProfiles.get(position);
		
		if(view == null) {
			view = mLayoutInflater.inflate(mLayoutResourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.kindergarden_name = (TextView) view.findViewById(R.id.kindergarden_name);
			viewHolder.notification_count = (TextView) view.findViewById(R.id.notification_count);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		viewHolder.kindergarden_name.setText(profile.getName().toUpperCase());
		
		if(profile.getNewMessage() > 0) {
			viewHolder.notification_count.setVisibility(View.VISIBLE);
			viewHolder.notification_count.setText(String.valueOf(profile.getNewMessage()));
		} else {
			viewHolder.notification_count.setVisibility(View.GONE);
		}
		
		return view;
	}
	
	private class ViewHolder {
		public TextView kindergarden_name;
		public TextView notification_count;
	}
}
