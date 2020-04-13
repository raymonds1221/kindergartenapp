package com.antlerslabs.kindergarten.pojo;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@ApiMethod(rootName="xml", subName="item")
@Table(name="calendar_details")
public class CalendarDetail implements BaseObject {
	@Parameter(name="uid")
	@Column(name="uid", primaryKey=true)
	private int uid;
	@Column(name="profile_uid")
	private int profileUid;
	@Parameter(name="title")
	@Column(name="title")
	private String title;
	@Parameter(name="description")
	@Column(name="description")
	private String description;
	@Parameter(name="event_date", format="yyyy-MM-dd")
	@Column(name="datecreated")
	private Date dateCreated;
	@Column(name="month")
	private int month;
	@Column(name="year")
	private int year;
	
	public CalendarDetail() {}
	
	public CalendarDetail(Parcel in) {
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
		dest.writeLong(dateCreated.getTime());
	}

	@Override
	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		title = in.readString();
		description = in.readString();
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

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}


	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new CalendarDetail(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new CalendarDetail[size];
		}
	};
}
