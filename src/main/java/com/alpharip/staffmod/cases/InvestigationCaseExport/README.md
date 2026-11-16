# Investigation Case Export

Local text-export prototype based on the original `InvestigationCaseExport` formatter.

- Accepts up to six caller-supplied field key/value pairs.
- Preserves insertion order and formats each field as `key: value`.
- Shows the generated text before saving.
- Saves explicitly to `config/alphaclient/investigation-case-export.txt` under the Minecraft game directory.
- Does not fetch case data; the user supplies the fields to export.

No server/Supabase integration, rank checks, StaffMods registration, automatic case retrieval, or secure/encrypted export is included.