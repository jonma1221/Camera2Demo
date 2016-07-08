package com.example.pixuredlinux3.simplecamera2demo.view;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import com.example.pixuredlinux3.simplecamera2demo.CameraRenderer;

/**
 * Created by pixuredlinux3 on 7/8/16.
 */
public class CameraGLSurfaceView extends GLSurfaceView {
    CameraRenderer mRenderer;
    Camera mCamera;

    public CameraGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        mRenderer = new CameraRenderer(context);
        setRenderer(mRenderer);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    queueEvent(new Runnable(){
                        public void run() {
                            mRenderer.setPosition(event.getX() / getWidth(),
                                    event.getY() / getHeight());
                        }});
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        mCamera.stopPreview();
        mCamera.release();
    }

    @Override
    public void onResume() {
        mCamera = Camera.open();
        Camera.Parameters p = mCamera.getParameters();

        mCamera.setParameters(p);

        queueEvent(new Runnable(){
            public void run() {
                mRenderer.setCamera(mCamera);
            }});

        super.onResume();
    }
}
