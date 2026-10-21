package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.pvp.LegacyPvpSettingsGui;
import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.alpharip.staffmod.permissions.PermissionService;
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
    private static final int EXIT = 300;
    private static final int ACCOUNT = 301;
    private static final int SEARCH_ID = 304;
    private static final int HUD_EDITOR = 305;
    private static final int STAFF_MODS = 306;
    private static final int PROFILE_SELECT_BASE = 400;
    private static final int PROFILE_CREATE = 430;
    private static final int PROFILE_EDIT = 431;
    private static final int PROFILE_DELETE = 432;
    private static final net.minecraft.util.ResourceLocation ALPHA_LOGO =
            new net.minecraft.util.ResourceLocation("alphastaffmod", "textures/gui/alpha-client-icon.png");
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private AlphaModule.Category selectedCategory = AlphaModule.Category.PUBLIC;
    private String selectedModuleId = "staffmods";
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int sidebarWidth, contentLeft, contentRight, contentTop, contentBottom;
    private int settingsLeft;
    private int scrollOffset;
    private int profileScrollOffset;
    private GuiTextField searchField;
    private String searchText = "";
    private boolean searchFocused;
    private String accessFingerprint = "";

    public AlphaModMenu(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(460, Math.max(280, width - 20));
        panelHeight = Math.min(300, Math.max(210, height - 24));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        sidebarWidth = 70;
        contentLeft = panelLeft + sidebarWidth + 10;
        contentRight = panelLeft + panelWidth - 112;
        contentTop = panelTop + 49;
        contentBottom = panelTop + panelHeight - 25;
        settingsLeft = contentRight + 8;

        searchField = new GuiTextField(SEARCH_ID, fontRendererObj,
                contentLeft + 3, panelTop + 25, Math.max(80, contentRight - contentLeft - 6), 15);
        searchField.setMaxStringLength(48);
        searchField.setText(searchText);
        searchField.setFocused(searchFocused);
        searchField.setEnableBackgroundDrawing(true);
        searchField.setTextColor(0xFFE7E9EE);
        searchField.setDisabledTextColour(0xFFB8BDC8);
        buttonList.add(new AlphaButton(ACCOUNT, panelLeft + panelWidth - 43, panelTop + 5, 16, 16, ""));
        buttonList.add(new AlphaButton(EXIT, panelLeft + panelWidth - 23, panelTop + 5, 16, 16, ""));
        buttonList.add(new AlphaButton(HUD_EDITOR, panelLeft + 5, panelTop + panelHeight - 36, 64, 14, "HUD EDITOR"));

        int categoryY = panelTop + 68;
        for (AlphaModule.Category category : AlphaModule.Category.values()) {
            if (category != AlphaModule.Category.PUBLIC
                    && category != AlphaModule.Category.BETA
                    && category != AlphaModule.Category.DONATOR) continue;
            if (!PermissionService.hasAccess(category.getRequiredRank())) continue;
            String label;
            switch (category) {
                case PUBLIC: label = "GENERAL"; break;
                case BETA: label = "BETA"; break;
                case DONATOR: label = "DONATOR"; break;
                case T_MOD: label = "T-MOD"; break;
                case MOD: label = "MOD"; break;
                case SR_MOD: label = "SR-MOD"; break;
                case ADMIN: label = "ADMIN"; break;
                case SR_ADMIN: label = "SR-ADMIN"; break;
                case MANAGEMENT: label = "MANAGEMENT"; break;
                case OWNER: label = "OWNER"; break;
                case FOUNDER: label = "FOUNDER"; break;
                default: continue;
            }
            buttonList.add(new AlphaButton(CATEGORY_BASE + category.ordinal(),
                    panelLeft + 5, categoryY, sidebarWidth - 10, 20,
                    label, category == selectedCategory));
            categoryY += 23;
        }
        if (PermissionService.hasAccess(PermissionService.Rank.T_MOD)) {
            buttonList.add(new AlphaButton(STAFF_MODS, panelLeft + 5, categoryY,
                    sidebarWidth - 10, 20, "STAFFMODS"));
            categoryY += 23;
        }
        if ("staffmods".equals(selectedModuleId)) {
            List<ModuleProfileManager.BhopProfile> profiles = ModuleProfileManager.getProfiles();
            int actionY = panelTop + panelHeight - 48;
            int profileTop = panelTop + 86;
            int profileBottom = actionY - 8;
            int visibleProfiles = Math.max(1, (profileBottom - profileTop) / 21);
            profileScrollOffset = Math.max(0, Math.min(profileScrollOffset, Math.max(0, profiles.size() - visibleProfiles)));
            int actionsLeft = settingsLeft + 4;
            int actionsRight = panelLeft + panelWidth - 10;
            int actionGap = 4;
            int actionWidth = Math.max(1, (actionsRight - actionsLeft - actionGap * 2) / 3);
            for (int row = 0; row < visibleProfiles && profileScrollOffset + row < profiles.size(); row++) {
                ModuleProfileManager.BhopProfile profile = profiles.get(profileScrollOffset + row);
                buttonList.add(new AlphaButton(PROFILE_SELECT_BASE + row, settingsLeft + 4,
                        profileTop + row * 21, Math.max(50, panelLeft + panelWidth - settingsLeft - 10),
                        18, profile.getProfileName(),
                        profile.getProfileName().equalsIgnoreCase(ModuleProfileManager.getSelectedProfileName())));
            }
            buttonList.add(new AlphaButton(PROFILE_CREATE, actionsLeft, actionY, actionWidth, 15, "CREATE"));
            buttonList.add(new AlphaButton(PROFILE_EDIT, actionsLeft + actionWidth + actionGap, actionY, actionWidth, 15, "EDIT"));
            buttonList.add(new AlphaButton(PROFILE_DELETE, actionsLeft + (actionWidth + actionGap) * 2, actionY, actionWidth, 15, "DELETE"));
        }

        List<AlphaModule> filtered = getFilteredModules();
        int gridWidth = Math.max(180, contentRight - contentLeft);
        int gap = 8;
        int tileWidth = (gridWidth - gap) / 2;
        int tileHeight = 54;
        int rowGap = 7;
        int visibleRows = Math.max(1, (contentBottom - contentTop) / (tileHeight + rowGap));
        int start = Math.min(scrollOffset, Math.max(0, filtered.size() - visibleRows * 2));
        for (int i = start; i < filtered.size() && i < start + visibleRows * 2; i++) {
            int local = i - start;
            int x = contentLeft + (local % 2) * (tileWidth + gap);
            int y = contentTop + (local / 2) * (tileHeight + rowGap);
            AlphaModule module = filtered.get(i);
            buttonList.add(new ModuleTileButton(MODULE_BASE + i, x, y, tileWidth, tileHeight,
                    module, module.getId().equals(selectedModuleId)));
        }

        accessFingerprint = buildAccessFingerprint();
    }

    private String buildAccessFingerprint() {
        StringBuilder fingerprint = new StringBuilder(PermissionService.getEffectiveRank().name());
        fingerprint.append('|').append(PermissionService.areModEntitlementsVerified());
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            fingerprint.append('|').append(module.getId()).append('=').append(module.isUnlocked());
        }
        return fingerprint.toString();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        String current = buildAccessFingerprint();
        if (!current.equals(accessFingerprint)) {
            initGui();
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
        if ("staffmods".equals(selectedModuleId)
                && (module == null || module.getCategory() != selectedCategory)) {
            // Keep the profile manager visible until the user chooses a module category.
            return null;
        }
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
        drawModalRectWithCustomSizedTexture(panelLeft + 18, panelTop + 5, 0, 0, 32, 32, 256, 256);
        drawString(fontRendererObj, "MODS", panelLeft + 8, panelTop + 40, 0xFFF0F1F4);
        drawString(fontRendererObj, AlphaModuleRegistry.getForCategory(selectedCategory).size()
                + " modules", contentLeft + 4, panelTop + 8, 0xFFB8BDC8);
        searchField.drawTextBox();

        drawString(fontRendererObj, "BROWSE", panelLeft + 6, panelTop + 57, 0xFFB8BDC8);
        drawString(fontRendererObj, selectedCategory.getTitle().toUpperCase(Locale.ROOT),
                contentLeft + 3, contentTop - 9, ACCENT);
        AlphaModule selected = getSelectedModule();
        if (selected == null) {
            drawString(fontRendererObj, "PROFILES",
                    settingsLeft + 5, panelTop + 39, 0xFFB8BDC8);
            List<ModuleProfileManager.BhopProfile> profiles = ModuleProfileManager.getProfiles();
            String activeProfile = ModuleProfileManager.getSelectedProfileName();
            int profileTop = panelTop + 67;
            int actionY = panelTop + panelHeight - 48;
            int profileBottom = actionY - 8;
            int visibleProfiles = Math.max(1, (profileBottom - profileTop) / 21);
            drawString(fontRendererObj, "Select a profile", settingsLeft + 7, panelTop + 55, 0xFFF3F4F6);
            if (profiles.isEmpty()) {
                drawString(fontRendererObj, "No profiles available.", settingsLeft + 7, profileTop + 4, 0xFFB8BDC8);
            } else {
                drawString(fontRendererObj, "Active: " + activeProfile, settingsLeft + 7,
                        panelTop + panelHeight - 27, 0xFF9CE2AC);
                if (profiles.size() > visibleProfiles) {
                    drawString(fontRendererObj, "Scroll for more (" + (profileScrollOffset + 1) + "-"
                            + Math.min(profiles.size(), profileScrollOffset + visibleProfiles) + "/" + profiles.size() + ")",
                            settingsLeft + 7, panelTop + 68, 0xFFB8BDC8);
                }
            }
        }
        String rankText = "RANK: " + (SupabaseCommunityService.isSignedIn()
                ? SupabaseCommunityService.getSignedInRankLabel() : "NO RANK");
        int rankMaxWidth = Math.max(24, panelLeft + panelWidth - 14 - settingsLeft);
        while (rankText.length() > 1 && fontRendererObj.getStringWidth(rankText) > rankMaxWidth) {
            rankText = rankText.substring(0, rankText.length() - 1);
        }
        drawString(fontRendererObj, rankText, settingsLeft + 5, bottom - 11,
                SupabaseCommunityService.isSignedIn() ? ACCENT : 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
        int iconY = panelTop + 5;
        int accountX = panelLeft + panelWidth - 43;
        int accountCenter = accountX + 8;
        drawRect(accountCenter - 2, iconY + 3, accountCenter + 2, iconY + 7, ACCENT);
        drawRect(accountCenter - 4, iconY + 8, accountCenter + 4, iconY + 12, ACCENT);
        int exitX = panelLeft + panelWidth - 23;
        drawRect(exitX + 4, iconY + 3, exitX + 6, iconY + 13, 0xFFE6E8ED);
        drawRect(exitX + 6, iconY + 7, exitX + 12, iconY + 9, 0xFFE6E8ED);
        drawRect(exitX + 10, iconY + 5, exitX + 12, iconY + 7, 0xFFE6E8ED);
        drawRect(exitX + 10, iconY + 9, exitX + 12, iconY + 11, 0xFFE6E8ED);
    }

    private void drawWrappedTextLimited(String text, int x, int y, int maxWidth, int color, int maxLines) {
        if (text == null || text.isEmpty() || maxLines <= 0) return;
        StringBuilder line = new StringBuilder();
        int lines = 0;
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fontRendererObj.getStringWidth(candidate) > maxWidth && line.length() > 0) {
                if (lines >= maxLines - 1) {
                    String last = line.toString();
                    while (last.length() > 3 && fontRendererObj.getStringWidth(last + "...") > maxWidth) last = last.substring(0, last.length() - 1);
                    drawString(fontRendererObj, last + "...", x, y, color);
                    return;
                }
                drawString(fontRendererObj, line.toString(), x, y, color);
                y += 10;
                lines++;
                line = new StringBuilder(word);
            } else line = new StringBuilder(candidate);
        }
        if (line.length() > 0 && lines < maxLines) {
            String last = line.toString();
            if (fontRendererObj.getStringWidth(last) > maxWidth) {
                while (last.length() > 3 && fontRendererObj.getStringWidth(last + "...") > maxWidth) last = last.substring(0, last.length() - 1);
                last += "...";
            }
            drawString(fontRendererObj, last, x, y, color);
        }
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
        if (button.id == EXIT) { mc.displayGuiScreen(null); return; }
        if (button.id == ACCOUNT) { mc.displayGuiScreen(new CommunityLoginGui(this)); return; }
        if (button.id == HUD_EDITOR) { mc.displayGuiScreen(new HudEditorGui(this)); return; }
        if (button.id == PROFILE_CREATE) {
            mc.displayGuiScreen(new ProfileNameGui(this, false, ""));
            return;
        }
        if (button.id == PROFILE_EDIT) {
            mc.displayGuiScreen(new ProfileNameGui(this, true, ModuleProfileManager.getSelectedProfileName()));
            return;
        }
        if (button.id == PROFILE_DELETE) {
            ModuleProfileManager.deleteSelectedProfile();
            initGui();
            return;
        }
        if (button.id >= PROFILE_SELECT_BASE && button.id < PROFILE_SELECT_BASE + 30) {
            List<ModuleProfileManager.BhopProfile> profiles = ModuleProfileManager.getProfiles();
            int index = profileScrollOffset + button.id - PROFILE_SELECT_BASE;
            if (index >= 0 && index < profiles.size()) {
                ModuleProfileManager.selectProfile(profiles.get(index).getProfileName());
            }
            initGui();
            return;
        }
        if (button.id == STAFF_MODS) {
            if (PermissionService.hasAccess(PermissionService.Rank.T_MOD)) {
                mc.displayGuiScreen(new StaffModsMenuGui(this));
            }
            return;
        }
        if (button.id >= CATEGORY_BASE
                && button.id < CATEGORY_BASE + AlphaModule.Category.values().length) {
            AlphaModule.Category category = AlphaModule.Category.values()[button.id - CATEGORY_BASE];
            if (PermissionService.hasAccess(category.getRequiredRank())) {
                selectedCategory = category;
                selectedModuleId = "";
            }
} else if (button.id >= MODULE_BASE) {
            List<AlphaModule> modules = getFilteredModules();
            int index = button.id - MODULE_BASE;
            if (index >= 0 && index < modules.size()) {
                AlphaModule clickedModule = modules.get(index);
                if ("quality".equals(clickedModule.getId())) {
                    searchFocused = false;
                    if (searchField != null) searchField.setFocused(false);
                    mc.displayGuiScreen(new QualityGui(this));
                    return;
                }
                if ("staffmods".equals(clickedModule.getId())) {
                    mc.displayGuiScreen(new StaffModsMenuGui(this));
                    return;
                }
                if (button instanceof ModuleTileButton && ((ModuleTileButton) button).wasSwitchClicked()) {
                    if (clickedModule.isImplemented() && clickedModule.isUnlocked()) {
                        clickedModule.setEnabled(!clickedModule.isEnabled());
                    }
                } else {
                    selectedModuleId = clickedModule.getId();
                    if ("name-viewer".equals(clickedModule.getId())
                            && clickedModule.isImplemented() && clickedModule.isUnlocked()) {
                        mc.displayGuiScreen(new NameViewerSettingsGui(this));
                        return;
                    }
                    if ("fps".equals(clickedModule.getId())
                            && clickedModule.isImplemented() && clickedModule.isUnlocked()) {
                        mc.displayGuiScreen(new FpsSettingsGui(this));
                        return;
                    }
                    if ("cps".equals(clickedModule.getId())
                            && clickedModule.isImplemented() && clickedModule.isUnlocked()) {
                        mc.displayGuiScreen(new CpsSettingsGui(this));
                        return;
                    }
                    if ("w-tap-assistance".equals(clickedModule.getId()) && clickedModule.isImplemented() && clickedModule.isUnlocked()) { mc.displayGuiScreen(new WTapSettingsGui(this)); return; }
                    if ("legacy-pvp".equals(clickedModule.getId())
                            && clickedModule.isImplemented() && clickedModule.isUnlocked()) {
                        mc.displayGuiScreen(new LegacyPvpSettingsGui(this));
                        return;
                    }
                    // Every module must open a settings screen, even when it has no specialized GUI yet.
                    mc.displayGuiScreen(new ModuleSettingsGui(this, clickedModule));
                    return;
                }
            }
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
            AlphaModule selected = getSelectedModule();
            if (selected != null && "quality".equals(selected.getId())) {
                List<ModuleProfileManager.BhopProfile> profiles = ModuleProfileManager.getProfiles();
                int profileTop = panelTop + 86;
                int actionY = panelTop + panelHeight - 48;
                int visibleProfiles = Math.max(1, (actionY - 8 - profileTop) / 21);
                int maxProfileOffset = Math.max(0, profiles.size() - visibleProfiles);
                profileScrollOffset = Math.max(0, Math.min(maxProfileOffset,
                        profileScrollOffset + (wheel < 0 ? 1 : -1)));
                initGui();
                return;
            }
            List<AlphaModule> modules = getFilteredModules();
            int maxOffset = Math.max(0, modules.size() - visibleRows() * 2);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + (wheel < 0 ? 2 : -2)));
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
