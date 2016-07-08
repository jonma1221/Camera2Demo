package com.example.pixuredlinux3.simplecamera2demo;

import com.example.pixuredlinux3.simplecamera2demo.mod.*;
/**
 * Created on 11/4/15.
 *
 * class container to holder location of
 * program, vertex shader and fragment shader
 */
public class ProgramContainer {
    public final int mProgram;
    public final int maPositionHandle;
    public final int maTexCoordHandle;
    public final int muMVPMatrixHandle;
    public final int muSTMatrixHandle;

    //Image handles that may not be set every time
    public int mTextureHandle;

    public PixuredFilter mFilter;
    public SaturationFilterMod mSaturationMod;
    public ContrastFilterMod mContrastMod;
    public WarmthFilterMod mWarmthMod;
    public BrightnessFilterMod mBrightnessMod;
    public VignetteFilterMod mVignetteMod;

    public ProgramContainer(int program, int maPos, int maTex, int muMVP, int muST) {
        mProgram = program;
        maPositionHandle = maPos;
        maTexCoordHandle = maTex;
        muMVPMatrixHandle = muMVP;
        muSTMatrixHandle = muST;
    }
}
