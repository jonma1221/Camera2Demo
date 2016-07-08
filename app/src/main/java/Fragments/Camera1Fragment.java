package Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.pixuredlinux3.simplecamera2demo.CameraRenderer;
import com.example.pixuredlinux3.simplecamera2demo.Camera1Preview;
import com.example.pixuredlinux3.simplecamera2demo.GPUImageFilterTools;
import com.example.pixuredlinux3.simplecamera2demo.GalleryActivity;
import com.example.pixuredlinux3.simplecamera2demo.R;
import com.example.pixuredlinux3.simplecamera2demo.TextureRenderer;
import com.example.pixuredlinux3.simplecamera2demo.view.CameraGLSurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * A simple {@link Fragment} subclass.
 */
public class Camera1Fragment extends Fragment {

    private static final int PICK_IMAGE = 1;
    private Camera mCamera = null;
    private Camera1Preview mCameraView = null;
    private int currentCameraId;
    private boolean flashToggle;
    private FrameLayout camera_view;
    private View surfaceView;

    private GPUImage mGPUImage;
    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;

    public Camera1Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera1, container, false);
        surfaceView = view;
        mGPUImage = new GPUImage(getActivity());
        mGPUImage.setGLSurfaceView((GLSurfaceView) view.findViewById(R.id.surfaceView));

        /*GLSurfaceView glSurfaceView = (GLSurfaceView) view.findViewById(R.id.surfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        final CameraRenderer mRenderer = new CameraRenderer(getActivity());
        glSurfaceView.setRenderer(mRenderer);
        mCamera = Camera.open();
        Camera.Parameters p = mCamera.getParameters();
        mCamera.setParameters(p);
        glSurfaceView.queueEvent(new Runnable(){
            public void run() {
                mRenderer.setCamera(mCamera);
            }
        });*/


        /*try{
            mCamera = Camera.open();
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            *//*mCameraView = new Camera1Preview(getActivity(), mCamera); //create a SurfaceView to show camera data
            camera_view = (FrameLayout) view.findViewById(R.id.camera_view);
            camera_view.addView(mCameraView); //add the SurfaceView to the layout*//*
        }*/


        // toggle flash
        final FloatingActionButton toggleFlash = (FloatingActionButton) view.findViewById(R.id.flash);
        toggleFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters p = mCamera.getParameters();
                if(!flashToggle) {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    toggleFlash.setImageResource(R.drawable.ic_flash_on_black_24dp);
                }
                else {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                    toggleFlash.setImageResource(R.drawable.ic_flash_off_black_24dp);
                }

                mCamera.setParameters(p);
                mCamera.startPreview();
                flashToggle = !flashToggle;
            }
        });

        // shoot picture
        FloatingActionButton shoot = (FloatingActionButton) view.findViewById(R.id.shoot);
        assert shoot != null;
        shoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });

        // toggle front/rear camera
        final FloatingActionButton switchCamera = (FloatingActionButton) view.findViewById(R.id.switchCamera);
        assert switchCamera != null;
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*mCamera.stopPreview();
                mCamera.release();*/
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
                //swap the id of the camera to be used
                if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    switchCamera.setImageResource(R.drawable.ic_camera_front_black_24dp);
                }
                else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    switchCamera.setImageResource(R.drawable.ic_camera_rear_black_24dp);
                }
                openCamera();
                /*mCamera = Camera.open(currentCameraId);

                setCameraDisplayOrientation(getActivity(), currentCameraId, mCamera);
                try {
                    mCamera.setPreviewDisplay(mCameraView.getmHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();*/
            }
        });

        final FloatingActionButton chooseFilter = (FloatingActionButton) view.findViewById(R.id.chooseFilter);
        assert switchCamera != null;
        chooseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPUImageFilterTools.showDialog(getActivity(), new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
                    @Override
                    public void onGpuImageFilterChosenListener(GPUImageFilter filter) {
                        switchFilterTo(filter);
                        mGPUImage.requestRender();
                    }
                });
            }
        });

        FloatingActionButton launchGalleryActivity = (FloatingActionButton) view.findViewById(R.id.gallery);
        launchGalleryActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GalleryActivity.class);
                startActivity(intent);
            }
        });
        //return glSurfaceView;
        return view;
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);
        }
    }

    private void openCamera(){
        try{
            mCamera = Camera.open(currentCameraId);
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }
        int orientation = getCameraDisplayOrientation(getActivity(), currentCameraId);
        boolean flipHorizontal = currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;
        mGPUImage.setUpCamera(mCamera, orientation, flipHorizontal, false);
    }

    /*public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }*/

    public int getCameraDisplayOrientation(final Activity activity, final int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("Error", "Error creating media file, check storage permissions: " );
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                mCamera.startPreview();
            } catch (FileNotFoundException e) {
                Log.d("Error", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Error", "Error accessing file: " + e.getMessage());
            }

            data = null;
            Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
            final GLSurfaceView view = (GLSurfaceView) surfaceView.findViewById(R.id.surfaceView);
            view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            mGPUImage.saveToPictures(bitmap, "Camera",
                    System.currentTimeMillis() + ".jpg",
                    new GPUImage.OnPictureSavedListener() {
                        @Override
                        public void onPictureSaved(final Uri uri) {
                            pictureFile.delete();
                            camera.startPreview();
                            view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                        }
                    });
        }
    };

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }


    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onPause() {
        mCamera.setPreviewCallback(null);
        mCamera.release();
        mCamera = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
    }
}


