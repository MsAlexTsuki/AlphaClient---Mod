# Armor Break Warning Prototype

Standalone Minecraft 1.8.9 / Forge client-side prototype, located beside `staffops/` on branch `mod-ideas`.

## Included

- **ArmorBreakWarning.java** — Singleton facade with explicit Forge event registration, local settings access, storage, and GUI opening.
- **ArmorBreakWarningSettings.java** — Holds enablement and the remaining-durability warning threshold. The default is 10%; the standalone settings screen cycles through 5%, 10%, 15%, 20%, and 25%.
- **ArmorBreakWarningRenderer.java** — Checks the local player's equipped armor inventory and counts damageable armor pieces at or below the configured remaining-durability threshold. Displays the original warning text and red HUD placement.
- **ArmorBreakWarningEventHandler.java** — Adapts the Forge overlay text event to the renderer.
- **ArmorBreakWarningStorage.java** — Stores enablement and threshold locally at `config/alphaclient/armor-break-warning.properties`.
- **ArmorBreakWarningGui.java** — Standalone settings preview for enablement and warning threshold.

## Prototype boundaries

- The source idea's 10% default threshold and low-durability piece count are retained.
- The configurable threshold and standalone settings screen are prototype additions; the source idea did not specify them.
- The warning observes only the local player's equipped armor. It does not predict opponent armor breakage or track armor durability over time.
- Registration is explicit. No existing menu button, keybind, AlphaModuleRegistry integration, AlphaClient Core registration, rank permission, or Launcher integration is added.
- No Supabase, remote service, account lookup, or remote persistence is included.
- Minecraft/Forge compilation and in-game behavior have not been verified.
