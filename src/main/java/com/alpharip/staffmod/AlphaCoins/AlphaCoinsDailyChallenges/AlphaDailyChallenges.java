package com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import java.io.File;

/**
 * Standalone Alpha Daily Challenges prototype.
 * Register explicitly from a future integration point; this does not modify AlphaClient Core.
 */
public final class AlphaDailyChallenges {
    private static final AlphaDailyChallenges INSTANCE = new AlphaDailyChallenges();

    private final DailyChallengeManager manager;
    private final DailyChallengeEventHandler eventHandler;
    private boolean registered;

    private AlphaDailyChallenges() {
        Minecraft mc = Minecraft.getMinecraft();
        File gameDirectory = mc == null || mc.mcDataDir == null
                ? new File(".") : mc.mcDataDir;
        File configDirectory = new File(gameDirectory, "config/alphaclient");
        manager = new DailyChallengeManager(configDirectory);
        eventHandler = new DailyChallengeEventHandler(this);
    }

    public static AlphaDailyChallenges getInstance() {
        return INSTANCE;
    }

    public synchronized void register() {
        if (registered) return;
        MinecraftForge.EVENT_BUS.register(eventHandler);
        FMLCommonHandler.instance().bus().register(eventHandler);
        registered = true;
    }

    public synchronized void unregister() {
        if (!registered) return;
        MinecraftForge.EVENT_BUS.unregister(eventHandler);
        FMLCommonHandler.instance().bus().unregister(eventHandler);
        manager.save();
        registered = false;
    }

    public DailyChallengeManager getManager() {
        return manager;
    }

    public void openGui(GuiScreen parent) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;
        if (mc.thePlayer != null) {
            manager.activateProfile(mc.thePlayer.getUniqueID().toString());
        } else {
            manager.activateProfile("offline");
        }
        mc.displayGuiScreen(new DailyChallengesGui(parent, manager));
    }

    public boolean isRegistered() {
        return registered;
    }
}
