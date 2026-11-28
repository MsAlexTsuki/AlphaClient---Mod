package com.alpharip.staffmod.aimpatternlibrary;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import java.util.List;

/** Standalone Minecraft 1.8.9 preview for browsing and organizing local patterns. */
public final class AimPatternLibraryGui extends GuiScreen {
    private final GuiScreen parent;
    private final AimPatternService service;
    private final AimPatternSettings settings;
    private static final int CREATE = 1, FILTER = 2, DELETE = 3, RESET_FILTER = 4, BACK = 5;
    private List<AimPattern> visible;

    public AimPatternLibraryGui(GuiScreen parent, AimPatternLibrary library) {
        if (library == null) throw new IllegalArgumentException("Library cannot be null");
        this.parent = parent;
        this.service = library.getService();
        this.settings = library.getSettings();
    }
    @Override
    public void initGui() {
        buttonList.clear();
        int x = width / 2 - 110, y = height / 2 + 42;
        buttonList.add(new GuiButton(CREATE, x, y, 106, 20, "CREATE PATTERN"));
        buttonList.add(new GuiButton(FILTER, x + 114, y, 106, 20, "FILTER: " +
                (settings.getFilter() == null ? "ALL" : settings.getFilter().getDisplayName())));
        buttonList.add(new GuiButton(DELETE, x, y + 24, 106, 20, "DELETE SELECTED"));
        buttonList.add(new GuiButton(RESET_FILTER, x + 114, y + 24, 106, 20, "RESET FILTER"));
        buttonList.add(new GuiButton(BACK, x, y + 48, 220, 20, "BACK"));
        visible = service.listVisible();
    }
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == CREATE) {
            int number = service.listVisible().size() + 1;
            service.create("Aim Pattern " + number, AimPatternCategory.GENERAL);
        } else if (button.id == FILTER) {
            AimPatternCategory current = settings.getFilter();
            settings.setFilter(current == null ? AimPatternCategory.values()[0] : current.next());
        } else if (button.id == DELETE) {
            service.delete(settings.getSelectedPatternId());
        } else if (button.id == RESET_FILTER) {
            settings.setFilter(null);
        } else if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "AIM PATTERN LIBRARY", width / 2, height / 2 - 92, 0xFFFFFF);
        visible = service.listVisible();
        int y = height / 2 - 66;
        int limit = Math.min(5, visible.size());
        for (int i = 0; i < limit; i++) {
            AimPattern pattern = visible.get(i);
            boolean selected = pattern.getId().equals(settings.getSelectedPatternId());
            String line = (selected ? "> " : "") + pattern.getName() + " [" +
                    pattern.getCategory().getDisplayName() + "] - " + pattern.getObservationCount() + " samples";
            drawCenteredString(fontRendererObj, line, width / 2, y + i * 15, selected ? 0xFFFFAA : 0xFFFFFF);
        }
        if (visible.isEmpty()) drawCenteredString(fontRendererObj, "No patterns created", width / 2, y, 0xAAAAAA);
        if (settings.isShowStatistics() && settings.getSelectedPatternId() != null) {
            AimPatternStats stats = service.statistics(settings.getSelectedPatternId());
            drawCenteredString(fontRendererObj, "Samples: " + stats.getCount() + "  Mean: " +
                    String.format(java.util.Locale.ROOT, "%.3f", stats.getMean()),
                    width / 2, height / 2 + 20, 0xCCCCCC);
        }
        drawCenteredString(fontRendererObj, "Local prototype data - session only", width / 2,
                height - 18, 0xFFAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
