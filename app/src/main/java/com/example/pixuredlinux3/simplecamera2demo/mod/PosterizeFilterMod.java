package com.example.pixuredlinux3.simplecamera2demo.mod;

/**
 * Created on 1/14/16.
 * posterizefiltermod for adding posterize modifications
 * to a surface
 */
public class PosterizeFilterMod extends SimpleUniformFilterMod {

    private static final String SHADER_HANDLER = "colorLevels";

    private static final String SHADER_CODE =
            " vec4 addPosterize(vec4 fragColor, float colorLevels) {" +
                    "   if (colorLevels <= 17.0) { " +
                    "       return floor((fragColor * colorLevels) + vec4(0.5)) / colorLevels;" +
                    "   } " +
                    "   else {" +
                    "       return fragColor;" +
                    "   }" +
                    "}";

    private static final String SHADER_METHOD = "fragColor = addPosterize(fragColor, " + FIELD_NAME + ");";

    private static final float MIN_INTENSITY = 17.5f;

    public PosterizeFilterMod(float max, int handlerId) {
        super(max, handlerId);
    }

    //Max intensity should be [1,17.5]
    @Override
    protected void init(int handlerId) {
        min_intensity = MIN_INTENSITY;
        shader_handler = SHADER_HANDLER + handlerId;
        shader_code = SHADER_CODE;
        shader_method = SHADER_METHOD;
    }
}
