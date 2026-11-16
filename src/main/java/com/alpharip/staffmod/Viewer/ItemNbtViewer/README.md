# Item NBT Viewer

Standalone prototype for Minecraft 1.8.9 / Forge 11.15.1.2318.

## Included
- Reads the item currently held by the local player.
- Serializes the ItemStack into an NBT compound for inspection.
- Displays the compound as an expandable tree with tag names, tag types, and values.
- Supports selecting a tree entry and copying that entry's serialized NBT value.
- Supports copying the complete serialized item NBT to the clipboard.
- Supports manual refresh and mouse-wheel scrolling.
- Limits tree expansion to 16 levels and 500 nodes.

## Integration hook

The prototype does not add a menu entry, keybind, module registration, or automatic event-bus registration. An existing client UI action can open it:

```java
ItemNbtViewer.getInstance().openGui(currentScreen);
```

The viewer reads the currently held stack when opened and refreshes the data while the screen is open.

## Scope and limitations
- Reads only the item stack held by the local client.
- Does not edit, replace, or send item NBT to the server.
- No Supabase calls, schema changes, launcher changes, or AlphaClient core wiring are included.
- The GUI is standalone and must be connected by a future integration layer.
- This prototype has not been compiled or tested in-game as part of this change.
