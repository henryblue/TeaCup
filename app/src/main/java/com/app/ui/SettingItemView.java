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


public class SettingItemView extends RelativeLayout {

	private CheckBox mCbStatus;
	private TextView mTitle;
    private TextView mContent;

	public SettingItemView(Context context) {
		super(context);
		initView(context);
	}

	public SettingItemView(Context context, AttributeSet attrs) {
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
		View.inflate(context, R.layout.setting_item_view, this);
        mCbStatus = (CheckBox) this.findViewById(R.id.cb_is_update);
        mTitle = (TextView) this.findViewById(R.id.tv_setting_title);
        mContent = (TextView) this.findViewById(R.id.tv_setting_content);
	}

	private void setAttributeSet(Context context, AttributeSet attrs) {
		TypedArray typeArray = context.obtainStyledAttributes(attrs,
				R.styleable.SettingItemView);
		final String title = typeArray
				.getString(R.styleable.SettingItemView_setting_title);
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}

        final String content = typeArray
                .getString(R.styleable.SettingItemView_setting_content);
        if (!TextUtils.isEmpty(title)) {
            setContent(content);
        } else {
			mContent.setVisibility(View.GONE);
		}

        final boolean isHideCb = typeArray
                .getBoolean(R.styleable.SettingItemView_hide_checkbox, false);
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

	/**
	 * 设置说明内容
	 * @param content
     */
	public void setContent(String content) {
        mContent.setText(content);
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
