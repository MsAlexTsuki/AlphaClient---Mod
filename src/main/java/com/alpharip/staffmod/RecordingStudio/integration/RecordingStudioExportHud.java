package com.alpharip.staffmod.RecordingStudio.integration;

import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;
import com.alpharip.staffmod.RecordingStudio.ui.RecordingStudioScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Independent export HUD lifecycle. It remains registered when the Studio screen
 * closes and unregisters itself after the accepted export has finished.
 */
public final class RecordingStudioExportHud {
    private static RecordingStudioExportHud active;
    private final RecordingStudioController controller;

    private RecordingStudioExportHud(RecordingStudioController controller) {
        if (controller == null) throw new IllegalArgumentException("controller");
        this.controller = controller;
    }

    public static synchronized void show(RecordingStudioController controller) {
        if (active != null) MinecraftForge.EVENT_BUS.unregister(active);
        active = new RecordingStudioExportHud(controller);
        MinecraftForge.EVENT_BUS.register(active);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!controller.isExportRunning()) {
            synchronized (RecordingStudioExportHud.class) {
                if (active == this) {
                    MinecraftForge.EVENT_BUS.unregister(this);
                    active = null;
                }
            }
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.fontRendererObj == null
                || minecraft.currentScreen instanceof RecordingStudioScreen) return;

        ScaledResolution resolution = new ScaledResolution(minecraft);
        String heading = "RECORDING STUDIO";
        String progress = "EXPORT " + controller.getExportProgressPercent() + "%";
        int panelWidth = Math.max(minecraft.fontRendererObj.getStringWidth(heading),
                minecraft.fontRendererObj.getStringWidth(progress)) + 16;
        int panelHeight = 30;
        int left = resolution.getScaledWidth() - panelWidth - 8;
        int top = resolution.getScaledHeight() - panelHeight - 8;

        Gui.drawRect(left, top, left + panelWidth, top + panelHeight, 0xD9101010);
        Gui.drawRect(left, top, left + 2, top + panelHeight, 0xFFFFC83D);
        minecraft.fontRendererObj.drawStringWithShadow(heading, left + 8, top + 5, 0xFFFFC83D);
        minecraft.fontRendererObj.drawStringWithShadow(progress, left + 8, top + 17, 0xFFFFFFFF);
    }
}
