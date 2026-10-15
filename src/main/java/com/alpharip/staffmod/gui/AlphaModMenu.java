package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Compact in-game AlphaClient module browser inspired by the supplied reference:
 * Minecraft remains visible around a translucent window, with categories,
 * searchable module tiles, and selected-module settings on the right.
 */
public final class AlphaModMenu extends GuiScreen {
    private static final int CATEGORY_BASE = 20;
    private static final int MODULE_BASE = 100;
    private static final int BACK = 300;
    private static final int COMMUNITY_LOGIN = 301;
    private static final int TOGGLE_MODULE = 302;
    private static final int STAFF_MENU = 303;
    private static final int SEARCH_ID = 304;
    private static final int HUD_EDITOR = 305;
    private static final net.minecraft.util.ResourceLocation ALPHA_LOGO =
            new net.minecraft.util.ResourceLocation("alphastaffmod", "textures/gui/alpha_launcher_logo.png");
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private AlphaModule.Category selectedCategory = AlphaModule.Category.PUBLIC;
    private String selectedModuleId = "alpha_hud";
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int sidebarWidth, contentLeft, contentRight, contentTop, contentBottom;
    private int settingsLeft;
    private int scrollOffset;
    private GuiTextField searchField;
    private String searchText = "";
    private boolean searchFocused;

    public AlphaModMenu(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(610, Math.max(440, width - 80));
        panelHeight = Math.min(320, Math.max(250, height - 70));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        sidebarWidth = 88;
        contentLeft = panelLeft + sidebarWidth + 10;
        contentRight = panelLeft + panelWidth - 126;
        contentTop = panelTop + 58;
        contentBottom = panelTop + panelHeight - 30;
        settingsLeft = contentRight + 8;

        searchField = new GuiTextField(SEARCH_ID, fontRendererObj,
                contentLeft + 4, panelTop + 27, Math.max(100, contentRight - contentLeft - 8), 17);
        searchField.setMaxStringLength(48);
        searchField.setText(searchText);
        searchField.setFocused(searchFocused);
        searchField.setEnableBackgroundDrawing(true);
        searchField.setTextColor(0xFFE7E9EE);
        searchField.setDisabledTextColour(0xFF858B98);
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 42, panelTop + 7, 34, 16, "X"));
        buttonList.add(new AlphaButton(COMMUNITY_LOGIN, panelLeft + panelWidth - 82, panelTop + 7, 34, 16, "⚙"));
        buttonList.add(new AlphaButton(HUD_EDITOR, panelLeft + 7, panelTop + panelHeight - 23, 82, 15, "HUD EDITOR"));
        buttonList.add(new AlphaButton(CATEGORY_BASE, panelLeft + 6, panelTop + 58, sidebarWidth - 12, 17,
                "GENERAL", selectedCategory == AlphaModule.Category.PUBLIC));

        if (PermissionService.hasAccess(PermissionService.Rank.BETA)) {
            buttonList.add(new AlphaButton(CATEGORY_BASE + 1, panelLeft + 6, panelTop + 79,
                    sidebarWidth - 12, 17, "BETA", selectedCategory == AlphaModule.Category.BETA));
        }
        if (PermissionService.hasAccess(PermissionService.Rank.DONATOR)) {
            buttonList.add(new AlphaButton(CATEGORY_BASE + 2, panelLeft + 6, panelTop + 100,
                    sidebarWidth - 12, 17, "DONATOR", selectedCategory == AlphaModule.Category.DONATOR));
        }
        if (PermissionService.hasModAccess("staff.bhop")) {
            buttonList.add(new AlphaButton(STAFF_MENU, panelLeft + 6, panelTop + panelHeight - 44,
                    sidebarWidth - 12, 17, "STAFF"));
        }

        List<AlphaModule> filtered = getFilteredModules();
        int gridWidth = Math.max(180, contentRight - contentLeft);
        int gap = 8;
        int tileWidth = (gridWidth - gap) / 2;
        int tileHeight = 32;
        int rowGap = 5;
        int visibleRows = Math.max(1, (contentBottom - contentTop) / (tileHeight + rowGap));
        int start = Math.min(scrollOffset, Math.max(0, filtered.size() - visibleRows * 2));
        for (int i = start; i < filtered.size() && i < start + visibleRows * 2; i++) {
            int local = i - start;
            int x = contentLeft + (local % 2) * (tileWidth + gap);
            int y = contentTop + (local / 2) * (tileHeight + rowGap);
            buttonList.add(new AlphaButton(MODULE_BASE + i, x, y, tileWidth, tileHeight,
                    filtered.get(i).getName(), filtered.get(i).getId().equals(selectedModuleId)));
        }

        AlphaModule selected = getSelectedModule();
        if (selected != null && selected.isImplemented() && selected.isUnlocked()) {
            buttonList.add(new AlphaButton(TOGGLE_MODULE, settingsLeft + 10, contentBottom - 31,
                    Math.max(105, panelLeft + panelWidth - 18 - (settingsLeft + 10)), 22,
                    selected.isEnabled() ? "DISABLE" : "ENABLE", !selected.isEnabled()));
        }
    }

    private List<AlphaModule> getFilteredModules() {
        List<AlphaModule> result = new ArrayList<AlphaModule>();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        for (AlphaModule module : AlphaModuleRegistry.getForCategory(selectedCategory)) {
            if (query.isEmpty() || module.getName().toLowerCase(Locale.ROOT).contains(query)
                    || module.getDescription().toLowerCase(Locale.ROOT).contains(query)) result.add(module);
        }
        return result;
    }

    private int visibleRows() {
        return Math.max(1, (contentBottom - contentTop) / 56);
    }

    private AlphaModule getSelectedModule() {
        AlphaModule module = AlphaModuleRegistry.find(selectedModuleId);
        if (module == null || module.getCategory() != selectedCategory) {
            List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
            return modules.isEmpty() ? null : modules.get(0);
        }
        return module;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Only dim the game slightly; the surrounding world remains part of the interface.
        drawRect(0, 0, width, height, 0x18000000);
        int right = panelLeft + panelWidth, bottom = panelTop + panelHeight;
        drawRect(panelLeft, panelTop, right, bottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, right, panelTop + 1, 0x554B515D);
        drawRect(panelLeft, panelTop + 1, panelLeft + 1, bottom - 1, 0x334B515D);
        drawRect(right - 1, panelTop + 1, right, bottom - 1, 0x334B515D);
        drawRect(panelLeft + sidebarWidth, panelTop + 2, panelLeft + sidebarWidth + 1, bottom - 1, 0x553F424A);
        drawRect(settingsLeft - 7, panelTop + 2, settingsLeft - 6, bottom - 1, 0x553F424A);

        Minecraft.getMinecraft().getTextureManager().bindTexture(ALPHA_LOGO);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawModalRectWithCustomSizedTexture(panelLeft + 10, panelTop + 8, 0, 0, 26, 26, 256, 256);
        drawString(fontRendererObj, "ALPHA", panelLeft + 42, panelTop + 9, 0xFFF0F1F4);
        drawString(fontRendererObj, "C L I E N T", panelLeft + 42, panelTop + 20, ACCENT);
        drawString(fontRendererObj, AlphaModuleRegistry.getForCategory(selectedCategory).size()
                + " modules", contentLeft + 4, panelTop + 12, 0xFF858B98);
        searchField.drawTextBox();

        drawString(fontRendererObj, "BROWSE", panelLeft + 7, panelTop + 46, 0xFF9297A2);
        drawString(fontRendererObj, selectedCategory.getTitle().toUpperCase(Locale.ROOT),
                contentLeft + 4, contentTop - 11, ACCENT);
        drawString(fontRendererObj, "DETAILS", settingsLeft + 7, panelTop + 46, 0xFF9297A2);

        List<AlphaModule> modules = getFilteredModules();
        int gridWidth = Math.max(180, contentRight - contentLeft);
        int gap = 8;
        int tileWidth = (gridWidth - gap) / 2;
        int tileHeight = 32, rowGap = 5;
        int visibleRows = Math.max(1, (contentBottom - contentTop) / (tileHeight + rowGap));
        int start = Math.min(scrollOffset, Math.max(0, modules.size() - visibleRows * 2));
        for (int i = start; i < modules.size() && i < start + visibleRows * 2; i++) {
            AlphaModule module = modules.get(i);
            int local = i - start;
            int x = contentLeft + (local % 2) * (tileWidth + gap);
            int y = contentTop + (local / 2) * (tileHeight + rowGap);
            boolean selected = module.getId().equals(selectedModuleId);
            drawRect(x, y, x + tileWidth, y + tileHeight, selected ? 0xD02B281D : 0xB5161820);
            drawRect(x, y, x + tileWidth, y + 1, selected ? ACCENT : 0x33494D57);
            if (selected) drawRect(x, y, x + 2, y + tileHeight, ACCENT);
            drawRect(x + 8, y + 9, x + 29, y + 31, selected ? 0x663F350F : 0x442E2B22);
            drawString(fontRendererObj, moduleIcon(module.getId()), x + 12, y + 15,
                    selected ? 0xFFFFD32F : 0xFFBBA34D);
            drawString(fontRendererObj, module.getName(), x + 35, y + 6,
                    module.isUnlocked() ? 0xFFF2F3F5 : 0xFF858B98);
            String status = module.isUnlocked() ? module.getStatusLabel() : "LOCKED";
            drawString(fontRendererObj, status, x + 35, y + 21,
                    module.isEnabled() ? 0xFF9CE2AC : 0xFF9A9EAA);
            int switchX = x + tileWidth - 24;
            drawRect(switchX, y + 15, switchX + 15, y + 23,
                    module.isEnabled() ? ACCENT : 0xFF50545D);
            drawRect(switchX + (module.isEnabled() ? 8 : 1), y + 16,
                    switchX + (module.isEnabled() ? 13 : 6), y + 22, 0xFFF8F8F8);
        }

        AlphaModule selected = getSelectedModule();
        if (selected != null) {
            drawRect(settingsLeft + 5, panelTop + 69, right - 8, panelTop + 108, 0xB5161820);
            drawString(fontRendererObj, moduleIcon(selected.getId()), settingsLeft + 12, panelTop + 81, ACCENT);
            drawString(fontRendererObj, selected.getName(), settingsLeft + 30, panelTop + 78, 0xFFF3F4F6);
            drawString(fontRendererObj, selected.isEnabled() ? "ON" : "OFF",
                    settingsLeft + 30, panelTop + 92, selected.isEnabled() ? 0xFF9CE2AC : 0xFF9A9EAA);
            drawWrappedText(selected.getDescription(), settingsLeft + 7, panelTop + 119,
                    Math.max(100, right - settingsLeft - 22), 0xFFB9BDC6);

            drawRect(settingsLeft + 5, panelTop + 161, right - 8, panelTop + 162, 0x553F424A);
            drawString(fontRendererObj, "OPTIONS", settingsLeft + 7, panelTop + 172, ACCENT);
            int y = panelTop + 190;
            drawString(fontRendererObj, "Status", settingsLeft + 10, y, 0xFF969BA6);
            drawString(fontRendererObj, selected.getStatusLabel(), settingsLeft + 65, y,
                    selected.isEnabled() ? 0xFF9CE2AC : 0xFFB9BDC6);
            y += 17;
            
            List<AlphaModuleFeature> features = selected.getFeatures();
            if (features.isEmpty()) {
                drawWrappedText(selected.isImplemented() ? "No extra options available yet."
                        : "Not implemented yet.", settingsLeft + 10, y,
                        Math.max(100, right - settingsLeft - 22), 0xFF858B98);
            } else {
                for (int i = 0; i < features.size() && y < contentBottom - 28; i++) {
                    AlphaModuleFeature feature = features.get(i);
                    drawString(fontRendererObj, feature.getName(), settingsLeft + 10, y,
                            feature.isAvailable() ? 0xFF9CE2AC : 0xFFB9BDC6);
                    y += 13;
                }
            }
        } else {
            drawString(fontRendererObj, "No modules found.", settingsLeft + 10, panelTop + 91, 0xFF858B98);
        }

        drawString(fontRendererObj, "RANK: " + (SupabaseCommunityService.isSignedIn() ? SupabaseCommunityService.getSignedInRankLabel() : "NO RANK"),
                settingsLeft + 7, bottom - 14, SupabaseCommunityService.isSignedIn() ? ACCENT : 0xFF858B98);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String moduleIcon(String id) {
        if ("alpha_hud".equals(id)) return "A";
        if ("fps_counter".equals(id)) return "F";
        if ("keystrokes".equals(id)) return "K";
        if ("motion_blur".equals(id)) return "M";
        if ("cosmetics".equals(id)) return "C";
        if ("staffmods".equals(id)) return "S";
        return "*";
    }

    private void drawWrappedText(String text, int x, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty()) return;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fontRendererObj.getStringWidth(candidate) > maxWidth && line.length() > 0) {
                drawString(fontRendererObj, line.toString(), x, y, color);
                y += 11;
                line = new StringBuilder(word);
            } else line = new StringBuilder(candidate);
        }
        if (line.length() > 0) drawString(fontRendererObj, line.toString(), x, y, color);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (button.id == COMMUNITY_LOGIN) { mc.displayGuiScreen(new CommunityLoginGui(this)); return; }
        if (button.id == HUD_EDITOR) { mc.displayGuiScreen(new HudEditorGui(this)); return; }
        if (button.id == STAFF_MENU) {
            if (PermissionService.hasModAccess("staff.bhop")) mc.displayGuiScreen(new StaffRankMenuGui(this));
            return;
        }
        if (button.id == CATEGORY_BASE) {
            selectedCategory = AlphaModule.Category.PUBLIC;
        } else if (button.id == CATEGORY_BASE + 1
                && PermissionService.hasAccess(PermissionService.Rank.BETA)) {
            selectedCategory = AlphaModule.Category.BETA;
        } else if (button.id == CATEGORY_BASE + 2
                && PermissionService.hasAccess(PermissionService.Rank.DONATOR)) {
            selectedCategory = AlphaModule.Category.DONATOR;
        } else if (button.id == TOGGLE_MODULE) {
            AlphaModule selected = getSelectedModule();
            if (selected != null && "staffmods".equals(selected.getId())) {
                if (PermissionService.hasModAccess("staff.bhop")) mc.displayGuiScreen(new StaffRankMenuGui(this));
                return;
            }
            if (selected != null && selected.isImplemented() && selected.isUnlocked()) {
                selected.setEnabled(!selected.isEnabled());
            }
        } else if (button.id >= MODULE_BASE) {
            List<AlphaModule> modules = getFilteredModules();
            int index = button.id - MODULE_BASE;
            if (index >= 0 && index < modules.size()) selectedModuleId = modules.get(index).getId();
        }
        scrollOffset = 0;
        initGui();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField != null && searchField.isFocused()) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                searchFocused = false;
                searchField.setFocused(false);
            } else {
                searchField.textboxKeyTyped(typedChar, keyCode);
                searchText = searchField.getText();
                searchFocused = true;
                scrollOffset = 0;
                initGui();
            }
            return;
        }
        if (keyCode == Keyboard.KEY_SLASH) {
            searchFocused = true;
            searchField.setFocused(true);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (searchField != null) {
            searchField.mouseClicked(mouseX, mouseY, mouseButton);
            searchFocused = searchField.isFocused();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            List<AlphaModule> modules = getFilteredModules();
            int maxOffset = Math.max(0, modules.size() - visibleRows() * 2);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + (wheel < 0 ? 2 : -2)));
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
