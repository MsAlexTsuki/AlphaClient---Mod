package com.alpharip.staffmod.staffops.ideas;

/** Stores cooldown timing state; callers supply the current time. */
public class DetectionCooldown {
    private long cooldownMillis;
    private long lastTriggeredAt = Long.MIN_VALUE;

    public DetectionCooldown(long cooldownMillis) {
        setCooldownMillis(cooldownMillis);
    }

    public void setCooldownMillis(long cooldownMillis) {
        this.cooldownMillis = Math.max(0L, cooldownMillis);
    }

    public long getCooldownMillis() {
        return cooldownMillis;
    }

    public boolean isReady(long nowMillis) {
        return lastTriggeredAt == Long.MIN_VALUE
                || nowMillis - lastTriggeredAt >= cooldownMillis;
    }

    public boolean tryTrigger(long nowMillis) {
        if (!isReady(nowMillis)) {
            return false;
        }
        lastTriggeredAt = nowMillis;
        return true;
    }

    public void reset() {
        lastTriggeredAt = Long.MIN_VALUE;
    }
}
