package com.antlerslabs.kindergarten.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@Table(name="codes")
public class Code implements BaseObject {
	@Column(name="uid", primaryKey=true, autoIncrement=true)
	private int uid;
	@Column(name="kgid")
	private int kgId;
	@Column(name="value")
	private String value;
	
	public Code() {}
	
	public Code(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(uid);
		dest.writeString(value);
	}

	@Override
	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public int getUid() {
		return uid;
	}

	@Override
	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		value = in.readString();
	}

	public int getKgId() {
		return kgId;
	}

	public void setKgId(int kgId) {
		this.kgId = kgId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new Code(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new Code[size];
		}
	};
}
