package com.alpharip.staffmod.store;

import com.alpharip.staffmod.store.addons.AddonCategory;
import com.alpharip.staffmod.store.addons.AddonEntry;
import com.alpharip.staffmod.store.addons.AddonInstallState;
import com.alpharip.staffmod.store.addons.AddonStore;
import com.alpharip.staffmod.store.cosmetics.CosmeticCatalog;
import com.alpharip.staffmod.store.cosmetics.CosmeticCategory;
import com.alpharip.staffmod.store.cosmetics.CosmeticEntry;
import com.alpharip.staffmod.store.partners.PartnerCatalog;
import com.alpharip.staffmod.store.partners.PartnerEntry;
import com.alpharip.staffmod.store.partners.PartnerType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.entity.EntityPlayerSP;
import java.io.IOException;
import java.util.List;

/** Minecraft 1.8.9 Store browser. Actions on Addons only change local prototype metadata. */
public class StoreGui extends GuiScreen {
    private static final int SEARCH_ID = 1;
    private static final int SECTION_BASE = 5;
    private static final int CATEGORY_BASE = 20;
    private static final int INSTALLED_ID = 40;
    private static final int INSTALL_ID = 50;
    private static final int ENABLE_ID = 51;
    private static final int DISABLE_ID = 52;
    private static final int REMOVE_ID = 53;
    private static final int BACK_ID = 60;
    private static final int ROW_HEIGHT = 38;

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

    private boolean showingCosmetics() {
        return "cosmetics".equals(store.getSelectedSectionId());
    }

    private boolean showingPartners() {
        return "partners".equals(store.getSelectedSectionId());
    }

    @Override public void initGui() {
        buttonList.clear();
        int left = Math.max(12, width / 2 - 220);
        int right = Math.min(width - 12, width / 2 + 220);
        searchField = new GuiTextField(SEARCH_ID, fontRendererObj, left, 39,
                Math.max(100, right - left - 86), 18);
        searchField.setMaxStringLength(80);
        searchField.setText(showingCosmetics()
                ? store.getCosmeticCatalog().getSearchQuery()
                : showingPartners()
                    ? store.getPartnerCatalog().getSearchQuery()
                    : store.getAddonStore().getSettings().getSearchQuery());

        buttonList.add(new GuiButton(BACK_ID, 12, height - 30, 72, 20, "Back"));
        int sectionWidth = Math.max(90, (right - left) / Math.max(1, store.getSections().size()));
        int sectionX = left;
        for (int i = 0; i < store.getSections().size(); i++) {
            Store.StoreSection section = store.getSections().get(i);
            String label = section.getId().equals(store.getSelectedSectionId())
                    ? "[" + section.getTitle() + "]" : section.getTitle();
            buttonList.add(new GuiButton(SECTION_BASE + i, sectionX, 62,
                    sectionWidth - 3, 20, label));
            sectionX += sectionWidth;
        }

        int detailLeft = Math.max(width / 2 + 35, right - 155);
        if (showingCosmetics()) {
            CosmeticCategory[] categories = CosmeticCategory.values();
            addCategoryButtons(left, right, categories.length, categories);
        } else if (showingPartners()) {
            PartnerType[] categories = PartnerType.values();
            addCategoryButtons(left, right, categories.length, categories);
        } else {
            AddonCategory[] categories = AddonCategory.values();
            addCategoryButtons(left, right, categories.length, categories);
            buttonList.add(new GuiButton(INSTALLED_ID, right - 80, 38, 80, 20,
                    store.getAddonStore().getSettings().isShowInstalledOnly() ? "All items" : "Installed"));
            buttonList.add(new GuiButton(INSTALL_ID, detailLeft, height - 54, 72, 20, "Install"));
            buttonList.add(new GuiButton(ENABLE_ID, detailLeft + 76, height - 54, 72, 20, "Enable"));
            buttonList.add(new GuiButton(DISABLE_ID, detailLeft, height - 30, 72, 20, "Disable"));
            buttonList.add(new GuiButton(REMOVE_ID, detailLeft + 76, height - 30, 72, 20, "Remove"));
        }

        listLeft = left;
        listRight = Math.max(left + 110, width / 2 + 18);
        listTop = 110;
        listBottom = height - 66;
    }

    private void addCategoryButtons(int left, int right, int count, Object[] categories) {
        int categoryWidth = Math.max(44, (right - left) / Math.max(1, count));
        for (int i = 0; i < count; i++) {
            String label;
            if (categories[i] instanceof AddonCategory)
                label = ((AddonCategory) categories[i]).getDisplayName();
            else if (categories[i] instanceof CosmeticCategory)
                label = ((CosmeticCategory) categories[i]).getDisplayName();
            else label = ((PartnerType) categories[i]).getDisplayName();
            buttonList.add(new GuiButton(CATEGORY_BASE + i, left + i * categoryWidth, 85,
                    categoryWidth - 2, 18, label));
        }
    }

    @Override public void updateScreen() {
        if (searchField != null) searchField.updateCursorCounter();
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField != null && searchField.textboxKeyTyped(typedChar, keyCode)) {
            if (showingCosmetics()) store.getCosmeticCatalog().setSearchQuery(searchField.getText());
            else if (showingPartners()) store.getPartnerCatalog().setSearchQuery(searchField.getText());
            else store.getAddonStore().getSettings().setSearchQuery(searchField.getText());
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
            int row = (mouseY - listTop) / ROW_HEIGHT;
            int index = scrollOffset + row;
            selectedIndex = index < visibleCount() ? index : -1;
        }
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
            int max = Math.max(0, visibleCount() - visibleRows);
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel > 0 ? -1 : 1)));
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        int sectionCount = store.getSections().size();
        if (button.id >= SECTION_BASE && button.id < SECTION_BASE + sectionCount) {
            Store.StoreSection section = store.getSections().get(button.id - SECTION_BASE);
            store.selectSection(section.getId());
            selectedIndex = -1;
            scrollOffset = 0;
            initGui();
            return;
        }
        if (button.id >= CATEGORY_BASE && button.id < CATEGORY_BASE + categoryCount()) {
            int categoryIndex = button.id - CATEGORY_BASE;
            if (showingCosmetics())
                store.getCosmeticCatalog().setSelectedCategory(CosmeticCategory.values()[categoryIndex]);
            else if (showingPartners())
                store.getPartnerCatalog().setSelectedType(PartnerType.values()[categoryIndex]);
            else store.getAddonStore().getSettings().setSelectedCategory(AddonCategory.values()[categoryIndex]);
            selectedIndex = -1;
            scrollOffset = 0;
            initGui();
            return;
        }
        if (button.id == INSTALLED_ID) {
            store.getAddonStore().getSettings().setShowInstalledOnly(
                    !store.getAddonStore().getSettings().isShowInstalledOnly());
            selectedIndex = -1;
            scrollOffset = 0;
            initGui();
            return;
        }
        if (button.id == BACK_ID) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (showingCosmetics() || showingPartners()) return;

        AddonEntry entry = getSelectedAddon();
        if (entry == null) {
            message = "Select an add-on first.";
            return;
        }
        AddonStore addons = store.getAddonStore();
        boolean changed = false;
        if (button.id == INSTALL_ID) changed = addons.markInstalled(entry.getId());
        else if (button.id == ENABLE_ID) changed = addons.enable(entry.getId());
        else if (button.id == DISABLE_ID) changed = addons.disable(entry.getId());
        else if (button.id == REMOVE_ID) changed = addons.markRemoved(entry.getId());
        message = changed
                ? (addons.getStorageWarning().isEmpty()
                    ? "Local state updated. No files were downloaded or executed."
                    : "State changed in memory; saving failed.")
                : "That action is not valid for the selected add-on state.";
    }

    private int categoryCount() {
        if (showingCosmetics()) return CosmeticCategory.values().length;
        if (showingPartners()) return PartnerType.values().length;
        return AddonCategory.values().length;
    }

    private int visibleCount() {
        if (showingCosmetics()) return store.getCosmeticCatalog().getVisibleCosmetics().size();
        if (showingPartners()) return store.getPartnerCatalog().getVisiblePartners().size();
        return store.getAddonStore().getVisibleAddons().size();
    }

    private AddonEntry getSelectedAddon() {
        List<AddonEntry> entries = store.getAddonStore().getVisibleAddons();
        return selectedIndex >= 0 && selectedIndex < entries.size() ? entries.get(selectedIndex) : null;
    }

    private CosmeticEntry getSelectedCosmetic() {
        List<CosmeticEntry> entries = store.getCosmeticCatalog().getVisibleCosmetics();
        return selectedIndex >= 0 && selectedIndex < entries.size() ? entries.get(selectedIndex) : null;
    }

    private PartnerEntry getSelectedPartner() {
        List<PartnerEntry> entries = store.getPartnerCatalog().getVisiblePartners();
        return selectedIndex >= 0 && selectedIndex < entries.size() ? entries.get(selectedIndex) : null;
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "ALPHA STORE", width / 2, 12, 0xFFFFFFFF);
        String pageTitle = showingCosmetics() ? "COSMETICS  /  CATALOGUE"
                : showingPartners() ? "PARTNERS  /  DIRECTORY" : "ADDONS  /  LOCAL CATALOGUE";
        drawCenteredString(fontRendererObj, pageTitle, width / 2, 25, 0xFFFFD866);
        if (searchField != null) searchField.drawTextBox();
        drawString(fontRendererObj, "Search", listLeft, 30, 0xFFB8BDC8);

        if (showingCosmetics()) drawCosmetics();
        else if (showingPartners()) drawPartners();
        else drawAddons();

        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(message, width - 100),
                94, height - 43, 0xFFFFD866);
        String warning = store.getAddonStore().getStorageWarning();
        if (!warning.isEmpty()) drawString(fontRendererObj,
                fontRendererObj.trimStringToWidth(warning, width - 24), 12, height - 12, 0xFFFF7777);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawSkinPreview() {
        int centerX = (listRight + width - 12) / 2;
        drawCenteredString(fontRendererObj, "YOUR MINECRAFT SKIN", centerX,
                listTop + 22, 0xFFFFD866);
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) {
            drawCenteredString(fontRendererObj, "Player skin unavailable",
                    centerX, listTop + 90, 0xFFB8BDC8);
            drawCenteredString(fontRendererObj, "Open the Store in-game to preview your skin.",
                    centerX, listTop + 104, 0xFF888888);
            return;
        }
        StoreSkinPreview.draw(centerX, listTop + 172, 58, player);
        drawCenteredString(fontRendererObj, player.getName(), centerX,
                listTop + 180, 0xFFB8BDC8);
    }

    private void drawAddons() {
        List<AddonEntry> entries = store.getAddonStore().getVisibleAddons();
        int visibleRows = Math.max(0, (listBottom - listTop) / ROW_HEIGHT);
        for (int i = 0; i < visibleRows && scrollOffset + i < entries.size(); i++) {
            int index = scrollOffset + i;
            AddonEntry entry = entries.get(index);
            int y = listTop + i * ROW_HEIGHT;
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
        if (entries.isEmpty()) drawEmptyState("No add-ons match this filter.",
                "Register catalogue entries from trusted AlphaClient code.");
        drawRect(listRight + 12, listTop, width - 12, listBottom, 0x88303030);
        drawString(fontRendererObj, "DETAILS", listRight + 20, listTop + 8, 0xFFFFD866);
        drawSkinPreview();
        AddonEntry selected = getSelectedAddon();
        if (selected == null) {
            drawString(fontRendererObj, "Select an add-on to view its details.",
                    listRight + 20, listTop + 198, 0xFFB8BDC8);
            return;
        }
        int x = listRight + 20;
        int y = listTop + 198;
        int maxWidth = width - x - 24;
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(selected.getName(), maxWidth),
                x, y, 0xFFFFFFFF);
        y += 16;
        y = drawWrapped(selected.getDescription(), x, y, maxWidth, 0xFFCCCCCC);
        y += 8;
        drawString(fontRendererObj, "Version: " + selected.getVersion(), x, y, 0xFFB8BDC8); y += 12;
        drawString(fontRendererObj, "Author: " + selected.getAuthor(), x, y, 0xFFB8BDC8); y += 12;
        drawString(fontRendererObj, "Category: " + selected.getCategory().getDisplayName(), x, y, 0xFFB8BDC8); y += 12;
        drawString(fontRendererObj, "State: " + store.getAddonStore().getRegistry().getState(selected.getId()),
                x, y, 0xFFB8BDC8);
    }

    private void drawCosmetics() {
        CosmeticCatalog catalog = store.getCosmeticCatalog();
        List<CosmeticEntry> entries = catalog.getVisibleCosmetics();
        int visibleRows = Math.max(0, (listBottom - listTop) / ROW_HEIGHT);
        for (int i = 0; i < visibleRows && scrollOffset + i < entries.size(); i++) {
            int index = scrollOffset + i;
            CosmeticEntry entry = entries.get(index);
            int y = listTop + i * ROW_HEIGHT;
            drawRect(listLeft, y, listRight, y + 34,
                    index == selectedIndex ? 0xCC665522 : 0xAA252525);
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(entry.getName(),
                    listRight - listLeft - 12), listLeft + 6, y + 5, 0xFFFFFFFF);
            drawString(fontRendererObj, entry.getCategory().getDisplayName(),
                    listLeft + 6, y + 19, 0xFFB8BDC8);
        }
        if (entries.isEmpty()) drawEmptyState("No cosmetics match this filter.",
                "Register real cosmetic entries from trusted AlphaClient code.");
        drawRect(listRight + 12, listTop, width - 12, listBottom, 0x88303030);
        drawString(fontRendererObj, "COSMETIC DETAILS", listRight + 20, listTop + 8, 0xFFFFD866);
        drawSkinPreview();
        CosmeticEntry selected = getSelectedCosmetic();
        if (selected == null) {
            drawString(fontRendererObj, "Select a cosmetic to view its details.",
                    listRight + 20, listTop + 198, 0xFFB8BDC8);
            return;
        }
        int x = listRight + 20;
        int y = listTop + 25;
        int maxWidth = width - x - 24;
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(selected.getName(), maxWidth),
                x, y, 0xFFFFFFFF);
        y += 16;
        y = drawWrapped(selected.getDescription(), x, y, maxWidth, 0xFFCCCCCC);
        y += 8;
        drawString(fontRendererObj, "Category: " + selected.getCategory().getDisplayName(),
                x, y, 0xFFB8BDC8); y += 12;
        if (!selected.getSourceClass().isEmpty())
            drawWrapped("Source: " + selected.getSourceClass(), x, y, maxWidth, 0xFFB8BDC8);
    }

    private void drawPartners() {
        PartnerCatalog catalog = store.getPartnerCatalog();
        List<PartnerEntry> entries = catalog.getVisiblePartners();
        int visibleRows = Math.max(0, (listBottom - listTop) / ROW_HEIGHT);
        for (int i = 0; i < visibleRows && scrollOffset + i < entries.size(); i++) {
            int index = scrollOffset + i;
            PartnerEntry entry = entries.get(index);
            int y = listTop + i * ROW_HEIGHT;
            drawRect(listLeft, y, listRight, y + 34,
                    index == selectedIndex ? 0xCC665522 : 0xAA252525);
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(entry.getName(),
                    listRight - listLeft - 12), listLeft + 6, y + 5, 0xFFFFFFFF);
            drawString(fontRendererObj, entry.getType().getDisplayName()
                    + (entry.getPlatform().isEmpty() ? "" : "  |  " + entry.getPlatform()),
                    listLeft + 6, y + 19, 0xFFB8BDC8);
        }
        if (entries.isEmpty()) drawEmptyState("No directory entries match this filter.",
                "Add partners, staff-cape recipients, or collaborators.");
        drawRect(listRight + 12, listTop, width - 12, listBottom, 0x88303030);
        drawString(fontRendererObj, "PARTNER DETAILS", listRight + 20, listTop + 8, 0xFFFFD866);
        drawSkinPreview();
        PartnerEntry selected = getSelectedPartner();
        if (selected == null) {
            drawString(fontRendererObj, "Select an entry to view its details.",
                    listRight + 20, listTop + 198, 0xFFB8BDC8);
            return;
        }
        int x = listRight + 20;
        int y = listTop + 25;
        int maxWidth = width - x - 24;
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(selected.getName(), maxWidth),
                x, y, 0xFFFFFFFF);
        y += 16;
        drawString(fontRendererObj, "Relationship: " + selected.getType().getDisplayName(),
                x, y, 0xFFB8BDC8); y += 12;
        if (!selected.getPlatform().isEmpty()) {
            drawString(fontRendererObj, "Platform: " + selected.getPlatform(), x, y, 0xFFB8BDC8);
            y += 12;
        }
        y = drawWrapped(selected.getDescription(), x, y + 4, maxWidth, 0xFFCCCCCC);
        if (!selected.getProfileUrl().isEmpty())
            drawWrapped("Link: " + selected.getProfileUrl(), x, y + 4, maxWidth, 0xFFB8BDC8);
    }

    private void drawEmptyState(String title, String subtitle) {
        drawCenteredString(fontRendererObj, title, (listLeft + listRight) / 2,
                listTop + 35, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, subtitle, (listLeft + listRight) / 2,
                listTop + 49, 0xFF888888);
    }

    private int drawWrapped(String text, int x, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty() || maxWidth <= 0) return y;
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
