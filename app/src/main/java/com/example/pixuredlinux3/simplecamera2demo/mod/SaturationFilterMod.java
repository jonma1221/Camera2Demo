package com.example.pixuredlinux3.simplecamera2demo.mod;

/**
 * Created on 1/14/16.
 */
public class SaturationFilterMod extends SimpleUniformFilterMod {

    private static final String SHADER_HANDLER = "saturation";

    private static final String SHADER_CODE =
            " const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);" +
                    " vec4 addSaturation(vec4 fragColor, float saturation) {" +
                    "    float luminance = dot(fragColor.rgb, luminanceWeighting);" +
                    "    lowp vec3 greyScaleColor = vec3(luminance);" +
                    "    return vec4(mix(greyScaleColor, fragColor.rgb, saturation), fragColor.w);" +
                    " }";

    private static final String SHADER_METHOD = "fragColor = addSaturation(fragColor, " + FIELD_NAME + ");";

    private static final float MIN_INTENSITY = 1f;

    public SaturationFilterMod(float max, int handlerId) {
        super(max, handlerId);
    }

    //Max intensity should be [0, 2]
    @Override
    protected void init(int handlerId) {
        min_intensity = MIN_INTENSITY;
        shader_handler = SHADER_HANDLER + handlerId;
        shader_code = SHADER_CODE;
        shader_method = SHADER_METHOD;
    }

}
