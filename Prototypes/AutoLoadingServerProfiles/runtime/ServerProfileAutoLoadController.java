package com.alpharip.staffmod.AutoLoadingServerProfiles.runtime;

import com.alpharip.staffmod.AutoLoadingServerProfiles.matching.ServerProfileMatcher;
import com.alpharip.staffmod.AutoLoadingServerProfiles.model.ServerProfileMatch;
import com.alpharip.staffmod.AutoLoadingServerProfiles.registry.ServerProfileRegistry;

/** Coordinates address matching and delegates profile activation to the client integration. */
public final class ServerProfileAutoLoadController {
    private final ServerProfileRegistry registry;
    private final ServerProfileMatcher matcher;
    private final ServerProfileActivationPort activationPort;

    public ServerProfileAutoLoadController(ServerProfileRegistry registry,
                                           ServerProfileMatcher matcher,
                                           ServerProfileActivationPort activationPort) {
        if (registry == null || matcher == null || activationPort == null) {
            throw new IllegalArgumentException("Registry, matcher and activation port are required");
        }
        this.registry = registry;
        this.matcher = matcher;
        this.activationPort = activationPort;
    }

    public ServerProfileAutoLoadResult onServerConnected(String serverAddress) {
        ServerProfileMatch match = matcher.findBest(serverAddress, registry.getAll());
        if (match == null) return ServerProfileAutoLoadResult.noMatch();

        boolean activated = activationPort.activateProfile(match.getBinding().getProfileId());
        return activated ? ServerProfileAutoLoadResult.activated(match)
                : ServerProfileAutoLoadResult.rejected(match);
    }
}
