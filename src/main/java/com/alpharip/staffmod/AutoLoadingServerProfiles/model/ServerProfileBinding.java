package com.alpharip.staffmod.AutoLoadingServerProfiles.model;

import com.alpharip.staffmod.AutoLoadingServerProfiles.matching.ServerAddressNormalizer;

/** Immutable mapping from a server address/domain to an existing profile identifier. */
public final class ServerProfileBinding {
    private final String serverAddress;
    private final String profileId;
    private final boolean enabled;

    public ServerProfileBinding(String serverAddress, String profileId, boolean enabled) {
        this.serverAddress = ServerAddressNormalizer.normalize(requireText(serverAddress, "serverAddress"));
        this.profileId = requireText(profileId, "profileId");
        this.enabled = enabled;
    }

    public String getServerAddress() { return serverAddress; }
    public String getProfileId() { return profileId; }
    public boolean isEnabled() { return enabled; }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        return value.trim();
    }
}
