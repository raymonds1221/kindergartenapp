package com.antlerslabs.kindergarten.adapter;

import com.antlerslabs.kindergarten.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MenuAdapter extends BaseAdapter {
	private Context mContext;
	private int mLayoutResourceId;
	private int[][] mMenus;
	private LayoutInflater mInflater;
	
	public MenuAdapter(Context context, int layoutResourceId, int[][] menus) {
		mContext = context;
		mLayoutResourceId = layoutResourceId;
		mMenus = menus;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return mMenus.length;
	}
	
	@Override
	public Object getItem(int position) {
		return mMenus[position];
	}
	
	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		
		if(view == null) {
			view = mInflater.inflate(mLayoutResourceId, null);
		}
		
		TextView menu = (TextView) view;
		menu.setCompoundDrawablesWithIntrinsicBounds(mMenus[position][0], 0, R.drawable.menu_arrow, 0);
		menu.setText(mMenus[position][1]);
		menu.setContentDescription(mContext.getString(mMenus[position][1]));
		return view;
	}
}
