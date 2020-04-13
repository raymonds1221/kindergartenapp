package com.antlerslabs.kindergarten.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.antlerslabs.kindergarten.annotation.*;

@Table(name="calendar_syncs")
public class CalendarSync implements BaseObject {
	@Column(name="uid", primaryKey=true, autoIncrement=true, allowChange=false)
	private int uid;
	@Column(name="event_id")
	private int eventId;
	@Column(name="calendar_detail_id")
	private int calendarDetailId;
	
	public CalendarSync() {}
	
	public CalendarSync(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(uid);
		dest.writeInt(eventId);
		dest.writeInt(calendarDetailId);
	}

	@Override
	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		eventId = in.readInt();
		calendarDetailId = in.readInt();
	}
	
	@Override
	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public int getUid() {
		return uid;
	}
	
	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getCalendarDetailId() {
		return calendarDetailId;
	}

	public void setCalendarDetailId(int calendarDetailId) {
		this.calendarDetailId = calendarDetailId;
	}


	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new CalendarSync(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new CalendarSync[size];
		}
	};
}
