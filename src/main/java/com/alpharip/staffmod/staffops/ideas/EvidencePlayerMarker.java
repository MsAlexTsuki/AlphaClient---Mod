package com.alpharip.staffmod.staffops.ideas;

/** Local reference linking an evidence item to a player name and observed time. */
public final class EvidencePlayerMarker {
    private String playerName = "";
    private String evidenceReference = "";
    private long observedAtMillis;

    public void set(String playerName, String evidenceReference, long observedAtMillis) {
        this.playerName = playerName == null ? "" : playerName;
        this.evidenceReference = evidenceReference == null ? "" : evidenceReference;
        this.observedAtMillis = observedAtMillis;
    }

    public String getPlayerName() { return playerName; }
    public String getEvidenceReference() { return evidenceReference; }
    public long getObservedAtMillis() { return observedAtMillis; }
}