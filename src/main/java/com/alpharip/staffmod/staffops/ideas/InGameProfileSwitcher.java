package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Foundation for selecting between saved AlphaClient profiles in-game.
 * UI rendering and persistence must be connected by the client integration.
 */
public final class InGameProfileSwitcher {
    private final List<String> profiles = new ArrayList<String>();
    private String activeProfile;

    public void setProfiles(List<String> profileNames) {
        profiles.clear();
        if (profileNames != null) {
            for (String profileName : profileNames) {
                if (profileName != null && !profileName.trim().isEmpty()
                        && !profiles.contains(profileName)) {
                    profiles.add(profileName);
                }
            }
        }
        if (activeProfile != null && !profiles.contains(activeProfile)) {
            activeProfile = null;
        }
    }

    public List<String> getProfiles() {
        return Collections.unmodifiableList(new ArrayList<String>(profiles));
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public boolean selectProfile(String profileName) {
        if (profileName == null || !profiles.contains(profileName)) {
            return false;
        }
        activeProfile = profileName;
        return true;
    }

    public boolean hasProfiles() {
        return !profiles.isEmpty();
    }
}
