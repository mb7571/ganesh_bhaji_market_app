"""Generate launcher icon PNGs for all densities from the brand icon.

Source artwork: gbm-icon.png at the repo root (512x512, full-bleed).
Each density PNG is resized with Lanczos and given rounded corners so it
keeps the same rounded-tile launcher style as before.

If Pillow is not installed, run:  pip install pillow
"""
import os

try:
    from PIL import Image, ImageDraw
    HAVE_PIL = True
except ImportError:
    HAVE_PIL = False

ROOT = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
SRC_ICON = os.path.join(ROOT, "gbm-icon.png")
RES = os.path.join(ROOT, "app", "src", "main", "res")

# density -> launcher icon size (dp * density/48)
SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

# Corner radius as a fraction of icon size (matches the previous style)
CORNER_RADIUS = 0.22


def make_icon(size: int) -> "Image.Image":
    src = Image.open(SRC_ICON).convert("RGBA")
    img = src.resize((size, size), Image.LANCZOS)

    # Rounded-corner mask for transparent corners
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    d.rounded_rectangle([0, 0, size - 1, size - 1],
                        radius=int(size * CORNER_RADIUS), fill=255)
    img.putalpha(mask)
    return img


def main():
    if not HAVE_PIL:
        raise SystemExit("Pillow is required:  pip install pillow")
    if not os.path.exists(SRC_ICON):
        raise SystemExit(f"Source icon not found: {SRC_ICON}")

    src = Image.open(SRC_ICON)
    print(f"source: {SRC_ICON} ({src.size[0]}x{src.size[1]})")

    for density, size in SIZES.items():
        folder = os.path.join(RES, f"mipmap-{density}")
        os.makedirs(folder, exist_ok=True)
        out = os.path.join(folder, "ic_launcher.png")
        make_icon(size).save(out, "PNG")
        print(f"wrote {out} ({size}x{size})")


if __name__ == "__main__":
    main()
