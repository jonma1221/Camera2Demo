package com.example.pixuredlinux3.simplecamera2demo;

import com.example.pixuredlinux3.simplecamera2demo.mod.*;
import java.util.ArrayList;

/**
 * Created on 1/15/16.
 * Filter generator to retrieve the custom pixured filters
 */
public class PixFilterGenerator {

    public static final String MOD_DEFAULT = "Default";
    public static final String MOD_GRAY = "Gray";
    public static final String MOD_SEPIA = "Sepia";
    public static final String MOD_DISRUPT = "Disrupt";
    public static final String MOD_AQUA = "Aqua";
    public static final String MOD_VIOLET = "Violet";
    public static final String MOD_NEON = "Neon";
    public static final String MOD_FOX = "Fox";
    public static final String MOD_BLIND = "Blind";
    public static final String MOD_POSTER = "Poster";

    /*Test*/
    public static final String MOD_LUMINANCE = "Luminance";

    public static ArrayList<PixuredFilter> getAllFilters() {
        ArrayList<PixuredFilter> filters = new ArrayList<>();
        filters.add(getDefault());
        filters.add(getGrayScaleFilter());
        filters.add(getSepiaFilter());
        filters.add(getDisrupt());
        filters.add(getAquatic());
        filters.add(getViolet());
        filters.add(getMeganFox());
        filters.add(getNeon());
        filters.add(getBlinding());
        filters.add(getPosterizeFilter());

        /*Test*/
        filters.add(getLuminance());
        return filters;
    }

    public static PixuredFilter getFilterFromName(String name) {
        if (MOD_DEFAULT.equals(name)) {
            return getDefault();
        } else if (MOD_DISRUPT.equals(name)) {
            return getDisrupt();
        } else if (MOD_AQUA.equals(name)) {
            return getAquatic();
        } else if (MOD_BLIND.equals(name)) {
            return getBlinding();
        } else if (MOD_FOX.equals(name)) {
            return getMeganFox();
        } else if (MOD_GRAY.equals(name)) {
            return getGrayScaleFilter();
        } else if (MOD_POSTER.equals(name)) {
            return getPosterizeFilter();
        } else if (MOD_NEON.equals(name)) {
            return getNeon();
        } else if (MOD_VIOLET.equals(name)) {
            return getViolet();
        } else if (MOD_SEPIA.equals(name)) {
            return getSepiaFilter();
        }

        return null;
    }

    /*Test*/
    public static PixuredFilter getLuminance(){
        ArrayList<PixFilterMod> mods = new ArrayList<>();
        float matrix [] = new float[] {
                0.3086f,   0.3086f,   0.3086f,   0.0f,
                0.6094f,   0.6094f,   0.6094f,   0.0f,
                0.0820f,   0.0820f,   0.0820f,   0.0f,
                0.0f,    0.0f,    0.0f,    1.0f,
        };
        ColorFilterMod colorFilter = new ColorFilterMod(1, matrix, 0);
        mods.add(colorFilter);

        return new PixuredFilter((ArrayList) mods, MOD_LUMINANCE);
    }

    public static PixuredFilter getDefault() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();
        return new PixuredFilter((ArrayList) mods, MOD_DEFAULT);
    }

    public static PixuredFilter getDisrupt() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();
        ContrastFilterMod contrast = new ContrastFilterMod(1.5f, 0);
        mods.add(contrast);

        PosterizeFilterMod posterize = new PosterizeFilterMod(5, 0);
        mods.add(posterize);

        WarmthFilterMod warmth = new WarmthFilterMod(.25f, 0);
        mods.add(warmth);

        return new PixuredFilter((ArrayList) mods, MOD_DISRUPT);

    }

    public static PixuredFilter getBlinding() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();

        BrightnessFilterMod brightness = new BrightnessFilterMod(.3f, 0);
        mods.add(brightness);

        SaturationFilterMod saturation = new SaturationFilterMod(.8f, 0);
        mods.add(saturation);

        return new PixuredFilter((ArrayList) mods, MOD_BLIND);

    }

    public static PixuredFilter getAquatic() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();

        BrightnessFilterMod brightness = new BrightnessFilterMod(-.2f, 0);
        mods.add(brightness);

        SaturationFilterMod saturation = new SaturationFilterMod(.8f, 0);
        mods.add(saturation);

        WarmthFilterMod warmth = new WarmthFilterMod(-.25f, 0);
        mods.add(warmth);

        return new PixuredFilter((ArrayList) mods, MOD_AQUA);

    }

    public static PixuredFilter getMeganFox() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();
        ContrastFilterMod contrast = new ContrastFilterMod(1.2f, 0);
        mods.add(contrast);

        WarmthFilterMod warmth = new WarmthFilterMod(.2f, 0);
        mods.add(warmth);

        VignetteFilterMod vignette = new VignetteFilterMod(.5f, 1f, 0);
        mods.add(vignette);

        return new PixuredFilter((ArrayList) mods, MOD_FOX);

    }

    public static PixuredFilter getNeon() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();
        float[] matrix = new float[]{
                1.f, 0f, -10f, 0.0f,
                -2f, 3f, 1f, 0.0f,
                6f, 1f, -1f, 0.0f,
                0f, 0f, 0f, 1.0f
        };

        ColorFilterMod colorFilter = new ColorFilterMod(1, matrix, 0);
        mods.add(colorFilter);

        BrightnessFilterMod brightness = new BrightnessFilterMod(-.2f, 0);
        mods.add(brightness);

        return new PixuredFilter((ArrayList) mods, MOD_NEON);
    }

    public static PixuredFilter getViolet() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();
        float[] matrix = new float[]{
                1.f, 1f, 1f, 0.0f,
                1f, -.15f, 1f, 0.0f,
                1f, 1f, 1f, 0.0f,
                0f, 0f, 0f, 1.0f
        };

        ColorFilterMod colorFilter = new ColorFilterMod(1, matrix, 0);
        mods.add(colorFilter);

        BrightnessFilterMod brightness = new BrightnessFilterMod(-.2f, 0);
        mods.add(brightness);

        return new PixuredFilter((ArrayList) mods, MOD_VIOLET);
    }

    public static PixuredFilter getSepiaFilter() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();
        float[] matrix = new float[]{
                0.3588f, 0.7044f, 0.1368f, 0.0f,
                0.2990f, 0.5870f, 0.1140f, 0.0f,
                0.2392f, 0.4696f, 0.0912f, 0.0f,
                0f, 0f, 0f, 1.0f
        };

        ColorFilterMod colorFilter = new ColorFilterMod(1, matrix, 0);
        mods.add(colorFilter);
        return new PixuredFilter((ArrayList) mods, MOD_SEPIA);
    }

    public static PixuredFilter getGrayScaleFilter() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();
        float[] matrix = new float[]{
                .5f, .5f, .5f, 0.0f,
                .5f, .5f, .5f, 0.0f,
                .5f, .5f, .5f, 0.0f,
                0f, 0f, 0f, 1.0f
        };

        ColorFilterMod colorFilter = new ColorFilterMod(1, matrix, 0);
        mods.add(colorFilter);
        return new PixuredFilter((ArrayList) mods, MOD_GRAY);
    }

    public static PixuredFilter getPosterizeFilter() {
        ArrayList<PixFilterMod> mods = new ArrayList<>();

        PosterizeFilterMod colorFilter = new PosterizeFilterMod(2.5f, 0);
        mods.add(colorFilter);
        return new PixuredFilter((ArrayList) mods, MOD_POSTER);
    }

}
