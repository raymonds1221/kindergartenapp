package com.antlerslabs.kindergarten;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.helpers.Constants;
import com.antlerslabs.kindergarten.helpers.PreferenceWrapper;
import com.antlerslabs.kindergarten.pojo.Kindergarden;
import com.antlerslabs.kindergarten.service.*;
import com.antlerslabs.kindergarten.db.*;

public class LauncherActivity extends Activity {
	private PreferenceWrapper mPrefsWrapper;
	private final Handler mHandler = new Handler();
	private IKGService mIKGService;
	private boolean mFinishRequestingProfiles = false;
	private DataSource mDataSource;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		mPrefsWrapper = PreferenceWrapper.getInstance(this);
		mDataSource = DataSourceFactory.getDataSourceFactory(this);
		
		mHandler.postDelayed(new Runnable() {
			public void run() {
				new Thread(new Runnable() {
					public void run() {
						final Intent intent = new Intent();
						final Context context = LauncherActivity.this;
						
						if(!mPrefsWrapper.getPreferenceBooleanValue(Constants.USER_IS_REGISTERED) ||
								!mPrefsWrapper.getPreferenceBooleanValue(Constants.REGISTERED_TO_CLOUD)) {
							GCMRegistrar.checkDevice(context);
							GCMRegistrar.checkManifest(context);
							String senderId = getString(R.string.gcm_sender_id);
							GCMRegistrar.register(context, senderId);
							//intent.setClass(context, CodeEntry.class);
							intent.setClass(context, Login.class);
							mPrefsWrapper.setPreferenceBooleanValue(Constants.PUSH_NOTIFICATION_ENABLED, true);
						} else {
							/*if(mPrefsWrapper.getPreferenceBooleanValue(Constants.USER_IS_TEACHER) &&
									!mPrefsWrapper.getPreferenceBooleanValue(Constants.TEACHER_HAS_NAME)) {
								intent.setClass(context, CodeEntry.class);
								intent.putExtra(Constants.SHOW_TEACHER_NAME, true);
							} else {
								Bundle bundle = getIntent().getExtras();
								
								if(bundle != null) {
									int push_by = bundle.getInt(Constants.PUSH_BY);
									Log.i("KG", "push by launcher: " + push_by);
									intent.putExtra(Constants.PUSH_BY, push_by);
								}
								intent.putExtra(Constants.FINISH_REQUESTING_PROFILES, mFinishRequestingProfiles);
								intent.setClass(context, MainActivity.class);
							}*/
							Bundle bundle = getIntent().getExtras();
							
							if(bundle != null) {
								int push_by = bundle.getInt(Constants.PUSH_BY);
								Log.i("KG", "push by launcher: " + push_by);
								intent.putExtra(Constants.PUSH_BY, push_by);
							}
							intent.putExtra(Constants.FINISH_REQUESTING_PROFILES, mFinishRequestingProfiles);
							List<Kindergarden> profiles = mDataSource.getForObjects(Kindergarden.class);
							
							if(profiles.size() > 0) {
								intent.setClass(context, MainActivity.class);
							} else {
								intent.setClass(context, CodeEntry.class);
							}
						}
						
						runOnUiThread(new Runnable() {
							public void run() {
								startActivity(intent);
							}
						});
					}
				}).start();
			}
		}, 2000);
		
		Intent intent = new Intent(this, KGService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(mIKGService != null && mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID) > 0) {
			try {
				mIKGService.requestProfiles(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
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
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			mIKGService = null;
		}
		
	};
	
	private final IKGServiceCallback.Stub mIKGServiceCallback = new IKGServiceCallback.Stub() {
		@Override
		public void onFinishRequestingProfiles() throws RemoteException {
			mFinishRequestingProfiles = true;
		}
	};
}
