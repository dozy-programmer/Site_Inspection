package com.akapps.sitesurvey.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import com.akapps.sitesurvey.Classes.Helper;
import com.akapps.sitesurvey.Fragment.Barcode_Scanner;
import com.akapps.sitesurvey.Fragment.Panel_Placement_Fragment;
import com.akapps.sitesurvey.Fragment.Project_Photos;
import com.akapps.sitesurvey.R;
import com.akapps.sitesurvey.Fragment.Project_Main_Fragment;
import com.tompee.funtablayout.FunTabLayout;
import com.tompee.funtablayout.PopTabAdapter;

public class View_Project extends AppCompatActivity implements PopTabAdapter.IconFetcher{

    // project data
    private int project_Position;
    private String fragmentTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieve position of project passed by previous activity (Home_Page)
        Intent current_Project_Position = getIntent();
        project_Position = current_Project_Position.getIntExtra("project_Position", -1);

        // layout is initialized
        initializeLayout();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    // when activity is closed, all cache for current project is deleted
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Helper.trimCache(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeLayout(){
        setContentView(R.layout.view_project);

        // blends tab-layout with actionbar
        getSupportActionBar().setElevation(0);

        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        FunTabLayout tabLayout = findViewById(R.id.tablayout);
        PopTabAdapter.Builder builder = new PopTabAdapter.Builder(this).
            setViewPager(viewPager).
            setTabPadding(24, 24, 24, 24).
            setTabTextAppearance(R.style.PopTabText).
            setTabBackgroundResId(R.drawable.ripple).
            setTabIndicatorColor(Color.BLACK).
            setIconFetcher(this).
            setIconDimension(70).
            setDefaultIconColor(Color.WHITE).
            setPopDuration(300);
        tabLayout.setUpWithAdapter(builder.build());
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Fragment currentFragment= null;
                switch (position) {
                    case 0:
                        currentFragment = Project_Main_Fragment.newInstance(project_Position);
                        break;
                    case 1:
                        currentFragment =  Project_Photos.newInstance(project_Position);
                        break;
                    case 2:
                        currentFragment =  Barcode_Scanner.newInstance(project_Position);
                        break;
                    case 3:
                        currentFragment =  Panel_Placement_Fragment.newInstance(project_Position);
                        break;
                }
                return currentFragment;
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        fragmentTitle = getString(R.string.one);
                        break;
                    case 1:
                        fragmentTitle = getString(R.string.two);
                        break;
                    case 2:
                        fragmentTitle = getString(R.string.three);
                        break;
                    case 3:
                        fragmentTitle = getString(R.string.four);
                        break;
                }
                return fragmentTitle;
            }
        });
    }

    @Override
    public int getIcon(int position) {
        int resource = R.mipmap.site_survey_icon_round;
        switch (position) {
            case 0:
                resource = R.drawable.icon_home_black_24;
                break;
            case 1:
                resource = R.drawable.icon_photo_camera_black_24;
                break;
            case 2:
                resource = R.drawable.icon_scan_icon;
                break;
            case 3:
                resource = R.drawable.ic_solar_frag_icon;
                break;
        }
        return resource;
    }
}
