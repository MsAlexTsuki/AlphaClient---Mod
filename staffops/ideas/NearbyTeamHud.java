package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data foundation for an in-game nearby-team HUD.
 * Minecraft team membership, live entity data, and HUD rendering must be
 * connected by client integration.
 */
public final class NearbyTeamHud {
    private boolean enabled = true;
    private double maximumDistance = 64.0D;
    private final List<TeamMemberStatus> nearbyMembers =
            new ArrayList<TeamMemberStatus>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getMaximumDistance() {
        return maximumDistance;
    }

    public void setMaximumDistance(double maximumDistance) {
        if (maximumDistance >= 0.0D) {
            this.maximumDistance = maximumDistance;
        }
    }

    public void updateMembers(List<TeamMemberStatus> members) {
        nearbyMembers.clear();
        if (members == null) {
            return;
        }
        for (TeamMemberStatus member : members) {
            if (member != null && member.getDistance() <= maximumDistance) {
                nearbyMembers.add(member);
            }
        }
    }

    public List<TeamMemberStatus> getNearbyMembers() {
        return Collections.unmodifiableList(
                new ArrayList<TeamMemberStatus>(nearbyMembers));
    }

    public static final class TeamMemberStatus {
        private final String playerName;
        private final float health;
        private final float maximumHealth;
        private final int armor;
        private final double distance;

        public TeamMemberStatus(String playerName, float health,
                float maximumHealth, int armor, double distance) {
            this.playerName = playerName;
            this.health = health;
            this.maximumHealth = maximumHealth;
            this.armor = armor;
            this.distance = distance;
        }

        public String getPlayerName() {
            return playerName;
        }

        public float getHealth() {
            return health;
        }

        public float getMaximumHealth() {
            return maximumHealth;
        }

        public int getArmor() {
            return armor;
        }

        public double getDistance() {
            return distance;
        }
    }
}
