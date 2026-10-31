// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun.eastereggs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.List;

/** Founder command reference showing every Easter Egg ID and its current toggles. */
public final class EasterEggIdListGui extends GuiScreen {
    private final GuiScreen parent;
    private int scrollOffset;
    private int listTop;
    private int listBottom;
    private static final int ROW_HEIGHT = 38;

    public EasterEggIdListGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(1, 8, height - 28, 80, 20, "Back"));
        listTop = 58;
        listBottom = height - 34;
        int visible = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, EasterEggManager.getEggs().size() - visible)));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == 1) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int visible = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
            int maxOffset = Math.max(0, EasterEggManager.getEggs().size() - visible);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + (wheel < 0 ? 1 : -1)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Easter Eggs - ID List", width / 2, 14, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "/alphaclient easteregg <on|off|activate|edit> <id>", width / 2, 30, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Scroll to view all 25 IDs. Toggle status is shown for reference.", width / 2, 43, 0xFFB8BDC8);

        List<EasterEggManager.Egg> eggs = EasterEggManager.getEggs();
        int end = Math.min(eggs.size(), scrollOffset + Math.max(1, (listBottom - listTop) / ROW_HEIGHT));
        int textWidth = Math.max(120, width - 36);
        for (int index = scrollOffset; index < end; index++) {
            EasterEggManager.Egg egg = eggs.get(index);
            int id = egg.getId();
            int y = listTop + (index - scrollOffset) * ROW_HEIGHT;
            String status = "ID " + id + "  |  Master: "
                    + (EasterEggManager.isMasterEnabled(id) ? "ON" : "OFF")
                    + "  |  Personal: " + (EasterEggManager.isUserEnabled(id) ? "ON" : "OFF");
            drawString(fontRendererObj, status, 12, y, 0xFFFFD35A);

            List<String> lines = fontRendererObj.listFormattedStringToWidth(egg.getText(), textWidth);
            int maxLines = 2;
            for (int line = 0; line < lines.size() && line < maxLines; line++) {
                drawString(fontRendererObj, lines.get(line), 24, y + 12 + line * 9, 0xFFFFFFFF);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
