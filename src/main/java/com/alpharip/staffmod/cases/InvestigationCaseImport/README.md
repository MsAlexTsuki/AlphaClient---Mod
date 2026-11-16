# Investigation Case Import

Local text-import prototype based on the original `InvestigationCaseImport` parser.

- Accepts up to eight caller-supplied lines in `key:value` format.
- Splits on the first colon, trims keys and values, and ignores lines without a valid non-empty key.
- Shows a parsed preview and reports how many fields were recognized.
- Duplicate keys follow the original map behavior: the later value replaces the earlier value.
- Import is preview-only; no case is changed or created automatically.

No file picker, server/Supabase integration, rank checks, StaffMods registration, or automatic case updates are included.