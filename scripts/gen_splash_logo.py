"""Generate splash-screen logo PNGs for all densities using Pillow.

Design: green rounded-square tile (matching the launcher icon) with a white
circle "plate", green leaf, and a thin white ring so it reads as a logo on
the light splash background. Also writes a centered wordmark below the logo
by keeping the mark square; the wordmark text is part of the layout.

If Pillow is not installed, run:  pip install pillow
"""
import os

from PIL import Image, ImageDraw

RES = "app/src/main/res"

# splash logo size (dp * density/48) — same scale as launcher icons
SIZES = {
    "mdpi": 96,
    "hdpi": 144,
    "xhdpi": 192,
    "xxhdpi": 288,
    "xxxhdpi": 384,
}


def make_splash_logo(size: int) -> Image.Image:
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    green = (46, 125, 50, 255)      # #2E7D32
    dark = (27, 94, 32, 255)        # #1B5E20
    white = (255, 255, 255, 255)

    # Rounded-square tile, like the launcher icon
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=int(size * 0.22),
                        fill=green)

    # Thin white ring inside the tile for a "badge" look
    ring_r = int(size * 0.44)
    cx = cy = size // 2
    d.ellipse([cx - ring_r, cy - ring_r, cx + ring_r, cy + ring_r],
              outline=white, width=max(2, size // 24))

    # White circle "plate"
    r = int(size * 0.32)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=white)

    # Green leaf inside the plate
    leaf_w, leaf_h = int(size * 0.32), int(size * 0.44)
    lx, ly = cx - leaf_w // 2, cy - leaf_h // 2 - int(size * 0.02)
    d.ellipse([lx, ly, lx + leaf_w, ly + leaf_h], fill=green)
    # leaf stem
    d.line([cx, cy - int(size * 0.05), cx, cy + int(size * 0.14)],
           fill=dark, width=max(2, size // 28))

    return img


def main():
    for density, size in SIZES.items():
        folder = os.path.join(RES, f"mipmap-{density}")
        os.makedirs(folder, exist_ok=True)
        out = os.path.join(folder, "splash_logo.png")
        make_splash_logo(size).save(out, "PNG")
        print(f"wrote {out} ({size}x{size})")


if __name__ == "__main__":
    main()
