package com.alpharip.staffmod.AutoLoadingServerProfiles.runtime;

/**
 * Adapter boundary for the existing profile system.
 * A Minecraft/client integration must implement this interface and activate the requested profile.
 */
public interface ServerProfileActivationPort {
    boolean activateProfile(String profileId);
}
