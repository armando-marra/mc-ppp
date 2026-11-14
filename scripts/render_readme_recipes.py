"""Render README recipes from generated JSON and local Minecraft textures.

Requires Pillow. Usage:
python scripts/render_readme_recipes.py --minecraft-jar PATH_TO_CLIENT_JAR
The Minecraft JAR is only read locally and is not copied into the repository.
"""

import argparse
from io import BytesIO
import json
from pathlib import Path
import zipfile

from PIL import Image, ImageDraw, ImageEnhance, ImageFont

ROOT = Path(__file__).resolve().parents[1]
GENERATED = ROOT / "src/main/generated"
NEAREST = Image.Resampling.NEAREST


def texture(archive, resource):
    namespace, name = resource.split(":")
    if namespace == "pointportals":
        with Image.open(ROOT / f"src/main/resources/assets/{namespace}/textures/{name}.png") as source:
            image = source.convert("RGBA")
    else:
        with archive.open(f"assets/{namespace}/textures/{name}.png") as stream:
            image = Image.open(BytesIO(stream.read())).convert("RGBA")
    return image.crop((0, 0, image.width, image.width))


def face(canvas, image, origin, u, v, brightness):
    """Project one texture onto a parallelogram, preserving pixel edges."""
    determinant = u[0] * v[1] - u[1] * v[0]
    a, b = image.width * v[1] / determinant, -image.width * v[0] / determinant
    d, e = -image.height * u[1] / determinant, image.height * u[0] / determinant
    projection = ImageEnhance.Brightness(image).enhance(brightness).transform(
        canvas.size, Image.Transform.AFFINE,
        (a, b, -a * origin[0] - b * origin[1], d, e, -d * origin[0] - e * origin[1]),
        resample=NEAREST,
    )
    canvas.alpha_composite(projection)


def cube(archive, top, side):
    canvas = Image.new("RGBA", (64, 64))
    side_image = texture(archive, side)
    face(canvas, side_image, (0, 16), (32, 16), (0, 32), 0.8)
    face(canvas, side_image, (32, 32), (32, -16), (0, 32), 0.6)
    face(canvas, texture(archive, top), (32, 0), (32, 16), (-32, 16), 1.0)
    return canvas


def icon(archive, item):
    if item == "minecraft:obsidian":
        return cube(archive, "minecraft:block/obsidian", "minecraft:block/obsidian")
    if item.startswith("pointportals:"):
        name = item.split(":")[1]
        model = json.loads((GENERATED / f"assets/pointportals/models/item/{name}.json").read_text())
        if "textures" in model:
            return texture(archive, model["textures"]["layer0"]).resize((64, 64), NEAREST)
        parent = model["parent"].split(":")[1]
        block = json.loads((GENERATED / f"assets/pointportals/models/{parent}.json").read_text())
        return cube(archive, block["textures"]["top"], block["textures"]["side"])
    return texture(archive, item.replace(":", ":item/")).resize((64, 64), NEAREST)


def slot(canvas, position, item, archive, size=80):
    x, y = position
    draw = ImageDraw.Draw(canvas)
    draw.rectangle((x, y, x + size - 1, y + size - 1), fill="#373737")
    draw.rectangle((x + 3, y + 3, x + size - 1, y + size - 1), fill="#ffffff")
    draw.rectangle((x + 3, y + 3, x + size - 4, y + size - 4), fill="#8b8b8b")
    if item:
        canvas.alpha_composite(icon(archive, item), (x + (size - 64) // 2, y + (size - 64) // 2))


def render(archive, recipe_name, title):
    recipe = json.loads((GENERATED / f"data/pointportals/recipe/{recipe_name}.json").read_text())
    canvas = Image.new("RGBA", (560, 330), "#c6c6c6")
    draw = ImageDraw.Draw(canvas)
    font = ImageFont.load_default(size=22)
    draw.text((24, 16), title, font=font, fill="#303030")
    for row, pattern in enumerate(recipe["pattern"]):
        for col, key in enumerate(pattern):
            slot(canvas, (24 + col * 80, 62 + row * 80), recipe["key"].get(key), archive)
    draw.polygon([(300, 163), (344, 163), (344, 149), (374, 182),
                  (344, 215), (344, 201), (300, 201)], fill="#777777")
    slot(canvas, (414, 142), recipe["result"]["id"], archive)
    count = recipe["result"].get("count", 1)
    if count > 1:
        draw.text((474, 194), str(count), font=font, fill="white", stroke_width=2, stroke_fill="#303030")
    output = ROOT / f"docs/images/crafting-{recipe_name}.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    canvas.convert("RGB").save(output)
    print(output)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--minecraft-jar", type=Path, required=True)
    args = parser.parse_args()
    with zipfile.ZipFile(args.minecraft_jar) as archive:
        render(archive, "teleporter", "Teleporter · 2 blocks")
        render(archive, "tuner", "Phase Tuner")


if __name__ == "__main__":
    main()
