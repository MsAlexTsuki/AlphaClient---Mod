# Screenshot Comparison prototype

Package: `com.alpharip.staffmod.screenshotcomparison`

## Included

- Local PNG/JPEG loading with readable-file, format and image-size validation.
- Before/after screenshot pairing.
- Side-by-side, alpha-overlay and pixel-difference preview modes.
- Changed-pixel percentage and mean RGB-difference metrics.
- A Minecraft 1.8.9 `GuiScreen` with path fields, load/compare controls and an in-game preview.

## Open the prototype

Call `ScreenshotComparison.open(parentScreen)` from a future integration point. This prototype is not registered in a menu, keybind, or module yet.

## Scope and limitations

- All image processing is local; no Supabase, network upload, or server integration is used.
- Settings are in-memory only and reset when the screen is reopened.
- File paths are entered manually; there is no native file picker.
- The difference view scales both images to a common canvas before comparing pixels. It is a visual aid, not a forensic image-authenticity tool.
- No existing screenshot gallery, capture, or evidence workflow is modified.
