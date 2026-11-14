package com.alpharip.staffmod.animalbreedingcooldowntimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/** Renders the remaining cooldown for tracked animals. */
public final class AnimalBreedingHud {
    private final AnimalBreedingCooldownTimer module;

    AnimalBreedingHud(AnimalBreedingCooldownTimer module) {
        this.module = module;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event == null || mc == null || mc.thePlayer == null || mc.theWorld == null) return;
        AnimalBreedingSettings settings = module.getSettings();
        if (!settings.isEnabled() || !settings.isHudEnabled()
                || mc.gameSettings.showDebugInfo) return;

        List<AnimalBreedingEntry> entries = module.getTracker().getAll();
        long now = System.currentTimeMillis();
        FontRenderer font = mc.fontRendererObj;
        int line = 0;
        for (AnimalBreedingEntry entry : entries) {
            int remaining = entry.getRemainingSeconds(now);
            String state = remaining == 0 ? "Ready" : format(remaining);
            font.drawStringWithShadow(entry.getDisplayName() + ": " + state,
                    settings.getX(), settings.getY() + line * 10, 0xFFFFFF);
            line++;
            if (line >= 8) break;
        }
    }

    private String format(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
