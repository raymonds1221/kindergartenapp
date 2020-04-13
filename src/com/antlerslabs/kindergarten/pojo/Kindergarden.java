package com.antlerslabs.kindergarten.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@ApiMethod(rootName="xml", subName="item")
@Table(name="kindergardens")
public class Kindergarden implements BaseObject {
	@Parameter(name="uid", isCharacter=true)
	@Column(name="uid", primaryKey=true)
	private int uid;
	@Parameter(name="name", isCharacter=true)
	@Column(name="name")
	private String name;
	@Parameter(name="description", isCharacter=true)
	@Column(name="description")
	private String description;
	@Parameter(name="email", isCharacter=true)
	@Column(name="email")
	private String email;
	@Parameter(name="phone_number", isCharacter=true)
	@Column(name="phone_number")
	private String phoneNumber;
	@Parameter(name="address", isCharacter=true)
	@Column(name="address")
	private String address;
	@Parameter(name="total_msg", isCharacter=true)
	@Column(name="total_msg")
	private int totalMessage;
	@Parameter(name="type", isCharacter=true)
	@Column(name="type")
	private String type;
	@Parameter(name="with_permission", isCharacter=true)
	@Column(name="with_permission")
	private int withPermission;
	@Column(name="new_msg")
	private int newMessage;
	@Column(name="profile_viewed")
	private boolean profileViewed;
	
	public Kindergarden() {}
	
	public Kindergarden(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
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
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(uid);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(email);
		dest.writeString(phoneNumber);
		dest.writeString(address);
	}

	@Override
	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		name = in.readString();
		description = in.readString();
		email = in.readString();
		phoneNumber = in.readString();
		address = in.readString();
	}
	
	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new Kindergarden(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new Kindergarden[size];
		}
	};

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getTotalMessage() {
		return totalMessage;
	}

	public void setTotalMessage(int totalMessage) {
		this.totalMessage = totalMessage;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getWithPermission() {
		return withPermission;
	}

	public void setWithPermission(int withPermission) {
		this.withPermission = withPermission;
	}

	public int getNewMessage() {
		return newMessage;
	}

	public void setNewMessage(int newMessage) {
		this.newMessage = newMessage;
	}

	public boolean isProfileViewed() {
		return profileViewed;
	}

	public void setProfileViewed(boolean profileViewed) {
		this.profileViewed = profileViewed;
	}
}
