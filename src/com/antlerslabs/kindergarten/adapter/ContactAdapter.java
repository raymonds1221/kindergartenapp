package com.antlerslabs.kindergarten.adapter;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageButton;
import android.net.Uri;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.helpers.Constants;
import com.antlerslabs.kindergarten.helpers.PreferenceWrapper;
import com.antlerslabs.kindergarten.pojo.Teacher;

public class ContactAdapter extends ArrayAdapter<Teacher> {
	private Context mContext;
	private int mLayoutResourceId;
	private List<Teacher> mTeachers;
	private LayoutInflater mLayoutInflater;
	private PreferenceWrapper mPrefsWrapper;

	public ContactAdapter(Context context, int layoutResourceId, List<Teacher> teachers) {
		super(context, layoutResourceId, teachers);
		
		mContext = context;
		mLayoutResourceId = layoutResourceId;
		mTeachers = teachers;
		
		mLayoutInflater = LayoutInflater.from(context);
		mPrefsWrapper = PreferenceWrapper.getInstance(context);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		final Teacher teacher = mTeachers.get(position);
		
		if(view == null) {
			view = mLayoutInflater.inflate(mLayoutResourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.contact_name = (TextView) view.findViewById(R.id.contact_name);
			viewHolder.call_btn = (ImageButton) view.findViewById(R.id.call_btn);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		String name = "";
		
		if(teacher.getFirstname() != null) {
			name = teacher.getFirstname().toUpperCase();
		}
		if(teacher.getLastname() != null) {
			name += " " + teacher.getLastname().toUpperCase();
		}
		viewHolder.contact_name.setText(name);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(teacher.getPhoneNumber() != null && !teacher.getPhoneNumber().equals("") &&
						mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID) != teacher.getUid()) {
					AlertDialog alert = new AlertDialog.Builder(mContext)
														.setMessage(R.string.call_prompt)
														.setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																Intent intent = new Intent(Intent.ACTION_CALL);
																intent.setData(Uri.parse("tel:" + teacher.getPhoneNumber()));
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
				}
			}
		});
		
		viewHolder.call_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(teacher.getPhoneNumber() != null && !teacher.getPhoneNumber().equals("") &&
						mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID) != teacher.getUid()) {
					AlertDialog alert = new AlertDialog.Builder(mContext)
														.setMessage(R.string.call_prompt)
														.setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																Intent intent = new Intent(Intent.ACTION_CALL);
																intent.setData(Uri.parse("tel:" + teacher.getPhoneNumber()));
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
				}
			}
		});
		
		if(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID) == teacher.getUid()) {
			viewHolder.call_btn.setVisibility(View.GONE);
		}
		
		return view;
	}
	
	private class ViewHolder {
		public TextView contact_name;
		public ImageButton call_btn;
	}
}
