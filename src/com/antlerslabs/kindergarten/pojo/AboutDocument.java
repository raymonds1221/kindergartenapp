package com.antlerslabs.kindergarten.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@ApiMethod(rootName="xml", subName="doc", hasChildNodes=true)
@Table(name="about_documents")
public class AboutDocument implements BaseObject {
	@Column(name="uid")
	private int uid;
	@Column(name="profile_uid")
	private int profileUid;
	@Parameter(name="doc_title")
	@Column(name="title")
	private String title;
	@Parameter(name="description")
	@Column(name="description")
	private String description;
	@Parameter(name="filename")
	@Column(name="filename")
	private String filename;
	
	public AboutDocument() {}
	
	public AboutDocument(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(uid);
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(filename);
	}
	
	@Override
	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		title = in.readString();
		description = in.readString();
		filename = in.readString();
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new AboutDocument(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new AboutDocument[size];
		}
	};
}
