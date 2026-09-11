package com.example.duofoldwallpaper

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
import kotlin.math.PI
import kotlin.math.abs

/**
 * Live wallpaper that reproduces the "iPhone Duo" fold-transition effect on
 * a book-style foldable, using the hinge-angle sensor to drive an AGSL
 * shader.
 *
 * READ THIS BEFORE DEBUGGING THE COVER SCREEN:
 * Android's own docs for [WallpaperService.Engine.getDisplayContext] state
 * that "for multiple display environment, multiple engines can be created
 * to render on each display" -- that documented (if under-specified)
 * mechanism is what [onSurfaceCreated] relies on below to tell the inner
 * display's engine apart from the outer/cover display's engine.
 *
 * What is NOT documented or guaranteed anywhere is that Samsung's One UI
 * actually routes a third-party live wallpaper's second Engine to the Z
 * Fold's physical cover screen. In practice this has been inconsistent
 * across One UI versions for other apps. Before you rely on it:
 *   1. Build and run this as-is.
 *   2. Set it as your wallpaper, fold the phone, and check `adb logcat -s
 *      DuoWallpaperService` for a second "Engine bound to displayId=..."
 *      line with outer=true.
 *   3. If you only ever see one engine, the outer-screen half of this
 *      effect will not run -- the inner display will still show a correct
 *      single-screen fold transition, it just won't drive the "window"
 *      effect on the cover screen. In that case, fall back to a foreground
 *      app using the Presentation API on the secondary Display (the
 *      approach in the original PoC thread), which does not depend on the
 *      wallpaper system routing anything for you.
 */
class DuoWallpaperService : WallpaperService() {

    companion object {
        private const val TAG = "DuoWallpaperService"

        // Recompute the smoothed fold value until it's within this many
        // radians of the sensor target; below that, stop the per-frame
        // loop entirely so an idle wallpaper doesn't keep re-drawing.
        private const val SETTLE_EPSILON = 0.0002f
        private const val LOW_PASS_ALPHA = 0.12f
    }

    override fun onCreateEngine(): Engine = DuoEngine()

    private inner class DuoEngine : Engine(), SensorEventListener {

        private val choreographer = Choreographer.getInstance()
        private val runtimeShader = RuntimeShader(ShaderCode.DUO_SHADER)
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = runtimeShader }

        private lateinit var sensorManager: SensorManager
        private var hingeSensor: Sensor? = null

        // 0 = fully open, PI = fully closed. displayedFold is low-pass
        // smoothed toward targetFold (the latest raw sensor reading).
        private var displayedFold = 0f
        private var targetFold = 0f

        private var canvasWidth = 0f
        private var canvasHeight = 0f
        private var imageWidth = 2670f
        private var imageHeight = 1878f

        private var isOuterDisplay = false
        private var displayRoleResolved = false
        private var frameCallbackPosted = false

        private val frameCallback = Choreographer.FrameCallback {
            frameCallbackPosted = false
            if (isVisible) {
                drawFrame()
                val stillMoving = abs(targetFold - displayedFold) > SETTLE_EPSILON
                if (stillMoving) postFrameCallbackIfNeeded()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)

            val bitmap = loadWallpaperBitmap()
            imageWidth = bitmap.width.toFloat()
            imageHeight = bitmap.height.toFloat()
            val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            // Correct API: RuntimeShader.setInputShader(name, Shader).
            // (There is no setInputBuffer(name, width, height, HardwareBuffer)
            // overload, and no HardwareBuffer.createFromBitmap() factory --
            // both were fabricated in an earlier draft of this code.)
            runtimeShader.setInputShader("uImage", bitmapShader)

            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            hingeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)
            if (hingeSensor == null) {
                Log.w(TAG, "TYPE_HINGE_ANGLE sensor not available on this device")
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            resolveDisplayRole()
        }

        /**
         * Determines whether this Engine instance is rendering the inner
         * or the outer display, using the documented getDisplayContext()
         * API (guaranteed non-null once onCreate() has run). The earlier
         * draft tried to read a nonexistent `surface.surfaceControl` field
         * for this -- that API doesn't exist on WallpaperService.Engine.
         */
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
            sensorManager.unregisterListener(this)
            stopFrameLoop()
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_HINGE_ANGLE) return
            val degrees = event.values[0]
            targetFold = ((180.0 - degrees) * PI / 180.0).toFloat().coerceIn(0f, PI.toFloat())
            postFrameCallbackIfNeeded()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

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

        private fun drawFrame() {
            if (!displayRoleResolved) resolveDisplayRole()
            if (canvasWidth <= 0f || canvasHeight <= 0f) return

            displayedFold += (targetFold - displayedFold) * LOW_PASS_ALPHA

            val holder = surfaceHolder
            val canvas: Canvas = (holder.lockHardwareCanvas() ?: holder.lockCanvas()) ?: return
            try {
                // Robust outer detection: either a secondary display or default display when folded / tall
                val effectiveIsOuterDisplay = isOuterDisplay || 
                    (getDisplayContext()?.display?.displayId == Display.DEFAULT_DISPLAY &&
                        (canvasHeight > canvasWidth * 1.5f || displayedFold > (PI.toFloat() / 2f)))

                val progress = if (effectiveIsOuterDisplay) {
                    ((PI.toFloat() - displayedFold) / (PI.toFloat() / 2f)).coerceIn(0f, 1f)
                } else {
                    (displayedFold / (PI.toFloat() / 2f)).coerceIn(0f, 1f)
                }
                val motion = FoldMath.smoothstep(progress)
                val radiusMax = 72.0f * (imageWidth / 1600.0f)

                runtimeShader.setFloatUniform("uPixel", 1f / imageWidth, 1f / imageHeight)
                runtimeShader.setFloatUniform("uMotion", motion)
                runtimeShader.setFloatUniform("uRadiusMax", radiusMax)

                if (effectiveIsOuterDisplay) {
                    val anchorX = FoldMath.anchorX(displayedFold)
                    val originX = (anchorX - FoldMath.INNER_FRAME_X) / FoldMath.INNER_FRAME_Z
                    val scaleX = FoldMath.OUTER_FRAME_Z / FoldMath.INNER_FRAME_Z

                    runtimeShader.setFloatUniform("uUvOffset", originX, 0f)
                    runtimeShader.setFloatUniform("uUvScale", scaleX / canvasWidth, 1f / canvasHeight)
                    runtimeShader.setFloatUniform("uGrad", originX, originX + scaleX)
                    runtimeShader.setFloatUniform("uFold", 0f)
                } else {
                    runtimeShader.setFloatUniform("uUvOffset", 0f, 0f)
                    runtimeShader.setFloatUniform("uUvScale", 1f / canvasWidth, 1f / canvasHeight)
                    runtimeShader.setFloatUniform("uGrad", 0.5f, 0.0f)
                    runtimeShader.setFloatUniform("uFold", displayedFold)
                }

                canvas.drawRect(0f, 0f, canvasWidth, canvasHeight, paint)
            } catch (t: Throwable) {
                Log.e(TAG, "drawFrame failed", t)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }

        /**
         * Loads the canonical wallpaper image from res/drawable-nodpi/wallpaper.png
         * (drawable-nodpi is deliberate: putting it in a density bucket would
         * let Android rescale it on load, which would throw off every pixel
         * measurement the shader math above depends on). A generated
         * placeholder ships in that slot -- replace it with your own asset,
         * recommended size 2670x1878 to match the frame math ported from the
         * web demo.
         */
        private fun loadWallpaperBitmap(): Bitmap {
            val resId = resources.getIdentifier("wallpaper", "drawable", packageName)
            if (resId != 0) {
                val opts = BitmapFactory.Options().apply { inScaled = false }
                BitmapFactory.decodeResource(resources, resId, opts)?.let { return it }
            }
            Log.w(TAG, "res/drawable-nodpi/wallpaper.png not found, using generated fallback")
            return createFallbackBitmap()
        }

        private fun createFallbackBitmap(): Bitmap {
            val w = 2670
            val h = 1878
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
