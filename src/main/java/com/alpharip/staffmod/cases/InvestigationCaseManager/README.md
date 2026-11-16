# Investigation Case Manager

Local-only prototype based on the original `InvestigationCases` catalogue.

- Create case entries with a title, summary, generated local ID, and creation timestamp.
- List stored cases and remove a selected entry.
- Save and reload case entries using versioned JSON storage.
- Preserve IDs when restoring valid records; malformed individual records are skipped.
- Includes a dedicated Minecraft 1.8.9-style GUI and a facade for opening it.

No Supabase/server synchronization, authoritative case ownership, rank checks, StaffMods registration, or live case retrieval is included. The Supabase TODO remains a planning comment only.