package com.antlerslabs.kindergarten;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.EditText;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.database.Cursor;
import android.net.Uri;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.helpers.*;
import com.antlerslabs.kindergarten.net.*;
import com.antlerslabs.kindergarten.parser.*;
import com.antlerslabs.kindergarten.pojo.LatestNews;

public class NewPost extends Activity {
	private int mLatestNewsAction = Constants.LATEST_NEWS_NEW;
	private ImageButton latest_news_image;
	private EditText latest_news_title;
	private EditText latest_news_details;
	private final String CAPTURED_IMAGE = "captured_image";
	private final String CAPTURED_FILE = "captured_file";
	private final String CAPTURED_URI = "captured_uri";
	private final String CAPTURED_IMAGE_SOURCE = "captured_image_source";
	private final int IMAGE_SOURCE_CAMERA = 1;
	private final int IMAGE_SOURCE_GALLERY = 2;
	private int IMAGE_SOURCE = 0;
	private Bitmap mImageBitmap;
	private PreferenceWrapper mPrefsWrapper;
	private Network mNetwork;
	private WebServiceRequest mWebServiceRequest;
	private Parser mParser;
	private LatestNews mLatestNews;
	private File mImageFile;
	private Uri mImageUri;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newpost);
		
		latest_news_image = (ImageButton) findViewById(R.id.latest_news_image);
		latest_news_title = (EditText) findViewById(R.id.latest_news_title);
		latest_news_details = (EditText) findViewById(R.id.latest_news_details);
		
		mPrefsWrapper = PreferenceWrapper.getInstance(this);
		mNetwork = Network.getInstance(this);
		mWebServiceRequest = WebServiceRequest.newInstance();
		
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null) {
			mLatestNewsAction = bundle.getInt(Constants.LATEST_NEWS_ACTION);
			
			if(mLatestNewsAction == Constants.LATEST_NEWS_EDIT) {
				mLatestNews = (LatestNews) bundle.getParcelable(Constants.ACTIVE_LATEST_NEWS);
				
				new Thread(new Runnable() {
					public void run() {
						if(mNetwork.isAvailable()) {
							mImageBitmap = BitmapFactory.decodeStream(mNetwork.downloadOnlineContent(mLatestNews.getImage()));
							IMAGE_SOURCE = IMAGE_SOURCE_CAMERA;
							
							runOnUiThread(new Runnable() {
								public void run() {
									latest_news_image.setImageBitmap(mImageBitmap);
								}
							});
						}
					}
				}).start();
				
				latest_news_title.setText(mLatestNews.getTitle());
				latest_news_details.setText(mLatestNews.getDetails());
			}
		}
		
		findViewById(R.id.abort_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		findViewById(R.id.publish_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(latest_news_title.getText().toString().equals("")) {
					Message.make(NewPost.this, R.string.empty_string, R.string.enter_title).toast();
					return;
				}
				if(latest_news_details.getText().toString().equals("")) {
					AlertDialog alert = new AlertDialog.Builder(NewPost.this)
														.setMessage(R.string.enter_details)
														.setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																dialog.dismiss();
																postNewBlog();
															}
														})
														.setNegativeButton(R.string.no_btn, new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																dialog.dismiss();
															}
														})
														.create();
					alert.show();
				} else {
					postNewBlog();
				}
			}
		});
	}
	
	private void postNewBlog() {
		final ProgressDialog progressDialog = new ProgressDialog(NewPost.this);
		progressDialog.setMessage("Please wait...");
		progressDialog.setCancelable(false);
		
		switch(mLatestNewsAction) {
			case Constants.LATEST_NEWS_NEW:
				progressDialog.show();
				
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
																.setApiName("blog")
																.addParameter("add")
																.putPostValues("postedby_uid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.USER_ID)))
																.putPostValues("profile_uid", String.valueOf(mPrefsWrapper.getPreferenceIntValue(Constants.DEFAULT_KG_ID)))
																.putPostValues("title", latest_news_title.getText().toString())
																.putPostValues("details", latest_news_details.getText().toString())
																.putPostValues("image", mImageBitmap)
																.setResultFormat(WebServiceRequest.DataFormat.XML)
																.setRequestMethod(WebServiceRequest.POST)
																.getRawResponse();
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							
							runOnUiThread(new Runnable() {
								public void run() {
									if(mParser.getSpecificValue("status") != null && mParser.getSpecificValue("status").toString().equals("success")) {
										progressDialog.dismiss();
										setResult(RESULT_OK);
										finish();
									}
								}
							});
						}
					}
				}).start();
				break;
			case Constants.LATEST_NEWS_EDIT:
				progressDialog.show();
				
				new Thread(new Runnable() {
					public void run() {
						synchronized(Constants.mLockObject) {
							String result = mWebServiceRequest.setApiUri(getString(R.string.api_url))
									.setApiName("blog")
									.addParameter("update")
									.putPostValues("blogid", String.valueOf(mLatestNews.getUid()))
									.putPostValues("title", latest_news_title.getText().toString())
									.putPostValues("details", latest_news_details.getText().toString())
									.putPostValues("image", mImageBitmap)
									.setResultFormat(WebServiceRequest.DataFormat.XML)
									.setRequestMethod(WebServiceRequest.POST)
									.getRawResponse();
							mParser = ParserFactory.buildParser(result, WebServiceRequest.DataFormat.XML);
							
							runOnUiThread(new Runnable() {
								public void run() {
									if(mParser.getSpecificValue("status") != null && mParser.getSpecificValue("status").toString().equals("success")) {
										progressDialog.dismiss();
										setResult(RESULT_OK);
										finish();
									}
								}
							});
						}
					}
				}).start();
				break;
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(CAPTURED_IMAGE_SOURCE, IMAGE_SOURCE);
		outState.putParcelable(CAPTURED_IMAGE, mImageBitmap);
		outState.putSerializable(CAPTURED_FILE, mImageFile);
		outState.putParcelable(CAPTURED_URI, mImageUri);
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		IMAGE_SOURCE = savedInstanceState.getInt(CAPTURED_IMAGE_SOURCE);
		mImageBitmap = (Bitmap) savedInstanceState.getParcelable(CAPTURED_IMAGE);
		mImageFile = (File) savedInstanceState.getSerializable(CAPTURED_FILE);
		mImageUri = (Uri) savedInstanceState.getParcelable(CAPTURED_URI);
	}
	
	public void onChooseImage(View view) {
		AlertDialog alert = new AlertDialog.Builder(this)
											.setCancelable(false)
											.setItems(R.array.latest_news_menu, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													Intent intent = new Intent();
													switch(which) {
														case 0:
															intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
															mImageFile = new File(Environment.getExternalStorageDirectory(), "kindergarden");
															if(!mImageFile.exists())
																mImageFile.mkdir();
															
															ContentValues values = new ContentValues();
															values.put(MediaStore.Images.Media.BUCKET_ID, mImageFile.toString().hashCode());
															values.put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, mImageFile.getName());
															values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
															mImageFile = new File(mImageFile, UUID.randomUUID() + ".jpg");
															values.put(MediaStore.Images.Media.DATA, mImageFile.toString());
															mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
															intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
															startActivityForResult(intent, Constants.REQUEST_CODE_CAMERA);
															break;
														case 1:
															intent.setType("image/*");
															intent.setAction(Intent.ACTION_GET_CONTENT);
															startActivityForResult(intent, Constants.REQUEST_CODE_GALLERY);
															break;
													}
												}
											})
											.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													dialog.dismiss();
												}
											})
											.create();
		alert.show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Constants.REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
			IMAGE_SOURCE = IMAGE_SOURCE_CAMERA;
			
			if(mImageUri == null) {
				mImageUri = Uri.fromFile(mImageFile);
			}
			
			try {
				mImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
				Matrix matrix = new Matrix();
				
				int width = mImageBitmap.getWidth();
				int height = mImageBitmap.getHeight();
				float scaleWidth = (width / 4.0f) / width;
				float scaleHeight = (height / 4.0f) / height;
				
				matrix.postScale(scaleWidth, scaleHeight);
				mImageBitmap = Bitmap.createBitmap(mImageBitmap, 0, 0, width, height, matrix, false);
				latest_news_image.setImageBitmap(mImageBitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(requestCode == Constants.REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
			IMAGE_SOURCE = IMAGE_SOURCE_GALLERY;
			Cursor cursor = managedQuery(data.getData(), new String[] {MediaStore.Images.Media.DATA}, null, null, null);
			
			if(cursor.moveToNext()) {
				String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				Matrix matrix = new Matrix();
				Bitmap bitmap = BitmapFactory.decodeFile(path);
				
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				
				float scaleWidth = (width / 4.0f) / width;
				float scaleHeight = (height / 4.0f) / height;
				
				matrix.postScale(scaleWidth, scaleHeight);
				mImageBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
				latest_news_image.setImageBitmap(mImageBitmap);
			}
		}
	}
}
