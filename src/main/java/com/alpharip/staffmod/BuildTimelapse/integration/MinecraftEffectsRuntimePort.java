package com.alpharip.staffmod.BuildTimelapse.integration;

import com.alpharip.staffmod.BuildTimelapse.effects.EffectsRuntimePort;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

/** Client-thread adapter that snapshots and restores the world and HUD values it changes. */
@SideOnly(Side.CLIENT)
public final class MinecraftEffectsRuntimePort implements EffectsRuntimePort {
    private final Minecraft minecraft;
    private World snapshotWorld;
    private long originalWorldTime;
    private boolean worldTimeCaptured;
    private boolean originalRaining;
    private boolean originalThundering;
    private int originalRainTime;
    private int originalThunderTime;
    private float originalRainStrength;
    private float originalThunderStrength;
    private boolean weatherCaptured;
    private boolean originalHideGui;
    private Boolean lastAppliedHideGui;

    public MinecraftEffectsRuntimePort(Minecraft minecraft) {
        if (minecraft == null) throw new IllegalArgumentException("minecraft");
        this.minecraft = minecraft;
    }

    @Override
    public void setWorldTime(long time) {
        requireClientWorld();
        captureWorld();
        if (!worldTimeCaptured) {
            originalWorldTime = snapshotWorld.getWorldTime();
            worldTimeCaptured = true;
        }
        snapshotWorld.setWorldTime(time);
    }

    @Override
    public void setWeather(String weather, float strength) {
        requireClientWorld();
        if (weather == null) throw new IllegalArgumentException("Weather is required");
        if (Float.isNaN(strength) || Float.isInfinite(strength)) {
            throw new IllegalArgumentException("Weather strength must be finite");
        }
        String mode = weather.trim().toLowerCase(Locale.ROOT);
        if (!"clear".equals(mode) && !"rain".equals(mode) && !"thunder".equals(mode)) {
            throw new IllegalArgumentException("Weather must be clear, rain or thunder");
        }
        captureWorld();
        if (!weatherCaptured) {
            WorldInfo info = snapshotWorld.getWorldInfo();
            originalRaining = info.isRaining();
            originalThundering = info.isThundering();
            originalRainTime = info.getRainTime();
            originalThunderTime = info.getThunderTime();
            originalRainStrength = snapshotWorld.getRainStrength(1.0F);
            originalThunderStrength = snapshotWorld.getThunderStrength(1.0F);
            weatherCaptured = true;
        }

        float level = Math.max(0.0F, Math.min(1.0F, strength));
        WorldInfo info = snapshotWorld.getWorldInfo();
        boolean raining = !"clear".equals(mode);
        boolean thundering = "thunder".equals(mode);
        info.setCleanWeatherTime(0);
        info.setRaining(raining);
        info.setThundering(thundering);
        info.setRainTime(raining ? Integer.MAX_VALUE : 0);
        info.setThunderTime(thundering ? Integer.MAX_VALUE : 0);
        snapshotWorld.setRainStrength(raining ? level : 0.0F);
        snapshotWorld.setThunderStrength(thundering ? level : 0.0F);
    }

    @Override
    public void setHudVisible(boolean visible) {
        requireClientThread();
        if (minecraft.gameSettings == null) throw new IllegalStateException("Minecraft settings are unavailable");
        if (lastAppliedHideGui == null) originalHideGui = minecraft.gameSettings.hideGUI;
        minecraft.gameSettings.hideGUI = !visible;
        lastAppliedHideGui = minecraft.gameSettings.hideGUI;
    }

    @Override
    public void restoreOriginalSettings() {
        requireClientThread();
        if (snapshotWorld != null && minecraft.theWorld == snapshotWorld) {
            if (worldTimeCaptured) snapshotWorld.setWorldTime(originalWorldTime);
            if (weatherCaptured) {
                WorldInfo info = snapshotWorld.getWorldInfo();
                info.setRaining(originalRaining);
                info.setThundering(originalThundering);
                info.setRainTime(originalRainTime);
                info.setThunderTime(originalThunderTime);
                snapshotWorld.setRainStrength(originalRainStrength);
                snapshotWorld.setThunderStrength(originalThunderStrength);
            }
        }
        if (lastAppliedHideGui != null && minecraft.gameSettings != null
                && minecraft.gameSettings.hideGUI == lastAppliedHideGui.booleanValue()) {
            minecraft.gameSettings.hideGUI = originalHideGui;
        }
        clearSnapshot();
    }

    private void captureWorld() {
        if (snapshotWorld == minecraft.theWorld) return;
        clearWorldSnapshot();
        snapshotWorld = minecraft.theWorld;
    }

    private void clearWorldSnapshot() {
        snapshotWorld = null;
        worldTimeCaptured = false;
        weatherCaptured = false;
    }

    private void clearSnapshot() {
        clearWorldSnapshot();
        lastAppliedHideGui = null;
    }

    private void requireClientWorld() {
        requireClientThread();
        if (minecraft.theWorld == null || !minecraft.theWorld.isRemote) {
            throw new IllegalStateException("A loaded client world is required for visual effects");
        }
    }

    private void requireClientThread() {
        if (!minecraft.isCallingFromMinecraftThread()) {
            throw new IllegalStateException("Visual effects must run on the Minecraft client thread");
        }
    }
}
