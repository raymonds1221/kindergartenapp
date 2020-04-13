package com.antlerslabs.kindergarten.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class Message {
	private Context mContext;
	private Toast mToast;
	private AlertDialog mAlert;
	private String mTitle;
	private String mText;
	
	private Message(Context context, String title, String message) {
		mContext = context;
		mTitle = title;
		mText = message;
	}
	
	private Message(Context context, int title, int message) {
		mContext = context;
		mTitle = context.getString(title);
		mText = context.getString(message);
	}

	public static Message make(Context context, String title, String message) {
		return new Message(context, title, message);
	}
	
	public static Message make(Context context, int title, int message) {
		return new Message(context, title, message);
	}
	
	public void toast() {
		mToast = Toast.makeText(mContext, mText, Toast.LENGTH_LONG);
		mToast.show();
	}
	
	public void alert() {
		mAlert = new AlertDialog.Builder(mContext)
								.setCancelable(false)
								.setTitle(mTitle)
								.setMessage(mText)
								.setNeutralButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}).create();
		mAlert.show();
	}
}
