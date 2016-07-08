package com.example.pixuredlinux3.simplecamera2demo;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.pixuredlinux3.simplecamera2demo.mod.PixFilterMod;
import java.util.ArrayList;

/**
 * Created on 12/22/15.
 * Detail class to hold all the information pertaining a fragment shader
 * and its possible modifications
 */
public class FragmentShaderDetail implements Parcelable {

    public static final Creator<FragmentShaderDetail> CREATOR = new Creator<FragmentShaderDetail>() {
        @Override
        public FragmentShaderDetail createFromParcel(Parcel in) {
            return new FragmentShaderDetail(in);
        }

        @Override
        public FragmentShaderDetail[] newArray(int size) {
            return new FragmentShaderDetail[size];
        }
    };
    private ArrayList<PixFilterMod> mods = new ArrayList<>();
    private int rotation;
    private float brightness;
    private float saturation;
    private float contrast;
    private float temp;
    private float vignetteStart;
    private float vignetteEnd;
    private float highlight;
    private float shadow;

    public FragmentShaderDetail() {
        rotation = 0;
        brightness = 0;
        saturation = 1;
        contrast = 1;
        temp = 0;
        vignetteStart = 0.5f;
        vignetteEnd = 1f;
        highlight = 1;
        shadow = 0;
    }

    protected FragmentShaderDetail(Parcel in) {
        in.readTypedList(mods, PixFilterMod.CREATOR);
        rotation = in.readInt();
        brightness = in.readFloat();
        saturation = in.readFloat();
        contrast = in.readFloat();
        temp = in.readFloat();
        vignetteStart = in.readFloat();
        vignetteEnd = in.readFloat();
        highlight = in.readFloat();
        shadow = in.readFloat();
    }

    public ArrayList<PixFilterMod> getMods() {
        return mods;
    }

    public void setMods(ArrayList<PixFilterMod> mods) {
        this.mods = mods;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public float getContrast() {
        return contrast;
    }

    public void setContrast(float contrast) {
        this.contrast = contrast;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getVignetteStart() {
        return vignetteStart;
    }

    public void setVignetteStart(float vignetteStart) {
        this.vignetteStart = vignetteStart;
    }

    public float getVignetteEnd() {
        return vignetteEnd;
    }

    public void setVignetteEnd(float vignetteEnd) {
        this.vignetteEnd = vignetteEnd;
    }

    public float getHighlight() {
        return highlight;
    }

    public void setHighlight(float highlight) {
        this.highlight = highlight;
    }

    public float getShadow() {
        return shadow;
    }

    public void setShadow(float shadow) {
        this.shadow = shadow;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mods);
        dest.writeInt(rotation);
        dest.writeFloat(brightness);
        dest.writeFloat(saturation);
        dest.writeFloat(contrast);
        dest.writeFloat(temp);
        dest.writeFloat(vignetteStart);
        dest.writeFloat(vignetteEnd);
        dest.writeFloat(highlight);
        dest.writeFloat(shadow);
    }
}
