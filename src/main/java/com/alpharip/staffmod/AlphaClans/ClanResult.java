package com.alpharip.staffmod.AlphaClans;

public final class ClanResult {
    private final boolean successful;
    private final String message;
    private final Clan clan;

    private ClanResult(boolean successful, String message, Clan clan) {
        this.successful = successful;
        this.message = message;
        this.clan = clan;
    }

    public static ClanResult success(String message, Clan clan) { return new ClanResult(true, message, clan); }
    public static ClanResult failure(String message) { return new ClanResult(false, message, null); }
    public boolean isSuccessful() { return successful; }
    public String getMessage() { return message; }
    public Clan getClan() { return clan; }
}
