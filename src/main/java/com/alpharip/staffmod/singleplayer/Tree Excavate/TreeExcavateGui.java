package com.alpharip.staffmod.singleplayer.treeexcavate;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

/** Compact AlphaClient-styled settings screen for Tree Excavate. */
public final class TreeExcavateGui extends AlphaGuiScreen {
    private final GuiScreen parent;
    private int left;
    private int top;
    private int panelWidth;

    public TreeExcavateGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(300, width - 20);
        left = (width - panelWidth) / 2;
        top = (height - 154) / 2;

        boolean enabled = TreeExcavateSettings.isEnabled();
        buttonList.add(new AlphaButton(1, left + 16, top + 48, panelWidth - 32, 22,
                "Tree Excavate: " + (enabled ? "ON" : "OFF"), enabled, enabled));
        buttonList.add(new AlphaButton(2, left + 16, top + 77, panelWidth - 32, 22,
                "Maximum logs: " + TreeExcavateSettings.getMaxLogs()));
        buttonList.add(new AlphaButton(3, left + 16, top + 112, panelWidth - 32, 20, "DONE"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) {
            TreeExcavateSettings.setEnabled(!TreeExcavateSettings.isEnabled());
        } else if (button.id == 2) {
            int current = TreeExcavateSettings.getMaxLogs();
            TreeExcavateSettings.setMaxLogs(current == 16 ? 32 : current == 32 ? 64 : current == 64 ? 128 : 16);
        } else if (button.id == 3) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + 154, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "TREE EXCAVATE", width / 2, top + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER", width / 2, top + 29, 0xFFFFD866);
        drawString(fontRendererObj, "Excavate key: " + Keyboard.getKeyName(
                com.alpharip.alphaclient.AlphaKeyBindingSettings.getKeyCode("treeExcavate", Keyboard.KEY_GRAVE)),
                left + 16, top + 99, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
