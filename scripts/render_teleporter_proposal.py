"""Render the approved Teleporter artwork, without changing mod assets.

Requires Pillow; run from any directory with python scripts/render_teleporter_proposal.py.
"""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from render_readme_recipes import face

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "docs/artwork/teleporter-alternative"


def font(size):
    path = Path("C:/Windows/Fonts/segoeui.ttf")
    return ImageFont.truetype(str(path), size) if path.exists() else ImageFont.load_default(size=size)


def block(state):
    canvas = Image.new("RGBA", (288, 288))
    with Image.open(ART / f"textures/teleporter_{state}_side.png") as source:
        side = source.convert("RGBA")
    with Image.open(ART / f"textures/teleporter_{state}_top.png") as source:
        top = source.convert("RGBA")
    face(canvas, side, (0, 72), (144, 72), (0, 144), 0.80)
    face(canvas, side, (144, 144), (144, -72), (0, 144), 0.60)
    face(canvas, top, (144, 0), (144, 72), (-144, 72), 1.0)
    return canvas


def main():
    canvas = Image.new("RGBA", (1200, 590), "#141923")
    draw = ImageDraw.Draw(canvas)
    draw.text((40, 26), "TELEPORTER / Rame e ossidiana", font=font(32), fill="#f1e7de")
    draw.text((40, 74), "Texture approvate · Texture originali 32×32 · Anteprima dei modelli", font=font(18), fill="#a8b0c2")
    states = [
        ("inactive", "SCOLLEGATO", "Nucleo scuro, nessun segnale", "#bb8e75"),
        ("standby", "COLLEGATO · IN ATTESA", "Rune tenui, senza alimentazione", "#6baca8"),
        ("active", "ATTIVO", "Nucleo acceso, alimentato da redstone", "#ba87ed"),
    ]
    for index, (state, title, detail, color) in enumerate(states):
        x = 26 + index * 392
        draw.rounded_rectangle((x, 125, x + 364, 550), radius=12, fill="#1d2431")
        canvas.alpha_composite(block(state), (x + 38, 150))
        draw.line((x + 24, 463, x + 340, 463), fill=color, width=2)
        draw.text((x + 24, 480), title, font=font(19), fill=color)
        draw.text((x + 24, 514), detail, font=font(16), fill="#bec5d3")
    output = ART / "preview.png"
    canvas.convert("RGB").save(output)
    print(output)


if __name__ == "__main__":
    main()
