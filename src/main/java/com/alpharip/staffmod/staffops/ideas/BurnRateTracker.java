package com.alpharip.staffmod.staffops.ideas;
/** KitMap resource counters; automatic item detection and fight lifecycle hooks are pending. */
public final class BurnRateTracker {
    private int gaps, potions, pearls, kills;
    public void resetLife() { gaps = potions = pearls = kills = 0; }
    public void recordGap() { gaps++; }
    public void recordPotion() { potions++; }
    public void recordPearl() { pearls++; }
    public void recordKill() { kills++; }
    public int getGaps() { return gaps; }
    public int getPotions() { return potions; }
    public int getPearls() { return pearls; }
    public int getKills() { return kills; }
    public double getItemsPerKill() { return kills == 0 ? 0D : (double)(gaps + potions + pearls) / kills; }
}
