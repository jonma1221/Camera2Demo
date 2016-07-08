package com.example.pixuredlinux3.simplecamera2demo.mod;

import android.opengl.GLES20;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.pixuredlinux3.simplecamera2demo.GLToolbox;


/**
 * Created on 1/14/16.
 */
public class SimpleUniformFilterMod extends PixFilterMod implements Parcelable {

    public static final Creator<SimpleUniformFilterMod> CREATOR = new Creator<SimpleUniformFilterMod>() {
        @Override
        public SimpleUniformFilterMod createFromParcel(Parcel in) {
            return new SimpleUniformFilterMod(in);
        }

        @Override
        public SimpleUniformFilterMod[] newArray(int size) {
            return new SimpleUniformFilterMod[size];
        }
    };
    protected static final String FIELD_NAME = "FILTER_HANDLER";
    protected String shader_handler;
    protected String shader_code;
    protected String shader_method;
    protected int mHandler;
    protected float min_intensity = 0f;
    protected float max_intensity;
    protected float currentIntensity;

    public SimpleUniformFilterMod(int handlerId) {
        init(handlerId);
    }

    public SimpleUniformFilterMod(float max, int handlerId) {
        this(handlerId);
        max_intensity = max;
        currentIntensity = max_intensity;
    }

    public SimpleUniformFilterMod(Parcel in) {
        super(in);
        min_intensity = in.readFloat();
        max_intensity = in.readFloat();
        currentIntensity = in.readFloat();
        shader_code = in.readString();
        shader_method = in.readString();
        shader_handler = in.readString();

    }

    protected void init(int handlerId) {
    }

    @Override
    public String getShaderField() {
        return "uniform float " + shader_handler + ";";
    }

    @Override
    public String getShaderCode() {
        return shader_code;
    }

    @Override
    public String getShaderMethod() {
        return shader_method.replace(FIELD_NAME, shader_handler);
    }

    @Override
    public void setMax(float... max) {
        if (max != null && max.length > 0) {
            max_intensity = max[0];
        }
    }

    @Override
    public void setRawIntensity(float... intensity) {
        if (intensity.length > 0) {
            currentIntensity = intensity[0];
        }
    }

    @Override
    public void setIntensity(float intensity) {
        currentIntensity = intensity * (max_intensity - min_intensity) + min_intensity;
    }

    @Override
    public void initHandler(int programId) {
        mHandler = GLES20.glGetUniformLocation(programId, shader_handler);
        GLToolbox.checkGlError("glGetUniformLocation " + shader_handler);
        if (mHandler == -1) {
            throw new RuntimeException("Could not get attrib location for " + shader_handler);
        }
    }

    @Override
    public void draw() {
        GLES20.glUniform1f(mHandler, currentIntensity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(min_intensity);
        dest.writeFloat(max_intensity);
        dest.writeFloat(currentIntensity);
        dest.writeString(shader_code);
        dest.writeString(shader_method);
        dest.writeString(shader_handler);

    }
}

