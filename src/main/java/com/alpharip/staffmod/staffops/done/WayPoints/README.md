# AlphaClient Waypoints

A local, client-side waypoint system for Minecraft 1.8.9. This document describes the behavior currently implemented in this package.

## Quick start

Open the waypoint screen with:

```text
/waypoint
/waypoint open
```

Create a waypoint at your current position:

```text
/waypoint add "Home Base"
/waypoint list
/waypoint select "Home Base"
```

The selected waypoint appears in the radar label and can be seen as a marker in the world. Waypoints are saved automatically.

## What the features do

### Waypoint records

Each waypoint stores an ID, label, server key, dimension ID, coordinates, category, color, icon text, profile, favorite and visited states, modification time, and whether it was created by the death marker.

Records are stored locally in `config/alphaclient/waypoints.json`. Profiles separate game-mode waypoint sets while retaining server and dimension boundaries. The active profile is selected with `/waypoint profile <name>`; creating a waypoint in a new profile makes that profile available in the settings screen. A waypoint is shown only in the server, dimension, and profile where it belongs. Single-player records use the local world's name. Multiplayer records use the server address.

### Waypoint screen

The compact screen lists waypoints in the current dimension and lets you:

- Create a marker at your current position.
- Select a marker as the navigation target.
- Delete the selected marker.
- Export the current server's waypoints.
- Import the waypoints file currently in the AlphaClient config folder.

Open it with `/waypoint open`. The screen does not pause the game.

### Navigation and route

Selecting a waypoint highlights it as the current target. The radar shows its name and distance.

A route can connect two or more saved waypoints in the same server and dimension. The world renderer draws each route segment. The command reports both direct endpoint distance and the sum of distances between consecutive stops; it does not calculate a path around terrain or obstacles. The selected destination and route are currently session-only and must be selected again after restarting Minecraft.

```text
/waypoint route "Home Base" "Village" Outpost
/waypoint clearroute
```

### World markers

Waypoints in the current server and dimension appear as colored vertical pins with a floating label, icon text, and distance. Markers are drawn within 256 blocks. Beam height and label size are configurable. A block outline can highlight the waypoint's block. Temporary hiding conceals world markers and radar without deleting waypoints; it resets when Minecraft restarts.

```text
/waypoint markers off
/waypoint markers on
```

Open display controls with `/waypoint visuals` or Options. Chroma animates waypoint colors. Beam height cycles through Off, 2, 8, and 32 blocks; label size ranges from 75% to 150%. Block highlight outlines each waypoint block. Hide waypoints temporarily hides world markers and radar until Minecraft restarts.

The icon control cycles built-in compact symbols. Set custom icon text with `/waypoint icon "Mine Entrance" M`; custom text supports up to four characters in Minecraft's font, so unsupported glyphs may not render. Arrival detection marks an unvisited waypoint as visited when you get within three blocks; toggle it with `/waypoint arrival on|off`.

### Radar minimap

The top-right radar shows nearby waypoint dots within 128 blocks. It is north-up and displays the selected target's name and distance. It appears when there is at least one waypoint in the current dimension.

```text
/waypoint minimap off
/waypoint minimap on
```

This radar shows waypoint positions; it does not render terrain.

### Death marker

When the player dies, AlphaClient saves a waypoint named `Death` at the death position. Death markers use the red `Death` category. The feature is enabled by default and can be toggled:

```text
/waypoint death off
/waypoint death on
```

### Waypoints from chat coordinates

Chat messages containing three integer coordinates separated by commas or spaces receive a `[save waypoint]` action. Clicking it fills in an `addat` command for review; the default behavior asks for confirmation before saving.

```text
/waypoint clickcoords off
/waypoint clickcoords on
```

The command can also create a marker directly at known coordinates:

```text
/waypoint addat 120 64 -35 "Mine Entrance"
```

### Categories and colors

Every marker has a category and color. Built-in category colors are General (gold), Death (red), Home (green), Event (cyan), Resource (orange), and Friend (purple). Custom category colors can be assigned to a waypoint with a six-digit hexadecimal color:

```text
/waypoint category "Mine Entrance" Resource FF8800
```

### Favorites and sorting

Favorites stay at the top of the list. Options cycles the remaining waypoints by distance, name, category, or most recently modified. Name completion and lookup are limited to the current server and profile.

### Coordinate history

Recent positions are sampled as the player explores and stored locally, up to 100 locations. `/waypoint history` lists recent positions in the current world; `/waypoint history add <number> [name]` creates a waypoint from one. History samples are recorded after moving about 32 blocks and at least 10 seconds since the previous sample; each entry also shows how long ago it was recorded.

### Event countdowns

Event patterns are configured separately for each server and saved locally. A matching chat line starts a countdown. If the regex has capture groups 1, 2, and 3 containing numeric X, Y, and Z coordinates, the event also creates a waypoint in the Event category.

Example for a server message such as `KOTH at 120,64,-35`:

```text
/waypoint pattern add KOTH 300 KOTH.*?(-?[0-9]+),(-?[0-9]+),(-?[0-9]+)
/waypoint pattern remove KOTH
```

The timer lasts for the configured number of seconds. Active timers last only for the current client session.

### Local sharing and import/export

`export` and `share` write the active profile's waypoints for the current server to:

```text
config/alphaclient/waypoints-share.tsv
```

Send that file to a friend. To import a received file, place it at the same location and run `/waypoint import`. Imported waypoints are assigned to the server and active profile you are currently using; their coordinates, dimension, category, color, icon, death, favorite, and visited states are retained.

This feature does not upload data, message friends, or synchronize through a server or Supabase.

## Commands

| Command | Behavior |
| --- | --- |
| `/waypoint`, `/waypoint open` | Open the waypoint screen |
| `/waypoint visuals` | Open visual, sorting, profile, and arrival options |
| `/waypoint profile <name>` | Switch or create a waypoint profile |
| `/waypoint add <name>` | Save your current coordinates |
| `/waypoint addat <x> <y> <z> [name]` | Save known coordinates |
| `/waypoint list` | List active-profile waypoints using the selected sort |
| `/waypoint select <name>` | Select a navigation target |
| `/waypoint favorite <name>` | Toggle a waypoint favorite |
| `/waypoint icon <name> <symbol>` | Set custom icon text (up to four characters) |
| `/waypoint remove <name>` | Remove a waypoint |
| `/waypoint route <start> <end> [stop ...]` | Draw a multi-stop route and report direct and segment distances |
| `/waypoint clearroute` | Clear the active route |
| `/waypoint history [add <number> [name]]` | View or save a recent coordinate |
| `/waypoint arrival on|off` | Toggle arrival detection |
| `/waypoint category <name> <category> <hex>` | Set category and color |
| `/waypoint minimap on|off` | Toggle the radar |
| `/waypoint markers on|off` | Toggle world markers |
| `/waypoint death on|off` | Toggle automatic death markers |
| `/waypoint clickcoords on|off` | Toggle chat coordinate actions |
| `/waypoint export`, `/waypoint share` | Write the local TSV share file |
| `/waypoint import` | Import the local TSV share file |
| `/waypoint clear confirm` | Delete waypoints in the active profile on this server |
| `/waypoint pattern add <name> <seconds> <regex>` | Add a server-specific chat pattern |
| `/waypoint pattern remove <name>` | Remove a server-specific chat pattern |

## Source file map

| File | Responsibility |
| --- | --- |
| `StaffWaypoint.java` | One saved waypoint and its fields |
| `WaypointManager.java` | CRUD, profiles, current context, selection, routes, and JSON persistence |
| `WaypointSettings.java` | Local saved toggles and active visual options |
| `WaypointFileUtils.java` | Shared safe replacement for local settings, history, and waypoint files |
| `WaypointTracker.java` | Arrival detection and local coordinate history |
| `WaypointVisualSettingsGui.java` | Chroma, beams, labels, profiles, sorting, and arrival controls |
| `ServerWaypointIntegration.java` | Server/world identity and context checks; no network integration |
| `WaypointCategoriesColors.java` | Category palette and per-waypoint color assignment |
| `WaypointsSortedByDistance.java` | Favorite-first sorting by distance, name, category, or modification time |
| `RouteBetweenWaypoints.java` | Multi-stop route segments, direct distance, and total segment distance |
| `WaypointImportExport.java` | TSV encoding, file read, and file write |
| `ShareWaypointsWithFriends.java` | Local export/import wrapper for sharing files |
| `WaypointNameAutocompleteInCommands.java` | Name completion for waypoint commands |
| `WaypointCommand.java` | The `/waypoint` command and its subcommands |
| `WaypointsGui.java` | Compact waypoint list and action screen |
| `DeathWaypointMod.java` | Death detection and automatic marker creation |
| `WaypointFromClickableCoordinates.java` | Chat coordinate detection and confirmation action |
| `MinimapWithWaypoints.java` | Top-right waypoint radar overlay |
| `WorldWaypointIcons.java` | Chroma pins, beams, labels, block outlines, and route rendering |
| `EventCountdownHub.java` | Per-server chat patterns, countdowns, and optional event markers |

## Local files

- Waypoint records: `config/alphaclient/waypoints.json`
- Feature toggles: `config/alphaclient/waypoints.properties`
- Coordinate history: `config/alphaclient/waypoint-coordinate-history.json`
- Event patterns: `config/alphaclient/waypoint_event_patterns_<server-hash>.properties`
- Share/import file: `config/alphaclient/waypoints-share.tsv`

## Verification status

The code is on the `mod-ideas` branch. This branch does not include Gradle build files. It has not been compiled or tested in-game. Treat runtime behavior as unverified until a build and Minecraft session test are run.
