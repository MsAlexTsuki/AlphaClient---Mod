package com.alpharip.staffmod.quality;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Fullbright toggle for AlphaClient.
 *
 * Raises the game's gamma while enabled and restores the user's previous
 * gamma setting when disabled.
 */
public final class FullbrightMod {
    private static final float FULLBRIGHT_GAMMA = 100.0F;

    private static boolean enabled;
    private static boolean originalGammaCaptured;
    private static float originalGamma;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean shouldEnable) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings == null || enabled == shouldEnable) {
            return;
        }

        if (shouldEnable) {
            originalGamma = minecraft.gameSettings.gammaSetting;
            originalGammaCaptured = true;
            enabled = true;
            minecraft.gameSettings.gammaSetting = FULLBRIGHT_GAMMA;
        } else {
            enabled = false;
            restoreGamma(minecraft);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings == null) {
            return;
        }

        // Keep full brightness active if another client tick resets gamma.
        minecraft.gameSettings.gammaSetting = FULLBRIGHT_GAMMA;
    }

    private static void restoreGamma(Minecraft minecraft) {
        if (originalGammaCaptured && minecraft != null && minecraft.gameSettings != null) {
            minecraft.gameSettings.gammaSetting = originalGamma;
        }
        originalGammaCaptured = false;
    }
}
