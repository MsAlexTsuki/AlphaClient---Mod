# AlphaClient Store

Location: `src/main/java/com/alpharip/staffmod/store/` (outside `staffops/`).

## Structure

- `Store.java` — central Store core, section registry, selected-section state, and Addons catalogue ownership.
- `StoreGui.java` — Minecraft 1.8.9 screen with catalogue list, search, category filters, installed-only filter, item details, and state actions.
- `Addons/AddonEntry.java` — immutable metadata for a catalogue item.
- `Addons/AddonCategory.java` — catalogue categories.
- `Addons/AddonRegistry.java` — bounded in-memory catalogue and per-item state.
- `Addons/AddonSearch.java` — local text and category filtering.
- `Addons/AddonManager.java` — validates local state transitions.
- `Addons/AddonStore.java` — Addons page facade and state persistence coordinator.
- `Addons/AddonStoreStorage.java` — versioned, bounded local state file with temporary-file replacement.
- `Addons/AddonStoreSettings.java` — search, category, and installed-only filters.
- `Addons/AddonStoreGui.java` — compatibility entry point that opens the central Store screen.

## Current behavior

The Store can display entries registered by trusted AlphaClient code, search them, filter by category, show only entries marked installed, inspect item metadata, and change local item states (available, installed, disabled, removed). State can optionally be saved to a local file by constructing `new AddonStore(path)`; entries themselves are registered by the client and are not downloaded from a catalogue server.

The GUI is opened with `new StoreGui(parentScreen, store)`. The client integration must create the Store with its intended local state path, register the available catalogue entries, and open the GUI from the chosen AlphaClient menu/button.

## Safety and integration boundaries

- No Supabase, remote catalogue, payment provider, download server, automatic external-code installation, or code execution is included.
- “Install”, “Enable”, “Disable”, and “Remove” change local catalogue metadata only. They do not add, remove, load, or unload executable mod files.
- The catalogue is intentionally empty until AlphaClient registers real entries; no fictional products are seeded.
- The core is separated from the live AlphaClient module/menu registry. Wiring that registry and deciding which real add-ons are distributable is an AlphaClient integration task.
- Minecraft 1.8.9 GUI and persistence behavior require compilation and runtime verification in the project environment.
