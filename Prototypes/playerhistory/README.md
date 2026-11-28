# Player History

Standalone prototype folder on branch `mod-ideas`, located beside `staffops/`.

## Purpose

Keep a local history of player identities observed in the active Minecraft client session. The prototype records when a player is first observed, later joins, leaves, session counts, and accumulated observed online time. A searchable standalone screen displays the locally collected summaries.

## Files

- **PlayerHistory.java** — Module facade and lifecycle boundary. Retains the original `isEnabled`, `setEnabled`, `addRecord`, `getRecords`, and `clearRecords` API; exposes explicit register/unregister and GUI-opening methods.
- **PlayerHistoryEntry.java** — Immutable per-player summary with UUID, latest name, first/last observation timestamps, session count, and accumulated online duration.
- **PlayerHistoryEvent.java** — Immutable first-observed/joined/left event with timestamp and connection context.
- **PlayerHistoryStore.java** — Maintains player summaries and a bounded 500-event timeline; returns sorted immutable snapshots.
- **PlayerHistoryStorage.java** — Loads/saves the local Properties file at `config/alphaclient/player-history.properties`; malformed individual entries are skipped and writes use a temporary file before replacement.
- **PlayerHistoryTracker.java** — Samples the player list exposed by the active client connection every 20 client ticks, ignores the local player, and detects observed presence transitions.
- **PlayerHistoryEventHandler.java** — Forge client-tick adapter; registration remains explicit and is not performed by AlphaClient Core.
- **PlayerHistoryFormatter.java** — Formats timestamps and accumulated durations for the screen.
- **PlayerHistoryGui.java** — Standalone searchable player list with online/offline state, session count, total observed time, and scroll support.

## Prototype boundaries and verification

- This prototype sees only player entries exposed through the local client's network player list. It cannot recover events from before tracking was enabled or inspect server-side/private player data.
- The first sample is labelled **FIRST_OBSERVED**, not a confirmed join. Later additions/removals from the visible player list are represented as JOINED/LEFT observations.
- Time totals are client-observed estimates. Polling occurs every 20 client ticks; brief transitions between samples may be missed.
- Tracking stops and active sessions are closed when the client disconnects, changes connection context, disables the module, or unregisters it.
- Data is stored locally only. No Supabase, remote service, account lookup, or server request is included.
- The GUI is a standalone prototype. No menu button, keybind, rank permission, StaffMods integration, or AlphaClient Core registration is added.
- No compile, automated test, or in-game test is claimed. Minecraft/Forge API compatibility and runtime behavior still require verification.

## Prototype decisions not specified by the source idea

The source idea contained only a basic in-memory text-record model. The live player-list sampling, per-UUID summaries, event timeline, searchable GUI, local Properties persistence, 20-tick sampling interval, and 500-event cap are implementation choices made for this prototype. They are not claims about requirements from the original idea.
