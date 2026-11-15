package com.alpharip.staffmod.AlphaCoins.alphacoinsplaytime;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;

/**
 * Explicit lifecycle wrapper for the playtime reward prototype.
 * Call register() from a separately approved integration point.
 */
public final class AlphaCoinsPlaytimeModule {
    private static final AlphaCoinsPlaytimeModule INSTANCE = new AlphaCoinsPlaytimeModule();

    private final AlphaCoinsPlaytime playtime;
    private final AlphaCoinsPlaytimeEventHandler eventHandler;
    private boolean registered;

    private AlphaCoinsPlaytimeModule() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File gameDirectory = minecraft == null || minecraft.mcDataDir == null
                ? new File(".") : minecraft.mcDataDir;
        File configDirectory = new File(gameDirectory, "config/alphaclient");
        playtime = new AlphaCoinsPlaytime(configDirectory);
        eventHandler = new AlphaCoinsPlaytimeEventHandler(playtime);
    }

    public static AlphaCoinsPlaytimeModule getInstance() { return INSTANCE; }

    public synchronized void register() {
        if (registered) return;
        FMLCommonHandler.instance().bus().register(eventHandler);
        registered = true;
    }

    public synchronized void unregister() {
        if (!registered) return;
        FMLCommonHandler.instance().bus().unregister(eventHandler);
        eventHandler.save();
        eventHandler.resetClock();
        registered = false;
    }

    public AlphaCoinsPlaytime getPlaytime() { return playtime; }
    public synchronized boolean isRegistered() { return registered; }
}
