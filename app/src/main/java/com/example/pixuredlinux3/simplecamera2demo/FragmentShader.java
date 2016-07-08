package com.example.pixuredlinux3.simplecamera2demo;

/**
 * Created on 11/3/15.
 * com.example.pixuredlinux3.simplecamera2demo.FragmentShader holds all the the fragment_shader codes
 * Fragment shader code from: https://github.com/CyberAgent/android-gpuimage/tree/master/library/src/jp/co/cyberagent/android/gpuimage
 */
public class FragmentShader {

    private static final String VIDEO_PREFIX = "#extension GL_OES_EGL_image_external : require\n";
    private static final String VIDEO_TEXTURE = "samplerExternalOES";
    private static final String IMAGE_TEXTURE = "sampler2D";
    private static final String PLACEHOLDER_MODIFIER = "MODIFIER_PLACEHOLDER";
    private static final String PLACEHOLDER_TEXTURE = "TEXTURE_HOLDER";

    public static String getVideoShader(SHADER shader) {
        String code = shader.getCode();
        StringBuilder sb = new StringBuilder();
        sb.append(VIDEO_PREFIX);
        String[] words = code.split("\\s+");
        for (String w : words) {
            if (w.equals(PLACEHOLDER_TEXTURE)) {
                sb.append(VIDEO_TEXTURE);
            } else if (w.equals(PLACEHOLDER_MODIFIER)) {
            } else {
                sb.append(w);
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String getImageShader(SHADER shader) {
        String code = shader.getCode();
        StringBuilder sb = new StringBuilder();
        String[] words = code.split("\\s+");
        for (String w : words) {
            if (w.equals(PLACEHOLDER_TEXTURE)) {
                sb.append(IMAGE_TEXTURE);
            } else if (w.equals(PLACEHOLDER_MODIFIER)) {
            } else {
                sb.append(w);
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String getImageShaderWithModifier(SHADER shader) {
        String code = shader.getCode();
        StringBuilder sb = new StringBuilder();
        String[] words = code.split("\\s+");
        for (String w : words) {
            if (w.equals(PLACEHOLDER_TEXTURE)) {
                sb.append(IMAGE_TEXTURE);
            } else if (w.equals("void")) {
                for (MODIFIERS mod : MODIFIERS.values()) {
                    sb.append(mod.code);
                    sb.append(" ");
                }
                sb.append(w);
            } else if (w.equals(PLACEHOLDER_MODIFIER)) {
                for (MODIFIERS mod : MODIFIERS.values()) {
                    sb.append(mod.getMethod());
                    sb.append(" ");
                }
            } else {
                sb.append(w);
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    public enum MODIFIERS {
        /**
         * Brightness is set from -1 to 1, default 0
         */
        BRIGHTNESS(
                " uniform float brightness;" +
                        " vec4 addBrightness(vec4 fragColor) {" +
                        "   return vec4((fragColor.rgb + vec3(brightness)), fragColor.w);" +
                        "}"
                , "fragColor = addBrightness(fragColor);"
                , "Brightness"
        ),

        /**
         * Saturation set from 0 to 2, default 1
         */
        SATURATION(
                " uniform float saturation;" +
                        " const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);" +
                        " vec4 addSaturation(vec4 fragColor) {" +
                        "    float luminance = dot(fragColor.rgb, luminanceWeighting);" +
                        "    lowp vec3 greyScaleColor = vec3(luminance);" +
                        "    return vec4(mix(greyScaleColor, fragColor.rgb, saturation), fragColor.w);" +
                        " }"
                , "fragColor = addSaturation(fragColor);"
                , "Saturation"),

        /**
         * Contrast set from 0 to 4, default 1
         * Visually adjusted from 1 to 2, default 1
         */
        CONTRAST(
                " uniform float contrast;" +
                        " vec4 addContrast(vec4 fragColor) {" +
                        "    return vec4(((fragColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), fragColor.w);" +
                        " }"
                , "fragColor = addContrast(fragColor);"
                , "Contrast"),

        /**
         * Warmth set from -1 to 1, default 0
         */
        WARMTH(
                "uniform float temperature;" +
                        "const float tint = 0.0;" +
                        "const lowp vec3 warmFilter = vec3(0.93, 0.54, 0.0);" +
                        "const mediump mat3 RGBtoYIQ = mat3(0.299, 0.587, 0.114, 0.596, -0.274, -0.322, 0.212, -0.523, 0.311);\n" +
                        "const mediump mat3 YIQtoRGB = mat3(1.0, 0.956, 0.621, 1.0, -0.272, -0.647, 1.0, -1.105, 1.702);\n" +
                        " vec4 addWarmth(vec4 fragColor) {" +
                        "	mediump vec3 yiq = RGBtoYIQ * fragColor.rgb;" +
                        "	yiq.b = clamp(yiq.b + tint*0.5226*0.1, -0.5226, 0.5226);" +
                        "	lowp vec3 rgb = YIQtoRGB * yiq;" +
                        "	lowp vec3 processed = vec3(" +
                        "		(rgb.r < 0.5 ? (2.0 * rgb.r * warmFilter.r) : (1.0 - 2.0 * (1.0 - rgb.r) * (1.0 - warmFilter.r)))," +
                        "		(rgb.g < 0.5 ? (2.0 * rgb.g * warmFilter.g) : (1.0 - 2.0 * (1.0 - rgb.g) * (1.0 - warmFilter.g)))," +
                        "		(rgb.b < 0.5 ? (2.0 * rgb.b * warmFilter.b) : (1.0 - 2.0 * (1.0 - rgb.b) * (1.0 - warmFilter.b))));" +
                        "	return vec4(mix(rgb, processed, temperature), fragColor.a);" +
                        " }"
                , "fragColor = addWarmth(fragColor);"
                , "Warmth"),

        /**
         * VIGNETTE set from set vignetteStart and vignetteEnd, default start .75, end .5
         */
        VIGNETTE(
                " const vec2 vignetteCenter = vec2(0.5, 0.5);" +
                        " const vec3 vignetteColor = vec3(0.0, 0.0, 0.0);" +
                        " uniform mediump float vignetteStart;" +
                        " uniform mediump float vignetteEnd;" +
                        " vec4 addVignette(vec4 fragColor) {" +
                        "     lowp vec3 rgb = fragColor.rgb;" +
                        "     lowp float d = distance(vTextureCoord, vec2(vignetteCenter.x, vignetteCenter.y));" +
                        "     lowp float percent = smoothstep(vignetteStart, vignetteEnd, d);" +
                        "     return vec4(mix(rgb.x, vignetteColor.x, percent), mix(rgb.y, vignetteColor.y, percent), mix(rgb.z, vignetteColor.z, percent), 1.0);" +
                        " }"
                , "fragColor = addVignette(fragColor);"
                , "Vignette"),

        /**
         * Highlightshadow
         */
        HIGHLIGHTSHADOW(
                " uniform lowp float shadows;" +
                        " uniform lowp float highlights;" +
                        " const mediump vec3 hsLuminanceWeighting = vec3(0.3, 0.3, 0.3);" +
                        " vec4 addhighlightShadow(vec4 fragColor) {" +
                        " 	mediump float luminance = dot(fragColor.rgb, hsLuminanceWeighting);\n" +
                        " 	mediump float shadow = clamp((pow(luminance, 1.0/(shadows+1.0)) + (-0.76)*pow(luminance, 2.0/(shadows+1.0))) - luminance, 0.0, 1.0);\n" +
                        " 	mediump float highlight = clamp((1.0 - (pow(1.0-luminance, 1.0/(2.0-highlights)) + (-0.8)*pow(1.0-luminance, 2.0/(2.0-highlights)))) - luminance, -1.0, 0.0);\n" +
                        " 	lowp vec3 result = vec3(0.0, 0.0, 0.0) + ((luminance + shadow + highlight) - 0.0) * ((fragColor.rgb - vec3(0.0, 0.0, 0.0))/(luminance - 0.0));\n" +
                        " 	return vec4(result.rgb, fragColor.a);" +
                        " }"
                , "//fragColor = addhighlightShadow(fragColor);\n"
                , "highlightShadow");

        private String code;
        private String nickname;
        private String method;

        MODIFIERS(String code, String method, String nickname) {
            this.code = code;
            this.method = method;
            this.nickname = nickname;
        }

        public static MODIFIERS fromName(String name) {
            MODIFIERS result = null;
            for (MODIFIERS mod : MODIFIERS.values()) {
                if (mod.name().equals(name)) {
                    result = mod;
                    break;
                }
            }
            return result;
        }

        public String getCode() {
            return code;
        }

        public String getMethod() {
            return method;
        }

        public String getNickname() {
            return nickname;
        }
    }

    public enum SHADER {
        DEFAULT(
                "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform TEXTURE_HOLDER sTexture;\n" +
                        " void main() {\n" +
                        "  vec4 fragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "    MODIFIER_PLACEHOLDER " +
                        "    gl_FragColor = fragColor;" +
                        "}\n"
                , "Default"),
        GRAY_SCALE(
                "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform TEXTURE_HOLDER sTexture;\n" +
                        "const mediump vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +
                        " void main() {\n" +
                        "  vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
                        "  float luminance = dot(textureColor.rgb, W);\n" +
                        "  vec4 fragColor = vec4(vec3(luminance), textureColor.a);\n" +
                        "    MODIFIER_PLACEHOLDER " +
                        "    gl_FragColor = fragColor;" +
                        "}\n"
                , "Oldie"),
        /**
         * setting colorLevels is required recommended levels (1-15) (lower levels more intense)
         * <mProgramContainer/>
         */
        POSTERIZE(
                "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform TEXTURE_HOLDER sTexture;\n" +
                        "const mediump float colorLevels = 2.5;\n" +
                        " void main() {\n" +
                        "  vec4 textureColor = texture2D(sTexture, vTextureCoord);" +
                        "  vec4 fragColor = floor((textureColor * colorLevels) + vec4(0.5)) / colorLevels;" +
                        "    MODIFIER_PLACEHOLDER " +
                        "    gl_FragColor = fragColor;" +
                        "}\n"
                , "Warhol"),
        INVERTED(
                "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform TEXTURE_HOLDER sTexture;\n" +
                        "uniform float uResS;" +
                        "uniform float uResT;" +
                        " void main() {\n" +
                        "  vec2 onePixel = vec2(1.0 / uResS, 1.0 / uResT);\n" +
                        "  float T = 1.0;\n" +
                        "  vec2 st = vTextureCoord.st;\n" +
                        "  vec4 textureColor = texture2D(sTexture, st);" +
                        "  vec3 irgb = textureColor.rgb;\n" +
                        "  vec3 neg = vec3(1., 1., 1.)-irgb;\n" +
                        "  vec4 fragColor = vec4(mix(irgb, neg, T), 1.);\n" +
                        "    MODIFIER_PLACEHOLDER " +
                        "    gl_FragColor = fragColor;" +
                        "}\n"
                , "Cannabis"),
        /**
         * center is from 0.0 to 1.0
         */
        GLASS_SPHERE(
                "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform TEXTURE_HOLDER sTexture;\n" +
                        "const mediump vec2 center = vec2(0.5, 0.5);" +
                        "const mediump float radius = 0.5;" +
                        "const mediump float aspectRatio = 1.0;" +
                        "const mediump float refractiveIndex = 0.5;" +
                        "const mediump vec3 lightPosition = vec3(-0.5, 0.5, 1.0);" +
                        "const mediump vec3 ambientLightPosition = vec3(0.0, 0.0, 1.0);" +
                        " void main() {\n" +
                        "   vec2 vTextureCoordToUse = vec2(vTextureCoord.x, (vTextureCoord.y * aspectRatio + 0.5 - 0.5 * aspectRatio));" +
                        "   float distanceFromCenter = distance(center, vTextureCoordToUse);" +
                        "   lowp float checkForPresenceWithinSphere = step(distanceFromCenter, radius);" +
                        "   distanceFromCenter = distanceFromCenter / radius;" +
                        "   float normalizedDepth = radius * sqrt(1.0 - distanceFromCenter * distanceFromCenter);" +
                        "   vec3 sphereNormal = normalize(vec3(vTextureCoordToUse - center, normalizedDepth));" +
                        "   vec3 refractedVector = 2.0 * refract(vec3(0.0, 0.0, -1.0), sphereNormal, refractiveIndex);" +
                        "   refractedVector.xy = -refractedVector.xy;" +
                        "   vec4 textureColor = texture2D(sTexture, (refractedVector.xy + 1.0) * 0.5);" +
                        "   vec3 finalSphereColor = textureColor.rgb;" +
                        "   float lightingIntensity = 2.5 * (1.0 - pow(clamp(dot(ambientLightPosition, sphereNormal), 0.0, 1.0), 0.25));" +
                        "   finalSphereColor += lightingIntensity;" +
                        "   lightingIntensity  = clamp(dot(normalize(lightPosition), sphereNormal), 0.0, 1.0);" +
                        "   lightingIntensity  = pow(lightingIntensity, 15.0);" +
                        "   finalSphereColor += vec3(0.8, 0.8, 0.8) * lightingIntensity;" +
                        "   vec4 fragColor = vec4(finalSphereColor, 1.0) * checkForPresenceWithinSphere;" +
                        "    MODIFIER_PLACEHOLDER " +
                        "    gl_FragColor = fragColor;" +
                        "}\n"
                , "Pet fish"),
        SEPIA(
                "precision mediump float;\n" +
                        "varying mediump vec2 vTextureCoord;\n" +
                        "uniform TEXTURE_HOLDER sTexture;\n" +
                        "const mediump mat4 colorMatrix = " +
                        "mat4(vec4(0.3588, 0.7044, 0.1368, 0.0)," +
                        "vec4(0.2990, 0.5870, 0.1140, 0.0)," +
                        "vec4(0.2392, 0.4696, 0.0912 ,0.0)," +
                        "vec4(0,0,0,1.0));" +
                        "const lowp float intensity = 1.0;" +
                        " void main()\n" +
                        "{\n" +
                        "    vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
                        "    vec4 outputColor = textureColor * colorMatrix;\n" +
                        "    vec4 fragColor = (intensity * outputColor) + ((1.0 - intensity) * textureColor);" +
                        "    MODIFIER_PLACEHOLDER " +
                        "    gl_FragColor = fragColor;" +
                        "}"
                , "Grandma"),

        /**
         * distance, slope best between -.3 and .3
         */
        HAZE(
                "precision mediump float;\n" +
                        "varying mediump vec2 vTextureCoord;\n" +
                        "uniform TEXTURE_HOLDER sTexture;\n" +
                        "const lowp float hazeDistance = -.3;\n" +
                        "const mediump float slope = 0.3;\n" +
                        " void main()\n" +
                        "{\n" +
                        "	 mediump vec4 color = vec4(1.0);" +
                        "	 mediump float  d = vTextureCoord.y * slope  +  hazeDistance; \n" +
                        "	 mediump vec4 c = texture2D(sTexture, vTextureCoord);\n" +
                        "	 mediump vec4 fragColor = (c - d * color) / (1.0 -d);\n" +
                        "    MODIFIER_PLACEHOLDER " +
                        "	 gl_FragColor = fragColor;\n" +
                        "}"
                , "Chemical"),
        /**
         * distance, slope best between -.3 and .3
         */

        TEST1(
                "precision mediump float;\n" +
                        "varying mediump vec2 vTextureCoord;\n" +
                        " uniform sampler2D inputImageTexture;\n" +
                        " const lowp float saturationTest = .2;\n" +
                        " const mediump vec3 testWeighting = vec3(0.2125, 0.7154, 0.0721);" +
                        " void main()\n" +
                        " {\n" +
                        "    lowp vec4 textureColor = texture2D(inputImageTexture, vTextureCoord);\n" +
                        "    lowp float luminance = dot(textureColor.rgb, testWeighting);\n" +
                        "    lowp vec3 greyScaleColor = vec3(luminance);\n" +
                        "    \n" +
                        "    vec4 fragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturationTest), textureColor.w);\n" +
                        "    MODIFIER_PLACEHOLDER " +
                        "    gl_FragColor = fragColor;\n" +
                        "}"
                , "TEST1"),

        TEST2(
                "precision mediump float;\n" +
                        "varying mediump vec2 vTextureCoord;\n" +
                        " uniform sampler2D inputImageTexture;\n" +
                        " const lowp float saturationTest = .4;\n" +
                        " const mediump vec3 testWeighting = vec3(0.2125, 0.7154, 0.0721);" +
                        " void main()\n" +
                        " {\n" +
                        "    lowp vec4 textureColor = texture2D(inputImageTexture, vTextureCoord);\n" +
                        "    lowp float luminance = dot(textureColor.rgb, testWeighting);\n" +
                        "    lowp vec3 greyScaleColor = vec3(luminance);\n" +
                        "    \n" +
                        "    vec4 fragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturationTest), textureColor.w);\n" +
                        "    MODIFIER_PLACEHOLDER " +
                        "    gl_FragColor = fragColor;\n" +
                        "}"
                , "TEST2");

        private String code;
        private String nickname;

        SHADER(String code, String nickname) {
            this.code = code;
            this.nickname = nickname;
        }

        public static SHADER fromName(String name) {
            SHADER result = DEFAULT;
            for (SHADER shader : SHADER.values()) {
                if (shader.name().equals(name)) {
                    result = shader;
                    break;
                }
            }
            return result;
        }

        public String getCode() {
            return code;
        }

        public String getNickname() {
            return nickname;
        }
    }

}
