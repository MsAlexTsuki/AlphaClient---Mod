# Evidence Case Linker

Standalone local registry for associating an evidence reference with a case ID.

- Creates or replaces a link by evidence reference.
- Stores an optional note and creation timestamp.
- Supports lookup by exact reference, filtering by partial reference, lookup by case ID, and unlinking.
- Saves and loads links through a local JSON file.
- Retains the original `link`, `getCaseId`, and `getLinks` facade methods.

**Limitations:** Evidence references are treated as opaque text; no files are copied, uploaded, validated, or inspected. Case IDs are not checked against a live case system. No server/Supabase integration, rank checks, StaffMods registration, or compile/runtime verification is included.
