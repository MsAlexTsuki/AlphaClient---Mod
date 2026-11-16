# Punishment Conflict Detector

Standalone client-side prototype for identifying overlapping active punishment records.

## Conflict rule

The prototype reports a conflict only when both records:
- are active at the time of the check (not revoked, already issued, and not expired);
- refer to the same target name, compared case-insensitively; and
- have the same punishment type.

Different punishment types are not automatically treated as conflicting. Permanent records use an expiry timestamp of `0`.

## Components

- `PunishmentRecord.java` — immutable local record with target, type, reason, issue/expiry timestamps, and revoked state.
- `PunishmentType.java` — supported punishment categories.
- `PunishmentConflict.java` — immutable pair returned when the rule matches.
- `PunishmentConflictDetector.java` — manages supplied records and detects conflicts.
- `PunishmentConflictDetectorGui.java` — read-only conflict overview with refresh and close actions.

## Integration example

```java
PunishmentConflictDetector detector = new PunishmentConflictDetector();
detector.addRecord(new PunishmentRecord(
    "case-001", "ExamplePlayer", PunishmentType.BAN, "Repeated cheating",
    issuedAtMillis, PunishmentRecord.PERMANENT, false
));
mc.displayGuiScreen(detector.createGui(mc.currentScreen));
```

The caller must provide actual records and timestamps. No server command format, punishment database, or server API is assumed.

## Scope and limitations

- Local prototype only; no Supabase, remote persistence, or server changes.
- No automatic retrieval of punishment history; a future approved adapter must supply records.
- No punishment creation, editing, revocation, or command execution.
- The GUI is a read-only viewer and is not registered in AlphaClient Core or the StaffMods menu.
- The access label documents the intended minimum rank (Trial Moderator); real rank authorization is not connected and must be enforced by the eventual integration.
- No default server-specific punishment rules are included.
- Minecraft 1.8.9 / Forge 11.15.1.2318 compilation and runtime behavior have not been verified.
