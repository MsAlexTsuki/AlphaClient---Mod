# Lifetime Stats Book Prototype

An all-time statistics model for a player's Minecraft activity.

## Included
- Typed counters for mob/player kills, deaths, mining, placing, crafting, movement, and play time.
- A thread-safe counter store with non-negative increments and overflow protection.
- Immutable snapshots for a future book or statistics GUI.
- A tracker API intended to receive values from Minecraft/Forge event adapters.

## Not included / not verified
- No event adapters are connected; counters do not increase automatically yet.
- No save/load format, reset confirmation, statistics screen, or book item.
- Existing Minecraft statistics are not imported or reconciled.
- No AlphaClient Core integration.
- Forge compilation and in-game behavior have not been tested.
