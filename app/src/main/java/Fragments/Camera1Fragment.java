package Fragments;


import android.app.Activity;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.pixuredlinux3.simplecamera2demo.Camera1Preview;
import com.example.pixuredlinux3.simplecamera2demo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class Camera1Fragment extends Fragment {

    private Camera mCamera = null;
    private Camera1Preview mCameraView = null;
    private int currentCameraId;
    private boolean flashToggle;

    public Camera1Fragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    /*@Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       if(savedInstanceState != null) mCamera = (Camera) savedInstanceState.getSerializable("camera");
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera1, container, false);

        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new Camera1Preview(getActivity(), mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout) view.findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

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

        FloatingActionButton shoot = (FloatingActionButton) view.findViewById(R.id.shoot);
        assert shoot != null;
        shoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });

        final FloatingActionButton switchCamera = (FloatingActionButton) view.findViewById(R.id.switchCamera);
        assert switchCamera != null;
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.stopPreview();

                // release the current camera before switching to prevent app crash
                mCamera.release();

                //swap the id of the camera to be used
                if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    switchCamera.setImageResource(R.drawable.ic_camera_front_black_24dp);
                }
                else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    switchCamera.setImageResource(R.drawable.ic_camera_rear_black_24dp);
                }
                mCamera = Camera.open(currentCameraId);

                setCameraDisplayOrientation(getActivity(), currentCameraId, mCamera);
                try {
                    mCamera.setPreviewDisplay(mCameraView.getmHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }
        });
        return view;
    }

    public static void setCameraDisplayOrientation(Activity activity,
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
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
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
        super.onPause();
        mCameraView.getHolder().removeCallback(mCameraView);
        mCamera.setPreviewCallback(null);
        mCamera.release();
    }
}
