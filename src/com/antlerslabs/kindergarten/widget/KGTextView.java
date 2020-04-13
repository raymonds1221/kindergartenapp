package com.antlerslabs.kindergarten.widget;

import com.antlerslabs.kindergarten.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;
import android.graphics.Typeface;

public class KGTextView extends TextView {

	public KGTextView(Context context) {
		super(context);
		changeFont(context, null);
	}
	
	public KGTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		changeFont(context, attrs);
	}
	
	public KGTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		changeFont(context, attrs);
	}
	
	private void changeFont(Context context, AttributeSet attrs) {
		Typeface typeface = null;
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FontType);
		int font = typedArray.getInt(R.styleable.FontType_font, 0);
		
		switch(font) {
			case 0:
				typeface = Typeface.createFromAsset(context.getAssets(), "Knockout-HTF28-JuniorFeatherwt.otf");
				break;
			case 1:
				typeface = Typeface.createFromAsset(context.getAssets(), "Knockout-HTF30-JuniorWelterwt.otf");
				break;
		}
		setTypeface(typeface);
	}
}
