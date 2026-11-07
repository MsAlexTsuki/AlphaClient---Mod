package com.alpharip.staffmod.singleplayer.alphahomes;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

/** Compact home list using the existing AlphaClient dark and gold menu style. */
public final class AlphaHomesGui extends AlphaGuiScreen {
    private static final int BACK = 90;
    private static final int PREVIOUS = 91;
    private static final int NEXT = 92;
    private static final int FIRST_HOME_BUTTON = 100;
    private static final int PAGE_SIZE = 6;

    private final GuiScreen parent;
    private int page;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public AlphaHomesGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!AlphaHomesManager.isAvailable(mc)) {
            mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(420, width - 24);
        panelHeight = Math.min(310, height - 24);
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;

        List<AlphaHome> homes = AlphaHomesManager.getInstance().getHomes();
        int pageCount = Math.max(1, (homes.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pageCount - 1));

        int start = page * PAGE_SIZE;
        int end = Math.min(homes.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            AlphaHome home = homes.get(index);
            int row = index - start;
            String label = "TELEPORT  •  " + truncate(home.getName(), 29);
            buttonList.add(new AlphaButton(FIRST_HOME_BUTTON + row,
                    panelLeft + 18, panelTop + 67 + row * 29,
                    panelWidth - 36, 24, label));
        }

        int footerY = panelTop + panelHeight - 30;
        buttonList.add(new AlphaButton(BACK, panelLeft + 18, footerY, 76, 20, "BACK"));
        if (page > 0) buttonList.add(new AlphaButton(PREVIOUS, panelLeft + panelWidth - 160,
                footerY, 64, 20, "PREVIOUS"));
        if (page + 1 < pageCount) buttonList.add(new AlphaButton(NEXT, panelLeft + panelWidth - 90,
                footerY, 72, 20, "NEXT"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (!AlphaHomesManager.isAvailable(mc)) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == PREVIOUS) {
            page = Math.max(0, page - 1);
            initGui();
            return;
        }
        if (button.id == NEXT) {
            page++;
            initGui();
            return;
        }
        if (button.id >= FIRST_HOME_BUTTON && button.id < FIRST_HOME_BUTTON + PAGE_SIZE) {
            int index = page * PAGE_SIZE + button.id - FIRST_HOME_BUTTON;
            List<AlphaHome> homes = AlphaHomesManager.getInstance().getHomes();
            if (index >= 0 && index < homes.size()
                    && AlphaHomesManager.getInstance().teleportTo(homes.get(index).getName())) {
                mc.displayGuiScreen(null);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!AlphaHomesManager.isAvailable(mc)) {
            mc.displayGuiScreen(parent);
            return;
        }

        List<AlphaHome> homes = AlphaHomesManager.getInstance().getHomes();
        int pageCount = Math.max(1, (homes.size() + PAGE_SIZE - 1) / PAGE_SIZE);

        drawRect(0, 0, width, height, 0x99000000);
        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xF00B0D12);
        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "ALPHAHOMES", width / 2, panelTop + 14, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER HOMES", width / 2, panelTop + 32, 0xFFFFD866);
        drawString(fontRendererObj, homes.size() + " saved home(s)", panelLeft + 18,
                panelTop + 49, 0xFFB8BDC8);

        if (homes.isEmpty()) {
            drawCenteredString(fontRendererObj, "No homes saved in this world yet.",
                    width / 2, panelTop + 105, 0xFFB8BDC8);
            drawCenteredString(fontRendererObj, "Use /alphahome sethome [name] to create one.",
                    width / 2, panelTop + 122, 0xFFB8BDC8);
        } else {
            drawString(fontRendererObj, "Page " + (page + 1) + " / " + pageCount,
                    panelLeft + 18, panelTop + panelHeight - 17, 0xFFB8BDC8);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) return "Home";
        if (value.length() <= maxLength) return value;
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
