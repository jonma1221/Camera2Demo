package com.example.pixuredlinux3.simplecamera2demo;

import com.example.pixuredlinux3.simplecamera2demo.mod.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created on 1/14/16.
 * Filter class for handling multiple mods and creating one comprehensive filter
 */
public class PixuredFilter {

    private static final String VIDEO_PREFIX = "#extension GL_OES_EGL_image_external : require\n";
    private static final String VIDEO_TEXTURE = "samplerExternalOES";
    private static final String IMAGE_TEXTURE = "sampler2D";
    private static final String PLACEHOLDER_MODIFIER = "MODIFIER_PLACEHOLDER";
    private static final String PLACEHOLDER_TEXTURE = "TEXTURE_HOLDER";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform TEXTURE_HOLDER sTexture;\n" +
                    " void main() {\n" +
                    "  vec4 fragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "    MODIFIER_PLACEHOLDER " +
                    "    gl_FragColor = fragColor;" +
                    "}\n";

    private ArrayList<PixFilterMod> mMods;
    private ArrayList<PixFilterMod> mAdjustMods;
    private String name;
    private float currentIntensity;

    private SaturationFilterMod mSaturationMod;
    private ContrastFilterMod mContrastMod;
    private WarmthFilterMod mWarmthMod;
    private BrightnessFilterMod mBrightnessMod;
    private VignetteFilterMod mVignetteMod;

    /**
     * Creates a com.example.pixuredlinux3.simplecamera2demo.PixuredFilter from the list of mods.
     * Note that ORDER DOES MATTER
     * @param mods
     */
    public PixuredFilter(List<PixFilterMod> mods, String name) {
        this(mods);
        this.name = name;
    }

    public PixuredFilter(List<PixFilterMod> mods) {
        mMods = new ArrayList<>();
        mMods.addAll(mods);
        mAdjustMods = new ArrayList<>();
    }

    /**
     * Adjust mods are mods that allows the user to further make
     * individual changes to the filter such as brightness and contrast
     * that are each toggled individually
     * @param mods
     */
    public void addAdjustMods(List<PixFilterMod> mods) {
        mAdjustMods.addAll(mods);
    }

    public ArrayList<PixFilterMod> getAdjustMods() {
        return mAdjustMods;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFragmentShader(boolean isVideo) {
        String code = FRAGMENT_SHADER;
        StringBuilder sb = new StringBuilder();
        if (isVideo) {
            sb.append(VIDEO_PREFIX);
        }
        String[] words = code.split("\\s+");
        HashMap<String, Boolean> methodTracker = new HashMap<>();
        for (String w : words) {
            if (w.equals(PLACEHOLDER_TEXTURE)) {
                if (isVideo) {
                    sb.append(VIDEO_TEXTURE);
                } else {
                    sb.append(IMAGE_TEXTURE);
                }
            } else if (w.equals("void")) {
                for (PixFilterMod mod : mMods) {
                    sb.append(mod.getShaderField());
                    sb.append(" ");
                }
                for (PixFilterMod mod : mAdjustMods) {
                    sb.append(mod.getShaderField());
                    sb.append(" ");
                }
                for (PixFilterMod mod : mMods) {
                    String shaderCode = mod.getShaderCode();
                    if (!methodTracker.containsKey(shaderCode)) {
                        sb.append(shaderCode);
                        sb.append(" ");
                        methodTracker.put(shaderCode, true);
                    }
                }
                for (PixFilterMod mod : mAdjustMods) {
                    String shaderCode = mod.getShaderCode();
                    if (!methodTracker.containsKey(shaderCode)) {
                        sb.append(shaderCode);
                        sb.append(" ");
                        methodTracker.put(shaderCode, true);
                    }
                }
                sb.append(w);
            } else if (w.equals(PLACEHOLDER_MODIFIER)) {
                for (PixFilterMod mod : mMods) {
                    sb.append(mod.getShaderMethod());
                    sb.append(" ");
                }
                for (PixFilterMod mod : mAdjustMods) {
                    sb.append(mod.getShaderMethod());
                    sb.append(" ");
                }
            } else {
                sb.append(w);
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    public void setIntensity(float intensity) {
        for (PixFilterMod mod : mMods) {
            mod.setIntensity(intensity);
        }
    }

    public void setHandler(int programId) {
        for (PixFilterMod mod : mMods) {
            mod.initHandler(programId);
        }
        for (PixFilterMod mod : mAdjustMods) {
            mod.initHandler(programId);
        }
    }

    public void drawMods() {
        for (PixFilterMod mod : mMods) {
            mod.draw();
        }
        for (PixFilterMod mod : mAdjustMods) {
            mod.draw();
        }
    }
}
