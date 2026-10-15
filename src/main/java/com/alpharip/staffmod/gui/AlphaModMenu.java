package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
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
    private static final int OPEN_SETTINGS = 302;
    private static final int SEARCH_ID = 304;
    private static final int HUD_EDITOR = 305;
    private static final net.minecraft.util.ResourceLocation ALPHA_LOGO =
            new net.minecraft.util.ResourceLocation("alphastaffmod", "textures/gui/alpha_launcher_logo.png");
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private AlphaModule.Category selectedCategory = AlphaModule.Category.STAFF;
    private String selectedModuleId = "staffmods";
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
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 62, panelTop + 8, 54, 20, "CLOSE"));
        buttonList.add(new AlphaButton(COMMUNITY_LOGIN, panelLeft + panelWidth - 132, panelTop + 8, 62, 20, "ACCOUNT"));
        buttonList.add(new AlphaButton(HUD_EDITOR, panelLeft + 8, panelTop + panelHeight - 30, 104, 22, "HUD EDITOR"));
        buttonList.add(new AlphaButton(CATEGORY_BASE + 3, panelLeft + 7, panelTop + 58, sidebarWidth - 14, 24,
                "BHOP", true));

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
            String moduleStatus = "staffmods".equals(filtered.get(i).getId())
                    ? (BhopMode.isEnabled() ? "ON" : "OFF") : filtered.get(i).getStatusLabel();
            String moduleLabel = filtered.get(i).getName() + "  |  " + moduleStatus;
            buttonList.add(new AlphaButton(MODULE_BASE + i, x, y, tileWidth, tileHeight,
                    moduleLabel, filtered.get(i).getId().equals(selectedModuleId)));
        }

        AlphaModule selected = getSelectedModule();
        if (selected != null && selected.isImplemented() && selected.isUnlocked()) {
            buttonList.add(new AlphaButton(OPEN_SETTINGS, settingsLeft + 10, contentBottom - 31,
                    Math.max(105, panelLeft + panelWidth - 18 - (settingsLeft + 10)), 24,
                    "OPEN SETTINGS", true));
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
        int tileHeight = 32;
        int rowGap = 5;
        int visibleRows = Math.max(1, (contentBottom - contentTop) / (tileHeight + rowGap));
        int start = Math.min(scrollOffset, Math.max(0, modules.size() - visibleRows * 2));
        for (int i = start; i < modules.size() && i < start + visibleRows * 2; i++) {
            int local = i - start;
            int x = contentLeft + (local % 2) * (tileWidth + gap);
            int y = contentTop + (local / 2) * (tileHeight + rowGap);
            drawRect(x, y, x + tileWidth, y + tileHeight, 0xB5161820);
            drawRect(x, y, x + tileWidth, y + 1, 0x33494D57);
        }

        AlphaModule selected = getSelectedModule();
        if (selected != null) {
            drawRect(settingsLeft + 5, panelTop + 69, right - 8, panelTop + 108, 0xB5161820);
            drawString(fontRendererObj, selected.getName(), settingsLeft + 12, panelTop + 78, 0xFFF3F4F6);
            boolean selectedHeaderEnabled = "staffmods".equals(selected.getId())
                    ? BhopMode.isEnabled() : selected.isEnabled();
            drawString(fontRendererObj, selectedHeaderEnabled ? "ON" : "OFF",
                    settingsLeft + 12, panelTop + 92, selectedHeaderEnabled ? 0xFF9CE2AC : 0xFF9A9EAA);
            drawWrappedText(selected.getDescription(), settingsLeft + 7, panelTop + 119,
                    Math.max(100, right - settingsLeft - 22), 0xFFB9BDC6);

            drawRect(settingsLeft + 5, panelTop + 161, right - 8, panelTop + 162, 0x553F424A);
            drawString(fontRendererObj, "OPTIONS", settingsLeft + 7, panelTop + 172, ACCENT);
            int y = panelTop + 190;
            drawString(fontRendererObj, "Status", settingsLeft + 10, y, 0xFF969BA6);
            boolean selectedEnabled = "staffmods".equals(selected.getId())
                    ? BhopMode.isEnabled() : selected.isEnabled();
            drawString(fontRendererObj, selectedEnabled ? "ON" : "OFF", settingsLeft + 65, y,
                    selectedEnabled ? 0xFF9CE2AC : 0xFFB9BDC6);
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
        if (button.id == CATEGORY_BASE + 3) {
            selectedCategory = AlphaModule.Category.STAFF;
        } else if (button.id == OPEN_SETTINGS) {
            AlphaModule selected = getSelectedModule();
            if (selected != null && "staffmods".equals(selected.getId())
                    && selected.isImplemented() && selected.isUnlocked()) {
                mc.displayGuiScreen(new BhopSettingsGui(this));
            }
            return;
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
