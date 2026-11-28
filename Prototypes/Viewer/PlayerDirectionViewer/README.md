# Player Direction Viewer

Standalone prototype for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Included
- Samples other player entities currently present in the local client's world.
- Captures each player's name, head yaw, pitch, and distance from the local player.
- Updates samples at the end of each client tick after registration.
- Displays a standalone GUI with compass direction, yaw/pitch angles, and distance.
- Provides explicit register/unregister hooks; it does not wire itself into AlphaClient core.

## Integration hook

Register the viewer from the future client initialization or feature-toggle layer:

```java
PlayerDirectionViewer viewer = PlayerDirectionViewer.getInstance();
viewer.register();
```

Open the GUI from an existing UI action:

```java
viewer.openGui(currentScreen);
```

Call `viewer.unregister()` when the owning feature is disabled.

## Scope and limitations
- Only player entities currently loaded by the local Minecraft client are sampled.
- This does not reveal hidden players, server-side data, or private network information.
- No Supabase calls, schema changes, launcher changes, menu registration, keybind, or AlphaClient core wiring are included.
- The prototype has not been compiled or tested in-game as part of this change.
