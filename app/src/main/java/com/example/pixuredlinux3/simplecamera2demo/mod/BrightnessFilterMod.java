package com.example.pixuredlinux3.simplecamera2demo.mod;

/**
 * Created on 1/14/16.
 */
public class BrightnessFilterMod extends SimpleUniformFilterMod {

    private static final String SHADER_HANDLER = "brightness";

    private static final String SHADER_CODE =
            " vec4 addBrightness(vec4 fragColor, float brightness) {" +
                    "   return vec4((fragColor.rgb + vec3(brightness)), fragColor.w);" +
                    "}";

    private static final String SHADER_METHOD = "fragColor = addBrightness(fragColor, " + FIELD_NAME + ");";

    private static final float MIN_INTENSITY = 0f;

    public BrightnessFilterMod(float max, int handlerId) {
        super(max, handlerId);
    }

    //Max intensity should be [-1,1]
    @Override
    protected void init(int handlerId) {
        min_intensity = MIN_INTENSITY;
        shader_handler = SHADER_HANDLER + handlerId;
        shader_code = SHADER_CODE;
        shader_method = SHADER_METHOD;
    }

}


