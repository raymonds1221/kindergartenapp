package com.antlerslabs.kindergarten.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.ApiMethod;
import com.antlerslabs.kindergarten.annotation.Parameter;

@ApiMethod(rootName="xml", subName="item")
public class Profile implements BaseObject {
	@Parameter(name="uid")
	private int uid;
	@Parameter(name="name")
	private String name;
	@Parameter(name="description")
	private String description;
	@Parameter(name="email")
	private String email;
	@Parameter(name="phone_number")
	private String phoneNumber;
	@Parameter(name="address")
	private String address;
	@Parameter(name="type")
	private String type;
	
	public Profile() {}
	
	public Profile(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public int getUid() {
		return uid;
	}
	
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
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int describeContents() {
		return 0;
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
			return new Profile(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new Profile[size];
		}
	};
}
