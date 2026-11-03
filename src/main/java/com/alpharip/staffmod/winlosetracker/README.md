# AlphaClient Practice Win/Loss Tracker

The tracker is designed for Minecraft Practice matches across different servers and modes. It does not require a separate tracker per mode.

## Implemented in this feature folder

- Server profiles group multiple explicitly registered hostnames.
- Previously unseen connected hostnames receive a basic profile automatically.
- Overall statistics aggregate matches across every hostname registered to one profile.
- Current-address statistics remain limited to the exact hostname currently connected.
- Individual address statistics can be retrieved separately and displayed on the HUD.
- Hostnames can be linked to an existing server profile through `addAddressToProfile`.
- Confirmed WIN/LOSS results can be recorded against the active server address.
- Display preferences exist for overall, current-address, and individual-address statistics.
- Properties-based local persistence stores profiles, aliases, records, timestamps, current address, and display preferences.
- Server discovery and the last connected address are saved when the active address changes.
- `WinLossResultDetector` recognizes a limited set of complete, explicit victory/defeat announcements after removing Minecraft formatting codes.
- `WinLossTrackerRuntime` contains Forge chat, client-tick, and HUD overlay handlers.

## Example: Minemen Club

One profile can register `minemen.club`, `eu.minemen.club`, `na.minemen.club`, and `as.minemen.club`.

Overall statistics aggregate matches across all registered addresses. Current-address statistics only include matches from the exact hostname currently connected. When enabled, the HUD can also show the other registered addresses separately.

## Remaining integration and verification

- The runtime listener still requires registration by the actual mod integration layer. The `mod-ideas` feature branch does not assume that `AlphaClient.java` is the real entrypoint, and no integration class has been changed.
- The three visibility settings need an in-game settings interface.
- The result detector recognizes only a limited set of complete result announcements. It is not guaranteed to recognize every server's wording and must be validated against actual Practice result messages.
- The duplicate guard suppresses an identical result message for ten seconds; this is a basic safeguard, not a universal match identifier.
- Build and in-game verification have not been performed. The feature remains a standalone foundation until it is integrated into the complete `development` mod.

## Detection limitation

Minecraft Forge does not provide one universal Practice match-result event. Servers use different result messages. The detector deliberately ignores partial phrases and result words embedded in unrelated chat, but additional server-specific result phrases may need to be added after observing the actual messages.
