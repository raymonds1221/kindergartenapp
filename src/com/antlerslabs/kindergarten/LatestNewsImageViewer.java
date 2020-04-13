package com.antlerslabs.kindergarten;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.antlerslabs.kindergarten.helpers.Constants;

public class LatestNewsImageViewer extends Activity {
	private ImageView latest_news_image;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.latest_news_imageviewer);
		
		latest_news_image = (ImageView) findViewById(R.id.latest_news_image);
		/*Bundle bundle = getIntent().getExtras();
		
		if(bundle != null) {
			Bitmap bitmap = (Bitmap) bundle.getParcelable(Constants.LATEST_NEWS_IMAGE);
			latest_news_image.setImageBitmap(bitmap);
		}*/
		
		if(Constants.mLatestNewsImage != null) {
			latest_news_image.setImageBitmap(Constants.mLatestNewsImage);
		}
		
		findViewById(R.id.abort_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
