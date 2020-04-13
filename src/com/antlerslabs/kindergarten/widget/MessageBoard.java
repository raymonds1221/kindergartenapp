package com.antlerslabs.kindergarten.widget;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.adapter.MessageBoardAdapter;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.pojo.Kindergarden;
import com.antlerslabs.kindergarten.pojo.Message;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import android.app.ProgressDialog;
import android.os.Handler;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.EditText;
import android.widget.TextView;

public class MessageBoard extends RelativeLayout {
	private Context mContext;
	private ListView list_messageboard;
	public ViewFlipper messageboard_switcher;
	private EditText message;
	private WebServiceRequest mWebServiceRequest;
	private DataSource mDataSource;
	private PreferenceWrapper mPrefsWrapper;
	private Parser mParser;
	private Network mNetwork;
	private List<Message> mMessages = new ArrayList<Message>();
	private MessageBoardAdapter mAdapter;
	private final Handler mHandler = new Handler();
	private int mPage = 1;
	private ProgressDialog mProgressDialog;

	public MessageBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.messageboard, this);
		
		mContext = context;
		list_messageboard = (ListView) findViewById(R.id.list_messageboard);
		messageboard_switcher = (ViewFlipper) findViewById(R.id.messageboard_switcher);
		message = (EditText) findViewById(R.id.message);
		mWebServiceRequest = WebServiceRequest.newInstance();
		mDataSource = DataSourceFactory.getDataSourceFactory(mContext);
		mPrefsWrapper = PreferenceWrapper.getInstance(context);
		mNetwork = Network.getInstance(context);
		
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setMessage("Loading...");
		
		mAdapter = new MessageBoardAdapter(mContext, R.layout.list_messageboard, mMessages);
		list_messageboard.setAdapter(mAdapter);
		
		findViewById(R.id.refresh_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				messageboard_switcher.setDisplayedChild(0);
				displayMessageBoard();
			}
		});
		
		findViewById(R.id.send_msg).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!message.getText().toString().equals("")) {
					addNewMessage(message.getText().toString());
					message.setText("");
				}
			}
		});
		
		message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_SEND) {
					addNewMessage(v.getText().toString());
					v.setText("");
					return true;
				}
				return false;
			}
		});
		
		list_messageboard.setOnScrollListener(new AbsListView.OnScrollListener() {
			private int firstVisibleItem;
			private int visibleItemCount;
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				this.firstVisibleItem = firstVisibleItem;
				this.visibleItemCount = visibleItemCount;
			}
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if((firstVisibleItem + visibleItemCount) == mAdapter.getCount() &&
						scrollState == SCROLL_STATE_IDLE) {
					Kindergarden profile = mDataSource.where("uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
														.getForObject(Kindergarden.class);
					if(profile.getTotalMessage() > mAdapter.getCount()) {
						findViewById(R.id.loadmore_progress).setVisibility(View.VISIBLE);
						loadMoreMessage();
					}
				}
			}
		});
	}
	
	public void displayMessageBoard() {
		mPage = 1;
		list_messageboard.setSelectionAfterHeaderView();
		
		if(mPrefsWrapper.getPreferenceBooleanValue(Constants.USER_IS_TEACHER) &&
				mPrefsWrapper.getPreferenceBooleanValue(Constants.TEACHER_HAS_PERMISSION))
			findViewById(R.id.view03).setVisibility(View.VISIBLE);
		else
			findViewById(R.id.view03).setVisibility(View.GONE);
		
		if(mNetwork.isAvailable()) {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						List<Message> messages = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																	.setApiName("message")
																	.addParameter("messages")
																	.addParameter("kgid")
																	.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																	.setResultFormat(WebServiceRequest.DataFormat.XML)
																	.setRequestMethod(WebServiceRequest.GET)
																	.getForObjects(Message.class);
						mMessages.clear();
						
						if(messages != null) {
							mMessages.addAll(messages);
							mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
										.delete(Message.class);
							
							for(Message message:messages) {
								message.setProfileUid(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID));
								mDataSource.insert(message);
							}
						}
							
						
						mHandler.post(new Runnable() {
							public void run() {
								messageboard_switcher.setDisplayedChild(1);
								mAdapter.notifyDataSetChanged();
							}
						});
					}
				}
			}).start();
		} else {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						List<Message> messages = mDataSource.where("profile_uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
															.orderBy("datecreated", "desc")
															.getForObjects(Message.class);
						mMessages.clear();
						
						if(messages != null)
							mMessages.addAll(messages);
						
						mHandler.post(new Runnable() {
							public void run() {
								messageboard_switcher.setDisplayedChild(1);
								mAdapter.notifyDataSetChanged();
							}
						});
					}
				}
			}).start();
		}
	}
	
	private void addNewMessage(final String message) {
		if(mNetwork.isAvailable()) {
			mProgressDialog.show();
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						String result = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
															.setApiName("message")
															.addParameter("add")
															.putPostValues("postedby_uid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID)))
															.putPostValues("profile_uid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
															.putPostValues("message", message)
															.setResultFormat(WebServiceRequest.DataFormat.XML)
															.setRequestMethod(WebServiceRequest.POST)
															.getRawResponse();
						mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
						mHandler.post(new Runnable() {
							public void run() {
								mProgressDialog.dismiss();
								if(mParser.getSpecificValue("status") != null && mParser.getSpecificValue("status").equals("success")) {
									Message msg = new Message();
									msg.setMessage(message);
									msg.setDateCreated(new Date());
									mMessages.add(0, msg);
									mAdapter.notifyDataSetChanged();
									msg.setProfileUid(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID));
									mDataSource.insert(msg);
								}
							}
						});
					}
				}
			}).start();
		} else {
			com.antlerslabs.kindergarten.helpers.Message.make(mContext, R.string.empty_string, R.string.check_network_connection_when_posting).toast();
		}
	}
	
	private void loadMoreMessage() {
		if(mNetwork.isAvailable()) {
			new Thread(new Runnable() {
				public void run() {
					synchronized(Constants.mLockObject) {
						mPage++;
						List<Message> messages = mWebServiceRequest.setApiUri(mContext.getString(R.string.api_url))
																		.setApiName("message")
																		.addParameter("messages")
																		.addParameter("kgid")
																		.addParameter(String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																		.addParameter("pageno")
																		.addParameter(String.valueOf(mPage))
																		.setResultFormat(WebServiceRequest.DataFormat.XML)
																		.setRequestMethod(WebServiceRequest.GET)
																		.getForObjects(Message.class);
						if(messages != null) {
							mMessages.addAll(messages);
							
							for(Message message: messages) {
								message.setProfileUid(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID));
								mDataSource.insert(message);
							}
						}
						
						mHandler.post(new Runnable() {
							public void run() {
								mAdapter.notifyDataSetChanged();
								findViewById(R.id.loadmore_progress).setVisibility(View.GONE);
							}
						});
					}
				}
			}).start();
		} else {
			findViewById(R.id.loadmore_progress).setVisibility(View.GONE);
		}
	}
}
