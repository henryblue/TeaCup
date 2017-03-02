package com.app.teacup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.app.ui.ThemeItemView;
import com.app.util.ToolUtils;

import java.util.ArrayList;
import java.util.List;


public class ThemeChangeActivity extends AppCompatActivity {

    private LinearLayout mLayout;
    private String[] mThemeArray;
    private int oldPos;
    private final List<ThemeItemView> themeItemViews = new ArrayList<>();

    private final int[] mColors = {R.color.colorPrimary, R.color.colorGreen, R.color.colorPink,
            R.color.colorBlack, R.color.colorGray, R.color.colorTeal, R.color.colorRed, R.color.colorPurple};

    private SharedPreferences mSps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSps = getSharedPreferences("config", MODE_PRIVATE);
        oldPos = mSps.getInt("themePos", 0);
        ToolUtils.onActivityCreateSetTheme(ThemeChangeActivity.this);
        setContentView(R.layout.activity_change_theme);
        mLayout = (LinearLayout) findViewById(R.id.ll_theme);
        mThemeArray = getResources().getStringArray(R.array.theme_style);
        initToolBar();
        loadLayout();

    }

    private void loadLayout() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        final SharedPreferences.Editor edit = mSps.edit();
        for (int i = 0; i < mThemeArray.length; i++) {
            final ThemeItemView themeView = new ThemeItemView(this);
            themeView.setLayoutParams(lp);
            themeView.setTitle(mThemeArray[i]);
            themeView.setTitleColor(ContextCompat.getColor(ThemeChangeActivity.this, mColors[i]));
            themeView.setImageBackground(mColors[i]);
            final int finalI = i;
            themeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (finalI != oldPos) {
                        themeItemViews.get(oldPos).setChecked(false);
                        themeView.setChecked(!themeView.isChecked());
                        if (themeView.isChecked()) {
                            edit.putInt("themePos", finalI);
                            edit.apply();
                            ToolUtils.changeToTheme(ThemeChangeActivity.this, true);
                        }
                    }
                    oldPos = finalI;
                }
            });
            if (i == mSps.getInt("themePos", 0)) {
                themeView.setChecked(true);
            }
            themeItemViews.add(themeView);
            mLayout.addView(themeView);
        }
    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.set_theme));
        }
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
