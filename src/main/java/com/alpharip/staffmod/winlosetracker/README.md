# AlphaClient Practice Win/Loss Tracker

A server-bound Practice win/loss tracker foundation.

## Current implementation

- Server profiles can group multiple explicitly registered hostnames.
- Unknown connected hostnames can receive their own basic profile through `getOrCreateProfileForAddress`.
- One profile can aggregate matches across its registered hostnames.
- Current-address statistics remain isolated to the exact connected hostname.
- Individual address statistics can be retrieved separately.
- A hostname can be linked to an existing profile through `addAddressToProfile`.
- Confirmed match results can be recorded against the current connected hostname.
- Overall, current-address, and individual-address visibility preferences are represented by `WinLossTrackerSettings`.
- `WinLossTrackerStorage` saves and restores profiles, aliases, match records, timestamps, current address, and settings to a caller-supplied local file.

## Example: Minemen Club

A Minemen Club profile may explicitly register:
- `minemen.club`
- `eu.minemen.club`
- `na.minemen.club`
- `as.minemen.club`

Overall statistics aggregate matches across all registered addresses. Current-address statistics only include matches from the exact hostname currently connected.

## Integration still required

- Registering the tracker with the Minecraft 1.8.9 / Forge client lifecycle.
- Reading the connected server hostname from the active Minecraft connection.
- Calling storage load/save from the appropriate client lifecycle and shutdown points.
- Connecting the three display preferences to an in-game settings screen.
- Rendering the selected statistics in a HUD or tracker screen.
- Detecting completed Practice matches from reliable server result messages.
- Preventing duplicate result registration when a server emits repeated messages.

## Detection limitation

There is no universal Practice result packet or chat format shared by every Minecraft server. The current code records a result only when another component confirms `WIN` or `LOSS`. Automatic result detection must be connected to verified result signals; generic chat keyword matching must not count arbitrary messages as matches.

## Verification

This change has not been built or tested in Minecraft.