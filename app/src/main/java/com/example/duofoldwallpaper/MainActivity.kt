package com.example.duofoldwallpaper

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val padding = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(padding, padding * 4, padding, padding)
        }

        root.addView(
            TextView(this).apply {
                text = getString(R.string.main_instructions)
                textSize = 16f
                setPadding(0, 0, 0, padding)
            }
        )

        root.addView(
            Button(this).apply {
                text = getString(R.string.set_wallpaper_button)
                setOnClickListener { openLiveWallpaperPicker() }
            }
        )

        setContentView(root)
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
