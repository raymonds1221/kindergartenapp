package com.antlerslabs.kindergarten.widget;

import com.antlerslabs.kindergarten.MainActivity;
import com.antlerslabs.kindergarten.NewPost;
import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.adapter.LatestNewsAdapter;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.db.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.os.Handler;
import android.content.Intent;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;
import android.widget.TextView;
import android.widget.AbsListView;

public class LatestNews extends LinearLayout {
	private Context mContext;
	private WebServiceRequest mWebServiceRequest;
	private DataSource mDataSource;
	private PreferenceWrapper mPrefsWrapper;
	private Network mNetwork;
	private Parser mParser;
	private final Handler mHandler = new Handler();
	public ViewFlipper latest_news_switcher;
	private PullToRefreshListView list_latest_news;
	private TextView latest_news_last_update;
	private final DateFormat mDateFormat = new SimpleDateFormat("HH:mm");
	private LatestNewsAdapter mAdapter;
	private final List<com.antlerslabs.kindergarten.pojo.LatestNews> mLatestNews = new ArrayList<com.antlerslabs.kindergarten.pojo.LatestNews>();
	private int mPage = 1;
	private int mPostCount;

	public LatestNews(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.latest_news, this);
		
		mContext = context;
		mWebServiceRequest = WebServiceRequest.newInstance();
		mDataSource = DataSourceFactory.getDataSourceFactory(context);
		mPrefsWrapper = PreferenceWrapper.getInstance(context);
		mNetwork = Network.getInstance(context);
		
		latest_news_switcher = (ViewFlipper) findViewById(R.id.latest_news_switcher);
		list_latest_news = (PullToRefreshListView) findViewById(R.id.list_latest_news);
		latest_news_last_update = (TextView) findViewById(R.id.latest_news_last_update);
		
		//displayNewPost();
		//displayWall();
		//requestLatestNewsCount();
		
		findViewById(R.id.new_post_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, NewPost.class);
				intent.putExtra(Constants.LATEST_NEWS_ACTION, Constants.LATEST_NEWS_NEW);
				((MainActivity) mContext).startActivityForResult(intent, Constants.REQUEST_CODE_NEW_POST);
			}
		});
	}
	
	public void displayNewPost() {
		if(mNetwork.isAvailable()) {
			if(mPrefsWrapper.getPreferenceBooleanValue(Constants.USER_IS_TEACHER)) {
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							int userId = mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID);
							String result = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																.setApiName("user")
																.addParameter("allowpost")
																.addParameter("userid")
																.addParameter(String.valueOf(userId))
																.addParameter("kgid")
																.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																.setRequestMethod(WebServiceRequest.GET)
																.getRawResponse();
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							mHandler.post(new Runnable() {
								public void run() {
									if(mParser.getSpecificValue("allow_post") != null && mParser.getSpecificValue("allow_post").toString().equals("1")) {
										findViewById(R.id.new_post_btn).setVisibility(View.VISIBLE);
										mPrefsWrapper.setPreferenceBooleanValue(Constants.TEACHER_HAS_PERMISSION, true);
									} else {
										findViewById(R.id.new_post_btn).setVisibility(View.GONE);
										mPrefsWrapper.setPreferenceBooleanValue(Constants.TEACHER_HAS_PERMISSION, false);
										//Message.make(mContext, R.string.no_permission_title, R.string.no_permission_message).alert();
									}
								}
							});
						}
					}
				}).start();
			} else {
				findViewById(R.id.new_post_btn).setVisibility(View.GONE);
			}
		} else {
			findViewById(R.id.new_post_btn).setVisibility(View.GONE);
		}
	}
	
	public void displayNewPostWithoutMessage() {
		if(mNetwork.isAvailable()) {
			if(mPrefsWrapper.getPreferenceBooleanValue(Constants.USER_IS_TEACHER)) {
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							int userId = mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID);
							String result = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																.setApiName("user")
																.addParameter("allowpost")
																.addParameter("userid")
																.addParameter(String.valueOf(userId))
																.addParameter("kgid")
																.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																.setRequestMethod(WebServiceRequest.GET)
																.getRawResponse();
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							mHandler.post(new Runnable() {
								public void run() {
									if(mParser.getSpecificValue("allow_post") != null && mParser.getSpecificValue("allow_post").toString().equals("1")) {
										findViewById(R.id.new_post_btn).setVisibility(View.VISIBLE);
										mPrefsWrapper.setPreferenceBooleanValue(Constants.TEACHER_HAS_PERMISSION, true);
									} else {
										findViewById(R.id.new_post_btn).setVisibility(View.GONE);
										mPrefsWrapper.setPreferenceBooleanValue(Constants.TEACHER_HAS_PERMISSION, false);
									}
								}
							});
						}
					}
				}).start();
			} else {
				findViewById(R.id.new_post_btn).setVisibility(View.GONE);
			}
		} else {
			findViewById(R.id.new_post_btn).setVisibility(View.GONE);
		}
	}
	
	public void displayWall() {
		if(mNetwork.isAvailable()) {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						mPage = 1;
						final List<com.antlerslabs.kindergarten.pojo.LatestNews> latestNews = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																											.setApiName("blog")
																											.addParameter("wall")
																											.addParameter("kgid")
																											.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																											.addParameter("format")
																											.setResultFormat(WebServiceRequest.DataFormat.XML)
																											.setRequestMethod(WebServiceRequest.GET)
																											.getForObjects(com.antlerslabs.kindergarten.pojo.LatestNews.class);
						if(latestNews != null) {
							mLatestNews.clear();
							mLatestNews.addAll(latestNews);
							
							for(com.antlerslabs.kindergarten.pojo.LatestNews ln: latestNews) {
								mDataSource.where("uid", ln.getUid())
											.getForObject(com.antlerslabs.kindergarten.pojo.LatestNews.class);
								if(mDataSource.getRecordCount() > 0)
									mDataSource.where("uid", ln.getUid())
												.delete(com.antlerslabs.kindergarten.pojo.LatestNews.class);
								ln.setProfileUid(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID));
								mDataSource.insert(ln);
							}
						}
						
						mHandler.post(new Runnable() {
							public void run() {
								if(findViewById(R.id.latest_news_update_progress).getVisibility() == View.VISIBLE) {
									findViewById(R.id.latest_news_update_progress).setVisibility(View.GONE);
								}
								
								list_latest_news.onRefreshComplete();
								latest_news_last_update.setText("SIST OPPDATERT: " + mDateFormat.format(new Date()));
								latest_news_switcher.setDisplayedChild(1);
								
								if(latestNews != null) {
									mAdapter = new LatestNewsAdapter(mContext, R.layout.list_latest_news, mLatestNews);
									list_latest_news.setAdapter(mAdapter);
									mAdapter.downloadImage();
								} else {
									list_latest_news.setAdapter(null);
								}
								
								list_latest_news.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
									@Override
									public void onRefresh() {
										requestLatestNewsCount();
										displayWall();
									}
								});
								
								list_latest_news.setOnScrollListener(new AbsListView.OnScrollListener() {
									private int firstVisibleItem;
									private int visibleItemCount;
									
									@Override
									public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
										this.firstVisibleItem = firstVisibleItem;
										this.visibleItemCount = visibleItemCount;
									}
									
									@Override
									public void onScrollStateChanged(AbsListView view, int scrollState) {
										if((firstVisibleItem + visibleItemCount) >= mAdapter.getCount() && scrollState == SCROLL_STATE_IDLE) {
											if(mPostCount > mAdapter.getCount()) {
												loadMoreLatestNews();
											}
										}
									}
								});
							}
						});
					}
					
				}
			}).start();
		} else {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						final List<com.antlerslabs.kindergarten.pojo.LatestNews> latestNews = mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
																								.orderBy("datecreated", "desc")
																								.getForObjects(com.antlerslabs.kindergarten.pojo.LatestNews.class);
						
						mLatestNews.clear();
						mLatestNews.addAll(latestNews);
						
						mHandler.post(new Runnable() {
							public void run() {
								if(findViewById(R.id.latest_news_update_progress).getVisibility() == View.VISIBLE) {
									findViewById(R.id.latest_news_update_progress).setVisibility(View.GONE);
								}
								
								list_latest_news.onRefreshComplete();
								latest_news_last_update.setText("SIST OPPDATERT: " + mDateFormat.format(new Date()));
								latest_news_switcher.setDisplayedChild(1);
								
								if(latestNews != null && latestNews.size() > 0) {
									mAdapter = new LatestNewsAdapter(mContext, R.layout.list_latest_news, mLatestNews);
									list_latest_news.setAdapter(mAdapter);
									
									list_latest_news.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
										@Override
										public void onRefresh() {
											requestLatestNewsCount();
											displayWall();
										}
									});
								} else {
									list_latest_news.setAdapter(null);
								}
							}
						});
					}
				}
			}).start();
		}
	}
	
	private void loadMoreLatestNews() {
		findViewById(R.id.load_more_latest_news_progress).setVisibility(View.VISIBLE);
		
		new Thread(new Runnable() {
			public void run() {
				synchronized(Constants.mLockObject) {
					mPage++;
					final List<com.antlerslabs.kindergarten.pojo.LatestNews> latestNews = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																											.setApiName("blog")
																											.addParameter("wall")
																											.addParameter("kgid")
																											.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																											.addParameter("pageno")
																											.addParameter(String.valueOf(mPage))
																											.addParameter("format")
																											.setResultFormat(WebServiceRequest.DataFormat.XML)
																											.setRequestMethod(WebServiceRequest.GET)
																											.getForObjects(com.antlerslabs.kindergarten.pojo.LatestNews.class);
					if(latestNews != null) {
						mLatestNews.addAll(latestNews);
						mDataSource.insertAll(latestNews);
					}
						
					
					mHandler.post(new Runnable() {
						public void run() {
							mAdapter.notifyDataSetChanged();
							mAdapter.downloadImage();
							
						}
					});
				}
			}
		}).start();
	}
	
	public void requestLatestNewsCount() {
		if(mNetwork.isAvailable()) {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						String result = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
															.setApiName("blog")
															.addParameter("count")
															.addParameter("kgid")
															.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
															.setResultFormat(WebServiceRequest.DataFormat.XML)
															.setRequestMethod(WebServiceRequest.GET)
															.getRawResponse();
						if(result != null) {
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							
							if(mParser.getSpecificValue("Virtual_Count") != null) {
								mPostCount = Integer.parseInt(mParser.getSpecificValue("Virtual_Count").toString());
								mPrefsWrapper.setPreferenceIntValue(Constants.LATEST_NEWS_COUNT, mPostCount);
							}
						}
					}
				}
			}).start();
		} else {
			mPostCount = mPrefsWrapper.getPreferenceIntValue(Constants.LATEST_NEWS_COUNT);
		}
	}
}
