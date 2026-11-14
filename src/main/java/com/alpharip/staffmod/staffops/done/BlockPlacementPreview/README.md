# Block Placement Preview

A local client-side placement helper for Minecraft 1.8.9. Hold a block and aim at a block face to preview its shape at the placement position.

## Current behavior

- Uses the block's collision shape when Minecraft provides one.
- For blocks without a collision shape, reads the vertices from Minecraft's existing baked block model and draws a matching translucent bounding volume. This covers many plants, torches, rails, and other non-solid blocks.
- Uses green for a placement that appears clear and red when the block placement check or a block/entity collision indicates a problem.
- Preserves the OpenGL state that was active before rendering.
- Is disabled by default and does not place blocks or send placement packets.
- Stores settings locally in `config/alphaclient/block-placement-preview.properties`.

Use `/blockpreview` to open settings or:

- `/blockpreview on|off|toggle`
- `/blockpreview collision on|off`
- `/blockpreview opacity <0-100>`

## Textures and shape limits

No custom PNG files are needed. The preview reads block geometry from Minecraft's registered block models; the current renderer draws the resulting outline and translucent volume, not the block's textured model.

The model bounds are drawn as one box, so models with gaps or disconnected pieces can include empty space inside the outline. If a block has neither a collision shape nor a usable baked model, the preview falls back to a full cube. Multi-block items such as beds and doors may only show the first block's volume. Placement validity remains an estimate because block-specific rules can differ.

## Ideas that fit this module

1. **Textured ghost block** — draw the actual model using Minecraft's existing texture atlas and tint it for placement status.
2. **Multi-block preview** — show every affected position for doors, beds, tall plants, and similar blocks.
3. **Placement reason label** — explain “blocked by entity”, “unsupported”, or “cannot place here”.
4. **Placement rotation controls** — preview alternate orientations for blocks whose direction depends on player facing or click position.
5. **Replacement feedback** — distinguish a replaceable target from an adjacent placement and show which existing block will be replaced.
6. **Range and visibility settings** — set maximum preview distance, outline thickness, and whether the volume is visible through blocks.
7. **Per-block display rules** — hide the preview for selected blocks or use a different color for particular block groups.
