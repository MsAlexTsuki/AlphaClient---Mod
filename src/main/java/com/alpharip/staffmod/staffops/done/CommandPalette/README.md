# AlphaClient Command Palette

Open with **P** in a world (rebind it under Minecraft Controls → AlphaClient) or with `/palette`.

Search and launch AlphaClient screens, waypoint actions, and registered modules. Use the category tabs to show all, recent, pinned, module, screen, waypoint, or command actions. Search ranks exact and partial matches before fuzzy matches. Use arrow keys and Enter, or click an entry. Pin actions with the plus at the right of a row or Ctrl+F. The palette stores pins and the last 12 actions locally in `config/alphaclient/command-palette.properties`.

Implemented and accessible modules have a toggle action and a direct settings action. Locked or unfinished modules appear as status entries and cannot be activated from this screen. Module changes use the existing AlphaClient permission and profile APIs.

Waypoint quick actions include creating a waypoint at the current position, selecting a waypoint, changing profile, and toggling a favorite. These actions prompt for a name or profile value before running the corresponding local command.

The palette does not pause the game. Its search text is temporary; favorites and recent actions are stored locally.
