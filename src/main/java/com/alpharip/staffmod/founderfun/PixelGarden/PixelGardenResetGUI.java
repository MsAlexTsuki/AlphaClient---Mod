// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.alphaclient.AlphaClient;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import java.io.IOException;

/** Small confirmation popup for token-based Pixel Garden resets. */
public final class PixelGardenResetGUI extends GuiScreen {
    private final GuiScreen parent;
    private int selected = -1;
    private String status = "";
    private boolean busy;

    public PixelGardenResetGUI(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        int left = width / 2 - 118;
        int top = height / 2 - 94;
        buttonList.add(new GuiButton(10, left + 8, top + 47, 220, 20, "Full Reset"));
        buttonList.add(new GuiButton(11, left + 8, top + 70, 220, 20, "Progress Reset"));
        buttonList.add(new GuiButton(12, left + 8, top + 93, 220, 20, "Coins Reset"));
        buttonList.add(new ColorButton(20, left + 8, top + 132, 106, 20, "Yes", 0xFF238B45));
        buttonList.add(new ColorButton(21, left + 122, top + 132, 106, 20, "No", 0xFFB52E35));
        updateButtons();
    }

    private void updateButtons() {
        for (GuiButton button : buttonList) {
            if (button.id >= 10 && button.id <= 12) {
                button.displayString = (button.id - 10 == selected ? "» " : "") +
                    (button.id == 10 ? "Full Reset" : button.id == 11 ? "Progress Reset" : "Coins Reset");
            }
            if (button.id == 20) button.enabled = selected >= 0 && !busy;
            if (button.id == 21) button.enabled = !busy;
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = width / 2 - 118, top = height / 2 - 94;
        drawRect(left, top, left + 236, top + 160, 0xF0202020);
        drawRect(left, top, left + 236, top + 1, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "Reset Garden", width / 2, top + 10, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "Do you want to really reset", width / 2, top + 25, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "your garden?", width / 2, top + 36, 0xFFFFFFFF);
        for (GuiButton button : buttonList) {
            if (button.id == 20) button.enabled = selected >= 0 && !busy;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!status.isEmpty()) drawCenteredString(fontRendererObj, status, width / 2, top + 116, 0xFFFF7777);
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id >= 10 && button.id <= 12) {
            selected = button.id - 10;
            updateButtons();
            return;
        }
        if (button.id == 21) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == 20 && selected >= 0 && !busy) {
            busy = true;
            updateButtons();
            String type = selected == 0 ? "full" : selected == 1 ? "progress" : "coins";
            try {
                SupabaseCommunityService.resetPixelGarden(type);
                com.google.gson.JsonObject save = SupabaseCommunityService.loadPixelGardenSave();
                PixelGardenGame game = PixelGardenGame.getInstance();
                if (save != null) game.loadSaveData(save);
                game.resetTotalHarvested();
                SupabaseCommunityService.savePixelGardenSave(game.toSaveData());
                status = "";
                if (mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(new ChatComponentText(
                            "§eYou have used 1 resettoken, goodluck on your journey back to the top"));
                }
                mc.displayGuiScreen(parent);
            } catch (Exception exception) {
                busy = false;
                status = exception.getMessage() == null ? "Reset failed." : exception.getMessage();
                updateButtons();
            }
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
            PixelGardenResetGUI.this.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, background);
            PixelGardenResetGUI.this.drawCenteredString(fontRendererObj, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, enabled ? 0xFFFFFFFF : 0xFFAAAAAA);
        }
    }
    @Override public void onGuiClosed() { super.onGuiClosed(); }
    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }
}
