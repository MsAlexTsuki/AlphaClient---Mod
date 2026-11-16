package com.alpharip.staffmod.punishmentcommandpreflight;

public final class PunishmentCommandRule {
    private final String commandName;
    private final int targetArgument;
    private final int durationArgument;
    private final int reasonStartArgument;

    public PunishmentCommandRule(String commandName, int targetArgument, int durationArgument, int reasonStartArgument) {
        if (commandName == null || commandName.trim().isEmpty()) throw new IllegalArgumentException("Command name must not be blank.");
        if (targetArgument < 0 || durationArgument < 0 || reasonStartArgument < 0) throw new IllegalArgumentException("Argument indexes must be zero or greater.");
        this.commandName = commandName.replaceFirst("^/", "").trim().toLowerCase(java.util.Locale.ROOT);
        this.targetArgument = targetArgument;
        this.durationArgument = durationArgument;
        this.reasonStartArgument = reasonStartArgument;
    }

    public String getCommandName() { return commandName; }
    public int getTargetArgument() { return targetArgument; }
    public int getDurationArgument() { return durationArgument; }
    public int getReasonStartArgument() { return reasonStartArgument; }
}
