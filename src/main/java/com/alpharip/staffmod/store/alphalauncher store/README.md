# AlphaLauncher Store Prototype

This folder contains a standalone, responsive frontend prototype for the Store experience intended for AlphaLauncher.

## Scope
- AlphaClient cosmetics only: capes, emotes, nametag styles, effects, and UI cosmetics.
- Memberships are shown as a possible future area.
- No resource packs, maps, worlds, general digital products, or third-party marketplace listings.

## Current prototype features
- Cosmetics catalogue with category filters, text search, and sorting.
- Product detail modal with item type, description, collection label, and item-specific preview artwork.
- Distinct visual treatments for capes, emotes, nametags, effects, and UI cosmetics.
- My Collection view with clearly marked sample-owned cosmetics.
- Equip and unequip interactions for sample-owned cosmetics.
- The equipped sample item is remembered locally in the browser when storage is available.
- Responsive desktop and mobile layouts.

## Important boundaries
- This is a standalone visual and interaction prototype, not a live AlphaLauncher integration.
- Catalogue entries and owned items are sample preview data, not real products, purchases, or account entitlements.
- Equip state is local browser demo state only; it is not synced to an Alpha account or Minecraft.
- No checkout, payment processing, balance, Supabase access, account ownership, entitlement grants, or live catalogue are implemented.
- No AlphaLauncher or AlphaClient production files are changed by this prototype.

## Preview
Open `index.html` in a browser to inspect the prototype.

- Interactive Minecraft-inspired player preview in the cosmetic detail modal.
- Drag horizontally with the mouse or touch to rotate the player model.
- Cosmetic-specific visual overlays are shown on the preview model.
- The player preview is a CSS-based visual mock-up, not a real Minecraft skin renderer or in-game preview.
