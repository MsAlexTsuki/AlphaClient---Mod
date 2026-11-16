# Case Redaction Tool

Standalone local prototype for manually redacting supplied case text.

- Accepts a document ID, title, and source text.
- Adds explicit half-open character ranges `[start,end)`; no automatic PII detection or inferred redaction policy is applied.
- Produces a preview that masks selected characters while preserving line breaks.
- Saves source documents and redaction ranges to a versioned local JSON file.
- Retains the original idea facade methods (`addRecord`, `getRecords`, enable state, and `clear`) for source compatibility.

**Limitations:** The current GUI uses a single-line source field and is therefore only suitable for short text during this prototype. The local JSON file contains unredacted source text and must be protected accordingly. No server/Supabase integration, rank checks, StaffMods registration, automatic sensitive-data detection, PDF/image redaction, or compile/runtime verification is included. The preview is not a secure document-export pipeline.
