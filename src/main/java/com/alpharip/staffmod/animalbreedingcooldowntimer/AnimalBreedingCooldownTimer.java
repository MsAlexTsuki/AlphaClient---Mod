package com.alpharip.staffmod.animalbreedingcooldowntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Standalone Animal Breeding Cooldown Timer prototype.
 * This class is not registered in the live AlphaClient module/menu system.
 */
public final class AnimalBreedingCooldownTimer {
    private final AnimalBreedingSettings settings = new AnimalBreedingSettings();
    private final AnimalBreedingTracker tracker = new AnimalBreedingTracker();
    private final AnimalBreedingStorage storage;
    private final AnimalBreedingEventHandler eventHandler;
    private final AnimalBreedingHud hud;

    public AnimalBreedingCooldownTimer(File configDirectory) {
        storage = new AnimalBreedingStorage(configDirectory);
        eventHandler = new AnimalBreedingEventHandler(this);
        hud = new AnimalBreedingHud(this);
        try {
            storage.load(settings, tracker);
        } catch (IOException exception) {
            // Keep safe in-memory defaults when the local settings file cannot be read.
        }
        MinecraftForge.EVENT_BUS.register(hud);
    }

    public AnimalBreedingSettings getSettings() { return settings; }
    public AnimalBreedingTracker getTracker() { return tracker; }
    public AnimalBreedingEventHandler getEventHandler() { return eventHandler; }
    public AnimalBreedingHud getHud() { return hud; }

    /** Record a parent only after a verified successful breeding callback. */
    public void recordSuccessfulBreeding(EntityAnimal animal) {
        if (animal == null || animal.getUniqueID() == null) return;
        UUID id = animal.getUniqueID();
        String name = animal.hasCustomName() ? animal.getCustomNameTag()
                : animal.getName();
        tracker.record(id, name, System.currentTimeMillis(), settings.getIntervalSeconds());
        save();
    }

    public void openGui(GuiScreen parent) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) mc.displayGuiScreen(new AnimalBreedingGui(parent, this));
    }

    public void save() {
        try {
            storage.save(settings, tracker.getAll());
        } catch (IOException exception) {
            // TODO: Expose a visible save error state instead of silently keeping memory-only changes.
        }
    }
}
