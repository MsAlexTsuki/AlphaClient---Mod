# Adventure Diary Prototype

An event-driven journal model that turns normalized gameplay events into dramatic diary prose.

## Included
- Typed events for entering worlds, kills, deaths, pickups, dimension changes, and boss defeats.
- A separate narrator responsible for the dramatic wording.
- A diary book that stores timestamped entries in insertion order.

## Not included / not verified
- Minecraft/Forge event listeners are not connected; callers must submit events to the book.
- No date grouping, chapter generation, GUI, or export feature.
- No disk persistence; entries currently live in memory.
- No AlphaClient Core integration.
- Forge compilation and in-game behavior have not been tested.
