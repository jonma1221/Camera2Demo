package com.example.pixuredlinux3.simplecamera2demo.mod;

import android.opengl.GLES20;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.pixuredlinux3.simplecamera2demo.GLToolbox;

/**
 * Created on 1/14/16.
 */
public class VignetteFilterMod extends PixFilterMod implements Parcelable {

    public static final Creator<VignetteFilterMod> CREATOR = new Creator<VignetteFilterMod>() {
        @Override
        public VignetteFilterMod createFromParcel(Parcel in) {
            return new VignetteFilterMod(in);
        }

        @Override
        public VignetteFilterMod[] newArray(int size) {
            return new VignetteFilterMod[size];
        }
    };
    private static final String START_FIELD_PLACEHOLDER = "START_FIELD";
    private static final String END_FIELD_PLACEHOLDER = "END_FIELD";
    private static final String SHADER_CODE =
            " const vec2 vignetteCenter = vec2(0.5, 0.5);" +
                    " const vec3 vignetteColor = vec3(0.0, 0.0, 0.0);" +
                    " vec4 addVignette(vec4 fragColor, float vignetteStart, float vignetteEnd) {" +
                    "     lowp vec3 rgb = fragColor.rgb;" +
                    "     lowp float d = distance(vTextureCoord, vec2(vignetteCenter.x, vignetteCenter.y));" +
                    "     lowp float percent = smoothstep(vignetteStart, vignetteEnd, d);" +
                    "     return vec4(mix(rgb.x, vignetteColor.x, percent), mix(rgb.y, vignetteColor.y, percent), mix(rgb.z, vignetteColor.z, percent), 1.0);" +
                    " }";
    private static final String SHADER_METHOD = "fragColor = addVignette(fragColor, " + START_FIELD_PLACEHOLDER + ", " + END_FIELD_PLACEHOLDER + ");";
    private static final float MIN_START_INTENSITY = 1f;
    private static final float MIN_END_INTENSITY = 1f;
    private String vignetteStartHandler = "vStart";
    private String vignetteEndHandler = "vEnd";
    private float maxStartIntensity;
    private float maxEndIntensity;
    private float currentStartIntensity;
    private float currentEndIntensity;
    private int mStartHandler;
    private int mEndHandler;

    //max of start and end should be [0, 1]
    public VignetteFilterMod(float maxStart, float maxEnd, int handlerId) {
        vignetteStartHandler += handlerId;
        vignetteEndHandler += handlerId;
        maxStartIntensity = maxStart;
        maxEndIntensity = maxEnd;
        currentStartIntensity = maxStartIntensity;
        currentEndIntensity = maxEndIntensity;
    }

    public VignetteFilterMod(Parcel in) {
        maxStartIntensity = in.readFloat();
        maxEndIntensity = in.readFloat();
        currentStartIntensity = in.readFloat();
        currentEndIntensity = in.readFloat();
        vignetteStartHandler = in.readString();
        vignetteEndHandler = in.readString();

    }

    @Override
    public String getShaderField() {
        return "uniform float " + vignetteStartHandler + ";" +
                "uniform float " + vignetteEndHandler + ";";
    }

    @Override
    public String getShaderCode() {
        return SHADER_CODE;
    }

    @Override
    public String getShaderMethod() {
        return SHADER_METHOD.replace(START_FIELD_PLACEHOLDER, vignetteStartHandler)
                .replace(END_FIELD_PLACEHOLDER, vignetteEndHandler);
    }

    /**
     * Max takes in at most 2 floats
     * max[0] will always effect the start while
     * max[1] will always effect the end
     * @param max
     */
    @Override
    public void setMax(float... max) {
        if (max.length > 1) {
            maxStartIntensity = max[0];
            maxEndIntensity = max[1];
        } else if (max.length > 0) {
            maxStartIntensity = max[0];
            maxEndIntensity = MIN_END_INTENSITY;
        }
    }

    @Override
    public void setRawIntensity(float... intensity) {
        if (intensity.length > 0) {
            currentStartIntensity = intensity[0];
            if (intensity.length > 1) {
                currentEndIntensity = intensity[1];
            }
        }
    }

    @Override
    public void setIntensity(float intensity) {
        currentStartIntensity = intensity * (maxStartIntensity - MIN_START_INTENSITY) + MIN_START_INTENSITY;
        currentEndIntensity = intensity * (maxEndIntensity - MIN_END_INTENSITY) + MIN_END_INTENSITY;
    }

    @Override
    public void initHandler(int programId) {
        mStartHandler = GLES20.glGetUniformLocation(programId, vignetteStartHandler);
        GLToolbox.checkGlError("glGetUniformLocation " + vignetteStartHandler);
        if (mStartHandler == -1) {
            throw new RuntimeException("Could not get attrib location for " + vignetteStartHandler);
        }
        mEndHandler = GLES20.glGetUniformLocation(programId, vignetteEndHandler);
        GLToolbox.checkGlError("glGetUniformLocation " + vignetteEndHandler);
        if (mEndHandler == -1) {
            throw new RuntimeException("Could not get attrib location for " + vignetteEndHandler);
        }
    }

    @Override
    public void draw() {
        GLES20.glUniform1f(mStartHandler, currentStartIntensity);
        GLES20.glUniform1f(mEndHandler, currentEndIntensity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(maxStartIntensity);
        dest.writeFloat(maxEndIntensity);
        dest.writeFloat(currentStartIntensity);
        dest.writeFloat(currentEndIntensity);
        dest.writeString(vignetteStartHandler);
        dest.writeString(vignetteEndHandler);
    }
}
