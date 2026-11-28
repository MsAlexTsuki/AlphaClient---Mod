package com.alpharip.staffmod.AutoLoadingServerProfiles.runtime;

import com.alpharip.staffmod.AutoLoadingServerProfiles.model.ServerProfileMatch;

/** Outcome of evaluating one server connection. */
public final class ServerProfileAutoLoadResult {
    public enum Status { NO_MATCH, ACTIVATED, ACTIVATION_REJECTED }

    private final Status status;
    private final ServerProfileMatch match;

    private ServerProfileAutoLoadResult(Status status, ServerProfileMatch match) {
        this.status = status;
        this.match = match;
    }

    public static ServerProfileAutoLoadResult noMatch() {
        return new ServerProfileAutoLoadResult(Status.NO_MATCH, null);
    }

    public static ServerProfileAutoLoadResult activated(ServerProfileMatch match) {
        return new ServerProfileAutoLoadResult(Status.ACTIVATED, match);
    }

    public static ServerProfileAutoLoadResult rejected(ServerProfileMatch match) {
        return new ServerProfileAutoLoadResult(Status.ACTIVATION_REJECTED, match);
    }

    public Status getStatus() { return status; }
    public ServerProfileMatch getMatch() { return match; }
}
