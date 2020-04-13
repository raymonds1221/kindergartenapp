package com.antlerslabs.kindergarten.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceWrapper {
	private static PreferenceWrapper mPrefsWrapper;
	private SharedPreferences mSharedPrefs;
	
	private PreferenceWrapper(Context context) {
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static final PreferenceWrapper getInstance(Context context) {
		if(mPrefsWrapper == null)
			mPrefsWrapper = new PreferenceWrapper(context);
		return mPrefsWrapper;
	}
	
	public void setPreferenceStringValue(String key, String value) {
		SharedPreferences.Editor editor = mSharedPrefs.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public String getPreferenceStringValue(String key) {
		return mSharedPrefs.getString(key, null);
	}
	
	public void setPreferenceBooleanValue(String key, boolean value) {
		SharedPreferences.Editor editor = mSharedPrefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public boolean getPreferenceBooleanValue(String key) {
		return mSharedPrefs.getBoolean(key, false);
	}
	
	public void setPreferenceIntValue(String key, int value) {
		SharedPreferences.Editor editor = mSharedPrefs.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public int getPreferenceIntValue(String key) {
		return mSharedPrefs.getInt(key, 0);
	}
	
	public void setPreferenceLongValue(String key, long value) {
		SharedPreferences.Editor editor = mSharedPrefs.edit();
		editor.putLong(key, value);
		editor.commit();
	}
	
	public long getPreferenceLongValue(String key) {
		return mSharedPrefs.getLong(key, 0);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
