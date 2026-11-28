# Fishing Bite Alarm

## Purpose

Fishing Bite Alarm is a standalone Minecraft 1.8.9 / Forge prototype. It watches the local player's active fishing hook and plays a local sound when the bobber produces a sharp downward movement while in water.

The prototype does not cast a rod, reel in a catch, change fishing behavior, or interact with a server.

## Original idea settings

The original `staffops/ideas/FishingBiteAlarm.java` contained only:

- `enabled = true`
- `intervalSeconds = 60`

The enabled default is preserved. The original source did not define what the interval represented. In this prototype, `intervalSeconds` is explicitly interpreted as the minimum time between alarm notifications, with the original 60-second default retained.

## Included parts

| File | Responsibility |
|---|---|
| FishingBiteAlarm.java | Standalone module facade, explicit Forge event lifecycle, settings access, and GUI entry point |
| FishingBiteAlarmSettings.java | Validated enable, alert interval, and volume settings |
| FishingHookSample.java | Immutable snapshot of the local fishing hook's identity, vertical position, motion, and water state |
| FishingBiteDetector.java | Detects a sudden downward bobber-motion impulse using a bounded client-side heuristic |
| FishingBiteAlarmEventHandler.java | Samples the local player's active fishing hook at the end of each client tick |
| FishingBiteNotifier.java | Plays the local alarm sound and enforces the minimum alert interval |
| FishingBiteAlarmGui.java | Settings screen for enablement, interval, volume, test sound, save, and return |
| FishingBiteAlarmStorage.java | Local properties-file settings persistence |

## Detection and notification

The event adapter samples the local player's active `EntityFishHook`. The detector triggers only when the hook is in water and its vertical motion changes into a sufficiently strong downward impulse while its Y position is decreasing. It tracks one hook entity at a time and resets when the hook disappears or changes.

The alarm uses the vanilla `random.orb` sound. The volume is configurable from 0% to 100%. The interval is configurable from 1 to 600 seconds and prevents repeated alerts inside that window. The test-sound button bypasses the alert interval.

Settings are stored locally at:

`config/alphaclient/fishing_bite_alarm.properties`

## Current status

**Present in prototype:** standalone module facade; explicit start/stop event registration; local fishing-hook sampling; heuristic downward-motion detection; local sound notification; configurable enablement, alert interval, and volume; test-sound action; settings GUI; local settings persistence.

**Missing / not integrated:** AlphaClient Core/module-menu registration, an approved route to open the settings GUI, and any production integration.

**Not verified:** whether the motion heuristic reliably matches the actual bite animation in Minecraft 1.8.9 / Forge 11.15.1.2318; exact event delivery and sound playback in-game; GUI behavior at different resolutions; settings round-trip behavior; and compilation against the repository's exact ForgeGradle setup. The detector is a heuristic, not a server-confirmed bite signal, and must not be described as fully reliable until tested.

No Supabase calls, remote persistence, Alpha Launcher changes, or AlphaClient Core changes are included.
