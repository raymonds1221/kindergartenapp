package com.antlerslabs.kindergarten.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@ApiMethod(rootName="xml", subName="item")
@Table(name="calendar_document")
public class CalendarDocument implements BaseObject {
	@Parameter(name="uid")
	@Column(name="uid", primaryKey=true)
	private int uid;
	@Parameter(name="calendar_uid")
	@Column(name="calendar_uid")
	private int calendarUid;
	@Parameter(name="doc_title")
	@Column(name="title")
	private String title;
	@Parameter(name="doc_desc")
	@Column(name="description")
	private String description;
	@Parameter(name="filename")
	@Column(name="filename")
	private String filename;
	
	public CalendarDocument() {}
	
	public CalendarDocument(Parcel in) {
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

	public int getCalendarUid() {
		return calendarUid;
	}

	public void setCalendarUid(int calendarUid) {
		this.calendarUid = calendarUid;
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
			return new CalendarDocument(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new CalendarDocument[size];
		}
	};
}
