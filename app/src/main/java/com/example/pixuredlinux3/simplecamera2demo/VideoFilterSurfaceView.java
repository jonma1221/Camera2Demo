/*
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
 *//*


package com.example.pixuredlinux3.simplecamera2demo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.pixured.app.mediafilter.GLToolbox;
import com.pixured.app.mediafilter.PixFilterGenerator;
import com.pixured.app.mediafilter.PixuredFilter;
import com.pixured.app.mediafilter.ProgramContainer;
import com.pixured.app.mediafilter.TextureRenderer;
import com.pixured.app.widget.viewgroup.TextureVideoView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.pixured.app.widget.viewgroup.TextureVideoView.State;

*/
/**
 * VideoFilterSurfaceView is used to hold GLSurfaceView
 * that renders videos with filters on a surface
 *//*

public class VideoFilterSurfaceView extends GLSurfaceView {
    private static final String TAG = "VideoFilterSurfaceView";

    VideoRender mRenderer;
    private String mMediaUri;

    public VideoFilterSurfaceView(Context context, String uri, float videoWHRatio) {
        super(context);

        setEGLContextClientVersion(2);
        mMediaUri = uri;
        mRenderer = new VideoRender(videoWHRatio);
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(context, Uri.parse(mMediaUri));
        } catch (IOException e) {
            Log.e(TAG, "error setting datasource");
            e.printStackTrace();
        }
        mRenderer.setMediaPlayer(mp);
        setRenderer(mRenderer);
    }

    public void setDataSource(Uri uri) {
        mRenderer.releaseMediaPlayer();
        mMediaUri = uri.toString();
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(getContext(), Uri.parse(mMediaUri));
        } catch (IOException e) {
            Log.e(TAG, "error setting datasource");
            e.printStackTrace();
        }
        mRenderer.setMediaPlayer(mp);
    }

    public VideoRender getRenderer() {
        return mRenderer;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "[onPause]");
        queueEvent(new Runnable() {
            public void run() {
                mRenderer.releaseMediaPlayer();
            }
        });
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "[onResume]");
        queueEvent(new Runnable() {
            public void run() {
                MediaPlayer mp = new MediaPlayer();
                try {
                    mp.setDataSource(getContext(), Uri.parse(mMediaUri));
                } catch (IOException e) {
                    Log.e(TAG, "error setting datasource");
                    e.printStackTrace();
                }
                mRenderer.setMediaPlayer(mp);
            }
        });
        super.onResume();
    }

    public class VideoRender
            implements Renderer, SurfaceTexture.OnFrameAvailableListener {

        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private final float[] mTriangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 0.f,
                1.0f, -1.0f, 0, 1.f, 0.f,
                -1.0f, 1.0f, 0, 0.f, 1.f,
                1.0f, 1.0f, 0, 1.f, 1.f,
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
        private final float mVideoWHRatio;
        private FloatBuffer mTriangleVertices;
        private State mState;
        private TextureVideoView.MediaPlayerListener mListener;
        private HashMap<String, ProgramContainer> mPrograms;
        private String mCurrentShader;
        private float mCurrentIntensity = 1f;
        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];
        private int[] mTextureIDs;
        private SurfaceTexture mSurface;
        private boolean updateSurface = false;
        private int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
        private MediaPlayer mMediaPlayer;
        private float viewWHRatio;

        public VideoRender(float ratio) {
            mTriangleVertices = ByteBuffer.allocateDirect(
                    mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);
            mVideoWHRatio = ratio;
            Matrix.setIdentityM(mSTMatrix, 0);
            mPrograms = new HashMap<>();
            mCurrentShader = PixFilterGenerator.MOD_DEFAULT;
        }

        public void setMediaPlayer(MediaPlayer player) {
            Log.d(TAG, "setting mediaplayer");
            mMediaPlayer = player;
            if (mSurface != null) {
                Surface surface = new Surface(mSurface);
                mMediaPlayer.setSurface(surface);
                surface.release();
                try {
                    setListener(mListener);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (IOException t) {
                    Log.e(TAG, "media player prepare failed");
                }
            }
        }

        public void releaseMediaPlayer() {
            Log.d(TAG, "releasing mediaplayer");
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mState = State.UNINITIALIZED;
            }
        }

        public void onDrawFrame(GL10 glUnused) {
            synchronized (this) {
                if (updateSurface) {
                    mSurface.updateTexImage();
                    mSurface.getTransformMatrix(mSTMatrix);
                    updateSurface = false;
                }
            }
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            ProgramContainer prog = mPrograms.get(mCurrentShader);

            if (prog == null) {
                return;
            }

            GLES20.glUseProgram(prog.mProgram);
            GLToolbox.checkGlError("glUseProgram");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //GLToolbox.checkGlError("glActiveTexture");
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureIDs[0]);
            //GLToolbox.checkGlError("glBindTexture");

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(prog.maPositionHandle, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            GLToolbox.checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(prog.maPositionHandle);
            GLToolbox.checkGlError("glEnableVertexAttribArray maPositionHandle");

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(prog.maTexCoordHandle, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            GLToolbox.checkGlError("glVertexAttribPointer maTexCoordHandle");
            GLES20.glEnableVertexAttribArray(prog.maTexCoordHandle);
            GLToolbox.checkGlError("glEnableVertexAttribArray maTexCoordHandle");

            prog.mFilter.setIntensity(mCurrentIntensity);
            prog.mFilter.drawMods();

            //Matrix.setIdentityM(mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(prog.muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(prog.muSTMatrixHandle, 1, false, mSTMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLToolbox.checkGlError("glDrawArrays");
            GLES20.glFinish();

        }

        public void onSurfaceChanged(GL10 glUnused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            viewWHRatio = ((float) width / (float) height);
            TextureRenderer.updateUMatrix(mMVPMatrix, mVideoWHRatio, viewWHRatio);
            Log.d(TAG, String.format("video ratio %.2f, view ratio: %.2f", mVideoWHRatio, viewWHRatio));
        }

        public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
            if (mSurface != null) {
                mState = State.UNINITIALIZED;
                mMediaPlayer.release();
                mSurface.release();
                mMediaPlayer = new MediaPlayer();
                try {
                    mMediaPlayer.setDataSource(getContext(), Uri.parse(mMediaUri));
                } catch (IOException e) {
                    Log.e(TAG, "error setting datasource");
                    e.printStackTrace();
                }
            }
            ArrayList<PixuredFilter> filters = PixFilterGenerator.getAllFilters();
            for (PixuredFilter filter : filters) {
                int mProgram = GLToolbox.createProgram(mVertexShader, filter.getFragmentShader(true));
                if (mProgram == 0) {
                    return;
                }
                int maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
                GLToolbox.checkGlError("glGetAttribLocation aPosition");
                if (maPositionHandle == -1) {
                    throw new RuntimeException("Could not get attrib location for aPosition");
                }
                int maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
                GLToolbox.checkGlError("glGetAttribLocation aTextureCoord");
                if (maTexCoordHandle == -1) {
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
                        maTexCoordHandle, muMVPMatrixHandle, muSTMatrixHandle);
                p.mFilter = filter;
                mPrograms.put(filter.getName(), p);
            }

            mTextureIDs = new int[2];
            GLES20.glGenTextures(2, mTextureIDs, 0);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureIDs[0]);
            GLToolbox.checkGlError("glBindTexture mTextureID");

            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);

            */
/*
             * Create the SurfaceTexture that will feed this textureID,
             * and pass it to the MediaPlayer
             *//*

            mSurface = new SurfaceTexture(mTextureIDs[0]);
            mSurface.setOnFrameAvailableListener(this);
            Surface surface = new Surface(mSurface);
            mMediaPlayer.setSurface(surface);
            surface.release();
            try {
                setListener(mListener);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException t) {
                Log.e(TAG, "media player prepare failed");
            }

            synchronized (this) {
                updateSurface = false;
            }
        }

        synchronized public void onFrameAvailable(SurfaceTexture surface) {
            updateSurface = true;
        }

        public PixuredFilter getCurrentShader() {
            return mPrograms.get(mCurrentShader).mFilter;
        }

        public float getFilterIntensity() {
            return mCurrentIntensity;
        }

        public void setFilterIntensity(float intensity) {
            mCurrentIntensity = intensity;
            requestRender();
        }

        public void setCurrentShader(String filtername, float intensity) {
            if (filtername != null) {
                mCurrentShader = filtername;
                mCurrentIntensity = intensity;
            }
        }

        public void pausePlay() {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                }
            }
        }

        public void seekTo(int milliseconds) {
            if (mMediaPlayer != null && mState != State.UNINITIALIZED) {
                mMediaPlayer.seekTo(milliseconds);
            }
        }

        public void pause() {
            if (mMediaPlayer != null && mState != State.UNINITIALIZED) {
                mMediaPlayer.pause();
            }
        }

        public void play() {
            if (mMediaPlayer != null && mState != State.UNINITIALIZED) {
                mMediaPlayer.start();
            }
        }

        public int getCurrentPosition() {
            if (mMediaPlayer != null && mState != State.UNINITIALIZED)
                return mMediaPlayer.getCurrentPosition();
            else
                return 0;
        }

        public void mute() {
            if (mMediaPlayer != null && mState != State.UNINITIALIZED)
                mMediaPlayer.setVolume(0,0);
        }

        public void unMute() {
            if (mMediaPlayer != null && mState != State.UNINITIALIZED)
                mMediaPlayer.setVolume(1,1);
        }

        public boolean isReady() {
            return (mState != State.UNINITIALIZED && mState != State.END);
        }

        public boolean isPlaying() {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        }

        public boolean hasEnded() {
            return mState == State.END;
        }

        public void setListener(TextureVideoView.MediaPlayerListener l) {
            mListener = l;
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mState = State.END;
                    if (mListener != null) {
                        mListener.onVideoEnd();
                    }
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    return false;
                }
            });
            // Play video when the media source is ready for playback.
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mState = State.PREPARED;
                    if (mListener != null) {
                        mListener.onVideoPrepared();
                    }
                }
            });
        }
    }
}*/
