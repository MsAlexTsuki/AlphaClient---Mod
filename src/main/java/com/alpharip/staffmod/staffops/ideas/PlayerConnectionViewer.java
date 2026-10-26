package com.alpharip.staffmod.staffops.ideas;

/**
 * Local snapshot of connection information available to this client.
 * It cannot inspect private server-side connection diagnostics.
 */
public final class PlayerConnectionViewer {
    private String serverAddress = "";
    private String connectionState = "Unknown";
    private long capturedAtMillis;

    public void capture(String serverAddress, String connectionState, long capturedAtMillis) {
        this.serverAddress = serverAddress == null ? "" : serverAddress;
        this.connectionState = connectionState == null ? "Unknown" : connectionState;
        this.capturedAtMillis = capturedAtMillis;
    }

    public String getServerAddress() { return serverAddress; }
    public String getConnectionState() { return connectionState; }
    public long getCapturedAtMillis() { return capturedAtMillis; }
}