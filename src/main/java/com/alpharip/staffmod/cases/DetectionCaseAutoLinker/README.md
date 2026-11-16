# Detection Case Auto-Linker

Standalone local prototype that links supplied detection-case records when all explicitly configured attribute keys have equal values.

- Stores caller-supplied case IDs, labels, and attributes.
- The user explicitly configures the attribute keys used for matching; with no keys configured, no automatic links are created.
- Exact value comparison is case-insensitive.
- Re-evaluates existing records when matching keys change and removes links when a case is removed.
- Persists records and configured keys locally as JSON.
- Retains the original idea facade methods (`addRecord`, `getRecords`, enable state, and `clear`).

**Limitations:** The prototype does not receive detection events, fetch real cases, infer matching rules, or modify case records. The current GUI captures one attribute per newly entered case. No server/Supabase integration, rank checks, StaffMods registration, or compile/runtime verification is included.
