# AlphaClient Practice Win/Loss Tracker

The tracker is designed for Minecraft Practice matches across different servers and modes. It does not require a separate tracker per mode.

## Implemented in this feature folder

- Server profiles group multiple explicitly registered hostnames.
- Previously unseen connected hostnames receive a basic profile automatically.
- Overall statistics aggregate matches across every hostname registered to one profile.
- Current-address statistics remain limited to the exact hostname currently connected.
- Individual address statistics can be retrieved separately.
- Hostnames can be linked to an existing server profile through `addAddressToProfile`.
- Confirmed WIN/LOSS results can be recorded against the active server address.
- Display preferences exist for overall, current-address, and individual-address statistics.
- Properties-based local persistence stores profiles, aliases, records, timestamps, current address, and display preferences.
- `WinLossResultDetector` recognizes a limited set of explicit victory/defeat phrases.
- `WinLossTrackerRuntime` contains Forge chat, client-tick, and HUD overlay handlers.

## Example: Minemen Club

One profile can register `minemen.club`, `eu.minemen.club`, `na.minemen.club`, and `as.minemen.club`.

Overall statistics aggregate matches across all registered addresses. Current-address statistics only include matches from the exact hostname currently connected.

## Remaining integration and verification

- The runtime listener must be registered from AlphaClient's existing initialization code. That central entrypoint has not been modified as part of this feature-only scope.
- The three visibility settings need an in-game settings interface. The current overlay displays overall and current-address statistics; individual-address breakdown is not rendered yet.
- The result detector currently recognizes a limited set of common explicit phrases. It is not guaranteed to recognize every server's wording and must be validated against actual Practice result messages.
- The duplicate guard suppresses identical result text for ten seconds; this is a basic safeguard, not a universal match identifier.
- Build and in-game verification have not been performed.

## Detection limitation

Minecraft Forge does not provide one universal Practice match-result event. Servers use different result messages. The detector deliberately ignores neutral or ambiguous chat, but additional server-specific result phrases may need to be added after observing the actual messages.