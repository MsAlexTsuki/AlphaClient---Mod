# Punishment Command Preflight

Client-side prototype for reviewing configured punishment commands before they are sent.

## Behavior

1. A Forge client-chat event observes outgoing slash commands.
2. Only commands with an explicitly registered PunishmentCommandRule are intercepted.
3. The command is cancelled and a review screen displays the parsed target, duration, and reason.
4. The user can cancel, or confirm to send the original command unchanged.
5. An optional evidence note can be entered for the local review. It is not transmitted to the server or persisted.

## Configure server-specific command formats

Command syntax is server-specific. This prototype intentionally ships with no assumed punishment command names or formats. Add rules from client initialization after the feature is enabled:

    PunishmentCommandPreflight preflight = PunishmentCommandPreflight.getInstance();
    preflight.addRule(new PunishmentCommandRule("yourcommand", 0, 1, 2));
    preflight.register();

The indexes are zero-based arguments after the command name. For example, 0, 1, 2 means target is argument 0, duration is argument 1, and reason starts at argument 2. Quoted arguments are supported.

## Scope and limitations

- Client-side only; no Supabase and no server changes.
- The server remains authoritative for permissions and punishment execution.
- This folder is a standalone prototype. It is not registered in the AlphaClient core or exposed in the StaffMods menu.
- Staff-rank gating (Trial Moderator and above) must be connected by the eventual module integration.
- Rules are currently configured in code; a settings UI and persistent local configuration are not included.
- Evidence is a local note shown during review; it is not uploaded, attached to a server case, or saved after the screen closes.
- Runtime integration and Minecraft 1.8.9 build verification remain outstanding.
