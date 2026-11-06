package com.alpharip.staffmod.badges.playtime;

import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import java.util.List;

/**
 * Connects playtime badges to the existing cumulative Minecraft Playtime Tracker.
 * The tracker counts process runtime independently of its HUD visibility setting.
 */
public final class PlaytimeBadgeService {
    private PlaytimeBadgeService() { }

    /** Reads the existing persisted total plus the current Minecraft session. */
    public static long getTotalPlaytimeSeconds() {
        return PlaytimeTrackerHud.getTotalPlaytimeSeconds();
    }

    /** Returns earned and locked badge states using the live cumulative total. */
    public static List<PlaytimeBadgeStatus> getCurrentBadges() {
        return PlaytimeBadgeCalculator.calculate(getTotalPlaytimeSeconds());
    }
}