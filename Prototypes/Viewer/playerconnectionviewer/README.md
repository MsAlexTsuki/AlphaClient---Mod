# Player Connection Viewer

Standalone prototype for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Included
- Local connection state (connected or disconnected).
- Local player name.
- Singleplayer or multiplayer connection type.
- Current server address when exposed by the Minecraft client.
- Session duration and last-sampled time.
- A standalone overview GUI with live refresh while it is open.
- A client-tick observer that keeps the session timer current while registered.

## Integration hook
The prototype deliberately does not modify AlphaClient core or register itself automatically. The future client initialization layer can call:

```java
PlayerConnectionViewer viewer = PlayerConnectionViewer.getInstance();
viewer.register();
```

Open the screen from an existing client UI action:

```java
viewer.openGui(currentScreen);
```

Call `viewer.unregister()` if the owning feature is explicitly shut down.

## Scope and limitations
- This is local-client information only. It cannot retrieve server-side connection diagnostics, hidden player data, or another player's private network details.
- The server address is read from the current local server entry; no connection history is persisted.
- No Supabase calls, schema changes, launcher changes, or AlphaClient core wiring are included.
- The GUI is standalone; no menu entry or keybind is added by this prototype.
