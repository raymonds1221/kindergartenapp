package com.antlerslabs.kindergarten.pojo;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.graphics.Bitmap;

import com.antlerslabs.kindergarten.annotation.*;

@ApiMethod(rootName="xml", subName="item")
@Table(name="latestnews")
public class LatestNews implements BaseObject {
	@Parameter(name="uid")
	@Column(name="uid", primaryKey=true)
	private int uid;
	@Column(name="profile_uid")
	private int profileUid;
	@Parameter(name="postedby_uid")
	@Column(name="postedby_uid")
	private int postedByUid;
	@Parameter(name="name")
	@Column(name="name")
	private String name;
	@Parameter(name="title")
	@Column(name="title")
	private String title;
	@Parameter(name="details")
	@Column(name="details")
	private String details;
	@Parameter(name="image")
	@Column(name="image")
	private String image;
	@Parameter(name="datecreated", format="yyyy-MM-dd HH:mm:ss")
	@Column(name="datecreated")
	private Date dateCreated;
	@Parameter(name="elapse")
	@Column(name="elapse")
	private String elapsed;
	@Column(name="image_bitmap")
	private Bitmap imageBitmap;
	
	public LatestNews() {}
	
	public LatestNews(Parcel in) {
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
	
	public int getProfileUid() {
		return profileUid;
	}

	public void setProfileUid(int profileUid) {
		this.profileUid = profileUid;
	}

	public int getPostedByUid() {
		return postedByUid;
	}

	public void setPostedByUid(int postedByUid) {
		this.postedByUid = postedByUid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getElapsed() {
		return elapsed;
	}

	public void setElapsed(String elapsed) {
		this.elapsed = elapsed;
	}
	
	public Bitmap getImageBitmap() {
		return imageBitmap;
	}

	public void setImageBitmap(Bitmap imageBitmap) {
		this.imageBitmap = imageBitmap;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(uid);
		dest.writeInt(postedByUid);
		dest.writeString(name);
		dest.writeString(title);
		dest.writeString(details);
		dest.writeString(image);
		dest.writeLong(dateCreated.getTime());
		dest.writeString(elapsed);
	}

	@Override
	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		postedByUid = in.readInt();
		name = in.readString();
		title = in.readString();
		details = in.readString();
		image = in.readString();
		dateCreated = new Date(in.readLong());
		elapsed = in.readString();
	}
	
	public static final Parcelable.Creator<BaseObject> CREATOR = new Parcelable.Creator<BaseObject>() {
		@Override
		public BaseObject createFromParcel(Parcel source) {
			return new LatestNews(source);
		}

		@Override
		public BaseObject[] newArray(int size) {
			return new LatestNews[size];
		}
	};
}
