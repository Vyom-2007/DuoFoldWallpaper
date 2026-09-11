package com.example.duofoldwallpaper

/**
 * AGSL (Android Graphics Shading Language, API 33+) port of the web demo's
 * screenColor() fragment shader: a progressive blur + darken "dissolve"
 * applied outward from a gradient anchor line, driven by fold motion.
 */
object ShaderCode {

    // uGrad conventions (matching the web demo):
    //   inner display: (0.5, 0.0)
    //   outer display: (0.0, 1.0)
    const val DUO_SHADER = """
        uniform shader uImage;      // canonical wallpaper (2670x1878-style layout)
        uniform vec2  uUvOffset;    // per-frame affine: canonical UV at output pixel (0,0)
        uniform vec2  uUvScale;     // canonical UV step per output pixel
        uniform vec2  uPixel;       // 1 / canonical image size
        uniform vec2  uGrad;        // gradient direction: (0.5, 0) vs (0, 1)
        uniform float uMotion;      // smoothstep(progress) for THIS display

        half4 main(vec2 p) {
            vec2 uv = uUvOffset + p * uUvScale;
            float edge = max(0.0, 1.0 - length((uv - uGrad) * vec2(2.0, 1.0)));
            
            float blurG  = clamp(edge, 0.0, 1.0);
            float darkG  = clamp((edge - 0.2) / 0.8, 0.0, 1.0);
            float effect = uMotion * pow(darkG, 1.35);
            float radius = 72.0 * uMotion * pow(blurG, 1.35);

            vec2 aa  = uPixel * 0.5;
            vec2 cov = smoothstep(-aa, aa, uv)
                     * (1.0 - smoothstep(vec2(1.0) - aa, vec2(1.0) + aa, uv));
            
            // AGSL eval expects pixel coords, not normalized UVs
            vec2 sampleCoord = uv / uPixel;
            half3 color = uImage.eval(sampleCoord).rgb * cov.x * cov.y;

            if (radius > 0.5) {
                vec2 fp = max(aa, uPixel * radius * 0.75);
                color = half3(0.0);
                for (int y = -2; y <= 2; y++) {
                    for (int x = -2; x <= 2; x++) {
                        float wx = (x == 0) ? 6.0 : (abs(float(x)) == 1.0 ? 4.0 : 1.0);
                        float wy = (y == 0) ? 6.0 : (abs(float(y)) == 1.0 ? 4.0 : 1.0);
                        vec2 suv = uv + vec2(float(x), float(y)) * uPixel * radius;
                        vec2 c = smoothstep(-fp, fp, suv)
                               * (1.0 - smoothstep(vec2(1.0) - fp, vec2(1.0) + fp, suv));
                        color += uImage.eval(suv / uPixel).rgb * c.x * c.y * wx * wy / 256.0;
                    }
                }
            }
            return half4(color * (1.0 - min(1.0, effect * 2.0)), 1.0);
        }
    """
}
