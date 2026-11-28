package com.alpharip.staffmod.badges.anniversary.birthday;

import java.time.LocalDate;
import java.util.Random;

/** Detects the configured birthday and emits one burst per calendar day. */
public final class BirthdayCelebrationService {
    private static final int PARTICLE_COUNT = 36;
    private final Random random;
    private LocalDate lastCelebratedDate;

    public BirthdayCelebrationService() {
        this(new Random());
    }

    BirthdayCelebrationService(Random random) {
        if (random == null) throw new IllegalArgumentException("Random source is required.");
        this.random = random;
    }

    /**
     * Call from the client tick integration while a world and player are available.
     * Returns true only when a celebration burst was submitted to the particle sink.
     */
    public boolean tryCelebrate(BirthdayCelebrationSettings settings, LocalDate currentDate,
                                double playerX, double playerY, double playerZ,
                                BirthdayConfettiParticleSink particleSink) {
        if (settings == null || currentDate == null || particleSink == null
                || !settings.isBirthdayToday(currentDate)
                || currentDate.equals(lastCelebratedDate)) {
            return false;
        }

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particleSink.spawn(createParticle(playerX, playerY, playerZ));
        }

        lastCelebratedDate = currentDate;
        return true;
    }

    private BirthdayConfettiParticle createParticle(double x, double y, double z) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double horizontalSpeed = 0.025D + random.nextDouble() * 0.12D;
        double verticalSpeed = 0.035D + random.nextDouble() * 0.13D;
        float red = random.nextFloat();
        float green = random.nextFloat();
        float blue = random.nextFloat();
        int lifetime = 30 + random.nextInt(31);

        return new BirthdayConfettiParticle(
                x + (random.nextDouble() - 0.5D) * 1.6D,
                y + 0.8D + random.nextDouble() * 1.2D,
                z + (random.nextDouble() - 0.5D) * 1.6D,
                Math.cos(angle) * horizontalSpeed,
                verticalSpeed,
                Math.sin(angle) * horizontalSpeed,
                red, green, blue, lifetime);
    }
}
