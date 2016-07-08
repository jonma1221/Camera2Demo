package com.example.pixuredlinux3.simplecamera2demo.mod;

/**
 * Created on 1/14/16.
 */
public class WarmthFilterMod extends SimpleUniformFilterMod {

    private static final String SHADER_HANDLER = "temperature";

    private static final String SHADER_CODE =
            "const float tint = 0.0;" +
                    "const lowp vec3 warmFilter = vec3(0.93, 0.54, 0.0);" +
                    "const mediump mat3 RGBtoYIQ = mat3(0.299, 0.587, 0.114, 0.596, -0.274, -0.322, 0.212, -0.523, 0.311);\n" +
                    "const mediump mat3 YIQtoRGB = mat3(1.0, 0.956, 0.621, 1.0, -0.272, -0.647, 1.0, -1.105, 1.702);\n" +
                    " vec4 addWarmth(vec4 fragColor, float temperature) {" +
                    "	mediump vec3 yiq = RGBtoYIQ * fragColor.rgb;" +
                    "	yiq.b = clamp(yiq.b + tint*0.5226*0.1, -0.5226, 0.5226);" +
                    "	lowp vec3 rgb = YIQtoRGB * yiq;" +
                    "	lowp vec3 processed = vec3(" +
                    "		(rgb.r < 0.5 ? (2.0 * rgb.r * warmFilter.r) : (1.0 - 2.0 * (1.0 - rgb.r) * (1.0 - warmFilter.r)))," +
                    "		(rgb.g < 0.5 ? (2.0 * rgb.g * warmFilter.g) : (1.0 - 2.0 * (1.0 - rgb.g) * (1.0 - warmFilter.g)))," +
                    "		(rgb.b < 0.5 ? (2.0 * rgb.b * warmFilter.b) : (1.0 - 2.0 * (1.0 - rgb.b) * (1.0 - warmFilter.b))));" +
                    "	return vec4(mix(rgb, processed, temperature), fragColor.a);" +
                    " }";

    private static final String SHADER_METHOD = "fragColor = addWarmth(fragColor, " + FIELD_NAME + ");";

    private static final float MIN_INTENSITY = 0f;

    public WarmthFilterMod(float max, int handlerId) {
        super(max, handlerId);
    }

    //max intensity should be [-1, 1]
    @Override
    protected void init(int handlerId) {
        min_intensity = MIN_INTENSITY;
        shader_handler = SHADER_HANDLER + handlerId;
        shader_code = SHADER_CODE;
        shader_method = SHADER_METHOD;
    }
}
