# Ally Faction Notes Prototype

Standalone Minecraft 1.8.9 / Forge GUI prototype for keeping local notes grouped by allied faction.

## Included

- Add notes under a faction name.
- Browse saved faction names and read the notes for the selected faction.
- Clear all notes belonging to a selected or entered faction.
- Persist data locally in `config/alphaclient/ally-faction-notes.json`.
- Scroll the faction and note lists independently.
- Keep the GUI open without pausing singleplayer.

## Entry point

Call `AllyFactionNotesModule.open()` from a future Kitmap/HCF menu integration.

## Deliberately not connected

- No AlphaClient core registration or module registry changes.
- No Supabase, network sync, or account-based sharing.
- No permission/rank assumptions; required access remains unconfirmed.
- No automatic faction detection. The user enters the faction name.

## Prototype status

The source files are placed on the `mod-ideas` branch only. This is not wired into the live client and has not been compiled or tested in Minecraft.
