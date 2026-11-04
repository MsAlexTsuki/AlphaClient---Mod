// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.singleplayer.worldedit.WorldEditGui;
import com.alpharip.staffmod.singleplayer.autotool.AutoToolGui;
import com.alpharip.staffmod.singleplayer.autotool.AutoToolSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public final class SingleplayerGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int WORLD_EDIT = 2;
    private static final int AUTO_TOOL = 3;
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private int panelLeft, panelRight, panelTop, panelBottom;

    public SingleplayerGui(GuiScreen parent) {
        this.parent = parent;
    }

    private boolean hasActiveSingleplayerWorld() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft != null && minecraft.isSingleplayer()
                && minecraft.thePlayer != null && minecraft.theWorld != null
                && minecraft.getIntegratedServer() != null;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!hasActiveSingleplayerWorld()) {
            if (mc != null) mc.displayGuiScreen(parent);
            return;
        }
        panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        panelRight = Math.min(width - 12, panelLeft + 460);
        panelTop = Math.max(12, height / 2 - Math.min(150, height / 2 - 12));
        panelBottom = Math.min(height - 12, panelTop + 300);

        buttonList.add(new AlphaButton(WORLD_EDIT, panelLeft + 14, panelTop + 78,
                panelRight - panelLeft - 28, 42, "World Edit", true));
        buttonList.add(new AlphaButton(AUTO_TOOL, panelLeft + 14, panelTop + 128,
                panelRight - panelLeft - 28, 42,
                "AutoTool: " + (AutoToolSettings.isEnabled() ? "ON" : "OFF"), AutoToolSettings.isEnabled()));
        buttonList.add(new AlphaButton(BACK, panelLeft + 10, panelBottom - 29, 90, 20, "BACK"));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!hasActiveSingleplayerWorld() && mc != null) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == WORLD_EDIT) {
            if (hasActiveSingleplayerWorld()) {
                mc.displayGuiScreen(new WorldEditGui(this));
            }
        } else if (button.id == AUTO_TOOL) {
            if (hasActiveSingleplayerWorld()) {
                mc.displayGuiScreen(new AutoToolGui(this));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER", width / 2,
                panelTop + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER MODS", width / 2,
                panelTop + 30, ACCENT);
        drawString(fontRendererObj, "Singleplayer-only tools", panelLeft + 12,
                panelTop + 52, 0xFFB8BDC8);
        drawString(fontRendererObj, "WorldEdit", panelLeft + 25, panelTop + 92, 0xFFF3F4F6);
        drawString(fontRendererObj, "Select regions and edit blocks in your local world.",
                panelLeft + 25, panelTop + 106, 0xFFB8BDC8);
        drawString(fontRendererObj, "AutoTool", panelLeft + 25, panelTop + 142, 0xFFF3F4F6);
        drawString(fontRendererObj, "Automatically selects a suitable hotbar tool for the targeted block.",
                panelLeft + 25, panelTop + 156, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
