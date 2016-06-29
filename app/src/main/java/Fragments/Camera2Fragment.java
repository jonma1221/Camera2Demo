package Fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Toast;

import com.example.pixuredlinux3.simplecamera2demo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Camera2Fragment extends Fragment {
    private final static String TAG = "Camera2test";
    private TextureView textureView;
    private Surface surface;

    private static final int REQUEST_CAMERA_RESULT = 1;
    private static final int REQUEST_WRITE_STORAGE_RESULT = 2;
    private String cameraId;
    private String frontCameraId;
    private boolean toggleCamera = false;
    private boolean toggleFlash = false;
    private int imageCount = 0;

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraManager manager;
    private Size size;

    private Handler backgroundHandler;
    private HandlerThread thread;

    public Camera2Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera2, container, false);
        textureView = (TextureView) view.findViewById(R.id.texture);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
                openCamera(manager);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        // Button to toggle flash
        final FloatingActionButton flashSwitch = (FloatingActionButton) view.findViewById(R.id.flash);
        assert flashSwitch != null;
        flashSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
                toggleFlash = !toggleFlash;
                if (!toggleFlash)
                    flashSwitch.setImageResource(R.drawable.ic_flash_off_black_24dp);
                else flashSwitch.setImageResource(R.drawable.ic_flash_on_black_24dp);
                openCamera(manager);
            }
        });

        // Button to shoot photo
        FloatingActionButton backCameraShoot = (FloatingActionButton) view.findViewById(R.id.shoot);
        assert backCameraShoot != null;
        backCameraShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    takePicture();

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        // Button to toggle front/rear camera
        final FloatingActionButton switchCamera = (FloatingActionButton) view.findViewById(R.id.switchCamera);
        assert switchCamera != null;
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
                toggleCamera = !toggleCamera;
                if (!toggleCamera)
                    switchCamera.setImageResource(R.drawable.ic_camera_front_black_24dp);
                else switchCamera.setImageResource(R.drawable.ic_camera_rear_black_24dp);
                openCamera(manager);
            }
        });
        return view;
    }

    // open camera and fetch camera data
    private void openCamera(CameraManager manager) {
        try {
            cameraId = manager.getCameraIdList()[0]; // front camera
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            for (String id : manager.getCameraIdList()) {
                characteristics = manager.getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = id;
                }
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                }
            }

            size = configs.getOutputSizes(SurfaceTexture.class)[0];

            String camera = toggleCamera ? frontCameraId : cameraId;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(getActivity(), "No permission to use the camera services", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[] {Manifest.permission.CAMERA},
                            REQUEST_CAMERA_RESULT);
                }
            }

            manager.openCamera(camera, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    cameraDevice.close();
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    cameraDevice.close();
                }
            }, null);
        }
        catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_CAMERA_RESULT:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getActivity(),
                            "Cannot run application because camera service permissions have not been granted",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_WRITE_STORAGE_RESULT:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void closeCamera() {
        cameraCaptureSession.close();
        cameraCaptureSession = null;
        cameraDevice.close();
        cameraDevice = null;
    }

    // submitting requests
    private void takePicture() throws CameraAccessException {

        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            //outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            // Create the requests
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            //captureBuilder.set(/* CaptureRequest.CONTROL_MODE */CaptureRequest.FLASH_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            if(toggleFlash){
                captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            }
            else{
                captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            }

            // Orientation
            WindowManager windowManager = (WindowManager) this
                    .getActivity().getSystemService(Context.WINDOW_SERVICE);
            int rotation = windowManager.getDefaultDisplay().getRotation();
            //int rotation = getWindowManager().getDefaultDisplay().getRotation();
            Log.d("Rotation:", rotation + "");
            int screenOrientation = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    screenOrientation = 0;
                    break;
                case Surface.ROTATION_90:
                    screenOrientation = 90;
                    break;
                case Surface.ROTATION_180:
                    screenOrientation = 180;
                    break;
                case Surface.ROTATION_270:
                    screenOrientation =  270;
                    break;
            }
            int adjustedOrientation = getJpegOrientation(characteristics, screenOrientation);
            /*ORIENTATIONS.append(Surface.ROTATION_0, 90);
            ORIENTATIONS.append(Surface.ROTATION_90, 0);
            ORIENTATIONS.append(Surface.ROTATION_180, 270);
            ORIENTATIONS.append(Surface.ROTATION_270, 180);*/

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, adjustedOrientation);
            //captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory()+"/img" + imageCount++ + ".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireNextImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED){
                                save(bytes);
                            }
                            else{
                                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    Toast.makeText(getActivity(),
                                            "We need write storage permission to start the gallery and save images",
                                            Toast.LENGTH_SHORT).show();
                                }
                                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_WRITE_STORAGE_RESULT);
                            }
                        }
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };

            //reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            reader.setOnImageAvailableListener(readerListener, backgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(getActivity(), "Saved:" + file, Toast.LENGTH_SHORT).show();
                    startPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        //session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                        session.capture(captureBuilder.build(), captureListener, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, /*mBackgroundHandler*/ backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }

    private void startPreview() {
        if(null == cameraDevice || !textureView.isAvailable() || null == size) {
            Log.e(TAG, "startPreview fail, return");
        }

        // Create surfaces
        SurfaceTexture texture = textureView.getSurfaceTexture();
        if(null == texture) {
            Log.e(TAG,"texture is null, return");
            return;
        }
        texture.setDefaultBufferSize(size.getWidth(), size.getHeight());
        surface = new Surface(texture);

        try {
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(getActivity(), "onConfigureFailed", Toast.LENGTH_LONG).show();
                }

            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(null == cameraDevice){
            Log.e(TAG, "error updating preview");
        }

        // Building requests
        try{
            captureRequestBuilder = cameraDevice.createCaptureRequest(cameraDevice.TEMPLATE_PREVIEW);
        }
        catch(CameraAccessException e) {
            e.printStackTrace();
        }
        captureRequestBuilder.addTarget(surface);

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        thread = new HandlerThread("CameraPreview");
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());

        try{
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        }
        catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

}
