package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Idea: organize Minecraft resource packs into named groups and show those
 * groups in the resource-pack screen, similar to Lunar Client's Pack Organizer.
 *
 * This model stores pack-to-group assignments. Resource-pack screen wiring,
 * folder discovery, compatibility filtering, and persistence are not included.
 * Search, pack tweaks, and the active-pack HUD already have separate idea
 * scaffolds in this package.
 */
public final class ResourcePackOrganizer {
    private final Map<String, String> groupByPack = new LinkedHashMap<String, String>();

    public void assignPack(String packId, String groupName) {
        if (!hasText(packId) || !hasText(groupName)) {
            return;
        }
        groupByPack.put(packId.trim(), groupName.trim());
    }

    public void removePack(String packId) {
        if (packId != null) {
            groupByPack.remove(packId.trim());
        }
    }

    public String getGroup(String packId) {
        return packId == null ? null : groupByPack.get(packId.trim());
    }

    public Set<String> getGroups() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(groupByPack.values()));
    }

    public Map<String, String> getAssignments() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(groupByPack));
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
