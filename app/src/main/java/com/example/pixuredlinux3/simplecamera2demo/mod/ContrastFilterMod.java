package com.example.pixuredlinux3.simplecamera2demo.mod;

/**
 * Created on 1/14/16.
 */
public class ContrastFilterMod extends SimpleUniformFilterMod {

    private static final String SHADER_HANDLER = "contrast";

    private static final String SHADER_CODE =
            " vec4 addContrast(vec4 fragColor, float contrast) {" +
                    "    return vec4(((fragColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), fragColor.w);" +
                    " }";

    private static final String SHADER_METHOD = "fragColor = addContrast(fragColor, " + FIELD_NAME + ");";

    private static final float MIN_INTENSITY = 1f;

    public ContrastFilterMod(float max, int handlerId) {
        super(max, handlerId);
    }

    //Max intensity should be [0.5, 2]
    @Override
    protected void init(int handlerId) {
        min_intensity = MIN_INTENSITY;
        shader_handler = SHADER_HANDLER + handlerId;
        shader_code = SHADER_CODE;
        shader_method = SHADER_METHOD;
    }

}
