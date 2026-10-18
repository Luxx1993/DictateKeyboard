#!/usr/bin/env python3
"""
Generator for the diktat app icon "Welle um die Welt" (wave around the globe).

Everything is flat 2D line art: a ring (the globe outline) and one wave that runs once
around the sphere. The 3D impression comes only from the stroke width: the wave is thick
where it runs in front (2.8) and thins smoothly to 1.3 where it runs behind the globe.

Design box: 48 x 48 units, centre (24, 24). Android adaptive icon: 108 dp viewport, the
48-unit box maps onto the visible 72 dp (scale 1.5, offset 18), so the globe (radius 20.6)
is ~62 dp wide and sits inside the 66 dp safe zone.

Run:  python3 diktat_icon.py            -> writes ./export/svg/* and ./export/android/*
No third-party packages needed.
"""
import math, os

# ---- geometry (final values, decided in the design canvas, round 11/12) -------------------
C = 24.0          # centre of the 48-unit design box
R = 20.6          # globe radius (ring and wave share it)
A = 19.0          # wave amplitude in degrees of latitude
K = 3             # wave repeats 3x around the globe -> 1.5 visible swings in front
TILT = 22.0       # globe tilted towards the viewer, degrees
PHASE = 180.0     # phase so the visible front wave reads like an "S" lying on its side
W_FRONT = 2.8     # stroke width in front (also the ring)
W_BACK = 1.3      # stroke width behind the globe
LEVELS = 9        # stroke width is quantised into this many bands for vector formats
N = 240           # samples along the wave

def proj(lon, lat, tilt=TILT, r=R):
    """Orthographic projection of a point on the sphere; returns x, y, depth (z>0 = front)."""
    lo, la, t = map(math.radians, (lon, lat, tilt))
    x = math.cos(la) * math.sin(lo); y = math.sin(la); z = math.cos(la) * math.cos(lo)
    y2 = y * math.cos(t) - z * math.sin(t); z2 = y * math.sin(t) + z * math.cos(t)
    return C + r * x, C - r * y2, z2

def lat_static(lon):
    return A * math.sin(math.radians(K * lon + PHASE))

def lat_anim(lon, tt, mode):
    """Latitude of the wave at loop position tt in [0,1).
    mode 'A': rotate only (splash).  mode 'D': rotate + amplitude, front and back in counter-phase."""
    off = 120.0 * tt                      # one loop = 120 deg (the wave repeats every 120 deg)
    if mode == 'A':
        return A * math.sin(math.radians(K * (lon + off) + PHASE))
    c = 0.5 - 0.5 * math.cos(2 * math.pi * 2 * tt)          # two breaths per loop, 0..1
    w = (math.cos(math.radians(lon)) + 1) / 2; w = w * w * (3 - 2 * w)   # 1 = front, 0 = back
    cc = w * c + (1 - w) * (1 - c)                           # back runs in counter-phase
    return A * (1.0 - 0.35 * cc) * math.sin(math.radians(K * (lon + off) + PHASE))

def width_for_depth(z):
    t = max(0.0, min(1.0, (z + 1) / 2)); t = t * t * (3 - 2 * t)
    return W_BACK + (W_FRONT - W_BACK) * t

def bands(latf):
    """Split the wave into LEVELS polylines, one per stroke width. Returns list of path strings."""
    pts = [proj(-180 + 360 * i / N, latf(-180 + 360 * i / N)) for i in range(N + 1)]
    out = [[] for _ in range(LEVELS)]; cur = None; run = []
    def flush():
        if cur is not None and len(run) > 1:
            out[cur].append("M" + " L".join(f"{x:.2f} {y:.2f}" for x, y in run))
    for i in range(N):
        x1, y1, z1 = pts[i]; x2, y2, z2 = pts[i + 1]
        t = max(0.0, min(1.0, ((z1 + z2) / 2 + 1) / 2)); t = t * t * (3 - 2 * t)
        lv = min(LEVELS - 1, int(round(t * (LEVELS - 1))))
        if lv != cur:
            flush(); cur = lv; run = [(x1, y1)]
        run.append((x2, y2))
    flush()
    return ["".join(b) for b in out]

def band_widths():
    return [W_BACK + (W_FRONT - W_BACK) * i / (LEVELS - 1) for i in range(LEVELS)]

RING = f"M{C+R:.2f} {C:.2f} A{R} {R} 0 1 1 {C-R:.2f} {C:.2f} A{R} {R} 0 1 1 {C+R:.2f} {C:.2f} Z"

# ---- SVG ----------------------------------------------------------------------------------
def svg_static(color):
    W = band_widths(); B = bands(lat_static)
    paths = "".join(f'<path d="{d}" stroke-width="{W[i]:.2f}"/>' for i, d in enumerate(B) if d)
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" width="480" height="480">'
            f'<g fill="none" stroke="{color}" stroke-linecap="round" stroke-linejoin="round">{paths}'
            f'<path d="{RING}" stroke-width="{W_FRONT}"/></g></svg>')

def svg_anim(mode, color, frames=120, dur=6):
    W = band_widths()
    F = [bands(lambda lon, f=f: lat_anim(lon, f / frames, mode)) for f in range(frames)]
    g = ""
    for lv in range(LEVELS):
        vals = ";".join((F[f][lv] or "M0 0") for f in range(frames))
        g += (f'<path d="{F[0][lv] or "M0 0"}" stroke-width="{W[lv]:.2f}">'
              f'<animate attributeName="d" dur="{dur}s" repeatCount="indefinite" calcMode="discrete" values="{vals}"/></path>')
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" width="480" height="480">'
            f'<g fill="none" stroke="{color}" stroke-linecap="round" stroke-linejoin="round">{g}'
            f'<path d="{RING}" stroke-width="{W_FRONT}"/></g></svg>')

# ---- Android VectorDrawable (static icon) ------------------------------------------------
def android_vector(color):
    W = band_widths(); B = bands(lat_static)
    paths = "\n".join(
        f'        <path android:pathData="{d}" android:strokeColor="{color}" android:strokeWidth="{W[i]:.2f}"\n'
        f'            android:strokeLineCap="round" android:strokeLineJoin="round" android:fillColor="#00000000"/>'
        for i, d in enumerate(B) if d)
    return f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Generated by docs/design/icon/diktat_icon.py - do not edit by hand. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <group android:translateX="18" android:translateY="18" android:scaleX="1.5" android:scaleY="1.5">
{paths}
        <path android:pathData="{RING}" android:strokeColor="{color}" android:strokeWidth="{W_FRONT}"
            android:fillColor="#00000000"/>
    </group>
</vector>
'''

if __name__ == "__main__":
    here = os.path.dirname(os.path.abspath(__file__))
    svg_dir = os.path.join(here, "export", "svg"); and_dir = os.path.join(here, "export", "android")
    os.makedirs(svg_dir, exist_ok=True); os.makedirs(and_dir, exist_ok=True)
    for name, col in (("white", "#FFFFFF"), ("black", "#1F1C1A")):
        open(os.path.join(svg_dir, f"diktat-icon-{name}.svg"), "w").write(svg_static(col))
    open(os.path.join(svg_dir, "diktat-anim-A-drehen.svg"), "w").write(svg_anim("A", "#1F1C1A"))
    open(os.path.join(svg_dir, "diktat-anim-D-drehen-amplitude-gegenlaeufig.svg"), "w").write(svg_anim("D", "#1F1C1A"))
    open(os.path.join(and_dir, "ic_diktat_globe_white.xml"), "w").write(android_vector("#FFFFFFFF"))
    open(os.path.join(and_dir, "ic_diktat_globe_black.xml"), "w").write(android_vector("#FF1F1C1A"))
    open(os.path.join(and_dir, "ic_diktat_globe_monochrome.xml"), "w").write(android_vector("#FFFFFFFF"))
    print("written to", os.path.join(here, "export"))
