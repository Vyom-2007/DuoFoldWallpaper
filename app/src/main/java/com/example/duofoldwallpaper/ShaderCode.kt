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
        uniform vec2  uGrad;        // gradient direction: (0.5, 0) vs (originX, originX + scaleX)
        uniform float uMotion;      // smoothstep(progress) for THIS display
        uniform float uFold;        // radians, 0 = open (only > 0 for inner display)
        uniform float uRadiusMax;   // scaled max blur radius based on texture size

        half4 main(vec2 p) {
            vec2 uv = uUvOffset + p * uUvScale;

            // Geometric 3D perspective warp for the moving half (inner screen)
            if (uv.x < 0.5 && uFold > 0.0001) {
                float c = cos(uFold);
                float s = sin(uFold);
                const float D  = 39.75052;   // EYE_Z - INNER_PLANE_Z
                const float FW = 15.7987;    // inner frame width
                float r = (0.5 - uv.x) * FW; // physical distance from hinge
                float depth = D / (D - r * s);
                float q = -r * c * depth;    // front-view projected x
                uv = vec2(0.5 + q / FW, 0.5 + (uv.y - 0.5) * depth);
            }

            // 1D linear ramp along X from hinge edge across panel
            float edge = (uv.x - uGrad.x) / (uGrad.y - uGrad.x);
            
            float blurG  = clamp(edge, 0.0, 1.0);
            float darkG  = clamp((edge - 0.2) / 0.8, 0.0, 1.0);
            float effect = uMotion * pow(darkG, 1.35);
            float radius = uRadiusMax * uMotion * pow(blurG, 1.35);

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
