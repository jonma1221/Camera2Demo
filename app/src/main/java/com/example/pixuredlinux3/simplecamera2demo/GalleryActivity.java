package com.example.pixuredlinux3.simplecamera2demo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import Fragments.GalleryFragment;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        String uri = getIntent().getStringExtra("uri");
        FragmentManager fm = getSupportFragmentManager();
        GalleryFragment fragment = GalleryFragment.newInstance(uri);
        fm.beginTransaction().add(R.id.galleryHolder, fragment).commit();
    }

    public static Intent newIntent(Context context, String uri){
        Intent intent = new Intent(context, GalleryActivity.class);
        intent.putExtra("uri", uri);
        return intent;
    }
}
