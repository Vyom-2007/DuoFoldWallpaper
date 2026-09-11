import os
import io
import json
import base64
from PIL import Image

screenshots_dir = r"a:\android\DuoFoldWallpaper\DuoFoldWallpaper\screenshots"
output_html_path = r"a:\android\DuoFoldWallpaper\DuoFoldWallpaper\DuoFold_Showcase.html"

# Angle metadata definitions
angle_data = [
    {
        "angle": 0,
        "display": "Outer Cover Screen",
        "phase": "cover",
        "phase_label": "Cover Screen",
        "resolution": "1080 x 2424",
        "radians": "3.1416 (π rad)",
        "motion": "1.000",
        "desc": "Phone completely closed. Wallpaper pinned to hinge seam on outer display with zero inner rendering."
    },
    {
        "angle": 10,
        "display": "Outer Cover Screen",
        "phase": "cover",
        "phase_label": "Cover Screen",
        "resolution": "1080 x 2424",
        "radians": "2.9671 (0.94π)",
        "motion": "0.944",
        "desc": "Initial crack open. Seam blur begins to soften outer display edge."
    },
    {
        "angle": 20,
        "display": "Outer Cover Screen",
        "phase": "cover",
        "phase_label": "Cover Screen",
        "resolution": "1080 x 2424",
        "radians": "2.7925 (0.89π)",
        "motion": "0.889",
        "desc": "Continuous UV perspective translation tracking physical hinge rotation."
    },
    {
        "angle": 30,
        "display": "Outer Cover Screen",
        "phase": "cover",
        "phase_label": "Cover Screen",
        "resolution": "1080 x 2424",
        "radians": "2.6180 (0.83π)",
        "motion": "0.833",
        "desc": "Final cover screen frame before system handoff to inner foldable display."
    },
    {
        "angle": 40,
        "display": "Inner Foldable Screen",
        "phase": "flex",
        "phase_label": "Flex Mode",
        "resolution": "2076 x 2152",
        "radians": "2.4435 (0.78π)",
        "motion": "0.778",
        "desc": "Inner screen active. Maximum 3D ray-cast perspective distortion on left wing."
    },
    {
        "angle": 50,
        "display": "Inner Foldable Screen",
        "phase": "flex",
        "phase_label": "Flex Mode",
        "resolution": "2076 x 2152",
        "radians": "2.2689 (0.72π)",
        "motion": "0.722",
        "desc": "Acute fold. Hinge blur band expands along the central folding axis."
    },
    {
        "angle": 60,
        "display": "Inner Foldable Screen",
        "phase": "flex",
        "phase_label": "Flex Mode",
        "resolution": "2076 x 2152",
        "radians": "2.0944 (0.67π)",
        "motion": "0.667",
        "desc": "Distinct depth disparity: perspective left wing contrasted against planar right panel."
    },
    {
        "angle": 70,
        "display": "Inner Foldable Screen",
        "phase": "flex",
        "phase_label": "Flex Mode",
        "resolution": "2076 x 2152",
        "radians": "1.9199 (0.61π)",
        "motion": "0.611",
        "desc": "Hinge raycast uncurls progressively as angle opens toward 90°."
    },
    {
        "angle": 80,
        "display": "Inner Foldable Screen",
        "phase": "flex",
        "phase_label": "Flex Mode",
        "resolution": "2076 x 2152",
        "radians": "1.7453 (0.56π)",
        "motion": "0.556",
        "desc": "Laptop flex posture. Hinge distortion remains stable and focused."
    },
    {
        "angle": 90,
        "display": "Inner Foldable Screen",
        "phase": "flex",
        "phase_label": "Flex Mode",
        "resolution": "2076 x 2152",
        "radians": "1.5708 (0.50π)",
        "motion": "0.500",
        "desc": "True 90° tabletop mode. Midpoint transition uniform with balanced blur gradient."
    },
    {
        "angle": 100,
        "display": "Inner Foldable Screen",
        "phase": "flex",
        "phase_label": "Flex Mode",
        "resolution": "2076 x 2152",
        "radians": "1.3963 (0.44π)",
        "motion": "0.444",
        "desc": "Opening past right angle. Projection distortion recedes toward planar UV space."
    },
    {
        "angle": 110,
        "display": "Inner Foldable Screen",
        "phase": "flex",
        "phase_label": "Flex Mode",
        "resolution": "2076 x 2152",
        "radians": "1.2217 (0.39π)",
        "motion": "0.389",
        "desc": "Wide flex angle. Subtle blur remaining along the hinge axis."
    },
    {
        "angle": 120,
        "display": "Inner Foldable Screen",
        "phase": "flat",
        "phase_label": "Flat Alignment",
        "resolution": "2076 x 2152",
        "radians": "1.0472 (0.33π)",
        "motion": "0.333",
        "desc": "Left and right panel perspective angles approach parallel alignment."
    },
    {
        "angle": 130,
        "display": "Inner Foldable Screen",
        "phase": "flat",
        "phase_label": "Flat Alignment",
        "resolution": "2076 x 2152",
        "radians": "0.8727 (0.28π)",
        "motion": "0.278",
        "desc": "Shader blur decays significantly; landscape continuity rapidly emerging."
    },
    {
        "angle": 140,
        "display": "Inner Foldable Screen",
        "phase": "flat",
        "phase_label": "Flat Alignment",
        "resolution": "2076 x 2152",
        "radians": "0.6981 (0.22π)",
        "motion": "0.222",
        "desc": "Distortion tightly localized strictly to central hinge strip."
    },
    {
        "angle": 150,
        "display": "Inner Foldable Screen",
        "phase": "flat",
        "phase_label": "Flat Alignment",
        "resolution": "2076 x 2152",
        "radians": "0.5236 (0.17π)",
        "motion": "0.167",
        "desc": "Near flat posture. Full mountain landscape clearly aligned across both panels."
    },
    {
        "angle": 160,
        "display": "Inner Foldable Screen",
        "phase": "flat",
        "phase_label": "Flat Alignment",
        "resolution": "2076 x 2152",
        "radians": "0.3491 (0.11π)",
        "motion": "0.111",
        "desc": "20° from flat. Minor residual blur smoothly settling to zero."
    },
    {
        "angle": 170,
        "display": "Inner Foldable Screen",
        "phase": "flat",
        "phase_label": "Flat Alignment",
        "resolution": "2076 x 2152",
        "radians": "0.1745 (0.06π)",
        "motion": "0.056",
        "desc": "10° from flat. Virtually indistinguishable from completely flat panorama."
    },
    {
        "angle": 180,
        "display": "Inner Foldable Screen",
        "phase": "flat",
        "phase_label": "Flat Alignment",
        "resolution": "2076 x 2152",
        "radians": "0.0000 (0.00 rad)",
        "motion": "0.000",
        "desc": "Completely flat (unfolded). Zero distortion, zero blur. Flawless 2-panel panorama."
    }
]

# Convert all images to WebP base64
print("Encoding images to WebP base64...")
for item in angle_data:
    a = item["angle"]
    file_path = os.path.join(screenshots_dir, f"angle_{a}deg.png")
    im = Image.open(file_path)
    im.thumbnail((1200, 1200), Image.Resampling.LANCZOS)
    buf = io.BytesIO()
    im.save(buf, format="WEBP", quality=88)
    b64_str = base64.b64encode(buf.getvalue()).decode("ascii")
    item["data_uri"] = f"data:image/webp;base64,{b64_str}"

data_json = json.dumps(angle_data)

html_template = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Duo Fold Wallpaper — 0° to 180° Hinge Angle Showcase</title>
<style>
:root {{
  --bg-primary: #0b0d14;
  --bg-card: rgba(22, 27, 44, 0.7);
  --bg-card-hover: rgba(30, 37, 60, 0.85);
  --border-color: rgba(255, 255, 255, 0.08);
  --border-active: rgba(99, 102, 241, 0.6);
  --accent-primary: #6366f1;
  --accent-secondary: #a855f7;
  --accent-cyan: #06b6d4;
  --text-main: #f3f4f6;
  --text-muted: #9ca3af;
  --text-dim: #6b7280;
  --font-sans: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}}

* {{
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}}

body {{
  background-color: var(--bg-primary);
  background-image: 
    radial-gradient(ellipse at 20% 0%, rgba(99, 102, 241, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 10%, rgba(168, 85, 247, 0.12) 0%, transparent 45%);
  color: var(--text-main);
  font-family: var(--font-sans);
  line-height: 1.5;
  min-height: 100vh;
  padding: 24px 16px 64px;
}}

.container {{
  max-width: 1280px;
  margin: 0 auto;
}}

/* Header */
header {{
  text-align: center;
  margin-bottom: 32px;
}}

.badge-row {{
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}}

.badge {{
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border: 1px solid var(--border-color);
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-muted);
}}

.badge.neon {{
  background: rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.4);
  color: #a5b4fc;
}}

h1 {{
  font-size: clamp(1.8rem, 4vw, 2.75rem);
  font-weight: 800;
  letter-spacing: -0.03em;
  background: linear-gradient(135deg, #ffffff 40%, #a5b4fc 80%, #c084fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
}}

.subtitle {{
  color: var(--text-muted);
  font-size: clamp(0.95rem, 2vw, 1.1rem);
  max-width: 680px;
  margin: 0 auto;
}}

/* Interactive Scrubber Stage */
.stage-card {{
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 40px;
  box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.5);
}}

.stage-grid {{
  display: grid;
  grid-template-columns: 1fr;
  gap: 28px;
}}

@media (min-width: 900px) {{
  .stage-grid {{
    grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
    align-items: center;
  }}
}}

.viewport-container {{
  position: relative;
  background: #050608;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.6);
  aspect-ratio: 1 / 1;
  max-height: 540px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  width: 100%;
}}

.viewport-img {{
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: opacity 0.15s ease-out;
}}

.viewport-overlay {{
  position: absolute;
  top: 14px;
  left: 14px;
  display: flex;
  gap: 8px;
}}

.viewport-badge {{
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #fff;
  padding: 5px 10px;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}}

/* Stage Controls */
.controls-panel {{
  display: flex;
  flex-direction: column;
  gap: 20px;
}}

.angle-headline {{
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}}

.angle-number {{
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(135deg, #ffffff, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}}

.angle-deg {{
  font-size: 2rem;
  color: #818cf8;
}}

.slider-box {{
  display: flex;
  flex-direction: column;
  gap: 8px;
}}

.slider-labels {{
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--text-dim);
  font-weight: 600;
}}

input[type="range"] {{
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  height: 10px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.12);
  outline: none;
  cursor: pointer;
  transition: background 0.2s;
}}

input[type="range"]::-webkit-slider-thumb {{
  -webkit-appearance: none;
  appearance: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #6366f1;
  box-shadow: 0 0 15px rgba(99, 102, 241, 0.8);
  border: 2px solid #ffffff;
  cursor: pointer;
  transition: transform 0.1s, background 0.2s;
}}

input[type="range"]::-webkit-slider-thumb:hover {{
  transform: scale(1.15);
  background: #4f46e5;
}}

/* Quick presets */
.preset-buttons {{
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}}

.btn {{
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--border-color);
  color: var(--text-main);
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}}

.btn:hover {{
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}}

.btn.primary {{
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}}

.btn.primary:hover {{
  box-shadow: 0 6px 18px rgba(99, 102, 241, 0.45);
}}

.btn.active {{
  border-color: var(--accent-primary);
  background: rgba(99, 102, 241, 0.2);
  color: #c7d2fe;
}}

/* Telemetry Card */
.telemetry-grid {{
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 12px;
  padding: 14px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}}

.tele-item {{
  display: flex;
  flex-direction: column;
  gap: 2px;
}}

.tele-label {{
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-dim);
  font-weight: 600;
}}

.tele-val {{
  font-size: 0.9rem;
  font-weight: 700;
  color: #e0e7ff;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}}

.effect-desc {{
  font-size: 0.9rem;
  color: var(--text-muted);
  background: rgba(99, 102, 241, 0.06);
  border-left: 3px solid var(--accent-primary);
  padding: 10px 14px;
  border-radius: 0 8px 8px 0;
}}

/* Filter Tabs */
.gallery-header {{
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}}

@media (min-width: 640px) {{
  .gallery-header {{
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }}
}}

.section-title {{
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}}

.filter-tabs {{
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}}

.tab-btn {{
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-color);
  color: var(--text-muted);
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}}

.tab-btn:hover {{
  color: #ffffff;
  background: rgba(255, 255, 255, 0.08);
}}

.tab-btn.active {{
  background: var(--accent-primary);
  border-color: var(--accent-primary);
  color: #ffffff;
}}

/* Gallery Grid */
.cards-grid {{
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}}

.angle-card {{
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;
}}

.angle-card:hover {{
  transform: translateY(-4px);
  border-color: var(--border-active);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.4);
}}

.angle-card.current {{
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.4);
}}

.card-thumb-wrap {{
  position: relative;
  background: #050608;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}}

.card-thumb {{
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.2s;
}}

.angle-card:hover .card-thumb {{
  transform: scale(1.03);
}}

.card-badge {{
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  color: #ffffff;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.15);
}}

.card-body {{
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}}

.card-title-row {{
  display: flex;
  justify-content: space-between;
  align-items: center;
}}

.card-angle {{
  font-size: 1.1rem;
  font-weight: 800;
  color: #ffffff;
}}

.card-phase {{
  font-size: 0.7rem;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
}}

.card-desc {{
  font-size: 0.78rem;
  color: var(--text-muted);
  line-height: 1.4;
  margin-top: 2px;
}}

/* Modal / Lightbox */
.modal-overlay {{
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.88);
  backdrop-filter: blur(8px);
  display: none;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}}

.modal-overlay.open {{
  display: flex;
}}

.modal-content {{
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}}

.modal-img {{
  max-width: 100%;
  max-height: 80vh;
  border-radius: 12px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
}}

.modal-close {{
  position: absolute;
  top: -40px;
  right: 0;
  background: transparent;
  border: none;
  color: #fff;
  font-size: 2rem;
  cursor: pointer;
  line-height: 1;
}}

.modal-info {{
  margin-top: 12px;
  text-align: center;
  color: #cbd5e1;
  font-size: 0.9rem;
}}
</style>
</head>
<body>

<div class="container">
  <header>
    <div class="badge-row">
      <span class="badge neon">Android 14+ / API 33+</span>
      <span class="badge">AGSL RuntimeShader</span>
      <span class="badge">Sensor.TYPE_HINGE_ANGLE</span>
    </div>
    <h1>Duo Fold Live Wallpaper</h1>
    <p class="subtitle">Complete 0° to 180° Hinge Angle Progression (10° Separation) on Foldable Device</p>
  </header>

  <!-- Interactive Stage -->
  <div class="stage-card">
    <div class="stage-grid">
      <!-- Image Viewport -->
      <div class="viewport-container" id="viewportWrap">
        <img id="viewportImg" class="viewport-img" src="" alt="Active Screen">
        <div class="viewport-overlay">
          <span class="viewport-badge" id="viewportBadge">180°</span>
          <span class="viewport-badge" id="viewportDisplayBadge">Inner Main</span>
        </div>
      </div>

      <!-- Controls Panel -->
      <div class="controls-panel">
        <div class="angle-headline">
          <div>
            <div style="font-size: 0.8rem; color: var(--text-dim); font-weight: 700; text-transform: uppercase;">Current Hinge Angle</div>
            <div class="angle-number"><span id="angleText">180</span><span class="angle-deg">°</span></div>
          </div>
          <button class="btn primary" id="playBtn">▶ Play Animation</button>
        </div>

        <div class="slider-box">
          <div class="slider-labels">
            <span>0° (Closed Cover)</span>
            <span>90° (Tabletop)</span>
            <span>180° (Flat Open)</span>
          </div>
          <input type="range" id="angleSlider" min="0" max="180" step="10" value="180">
        </div>

        <div class="preset-buttons">
          <button class="btn" onclick="setAngle(0)">0° Closed</button>
          <button class="btn" onclick="setAngle(40)">40° Flex Init</button>
          <button class="btn" onclick="setAngle(90)">90° Tabletop</button>
          <button class="btn" onclick="setAngle(140)">140° Open</button>
          <button class="btn" onclick="setAngle(180)">180° Flat</button>
        </div>

        <!-- Telemetry HUD -->
        <div class="telemetry-grid">
          <div class="tele-item">
            <span class="tele-label">Active Display Target</span>
            <span class="tele-val" id="teleDisplay">Inner Foldable Screen</span>
          </div>
          <div class="tele-item">
            <span class="tele-label">Resolution</span>
            <span class="tele-val" id="teleRes">2076 x 2152</span>
          </div>
          <div class="tele-item">
            <span class="tele-label">Fold Radians (targetFold)</span>
            <span class="tele-val" id="teleRadians">0.0000 rad</span>
          </div>
          <div class="tele-item">
            <span class="tele-label">Motion Parameter (uMotion)</span>
            <span class="tele-val" id="teleMotion">0.000</span>
          </div>
        </div>

        <div class="effect-desc" id="teleDesc">
          Completely flat (unfolded). Zero distortion, zero blur. Full canonical panorama spans both screens.
        </div>
      </div>
    </div>
  </div>

  <!-- Gallery Section -->
  <div class="gallery-header">
    <div>
      <h2 class="section-title">All Captured Angles (19 Frames)</h2>
      <p style="color: var(--text-dim); font-size: 0.85rem;">Click any card to inspect or scrub the live player</p>
    </div>
    <div class="filter-tabs">
      <button class="tab-btn active" onclick="filterGallery('all', this)">All (19)</button>
      <button class="tab-btn" onclick="filterGallery('cover', this)">Cover Display (4)</button>
      <button class="tab-btn" onclick="filterGallery('flex', this)">Flex Mode (8)</button>
      <button class="tab-btn" onclick="filterGallery('flat', this)">Flat Mode (7)</button>
    </div>
  </div>

  <!-- Cards Grid -->
  <div class="cards-grid" id="cardsGrid"></div>
</div>

<!-- Modal Lightbox -->
<div class="modal-overlay" id="modalOverlay" onclick="closeModal(event)">
  <div class="modal-content">
    <button class="modal-close" onclick="closeModalDirect()">&times;</button>
    <img id="modalImg" class="modal-img" src="" alt="Fullscreen Capture">
    <div class="modal-info" id="modalInfo"></div>
  </div>
</div>

<script>
const angleData = {data_json};

let currentAngle = 180;
let isPlaying = false;
let playInterval = null;
let playDirection = -10; // start folding down from 180

const slider = document.getElementById('angleSlider');
const angleText = document.getElementById('angleText');
const viewportImg = document.getElementById('viewportImg');
const viewportBadge = document.getElementById('viewportBadge');
const viewportDisplayBadge = document.getElementById('viewportDisplayBadge');
const teleDisplay = document.getElementById('teleDisplay');
const teleRes = document.getElementById('teleRes');
const teleRadians = document.getElementById('teleRadians');
const teleMotion = document.getElementById('teleMotion');
const teleDesc = document.getElementById('teleDesc');
const playBtn = document.getElementById('playBtn');
const cardsGrid = document.getElementById('cardsGrid');

function renderGrid(filter = 'all') {{
  cardsGrid.innerHTML = '';
  angleData.forEach(item => {{
    if (filter !== 'all' && item.phase !== filter) return;
    
    const card = document.createElement('div');
    card.className = `angle-card ${{item.angle === currentAngle ? 'current' : ''}}`;
    card.id = `card-${{item.angle}}`;
    card.onclick = () => {{
      setAngle(item.angle);
      openModal(item);
    }};
    
    card.innerHTML = `
      <div class="card-thumb-wrap">
        <img class="card-thumb" src="${{item.data_uri}}" alt="${{item.angle}} degrees" loading="lazy">
        <div class="card-badge">${{item.angle}}°</div>
      </div>
      <div class="card-body">
        <div class="card-title-row">
          <span class="card-angle">${{item.angle}}°</span>
          <span class="card-phase">${{item.phase_label}}</span>
        </div>
        <div style="font-size: 0.72rem; color: #a5b4fc; font-weight: 600;">${{item.display}}</div>
        <p class="card-desc">${{item.desc}}</p>
      </div>
    `;
    cardsGrid.appendChild(card);
  }});
}}

function updateStage(angle) {{
  currentAngle = Number(angle);
  slider.value = currentAngle;
  angleText.textContent = currentAngle;
  
  const item = angleData.find(d => d.angle === currentAngle) || angleData[18];
  
  viewportImg.src = item.data_uri;
  viewportBadge.textContent = `${{item.angle}}°`;
  viewportDisplayBadge.textContent = item.display.includes('Cover') ? 'Cover Display' : 'Inner Main';
  
  teleDisplay.textContent = item.display;
  teleRes.textContent = item.resolution;
  teleRadians.textContent = item.radians;
  teleMotion.textContent = item.motion;
  teleDesc.textContent = item.desc;
  
  // Highlight active card
  document.querySelectorAll('.angle-card').forEach(c => c.classList.remove('current'));
  const activeCard = document.getElementById(`card-${{currentAngle}}`);
  if (activeCard) activeCard.classList.add('current');
}}

function setAngle(angle) {{
  stopAnimation();
  updateStage(angle);
}}

slider.addEventListener('input', (e) => {{
  stopAnimation();
  updateStage(e.target.value);
}});

function toggleAnimation() {{
  if (isPlaying) {{
    stopAnimation();
  }} else {{
    startAnimation();
  }}
}}

function startAnimation() {{
  isPlaying = true;
  playBtn.textContent = '⏸ Pause';
  playBtn.classList.add('active');
  playInterval = setInterval(() => {{
    let nextAngle = currentAngle + playDirection;
    if (nextAngle < 0) {{
      nextAngle = 10;
      playDirection = 10;
    }} else if (nextAngle > 180) {{
      nextAngle = 170;
      playDirection = -10;
    }}
    updateStage(nextAngle);
  }}, 400);
}}

function stopAnimation() {{
  isPlaying = false;
  playBtn.textContent = '▶ Play Animation';
  playBtn.classList.remove('active');
  if (playInterval) {{
    clearInterval(playInterval);
    playInterval = null;
  }}
}}

playBtn.addEventListener('click', toggleAnimation);

function filterGallery(phase, btn) {{
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  renderGrid(phase);
}}

// Lightbox modal
const modal = document.getElementById('modalOverlay');
const modalImg = document.getElementById('modalImg');
const modalInfo = document.getElementById('modalInfo');

function openModal(item) {{
  modalImg.src = item.data_uri;
  modalInfo.innerHTML = `<strong>${{item.angle}}°</strong> — ${{item.display}} (${{item.resolution}})<br>${{item.desc}}`;
  modal.classList.add('open');
}}

function closeModal(e) {{
  if (e.target === modal) {{
    modal.classList.remove('open');
  }}
}}

function closeModalDirect() {{
  modal.classList.remove('open');
}}

document.addEventListener('keydown', (e) => {{
  if (e.key === 'Escape') closeModalDirect();
  if (e.key === 'ArrowRight') {{
    if (currentAngle < 180) setAngle(currentAngle + 10);
  }}
  if (e.key === 'ArrowLeft') {{
    if (currentAngle > 0) setAngle(currentAngle - 10);
  }}
  if (e.key === ' ') {{
    e.preventDefault();
    toggleAnimation();
  }}
}});

// Initialize
renderGrid('all');
updateStage(180);
</script>
</body>
</html>
"""

print(f"Writing standalone HTML file to: {output_html_path}")
with open(output_html_path, "w", encoding="utf-8") as f:
    f.write(html_template)

file_size_mb = os.path.getsize(output_html_path) / (1024 * 1024)
print(f"Successfully created standalone HTML file! Size: {file_size_mb:.2f} MB")
