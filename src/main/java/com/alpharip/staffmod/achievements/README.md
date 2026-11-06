# Achievements Prototype

Folder: `achievements/`

This is a local Java prototype for an achievement checklist with explanations. It does not define a production achievement catalogue or connect to account data.

- **AchievementEntry.java** — Holds one achievement's ID, title, description, explanation, category, target, progress, and unlock timestamp. Validate timestamp semantics and define how progress is populated by approved gameplay events.
- **AchievementChecklist.java** — Provides an in-memory registry, progress updates, unlock operations, and completed-entry queries. Add persistence only after the local data format and lifecycle are specified.
- **AchievementChecklistSearch.java** — Performs case-insensitive matching against title, description, explanation, and category. Add tests for empty queries, null input, and Unicode/case behavior.
- **AchievementChecklistSettings.java** — Holds session-local enablement and search query. Persistence and a settings editor are not implemented.
- **AchievementChecklistGui.java** — Provides a Minecraft 1.8.9 preview with selectable entries, progress states, explanation text, and navigation. Connect it to a real menu only after the catalogue and entry source are defined; finish layout/overflow handling for long text and larger lists.

**Prototype boundary:** No predefined achievement list, gameplay event hooks, persistent storage, Supabase integration, account synchronization, or AlphaClient Core integration is included. The GUI requires caller-supplied entries. No compile or in-game test is claimed.
