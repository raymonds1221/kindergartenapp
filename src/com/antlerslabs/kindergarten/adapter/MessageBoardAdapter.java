package com.antlerslabs.kindergarten.adapter;

import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.pojo.Message;

public class MessageBoardAdapter extends ArrayAdapter<Message> {
	private Context mContext;
	private int mLayoutResourceId;
	private List<Message> mMessages;
	private LayoutInflater mLayoutInflater;
	private DateFormat mDateFormat = new SimpleDateFormat("dd.MM.yy - HH:mm");

	public MessageBoardAdapter(Context context, int layoutResourceId, List<Message> messages) {
		super(context, layoutResourceId, messages);
		
		mContext = context;
		mLayoutResourceId = layoutResourceId;
		mMessages = messages;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		final Message message = mMessages.get(position);
		
		if(view == null) {
			view = mLayoutInflater.inflate(mLayoutResourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.messageboard_content = (TextView) view.findViewById(R.id.messageboard_content);
			viewHolder.messageboard_user = (TextView) view.findViewById(R.id.messageboard_user);
			viewHolder.messageboard_date = (TextView) view.findViewById(R.id.messageboard_date);
			viewHolder.share_btn = (ImageView) view.findViewById(R.id.share_btn);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		viewHolder.messageboard_content.setText(message.getMessage());
		viewHolder.messageboard_user.setText(message.getUser());
		viewHolder.messageboard_date.setText(" - " + mDateFormat.format(message.getDateCreated()));
		viewHolder.share_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog alert = new AlertDialog.Builder(mContext)
													.setItems(R.array.share_menu, new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															Intent intent = new Intent();
															switch(which) {
																case 0:
																	Uri smsUri = Uri.parse("sms:");
																	intent.setAction(Intent.ACTION_VIEW);
																	intent.setData(smsUri);
																	intent.putExtra("sms_body", message.getMessage());
																	intent.setType("vnd.android-dir/mms-sms");
																	mContext.startActivity(intent);
																	break;
																case 1:
																	intent = new Intent(Intent.ACTION_SEND);
																	intent.setType("text/plain");
																	intent.putExtra(Intent.EXTRA_SUBJECT, "Meldinger - Barnehage");
																	intent.putExtra(Intent.EXTRA_TEXT, message.getMessage());
																	mContext.startActivity(Intent.createChooser(intent, "Send Email"));
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
		
		return view;
	}
	
	private class ViewHolder {
		public TextView messageboard_content;
		public TextView messageboard_user;
		public TextView messageboard_date;
		public ImageView share_btn;
	}
}
