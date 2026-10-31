# Block Placement Preview

A local client-side placement helper for Minecraft 1.8.9. Hold a block and aim at a block face to see its prospective collision volume at the placement position.

## Current behavior

- Draws a translucent preview volume with an outline.
- Uses green for a placement that appears clear and red when the block placement check or a block/entity collision indicates a problem.
- Preserves the OpenGL state that was active before rendering, so the preview is less likely to affect other world rendering.
- Is disabled by default and does not place blocks or send placement packets.
- Stores its settings locally in `config/alphaclient/block-placement-preview.properties`.

Use `/blockpreview` to open settings or:

- `/blockpreview on|off|toggle`
- `/blockpreview collision on|off`
- `/blockpreview opacity <0-100>`

## Known limits

- Some blocks use a full-cube fallback when Minecraft reports no collision box. For plants, torches, and other non-solid blocks, this can make the preview larger than the actual placed shape.
- Multi-block items, such as beds and doors, may only show the first block's volume.
- The preview estimates placement validity; block-specific placement rules can still make the actual result differ.

## Ideas that fit this module

1. **Textured ghost block** — render the actual block model, tinting it green/red, instead of a generic volume.
2. **Accurate non-solid shapes** — use selection/render bounds when a block has no collision box, with a small fallback only when neither shape exists.
3. **Multi-block preview** — show every affected position for doors, beds, tall plants, and similar blocks.
4. **Placement reason label** — explain “blocked by entity”, “unsupported”, or “cannot place here” near the preview.
5. **Placement rotation controls** — preview alternate orientations for blocks whose direction depends on player facing or click position.
6. **Replacement feedback** — distinguish a replaceable target from a new adjacent placement and show which existing block will be replaced.
7. **Range and visibility settings** — set maximum preview distance, outline thickness, and whether the volume is visible through blocks.
8. **Per-block display rules** — hide the preview for selected blocks or use a different color for particular block groups.
