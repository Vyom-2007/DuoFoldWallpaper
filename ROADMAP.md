# Project Roadmap

This document outlines planned improvements, performance optimizations, and feature milestones for **Duo Fold Wallpaper**.

---

## 🚀 Near-Term Milestones (v1.1 - v1.2)

- [ ] **Mip-Chain Texture Blur Optimization**
  - Current status: 5×5 weighted Gaussian filter (25 texture lookups per pixel at higher radii).
  - Target: Implement a multi-level downscale pyramid (1/2×, 1/4×, 1/8×) to dramatically reduce fragment shader overhead on high-refresh 120 Hz displays.
  - Tracking: `good first issue`

- [ ] **Presentation API Fallback for One UI**
  - Some Samsung One UI releases allocate only one `WallpaperService.Engine` (binding to `displayId=0`).
  - Target: Provide an opt-in foreground service / `android.app.Presentation` fallback for devices that don't route the cover display to live wallpapers.
  - Tracking: `help wanted`

- [ ] **Storage Access Framework (SAF) & Live Crop Tool**
  - Upgrade the image picker to support direct cropping, aspect ratio locking, and seam positioning for custom wallpapers.

- [ ] **OEM Hinge Convention Auto-Calibration**
  - Different manufacturers occasionally report inverted hinge angle values (e.g. 0° flat vs 180° flat).
  - Target: Add heuristic detection and a toggle in `MainActivity` to flip hinge conventions if needed.

---

## 🌟 Medium-Term Goals (v1.5)

- [ ] **Clamshell / Flip Support (Horizontal Hinge)**
  - Add orientation detection for vertical foldables (Galaxy Z Flip series, Moto Razr).
  - Adapt the AGSL perspective warp to deform along the Y-axis instead of the X-axis.

- [ ] **Live Wallpaper Settings Activity**
  - Hook `MainActivity` directly into the system live wallpaper settings via `android:settingsActivity` in `wallpaper.xml`.

- [ ] **Spring Dynamics & Inertia**
  - Integrate a physics-based spring curve for smoothing hinge angle changes, matching iOS-style fluid motion during rapid opening and closing.

---

## 🔮 Future Explorations

- [ ] **Multi-Layer Parallax**
  - Separate background and foreground wallpaper elements with subtle parallax when folding or panning across launcher pages.
- [ ] **Dynamic Palette & Material You Theming**
  - Extract accent colors from custom wallpapers and tint the subtle darken gradient accordingly.
