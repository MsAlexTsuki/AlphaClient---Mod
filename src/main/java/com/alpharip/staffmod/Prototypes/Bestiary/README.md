# Bestiary Prototype

A Minecraft 1.8.9 mob-kill logbook prototype.

## Implemented in this stage
- A mob species is unlocked only after a player receives kill credit for it.
- Each species is stored once by its entity registry ID.
- Further credited kills increment that species' kill count.
- Entries retain the first world, first kill time, and most recent kill time.
- A Forge LivingDeathEvent listener filters out player deaths and deaths not caused by the local player.
- Event registration is explicit through the standalone Bestiary entry point; it is not wired into AlphaClient Core.

## Not included / not verified yet
- No Bestiary GUI or visual mob catalogue.
- No disk persistence; the current book is in memory only.
- No per-world versus cross-world save policy has been selected yet.
- Projectile and indirect damage attribution has not been verified in a running 1.8.9 client.
- No AlphaClient Core/module-registry integration.
- Forge compilation and in-game behavior have not been tested.
