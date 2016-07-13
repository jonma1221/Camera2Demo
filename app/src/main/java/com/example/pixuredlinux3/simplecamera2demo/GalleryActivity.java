package com.example.pixuredlinux3.simplecamera2demo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;

import Fragments.GalleryFragment;

public class GalleryActivity extends AppCompatActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        final String uri = getIntent().getStringExtra("uri");

        final ArrayList<PixuredFilter> filters = PixFilterGenerator.getAllFilters();

        mViewPager = (ViewPager) findViewById(R.id.pagerFilter);
        FragmentManager fm = getSupportFragmentManager();
        //GalleryFragment fragment = GalleryFragment.newInstance(uri);
        //fm.beginTransaction().add(R.id.galleryHolder, fragment).commit();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                String name = filters.get(position).getName();
                return GalleryFragment.newInstance(uri, name);
            }

            @Override
            public int getCount() {
                return filters.size();
            }
        });

    }

    public static Intent newIntent(Context context, String uri){
        Intent intent = new Intent(context, GalleryActivity.class);
        intent.putExtra("uri", uri);
        return intent;
    }
}
