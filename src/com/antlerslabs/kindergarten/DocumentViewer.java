package com.antlerslabs.kindergarten;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.helpers.Constants;

public class DocumentViewer extends Activity {
	private WebView document_view;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.document_viewer);
		
		document_view = (WebView) findViewById(R.id.document_view);
		
		Bundle bundle = getIntent().getExtras();
		
		if(bundle != null) {
			String document = bundle.getString(Constants.DOCUMENT_NAME);
			document_view.getSettings().setJavaScriptEnabled(true);
			document_view.getSettings().setPluginsEnabled(true);
			document_view.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView webView, String url) {
					findViewById(R.id.webview_progress).setVisibility(View.GONE);
				}
				
				@Override
				public boolean shouldOverrideUrlLoading(WebView webView, String url) {
					webView.loadUrl(url);
					return true;
				}
			});
			document_view.loadUrl("https://docs.google.com/a/antlersmedia.com/viewer?url=" + document);
		}
		
		findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
