# Playtime Badges Prototype

## Milestones
- 1 hour
- 1 day
- 1 month
- 3 months
- 6 months
- 1 year
- 2 years

## Playtime source
This prototype reads the existing cumulative counter through
`com.alpharip.staffmod.hud.PlaytimeTrackerHud.getTotalPlaytimeSeconds()`.
That counter loads the saved total from `config/alphaclient-playtime.txt`
and adds the current Minecraft process session. Its tracking is independent
of whether the Playtime Tracker HUD is enabled or visible.

## Threshold convention
The existing Playtime Tracker defines a month as 30 days and a year as
365 days. The badge thresholds deliberately use the same convention:
- 1 month = 30 days
- 3 months = 90 days
- 6 months = 180 days
- 1 year = 365 days
- 2 years = 730 days

## Scope
- Standalone prototype under Badges/Playtime.
- No edits to Alpha Client Core or the development branch.
- No Supabase integration or account-service calls.
- The GUI is a preview screen; opening it from a menu and registering any
  optional badge notifications must be integrated later in the intended
  client module flow.
- No badge textures/artwork are included in this prototype.
