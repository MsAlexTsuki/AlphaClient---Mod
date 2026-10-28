// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.alphaclient.AlphaClient;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
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
        buttonList.add(new GuiButton(20, left + 8, top + 132, 106, 20, "Yes"));
        buttonList.add(new GuiButton(21, left + 122, top + 132, 106, 20, "No"));
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
            if (button.id == 20) {
                button.enabled = selected >= 0 && !busy;
                button.packedFGColour = 0xFF55DD55;
            }
            if (button.id == 21) button.packedFGColour = 0xFFFF5555;
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
                if (save != null) PixelGardenGame.getInstance().loadSaveData(save);
                status = "";
                mc.displayGuiScreen(parent);
            } catch (Exception exception) {
                busy = false;
                status = exception.getMessage() == null ? "Reset failed." : exception.getMessage();
                updateButtons();
            }
        }
    }

    @Override public void onGuiClosed() { super.onGuiClosed(); }
    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }
}
