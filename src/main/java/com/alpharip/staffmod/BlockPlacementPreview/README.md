# Block Placement Preview

A local client-side placement helper for Minecraft 1.8.9. Hold a block and aim at a block face to see the block's prospective collision shape at the placement position. The preview is a translucent volume rather than a textured ghost model.

When collision colors are enabled, green indicates that the block's placement check is clear and red indicates that the block cannot be placed there or the preview volume collides with something. The feature is disabled by default.

Use `/blockpreview` to open settings or:

- `/blockpreview on|off|toggle`
- `/blockpreview collision on|off`
- `/blockpreview opacity <0-100>`

Settings are stored locally in `config/alphaclient/block-placement-preview.properties`. The preview does not place blocks or send placement packets. Multi-block items may only show the first block's placement volume.
