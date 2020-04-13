package com.antlerslabs.kindergarten.pojo;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@ApiMethod(rootName="xml", subName="item")
@Table(name="messages")
public class Message implements BaseObject {
	@Column(name="uid", primaryKey=true, autoIncrement=true, allowChange=false)
	private int uid;
	@Column(name="profile_uid")
	private int profileUid;
	@Parameter(name="message")
	@Column(name="message")
	private String message;
	@Parameter(name="user")
	@Column(name="user")
	private String user;
	@Parameter(name="datecreated", format="yyyy-MM-dd HH:mm:ss")
	@Column(name="datecreated")
	private Date dateCreated;
	
	public Message() {}
	
	public Message(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(uid);
		dest.writeString(message);
		dest.writeLong(dateCreated.getTime());
	}
	
	@Override
	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		message = in.readString();
		dateCreated = new Date(in.readLong());
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new Message(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new Message[size];
		}
	};

}
