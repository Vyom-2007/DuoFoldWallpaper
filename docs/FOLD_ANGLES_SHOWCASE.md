# Duo Fold Wallpaper — Hinge Angle Showcase (0° to 180°)

This document demonstrates the full sequence of fold transitions for **Duo Fold Wallpaper** captured at **10° increments** from **0° (fully closed)** to **180° (fully flat)**.

---

## Technical Summary of the Transition

The live wallpaper operates with three distinct stages governed by `Sensor.TYPE_HINGE_ANGLE`:

1. **Cover / Outer Screen Mode (0° to 30°)**:
   - As the device closes, the outer display becomes the active rendering target.
   - The shader projects the canonical image anchored to the physical fold seam (`FoldMath.computeOuterOffsetX`), dissolving gracefully into the closed state.
2. **Hinge Ray-Intersection Perspective Dissolve (40° to 110°)**:
   - On the inner foldable screen, the left wing undergoes dynamic 3D ray-cast perspective distortion (`FoldMath.anchorX(foldRadians)`).
   - Near the physical hinge line, an AGSL blur and motion dissolve effect scales with the fold angle (`FoldMath.smoothstep(progress)`).
3. **Flat Unfolded Alignment (120° to 180°)**:
   - As the phone flattens toward 180°, perspective distortion and hinge blur smoothly decay to zero.
   - At 180°, the canonical mountain landscape aligns perfectly flat across both panels with zero distortion.

---

## Summary Table: 0° to 180° (10° Separation)

| Angle | Active Display | `targetFold` (rad) | Motion Progress | Screenshot |
|:---:|:---:|:---:|:---:|:---:|
| **0°** | Outer / Cover | $\pi \approx 3.1416$ | 1.000 | [angle_0deg.png](./screenshots/angle_0deg.png) |
| **10°** | Outer / Cover | $0.94\pi \approx 2.967$ | 0.944 | [angle_10deg.png](./screenshots/angle_10deg.png) |
| **20°** | Outer / Cover | $0.89\pi \approx 2.793$ | 0.889 | [angle_20deg.png](./screenshots/angle_20deg.png) |
| **30°** | Outer / Cover | $0.83\pi \approx 2.618$ | 0.833 | [angle_30deg.png](./screenshots/angle_30deg.png) |
| **40°** | Inner Main | $0.78\pi \approx 2.443$ | 0.778 | [angle_40deg.png](./screenshots/angle_40deg.png) |
| **50°** | Inner Main | $0.72\pi \approx 2.269$ | 0.722 | [angle_50deg.png](./screenshots/angle_50deg.png) |
| **60°** | Inner Main | $0.67\pi \approx 2.094$ | 0.667 | [angle_60deg.png](./screenshots/angle_60deg.png) |
| **70°** | Inner Main | $0.61\pi \approx 1.920$ | 0.611 | [angle_70deg.png](./screenshots/angle_70deg.png) |
| **80°** | Inner Main | $0.56\pi \approx 1.745$ | 0.556 | [angle_80deg.png](./screenshots/angle_80deg.png) |
| **90°** | Inner Main | $0.50\pi \approx 1.571$ | 0.500 | [angle_90deg.png](./screenshots/angle_90deg.png) |
| **100°** | Inner Main | $0.44\pi \approx 1.396$ | 0.444 | [angle_100deg.png](./screenshots/angle_100deg.png) |
| **110°** | Inner Main | $0.39\pi \approx 1.222$ | 0.389 | [angle_110deg.png](./screenshots/angle_110deg.png) |
| **120°** | Inner Main | $0.33\pi \approx 1.047$ | 0.333 | [angle_120deg.png](./screenshots/angle_120deg.png) |
| **130°** | Inner Main | $0.28\pi \approx 0.873$ | 0.278 | [angle_130deg.png](./screenshots/angle_130deg.png) |
| **140°** | Inner Main | $0.22\pi \approx 0.698$ | 0.222 | [angle_140deg.png](./screenshots/angle_140deg.png) |
| **150°** | Inner Main | $0.17\pi \approx 0.524$ | 0.167 | [angle_150deg.png](./screenshots/angle_150deg.png) |
| **160°** | Inner Main | $0.11\pi \approx 0.349$ | 0.111 | [angle_160deg.png](./screenshots/angle_160deg.png) |
| **170°** | Inner Main | $0.06\pi \approx 0.175$ | 0.056 | [angle_170deg.png](./screenshots/angle_170deg.png) |
| **180°** | Inner Main | $0.00$ | 0.000 | [angle_180deg.png](./screenshots/angle_180deg.png) |

---

## Detailed Visual Breakdown

### Phase 1: Outer / Cover Screen (0° to 30°)

#### 0° — Fully Folded / Closed Cover Display
The phone is completely closed. The outer screen renders the right-hand portion of the wallpaper, locked to the fold edge.
![0 Degrees](./screenshots/angle_0deg.png)

#### 10° — Starting to Crack Open
Slight hinge opening detected by the sensor. Hinge blur begins to soften the seam.
![10 Degrees](./screenshots/angle_10deg.png)

#### 20° — Opening Angle
Smooth transition continues on outer screen prior to system display handoff.
![20 Degrees](./screenshots/angle_20deg.png)

#### 30° — Cover Screen Pre-Handoff
Final angle before the inner foldable display takes over primary focus.
![30 Degrees](./screenshots/angle_30deg.png)

---

### Phase 2: Flex / Tabletop Mode (40° to 110°)

#### 40° — Inner Display Active / Sharp Fold Angle
Inner screen is activated. Deep 3D perspective fold angle with maximum ray-cast distortion along the left edge.
![40 Degrees](./screenshots/angle_40deg.png)

#### 50° — Deep Fold Angle
Hinge blur gradient extends across the folding seam.
![50 Degrees](./screenshots/angle_50deg.png)

#### 60° — Strong Fold Transition
Clear depth separation between the left perspective-projected plane and the right planar display.
![60 Degrees](./screenshots/angle_60deg.png)

#### 70° — Acute Fold Posture
Hinge raycast projection progressively flattens.
![70 Degrees](./screenshots/angle_70deg.png)

#### 80° — Flex Mode Approaching 90°
Typical laptop/flex mode posture. Hinge distortion remains sharp and stable.
![80 Degrees](./screenshots/angle_80deg.png)

#### 90° — 90° Right Angle (Tabletop Mode)
Equal weight between inner fold and flat pane. The AGSL motion uniform reaches 0.50.
![90 Degrees](./screenshots/angle_90deg.png)

#### 100° — Opening Past Right Angle
Perspective stretching begins rapidly receding toward planar UV coordinates.
![100 Degrees](./screenshots/angle_100deg.png)

#### 110° — Wide Flex Angle
Subtle blur remaining along the hinge axis.
![110 Degrees](./screenshots/angle_110deg.png)

---

### Phase 3: Flat Unfolded Alignment (120° to 180°)

#### 120° — Gentle Fold Angle
Left panel perspective angle is almost parallel to the right panel.
![120 Degrees](./screenshots/angle_120deg.png)

#### 130° — Soft Transition
Shader blur continues diminishing.
![130 Degrees](./screenshots/angle_130deg.png)

#### 140° — Almost Open
Distortion is subtle and localized only within a small radius of the hinge center.
![140 Degrees](./screenshots/angle_140deg.png)

#### 150° — Near Flat
Nearly complete landscape continuity across the seam.
![150 Degrees](./screenshots/angle_150deg.png)

#### 160° — 20° from Flat
Minor residual blur settling to zero.
![160 Degrees](./screenshots/angle_160deg.png)

#### 170° — 10° from Flat
Imperceptible perspective offset; smoothstep settling toward rest state.
![170 Degrees](./screenshots/angle_170deg.png)

#### 180° — Completely Flat (Unfolded)
Sensor detects 180°. Zero distortion, zero blur. The full canonical mountain panorama spans both screens seamlessly.
![180 Degrees](./screenshots/angle_180deg.png)

---

## How to Re-run or Automate this Capture

To re-run the full 0°–180° 10-degree sweep at any time:

```powershell
$adb = "C:\Users\devve\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$outDir = "a:\android\DuoFoldWallpaper\DuoFoldWallpaper\screenshots"

0..18 | ForEach-Object {
    $angle = $_ * 10
    & $adb -s emulator-5554 emu sensor set hinge-angle0 $angle
    Start-Sleep -Milliseconds 700
    & $adb -s emulator-5554 shell screencap -p "/sdcard/angle_${angle}deg.png"
    & $adb -s emulator-5554 pull "/sdcard/angle_${angle}deg.png" "$outDir\angle_${angle}deg.png"
}
```
