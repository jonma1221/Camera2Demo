package com.example.pixuredlinux3.simplecamera2demo.mod;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 1/14/16.
 * parent object of all filters
 */
public class PixFilterMod implements Parcelable {

    public PixFilterMod(){}

    /**
     * Returns the c method code for the filter
     */
    public String getShaderCode() {
        return null;
    }

    /**
     * Returns the shaderField
     */
    public String getShaderField() {
        return null;
    }

    /**
     * Returns the c code for the filter method
     */
    public String getShaderMethod() {
        return null;
    }

    /**
     * Sets the max value of this com.example.pixuredlinux3.simplecamera2demo.mod
     * @param max
     */
    public void setMax(float... max) {
    }

    public void setRawIntensity(float... intensity) {
    }

    /**
     * Sets the intensity range[0,1] with
     * 0 being default with 1 at max
     * @param intensity
     */
    public void setIntensity(float intensity){}

//    /**
//     * gets the intensity range[0,1] with
//     * 0 being default with 1 at max
//     * @param intensity
//     */
//    void getIntensity(float intensity);

    /**
     * inits and retrieve the handlers given a valid program id.
     * this is typically called when the OpenGL surface is first created
     * @param programId
     */
    public void initHandler(int programId){}

    /**
     * draws in onDrawFrame of an opengl surface with
     * the set values.
     * can ONLY be called after initHandler is called (and init properly)
     */
    public void draw(){}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public PixFilterMod(Parcel in) {
    }

    public static final Creator<PixFilterMod> CREATOR = new Creator<PixFilterMod>() {
        @Override
        public PixFilterMod createFromParcel(Parcel in) {
            return new PixFilterMod(in);
        }

        @Override
        public PixFilterMod[] newArray(int size) {
            return new PixFilterMod[size];
        }
    };
    
    
}
