# Oval portal concept

`preview.png` is AI-generated concept art approved as a visual direction. It is
not an in-game screenshot. It depicts a vertical cyan/violet energy membrane
above the copper-and-obsidian Teleporter, without rendering the destination.

The implementation released in 0.1.2 clips the live vanilla Nether portal sprite to an oval,
using its native animation, colors and alpha. It keeps a violet/cyan outline and
real vanilla portal particles around its volume. The earlier procedural spiral
and renderer motes were replaced following feedback. It does not
implement the concept image's bloom or dynamic lighting. Real-client frames are
produced by `./gradlew runPortalClientTest` in `build/portal-client-test/screenshots/`.

`implemented-active.png` and `implemented-standby.png` are actual client captures
from that regression, showing the active oval and its disappearance on loss of
power. They are copied without image editing from the generated screenshots.
