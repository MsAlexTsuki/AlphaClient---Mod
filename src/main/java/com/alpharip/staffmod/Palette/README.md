# AlphaClient Command Palette

The Command Palette is part of the current `development` branch and is opened with **P** while in a world or with **/palette**.

## Included development integration

- Reads modules dynamically from `AlphaModuleRegistry`, so newly registered development modules appear without maintaining a second module list.
- Shows registered module features from `AlphaModule.getFeatures()`, including their current available, locked, and coming-soon states.
- Allows module and implemented feature toggles only when the current permission checks report access.
- Opens current AlphaClient screens and command routes, including General, Quality, HUD Editor, Profiles, Staff Operations, StaffMods, Waypoints, and Minimap settings.
- Adds rank-folder shortcuts only when the current rank checks allow them. The Founder folder shortcut requires a signed-in Founder session; Owner does not receive it.
- Keeps pins and the twelve most recent actions in `config/alphaclient/command-palette.properties`.

## Controls

- Search by action name, description, category, or command.
- Use Up/Down and Enter to navigate and run an action.
- Click an action to run it.
- Click **+** or press **Ctrl+F** to pin/unpin the selected action.
- Use the category tabs to filter All, Recent, Favorites, Modules, Screens, Waypoints, or Commands.
- Press Esc to close the palette.
