package com.alpharip.staffmod.badges.anniversary.birthday;

/**
 * Adapter boundary for the Minecraft 1.8.9 particle renderer.
 * The client integration must implement this interface and submit particles
 * to Minecraft's effect renderer.
 */
public interface BirthdayConfettiParticleSink {
    void spawn(BirthdayConfettiParticle particle);
}
