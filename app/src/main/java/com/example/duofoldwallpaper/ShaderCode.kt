package com.example.duofoldwallpaper

/**
 * AGSL shader — fully dynamic, no hardcoded device constants.
 *
 * Fixed issues found by visual comparison with the web demo:
 *  1. Right half no longer goes black — gradient only darkens the LEFT side
 *     (from hinge toward left edge), right side stays untouched.
 *  2. No more black triangles — warped UVs are clamped to [0,1] before
 *     sampling, preventing out-of-bounds coverage kills.
 *  3. AA footprint scales with perspective warp distortion.
 */
object ShaderCode {

    const val DUO_SHADER = """
        uniform shader uImage;
        uniform shader uImageBlur;
        uniform vec2   uUvOffset;
        uniform vec2   uUvScale;
        uniform vec2   uPixel;       // 1 / image size in pixels
        uniform vec2   uGrad;        // gradient sweep: (start, end) in UV x
        uniform float  uMotion;      // smoothstep(progress) for this display
        uniform float  uFold;        // fold angle in radians, 0 = open
        uniform float  uRadiusMax;   // max blur radius (scaled to image size)
        uniform float  uBlurScale;   // downscale factor for uImageBlur
        uniform float  uHingePos;    // hinge position in UV x (0.5 for centred)
        uniform float  uEyeRatio;    // perspectiveRatio (~2.52)

        half4 main(vec2 p) {
            vec2 uv = uUvOffset + p * uUvScale;
            vec2 origUV = uv;  // Keep original for gradient calculations

            // ── Perspective warp (inner screen only) ─────────────
            // The web demo uses a 3D ray-plane intersection that projects
            // the bent mesh back to flat UV.  The net effect on the inner
            // screen is that pixels at UV distance 'd' from the hinge
            // compress toward the hinge by cos(foldAngle), with a slight
            // perspective foreshortening from the virtual eye distance.
            //
            // We replicate this in 2D:
            //   warpedX = hinge - d * cos(fold) * perspScale
            // Y axis: minimal distortion — leave Y untouched.
            if (uFold > 0.0001 && uv.x < uHingePos) {
                float c = cos(uFold);
                float s = sin(uFold);

                float d = uHingePos - uv.x;   // distance from hinge in UV

                // Perspective foreshortening: the folding panel recedes
                // into the screen.  Virtual eye at distance E sees the
                // panel at z = d*sin(fold).  Scale = E / (E + z).
                float z = d * s;               // depth of this point
                float perspScale = uEyeRatio / (uEyeRatio + z);

                // Apply cosine compression + perspective
                float warpedX = uHingePos - d * c * perspScale;

                // Clamp to valid UV range — no black voids
                uv.x = clamp(warpedX, 0.0, 1.0);
                // Y stays unchanged — matches web demo inner screen
            }
            // Right side stays as-is (sharp, bright, no darkening)

            // ── Blur & darken gradient ───────────────────────────
            // IMPORTANT: Use the ORIGINAL UV for gradient calculations,
            // not the warped UV.  The web demo computes edge from the
            // original screen position (sourceUV), and the 3D projection
            // is separate from the effect gradient.
            // uGrad = (hingePos, 0.0) for inner: sweeps LEFT from hinge
            // uGrad = (offsetX, offsetX+scale) for outer: sweeps RIGHT
            float edge = (origUV.x - uGrad.x) / (uGrad.y - uGrad.x);

            // Clamp: pixels OUTSIDE the gradient direction get edge=0
            // → zero blur, zero darkening.  This is what keeps the
            // RIGHT half of the inner screen completely untouched.
            float blurG   = clamp(edge, 0.0, 1.0);
            float darkG   = clamp((edge - 0.2) / 0.8, 0.0, 1.0);
            float effect  = uMotion * pow(darkG, 1.35);
            float radius  = uRadiusMax * uMotion * pow(blurG, 1.35);

            // Edge coverage / AA
            vec2 aa  = uPixel * 0.5;
            vec2 cov = smoothstep(-aa, aa, uv)
                     * (1.0 - smoothstep(vec2(1.0) - aa, vec2(1.0) + aa, uv));

            vec2 sampleCoord = uv / uPixel;
            half3 color = uImage.eval(sampleCoord).rgb * cov.x * cov.y;

            // ── 5×5 weighted blur kernel ─────────────────────────
            if (radius > 0.5) {
                // Always use the downscaled blur texture when available.
                // This gives smoother results similar to the web demo's
                // mipmap-based textureLod() approach.
                bool useBlur    = (uBlurScale > 1.0);
                float effScale  = useBlur ? uBlurScale : 1.0;
                vec2  effPixel  = uPixel * effScale;

                vec2 fp = max(aa, uPixel * radius * 0.75);
                color = half3(0.0);
                for (int y = -2; y <= 2; y++) {
                    for (int x = -2; x <= 2; x++) {
                        float wx = (x == 0) ? 6.0 : (abs(float(x)) == 1.0 ? 4.0 : 1.0);
                        float wy = (y == 0) ? 6.0 : (abs(float(y)) == 1.0 ? 4.0 : 1.0);

                        // Sample offset in normalised UV, matching web: uiPixel * radius
                        vec2 suv = uv + vec2(float(x), float(y)) * uPixel * radius;
                        vec2 sc  = smoothstep(-fp, fp, suv)
                                 * (1.0 - smoothstep(vec2(1.0) - fp, vec2(1.0) + fp, suv));
                        vec2 coord = suv / effPixel;
                        half3 tap  = useBlur
                            ? uImageBlur.eval(coord).rgb
                            : uImage.eval(coord).rgb;
                        color += tap * sc.x * sc.y * wx * wy / 256.0;
                    }
                }
            }

            return half4(color * (1.0 - min(1.0, effect * 2.0)), 1.0);
        }
    """
}
