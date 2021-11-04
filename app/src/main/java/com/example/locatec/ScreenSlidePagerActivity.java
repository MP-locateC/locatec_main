package com.example.locatec;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


public class ScreenSlidePagerActivity extends FragmentActivity {
    public static Context viewPagerContext;
    private static final int NUM_PAGES = 3;
    public ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    ReportFirstPage firstPage;
    ReportSecondPage secondPage;
    Button goBack;
    FrameLayout loadingOvarlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_page);
        loadingOvarlay = (FrameLayout) findViewById(R.id.loading_overlay);
        loadingOvarlay.bringToFront();

        goBack = (Button) findViewById(R.id.goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false);
        viewPagerContext = this;
    }

    public void goNext() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }
    public void goFirst() {
        viewPager.setCurrentItem(0);
    }
    public boolean submit() {
        loadingOvarlay.setVisibility(View.VISIBLE);

        if(secondPage.isAddingImage) {
            // 이미지가 제대로 들어있는지 확인
           return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    firstPage = new ReportFirstPage();
                    return firstPage;
                case 1:
                    secondPage = new ReportSecondPage();
                    return secondPage;
            }
            return new ReportSecondPage();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}