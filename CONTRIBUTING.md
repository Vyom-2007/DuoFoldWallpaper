# Contributing to Duo Fold Wallpaper

Thank you for your interest in contributing to **Duo Fold Wallpaper**! We welcome bug fixes, device profile calibrations, shader performance improvements, and feature contributions.

---

## Quick Start & Development Setup

### Prerequisites
- **Android Studio Hedgehog (2023.1.1)** or newer (Ladybug / Meerkat recommended)
- **JDK 17**
- **Android SDK Platform 35** and **API 33+** (Required for AGSL `RuntimeShader`)
- A physical foldable device (Samsung Galaxy Z Fold, Google Pixel Fold, OnePlus Open) OR the **Android Emulator** with a foldable system image (e.g. Pixel Fold / Pixel 10 Pro Fold).

### Building Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/Vyom-2007/DuoFoldWallpaper.git
   cd DuoFoldWallpaper
   ```
2. Build the debug APK using Gradle wrapper:
   ```bash
   # On Linux/macOS
   ./gradlew assembleDebug

   # On Windows PowerShell
   .\gradlew.bat assembleDebug
   ```
3. Install to your connected device/emulator:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Testing on the Android Emulator

You do not need a physical foldable to contribute! You can drive the fold animation via ADB:

1. Launch a foldable AVD (e.g., Pixel 10 Pro Fold or Pixel Fold).
2. Set the live wallpaper in the Android settings or via the app's **"Set as Live Wallpaper"** button.
3. Send simulated hinge angles (0° to 180°):
   ```bash
   # Test 90 degrees (tabletop)
   adb emu sensor set hinge-angle0 90

   # Test 180 degrees (flat unfolded)
   adb emu sensor set hinge-angle0 180

   # Test 0 degrees (fully closed)
   adb emu sensor set hinge-angle0 0
   ```
4. Check real-time engine and display binding logs:
   ```bash
   adb logcat -s DuoWallpaperService,DeviceConfig
   ```

---

## Architecture & Codebase Tour

- [**ShaderCode.kt**](file:///a:/android/DuoFoldWallpaper/DuoFoldWallpaper/app/src/main/java/com/example/duofoldwallpaper/ShaderCode.kt): Contains the core AGSL (`RuntimeShader`) program. Computes the ray-plane perspective warp, original UV edge gradients, and 25-tap progressive blur.
- [**FoldMath.kt**](file:///a:/android/DuoFoldWallpaper/DuoFoldWallpaper/app/src/main/java/com/example/duofoldwallpaper/FoldMath.kt): Pure geometric transformations, smoothstep curves, outer UV offset calculations, and demo mode phase animations.
- [**DeviceConfig.kt**](file:///a:/android/DuoFoldWallpaper/DuoFoldWallpaper/app/src/main/java/com/example/duofoldwallpaper/DeviceConfig.kt): Hardware geometry abstraction. Detects physical display dimensions, density, hinge coordinate, and perspective ratios dynamically.
- [**DuoWallpaperService.kt**](file:///a:/android/DuoFoldWallpaper/DuoFoldWallpaper/app/src/main/java/com/example/duofoldwallpaper/DuoWallpaperService.kt): Android `WallpaperService` managing the per-display `Engine`, listening to `Sensor.TYPE_HINGE_ANGLE`, and orchestrating frame draws with Choreographer.
- [**MainActivity.kt**](file:///a:/android/DuoFoldWallpaper/DuoFoldWallpaper/app/src/main/java/com/example/duofoldwallpaper/MainActivity.kt): Configuration UI, custom image selector, and demo mode toggle.

---

## How to Pick an Issue & Submit a PR

1. Browse open issues with the [`good first issue`](https://github.com/Vyom-2007/DuoFoldWallpaper/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) tag.
2. Comment on the issue to let others know you are working on it.
3. Create a descriptive branch for your work:
   ```bash
   git checkout -b feat/recalibrate-fold-math
   ```
4. Ensure your changes compile cleanly without warnings:
   ```bash
   ./gradlew assembleDebug
   ```
5. Submit a Pull Request targeting the `main` branch with:
   - Clear description of the problem and your solution.
   - Screen recording or screenshots showing before/after behavior.
   - Mention the issue number (e.g. `Fixes #3`).

---

## Code Style & Guidelines

- **Zero Hardcoded Geometry**: All device-dependent numbers must be passed through [DeviceConfig](file:///a:/android/DuoFoldWallpaper/DuoFoldWallpaper/app/src/main/java/com/example/duofoldwallpaper/DeviceConfig.kt).
- **Battery Optimization**: Any continuous animation or frame callback must be conditional. When the hinge stops moving, the Choreographer loop must be paused.
- **Shader Performance**: Avoid expensive branch divergent instructions in AGSL inner loops. Always clamp UV lookups.
