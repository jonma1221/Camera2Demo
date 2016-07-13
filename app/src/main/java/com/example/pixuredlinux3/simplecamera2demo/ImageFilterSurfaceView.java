package com.example.pixuredlinux3.simplecamera2demo;/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.example.pixuredlinux3.simplecamera2demo.mod.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 11.11.2015
 * com.example.pixuredlinux3.simplecamera2demo.ImageFilterSurfaceView is used to hold GLSurfaceView
 * that renders videos with filters on a surface
 */
public class ImageFilterSurfaceView extends GLSurfaceView {
    private static final String TAG = "com.example.pixuredlinux3.simplecamera2demo.ImageFilterSurfaceView";

    private Context context;
    private ImageRender mRenderer;
    private String URI;
    private String mMediaUri;
    private Bitmap origBitmap;

    public void setRenderer(String uri, Context context, Bitmap bitmap){
        setEGLContextClientVersion(2);
        origBitmap = bitmap;

        Log.d("Width", "Width: " + origBitmap.getWidth());
        Log.d("Height", "Height: " + origBitmap.getHeight());
        mRenderer = new ImageRender(origBitmap, (float) origBitmap.getWidth() / (float) origBitmap.getHeight());
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setRenderer(String uri, Context context){
        mMediaUri = uri;
        setEGLContextClientVersion(2);
        URI = getRealPathFromURI(Uri.parse(mMediaUri), context);
        ExifInterface exif;
        int exifOrientation = 0;
        try {
            exif = new ExifInterface(URI);
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int rotation;
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
            default:
                rotation = 0;
                break;
        }

        BitmapTask task = new BitmapTask(URI, rotation);
        try {
            task.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /*BitmapFactory.Options options = new BitmapFactory.Options();

        origBitmap = BitmapFactory.decodeFile(URI);
        origBitmap = getDownScaledBitmap(origBitmap, 500);*/

        /*options.inJustDecodeBounds = true;
        origBitmap = BitmapFactory.decodeFile(URI, options);
        //options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inSampleSize = 8;
        options.inJustDecodeBounds = false;
        origBitmap = BitmapFactory.decodeFile(URI, options);*/

        /*Log.d("Width", "Width: " + origBitmap.getWidth());
        Log.d("Height", "Height: " + origBitmap.getHeight());
        if (rotation != 0) {
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.postRotate(rotation);
            origBitmap = Bitmap.createBitmap(origBitmap, 0, 0, origBitmap.getWidth(), origBitmap.getHeight(), matrix, true);
        }
        mRenderer = new ImageRender(origBitmap, (float) origBitmap.getWidth() / (float) origBitmap.getHeight());
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);*/
    }

    public ImageFilterSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ImageFilterSurfaceView(Context context, String uri) {
        super(context);
        setEGLContextClientVersion(2);
        String URI = getRealPathFromURI(Uri.parse(uri), context);
        mMediaUri = uri;
        ExifInterface exif;
        int exifOrientation = 0;
        try {
            exif = new ExifInterface(URI);
            //exif = new ExifInterface(Uri.parse(mMediaUri).getPath());
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int rotation;
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
            default:
                rotation = 0;
                break;
        }
        //origBitmap = BitmapDecoder.getDecoder().decode(new File(Uri.parse(uri).getPath()));

        origBitmap = BitmapFactory.decodeFile(URI);
        origBitmap = scaleDown(origBitmap, 800, false);
        Log.d("Width", "Width: " + origBitmap.getWidth());
        Log.d("Height", "Height: " + origBitmap.getHeight());
        if (rotation != 0) {
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.postRotate(rotation);
            origBitmap = Bitmap.createBitmap(origBitmap, 0, 0, origBitmap.getWidth(), origBitmap.getHeight(), matrix, true);
        }
        //TODO: crash here
        mRenderer = new ImageRender(origBitmap, (float) origBitmap.getWidth() / (float) origBitmap.getHeight());
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
    private static Bitmap getDownScaledBitmap(Bitmap bm, float longerEdgeMax) {
        if (longerEdgeMax <= Integer.MAX_VALUE) {

            int bmW = bm.getWidth();
            int bmH = bm.getHeight();

            if (bmW > bmH) {
                if (bmW <= longerEdgeMax) {
                    return bm;
                } else {
                    bmH = (int) (bmH * (longerEdgeMax / bmW));
                    bmW = (int) longerEdgeMax;
                }
            } else {
                if (bmH <= longerEdgeMax) {
                    return bm;
                } else {
                    bmW = (int) (bmW * (longerEdgeMax / bmH));
                    bmH = (int) longerEdgeMax;
                }
            }

            return Bitmap.createScaledBitmap(bm, bmW, bmH, false);
        } else {
            return bm;
        }
    }
    // this is the same method from Android's sample code
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    public String getRealPathFromURI(Uri contentUri, Context context) {
        String[] proj = { MediaStore.Images.Media.DATA };

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public Bitmap getBitmap() {
        return origBitmap;
    }

    public void setDataSource(Uri uri) {
        mMediaUri = uri.toString();
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(getContext(), Uri.parse(mMediaUri));
        } catch (IOException e) {
            Log.e(TAG, "error setting datasource");
            e.printStackTrace();
        }
    }

    public ImageRender getRenderer() {
        return mRenderer;
    }


    public class ImageRender implements Renderer {

        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private final float[] mTriangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 1.f,
                1.0f, -1.0f, 0, 1.f, 1.f,
                -1.0f, 1.0f, 0, 0.f, 0.f,
                1.0f, 1.0f, 0, 1.f, 0.f,
        };

        private final float[] mTriangleVerticesData90 = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 1.f, 1.f,
                1.0f, -1.0f, 0, 0.f, 0.f,
                -1.0f, 1.0f, 0, 1.f, 0.f,
                1.0f, 1.0f, 0, 0.f, 1.f,
        };
        private final String mVertexShader =
                "uniform mat4 uMVPMatrix;\n" +
                        "uniform mat4 uSTMatrix;\n" +
                        "attribute vec4 aPosition;\n" +
                        "attribute vec4 aTextureCoord;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "  gl_Position = uMVPMatrix * aPosition;\n" +
                        "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                        "}\n";
        FragmentShaderDetail mShaderDetail;
        private FloatBuffer mTriangleVertices;
        private HashMap<String, ProgramContainer> mPrograms;
        private String mCurrentFilter;
        private float currentIntensity;
        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];
        private int[] mTextureIDs;
        private Bitmap mBitmap;
        private float mImageWHRatio;
        private float mFlippedImageWHRatio;
        private float viewWHRatio;
        private MyIO.BitmapRetrieverListener mListener;

        int dWidth;
        int dHeight;

        public ImageRender(Bitmap bm, float ratio) {
            mTriangleVertices = ByteBuffer.allocateDirect(
                    mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);
            mImageWHRatio = ratio;
            mFlippedImageWHRatio = 1f / mImageWHRatio;
            Matrix.setIdentityM(mSTMatrix, 0);
            mPrograms = new HashMap<>();
            mShaderDetail = new FragmentShaderDetail();
            mCurrentFilter = PixFilterGenerator.getDefault().getName();
            mBitmap = bm;
        }

        public void onDrawFrame(GL10 glUnused) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            ProgramContainer p = mPrograms.get(mCurrentFilter);
            if (p == null) {
                return;
            }
            GLES20.glUseProgram(p.mProgram);
            GLToolbox.checkGlError("glUseProgram");

            // Set the input texture
            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(p.maPositionHandle, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            GLToolbox.checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(p.maPositionHandle);
            GLToolbox.checkGlError("glEnableVertexAttribArray maPositionHandle");

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(p.maTexCoordHandle, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            GLToolbox.checkGlError("glVertexAttribPointer maTexCoordHandle");
            GLES20.glEnableVertexAttribArray(p.maTexCoordHandle);
            GLToolbox.checkGlError("glEnableVertexAttribArray maTexCoordHandle");

            GLES20.glUniformMatrix4fv(p.muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(p.muSTMatrixHandle, 1, false, mSTMatrix, 0);
            p.mFilter.setIntensity(currentIntensity);
            p.mBrightnessMod.setRawIntensity(mShaderDetail.getBrightness());
            p.mSaturationMod.setRawIntensity(mShaderDetail.getSaturation());
            p.mContrastMod.setRawIntensity(mShaderDetail.getContrast());
            p.mWarmthMod.setRawIntensity(mShaderDetail.getTemp());
            p.mVignetteMod.setRawIntensity(mShaderDetail.getVignetteStart(), 1f);
            //TODO: reenable when settings won't interfere with other mods
//            GLES20.glUniform1f(p.mShadowHandle, mShadow);
//            GLES20.glUniform1f(p.mHighlightHandle, mHighlight);

            p.mFilter.drawMods();

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLToolbox.checkGlError("glActiveTexture");
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIDs[0]);
            GLToolbox.checkGlError("glBindTexture");
            GLES20.glUniform1i(p.mTextureHandle, 0);
            GLToolbox.checkGlError("glUniformTexture");
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLToolbox.checkGlError("glDrawArrays");

            if (mListener != null) {
                mListener.onBitmapRetrieved(takeSnapshot(glUnused));
            }
            GLES20.glFinish();
        }

        public void onSurfaceChanged(GL10 glUnused, int width, int height) {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            dWidth = metrics.widthPixels;
            dHeight = metrics.heightPixels;
            Log.d("Frame Width:", "" + dWidth);
            Log.d("Frame Height:", "" + dHeight);
           //GLES20.glViewport(0, 0, width, height);
            GLES20.glViewport(0, 0, dWidth, dHeight);
            viewWHRatio = ((float) width / (float) height);
            int curflip = mShaderDetail.getRotation() / 90;
            TextureRenderer.updateUMatrix(mMVPMatrix,
                    curflip % 2 == 0 ? mImageWHRatio : mFlippedImageWHRatio,
                    viewWHRatio, mShaderDetail.getRotation());
        }

        public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
            ArrayList<PixuredFilter> filters = PixFilterGenerator.getAllFilters();
            for (PixuredFilter filter : filters) {

                ArrayList<PixFilterMod> mAdjustFilters = new ArrayList<>();
                SaturationFilterMod mSaturationMod = new SaturationFilterMod(1, 9);
                ContrastFilterMod mContrastMod = new ContrastFilterMod(1, 9);
                WarmthFilterMod mWarmthMod = new WarmthFilterMod(0, 9);
                BrightnessFilterMod mBrightnessMod = new BrightnessFilterMod(0, 9);
                VignetteFilterMod mVignetteMod = new VignetteFilterMod(1, 1, 9);
                mAdjustFilters.add(mSaturationMod);
                mAdjustFilters.add(mContrastMod);
                mAdjustFilters.add(mWarmthMod);
                mAdjustFilters.add(mBrightnessMod);
                mAdjustFilters.add(mVignetteMod);
                filter.addAdjustMods(mAdjustFilters);

                int mProgram = GLToolbox.createProgram(mVertexShader, filter.getFragmentShader(false));
                if (mProgram == 0) {
                    return;
                }
                int maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
                GLToolbox.checkGlError("glGetAttribLocation aPosition");
                if (maPositionHandle == -1) {
                    throw new RuntimeException("Could not get attrib location for aPosition");
                }
                int mTextureHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
                int maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
                GLToolbox.checkGlError("glGetAttribLocation aTextureCoord");
                if (maTextureHandle == -1) {
                    throw new RuntimeException("Could not get attrib location for aTextureCoord");
                }

                int muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
                GLToolbox.checkGlError("glGetUniformLocation uMVPMatrix");
                if (muMVPMatrixHandle == -1) {
                    throw new RuntimeException("Could not get attrib location for uMVPMatrix");
                }

                int muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
                GLToolbox.checkGlError("glGetUniformLocation uSTMatrix");
                if (muSTMatrixHandle == -1) {
                    throw new RuntimeException("Could not get attrib location for uSTMatrix");
                }

                filter.setHandler(mProgram);
                ProgramContainer p = new ProgramContainer(mProgram, maPositionHandle,
                        maTextureHandle, muMVPMatrixHandle, muSTMatrixHandle);
                p.mTextureHandle = mTextureHandle;
                p.mFilter = filter;
                p.mBrightnessMod = mBrightnessMod;
                p.mSaturationMod = mSaturationMod;
                p.mWarmthMod = mWarmthMod;
                p.mContrastMod = mContrastMod;
                p.mVignetteMod = mVignetteMod;
                mPrograms.put(filter.getName(), p);
            }

            mTextureIDs = new int[2];
            GLToolbox.loadTextures(mTextureIDs, mBitmap);
        }

        public int getFrameWidth(){
            return dWidth;
        }

        public int getFrameHeight(){
            return dHeight;
        }

        public PixuredFilter getCurrentShader() {
            return mPrograms.get(mCurrentFilter).mFilter;
        }

        public float getFilterIntensity() {
            return currentIntensity;
        }

        public void setFilterIntensity(float intensity) {
            currentIntensity = intensity;
            requestRender();
        }

        public void setCurrentFilter(String filtername, float intensity) {
            if (filtername != null) {
                mCurrentFilter = filtername;
                currentIntensity = intensity;
                requestRender();
            }
        }

        public FragmentShaderDetail getShaderDetail() {
            return mShaderDetail;
        }

        public float getBrightness() {
            return (mShaderDetail.getBrightness() + 1f) / 2f;
        }

        /**
         * All com.example.pixuredlinux3.simplecamera2demo.mod settings will expect float between
         * 0 and 1 and will return such
         */
        public void setBrightness(float brightness) {

            mShaderDetail.setBrightness(brightness * 2f - 1f);
            requestRender();
        }

        public float getSaturation() {
            return mShaderDetail.getSaturation() / 2f;
        }

        public void setSaturation(float saturation) {
            mShaderDetail.setSaturation(saturation * 2);
            requestRender();
        }

        public float getContrast() {
            float adjustContrast;
            float contrast = mShaderDetail.getContrast();
            if (contrast > 1.5f) {
                adjustContrast = contrast / 2f;
            } else {
                adjustContrast = contrast - .5f;
            }
            return adjustContrast;
        }

        public void setContrast(float contrast) {
            float adjustContrast;
            if (contrast <= .5) {
                adjustContrast = contrast + .5f;
            } else {
                adjustContrast = 2f * contrast;
            }
            mShaderDetail.setContrast(adjustContrast);
            requestRender();
        }

        public float getWarmth() {
            return (mShaderDetail.getTemp() + 1f) / 2f;
        }

        public void setWarmth(float temp) {
            mShaderDetail.setTemp(temp * 2f - 1f);
            requestRender();
        }

        public float getVignette() {
            return Math.abs(mShaderDetail.getVignetteStart() * 2f - 1);
        }

        public void setVignette(float vig) {
            vig = Math.abs(vig - 1);
            mShaderDetail.setVignetteStart(vig * .5f);
            requestRender();
        }

        public float getHighlight() {
            return mShaderDetail.getHighlight();
        }

        public void setHighlight(float hl) {
            mShaderDetail.setHighlight(hl);
            requestRender();
        }

        public float getShadow() {
            return mShaderDetail.getShadow();
        }

        public void setShadow(float shadow) {
            mShaderDetail.setShadow(shadow);
            requestRender();
        }

        public void setRotation(int degree) {
            mShaderDetail.setRotation(degree);
            int curflip = mShaderDetail.getRotation() / 90;
            TextureRenderer.updateUMatrix(mMVPMatrix,
                    curflip % 2 == 0 ? mImageWHRatio : mFlippedImageWHRatio,
                    viewWHRatio, mShaderDetail.getRotation());
            requestRender();
        }

        public void getCurrentRenderedBitmap(MyIO.BitmapRetrieverListener l) {
            mListener = l;
            requestRender();
        }

        public Bitmap takeSnapshot(GL10 mGL) {
//            final int width = com.example.pixuredlinux3.simplecamera2demo.ImageFilterSurfaceView.this.getWidth();
//            final int height = com.example.pixuredlinux3.simplecamera2demo.ImageFilterSurfaceView.this.getHeight();
//            ByteBuffer mPixelBuf = ByteBuffer.allocateDirect(width * height * 4);
//            mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
//            mPixelBuf.rewind();
//            GLES20.glReadPixels(0, 0, width, height,
//                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mPixelBuf);
//            Bitmap bm = Bitmap.createBitmap(width,
//                    height, Bitmap.Config.ARGB_8888);
//            mPixelBuf.rewind();
//            bm.copyPixelsFromBuffer(mPixelBuf);
//            android.graphics.Matrix matrix = new android.graphics.Matrix();
//            matrix.postRotate(180);
//            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
//            mPixelBuf.clear();
//            return bm;
            final int mWidth = ImageFilterSurfaceView.this.getWidth();
            final int mHeight = ImageFilterSurfaceView.this.getHeight();
            IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
            IntBuffer ibt = IntBuffer.allocate(mWidth * mHeight);
            mGL.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

            // Convert upside down mirror-reversed image to right-side up normal
            // image.
            for (int i = 0; i < mHeight; i++) {
                for (int j = 0; j < mWidth; j++) {
                    ibt.put((mHeight - i - 1) * mWidth + j, ib.get(i * mWidth + j));
                }
            }

            Bitmap mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mBitmap.copyPixelsFromBuffer(ibt);
            return mBitmap;
        }
    }

    class BitmapTask extends AsyncTask<Void, Void, Bitmap>{
        //Bitmap origBitmap;
        String uri;
        int rotation;
        String filter;
        int intensity;

        public BitmapTask(String uri, int rotation){
            this.uri = uri;
            this.rotation = rotation;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            origBitmap = BitmapFactory.decodeFile(uri, options);
            //options.inSampleSize = calculateInSampleSize(options, dWidth, dHeight);
            options.inSampleSize = 4;
            options.inJustDecodeBounds = false;
            origBitmap = BitmapFactory.decodeFile(uri, options);
            return origBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Log.d("Background", "Executed");
            Log.d("Width", "Width: " + origBitmap.getWidth());
            Log.d("Height", "Height: " + origBitmap.getHeight());
            if (rotation != 0) {
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                matrix.postRotate(rotation);
                origBitmap = Bitmap.createBitmap(origBitmap, 0, 0, origBitmap.getWidth(), origBitmap.getHeight(), matrix, true);
            }
            mRenderer = new ImageRender(origBitmap, (float) origBitmap.getWidth() / (float) origBitmap.getHeight());
            setRenderer(mRenderer);
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        }
    }
}

