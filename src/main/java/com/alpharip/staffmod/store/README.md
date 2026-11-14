# AlphaClient Store

Location: `src/main/java/com/alpharip/staffmod/store/` (outside `staffops/`).

## Store sections

- **Addons** — locally registered add-on metadata, search, category filters, installed-only filter, details, and local prototype state transitions.
- **Cosmetics** — a separate cosmetic catalogue with category filters, search, and item details.
- **Partners** — a directory for people who promote, help, or collaborate with AlphaClient, with relationship filters, search, and profile details.
- `Store.java` — central section registry, selected-section state, and ownership of the Addons, Cosmetics, and Partners catalogues.
- `StoreGui.java` — Minecraft 1.8.9 section navigation and catalogue browser.

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

## Cosmetics files

- `cosmetics/CosmeticEntry.java` — immutable cosmetic catalogue metadata.
- `cosmetics/CosmeticCategory.java` — Capes, Trails, Emotes, Names, Chat, Effects, UI, and Other filters.
- `cosmetics/CosmeticRegistry.java` — bounded registry populated by trusted AlphaClient code.
- `cosmetics/CosmeticCatalog.java` — local search and category filtering.

## Partners files

- `partners/PartnerEntry.java` — immutable partner profile metadata (name, relationship type, description, platform, and optional profile link).
- `partners/PartnerType.java` — Promoter, Helper, and Collaborator relationship filters.
- `partners/PartnerRegistry.java` — bounded in-memory directory populated by trusted local AlphaClient code.
- `partners/PartnerCatalog.java` — local search and relationship filtering.

## Current behavior

Both sections are browseable through the central Store GUI. Addons support local metadata states (available, installed, disabled, removed); optional local state persistence is available through `new AddonStore(path)`. Cosmetics support catalogue registration, search, category filtering, and details. Partners support directory registration, search, relationship filtering, and profile details. The catalogues are not pre-populated with invented entries.

## Explicit integration boundaries

- Addon “Install”, “Enable”, “Disable”, and “Remove” actions change local metadata only. They do not download, add, load, unload, or execute executable mod files.
- Cosmetics are browse-only catalogue entries. No purchase, coin balance, ownership grant, equip operation, payment provider, or remote service is implemented.
- Partners are directory records only. No partnership approval, messaging, social-platform verification, payment, or external profile lookup is implemented.
- No Supabase or remote catalogue is used.
- The 128 files under the moved `Cosmetics/` directory are existing idea/prototype files. Their current package declarations still use `com.alpharip.staffmod.staffops.ideas`; they are not silently treated as working Store products by this catalogue.
- The live AlphaClient module/menu registry is not wired to this Store prototype.
- Minecraft 1.8.9 GUI behavior and Java compilation/runtime behavior have not been verified in the project environment.
