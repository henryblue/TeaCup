package com.app.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.teacup.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class ThemeItemView extends RelativeLayout {

	private CheckBox mCbStatus;
	private TextView mTitle;
    private CircleImageView mImgView;

	public ThemeItemView(Context context) {
		super(context);
		initView(context);
	}

	public ThemeItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		setAttributeSet(context, attrs);
	}

	/**
	 * 初始化布局文件
	 * 
	 * @param context
	 */
	private void initView(Context context) {
		View.inflate(context, R.layout.theme_item_view, this);
        mCbStatus = (CheckBox) findViewById(R.id.cb_is_update);
        mTitle = (TextView) findViewById(R.id.tv_theme_title);
		mImgView = (CircleImageView) findViewById(R.id.tv_theme_img);
	}

	private void setAttributeSet(Context context, AttributeSet attrs) {
		TypedArray typeArray = context.obtainStyledAttributes(attrs,
				R.styleable.ThemeItemView);
		final String title = typeArray
				.getString(R.styleable.ThemeItemView_theme_title);
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
		final int resourceId = typeArray
				.getResourceId(R.styleable.ThemeItemView_theme_background, R.color.colorPrimary);
		setImageBackground(resourceId);
		final boolean isHideCb = typeArray
                .getBoolean(R.styleable.ThemeItemView_theme_checkbox, false);
        if (isHideCb) {
            mCbStatus.setVisibility(View.INVISIBLE);
        }

		typeArray.recycle();
	}

	/**
	 * 设置标题
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		mTitle.setText(title);
	}

	public void setTitleColor(int color) {
		mTitle.setTextColor(color);
	}

	public void setImageBackground(int resourceId) {
		mImgView.setImageResource(resourceId);
	}
	/**
	 * 判断checkbox是否获得焦点
	 */
	public boolean isChecked() {
		return mCbStatus.isChecked();
	}

	/**
	 * 设置checkbox选中状态
	 */
	public void setChecked(boolean checked) {
		mCbStatus.setChecked(checked);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			return true;
		default:
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

}
