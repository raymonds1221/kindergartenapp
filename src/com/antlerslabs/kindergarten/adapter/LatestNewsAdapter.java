package com.antlerslabs.kindergarten.adapter;

import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.os.Handler;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.antlerslabs.kindergarten.MainActivity;
import com.antlerslabs.kindergarten.LatestNewsImageViewer;
import com.antlerslabs.kindergarten.NewPost;
import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.helpers.Constants;
import com.antlerslabs.kindergarten.helpers.Network;
import com.antlerslabs.kindergarten.helpers.PreferenceWrapper;
import com.antlerslabs.kindergarten.pojo.LatestNews;
import com.antlerslabs.kindergarten.db.*;

public class LatestNewsAdapter extends ArrayAdapter<LatestNews> {
	private Context mContext;
	private int mLayoutResourceId;
	private List<LatestNews> mLatestNews;
	private LayoutInflater mLayoutInflater;
	private DateFormat mDateFormat;
	private DataSource mDataSource;
	private Network mNetwork;
	private PreferenceWrapper mPrefsWrapper;
	private final Handler mHandler = new Handler();

	public LatestNewsAdapter(Context context, int layoutResourceId, List<LatestNews> latestNews) {
		super(context, layoutResourceId, latestNews);
		
		mContext = context;
		mLayoutResourceId = layoutResourceId;
		mLatestNews = latestNews;
		mLayoutInflater = LayoutInflater.from(context);
		mDateFormat = new SimpleDateFormat("MM.dd.yy - HH:mm");
		mDataSource = DataSourceFactory.getDataSourceFactory(context);
		mNetwork = Network.getInstance(context);
		mPrefsWrapper = PreferenceWrapper.getInstance(context);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder viewHolder;
		final LatestNews latestNews = mLatestNews.get(position);
		
		if(view == null) {
			view = mLayoutInflater.inflate(mLayoutResourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.latest_news_image = (ImageView) view.findViewById(R.id.latest_news_image);
			viewHolder.latest_news_title = (TextView) view.findViewById(R.id.latest_news_title);
			viewHolder.latest_news_details = (TextView) view.findViewById(R.id.latest_news_details);
			viewHolder.edit_btn = (ImageButton) view.findViewById(R.id.edit_btn);
			viewHolder.latest_news_publisher = (TextView) view.findViewById(R.id.latest_news_publisher);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		if(latestNews.getImageBitmap() != null) {
			viewHolder.latest_news_image.setImageBitmap(latestNews.getImageBitmap());
			viewHolder.latest_news_image.setVisibility(View.VISIBLE);
		} else {
			viewHolder.latest_news_image.setVisibility(View.GONE);
		}
		if(latestNews.getTitle() != null)
			viewHolder.latest_news_title.setText(latestNews.getTitle().toUpperCase());
		viewHolder.latest_news_details.setText(latestNews.getDetails());
		if(latestNews.getDateCreated() != null)
		viewHolder.latest_news_publisher.setText(latestNews.getName() + " - " + mDateFormat.format(latestNews.getDateCreated()));
		
		int postedByUid = mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID);
		
		if(latestNews.getPostedByUid() == postedByUid && mPrefsWrapper.getPreferenceBooleanValue(Constants.USER_IS_TEACHER) && mNetwork.isAvailable()) {
			viewHolder.edit_btn.setVisibility(View.VISIBLE);
		} else {
			viewHolder.edit_btn.setVisibility(View.GONE);
		}
		
		viewHolder.edit_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, NewPost.class);
				intent.putExtra(Constants.LATEST_NEWS_ACTION, Constants.LATEST_NEWS_EDIT);
				intent.putExtra(Constants.ACTIVE_LATEST_NEWS, latestNews);
				((MainActivity) mContext).startActivityForResult(intent, Constants.REQUEST_CODE_NEW_POST);
			}
		});
		
		viewHolder.latest_news_image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, LatestNewsImageViewer.class);
				//intent.putExtra(Constants.LATEST_NEWS_IMAGE, latestNews.getImageBitmap());
				Constants.mLatestNewsImage = latestNews.getImageBitmap();
				mContext.startActivity(intent);
			}
		});
		
		return view;
	}
	
	public void downloadImage() {
		new Thread(new Runnable() {
			public void run() {
				synchronized(Constants.mLockObject) {
					for(LatestNews latestNews: mLatestNews) {
						if(!Thread.interrupted()) {
							Bitmap bitmap = BitmapFactory.decodeStream(mNetwork.downloadOnlineContent(latestNews.getImage()));
							latestNews.setImageBitmap(bitmap);
							
							mDataSource.where("uid", latestNews.getUid())
										.update(latestNews);
							
							mHandler.post(new Runnable() {
								public void run() {
									notifyDataSetChanged();
								}
							});
						} else {
							break;
						}
					}
				}
			}
		}).start();
	}
	
	private class ViewHolder {
		public ImageView latest_news_image;
		public TextView latest_news_title;
		public TextView latest_news_details;
		public ImageButton edit_btn;
		public TextView latest_news_publisher;
	}
}
