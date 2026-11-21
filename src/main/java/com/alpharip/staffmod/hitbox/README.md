# Hitbox Prototype

Package: `com.alpharip.staffmod.hitbox`

Isolated Minecraft 1.8.9 / Forge 11.15.1.2318 prototype, located beside `staffops`.

## Included

- `HitboxModule`: lifecycle wrapper with explicit Forge event-bus registration.
- `HitboxSettings`: enabled state, render range, line width, RGB color, and alpha.
- `HitboxRenderer`: renders the bounding boxes of other visible player entities.
- `HitboxGeometry`: interpolates entity positions and converts bounding boxes to render-view-relative coordinates.

## Fixed visibility rules

- Hitboxes never render through walls. OpenGL depth testing is always enabled.
- Invisible players are always excluded. There is no setting to enable their hitboxes.
- Only other alive player entities are drawn.
- Default range is 32 blocks.
- Default color is Alpha gold.
- The renderer visualizes existing bounding boxes; it does not modify hit detection or collision geometry.

## Integration boundary

The prototype is not registered in `AlphaClient.initialize()`, is not added to the module registry, and has no menu/config persistence integration. Those changes are outside this prototype folder and require a separate integration decision.

To activate it from an approved integration point, create and retain a `HitboxModule` instance, call `register()`, and then call `setEnabled(true)`. Call `unregister()` when the integration is shut down.

## Verification

The source was updated on the `mod-ideas` branch. No Gradle build or in-game test has been run.
