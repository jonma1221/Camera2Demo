package com.example.pixuredlinux3.simplecamera2demo.mod;

import android.opengl.GLES20;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.pixuredlinux3.simplecamera2demo.GLToolbox;

/**
 * Created on 1/14/16.
 * ColorFilterMod unlike the other filtermods requires
 * a matrix to modify the image itself. Once matrix is set the coordinates
 * do not change, only the intensity to adjust from default to fully filtered
 */
public class ColorFilterMod extends PixFilterMod implements Parcelable {

    public static final Creator<ColorFilterMod> CREATOR = new Creator<ColorFilterMod>() {
        @Override
        public ColorFilterMod createFromParcel(Parcel in) {
            return new ColorFilterMod(in);
        }

        @Override
        public ColorFilterMod[] newArray(int size) {
            return new ColorFilterMod[size];
        }
    };
    private static final String INTENSITY_FIELD_PLACEHOLDER = "INTENSITY_FIELD";
    private static final String MATRIX_FIELD_PLACEHOLDER = "MATRIX_FIELD";
    private static final String SHADER_CODE =
            "vec4 addColorFilter(vec4 fragColor, float intensity, mat4 matrix)\n" +
                    "{" +
                    "    vec4 outputColor = fragColor * matrix;" +
                    "    return (intensity * outputColor) + ((1.0 - intensity) * fragColor);" +
                    "}";
    private static final String SHADER_METHOD = "fragColor = addColorFilter(fragColor, " + INTENSITY_FIELD_PLACEHOLDER +
            ", " + MATRIX_FIELD_PLACEHOLDER + ");";
    private static final float MIN_INTENSITY = 0f;
    private String intensity_handler = "colorIntensity";
    private String matrix_handler = "colorMatrix";
    private float maxIntensity;
    private float currentIntensity;
    private int mIntensityHandler;
    private int mMatrixHandler;
    private float[] mMatrix;

    //max should be [0, 1]
    public ColorFilterMod(float max, float[] matrix, int handlerId) {
        intensity_handler += handlerId;
        matrix_handler += handlerId;
        maxIntensity = max;
        currentIntensity = maxIntensity;
        mMatrix = matrix;
    }

    public ColorFilterMod(Parcel in) {
        maxIntensity = in.readFloat();
        currentIntensity = in.readFloat();
        mMatrix = in.createFloatArray();
        intensity_handler = in.readString();
        matrix_handler = in.readString();

    }

    @Override
    public String getShaderField() {
        return "uniform mat4 " + matrix_handler + ";" +
                "uniform float " + intensity_handler + ";";
    }

    @Override
    public String getShaderCode() {
        return SHADER_CODE;
    }

    @Override
    public String getShaderMethod() {
        return SHADER_METHOD.replace(INTENSITY_FIELD_PLACEHOLDER, intensity_handler)
                .replace(MATRIX_FIELD_PLACEHOLDER, matrix_handler);
    }

    @Override
    public void setMax(float... max) {
        if (max.length > 0) {
            maxIntensity = max[0];
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
        currentIntensity = intensity * (maxIntensity - MIN_INTENSITY) + MIN_INTENSITY;
    }

    @Override
    public void initHandler(int programId) {
        mIntensityHandler = GLES20.glGetUniformLocation(programId, intensity_handler);
        GLToolbox.checkGlError("glGetUniformLocation " + intensity_handler);
        if (mIntensityHandler == -1) {
            throw new RuntimeException("Could not get attrib location for " + intensity_handler);
        }
        mMatrixHandler = GLES20.glGetUniformLocation(programId, matrix_handler);
        GLToolbox.checkGlError("glGetUniformLocation " + matrix_handler);
        if (mIntensityHandler == -1) {
            throw new RuntimeException("Could not get attrib location for " + matrix_handler);
        }
    }

    @Override
    public void draw() {
        GLES20.glUniform1f(mIntensityHandler, currentIntensity);
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMatrix, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(maxIntensity);
        dest.writeFloat(currentIntensity);
        dest.writeFloatArray(mMatrix);
        dest.writeString(intensity_handler);
        dest.writeString(matrix_handler);
    }
}