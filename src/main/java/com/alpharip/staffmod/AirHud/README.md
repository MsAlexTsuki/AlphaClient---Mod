# Air HUD Prototype

Standalone Minecraft 1.8.9 / Forge client-side prototype.

## Included
- Independent module facade with explicit Forge event registration and unregistration.
- Underwater air display showing remaining air ticks and a tick-derived seconds estimate.
- Four screen-corner positions.
- Standalone settings GUI for enablement, label visibility, position, and text color.
- Local settings file: `config/alphaclient/air-hud.properties`.
- No dependency on AlphaModuleRegistry or AlphaClient Core.

## Prototype boundaries
- No existing menu button or keybind is added.
- The module is not registered by AlphaClient Core.
- No Supabase, remote storage, account integration, or Launcher integration.
- The seconds value is an estimate derived from the local air-tick value.
- Minecraft/Forge compilation and in-game behavior have not been verified.
