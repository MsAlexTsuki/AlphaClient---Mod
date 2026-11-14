# AlphaClient Store

Location: `src/main/java/com/alpharip/staffmod/store/` (outside `staffops/`).

## Store sections

- **Addons** — locally registered add-on metadata, search, category filters, installed-only filter, details, and local prototype state transitions.
- **Cosmetics** — 128 migrated cosmetic components are automatically registered in the catalogue, with category filters, search, and item details.
- **Partners** — a directory for people who promote AlphaClient, receive staff capes, or collaborate with AlphaClient, with relationship filters, search, and profile details.
- `Store.java` — central section registry, selected-section state, and ownership of the Addons, Cosmetics, and Partners catalogues.
- `StoreGui.java` — Minecraft 1.8.9 section navigation and catalogue browser.
- `StoreSkinPreview.java` — renders the currently logged-in Minecraft player and their active skin in the right-hand Store panel when a world/player is loaded. Main-menu profile rendering is still not implemented.

## Cosmetics files

All 128 files previously stored in `store/Cosmetics/` have been moved into the lowercase package directory `store/cosmetics/`. Their package declarations now use `com.alpharip.staffmod.store.cosmetics`.

- `CosmeticEntry.java` — immutable cosmetic catalogue metadata.
- `CosmeticCategory.java` — Capes, Trails, Emotes, Names, Chat, Effects, UI, and Other filters.
- `CosmeticRegistry.java` — bounded registry populated by trusted AlphaClient code.
- `CosmeticCatalog.java` — local search and category filtering; every constructor registers the migrated components.
- `CosmeticPrototypeCatalog.java` — registers all 128 migrated components with catalogue metadata and category assignments.
- `CosmeticAssetManager.java` — resolves optional texture assets under `src/main/resources/assets/alphastaffmod/textures/cosmetics/<category>/<id>.png` and safely reports missing assets.
- `CosmeticPreviewRenderer.java` — shows the real texture in the Store details panel when present, or a clear missing-asset placeholder. Cape previews preserve the 64×32 Minecraft cape texture ratio.

The migrated Java files are prototype foundations. The catalogue labels them as prototypes and does not claim that live rendering, ownership, equipping, payments, or purchase behavior is implemented. Some files are supporting systems rather than standalone cosmetic products; their catalogue descriptions still explicitly identify the prototype-only state.

## Addons files

- `Addons/AddonEntry.java` — immutable add-on metadata.
- `Addons/AddonCategory.java` — add-on categories.
- `Addons/AddonRegistry.java` — bounded in-memory catalogue and per-item state.
- `Addons/AddonSearch.java` — local text and category filtering.
- `Addons/AddonManager.java` — validates local state transitions.
- `Addons/AddonStore.java` — Addons page facade and state persistence coordinator.
- `Addons/AddonStoreStorage.java` — versioned, bounded local state file with temporary-file replacement.
- `Addons/AddonStoreSettings.java` — search, category, and installed-only filters.
- `Addons/AddonStoreGui.java` — compatibility entry point for the central Store screen.

## Partners files

- `partners/PartnerEntry.java` — immutable directory profile metadata.
- `partners/PartnerType.java` — Partners, Staff, and Collaborator relationship filters. Staff is intended for people who receive staff capes.
- `partners/PartnerRegistry.java` — bounded in-memory directory populated by trusted local AlphaClient code.
- `partners/PartnerCatalog.java` — local search and relationship filtering.

## Current behavior and boundaries

- Addons support local metadata states (available, installed, disabled, removed); optional local state persistence is available through `new AddonStore(path)`. Addon actions do not download or execute files.
- Cosmetics support automatic prototype registration, browsing, search, category filtering, details, and optional texture previews. Missing PNG files do not break the catalogue; the Store displays an asset-missing placeholder. Cape artwork is expected at `src/main/resources/assets/alphastaffmod/textures/cosmetics/capes/<cosmetic-id>.png` in Minecraft 1.8.9 64×32 format. Image-backed Trails, Effects, and UI cosmetics use 64×64 PNG files under their lowercase category directory. Names, Chat, and Emotes are not treated as static PNG cosmetics by this resolver. The selected texture preview is currently a flat asset preview, not a cosmetic attached to the 3D player model. The 128 migrated files are not yet complete live cosmetic implementations.
- Partners support directory registration, search, relationship filtering, and profile details.
- No purchase flow, coin balance, payment provider, ownership grant, or equip operation is implemented.
- No Supabase or remote catalogue is used.
- The Store is not wired into the live AlphaClient menu/module registry.
- The 3D player preview currently requires `mc.thePlayer`; it does not load a session profile from the main menu.
- Minecraft 1.8.9 GUI behavior and Java compilation/runtime behavior have not been verified in the project environment.
