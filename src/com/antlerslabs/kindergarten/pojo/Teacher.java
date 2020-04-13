package com.antlerslabs.kindergarten.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@ApiMethod(rootName="xml", subName="item")
@Table(name="teachers")
public class Teacher implements BaseObject {
	@Parameter(name="uid")
	@Column(name="uid", primaryKey=true)
	private int uid;
	@Column(name="profile_uid")
	private int profileUid;
	@Parameter(name="firstname")
	@Column(name="firstname")
	private String firstname;
	@Parameter(name="lastname")
	@Column(name="lastname")
	private String lastname;
	@Parameter(name="email")
	@Column(name="email")
	private String email;
	@Parameter(name="phone_number")
	@Column(name="phone_number")
	private String phoneNumber;
	
	public Teacher() {}
	
	public Teacher(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(uid);
		dest.writeString(firstname);
		dest.writeString(lastname);
		dest.writeString(email);
		dest.writeString(phoneNumber);
	}

	@Override
	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		firstname = in.readString();
		lastname = in.readString();
		email = in.readString();
		phoneNumber = in.readString();
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

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new Teacher(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new Teacher[size];
		}
	};
}
