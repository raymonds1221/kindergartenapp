package com.antlerslabs.kindergarten.helpers;

import android.graphics.Bitmap;

public class Constants {
	public static final int REQUEST_CODE_QR_CODE = 1;
	public static final int REQUEST_CODE_NEW_POST = 2;
	public static final int REQUEST_CODE_EDIT_POST = 3;
	public static final int REQUEST_CODE_CAMERA = 4;
	public static final int REQUEST_CODE_GALLERY = 5;
	public static final int REQUEST_CODE_ADD_KINDERGARDEN = 6;
	public static final int REQUEST_CODE_ADD_CODE = 7;
	public static final int REQUEST_CODE_ADMIN_APPROVAL = 10;
	public static final int REQUEST_CODE_REGISTRATION = 12;
	
	public static final int RESULT_ADMIN_APPROVAL = 11;
	public static final int RESULT_REGISTRATION = 13;
	
	public static final int PUSH_BY_MESSAGEBOARD = 8;
	public static final int PUSH_BY_CALENDAR = 9;
	public static final String PUSH_BY = "push_by";
	
	public static final String USER_ID = "user_id";
	public static final String USER_IS_REGISTERED = "user_is_registered";
	public static final String USER_IS_TEACHER = "user_is_teacher";
	public static final String TEACHER_HAS_NAME = "teacher_has_name";
	public static final String SHOW_TEACHER_NAME = "show_teacher_name";
	public static final String DEFAULT_KG_ID = "default_kg_id";
	public static final String TEACHER_HAS_PERMISSION = "teacher_has_permission";
	public static final String REGISTERED_TO_CLOUD = "registered_to_cloud";
	public static final String CLOUD_REGISTRATION_ID = "cloud_registration_id";
	public static final String LAST_ENTERED_CODE = "last_entered_code";
	public static final String DEFAULT_CALENDAR_ID = "calendar_id";
	public static final String ABOUT_DETAILS_LAST_UPDATED = "about_details_last_updated";
	public static final String LATEST_NEWS_IMAGE = "latest_news_image";
	public static final String LATEST_NEWS_COUNT = "latest_news_count";
	public static final String FINISH_REQUESTING_PROFILES = "finish_requesting_profiles";
	public static final String NEED_REQUEST_DOCUMENTS = "need_request_documents";
	
	public static final String HAS_REQUESTED_PROFILES = "has_requested_profiles";
	public static final String LATEST_NEWS_ACTION = "latest_news_action";
	public static final String DOCUMENT_NAME = "document_name";
	public static final int LATEST_NEWS_NEW = 100;
	public static final int LATEST_NEWS_EDIT = 101;
	public static final String ACTIVE_LATEST_NEWS = "active_latest_news";
	public static final String CALENDAR_DETAIL = "calendar_detail";
	public static final String DEFAULT_PROFILE = "default_profile";
	
	public static final String PUSH_NOTIFICATION_ENABLED = "push_notification_enabled";
	public static final String SYNC_CALENDAR_ENABLED = "sync_calendar_enabled";
	
	public static final Object mLockObject = new Object();
	public static Bitmap mLatestNewsImage = null;
}