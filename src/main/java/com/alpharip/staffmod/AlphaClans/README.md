# Alpha Clans prototype

Location: `src/main/java/com/alpharip/staffmod/AlphaClans/` (a sibling of `staffops/`).

## Included

- Clan creation with validated names and tags.
- One-clan-per-player membership.
- Owner, officer, and member roles with permission checks.
- Invitations with seven-day expiry, accept and decline.
- Join, leave, kick, promote, demote, ownership transfer, rename, tag changes, and disband.
- Clan chat history (last 100 messages; 256 characters per message).
- Command parser and command handler with quoted arguments.
- UI-ready screen snapshot model.
- Local binary persistence with version header, bounds checks, and atomic file replacement.

## Local-only prototype boundary

This prototype uses a local file through `FileClanRepository`. It does not use Supabase, a remote API, or account credentials. A local file cannot synchronize clan membership or chat between different players or computers. A server-authoritative repository and Minecraft command/GUI/event registration are integration work and are deliberately not wired into AlphaClient core from this ideas branch.

## Entry point

Create the module with a path chosen by the integration layer:

```java
AlphaClans alphaClans = AlphaClans.open(storagePath);
ClanService clans = alphaClans.getClanService();
ClanScreenModel.Snapshot view = alphaClans.getScreenModel().createSnapshot(playerUuid);
```

The command handler requires a `ClanPlayerDirectory` adapter that resolves player names to UUIDs. The module does not assume a Minecraft player lookup API.
