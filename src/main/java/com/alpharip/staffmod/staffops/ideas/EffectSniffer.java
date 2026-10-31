package com.alpharip.staffmod.staffops.ideas;
/** Potion-particle observation placeholder. No unverified particle-color mapping is assumed. */
public final class EffectSniffer {
    public enum Observation { UNKNOWN, POSSIBLE_SPEED, POSSIBLE_STRENGTH }
    public Observation classifyParticle(int red, int green, int blue) {
        // Detection rules require verified Minecraft particle data.
        return Observation.UNKNOWN;
    }
}
