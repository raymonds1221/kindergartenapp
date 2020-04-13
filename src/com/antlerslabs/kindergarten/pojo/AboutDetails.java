package com.antlerslabs.kindergarten.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@ApiMethod(rootName="xml", subName="about", hasChildNodes=true)
@Table(name="about_details")
public class AboutDetails implements BaseObject {
	@Column(name="uid", primaryKey=true, autoIncrement=true)
	private int uid;
	@Column(name="profile_uid")
	private int profileUid;
	@Parameter(name="details")
	@Column(name="details")
	private String details;
	
	public AboutDetails() {
		
	}
	
	public AboutDetails(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(details);
	}

	@Override
	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public int getUid() {
		return uid;
	}
	
	public int getProfileUid() {
		return profileUid;
	}

	public void setProfileUid(int profileUid) {
		this.profileUid = profileUid;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	@Override
	public void readFromParcel(Parcel in) {
		details = in.readString();
	}
	
	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new AboutDetails(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new AboutDetails[size];
		}
	};
}
