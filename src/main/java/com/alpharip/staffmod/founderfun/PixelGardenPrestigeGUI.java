// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Yes/No confirmation for the immediate full Pixel Garden Prestige reset. */
public final class PixelGardenPrestigeGUI extends GuiScreen {
    private final GuiScreen parent;
    private String status = "";
    private boolean busy;

    public PixelGardenPrestigeGUI(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        int left = width / 2 - 120, top = height / 2 - 48;
        buttonList.add(new ColorButton(1, left + 8, top + 68, 104, 20, "Yes", 0xFF238B45));
        buttonList.add(new ColorButton(2, left + 128, top + 68, 104, 20, "No", 0xFFB52E35));
        updateButtons();
    }

    private void updateButtons() {
        for (GuiButton button : buttonList) button.enabled = !busy;
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = width / 2 - 120, top = height / 2 - 48;
        drawRect(left, top, left + 240, top + 112, 0xF0202020);
        drawRect(left, top, left + 240, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "PRESTIGE", width / 2, top + 12, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "Spend 20,000 coins for a full reset?", width / 2, top + 31, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "All garden progress will be reset immediately.", width / 2, top + 45, 0xFFB7C4D5);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!status.isEmpty()) drawCenteredString(fontRendererObj, status, width / 2, top + 92, 0xFFFF7777);
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 2 && !busy) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id != 1 || busy) return;
        busy = true;
        updateButtons();
        try {
            SupabaseCommunityService.prestigePixelGarden();
            com.google.gson.JsonObject save = SupabaseCommunityService.loadPixelGardenSave();
            if (save == null) throw new IllegalStateException("Prestige succeeded, but the reset save could not be loaded.");
            PixelGardenGame.getInstance().loadSaveData(save);
            mc.displayGuiScreen(parent);
        } catch (Exception exception) {
            busy = false;
            status = exception.getMessage() == null ? "Prestige failed." : exception.getMessage();
            updateButtons();
        }
    }

    private final class ColorButton extends GuiButton {
        private final int fill;
        private ColorButton(int id, int x, int y, int width, int height, String label, int fill) {
            super(id, x, y, width, height, label);
            this.fill = fill;
        }
        @Override public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (!visible) return;
            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            int background = enabled ? fill : 0xFF555555;
            PixelGardenPrestigeGUI.this.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, background);
            PixelGardenPrestigeGUI.this.drawCenteredString(fontRendererObj, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0xFFFFFFFF);
        }
    }

    @Override public boolean doesGuiPauseGame() { return false; }
    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && !busy) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }
}
