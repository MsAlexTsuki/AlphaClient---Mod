package com.alpharip.staffmod.trainers.potionthrowcoach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local, in-memory splash-potion analysis prototype.
 *
 * A future client-side Forge adapter must forward damage, thrown-potion and
 * landing events with their timestamps and world coordinates. This class does
 * not hook Minecraft events, alter potion behaviour, or provide gameplay aid.
 */
public final class PotionThrowCoach {
    public static final String NAME = "Potion Throw Coach";
    public static final String CATEGORY = "Practice";
    public static final String STATUS = "PROTOTYPE";
    public static final String REQUIRED_ACCESS = "Elite+ (analysis only; no gameplay assistance).";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;
    // TODO(Supabase): keep this prototype local; document schema and policies before any future integration.

    private final PotionThrowCoachConfig config;
    private final List<PotionThrowResult> history = new ArrayList<PotionThrowResult>();
    private long nextThrowId = 1L;
    private long lastDamageTimestamp = -1L;
    private PendingThrow pendingThrow;

    public PotionThrowCoach() {
        this(new PotionThrowCoachConfig());
    }

    public PotionThrowCoach(PotionThrowCoachConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.config = config;
    }

    /** Records the time at which the local player took damage. */
    public synchronized void recordDamage(long timestampMillis) {
        requireNonNegative(timestampMillis, "timestampMillis");
        lastDamageTimestamp = timestampMillis;
    }

    /**
     * Starts tracking a thrown splash potion. The adapter supplies the potion
     * spawn/origin position and event timestamp.
     */
    public synchronized long recordPotionThrown(double x, double y, double z,
                                                long timestampMillis) {
        requireNonNegative(timestampMillis, "timestampMillis");
        if (pendingThrow != null) {
            throw new IllegalStateException("A potion throw is already awaiting its landing.");
        }
        if (lastDamageTimestamp > timestampMillis) {
            throw new IllegalArgumentException("Throw timestamp cannot precede the damage timestamp.");
        }

        long reactionTime = lastDamageTimestamp < 0L
                ? -1L : timestampMillis - lastDamageTimestamp;
        pendingThrow = new PendingThrow(nextThrowId++,
                new PotionThrowResult.Position(x, y, z),
                timestampMillis, reactionTime);
        lastDamageTimestamp = -1L;
        return pendingThrow.id;
    }

    /**
     * Completes the attempt using the actual potion landing position and the
     * player's feet position at that moment. Distances are horizontal X/Z.
     */
    public synchronized PotionThrowResult recordPotionLanded(
            double landingX, double landingY, double landingZ,
            double playerX, double playerY, double playerZ,
            long timestampMillis) {
        requireNonNegative(timestampMillis, "timestampMillis");
        if (pendingThrow == null) {
            throw new IllegalStateException("There is no potion throw awaiting a landing.");
        }
        if (timestampMillis < pendingThrow.throwTimestampMillis) {
            throw new IllegalArgumentException("Landing timestamp cannot precede the throw timestamp.");
        }

        PotionThrowResult.Position landing =
                new PotionThrowResult.Position(landingX, landingY, landingZ);
        PotionThrowResult.Position playerFeet =
                new PotionThrowResult.Position(playerX, playerY, playerZ);
        double offset = landing.horizontalDistanceTo(playerFeet);
        boolean accurate = offset <= config.getAccurateLandingRadius();

        PotionThrowResult result = new PotionThrowResult(
                pendingThrow.id,
                pendingThrow.reactionTimeMillis,
                timestampMillis - pendingThrow.throwTimestampMillis,
                offset,
                accurate,
                pendingThrow.origin,
                landing,
                playerFeet
        );
        history.add(result);
        trimHistory();
        pendingThrow = null;
        return result;
    }

    /** Cancels an unfinished throw without adding it to the statistics. */
    public synchronized boolean cancelPendingThrow() {
        if (pendingThrow == null) return false;
        pendingThrow = null;
        return true;
    }

    public synchronized boolean hasPendingThrow() { return pendingThrow != null; }

    public synchronized List<PotionThrowResult> getHistory() {
        return Collections.unmodifiableList(
                new ArrayList<PotionThrowResult>(history));
    }

    public synchronized PotionThrowCoachStats getStats() {
        return new PotionThrowCoachStats(history);
    }

    public PotionThrowCoachConfig getConfig() { return config; }

    public synchronized void reset() {
        history.clear();
        pendingThrow = null;
        lastDamageTimestamp = -1L;
        nextThrowId = 1L;
    }

    private void trimHistory() {
        while (history.size() > config.getMaximumHistoryEntries()) {
            history.remove(0);
        }
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0L) throw new IllegalArgumentException(name + " must be >= 0.");
    }

    private static final class PendingThrow {
        private final long id;
        private final PotionThrowResult.Position origin;
        private final long throwTimestampMillis;
        private final long reactionTimeMillis;

        private PendingThrow(long id, PotionThrowResult.Position origin,
                             long throwTimestampMillis, long reactionTimeMillis) {
            this.id = id;
            this.origin = origin;
            this.throwTimestampMillis = throwTimestampMillis;
            this.reactionTimeMillis = reactionTimeMillis;
        }
    }
}
