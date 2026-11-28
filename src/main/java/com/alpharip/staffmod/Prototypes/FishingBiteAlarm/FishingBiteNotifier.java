package com.alpharip.staffmod.fishingbitealarm;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.audio.PositionedSoundRecord;

/** Plays a local sound when the detector reports a bite impulse. */
final class FishingBiteNotifier {
    private static final ResourceLocation BITE_SOUND = new ResourceLocation("random.orb");

    private final FishingBiteAlarmSettings settings;
    private long lastAlertAtMillis;

    FishingBiteNotifier(FishingBiteAlarmSettings settings) {
        if (settings == null) throw new IllegalArgumentException("settings");
        this.settings = settings;
    }

    boolean notifyBite() {
        if (!settings.isEnabled()) return false;
        long now = System.currentTimeMillis();
        long intervalMillis = settings.getIntervalSeconds() * 1000L;
        if (lastAlertAtMillis != 0L && now - lastAlertAtMillis < intervalMillis) {
            return false;
        }
        playSound();
        lastAlertAtMillis = now;
        return true;
    }

    void playTestSound() {
        playSound();
    }

    private void playSound() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getSoundHandler() == null) return;
        float volume = settings.getVolume();
        if (volume <= 0.0F) return;
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(BITE_SOUND, volume));
    }
}
