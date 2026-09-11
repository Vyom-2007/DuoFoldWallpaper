# DuoFoldWallpaper — Complete Launch Kit & Ready-to-Post Drafts

Official Links for all posts:
- **GitHub Repository**: `https://github.com/Vyom-2007/DuoFoldWallpaper`
- **Interactive 0°–180° Web Showcase**: `https://vyom-2007.github.io/DuoFoldWallpaper/`
- **Pre-Built APK Release**: `https://github.com/Vyom-2007/DuoFoldWallpaper/releases/tag/v1.0.0`
- **Hero Image / Video Asset**: `docs/pixel_fold_hero.png`

---

## 1. Reddit Announcements

### Post 1: r/GalaxyFold (Day 0 — Priority #1)
> **Subreddit**: [r/GalaxyFold](https://reddit.com/r/GalaxyFold)  
> **Flair**: `Mod / Customization` or `Discussion`  
> **Media**: Upload the 15-second screen recording / GIF of the fold transition.

**Title**:
```text
I rebuilt the iPhone Duo's viral fold animation as an open-source live wallpaper for the Z Fold & Pixel Fold (AGSL shader + hinge sensor)
```

**Body**:
```markdown
When Apple showed the iPhone Duo's unfold animation, the visual effect went viral. A few days ago, u/moomanjohnny shared a proof-of-concept showing that Android's hinge sensor could drive a similar effect.

I decided to take it all the way to a full, production-ready Android live wallpaper:

- **120 Hz Hardware Hinge Sensor (`Sensor.TYPE_HINGE_ANGLE`)**: Drives the fold state in real time with low-pass jitter smoothing.
- **Battery-Conscious Choreographer Loop**: Automatically pauses rendering when the hinge settles, so idling won't drain your battery.
- **AGSL RuntimeShader (`ShaderCode.kt`)**: 25-tap progressive blur kernel with cosine perspective compression and original-UV gradient mapping (no edge streaking or color void artifacts).
- **Dual-Display Architecture**: Spawns independent `WallpaperService.Engine` instances per physical display, projecting a hinge-anchored crop onto the cover screen so it behaves like a physical "window" into the inner canvas.
- **Custom Image Picker**: You can load any custom wallpaper or photo from your gallery.

### Links
- **GitHub (MIT)**: https://github.com/Vyom-2007/DuoFoldWallpaper
- **Interactive 0°–180° Showcase (in-browser)**: https://vyom-2007.github.io/DuoFoldWallpaper/
- **Pre-built APK**: https://github.com/Vyom-2007/DuoFoldWallpaper/releases/tag/v1.0.0

### Honest Limitations
1. **Samsung One UI Cover Engine**: Android supports multi-display wallpapers, but depending on your One UI version, Samsung sometimes restricts third-party wallpapers to single-engine mode on the outer screen. The README includes a logcat check (`adb logcat -s DuoWallpaperService`) to test your device.
2. **Blur Kernel**: Currently a PoC-grade 25-tap Gaussian AGSL kernel rather than a mipmapped pyramid. Contributions on Vulkan mip-chain downsampling are warmly welcome!

Feedback on the 90° tabletop frame and fold geometry is appreciated. Happy to answer any technical questions in the comments!
```

---

### Post 2: r/androiddev (Day 1 — Engineering Deep-Dive)
> **Subreddit**: [r/androiddev](https://reddit.com/r/androiddev)  
> **Flair**: `Show & Tell`

**Title**:
```text
Porting Apple's iPhone Duo fold animation to Android foldables with AGSL RuntimeShader and Sensor.TYPE_HINGE_ANGLE (Open Source)
```

**Body**:
```markdown
Hey everyone! Over the past week, I reverse-engineered and ported the iPhone Duo's fold transition animation to Android foldables (Google Pixel Fold / 9 Pro Fold / 10 Pro Fold and Samsung Galaxy Z Fold series).

The project is completely open source under MIT:
https://github.com/Vyom-2007/DuoFoldWallpaper

### Key Technical Details

1. **AGSL Shader Implementation**:
   - Written in Android Graphics Shading Language (`RuntimeShader`, API 33+).
   - Ray-plane perspective foreshortening: $z = d \cdot \sin(\theta)$ and perspective scaling: $S = \frac{E}{E + z}$.
   - Cosine compression: $X_{\text{warped}} = X_{\text{hinge}} - d \cdot \cos(\theta) \cdot S$.
   - OrigUV mapping: Avoids horizontal color-void streaking on the non-folding panel by sampling coordinates relative to canonical image space.
   - Dual-resolution 25-tap progressive blur weighted by fold progression.

2. **Hinge Sensor & Choreographer Lifecycle**:
   - Listens to `Sensor.TYPE_HINGE_ANGLE` at fastest rate.
   - To avoid wake-lock battery drain, the `Choreographer.FrameCallback` loop dynamically sleeps whenever angular delta falls below $\epsilon = 0.001$ rad and wakes immediately on sensor interrupt.

3. **Multi-Display Architecture & OEM Idiosyncrasies**:
   - `WallpaperService` creates an engine per physical display (`Display.DEFAULT_DISPLAY` vs secondary cover display).
   - Projects an affine crop of the canonical image onto the outer screen.

### Code & Live Demo
- **Repo**: https://github.com/Vyom-2007/DuoFoldWallpaper
- **Web Showcase**: https://vyom-2007.github.io/DuoFoldWallpaper/

Would love feedback on AGSL optimizations or experiences with multi-display WallpaperService quirks across different OEM builds.
```

---

### Post 3: r/Android & r/Samsung (Day 2–3)
> **Subreddit**: [r/Android](https://reddit.com/r/Android) & [r/Samsung](https://reddit.com/r/Samsung)  
> **Flair**: `App` / `Customization`

**Title**:
```text
I ported the viral iPhone Duo fold wallpaper animation to Galaxy Z Fold & Pixel Fold — free and open source
```

**Body**:
```markdown
Like many of you, I saw the viral iPhone Duo unfold animation last week. Rather than waiting years for OEM support, I built an open-source Android live wallpaper that brings the exact same hinge-reactive 3D fold perspective, blur, and cover-screen window effect to Android foldables.

**Features:**
- Real-time folding driven by your phone's physical hinge angle sensor.
- Zero battery drain when phone is resting/idle.
- Custom image support: pick any picture from your gallery.
- Open-source, no ads, no trackers, no permissions needed except the wallpaper picker.

**Download & Info:**
- APK Release: https://github.com/Vyom-2007/DuoFoldWallpaper/releases/tag/v1.0.0
- Web Demo (try the slider on any phone/PC): https://vyom-2007.github.io/DuoFoldWallpaper/
- GitHub Source: https://github.com/Vyom-2007/DuoFoldWallpaper
```

---

## 2. X / Twitter Announcement Thread

### Tweet 1 (Hook + Video):
```text
Apple's iPhone Duo fold animation is the most talked-about UI effect of the year.

So I ported it to the Samsung Galaxy Z Fold & Pixel Fold. 
As a live wallpaper. 
100% open source. 🧵👇

[ATTACH HERO VIDEO OR GIF]
```

### Tweet 2 (How it works):
```text
2/ How it works:
• Sensor.TYPE_HINGE_ANGLE tracks physical folding at 120 Hz with low-pass smoothing
• Custom AGSL RuntimeShader applies 3D ray-plane perspective warp + 25-tap progressive blur
• Choreographer loop auto-pauses when the hinge settles, so zero battery drain on idle ⚡
```

### Tweet 3 (The Cover Screen Window):
```text
3/ The cover screen acts as a physical "window" into the inner screen:
We spawn independent WallpaperService Engines per display and project a hinge-anchored affine crop onto the outer panel. When you close the phone, the wallpaper aligns seamlessly with the edge.
```

### Tweet 4 (Links & Web Showcase):
```text
4/ Try it out or contribute (MIT License):

📦 GitHub: https://github.com/Vyom-2007/DuoFoldWallpaper
🌐 Live 0°–180° In-Browser Scrubber: https://vyom-2007.github.io/DuoFoldWallpaper/
📲 Download APK: https://github.com/Vyom-2007/DuoFoldWallpaper/releases/tag/v1.0.0

Built with Kotlin & AGSL. RTs appreciated! #AndroidDev #GalaxyZFold #PixelFold #OpenSource
```

---

## 3. Hacker News (Show HN)

> **Submission URL**: `https://github.com/Vyom-2007/DuoFoldWallpaper`  
> **Optimal Time**: Tuesday / Wednesday, 7:00 AM – 9:00 AM PT

**Title**:
```text
Show HN: DuoFoldWallpaper – iPhone Duo fold transition ported to Android foldables
```

**First Comment (Post immediately after submission)**:
```markdown
Hi HN! When Apple showcased the iPhone Duo unfold animation, I wanted to see how closely we could recreate the visual transition on Android foldables using public APIs.

This live wallpaper ports the effect to devices like the Google Pixel Fold series and Samsung Galaxy Z Fold using:
1. `Sensor.TYPE_HINGE_ANGLE` sampled with a low-pass filter to eliminate sensor noise.
2. An AGSL (Android Graphics Shading Language) `RuntimeShader` performing ray-plane perspective foreshortening ($z = d \cdot \sin(\theta)$) and cosine compression ($X_{\text{warped}} = X_{\text{hinge}} - d \cdot \cos(\theta) \cdot S$), coupled with a 25-tap progressive blur.
3. Multi-display `WallpaperService.Engine` instances mapping an affine window crop to the cover display.
4. A battery-conscious render loop: `Choreographer.FrameCallback` sleeps whenever the hinge angle velocity drops to zero.

If you don't have a foldable nearby, I built an interactive 0°–180° angle showcase you can scrub through in your browser via GitHub Pages:
https://vyom-2007.github.io/DuoFoldWallpaper/

The repo is MIT-licensed, with prebuilt APKs available under Releases. Would love your feedback on the math and shader pipeline!
```

---

## 4. LinkedIn (Engineering Story Angle)

```markdown
Last week Apple unveiled the iPhone Duo, and its fold transition animation quickly became the most discussed mobile UI effect of the year.

I spent the weekend asking an engineering question: Can this exact transition be faithfully ported to Android foldables using native, public APIs?

The result is **DuoFoldWallpaper** — an open-source Android live wallpaper for the Galaxy Z Fold and Google Pixel Fold series.

Here is what made it an exciting graphics and systems challenge:

🔹 **AGSL Ray-Plane Shader**: Replicating 3D perspective foreshortening ($z = d \cdot \sin(\theta)$) and cosine compression inside a 2D AGSL RuntimeShader, paired with an original-UV gradient mapping that eliminates horizontal streaking.
🔹 **Battery-Conscious 120 Hz Loop**: Listening to `Sensor.TYPE_HINGE_ANGLE` at high frequency while ensuring the Choreographer frame loop automatically parks itself when the hinge settles, preventing battery drain.
🔹 **Multi-Display Architecture**: Instantiating independent `WallpaperService.Engine` surfaces per physical screen so the closed cover display acts as a calibrated viewport into the unfolded canvas.
🔹 **OEM Edge Cases**: Discovering and documenting how different Android OEM builds handle multi-display wallpaper surfaces.

The project is fully open source (MIT), complete with an in-browser 0°–180° interactive showcase:

👉 GitHub Repo: https://github.com/Vyom-2007/DuoFoldWallpaper
👉 Interactive Web Showcase: https://vyom-2007.github.io/DuoFoldWallpaper/
👉 Prebuilt APK Release: https://github.com/Vyom-2007/DuoFoldWallpaper/releases/tag/v1.0.0

Contributions, discussions, and shader optimizations are warmly welcome!

#AndroidDev #MobileEngineering #ComputerGraphics #Kotlin #Foldables #OpenSource #Android #SoftwareEngineering
```

---

## 5. Short-Form Video (TikTok / Reels / YouTube Shorts)

### Video Concept
- **Duration**: 15–20 seconds
- **Format**: Vertical 9:16
- **Music**: Trending minimal tech / aesthetic synth audio

### Shot List & Script:
1. **0:00 – 0:03 (The Hook)**:
   - *Visual*: Close-up on the phone folded closed.
   - *Text Overlay*: "Apple spent $1,999 on this fold animation..."
2. **0:03 – 0:09 (The Reveal)**:
   - *Visual*: Smoothly unfold the phone to 90° tabletop, then to 180° flat landscape. Watch the wallpaper compress, blur, and expand seamlessly.
   - *Text Overlay*: "...so I put it on an Android phone for FREE."
3. **0:09 – 0:15 (The Cover Screen Window)**:
   - *Visual*: Close the phone; show the outer screen seamlessly displaying the edge crop.
   - *Text Overlay*: "Cover screen acts like a physical window."
4. **0:15 – 0:18 (Call to Action)**:
   - *Visual*: Screen showing GitHub repo or web demo.
   - *Text Overlay*: "100% Free & Open Source. Link in bio! 📲"

**Caption**:
```text
Apple's viral iPhone Duo fold animation running on Android! 🤯 Live wallpaper driven by the physical hinge sensor. Free & open source. Link in bio to download!

#GalaxyZFold #PixelFold #iPhoneDuo #Android #TechTok #FoldablePhone #OpenSource #Samsung #Tech
```

---

## 6. Dev.to / Medium Article Outline

**Title**:
```text
Porting Apple's iPhone Duo Fold Animation to Android with AGSL and Hinge Sensors
```

**Subtitle**:
```text
How to build a 120 Hz hinge-reactive 3D perspective live wallpaper using Android Graphics Shading Language (AGSL) and WallpaperService.
```

**Key Sections**:
1. **Introduction**: The hype behind the iPhone Duo unfold animation and the challenge of porting it to Android.
2. **The Geometry of a Fold**: Explaining ray-plane perspective scaling ($S = \frac{E}{E + z}$) and cosine displacement.
3. **Writing the AGSL Shader**: Overcoming texture coordinates stretching, original UV mapping, and implementing the 25-tap progressive blur.
4. **Lifecycle & Battery Optimization**: Why naive frame loops destroy battery and how Choreographer parking solves it.
5. **Handling Dual Displays**: Multi-engine `WallpaperService` vs cover screen limitations.
6. **Try It Yourself**: Links to GitHub repo, web demo, and pre-built APK.

---

## 7. Execution Checklist & Momentum Rules

| Timing | Platform | Key Action |
|---|---|---|
| **Day 0 (9:00 AM ET)** | Reddit (`r/GalaxyFold`) + X Thread | Post main hero video + link to repo & web showcase. |
| **Day 0 (Evening)** | TikTok / Reels / Shorts | Post vertical video with "Apple's $1,999 animation on Android" hook. |
| **Day 1 (7:00 AM PT)** | Hacker News (Show HN) + LinkedIn | Post technical writeup and stay online to answer comments. |
| **Day 2–3** | `r/androiddev` & `r/Android` | Post developer-focused breakdown and APK download guide. |
| **First 6 Hours Rule** | All Platforms | **Reply to every comment within 15–30 minutes.** Algorithms prioritize early engagement velocity! |
