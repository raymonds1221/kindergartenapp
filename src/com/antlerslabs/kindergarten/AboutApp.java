package com.antlerslabs.kindergarten;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import com.antlerslabs.kindergarten.R;

public class AboutApp extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_app);
		
		TextView about_app_details = (TextView) findViewById(R.id.about_app_details);
		about_app_details.setMovementMethod(new ScrollingMovementMethod());
		
		findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
