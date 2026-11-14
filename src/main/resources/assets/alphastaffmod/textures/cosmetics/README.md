# Cosmetic texture assets

Place optional cosmetic PNG files in this resource tree:

- Capes: `assets/alphastaffmod/textures/cosmetics/capes/<cosmetic-id>.png` — Minecraft 1.8.9 cape texture format, 64×32.
- Other image-backed categories: `assets/alphastaffmod/textures/cosmetics/<category>/<cosmetic-id>.png` — 64×64 PNG.

The Store resolves texture names from the registered cosmetic ID and category. Do not add placeholder artwork: if a PNG is not present, the Store shows an asset-missing preview state. This directory intentionally contains no cape artwork yet.

The current Store preview displays the selected texture as a flat image. It does not yet attach capes or other cosmetics to the 3D player model.