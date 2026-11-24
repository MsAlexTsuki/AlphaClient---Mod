# Bestiary Prototype

A standalone mob-discovery log for Minecraft 1.8.9.

## Included
- A distinct entry for each mob registry ID.
- First encounter world and timestamp.
- Last encounter timestamp and total encounter count.
- A tracker API intended to receive nearby/visible mob observations.
- A book model that exposes a read-only snapshot of discovered entries.

## Not included / not verified
- No automatic entity scanning or encounter-radius rule is wired yet.
- No GUI/book screen, icon rendering, or sorting controls.
- No disk persistence; entries currently live in memory.
- No AlphaClient Core or module-registry integration.
- Forge compilation and in-game behavior have not been tested.
