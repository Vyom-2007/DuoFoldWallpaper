package com.example.duofoldwallpaper

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos

/**
 * Fold-effect math parameterised by [DeviceConfig].
 *
 * No hardcoded device constants — every geometric value is derived from
 * the detected device's display measurements.
 */
object FoldMath {

    // ── Demo animation timing (web demo main.js L322-L342) ──────────
    const val DEMO_CYCLE_DURATION = 8.6f
    const val DEMO_HOLD_FLAT_END = 1.2f
    const val DEMO_FOLD_END = 4.3f
    const val DEMO_HOLD_CLOSED_END = 5.5f
    private const val DEMO_FOLD_SPAN = DEMO_FOLD_END - DEMO_HOLD_FLAT_END   // 3.1
    private const val DEMO_UNFOLD_SPAN = DEMO_CYCLE_DURATION - DEMO_HOLD_CLOSED_END // 3.1

    /**
     * Computes the UV x-offset for the outer/cover display so the wallpaper
     * appears hinged at the physical fold edge.
     *
     * In UV space: the hinge is at [DeviceConfig.hingeRatio], and the cover
     * display shows [DeviceConfig.coverToInnerRatio] worth of the image
     * starting from the hinge and going rightward.
     *
     * @param foldRadians  0 = fully open, PI = fully closed.
     * @param config       The detected device geometry.
     * @return UV x-offset for the outer display's left edge.
     */
    fun outerUvOffsetX(foldRadians: Float, config: DeviceConfig): Float {
        // At fully open (foldRadians=0), outer shows from hingeRatio rightward.
        // As the device folds, the anchor point shifts due to 3D projection.
        // The shift amount depends on the perspective ratio.
        val hingeShift = perspectiveHingeShift(foldRadians, config)
        return config.hingeRatio + hingeShift
    }

    /**
     * How much the hinge edge appears to shift in UV space due to the 3D
     * perspective of folding.  At foldRadians=0 this is 0; at PI it's
     * a small negative value (the hinge "pulls" inward).
     */
    private fun perspectiveHingeShift(foldRadians: Float, config: DeviceConfig): Float {
        if (foldRadians < 0.001f) return 0f
        val s = kotlin.math.sin(foldRadians)
        val c = cos(foldRadians)
        // A tiny strip at the hinge edge projects slightly differently
        // when the cover half rotates.  The shift is proportional to
        // sin(fold) scaled by the inverse perspective ratio.
        val edgeDist = 0.001f  // infinitesimal strip at hinge
        val depth = config.perspectiveRatio / (config.perspectiveRatio + edgeDist * s)
        return edgeDist * c * (depth - 1f)
    }

    /** Standard smoothstep, input clamped to [0, 1]. */
    fun smoothstep(x: Float): Float {
        val t = x.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    /** Demo animation: fold angle in degrees for a given cycle phase. */
    fun demoAngleDegrees(phase: Float): Float {
        return when {
            phase < DEMO_HOLD_FLAT_END -> 180f
            phase < DEMO_FOLD_END -> {
                90f * (1f + cos((phase - DEMO_HOLD_FLAT_END) / DEMO_FOLD_SPAN * PI.toFloat()))
            }
            phase < DEMO_HOLD_CLOSED_END -> 0f
            else -> {
                90f * (1f - cos((phase - DEMO_HOLD_CLOSED_END) / DEMO_UNFOLD_SPAN * PI.toFloat()))
            }
        }
    }

    /** Initial phase so the demo animation picks up from [currentDegrees]. */
    fun demoPhaseForAngle(currentDegrees: Float): Float {
        return DEMO_HOLD_FLAT_END +
            acos(2f * currentDegrees / 180f - 1f).toFloat() / PI.toFloat() * DEMO_FOLD_SPAN
    }
}
