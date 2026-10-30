// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun.eastereggs;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

/** Compact two-column Easter Egg controls using the shared AlphaClient GUI style. */
public final class EasterEggSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int DEATHSCREENS = 2;
    private static final int FIRST_EGG_BUTTON = 100;
    private static final int ROW_HEIGHT = 24;

    private final GuiScreen parent;
    private boolean deathscreensExpanded;
    private int scrollOffset;
    private int panelLeft;
    private int panelRight;
    private int panelTop;
    private int panelBottom;
    private int firstEggY;
    private int visibleRows;

    public EasterEggSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelLeft = Math.max(12, width / 2 - Math.min(190, width / 2 - 12));
        panelRight = Math.min(width - 12, panelLeft + 380);
        panelTop = Math.max(12, height / 2 - Math.min(170, height / 2 - 12));
        panelBottom = Math.min(height - 12, panelTop + 340);

        buttonList.add(new AlphaButton(BACK, panelLeft + 12, panelBottom - 30, 86, 20, "BACK"));
        buttonList.add(new AlphaButton(DEATHSCREENS, panelLeft + 12, panelTop + 58,
                panelRight - panelLeft - 24, 26, "Deathscreens",
                true, deathscreensExpanded));

        firstEggY = panelTop + 94;
        int gridBottom = panelBottom - 42;
        visibleRows = Math.max(1, (gridBottom - firstEggY) / ROW_HEIGHT);
        int totalRows = (EasterEggManager.getEggs().size() + 1) / 2;
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, totalRows - visibleRows)));

        if (deathscreensExpanded) {
            List<EasterEggManager.Egg> eggs = EasterEggManager.getEggs();
            int start = scrollOffset * 2;
            int end = Math.min(eggs.size(), start + visibleRows * 2);
            int gap = 8;
            int columnWidth = (panelRight - panelLeft - 32 - gap) / 2;
            for (int index = start; index < end; index++) {
                EasterEggManager.Egg egg = eggs.get(index);
                int local = index - start;
                int column = local % 2;
                int row = local / 2;
                int x = panelLeft + 12 + column * (columnWidth + gap);
                int y = firstEggY + row * ROW_HEIGHT;
                int id = egg.getId();
                boolean enabled = EasterEggManager.isUserEnabled(id);
                buttonList.add(new AlphaButton(FIRST_EGG_BUTTON + id, x, y,
                        columnWidth, 20, "ID: " + id + "  " + (enabled ? "ON" : "OFF"),
                        false, enabled));
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == DEATHSCREENS) {
            deathscreensExpanded = !deathscreensExpanded;
            initGui();
        } else if (button.id > FIRST_EGG_BUTTON && button.id <= FIRST_EGG_BUTTON + 25) {
            int id = button.id - FIRST_EGG_BUTTON;
            EasterEggManager.setUserEnabled(id, !EasterEggManager.isUserEnabled(id));
            initGui();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (!deathscreensExpanded) return;
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int totalRows = (EasterEggManager.getEggs().size() + 1) / 2;
            int maxScroll = Math.max(0, totalRows - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "EASTER EGGS", width / 2, panelTop + 15, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "PERSONAL DEATHSCREEN SETTINGS", width / 2,
                panelTop + 33, 0xFFFFD866);
        drawString(fontRendererObj, "Choose which messages can appear on your death screen.",
                panelLeft + 12, panelTop + 44, 0xFFB8BDC8);
        if (deathscreensExpanded && EasterEggManager.getEggs().size() > visibleRows * 2) {
            drawString(fontRendererObj, "Scroll to view all 25 IDs",
                    panelLeft + 112, panelBottom - 24, 0xFF8E95A3);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
