# Resource Pack Organizer Prototype

Branch: `mod-ideas`

Package: `com.alpharip.staffmod.resourcepackorganizer`

## Goal

Organize Minecraft 1.8.9 resource packs into **real subfolders** under:

```
.minecraft/resourcepacks/
```

The organizer scans ZIP packs and unpacked resource-pack directories, creates category folders, filters by name, and moves a selected pack between the root and a category folder.

## Current prototype contents

- `ResourcePackEntry`: pack file metadata used by the organizer.
- `ResourcePackFileManager`: discovers categories and packs, creates category folders, and performs guarded file moves.
- `ResourcePackOrganizer`: query and operation facade.
- `ResourcePackOrganizerGui`: standalone management screen with category selection, search, group creation, paging, and move actions.

## Important Minecraft 1.8.9 limitation

Vanilla Minecraft 1.8.9 scans the top level of `resourcepacks`; it does not recursively discover packs inside arbitrary category directories. Therefore, moving a pack into a category subfolder makes it organized on disk but does **not** make that nested pack appear in the vanilla resource-pack selection screen.

A separate integration with Minecraft's resource-pack repository is required before nested packs can be activated from the normal resource-pack screen. This prototype deliberately does not patch that repository, alter active packs, or register itself in AlphaClient Core.

## Not included yet

- Minecraft resource-pack repository bridge and activation/deactivation.
- Active-pack priority ordering.
- Pack preview/metadata display beyond filename and file type.
- Favorites and persistent UI preferences.
- Registration in the AlphaClient menu/module system.

## Safety behavior

- Only ZIP files and directories containing `pack.mcmeta` are treated as packs.
- Category names cannot contain path separators or traversal components.
- Moves are limited to a pack directly inside `resourcepacks` or one immediate category directory.
- Existing destination files are not overwritten.
- The original `staffops/ideas/ResourcePackOrganizer.java` scaffold is left unchanged.
