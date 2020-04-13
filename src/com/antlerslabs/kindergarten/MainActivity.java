package com.antlerslabs.kindergarten;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Handler;
import android.os.Process;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.BroadcastReceiver;
import android.view.Gravity;
import android.view.View;
import android.view.KeyEvent;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ViewFlipper;
import android.widget.TextView;
import android.net.ConnectivityManager;

import com.antlerslabs.kindergarten.service.IKGService;
import com.antlerslabs.kindergarten.service.IKGServiceCallback;
import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.adapter.KindergardenAdapter;
import com.antlerslabs.kindergarten.adapter.MenuAdapter;
import com.antlerslabs.kindergarten.db.*;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.pojo.Kindergarden;
import com.antlerslabs.kindergarten.service.*;
import com.antlerslabs.kindergarten.widget.*;

public class MainActivity extends Activity implements Settings.OnSettingsListener {
	private final int[][] mMenus = new int[][] {
			{R.drawable.menu_latest_news, R.string.menu_latest_news},
			{R.drawable.menu_messages, R.string.menu_messages},
			{R.drawable.menu_calendar, R.string.menu_calendar},
			{R.drawable.menu_about, R.string.menu_about},
			{R.drawable.menu_contact, R.string.menu_contact},
			{R.drawable.menu_settings, R.string.menu_settings_2}
	};
	private ViewFlipper sliding_menu_content;
	private TextView kindergarden_name;
	private ListView list_menu;
	private ListView kindergarden_list;
	private SlidingMenu main_sliding_menu;
	private LatestNews latest_news;
	private Calendar calendar;
	private MessageBoard message_board;
	private AboutUs about_us;
	private ContactUs contact_us;
	private Settings settings;
	private IKGService mIKGService;
	private DataSource mDataSource;
	private PreferenceWrapper mPrefsWrapper;
	private Network mNetwork;
	private final Handler mHandler = new Handler();
	private boolean mHasRequestedProfiles = false;
	public static final String KINDERGARDEN_RECEIVER = "com.antlers.kindergarden.KINDERGARDEN_RECEIVER";
	private KindergardenReceiver mKindergardenReceiver;
	private ConnectionStateReceiver mConnectionStateReceiver;
	private boolean mFinishRequestingProfiles = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		sliding_menu_content = (ViewFlipper) findViewById(R.id.sliding_menu_content);
		list_menu = (ListView) findViewById(R.id.list_menu);
		kindergarden_name = (TextView) findViewById(R.id.kindergarden_name);
		kindergarden_list = (ListView) findViewById(R.id.kindergarden_list);
		main_sliding_menu = (SlidingMenu) findViewById(R.id.main_sliding_menu);
		latest_news = (LatestNews) findViewById(R.id.latest_news);
		calendar = (Calendar) findViewById(R.id.calendar);
		message_board = (MessageBoard) findViewById(R.id.message_board);
		about_us = (AboutUs) findViewById(R.id.about_us);
		contact_us = (ContactUs) findViewById(R.id.contact_us);
		settings = (Settings) findViewById(R.id.settings);
		MenuAdapter menuAdapter = new MenuAdapter(this, R.layout.list_menu, mMenus);
		list_menu.setAdapter(menuAdapter);
		
		mDataSource = DataSourceFactory.getDataSourceFactory(this);
		mPrefsWrapper = PreferenceWrapper.getInstance(this);
		mNetwork = Network.getInstance(this);
		
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, AbsListView.LayoutParams.FILL_PARENT);
		LinearLayout linearLayout = new LinearLayout(this);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.RIGHT;
		ImageButton add_btn = new ImageButton(this);
		add_btn.setLayoutParams(layoutParams);
		add_btn.setImageResource(R.drawable.menu_add);
		add_btn.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		add_btn.setPadding(20, 10, 10, 10);
		linearLayout.addView(add_btn);
		linearLayout.setLayoutParams(params);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		kindergarden_list.addFooterView(linearLayout);
		
		list_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				sliding_menu_content.setDisplayedChild(position);
				main_sliding_menu.hideMenu();
				
				switch(position) {
					case 0:
						latest_news.latest_news_switcher.setDisplayedChild(0);
						Thread.currentThread().interrupt();
						latest_news.displayNewPost();
						latest_news.displayWall();
						break;
					case 1:
						message_board.messageboard_switcher.setDisplayedChild(0);
						message_board.displayMessageBoard();
						break;
					case 2:
						calendar.displayCalendar();
						break;
					case 3:
						about_us.aboutus_switcher.setDisplayedChild(0);
						about_us.displayAboutUs();
						break;
					case 4:
						contact_us.displayContactUs();
						break;
					case 5:
						settings.displayPushNotification();
						break;
				}
			}
		});
		
		add_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				main_sliding_menu.hideMenu();
				Intent intent = new Intent(MainActivity.this, AddKindergarden.class);
				startActivityForResult(intent, Constants.REQUEST_CODE_ADD_KINDERGARDEN);
			}
		});
		
		linearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				main_sliding_menu.hideMenu();
				Intent intent = new Intent(MainActivity.this, AddKindergarden.class);
				startActivityForResult(intent, Constants.REQUEST_CODE_ADD_KINDERGARDEN);
			}
		});
		
		mKindergardenReceiver = new KindergardenReceiver();
		IntentFilter intentFilter = new IntentFilter(KINDERGARDEN_RECEIVER);
		registerReceiver(mKindergardenReceiver, intentFilter);
		
		mConnectionStateReceiver = new ConnectionStateReceiver();
		intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mConnectionStateReceiver, intentFilter);
		
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null) {
			int push_by = bundle.getInt(Constants.PUSH_BY);
			
			switch(push_by) {
				case Constants.PUSH_BY_MESSAGEBOARD:
					sliding_menu_content.setDisplayedChild(1);
					message_board.messageboard_switcher.setDisplayedChild(0);
					message_board.displayMessageBoard();
					break;
				case Constants.PUSH_BY_CALENDAR:
					sliding_menu_content.setDisplayedChild(2);
					calendar.displayCalendar();
					break;
			}
			
			mFinishRequestingProfiles = bundle.getBoolean(Constants.FINISH_REQUESTING_PROFILES);
			
			if(mFinishRequestingProfiles)
				populateKGList();
		}
		
		if(!mNetwork.isAvailable()) {
			Message.make(this, R.string.empty_string, R.string.check_network_connection).toast();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Intent intent = new Intent(this, KGService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
		
		if(!mHasRequestedProfiles && !mFinishRequestingProfiles) {
			mHandler.postDelayed(new Runnable() {
				public void run() {
					int userId = mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID);
					try {
						mIKGService.requestProfiles(userId);
						
						if(mPrefsWrapper.getPreferenceBooleanValue(Constants.SYNC_CALENDAR_ENABLED) &&
								mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_CALENDAR_ID) > 0) {
							mIKGService.requestSyncCalendar();
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}, 2000);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unbindService(mServiceConnection);
		unregisterReceiver(mKindergardenReceiver);
		unregisterReceiver(mConnectionStateReceiver);
		Process.killProcess(Process.myPid());
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Constants.REQUEST_CODE_NEW_POST && resultCode == RESULT_OK) {
			latest_news.latest_news_switcher.setDisplayedChild(0);
			latest_news.displayWall();
		}
	}
	
	private void populateKGList() {
		new Thread(new Runnable() {
			public void run() {
				synchronized(Constants.mLockObject) {
					final List<Kindergarden> profiles = mDataSource.getForObjects(Kindergarden.class);
					final Kindergarden profile = mDataSource.where("uid", mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID))
							.getForObject(Kindergarden.class);
					
					runOnUiThread(new Runnable() {
						public void run() {
							latest_news.displayNewPost();
							latest_news.displayWall();
							latest_news.requestLatestNewsCount();
							
							kindergarden_name.setText(profile.getName().toUpperCase());
							
							final KindergardenAdapter adapter = new KindergardenAdapter(MainActivity.this, R.layout.list_kindergarden, profiles);
							kindergarden_list.setAdapter(adapter);
							
							kindergarden_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									main_sliding_menu.hideMenu();
									final Kindergarden profile = profiles.get(position);
									boolean isProfileViewed = profile.isProfileViewed();
									
									kindergarden_name.setText(profile.getName().toUpperCase());
									
									Thread.currentThread().interrupt();
									
									new Thread(new Runnable() {
										public void run() {
											synchronized(Constants.mLockObject) {
												profile.setProfileViewed(true);
												profile.setNewMessage(0);
												mDataSource.where("uid", profile.getUid())
															.update(profile);
												
												runOnUiThread(new Runnable() {
													public void run() {
														adapter.notifyDataSetChanged();
													}
												});
											}
										}
									}).start();
									
									mPrefsWrapper.setPreferenceBooleanValue(Constants.USER_IS_TEACHER, profile.getType().equals("TEACHER"));
									mPrefsWrapper.setPreferenceIntValue(Constants.DEFAULT_KG_ID, profile.getUid());
									mPrefsWrapper.setPreferenceBooleanValue(Constants.NEED_REQUEST_DOCUMENTS, true);
									
									if(isProfileViewed) {
										sliding_menu_content.setDisplayedChild(0);
										latest_news.latest_news_switcher.setDisplayedChild(0);
										latest_news.displayNewPost();
										latest_news.displayWall();
									} else {
										sliding_menu_content.setDisplayedChild(1);
										message_board.messageboard_switcher.setDisplayedChild(0);
										message_board.displayMessageBoard();
									}
								}
							});
						}
					});
				}
			}
		}).start();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(main_sliding_menu.isMenuShowing()) {
				main_sliding_menu.hideMenu();
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mIKGService = IKGService.Stub.asInterface(service);
			
			try {
				mIKGService.registerCallback(mIKGServiceCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			try {
				mIKGService.unregisterCallback(mIKGServiceCallback);
			} catch(RemoteException e) {
				e.printStackTrace();
			}
			mIKGService = null;
		}
	};
	
	private final IKGServiceCallback.Stub mIKGServiceCallback = new IKGServiceCallback.Stub() {
		@Override
		public void onFinishRequestingProfiles() throws RemoteException {
			mHasRequestedProfiles = true;
			populateKGList();
		}
	};
	
	public void onViewHideMenu(View view) {
		if(main_sliding_menu.isMenuShowing()) {
			main_sliding_menu.hideMenu();
		} else {
			main_sliding_menu.showMenu();
		}
	}
	
	private class KindergardenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			populateKGList();
		}
	}
	
	private class ConnectionStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Network network = Network.getInstance(context);
			
			if(network.isAvailable()) {
				latest_news.displayNewPostWithoutMessage();
			} else {
				Message.make(context, R.string.empty_string, R.string.check_network_connection).toast();
			}
		}
	}

	@Override
	public void onSyncCalendar() {
		try {
			mIKGService.requestSyncCalendar();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
