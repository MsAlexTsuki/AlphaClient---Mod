# Practice Win/Loss Tracker

The tracker is intended for Minecraft Practice matches. It does not require
separate configuration for Boxing, NoDebuff, Sumo, BuildUHC, or other Practice
modes. A recognized win announcement records one win; a recognized defeat/loss
announcement records one loss.

## Included in this feature folder

- Server profiles can group multiple addresses.
- Previously unseen server addresses can receive a basic profile automatically.
- Overall statistics aggregate records in a server profile.
- Current-address statistics remain separate from other addresses in that profile.
- Match records include the result and timestamp.
- Properties-based persistence stores profiles, addresses, records, and display preferences.
- A result detector recognizes a limited set of common explicit victory/defeat phrases.
- A Forge event adapter can read result chat messages, save results, and render a basic statistics overlay.

## Integration requirement

WinLossTrackerRuntime must be initialized and registered on MinecraftForge.EVENT_BUS
from the existing AlphaClient initialization method. That file is outside this feature
folder and has intentionally not been changed.

## Detection limitation

Forge does not expose one universal Practice match-result event. Servers use
different result messages. The detector recognizes common explicit phrases only;
additional real server messages may need matching rules. Identical result text is
suppressed for ten seconds, but this is not a universal match identifier and cannot
guarantee duplicate prevention for every server.

## Not implemented

- A settings screen to toggle the statistics display.
- A server-profile/alias management screen.
- Build and in-game verification.
