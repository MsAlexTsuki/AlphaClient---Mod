package com.alpharip.staffmod.AlphaCoins.alphacoinsplaytime;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Forge client-tick adapter. Register explicitly; it is not wired into AlphaClient Core. */
public final class AlphaCoinsPlaytimeEventHandler {
    private static final long MAX_TICK_DELTA_MILLIS = 1000L;

    private final AlphaCoinsPlaytime playtime;
    private long lastTickNanos;
    private String activeProfileId;

    public AlphaCoinsPlaytimeEventHandler(AlphaCoinsPlaytime playtime) {
        if (playtime == null) throw new IllegalArgumentException("Playtime service is required.");
        this.playtime = playtime;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null) {
            lastTickNanos = 0L;
            activeProfileId = null;
            return;
        }

        String profileId = minecraft.thePlayer.getUniqueID().toString();
        if (!profileId.equals(activeProfileId) || !playtime.isLoaded()) {
            if (!playtime.activateProfile(profileId)) {
                lastTickNanos = 0L;
                activeProfileId = null;
                return;
            }
            activeProfileId = profileId;
            lastTickNanos = System.nanoTime();
            return;
        }

        long now = System.nanoTime();
        if (lastTickNanos != 0L) {
            long elapsedMillis = (now - lastTickNanos) / 1000000L;
            if (elapsedMillis > 0L) {
                playtime.recordActivePlaytime(Math.min(elapsedMillis, MAX_TICK_DELTA_MILLIS));
            }
        }
        lastTickNanos = now;
    }

    public void resetClock() {
        lastTickNanos = 0L;
    }

    public void save() {
        playtime.save();
    }
}
