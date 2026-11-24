# ToggleSneak & ToggleSprint Prototype

Standalone Minecraft 1.8.9 / Forge 11.15.1.2318 / Java 8 prototype.

## Included

- Separate local state models for ToggleSneak and ToggleSprint.
- Separate configurable activation keybindings, initially unbound so they do not replace existing controls.
- A shared client-tick handler that toggles the existing vanilla sneak/sprint key states.
- Physical keyboard and mouse input is retained when a toggle is off.
- Movement keys are released while a GUI is open or the game is unfocused.
- Toggle states reset when no player/world is available and when the runtime is stopped.
- English keybinding labels under the alphaclient resource domain.
- Explicit runtime start/stop lifecycle; no AlphaClient Core entry point is changed.

## Current defaults

- ToggleSneak module enabled: yes (matching the original idea scaffold).
- ToggleSprint module enabled: no (matching the original idea scaffold).
- Both activation keybindings: unbound. Assign them in Minecraft Options → Controls before use.
- Both toggled movement states: off at startup.

## Integration boundary

The prototype is not automatically started. A future, separately approved integration must create ToggleMovementRuntime, call start() on the client, and call stop() during client shutdown. No AlphaClient Core, StaffMods menu, module registry, Supabase service, or Alpha Launcher integration is included.

## Verification status

- Source files created: prototype implementation.
- Forge compilation: not run.
- In-game behavior: not tested.
- Keybinding behavior and interaction with other mods remain unverified until compiled and tested in Minecraft 1.8.9.
