# Duo Fold Wallpaper

Android live wallpaper that ports the "iPhone Duo" fold-transition effect
(hinge-anchored blur/dissolve between inner and outer screens) to a
Samsung Galaxy Z Fold 8, using `Sensor.TYPE_HINGE_ANGLE` + an AGSL
`RuntimeShader`.

This is a corrected, complete build of the code sketched out in an earlier
draft. What changed and why is below — read it before you spend time in a
debugger.

## What was fixed from the earlier draft

1. **`RuntimeShader.setInputBuffer(name, width, height, HardwareBuffer)`
   does not exist**, and neither does `HardwareBuffer.createFromBitmap()`.
   The real API is `RuntimeShader.setInputShader(name, Shader)`. This
   project builds a `BitmapShader` from the loaded bitmap and passes that
   in instead — see `DuoWallpaperService.onCreate()`.

2. **`surface.surfaceControl?.displayId` is not an accessible API** on
   `WallpaperService.Engine`. The documented way to tell which physical
   display an `Engine` is bound to is `getDisplayContext()?.display?.displayId`,
   which the platform docs guarantee is non-null once `onCreate()` has run.
   That's what `resolveDisplayRole()` uses now.

3. **The frame loop no longer runs unconditionally forever.** The
   `Choreographer` callback re-posts itself only while the smoothed fold
   value hasn't caught up to the latest sensor reading; once it settles,
   the loop stops until the next `onSensorChanged` event. This matters a
   lot for a wallpaper that's expected to sit idle on two 120 Hz displays
   most of the time.

4. Added basic robustness: a `try/catch` around the per-frame shader draw,
   a fallback gradient bitmap if `wallpaper.png` is missing or fails to
   decode, and proper sensor/Choreographer teardown in
   `onSurfaceDestroyed`/`onDestroy` (the earlier draft only tore down on
   `onVisibilityChanged(false)`, which isn't always called before
   destruction).

## The one thing that isn't fixed, because it can't be from code alone

**Whether Samsung's One UI will actually give a third-party live wallpaper
a second `Engine` bound to the Z Fold's physical cover screen is not
documented or guaranteed anywhere.** The Android platform docs only
promise that *"for multiple display environment, multiple engines can be
created to render on each display"* — they don't commit to cover screens
on foldables counting as such an environment for third-party apps, and
in practice this has been inconsistent across One UI versions for other
apps (some report needing "Apply to both screens" toggles that aren't
present in every One UI release; some report it silently not working).

**Test this first, before relying on it:**

```
adb logcat -s DuoWallpaperService
```

Set this app as your wallpaper, then fold/unfold the phone. Look for two
different lines like:

```
D DuoWallpaperService: Engine bound to displayId=0 outer=false
D DuoWallpaperService: Engine bound to displayId=<n> outer=true
```

- **If you see both** — great, the outer "window" effect is live, and the
  anchor math in `FoldMath.kt` is driving it.
- **If you only ever see the `displayId=0` line** — the wallpaper system
  isn't giving you a cover-screen surface. The inner display will still
  show a correct single-screen fold transition (it just won't drive the
  outer half). Your fallback in that case is the foreground-app approach
  from the original PoC thread: a normal `Activity` on the inner display
  plus an `android.app.Presentation` explicitly opened on the secondary
  `Display` from `DisplayManager` — that path doesn't depend on the
  wallpaper system routing anything for you, at the cost of not being a
  "true" system wallpaper (it has to be running in the foreground).

## Using your own image

A placeholder gradient image ships at
`app/src/main/res/drawable-nodpi/wallpaper.png` (2670×1878, matching the
canonical layout the anchor math assumes) so the project builds and runs
immediately. Replace that file with your own image at the same resolution
whenever you're ready — keep it in `drawable-nodpi` specifically, since
any other density bucket will let Android rescale it on load and throw
off the shader's pixel math.

## Build & install

```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or just open the project root in Android Studio and hit Run — the
Gradle wrapper (jar, `gradlew`, `gradlew.bat`) is already included,
pinned to Gradle 8.7 / AGP 8.6.0 / Kotlin 1.9.24, which support the
API 33+ `minSdk` this project requires (AGSL's `RuntimeShader` isn't
available below API 33).

Then, on the phone:

1. Long-press the home screen → **Wallpapers** → **Live Wallpapers**.
2. Select **Duo Fold Wallpaper**.
3. Or just launch the app — it has a single "Set as Live Wallpaper"
   button that jumps straight to this wallpaper in the system picker.

## Project layout

```
app/src/main/java/com/example/duofoldwallpaper/
  FoldMath.kt              -- ported anchor-point projection math
  ShaderCode.kt            -- the AGSL shader source
  DuoWallpaperService.kt   -- WallpaperService + per-display Engine
  MainActivity.kt          -- picker-launch convenience screen
app/src/main/res/
  drawable-nodpi/wallpaper.png  -- placeholder canonical image (replace me)
  xml/wallpaper.xml             -- wallpaper metadata descriptor
```
