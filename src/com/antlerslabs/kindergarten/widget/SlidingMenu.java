package com.antlerslabs.kindergarten.widget;

import com.antlerslabs.kindergarten.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.Display;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class SlidingMenu extends HorizontalScrollView {
	private View mMenu;
	private View mMenuHandle;
	private View mContent;
	private Context mContext;
	private int mStartPosition;
	private boolean mMenuShowing;

	public SlidingMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		setHorizontalFadingEdgeEnabled(false);
		setVerticalFadingEdgeEnabled(false);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return false;
	}
	
	@Override
	public void onFinishInflate() {
		mMenu = findViewById(R.id.sliding_menu);
		mMenuHandle = findViewById(R.id.sliding_menu_handle);
		mContent = findViewById(R.id.sliding_menu_content);
		
		if(mMenu == null)
			throw new RuntimeException("Please specify menu with id 'sliding_menu'");
		if(mMenuHandle == null)
			throw new RuntimeException("Please specify menu handler with id 'sliding_menu_handle'");
		if(mContent == null)
			throw new RuntimeException("Please specify content with id 'sliding_menu_content'");
		
		WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		
		mStartPosition = mMenu.getLayoutParams().width;
		mContent.setLayoutParams(new LinearLayout.LayoutParams(display.getWidth(), display.getHeight()));
		
		mMenuHandle.setOnClickListener(new MenuHandlerListener());
	}
	
	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(!mMenuShowing)
			hideMenu();
	}
	
	public void showMenu() {
		smoothScrollTo(0, 0);
		mMenuShowing = true;
	}
	
	public void hideMenu() {
		smoothScrollTo(mStartPosition, 0);
		mMenuShowing = false;
	}
	
	public boolean isMenuShowing() {
		return mMenuShowing;
	}
	
	private class MenuHandlerListener implements View.OnClickListener {
		public void onClick(View view) {
			if(mMenuShowing)
				hideMenu();
			else
				showMenu();
		}
	}
	
}
