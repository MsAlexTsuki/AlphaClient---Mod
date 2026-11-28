# Armor Durability Warning prototype

This prototype is separate from `ArmorDurabilityTracker`. The tracker estimates
opponent armor break timing from observed durability loss; this warning scans
the local player's equipped armor inventory.

## Included

- Scans the four equipped armor slots in Minecraft 1.8.9.
- Uses the remaining durability percentage, not incoming hit damage.
- Warns at the configured threshold (default: 10% or less).
- Counts the pieces below the threshold and renders the existing compact HUD text.
- Separates scanning, result data, configuration, and HUD rendering into distinct classes.
- Uses local Java state only; no Supabase or server integration.

## Files

- `ArmorDurabilityWarningMod.java` — Forge HUD event adapter.
- `ArmorDurabilityWarningConfig.java` — threshold and HUD placement/color defaults.
- `ArmorDurabilityWarningScanner.java` — equipped-armor scan.
- `ArmorDurabilityWarningPiece.java` — per-piece warning snapshot.
- `ArmorDurabilityWarningResult.java` — immutable scan result.
- `ArmorDurabilityWarningRenderer.java` — HUD drawing.

## Integration status

The current `AlphaClient` source already imports and registers
`com.alpharip.staffmod.hud.ArmorDurabilityWarningMod`; that package is retained
for this adapter. The current `AlphaModuleRegistry` also contains the
`armor-durability-warning` module ID, so the existing module toggle and access
checks are retained. This prototype does not modify `AlphaClient`, the module
registry, or any other core file.

No build or in-game test has been performed.
