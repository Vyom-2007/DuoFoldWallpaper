package com.example.duofoldwallpaper

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var previewImage: ImageView
    private lateinit var demoSwitch: Switch

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { saveCustomImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        val padding = (24 * density).toInt()
        val smallPad = (12 * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(padding, (48 * density).toInt(), padding, padding)
            setBackgroundColor(0xFFF6F6F3.toInt())
        }

        // Title
        root.addView(
            TextView(this).apply {
                text = "Duo Fold Wallpaper"
                textSize = 26f
                setTextColor(0xFF262C24.toInt())
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, smallPad)
            }
        )

        // Subtitle
        root.addView(
            TextView(this).apply {
                text = "Ported from the iPhone Duo web demo"
                textSize = 14f
                setTextColor(0xFF818974.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, (24 * density).toInt())
            }
        )

        // Preview card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val cardBg = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 20 * density
            }
            background = cardBg
            elevation = 8 * density
            setPadding(smallPad, smallPad, smallPad, smallPad)
        }

        previewImage = ImageView(this).apply {
            val imgLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (200 * density).toInt()
            )
            layoutParams = imgLp
            scaleType = ImageView.ScaleType.CENTER_CROP
            val roundedBg = GradientDrawable().apply {
                cornerRadius = 12 * density
                setColor(0xFFECEFE6.toInt())
            }
            background = roundedBg
            clipToOutline = true
            outlineProvider = object : android.view.ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, 12 * density)
                }
            }
        }
        card.addView(previewImage)
        loadPreviewImage()

        root.addView(card, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = (20 * density).toInt()
        })

        // Button row
        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        buttonRow.addView(
            createStyledButton("Choose Image").apply {
                setOnClickListener { pickImageLauncher.launch("image/*") }
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = (8 * density).toInt()
            }
        )

        buttonRow.addView(
            createStyledButton("Set Wallpaper").apply {
                setOnClickListener { openLiveWallpaperPicker() }
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (8 * density).toInt()
            }
        )

        root.addView(buttonRow, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = (20 * density).toInt()
        })

        // Demo mode toggle
        val demoRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val toggleBg = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 14 * density
            }
            background = toggleBg
            elevation = 2 * density
            setPadding((16 * density).toInt(), (14 * density).toInt(),
                (16 * density).toInt(), (14 * density).toInt())
        }

        demoRow.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(context).apply {
                    text = "Demo Mode"
                    textSize = 15f
                    setTextColor(0xFF262C24.toInt())
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                })
                addView(TextView(context).apply {
                    text = "Animate the fold cycle without a physical sensor"
                    textSize = 12f
                    setTextColor(0xFF818974.toInt())
                })
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )

        val prefs = getSharedPreferences(DuoWallpaperService.PREFS_NAME, MODE_PRIVATE)
        demoSwitch = Switch(this).apply {
            isChecked = prefs.getBoolean(DuoWallpaperService.PREF_DEMO_MODE, false)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit()
                    .putBoolean(DuoWallpaperService.PREF_DEMO_MODE, isChecked)
                    .apply()
            }
        }
        demoRow.addView(demoSwitch)

        root.addView(demoRow, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = (20 * density).toInt()
        })

        // Instructions
        root.addView(
            TextView(this).apply {
                text = getString(R.string.main_instructions)
                textSize = 13f
                setTextColor(0xFF818974.toInt())
                gravity = Gravity.CENTER
                setPadding(smallPad, 0, smallPad, 0)
            }
        )

        val scrollView = android.widget.ScrollView(this).apply {
            addView(root)
        }

        setContentView(scrollView)
    }

    private fun createStyledButton(label: String): Button {
        val density = resources.displayMetrics.density
        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 14f
            setTextColor(0xFFF7FAF4.toInt())
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            val bg = GradientDrawable().apply {
                setColor(0xFF262C24.toInt())
                cornerRadius = 14 * density
            }
            background = bg
            stateListAnimator = null
            elevation = 0f
            setPadding(
                (16 * density).toInt(), (14 * density).toInt(),
                (16 * density).toInt(), (14 * density).toInt()
            )
        }
    }

    private fun loadPreviewImage() {
        val customFile = File(filesDir, DuoWallpaperService.CUSTOM_IMAGE_FILE)
        if (customFile.exists()) {
            val bmp = BitmapFactory.decodeFile(customFile.absolutePath)
            if (bmp != null) {
                previewImage.setImageBitmap(bmp)
                return
            }
        }
        // Fall back to bundled wallpaper
        val resId = resources.getIdentifier("wallpaper", "drawable", packageName)
        if (resId != 0) {
            previewImage.setImageResource(resId)
        }
    }

    private fun saveCustomImage(uri: Uri) {
        try {
            val input = contentResolver.openInputStream(uri) ?: return
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()
            if (bitmap == null) {
                Toast.makeText(this, "Unable to decode image", Toast.LENGTH_SHORT).show()
                return
            }
            val outFile = File(filesDir, DuoWallpaperService.CUSTOM_IMAGE_FILE)
            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            previewImage.setImageBitmap(bitmap)

            // Notify the wallpaper service to reload
            getSharedPreferences(DuoWallpaperService.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(DuoWallpaperService.PREF_CUSTOM_IMAGE, outFile.absolutePath)
                .apply()

            Toast.makeText(this, "Wallpaper updated", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openLiveWallpaperPicker() {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this@MainActivity, DuoWallpaperService::class.java)
            )
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.picker_unavailable), Toast.LENGTH_LONG).show()
        }
    }
}
