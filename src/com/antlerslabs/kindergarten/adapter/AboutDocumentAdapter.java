package com.antlerslabs.kindergarten.adapter;

import java.util.List;

import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageButton;

import com.antlerslabs.kindergarten.DocumentViewer;
import com.antlerslabs.kindergarten.R;
import com.antlerslabs.kindergarten.helpers.Constants;
import com.antlerslabs.kindergarten.pojo.AboutDocument;

public class AboutDocumentAdapter extends ArrayAdapter<AboutDocument> {
	private Context mContext;
	private int mLayoutResourceId;
	private List<AboutDocument> mAboutDocuments;
	private LayoutInflater mLayoutInflater;

	public AboutDocumentAdapter(Context context, int layoutResourceId, List<AboutDocument> aboutDocuments) {
		super(context, layoutResourceId, aboutDocuments);
		
		mContext = context;
		mLayoutResourceId = layoutResourceId;
		mAboutDocuments = aboutDocuments;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder;
		final AboutDocument aboutDocument = mAboutDocuments.get(position);
		
		if(view == null) {
			view = mLayoutInflater.inflate(mLayoutResourceId, null);
			viewHolder = new ViewHolder();
			viewHolder.document_title = (TextView) view.findViewById(R.id.document_title);
			viewHolder.view_btn = (ImageButton) view.findViewById(R.id.view_btn);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		viewHolder.document_title.setText(aboutDocument.getTitle().toUpperCase());
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, DocumentViewer.class);
				intent.putExtra(Constants.DOCUMENT_NAME, aboutDocument.getFilename());
				mContext.startActivity(intent);
			}
		});
		
		return view;
	}
	
	private class ViewHolder {
		public TextView document_title;
		@SuppressWarnings("unused")
		public ImageButton view_btn;
	}
}
