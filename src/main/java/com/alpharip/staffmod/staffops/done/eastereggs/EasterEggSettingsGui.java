// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun.eastereggs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.List;

/** Scrollable list of individually configurable Easter Eggs. */
public final class EasterEggSettingsGui extends GuiScreen {
    private final GuiScreen parent;
    private int scrollOffset;
    private int listTop;
    private int listBottom;

    public EasterEggSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(1, 8, height - 30, 80, 20, "Back"));
        listTop = 48;
        listBottom = height - 38;
        int visible = Math.max(1, (listBottom - listTop) / 22);
        List<EasterEggManager.Egg> eggs = EasterEggManager.getEggs();
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, eggs.size() - visible)));
        for (int i = 0; i < visible && i + scrollOffset < eggs.size(); i++) {
            EasterEggManager.Egg egg = eggs.get(i + scrollOffset);
            int id = egg.getId();
            buttonList.add(new GuiButton(100 + id, width / 2 - 150, listTop + i * 22, 300, 20,
                    "Egg " + id + ": " + (EasterEggManager.isUserEnabled(id) ? "ON" : "OFF")));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == 1) Minecraft.getMinecraft().displayGuiScreen(parent);
        else if (button.id >= 101 && button.id <= 125) {
            int id = button.id - 100;
            EasterEggManager.setUserEnabled(id, !EasterEggManager.isUserEnabled(id));
            initGui();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int visible = Math.max(1, (listBottom - listTop) / 22);
            scrollOffset = Math.max(0, Math.min(25 - visible, scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Easter Eggs", width / 2, 14, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Enable individual death-screen messages", width / 2, 29, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
