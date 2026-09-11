package com.example.duofoldwallpaper

import kotlin.math.cos
import kotlin.math.sin

/**
 * Ports the 3D ray-intersection anchor math from the web reference
 * implementation (chuspeeism/iphone-duo, main.js) into a 2D affine UV
 * offset that can drive an AGSL shader per output pixel.
 *
 * The web demo projects a 3D fold model through a fixed front-view camera;
 * on a real device each physical display is flat, so that whole projection
 * collapses to "what horizontal UV offset makes the outer display's content
 * line up with the inner display's content at the physical hinge edge".
 * This function computes exactly that offset, once per frame.
 */
object FoldMath {
    private const val EYE_Z = 40.0f
    private const val INNER_PLANE_Z = 0.24948f
    private const val PIVOT_Z = 0.275454f

    // Hinge edge coordinates in model space, before rotation.
    private const val HINGE_X = -0.23396f
    private const val HINGE_Y = -0.550084f // (-0.27463 - 0.275454)

    // Outer UI frame width, scaled into the same projected space as the
    // inner plane (ported directly from the web demo's frame math).
    private const val OUTER_FRAME_Z = 7.73936f * ((EYE_Z - INNER_PLANE_Z) / (EYE_Z - 0.825538f))

    /**
     * Horizontal UV offset (in the canonical image's normalized space) for
     * the outer/cover display, so the wallpaper appears hinged to the
     * physical fold edge as [foldRadians] changes.
     *
     * @param foldRadians 0 = fully open, PI = fully closed.
     */
    fun computeOuterOffsetX(foldRadians: Float): Float {
        val c = cos(foldRadians)
        val s = sin(foldRadians)

        // Rotate the hinge edge around the pivot.
        val foldedX = c * HINGE_X + s * HINGE_Y
        val foldedY = -s * HINGE_X + c * HINGE_Y + PIVOT_Z

        // Project the rotated edge onto the inner screen plane from the
        // fixed reference eye at (0, 0, EYE_Z).
        val edgeDepth = (INNER_PLANE_Z - EYE_Z) / (foldedY - EYE_Z)
        val anchorX = foldedX * edgeDepth

        // Negative because the outer screen shows the right-hand portion
        // of the canonical image, mirrored from the hinge edge inward.
        return -anchorX / OUTER_FRAME_Z
    }

    /** Standard smoothstep, input clamped to [0, 1] first. */
    fun smoothstep(x: Float): Float {
        val t = x.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
}
