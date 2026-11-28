package com.alpharip.staffmod.fishingbitealarm;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;

/** Standalone Fishing Bite Alarm prototype; not registered by AlphaClient Core. */
public final class FishingBiteAlarm {
    public static final String NAME = "Fishing Bite Alarm";
    public static final boolean REQUIRES_SUPABASE = false;

    private final FishingBiteAlarmSettings settings;
    private final FishingBiteDetector detector;
    private final FishingBiteNotifier notifier;
    private final FishingBiteAlarmStorage storage;
    private final FishingBiteAlarmEventHandler eventHandler;
    private final File settingsFile;
    private boolean running;

    public FishingBiteAlarm() {
        this.settings = new FishingBiteAlarmSettings();
        this.detector = new FishingBiteDetector();
        this.notifier = new FishingBiteNotifier(settings);
        this.storage = new FishingBiteAlarmStorage();
        this.eventHandler = new FishingBiteAlarmEventHandler(this);
        File root = Minecraft.getMinecraft().mcDataDir == null
                ? new File(".") : Minecraft.getMinecraft().mcDataDir;
        this.settingsFile = new File(root, "config/alphaclient/fishing_bite_alarm.properties");
        storage.load(settingsFile, settings);
    }

    /** Register the client-tick adapter explicitly; this prototype does not auto-start. */
    public void start() {
        if (running) return;
        MinecraftForge.EVENT_BUS.register(eventHandler);
        running = true;
    }

    public void stop() {
        if (!running) return;
        MinecraftForge.EVENT_BUS.unregister(eventHandler);
        detector.reset();
        running = false;
        save();
    }

    public boolean isRunning() { return running; }
    public FishingBiteAlarmSettings getSettings() { return settings; }

    public void openSettings(GuiScreen parent) {
        Minecraft.getMinecraft().displayGuiScreen(new FishingBiteAlarmGui(this, parent));
    }

    void onHookSample(FishingHookSample sample) {
        if (!settings.isEnabled()) {
            detector.reset();
            return;
        }
        if (detector.update(sample)) {
            notifier.notifyBite();
        }
    }

    void onNoActiveHook() {
        detector.reset();
    }

    public void testAlarm() {
        notifier.playTestSound();
    }

    public void save() {
        storage.save(settingsFile, settings);
    }
}
