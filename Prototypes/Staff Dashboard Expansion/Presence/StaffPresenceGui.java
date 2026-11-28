package com.alpharip.staffdashboardexpansion.presence;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Standalone Minecraft 1.8.9 screen. The host must open this screen explicitly. */
public final class StaffPresenceGui extends GuiScreen {
    private static final int ROW_HEIGHT = 22;
    private final StaffPresenceManager manager;
    private int scrollOffset;
    private String notice = "Presence data is supplied by the caller.";

    public StaffPresenceGui(StaffPresenceManager manager) {
        if (manager == null) throw new IllegalArgumentException("manager cannot be null");
        this.manager = manager;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = width / 2 - 150;
        buttonList.add(new GuiButton(0, left, height -  thirty(), 90, 20, "Filter: " + manager.getFilter().name()));
        buttonList.add(new GuiButton(1, left + 95, height - thirty(), 90, 20, "Sort"));
        buttonList.add(new GuiButton(2, left + 190, height - thirty(), 60, 20, "Refresh"));
        buttonList.add(new GuiButton(3, left + 255, height - thirty(), 45, 20, "Done"));
    }

    private int thirty() { return 30; }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            StaffPresenceManager.StatusFilter[] values = StaffPresenceManager.StatusFilter.values();
            manager.setFilter(values[(manager.getFilter().ordinal() + 1) % values.length]);
            scrollOffset = 0;
            button.displayString = "Filter: " + manager.getFilter().name();
        } else if (button.id == 1) {
            StaffPresenceManager.SortMode[] values = StaffPresenceManager.SortMode.values();
            manager.setSortMode(values[(manager.getSortMode().ordinal() + 1) % values.length]);
            button.displayString = "Sort: " + manager.getSortMode().name().replace("LAST_SEEN_", "");
        } else if (button.id == 2) {
            scrollOffset = 0;
            notice = "View refreshed; no external presence source is connected.";
        } else if (button.id == 3) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Presence", width / 2, 14, 0xFFFFFF);
        long now = System.currentTimeMillis();
        List<StaffPresenceEntry> entries = manager.getVisibleEntries(now);
        int online = manager.count(StaffPresenceManager.StatusFilter.ONLINE);
        int offline = manager.count(StaffPresenceManager.StatusFilter.OFFLINE);
        drawCenteredString(fontRendererObj, "Online: " + online + "   Offline: " + offline
                + "   Total: " + manager.count(StaffPresenceManager.StatusFilter.ALL),
                width / 2, 29, 0xAAAAAA);

        int x = width / 2 - 150;
        int y = 49;
        drawString(fontRendererObj, "STAFF MEMBER", x + 5, y, 0xCCCCCC);
        drawString(fontRendererObj, "STATUS", x + 145, y, 0xCCCCCC);
        drawString(fontRendererObj, "LAST SEEN", x + 205, y, 0xCCCCCC);
        drawHorizontalLine(x, x + 300, y + 12, 0xFF777777);

        int visibleRows = Math.max(1, (height - 112) / ROW_HEIGHT);
        int maxOffset = Math.max(0, entries.size() - visibleRows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
        for (int row = 0; row < visibleRows && row + scrollOffset < entries.size(); row++) {
            StaffPresenceEntry entry = entries.get(row + scrollOffset);
            int rowY = y + 17 + row * ROW_HEIGHT;
            boolean stale = manager.isStale(entry, now);
            int color = stale ? 0xFFFFAA : 0xFFFFFF;
            drawString(fontRendererObj, trim(entry.getStaffName(), 20), x + 5, rowY, color);
            drawString(fontRendererObj, trim(entry.getStatus(), 9), x + 145, rowY,
                    "Online".equalsIgnoreCase(entry.getStatus()) ? 0x55FF55 :
                    "Offline".equalsIgnoreCase(entry.getStatus()) ? 0xFF7777 : 0xAAAAAA);
            drawString(fontRendererObj, formatAge(entry.getAgeMillis(now)), x + 205, rowY, color);
            if (stale) drawString(fontRendererObj, "STALE", x + 265, rowY, 0xFFAA00);
        }
        if (entries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No presence observations match this filter.", width / 2, y + 45, 0xAAAAAA);
        }
        drawCenteredString(fontRendererObj, notice, width / 2, height - 43, 0x888888);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, Math.max(1, max - 3)) + "...";
    }

    private String formatAge(long millis) {
        long seconds = millis / 1000L;
        if (seconds < 60L) return seconds + "s ago";
        long minutes = seconds / 60L;
        if (minutes < 60L) return minutes + "m ago";
        long hours = minutes / 60L;
        if (hours < 24L) return hours + "h ago";
        return (hours / 24L) + "d ago";
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) scrollOffset = Math.max(0, scrollOffset + (wheel > 0 ? -2 : 2));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(null);
        else super.keyTyped(typedChar, keyCode);
    }

    // TODO: Connect this screen to the Staff Dashboard only after explicit integration approval.
    // TODO: Populate the manager from an approved client-visible presence source.
}
