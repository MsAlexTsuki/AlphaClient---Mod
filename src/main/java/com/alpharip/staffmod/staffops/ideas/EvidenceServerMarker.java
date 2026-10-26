package com.alpharip.staffmod.staffops.ideas;

/**
 * Local metadata marker for the server context associated with evidence.
 * The supplied address/name is descriptive metadata, not server verification.
 */
public final class EvidenceServerMarker {
    private String serverName = "";
    private String serverAddress = "";
    private long observedAtMillis;

    public void set(String serverName, String serverAddress, long observedAtMillis) {
        this.serverName = serverName == null ? "" : serverName;
        this.serverAddress = serverAddress == null ? "" : serverAddress;
        this.observedAtMillis = observedAtMillis;
    }

    public String getServerName() { return serverName; }
    public String getServerAddress() { return serverAddress; }
    public long getObservedAtMillis() { return observedAtMillis; }
}