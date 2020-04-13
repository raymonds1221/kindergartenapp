package com.antlerslabs.kindergarten.adapter;

import java.util.List;

import android.os.Handler;
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

import com.antlerslabs.kindergarten.MainActivity;
import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.pojo.Kindergarden;

public class Kindergarden02Adapter extends ArrayAdapter<Kindergarden> {
	private Context mContext;
	private int mLayoutResourceId;
	private List<Kindergarden> mProfiles;
	private LayoutInflater mLayoutInflater;
	private Network mNetwork;
	private PreferenceWrapper mPrefsWrapper;
	private WebServiceRequest mWebServiceRequest;
	private Parser mParser;
	private DataSource mDataSource;
	private final Handler mHandler = new Handler();

	public Kindergarden02Adapter(Context context, int layoutResourceId, List<Kindergarden> profiles) {
		super(context, layoutResourceId, profiles);
		
		mContext = context;
		mLayoutResourceId = layoutResourceId;
		mProfiles = profiles;
		mLayoutInflater = LayoutInflater.from(context);
		mNetwork = Network.getInstance(context);
		mPrefsWrapper = PreferenceWrapper.getInstance(context);
		mWebServiceRequest = WebServiceRequest.newInstance();
		mDataSource = DataSourceFactory.getDataSourceFactory(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		final Kindergarden profile = mProfiles.get(position);
		
		if(view == null) {
			view = mLayoutInflater.inflate(mLayoutResourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.kindergarden_name = (TextView) view.findViewById(R.id.kindergarden_name);
			viewHolder.delete_btn = (ImageButton) view.findViewById(R.id.delte_btn);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		viewHolder.kindergarden_name.setText(profile.getName().toUpperCase());
		viewHolder.delete_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(profile.getUid() == mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)) {
					Message.make(mContext, R.string.empty_string, R.string.unable_to_delete).toast();
					return;
				}
				if(mNetwork.isAvailable()) {
					AlertDialog alert = new AlertDialog.Builder(mContext)
											.setMessage(R.string.delete_profile)
											.setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													new Thread(new Runnable() {
														public void run() {
															synchronized(Constants.mLockObject) {
																String result = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																									.setApiName("profile")
																									.addParameter("remove")
																									.putPostValues("userid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID)))
																									.putPostValues("kgid", String.valueOf(profile.getUid()))
																									.setResultFormat(WebServiceRequest.DataFormat.XML)
																									.setRequestMethod(WebServiceRequest.POST)
																									.getRawResponse();
																mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
																
																if(mParser.getSpecificValue("status") != null && !mParser.getSpecificValue("status").toString().equals("failure")) {
																	mDataSource.where("uid", profile.getUid())
																				.delete(Kindergarden.class);
																	mProfiles.remove(profile);
																	mHandler.post(new Runnable() {
																		public void run() {
																			notifyDataSetChanged();
																			Intent intent = new Intent(MainActivity.KINDERGARDEN_RECEIVER);
																			mContext.sendBroadcast(intent);
																		}
																	});
																}
															}
														}
													}).start();
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
					
				} else {
					Message.make(mContext, R.string.empty_string, R.string.check_network_connection).toast();
				}
			}
		});
		
		return view;
	}
	
	private class ViewHolder {
		public TextView kindergarden_name;
		public ImageButton delete_btn;
	}
}
