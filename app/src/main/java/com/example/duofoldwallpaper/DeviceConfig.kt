package com.example.duofoldwallpaper

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.WindowManager

/**
 * Device-specific geometry for the fold effect.
 *
 * Instead of hardcoding iPhone Duo 3D model constants (EYE_Z=40,
 * INNER_FRAME_Z=15.7987, HINGE_X=-0.23396 …), this derives every value
 * the shader needs from the real display measurements of the running device.
 *
 * All shader-facing values are in **normalised UV space (0..1)** or
 * dimensionless ratios, so the AGSL code no longer carries any device-
 * specific magic numbers.
 */
data class DeviceConfig(
    /** Inner (unfolded) display width in pixels. */
    val innerWidthPx: Int,
    /** Inner (unfolded) display height in pixels. */
    val innerHeightPx: Int,
    /** Cover (outer) display width in pixels. */
    val coverWidthPx: Int,
    /** Cover (outer) display height in pixels. */
    val coverHeightPx: Int,
    /** Display density in DPI. */
    val densityDpi: Int,
    /** Hinge X position on the inner display, in pixels from the left. */
    val hingePositionPx: Int,
) {
    // ── Derived values used by the shader and FoldMath ───────────────

    /** Hinge position in UV space (0..1).  0.5 = centred, typical. */
    val hingeRatio: Float = hingePositionPx.toFloat() / innerWidthPx.toFloat()

    /** Physical inner display width in inches. */
    val innerWidthInches: Float = innerWidthPx.toFloat() / densityDpi.toFloat()

    /**
     * Perspective ratio = virtualEyeDistance / frameWidth.
     *
     * The iPhone Duo web demo used EYE_Z=40 and FRAME_W=15.7987, giving
     * a ratio of ~2.52.  We use the same ratio so the visual warp strength
     * looks equivalent regardless of physical screen size.
     */
    val perspectiveRatio: Float = 2.52f

    /** Cover display width as a fraction of the inner display width. */
    val coverToInnerRatio: Float = coverWidthPx.toFloat() / innerWidthPx.toFloat()

    /**
     * Maximum blur radius, scaled to the device's pixel density.
     * The web demo used 72 source-pixels on a 1600px-wide texture.
     * We scale proportionally: 72 * (innerWidth / 1600).
     */
    val maxBlurRadius: Float = 72f * (innerWidthPx.toFloat() / 1600f)

    companion object {
        private const val TAG = "DeviceConfig"

        // ── Named configurations for known devices ──────────────────

        /** Google Pixel 10 Pro Fold (from AVD config). */
        val PIXEL_10_PRO_FOLD = DeviceConfig(
            innerWidthPx = 2076,
            innerHeightPx = 2152,
            coverWidthPx = 1080,
            coverHeightPx = 2364,
            densityDpi = 390,
            hingePositionPx = 1038,  // hw.sensor.hinge.areas = 1038-0-0-2152
        )

        /** Samsung Galaxy Z Fold 6. */
        val GALAXY_Z_FOLD_6 = DeviceConfig(
            innerWidthPx = 1856,
            innerHeightPx = 2160,
            coverWidthPx = 968,
            coverHeightPx = 2376,
            densityDpi = 373,
            hingePositionPx = 928,
        )

        /** Samsung Galaxy Z Fold 8 (estimated). */
        val GALAXY_Z_FOLD_8 = DeviceConfig(
            innerWidthPx = 1968,
            innerHeightPx = 2184,
            coverWidthPx = 1080,
            coverHeightPx = 2400,
            densityDpi = 402,
            hingePositionPx = 984,
        )

        /** iPhone Duo (web demo reference — for comparison only). */
        val IPHONE_DUO_WEB_REFERENCE = DeviceConfig(
            innerWidthPx = 1600,
            innerHeightPx = 1125,
            coverWidthPx = 774,
            coverHeightPx = 1125,
            densityDpi = 460,
            hingePositionPx = 800,
        )

        /**
         * Auto-detects the device configuration at runtime.
         *
         * Priority:
         * 1. Known device by Build.DEVICE / Build.MODEL
         * 2. Runtime detection from DisplayManager + hinge sensor
         * 3. Fallback to Pixel 10 Pro Fold defaults
         */
        fun detect(context: Context): DeviceConfig {
            // 1. Try known devices first
            val known = detectKnownDevice()
            if (known != null) {
                Log.d(TAG, "Using known config for ${Build.DEVICE}: $known")
                return known
            }

            // 2. Runtime detection
            val runtime = detectFromDisplays(context)
            if (runtime != null) {
                Log.d(TAG, "Auto-detected config: $runtime")
                return runtime
            }

            // 3. Fallback
            Log.w(TAG, "Could not detect device geometry, using Pixel 10 Pro Fold defaults")
            return PIXEL_10_PRO_FOLD
        }

        private fun detectKnownDevice(): DeviceConfig? {
            val device = Build.DEVICE.lowercase()
            val model = Build.MODEL.lowercase()
            return when {
                device.contains("comet") || model.contains("pixel 10 pro fold") ||
                    device.contains("pixel_10_pro_fold") -> PIXEL_10_PRO_FOLD

                device.contains("q5q") || model.contains("z fold6") ||
                    model.contains("z fold 6") -> GALAXY_Z_FOLD_6

                device.contains("dm3q") || device.contains("b6q") ||
                    model.contains("z fold5") || model.contains("z fold 5") ->
                    GALAXY_Z_FOLD_6 // close enough

                else -> null
            }
        }

        /**
         * Attempts to detect display geometry from [DisplayManager].
         *
         * For foldables, Android typically exposes:
         * - Display.DEFAULT_DISPLAY (display 0): the inner/unfolded display
         * - A secondary display (display != 0): the cover display
         *
         * The hinge position is assumed to be at the horizontal centre of the
         * inner display — true for every major book-style foldable to date.
         */
        private fun detectFromDisplays(context: Context): DeviceConfig? {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                ?: return null
            val displays = dm.displays ?: return null

            var innerW = 0
            var innerH = 0
            var coverW = 0
            var coverH = 0
            var dpi = 0

            for (display in displays) {
                val mode = display.mode
                val w = mode.physicalWidth
                val h = mode.physicalHeight
                if (display.displayId == Display.DEFAULT_DISPLAY) {
                    innerW = w
                    innerH = h
                    // Get DPI from the context's resources (more reliable than deprecated metrics)
                    dpi = context.resources.displayMetrics.densityDpi
                } else {
                    coverW = w
                    coverH = h
                }
            }

            // Need at least the inner display
            if (innerW <= 0 || innerH <= 0 || dpi <= 0) return null

            // If inner display is taller than wide (portrait), it's not unfolded yet.
            // Use the wider dimension as width for a landscape-first foldable.
            if (innerH > innerW) {
                val tmp = innerW; innerW = innerH; innerH = tmp
            }

            // If no cover display found, estimate from inner (cover ≈ half width)
            if (coverW <= 0) {
                coverW = innerW / 2
                coverH = innerH
            }

            // Hinge at centre (standard for book-style foldables)
            val hingePos = innerW / 2

            return DeviceConfig(
                innerWidthPx = innerW,
                innerHeightPx = innerH,
                coverWidthPx = coverW,
                coverHeightPx = coverH,
                densityDpi = dpi,
                hingePositionPx = hingePos,
            )
        }
    }

    override fun toString(): String =
        "DeviceConfig(inner=${innerWidthPx}×${innerHeightPx}, cover=${coverWidthPx}×${coverHeightPx}, " +
            "hinge=$hingePositionPx/${innerWidthPx}=${String.format("%.3f", hingeRatio)}, " +
            "dpi=$densityDpi, perspective=$perspectiveRatio, " +
            "coverRatio=${String.format("%.3f", coverToInnerRatio)}, " +
            "maxBlur=${String.format("%.1f", maxBlurRadius)})"
}
