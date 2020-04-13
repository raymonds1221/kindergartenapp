package com.antlerslabs.kindergarten.pojo;

import android.os.Parcelable;
import android.os.Parcel;

public interface BaseObject extends Parcelable {
	public void setUid(int uid);
	public int getUid();
	public void readFromParcel(Parcel in);
}
