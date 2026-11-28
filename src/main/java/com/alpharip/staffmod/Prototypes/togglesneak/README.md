# ToggleSneak & ToggleSprint Prototype

Standalone prototype for Minecraft 1.8.9, Forge 11.15.1.2318, and Java 8.

## Java source layout

The implementation is split by responsibility instead of putting the entire feature into one or two classes.

| File | Responsibility |
|---|---|
| `ToggleMovementModule.java` | Shared contract for independently enabled movement-toggle modules |
| `AbstractToggleMovementModule.java` | Shared enabled/toggled state transitions |
| `ToggleMovementType.java` | Identifies Sneak or Sprint and resolves the matching vanilla key binding |
| `ToggleSneak.java` | ToggleSneak module state |
| `ToggleSprint.java` | ToggleSprint module state |
| `ToggleMovementInput.java` | Reads physical keyboard and mouse input |
| `ToggleMovementKeyBindings.java` | Registers and consumes the two configurable activation keybindings |
| `ToggleMovementKeyState.java` | Applies/release vanilla key state and handles a remapped vanilla key |
| `ToggleMovementController.java` | Coordinates module state, activation presses, focus, world state, and vanilla bindings |
| `ToggleMovementKeyHandler.java` | Thin Forge client-tick event adapter |
| `ToggleMovementRuntime.java` | Owns the modules and starts/stops the event handler |

## Current behavior

- ToggleSneak and ToggleSprint are independently enabled by default.
- Both start with their movement toggle state off.
- Activation keybindings are separate and initially unbound. Assign them in Minecraft Options → Controls.
- Activation presses toggle the matching vanilla sneak or sprint key state.
- Physical keyboard and mouse input continues to work when a toggle is off.
- A changed vanilla key binding releases the previously controlled key code.
- Movement key states are released while a GUI is open or Minecraft has lost focus. The toggle state is retained and reapplied when gameplay resumes.
- Toggle states reset when the player or world is unavailable and when the runtime is stopped.
- The runtime guards against duplicate starts and unregisters its event handler on stop.

## Integration boundary

This is a standalone prototype. It is not automatically started and is not registered in AlphaClient Core, the module registry, or a GUI. A separately approved integration must create `ToggleMovementRuntime`, call `start()` on the client lifecycle, and call `stop()` during shutdown.

The activation keybindings are registered with Forge when the runtime starts. Minecraft's Controls screen is responsible for assigning their keys. No custom settings GUI or separate settings persistence layer is included.

No AlphaClient Core, `development`, StaffMods menu, Supabase service, or Alpha Launcher integration is included.

## Verification status

- Source structure: 11 Java files are present in this package.
- Forge compilation: not run.
- In-game behavior: not tested.
- Input timing and interaction with other mods: not verified in Minecraft 1.8.9.
