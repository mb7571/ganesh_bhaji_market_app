"""Generate launcher icon PNGs for all densities using Pillow.

If Pillow is not installed, run:  pip install pillow
"""
import os
import struct
import zlib

try:
    from PIL import Image, ImageDraw
    HAVE_PIL = True
except ImportError:
    HAVE_PIL = False

RES = "app/src/main/res"

# density -> launcher icon size (dp * density/48)
SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def make_icon_pil(size: int) -> "Image.Image":
    """Green rounded-square with a leaf, like a fresh-vegetable market logo."""
    from PIL import ImageDraw
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Rounded square background
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=int(size * 0.22),
                        fill=(46, 125, 50, 255))  # #2E7D32

    # White circle "plate" in the middle
    cx, cy = size // 2, int(size * 0.56)
    r = int(size * 0.30)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(255, 255, 255, 255))

    # Green leaf inside the circle
    leaf_w, leaf_h = int(size * 0.30), int(size * 0.42)
    lx, ly = cx - leaf_w // 2, cy - leaf_h // 2
    d.ellipse([lx, ly, lx + leaf_w, ly + leaf_h], fill=(46, 125, 50, 255))
    # leaf stem
    d.line([cx, cy + leaf_h // 2 - 2, cx, cy + leaf_h // 2 + int(size * 0.08)],
           fill=(27, 94, 32, 255), width=max(2, size // 32))

    return img


def crc32(data: bytes) -> int:
    return zlib.crc32(data) & 0xFFFFFFFF


def chunk(tag: bytes, data: bytes) -> bytes:
    return (struct.pack(">I", len(data)) + tag + data
            + struct.pack(">I", crc32(tag + data)))


def write_png_fallback(size: int, path: str):
    """Minimal solid green rounded-ish PNG without Pillow (crude fallback)."""
    # Very simple: solid green square (no rounded corners) as fallback
    raw = b""
    row = b"\x00" + bytes([46, 125, 50, 255] * size)
    raw = row * size
    png = (b"\x89PNG\r\n\x1a\n"
           + chunk(b"IHDR", struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0))
           + chunk(b"IDAT", zlib.compress(raw))
           + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def main():
    for density, size in SIZES.items():
        folder = os.path.join(RES, f"mipmap-{density}")
        os.makedirs(folder, exist_ok=True)
        out = os.path.join(folder, "ic_launcher.png")
        if HAVE_PIL:
            img = make_icon_pil(size)
            img.save(out, "PNG")
            print(f"wrote {out} ({size}x{size})")
        else:
            write_png_fallback(size, out)
            print(f"wrote {out} (fallback solid, {size}x{size})")


if __name__ == "__main__":
    main()
