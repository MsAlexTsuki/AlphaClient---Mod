package com.alpharip.staffmod.store;

import com.alpharip.staffmod.store.addons.AddonCategory;
import com.alpharip.staffmod.store.addons.AddonEntry;
import com.alpharip.staffmod.store.addons.AddonInstallState;
import com.alpharip.staffmod.store.addons.AddonStore;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.io.IOException;
import java.util.List;

/**
 * Full Minecraft 1.8.9 Store screen for the locally registered Addons catalogue.
 * State actions are local metadata flags only; no download or external code execution occurs.
 */
public class StoreGui extends GuiScreen {
    private static final int SEARCH_ID = 1;
    private static final int CATEGORY_BASE = 10;
    private static final int INSTALLED_ID = 30;
    private static final int INSTALL_ID = 40;
    private static final int ENABLE_ID = 41;
    private static final int DISABLE_ID = 42;
    private static final int REMOVE_ID = 43;
    private static final int BACK_ID = 50;

    private final GuiScreen parent;
    private final Store store;
    private GuiTextField searchField;
    private int selectedIndex = -1;
    private int scrollOffset;
    private String message = "";
    private int listLeft, listRight, listTop, listBottom;

    public StoreGui(GuiScreen parent, Store store) {
        if (store == null) throw new IllegalArgumentException("Store is required.");
        this.parent = parent;
        this.store = store;
    }

    @Override public void initGui() {
        buttonList.clear();
        int left = Math.max(12, width / 2 - 220);
        int right = Math.min(width - 12, width / 2 + 220);
        searchField = new GuiTextField(SEARCH_ID, fontRendererObj, left, 39, Math.max(100, right - left - 86), 18);
        searchField.setMaxStringLength(80);
        buttonList.add(new GuiButton(INSTALLED_ID, right - 80, 38, 80, 20,
                store.getAddonStore().getSettings().isShowInstalledOnly() ? "All items" : "Installed"));
        AddonCategory[] categories = AddonCategory.values();
        int categoryWidth = Math.max(48, (right - left) / categories.length);
        for (int i = 0; i < categories.length; i++) {
            buttonList.add(new GuiButton(CATEGORY_BASE + i, left + i * categoryWidth, 62,
                    categoryWidth - 2, 20, categories[i].getDisplayName()));
        }
        int detailLeft = Math.max(width / 2 + 35, right - 155);
        buttonList.add(new GuiButton(INSTALL_ID, detailLeft, height - 54, 72, 20, "Install"));
        buttonList.add(new GuiButton(ENABLE_ID, detailLeft + 76, height - 54, 72, 20, "Enable"));
        buttonList.add(new GuiButton(DISABLE_ID, detailLeft, height - 30, 72, 20, "Disable"));
        buttonList.add(new GuiButton(REMOVE_ID, detailLeft + 76, height - 30, 72, 20, "Remove"));
        buttonList.add(new GuiButton(BACK_ID, 12, height - 30, 72, 20, "Back"));
        listLeft = left;
        listRight = Math.max(left + 110, width / 2 + 18);
        listTop = 91;
        listBottom = height - 66;
    }

    @Override public void updateScreen() { if (searchField != null) searchField.updateCursorCounter(); }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField != null && searchField.textboxKeyTyped(typedChar, keyCode)) {
            store.getAddonStore().getSettings().setSearchQuery(searchField.getText());
            selectedIndex = -1;
            scrollOffset = 0;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (searchField != null) searchField.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && mouseX >= listLeft && mouseX < listRight
                && mouseY >= listTop && mouseY < listBottom) {
            int row = (mouseY - listTop) / 38;
            int index = scrollOffset + row;
            selectedIndex = index < store.getAddonStore().getVisibleAddons().size() ? index : -1;
        }
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int visibleRows = Math.max(1, (listBottom - listTop) / 38);
            int max = Math.max(0, store.getAddonStore().getVisibleAddons().size() - visibleRows);
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel > 0 ? -1 : 1)));
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        AddonStore addons = store.getAddonStore();
        if (button.id >= CATEGORY_BASE && button.id < CATEGORY_BASE + AddonCategory.values().length) {
            addons.getSettings().setSelectedCategory(AddonCategory.values()[button.id - CATEGORY_BASE]);
            selectedIndex = -1;
            scrollOffset = 0;
        } else if (button.id == INSTALLED_ID) {
            addons.getSettings().setShowInstalledOnly(!addons.getSettings().isShowInstalledOnly());
            selectedIndex = -1;
            scrollOffset = 0;
            initGui();
        } else if (button.id == BACK_ID) {
            mc.displayGuiScreen(parent);
        } else {
            AddonEntry entry = getSelectedEntry();
            if (entry == null) {
                message = "Select an add-on first.";
                return;
            }
            boolean changed = false;
            if (button.id == INSTALL_ID) changed = addons.markInstalled(entry.getId());
            else if (button.id == ENABLE_ID) changed = addons.enable(entry.getId());
            else if (button.id == DISABLE_ID) changed = addons.disable(entry.getId());
            else if (button.id == REMOVE_ID) changed = addons.markRemoved(entry.getId());
            message = changed ? "Local state updated. No files were downloaded or executed."
                    : "That action is not valid for the selected add-on state.";
        }
    }

    private AddonEntry getSelectedEntry() {
        List<AddonEntry> entries = store.getAddonStore().getVisibleAddons();
        return selectedIndex >= 0 && selectedIndex < entries.size() ? entries.get(selectedIndex) : null;
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "ALPHA STORE", width / 2, 12, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "ADDONS  /  LOCAL CATALOGUE", width / 2, 25, 0xFFFFD866);
        if (searchField != null) searchField.drawTextBox();
        drawString(fontRendererObj, "Search", listLeft, 30, 0xFFB8BDC8);

        List<AddonEntry> entries = store.getAddonStore().getVisibleAddons();
        int visibleRows = Math.max(0, (listBottom - listTop) / 38);
        for (int i = 0; i < visibleRows && scrollOffset + i < entries.size(); i++) {
            int index = scrollOffset + i;
            AddonEntry entry = entries.get(index);
            int y = listTop + i * 38;
            drawRect(listLeft, y, listRight, y + 34,
                    index == selectedIndex ? 0xCC665522 : 0xAA252525);
            String title = entry.getName() + "  v" + entry.getVersion();
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(title, listRight - listLeft - 12),
                    listLeft + 6, y + 5, 0xFFFFFFFF);
            AddonInstallState state = store.getAddonStore().getRegistry().getState(entry.getId());
            String subtitle = entry.getCategory().getDisplayName() + "  |  " + state.name();
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(subtitle, listRight - listLeft - 12),
                    listLeft + 6, y + 19, 0xFFB8BDC8);
        }
        if (entries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No add-ons match this filter.", (listLeft + listRight) / 2,
                    listTop + 35, 0xFFB8BDC8);
            drawCenteredString(fontRendererObj, "Register catalogue entries from trusted AlphaClient code.",
                    (listLeft + listRight) / 2, listTop + 49, 0xFF888888);
        }

        AddonEntry selected = getSelectedEntry();
        int detailX = listRight + 12;
        int detailWidth = width - detailX - 12;
        drawRect(detailX, listTop, width - 12, listBottom, 0x88303030);
        drawString(fontRendererObj, "DETAILS", detailX + 8, listTop + 8, 0xFFFFD866);
        if (selected != null) {
            int y = listTop + 25;
            drawString(fontRendererObj, selected.getName(), detailX + 8, y, 0xFFFFFFFF);
            y += 16;
            y = drawWrapped(selected.getDescription(), detailX + 8, y, detailWidth - 16, 0xFFCCCCCC);
            y += 8;
            drawString(fontRendererObj, "Version: " + selected.getVersion(), detailX + 8, y, 0xFFB8BDC8); y += 12;
            drawString(fontRendererObj, "Author: " + selected.getAuthor(), detailX + 8, y, 0xFFB8BDC8); y += 12;
            drawString(fontRendererObj, "Category: " + selected.getCategory().getDisplayName(), detailX + 8, y, 0xFFB8BDC8); y += 12;
            drawString(fontRendererObj, "State: " + store.getAddonStore().getRegistry().getState(selected.getId()),
                    detailX + 8, y, 0xFFB8BDC8);
        } else {
            drawString(fontRendererObj, "Select an item to view its details.", detailX + 8, listTop + 28, 0xFFB8BDC8);
        }
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(message, width - 100),
                94, height - 43, 0xFFFFD866);
        String warning = store.getAddonStore().getStorageWarning();
        if (!warning.isEmpty()) drawString(fontRendererObj,
                fontRendererObj.trimStringToWidth(warning, width - 24), 12, height - 12, 0xFFFF7777);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int drawWrapped(String text, int x, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty()) return y;
        String remaining = text;
        while (!remaining.isEmpty()) {
            String line = fontRendererObj.trimStringToWidth(remaining, maxWidth);
            if (line.isEmpty()) break;
            drawString(fontRendererObj, line, x, y, color);
            remaining = remaining.substring(line.length()).trim();
            y += 10;
        }
        return y;
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
