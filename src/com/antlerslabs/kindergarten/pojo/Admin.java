package com.antlerslabs.kindergarten.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@ApiMethod(rootName="xml", subName="item")
@Table(name="admins")
public class Admin implements BaseObject {
	@Column(name="uid", primaryKey=true, autoIncrement=true)
	private int uid;
	@Column(name="profile_uid")
	private int profileUid;
	@Parameter(name="name")
	@Column(name="name")
	private String name;
	@Parameter(name="phone_number")
	@Column(name="phone_number")
	private String phoneNumber;
	@Parameter(name="email")
	@Column(name="email")
	private String email;
	@Parameter(name="role")
	@Column(name="role")
	private String role;
	
	public Admin() {}
	
	public Admin(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}

	@Override
	public void readFromParcel(Parcel in) {
		
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new Admin(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new Admin[size];
		}
	};
}
