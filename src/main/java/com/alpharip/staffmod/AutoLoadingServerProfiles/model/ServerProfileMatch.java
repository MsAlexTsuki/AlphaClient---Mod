package com.alpharip.staffmod.AutoLoadingServerProfiles.model;

/** Describes the binding selected for a server connection. */
public final class ServerProfileMatch {
    public enum Type { EXACT, DOMAIN }

    private final ServerProfileBinding binding;
    private final Type type;

    public ServerProfileMatch(ServerProfileBinding binding, Type type) {
        if (binding == null || type == null) throw new IllegalArgumentException("Match values are required");
        this.binding = binding;
        this.type = type;
    }

    public ServerProfileBinding getBinding() { return binding; }
    public Type getType() { return type; }
}
