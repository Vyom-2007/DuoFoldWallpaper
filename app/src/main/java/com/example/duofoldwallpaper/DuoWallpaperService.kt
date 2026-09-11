package com.example.duofoldwallpaper

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.Choreographer
import android.view.Display
import android.view.SurfaceHolder
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

/**
 * Live wallpaper that reproduces the fold-transition effect from the iPhone
 * Duo web demo, dynamically adapted to whichever foldable device it runs on.
 *
 * All device-specific geometry (hinge position, display sizes, perspective
 * ratio) comes from [DeviceConfig], which auto-detects the running device
 * or falls back to the Pixel 10 Pro Fold as the primary target.
 */
class DuoWallpaperService : WallpaperService() {

    companion object {
        private const val TAG = "DuoWallpaperService"

        private const val SETTLE_EPSILON = 0.0002f
        private const val LOW_PASS_ALPHA = 0.12f
        private const val BLUR_DOWNSCALE = 4f

        const val PREFS_NAME = "duo_wallpaper_prefs"
        const val PREF_CUSTOM_IMAGE = "custom_image_path"
        const val PREF_DEMO_MODE = "demo_mode"
        const val CUSTOM_IMAGE_FILE = "custom_wallpaper.png"
    }

    override fun onCreateEngine(): Engine = DuoEngine()

    private inner class DuoEngine : Engine(), SensorEventListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

        private val choreographer = Choreographer.getInstance()
        private val runtimeShader = RuntimeShader(ShaderCode.DUO_SHADER)
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = runtimeShader }

        private lateinit var sensorManager: SensorManager
        private lateinit var prefs: SharedPreferences
        private lateinit var deviceConfig: DeviceConfig
        private var hingeSensor: Sensor? = null

        private var displayedFold = 0f
        private var targetFold = 0f

        private var canvasWidth = 0f
        private var canvasHeight = 0f
        private var imageWidth = 0f
        private var imageHeight = 0f

        private var isOuterDisplay = false
        private var displayRoleResolved = false
        private var frameCallbackPosted = false

        private var demoMode = false
        private var demoPhase = 0f
        private var lastFrameTimeNanos = 0L

        private val frameCallback = Choreographer.FrameCallback { now ->
            frameCallbackPosted = false
            if (isVisible) {
                drawFrame(now)
                val stillMoving = demoMode || abs(targetFold - displayedFold) > SETTLE_EPSILON
                if (stillMoving) postFrameCallbackIfNeeded()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)

            // ── Detect device geometry ─────────────────────────────
            deviceConfig = DeviceConfig.detect(this@DuoWallpaperService)
            Log.i(TAG, "Device config: $deviceConfig")

            // Set the device-geometry uniforms (these don't change per frame)
            runtimeShader.setFloatUniform("uHingePos", deviceConfig.hingeRatio)
            runtimeShader.setFloatUniform("uEyeRatio", deviceConfig.perspectiveRatio)

            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener(this)
            demoMode = prefs.getBoolean(PREF_DEMO_MODE, false)

            loadAndBindWallpaper()

            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            hingeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)
            if (hingeSensor == null) {
                Log.w(TAG, "TYPE_HINGE_ANGLE sensor not available on this device")
            }
        }

        private fun loadAndBindWallpaper() {
            val bitmap = loadWallpaperBitmap()
            imageWidth = bitmap.width.toFloat()
            imageHeight = bitmap.height.toFloat()

            val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            runtimeShader.setInputShader("uImage", bitmapShader)

            val blurW = (bitmap.width / BLUR_DOWNSCALE).toInt().coerceAtLeast(1)
            val blurH = (bitmap.height / BLUR_DOWNSCALE).toInt().coerceAtLeast(1)
            val blurBitmap = Bitmap.createScaledBitmap(bitmap, blurW, blurH, true)
            val blurShader = BitmapShader(blurBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            runtimeShader.setInputShader("uImageBlur", blurShader)
            runtimeShader.setFloatUniform("uBlurScale", BLUR_DOWNSCALE)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            resolveDisplayRole()
        }

        private fun resolveDisplayRole() {
            val displayId = getDisplayContext()?.display?.displayId
            isOuterDisplay = displayId != null && displayId != Display.DEFAULT_DISPLAY
            displayRoleResolved = true
            Log.d(TAG, "Engine bound to displayId=$displayId outer=$isOuterDisplay")
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            canvasWidth = width.toFloat()
            canvasHeight = height.toFloat()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                hingeSensor?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
                }
                lastFrameTimeNanos = System.nanoTime()
                postFrameCallbackIfNeeded()
            } else {
                sensorManager.unregisterListener(this)
                stopFrameLoop()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            sensorManager.unregisterListener(this)
            stopFrameLoop()
        }

        override fun onDestroy() {
            super.onDestroy()
            prefs.unregisterOnSharedPreferenceChangeListener(this)
            sensorManager.unregisterListener(this)
            stopFrameLoop()
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_HINGE_ANGLE) return
            if (demoMode) return
            val degrees = event.values[0]
            targetFold = ((180.0 - degrees) * PI / 180.0).toFloat().coerceIn(0f, PI.toFloat())
            postFrameCallbackIfNeeded()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
            when (key) {
                PREF_CUSTOM_IMAGE -> loadAndBindWallpaper()
                PREF_DEMO_MODE -> {
                    demoMode = prefs.getBoolean(PREF_DEMO_MODE, false)
                    if (demoMode) {
                        val currentDegrees = (180f - displayedFold * 180f / PI.toFloat())
                            .coerceIn(0f, 180f)
                        demoPhase = FoldMath.demoPhaseForAngle(currentDegrees)
                        lastFrameTimeNanos = System.nanoTime()
                    }
                    postFrameCallbackIfNeeded()
                }
            }
        }

        private fun postFrameCallbackIfNeeded() {
            if (!frameCallbackPosted && isVisible) {
                frameCallbackPosted = true
                choreographer.postFrameCallback(frameCallback)
            }
        }

        private fun stopFrameLoop() {
            choreographer.removeFrameCallback(frameCallback)
            frameCallbackPosted = false
        }

        private fun drawFrame(nowNanos: Long) {
            if (!displayRoleResolved) resolveDisplayRole()
            if (canvasWidth <= 0f || canvasHeight <= 0f) return

            val deltaSec = min((nowNanos - lastFrameTimeNanos) / 1_000_000_000f, 0.05f)
            lastFrameTimeNanos = nowNanos

            if (demoMode) {
                demoPhase = (demoPhase + deltaSec) % FoldMath.DEMO_CYCLE_DURATION
                val degrees = FoldMath.demoAngleDegrees(demoPhase)
                displayedFold = ((180f - degrees) * PI.toFloat() / 180f).coerceIn(0f, PI.toFloat())
            } else {
                displayedFold += (targetFold - displayedFold) * LOW_PASS_ALPHA
            }

            val holder = surfaceHolder
            val canvas: Canvas = (holder.lockHardwareCanvas() ?: holder.lockCanvas()) ?: return
            try {
                // ── Determine display role ──────────────────────────
                // On foldables with a single wallpaper engine, use aspect
                // ratio as a heuristic: portrait-tall → outer/cover display.
                val effectiveIsOuter = isOuterDisplay ||
                    (getDisplayContext()?.display?.displayId == Display.DEFAULT_DISPLAY &&
                        canvasHeight > canvasWidth * 1.5f)

                // ── Progress & motion ───────────────────────────────
                val progress = if (effectiveIsOuter) {
                    ((PI.toFloat() - displayedFold) / (PI.toFloat() / 2f)).coerceIn(0f, 1f)
                } else {
                    (displayedFold / (PI.toFloat() / 2f)).coerceIn(0f, 1f)
                }
                val motion = FoldMath.smoothstep(progress)

                // ── Per-frame shader uniforms (all dynamic) ─────────
                runtimeShader.setFloatUniform("uPixel", 1f / imageWidth, 1f / imageHeight)
                runtimeShader.setFloatUniform("uMotion", motion)
                runtimeShader.setFloatUniform("uRadiusMax", deviceConfig.maxBlurRadius)

                if (effectiveIsOuter) {
                    // Outer/cover display: show the right portion of the
                    // wallpaper, anchored at the hinge edge.
                    val offsetX = FoldMath.outerUvOffsetX(displayedFold, deviceConfig)
                    val scaleX = deviceConfig.coverToInnerRatio

                    runtimeShader.setFloatUniform("uUvOffset", offsetX, 0f)
                    runtimeShader.setFloatUniform("uUvScale", scaleX / canvasWidth, 1f / canvasHeight)
                    // Gradient sweeps from hinge edge toward the right edge
                    runtimeShader.setFloatUniform("uGrad", offsetX, offsetX + scaleX)
                    runtimeShader.setFloatUniform("uFold", 0f)
                } else {
                    // Inner display: full wallpaper, gradient sweeps from
                    // hinge toward the left edge.
                    runtimeShader.setFloatUniform("uUvOffset", 0f, 0f)
                    runtimeShader.setFloatUniform("uUvScale", 1f / canvasWidth, 1f / canvasHeight)
                    runtimeShader.setFloatUniform("uGrad", deviceConfig.hingeRatio, 0f)
                    runtimeShader.setFloatUniform("uFold", displayedFold)
                }

                canvas.drawRect(0f, 0f, canvasWidth, canvasHeight, paint)
            } catch (t: Throwable) {
                Log.e(TAG, "drawFrame failed", t)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }

        private fun loadWallpaperBitmap(): Bitmap {
            val customFile = File(filesDir, CUSTOM_IMAGE_FILE)
            if (customFile.exists()) {
                val opts = BitmapFactory.Options().apply { inScaled = false }
                BitmapFactory.decodeFile(customFile.absolutePath, opts)?.let {
                    Log.d(TAG, "Loaded custom wallpaper: ${it.width}×${it.height}")
                    return it
                }
            }
            val resId = resources.getIdentifier("wallpaper", "drawable", packageName)
            if (resId != 0) {
                val opts = BitmapFactory.Options().apply { inScaled = false }
                BitmapFactory.decodeResource(resources, resId, opts)?.let { return it }
            }
            Log.w(TAG, "No wallpaper found, using generated fallback")
            return createFallbackBitmap()
        }

        private fun createFallbackBitmap(): Bitmap {
            // Size based on actual device inner display
            val w = deviceConfig.innerWidthPx
            val h = deviceConfig.innerHeightPx
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            val gradient = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(0xFF1E1E2E.toInt(), 0xFF4B2E83.toInt(), 0xFF0F3460.toInt()),
                null, Shader.TileMode.CLAMP
            )
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply { shader = gradient })
            return bmp
        }
    }
}
