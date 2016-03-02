package com.dehboxturtle.instaclone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenImageActivity extends AppCompatActivity {
    private int startPos;
    private ArrayList<String> images;
    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;
    private View pagerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        super.onCreate(savedInstanceState);
        Intent fullscreen = getIntent();
        startPos = fullscreen.getIntExtra("position", 0);
        images = fullscreen.getStringArrayListExtra("images");

        setContentView(R.layout.activity_fullscreen_image);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pagerContainer = findViewById(R.id.pager_container);

        mPager = (ViewPager) findViewById(R.id.fullscreen_content);
        mPagerAdapter = new ScreenSlidePageAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

 /*   @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class ScreenSlidePageAdapter extends FragmentStatePagerAdapter {


        public ScreenSlidePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            FullScreenImageContentFragment frag = new FullScreenImageContentFragment();
            Bundle bun = new Bundle();
            bun.putString("image", images.get(position));
            frag.setArguments(bun);
            return frag;
        }

        @Override
        public int getCount() {
            return images.size();
        }
    }
}
