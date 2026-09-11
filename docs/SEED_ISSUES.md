# Seed Issues for GitHub

Copy and paste these issue templates directly into your GitHub repository under **Issues > New Issue** to give contributors clear, actionable tasks.

---

### Issue 1: Recalibrate FoldMath constants for 2076×2152 inner display aspect
- **Labels**: `enhancement`, `good first issue`
- **Body**:
```markdown
### Summary
The Pixel 10 Pro Fold inner display has a nearly square aspect ratio (2076×2152, aspect ratio ~0.965).
Currently, the horizontal perspective compression ratio in `DeviceConfig.kt` uses the reference constant (`2.52f`).

### Tasks
- [ ] Profile the perceived fold curvature on 2076×2152 displays.
- [ ] Add an aspect-ratio correction factor in `DeviceConfig.perspectiveRatio` so square-aspect foldables don't over-compress at intermediate angles (e.g. 90° tabletop).
- [ ] Verify both in emulator and on physical hardware.
```

---

### Issue 2: Presentation-API fallback for devices where One UI provides single Engine
- **Labels**: `enhancement`, `help wanted`
- **Body**:
```markdown
### Summary
On certain Samsung One UI builds, third-party `WallpaperService` implementations only receive an `Engine` instance for `displayId = 0` (the main display) and are not dispatched a secondary surface for the outer cover screen.

### Proposed Solution
Implement an optional foreground fallback using `android.app.Presentation` on the secondary `Display` obtained from `DisplayManager`.

### References
- Check `DuoWallpaperService.kt` `resolveDisplayRole()`
- Android `Presentation` documentation: https://developer.android.com/reference/android/app/Presentation
```

---

### Issue 3: Mip-chain blur to optimize 25-tap cost at large radii
- **Labels**: `performance`, `good first issue`
- **Body**:
```markdown
### Summary
In `ShaderCode.kt`, the 5×5 weighted Gaussian blur executes 25 texture taps per fragment when `radius > 0.5`. On 120Hz high-resolution foldable screens (2160x1856+), this can place unnecessary load on the mobile GPU at high angles.

### Proposed Solution
- Generate a multi-level downscale pyramid (e.g., 2×, 4×, 8× downscaled bitmaps) during wallpaper preparation.
- Switch between mip-levels based on the computed `radius` uniform in AGSL, reducing the required sampling distance.
```

---

### Issue 4: Detect hinge-angle convention per OEM (log + auto-flip)
- **Labels**: `bug`, `enhancement`
- **Body**:
```markdown
### Summary
Most OEMs follow the Android standard where `0°` = fully folded / closed and `180°` = flat open. However, some OEM firmware reports inverted angles.

### Tasks
- [ ] Log the initial detected hinge angle and monitor angular progression on open.
- [ ] Add an auto-detection heuristic or a manual toggle in `MainActivity` to invert fold direction if the device reports `180°` when closed.
```

---

### Issue 5: Settings activity via wallpaper.xml metadata
- **Labels**: `enhancement`, `good first issue`
- **Body**:
```markdown
### Summary
Currently, users configure custom images and demo mode by opening the standalone app. Android's wallpaper picker supports opening a settings activity directly from the wallpaper preview screen.

### Tasks
- [ ] Add `android:settingsActivity="com.example.duofoldwallpaper.MainActivity"` to `app/src/main/res/xml/wallpaper.xml`.
- [ ] Verify that opening wallpaper settings from the launcher jumps directly to `MainActivity`.
```

---

### Issue 6: Clamshell (Galaxy Z Flip / Moto Razr) vertical fold profile
- **Labels**: `feature`, `help wanted`
- **Body**:
```markdown
### Summary
Book-style foldables (Fold) fold horizontally along a vertical hinge line. Clamshell foldables (Flip) fold vertically along a horizontal hinge line.

### Tasks
- [ ] Add device detection for Flip / Clamshell models in `DeviceConfig.kt`.
- [ ] Add a vertical deformation mode in `ShaderCode.kt` where `uv.y` is warped instead of `uv.x`.
```
