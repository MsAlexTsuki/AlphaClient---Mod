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

Each waypoint stores an ID, label, server key, dimension ID, coordinates, category, color, icon label, and whether it was created by the death marker.

Records are stored locally in `config/alphaclient/waypoints.json`. A waypoint is shown only in the server and dimension where it belongs. Single-player records use the local world's name. Multiplayer records use the server address.

### Waypoint screen

The compact screen lists waypoints in the current dimension, nearest first. It lets you:

- Create a marker at your current position.
- Select a marker as the navigation target.
- Delete the selected marker.
- Export the current server's waypoints.
- Import the waypoints file currently in the AlphaClient config folder.

Open it with `/waypoint open`. The screen does not pause the game.

### Navigation and route

Selecting a waypoint highlights it as the current target. The radar shows its name and distance.

A route connects two saved waypoints in the same server and dimension. The world renderer draws a straight line between them and the radar displays the route distance. This is a direct-line guide; it does not calculate a path around terrain or obstacles. The selected destination and route are currently session-only and must be selected again after restarting Minecraft.

```text
/waypoint route "Home Base" "Village"
/waypoint clearroute
```

### World markers

Waypoints in the current server and dimension appear as colored vertical pins with a floating label, icon text, and distance. Markers are drawn within 256 blocks. The marker display can be toggled and the choice is saved locally.

```text
/waypoint markers off
/waypoint markers on
```

Open the visual controls with /waypoint visuals or the Options button. Chroma animates waypoint colors. Beam height cycles through Off, 2, 8, and 32 blocks. Label size cycles from 75% to 150%. The icon control cycles through pin, home, star, death, diamond, chest, and event; with a waypoint selected it changes that waypoint, otherwise it sets the default for new waypoints. Hide waypoints temporarily hides the world markers and radar without deleting data. Block highlight draws an outline around each waypoint block.

Icon IDs are rendered as compact text symbols beside the label, not image files.

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

### Distance sorting and command completion

The screen and `list` command show current-dimension waypoints nearest first. Names can be completed for waypoint commands. Completion and name lookup use markers belonging to the current server.

### Event countdowns

Event patterns are configured separately for each server and saved locally. A matching chat line starts a countdown. If the regex has capture groups 1, 2, and 3 containing numeric X, Y, and Z coordinates, the event also creates a waypoint in the Event category.

Example for a server message such as `KOTH at 120,64,-35`:

```text
/waypoint pattern add KOTH 300 KOTH.*?(-?[0-9]+),(-?[0-9]+),(-?[0-9]+)
/waypoint pattern remove KOTH
```

The timer lasts for the configured number of seconds. Active timers last only for the current client session.

### Local sharing and import/export

`export` and `share` write the current server's waypoints to:

```text
config/alphaclient/waypoints-share.tsv
```

Send that file to a friend. To import a received file, place it at the same location and run `/waypoint import`. Imported waypoints are assigned to the server you are currently connected to; their coordinates, dimension, category, color, and icon label are retained.

This feature does not upload data, message friends, or synchronize through a server or Supabase.

## Commands

| Command | Behavior |
| --- | --- |
| `/waypoint`, `/waypoint open` | Open the waypoint screen |
| `/waypoint visuals` | Open marker display options |
| `/waypoint add <name>` | Save your current coordinates |
| `/waypoint addat <x> <y> <z> [name]` | Save known coordinates |
| `/waypoint list` | List current-dimension waypoints, nearest first |
| `/waypoint select <name>` | Select a navigation target |
| `/waypoint remove <name>` | Remove a waypoint |
| `/waypoint route <start> <end>` | Draw a straight-line route between two markers |
| `/waypoint clearroute` | Clear the active route |
| `/waypoint category <name> <category> <hex>` | Set category and color |
| `/waypoint minimap on|off` | Toggle the radar |
| `/waypoint markers on|off` | Toggle world markers |
| `/waypoint death on|off` | Toggle automatic death markers |
| `/waypoint clickcoords on|off` | Toggle chat coordinate actions |
| `/waypoint export`, `/waypoint share` | Write the local TSV share file |
| `/waypoint import` | Import the local TSV share file |
| `/waypoint clear confirm` | Delete this server's waypoints |
| `/waypoint pattern add <name> <seconds> <regex>` | Add a server-specific chat pattern |
| `/waypoint pattern remove <name>` | Remove a server-specific chat pattern |

## Source file map

| File | Responsibility |
| --- | --- |
| `StaffWaypoint.java` | One saved waypoint and its fields |
| `WaypointManager.java` | CRUD, current context, selection, routes, and JSON persistence |
| `WaypointSettings.java` | Local saved toggles for overlays and capture |
| `ServerWaypointIntegration.java` | Server/world identity and context checks; no network integration |
| `WaypointCategoriesColors.java` | Category palette and per-waypoint color assignment |
| `WaypointsSortedByDistance.java` | Nearest-first sorting |
| `RouteBetweenWaypoints.java` | Route endpoints, straight-line distance, and bearing calculations |
| `WaypointImportExport.java` | TSV encoding, file read, and file write |
| `ShareWaypointsWithFriends.java` | Local export/import wrapper for sharing files |
| `WaypointNameAutocompleteInCommands.java` | Name completion for waypoint commands |
| `WaypointCommand.java` | The `/waypoint` command and its subcommands |
| `WaypointsGui.java` | Compact waypoint list and action screen |
| `DeathWaypointMod.java` | Death detection and automatic marker creation |
| `WaypointFromClickableCoordinates.java` | Chat coordinate detection and confirmation action |
| `MinimapWithWaypoints.java` | Top-right waypoint radar overlay |
| `WorldWaypointIcons.java` | World marker rendering and icon-label access |
| `EventCountdownHub.java` | Per-server chat patterns, countdowns, and optional event markers |

## Local files

- Waypoint records: `config/alphaclient/waypoints.json`
- Feature toggles: `config/alphaclient/waypoints.properties`
- Event patterns: `config/alphaclient/waypoint_event_patterns_<server-hash>.properties`
- Share/import file: `config/alphaclient/waypoints-share.tsv`

## Verification status

The code has been added to the `mod-ideas` branch, but this branch does not contain its Gradle build setup. It has not been compiled or tested in-game. Treat runtime behavior as unverified until a build and Minecraft session test are run.
