# Win/Loss Tracker

Initial data-model foundation for the AlphaClient Win/Loss Tracker.

## Included
- Server profiles can group multiple explicitly registered addresses.
- Overall statistics aggregate every match recorded under the profile.
- Current-address statistics are calculated separately for the exact connected address.
- Optional per-address statistics can be exposed by the settings/UI layer.
- Display preferences are represented by `WinLossTrackerSettings`.

## Example
A Minemen Club profile can register `minemen.club`, `eu.minemen.club`,
`na.minemen.club`, and `as.minemen.club`. Overall statistics aggregate
all four addresses, while current-address statistics only include the
address currently connected.

## Not implemented yet
- Automatic Minecraft server connection detection.
- Automatic win/loss detection.
- Automatic alias discovery or DNS/IP resolution.
- Persistence of profiles, aliases, records, and settings.
- Settings GUI, HUD, and in-game display.
- Build and in-game verification.
