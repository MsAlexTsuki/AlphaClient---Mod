package com.alpharip.staffmod.punishmentcommandpreflight;

/** Immutable preview of a command intercepted before it is sent. */
public final class PunishmentCommandDraft {
    private final String originalCommand, commandName, target, duration, reason, evidence;

    public PunishmentCommandDraft(String originalCommand, String commandName, String target, String duration, String reason) {
        this(originalCommand, commandName, target, duration, reason, "");
    }

    private PunishmentCommandDraft(String originalCommand, String commandName, String target, String duration, String reason, String evidence) {
        this.originalCommand = safe(originalCommand);
        this.commandName = safe(commandName);
        this.target = safe(target);
        this.duration = safe(duration);
        this.reason = safe(reason);
        this.evidence = safe(evidence);
    }

    public PunishmentCommandDraft withEvidence(String value) {
        return new PunishmentCommandDraft(originalCommand, commandName, target, duration, reason, value);
    }

    public String getOriginalCommand() { return originalCommand; }
    public String getCommandName() { return commandName; }
    public String getTarget() { return target; }
    public String getDuration() { return duration; }
    public String getReason() { return reason; }
    public String getEvidence() { return evidence; }
    private static String safe(String value) { return value == null ? "" : value; }
}
