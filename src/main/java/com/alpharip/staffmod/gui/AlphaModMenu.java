// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.pvp.LegacyPvpFeatureCatalog;
import com.alpharip.staffmod.pvp.LegacyPvpSettings;
import com.alpharip.staffmod.pvp.LegacyPvpTransformer;
import com.alpharip.staffmod.founderfun.PixelGardenGUI;
import com.alpharip.staffmod.waypoints.WaypointsGui;
import com.alpharip.staffmod.singleplayer.worldedit.RogueWorldEditGui;
import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.ping.PingMod;
import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.keystrokes.KeystrokesMod;
import com.alpharip.staffmod.hud.InfoHudModules;
import com.alpharip.staffmod.hud.potionhudinfo.PotionHudInfoModule;
import com.alpharip.staffmod.hud.potionhudinfo.PotionHudInfoEffectSetting;
import net.minecraft.potion.Potion;
import com.alpharip.staffmod.ComboCounter.ComboCounter;
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
public final class AlphaModMenu extends AlphaGuiScreen {
    private static final int CATEGORY_BASE = 20;
    private static final int MODULE_BASE = 100;
    private static final int EXIT = 300;
    private static final int ACCOUNT = 301;
    private static final int SEARCH_ID = 304;
    private static final int HUD_EDITOR = 305;
    private static final int STAFF_MODS = 306;
    private static final int WAYPOINTS = 307;
    private static final int PROFILE_SELECT_BASE = 400;
    private static final int PROFILE_CREATE = 430;
    private static final int PROFILE_EDIT = 431;
    private static final int PROFILE_DELETE = 432;
    private static final int DONATOR_SUBFOLDER_BASE = 500;
    private static final int SETTINGS_BASE = 600;
    private static final int SETTINGS_BACK = 308, SETTINGS_PREVIOUS = 309;
    private static final net.minecraft.util.ResourceLocation ALPHA_LOGO =
            new net.minecraft.util.ResourceLocation("alphastaffmod", "textures/gui/alpha_client_logo.png");
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private AlphaModule.Category selectedCategory = AlphaModule.Category.PUBLIC;
    private String selectedModuleId = "staffmods";
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int sidebarWidth, contentLeft, contentRight, contentTop, contentBottom;
    private int settingsLeft;
    private int scrollOffset;
    private int categoryScrollOffset;
    private int profileScrollOffset;
    private GuiTextField searchField;
    private String searchText = "";
    private boolean searchFocused;
    private String accessFingerprint = "";
    private String settingsSubpage = "";
    private GuiTextField[] keystrokesColorFields = new GuiTextField[5];
    private static final int COLOR_SAVE = 701;
    private static final int COLOR_BACK = 702;
    private static final int LEGACY_DETAIL_BACK = 721;
    private static final int LEGACY_TOGGLE = 722;
    private static final int LEGACY_OPEN = 723;
    private static final int LEGACY_FEATURE_BASE = 730;
    private String legacySelectedFeatureId = "";
    private static final int POTION_LIST_BASE = 820, POTION_PAGE_PREV = 830, POTION_PAGE_NEXT = 831, POTION_MANAGER_BACK = 835;
    private static final int POTION_TOGGLE = 832, POTION_EDIT = 833, POTION_IMPORT = 834;
    private static final int POTION_EDITOR_BACK = 840, POTION_EDITOR_SAVE = 841;
    private static final int POTION_NAME_R_DOWN = 850, POTION_NAME_G_DOWN = 851, POTION_NAME_B_DOWN = 852;
    private static final int POTION_TIMER_R_DOWN = 853, POTION_TIMER_G_DOWN = 854, POTION_TIMER_B_DOWN = 855;
    private static final int POTION_NAME_R_UP = 856, POTION_NAME_G_UP = 857, POTION_NAME_B_UP = 858;
    private static final int POTION_TIMER_R_UP = 859, POTION_TIMER_G_UP = 860, POTION_TIMER_B_UP = 861;
    private static final int POTION_CHROMA_NAME = 862, POTION_CHROMA_TIMER = 863, POTION_BOLD = 864;
    private static final int POTION_ITALIC = 865, POTION_UNDERLINE = 866, POTION_FLICKER = 867;
    private static final int POTION_THRESHOLD_DOWN = 868, POTION_THRESHOLD_UP = 869;
    private static final int POTION_SPEED_DOWN = 870, POTION_SPEED_UP = 871;
    private static final int POTION_IMPORT_PREV = 880, POTION_IMPORT_NEXT = 881, POTION_IMPORT_COPY = 882, POTION_IMPORT_BACK = 883;
    private static final int POTION_PAGE_SIZE = 7;
    private final List<Integer> potionEffectIds = new ArrayList<Integer>();
    private final List<Integer> potionImportSourceIds = new ArrayList<Integer>();
    private int potionEffectIndex, potionEffectPage, potionImportSourceIndex;
    private GuiTextField potionEffectNameField;

    public AlphaModMenu(GuiScreen parent) { this(parent, AlphaModule.Category.PUBLIC); }

    public AlphaModMenu(GuiScreen parent, AlphaModule.Category initialCategory) {
        this.parent = parent;
        this.selectedCategory = initialCategory == null ? AlphaModule.Category.PUBLIC : initialCategory;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if ("keystrokes-colors".equals(settingsSubpage)) Keyboard.enableRepeatEvents(true);
        panelWidth = Math.min(460, Math.max(280, width - 20));
        panelHeight = Math.min(300, Math.max(210, height - 24));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        sidebarWidth = 70;
        contentLeft = panelLeft + sidebarWidth + 10;
        contentRight = panelLeft + panelWidth - 112;
        contentTop = panelTop + 49;
        contentBottom = panelTop + panelHeight - 25;
        settingsLeft = isModuleSettingsOpen() ? contentLeft : contentRight + 8;

        searchField = new GuiTextField(SEARCH_ID, fontRendererObj,
                contentLeft + 3, panelTop + 25, Math.max(80, contentRight - contentLeft - 6), 15);
        searchField.setMaxStringLength(48);
        searchField.setText(searchText);
        searchField.setFocused(searchFocused);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setTextColor(0xFFE7E9EE);
        searchField.setDisabledTextColour(0xFFB8BDC8);
        buttonList.add(new AlphaMenuButton(ACCOUNT, panelLeft + panelWidth - 43, panelTop + 5, 16, 16, ""));
        buttonList.add(new AlphaMenuButton(EXIT, panelLeft + panelWidth - 23, panelTop + 5, 16, 16, ""));
        // Keep Waypoints and the HUD editor permanently available at the bottom of the sidebar.
        buttonList.add(new AlphaMenuButton(WAYPOINTS, panelLeft + 5, panelTop + panelHeight - 59, 60, 18, "WAYPOINTS", true));
        buttonList.add(new AlphaMenuButton(HUD_EDITOR, panelLeft + 5, panelTop + panelHeight - 37, 60, 18, "HUD EDITOR"));

        List<AlphaModule.Category> sidebarCategories = new ArrayList<AlphaModule.Category>();
        for (AlphaModule.Category category : AlphaModule.Category.values()) {
            // Staff rank folders belong exclusively inside StaffMods, not in the regular Mods sidebar.
            if (isStaffRankCategory(category)
                    || isDonatorRankCategory(category)) continue;
            boolean categoryAllowed = category == AlphaModule.Category.DONATOR
                    ? hasAnyUnlockedDonatorFolder()
                    : PermissionService.hasAccess(category.getRequiredRank());
            if (!categoryAllowed) {
                categoryAllowed = hasUnlockedModuleInCategory(category);
            }
            if (categoryAllowed) sidebarCategories.add(category);
        }
        boolean showStaffMods = PermissionService.hasStaffModsAccess();
        int categoryY = panelTop + 68;
        int categoryBottom = panelTop + panelHeight - 43;
        int visibleCategorySlots = Math.max(1, (categoryBottom - categoryY) / 23);
        int sidebarItemCount = sidebarCategories.size() + (showStaffMods ? 1 : 0);
        int maxCategoryScroll = Math.max(0, sidebarItemCount - visibleCategorySlots);
        categoryScrollOffset = Math.max(0, Math.min(categoryScrollOffset, maxCategoryScroll));
        for (int i = categoryScrollOffset; i < sidebarCategories.size()
                && i < categoryScrollOffset + visibleCategorySlots; i++) {
            AlphaModule.Category category = sidebarCategories.get(i);
            String label;
            switch (category) {
                case PUBLIC: label = "GENERAL"; break;
                case BETA: label = "BETA"; break;
                case DONATOR: label = "Donator"; break;
                case T_MOD: label = "T-MOD"; break;
                case MOD: label = "MOD"; break;
                case SR_MOD: label = "SR-MOD"; break;
                case ADMIN: label = "ADMIN"; break;
                case SR_ADMIN: label = "SR-ADMIN"; break;
                case MANAGEMENT: label = "MANAGEMENT"; break;
                case PLAT_ADMIN: label = "PLAT-ADMIN"; break;
                case MANAGER: label = "MANAGER"; break;
                case OWNER: label = "OWNER"; break;
                case FOUNDER: label = "FOUNDER"; break;
                default: continue;
            }
            buttonList.add(new AlphaMenuButton(CATEGORY_BASE + category.ordinal(),
                    panelLeft + 5, categoryY, sidebarWidth - 10, 20,
                    label, category == selectedCategory));
            categoryY += 23;
        }
        if (showStaffMods && sidebarCategories.size() >= categoryScrollOffset
                && sidebarCategories.size() < categoryScrollOffset + visibleCategorySlots) {
            int staffY = panelTop + 68 + (sidebarCategories.size() - categoryScrollOffset) * 23;
            buttonList.add(new AlphaMenuButton(STAFF_MODS, panelLeft + 5, staffY,
                    sidebarWidth - 10, 20, "STAFFMODS"));
        }
        if (selectedCategory == AlphaModule.Category.DONATOR) {
            AlphaModule.Category[] donorCategories = {
                AlphaModule.Category.SCOUT, AlphaModule.Category.ROGUE, AlphaModule.Category.ELITE,
                AlphaModule.Category.ALPHA, AlphaModule.Category.ALPHA_PLUS
            };
            int gap = 8;
            int tileWidth = Math.max(50, (contentRight - contentLeft - gap * 2) / 3);
            int tileHeight = 38;
            for (int i = 0; i < donorCategories.length; i++) {
                AlphaModule.Category donorCategory = donorCategories[i];
                // Rank access or an explicit grant for at least one module unlocks this folder.
                if (!hasDonatorFolderAccess(donorCategory)) continue;
                int x = contentLeft + (i % 3) * (tileWidth + gap);
                int y = contentTop + (i / 3) * (tileHeight + gap);
                buttonList.add(new AlphaMenuButton(DONATOR_SUBFOLDER_BASE + i, x, y,
                        tileWidth, tileHeight, donorCategory.getTitle()));
            }
        }

        if (!isModuleSettingsOpen()) {
            List<ModuleProfileManager.BhopProfile> profiles = ModuleProfileManager.getProfiles();
            int actionY = panelTop + panelHeight - 49;
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
                buttonList.add(new AlphaMenuButton(PROFILE_SELECT_BASE + row, settingsLeft + 4,
                        profileTop + row * 21, Math.max(50, panelLeft + panelWidth - settingsLeft - 10),
                        18, profile.getProfileName(),
                        profile.getProfileName().equalsIgnoreCase(ModuleProfileManager.getSelectedProfileName())));
            }
            buttonList.add(new AlphaMenuButton(PROFILE_CREATE, actionsLeft, actionY, actionWidth, 18, "CREATE"));
            buttonList.add(new AlphaMenuButton(PROFILE_EDIT, actionsLeft + actionWidth + actionGap, actionY, actionWidth, 18, "EDIT"));
            buttonList.add(new AlphaMenuButton(PROFILE_DELETE, actionsLeft + (actionWidth + actionGap) * 2, actionY, actionWidth, 18, "DELETE"));
        }

        if (isModuleSettingsOpen()) addModuleSettingsButtons();

        if (!isModuleSettingsOpen()) {
            List<AlphaModule> filtered = getFilteredModules();
            int gridWidth = Math.max(180, contentRight - contentLeft);
            int gap = 10;
            int tileWidth = (gridWidth - gap) / 2;
            int tileHeight = 62;
            int rowGap = 9;
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
        }

        accessFingerprint = buildAccessFingerprint();
    }

    private String buildAccessFingerprint() {
        StringBuilder fingerprint = new StringBuilder(PermissionService.getEffectiveRank().name());
        fingerprint.append('|').append(PermissionService.getVerifiedOnlineRanks());
        fingerprint.append('|').append(PermissionService.areModEntitlementsVerified());
        fingerprint.append("|singleplayer=").append(hasActiveSingleplayerWorld());
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

    private boolean hasActiveSingleplayerWorld() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft != null && minecraft.isSingleplayer()
                && minecraft.thePlayer != null && minecraft.theWorld != null
                && minecraft.getIntegratedServer() != null;
    }

    private boolean hasBhopAccess() {
        AlphaModule bhop = AlphaModuleRegistry.find("staffmods");
        return bhop != null && bhop.isUnlocked();
    }

    private boolean isDonatorRankCategory(AlphaModule.Category category) {
        return category == AlphaModule.Category.SCOUT
                || category == AlphaModule.Category.ROGUE
                || category == AlphaModule.Category.ELITE
                || category == AlphaModule.Category.ALPHA
                || category == AlphaModule.Category.ALPHA_PLUS;
    }

    private boolean hasUnlockedModuleInCategory(AlphaModule.Category category) {
        for (AlphaModule module : AlphaModuleRegistry.getForCategory(category)) {
            if (!"staffmods".equals(module.getId()) && module.isUnlocked()) return true;
        }
        return false;
    }

    private boolean hasDonatorFolderAccess(AlphaModule.Category category) {
        return PermissionService.hasAccess(category.getRequiredRank())
                || hasUnlockedModuleInCategory(category);
    }

    private boolean hasAnyUnlockedDonatorFolder() {
        AlphaModule.Category[] donorCategories = {
            AlphaModule.Category.SCOUT, AlphaModule.Category.ROGUE, AlphaModule.Category.ELITE,
            AlphaModule.Category.ALPHA, AlphaModule.Category.ALPHA_PLUS
        };
        for (AlphaModule.Category category : donorCategories) {
            if (hasDonatorFolderAccess(category)) return true;
        }
        return false;
    }

    private boolean isStaffRankCategory(AlphaModule.Category category) {
        switch (category) {
            case T_MOD:
            case MOD:
            case SR_MOD:
            case ADMIN:
            case SR_ADMIN:
            case MANAGEMENT:
            case PLAT_ADMIN:
            case MANAGER:
            case OWNER:
            case FOUNDER:
                return true;
            default:
                return false;
        }
    }

    private List<AlphaModule> getFilteredModules() {
        List<AlphaModule> result = new ArrayList<AlphaModule>();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        for (AlphaModule module : AlphaModuleRegistry.getForCategory(selectedCategory)) {
            // The Singleplayer folder is only available inside an active integrated-server world.
            if ("singleplayer".equals(module.getId()) && !hasActiveSingleplayerWorld()) continue;
            // World Edit is managed only from the Singleplayer -> World Edit screen.
            if ("world-edit".equals(module.getId())) continue;
            // Bhop is exclusive to STAFFMODS -> GENERAL, never a rank-category module.
            if ("staffmods".equals(module.getId())) continue;
            // Do not render locked modules; visibility must match the effective permission result.
            if (!module.isUnlocked()) continue;
            if (query.isEmpty() || module.getName().toLowerCase(Locale.ROOT).contains(query)
                    || module.getDescription().toLowerCase(Locale.ROOT).contains(query)) result.add(module);
        }

        // Keep the dedicated General folders in a fixed order at the end of the list.
        if (selectedCategory == AlphaModule.Category.PUBLIC) {
            AlphaModule singleplayer = null;
            AlphaModule misc = null;
            AlphaModule quality = null;
            for (int i = result.size() - 1; i >= 0; i--) {
                String id = result.get(i).getId();
                if ("quality".equals(id)) {
                    quality = result.remove(i);
                } else if ("misc".equals(id)) {
                    misc = result.remove(i);
                } else if ("singleplayer".equals(id)) {
                    singleplayer = result.remove(i);
                }
            }
            if (singleplayer != null) result.add(singleplayer);
            if (misc != null) result.add(misc);
            if (quality != null) result.add(quality);
        }
        return result;
    }

    private int visibleRows() {
        // Keep this in sync with the regular module tile layout in initGui().
        // Each row is 62px tall with a 9px gap.
        return Math.max(1, (contentBottom - contentTop) / 71);
    }

    private AlphaModule getSelectedModule() {
        // Scout is a folder view: never auto-select Pixel Garden in its details panel.
        // Pixel Garden opens only through an explicit click on its module tile.
        if (selectedCategory == AlphaModule.Category.SCOUT) return null;
        AlphaModule module = AlphaModuleRegistry.find(selectedModuleId);
        if ("singleplayer".equals(selectedModuleId) && !hasActiveSingleplayerWorld()) return null;
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
        // Keep the Minecraft world visible behind a softly rounded, isolated menu surface.
        drawRect(0, 0, width, height, 0x18000000);
        int right = panelLeft + panelWidth, bottom = panelTop + panelHeight;
        AlphaMenuRender.roundedRect(panelLeft + 2, panelTop + 3, right + 2, bottom + 3, 11, 0x55000000);
        AlphaMenuRender.roundedRect(panelLeft, panelTop, right, bottom, 11, 0xF20D1017);
        AlphaMenuRender.roundedOutline(panelLeft, panelTop, right, bottom, 11, 0x99515A68);
        AlphaMenuRender.roundedRect(panelLeft + 5, panelTop + 5, panelLeft + sidebarWidth - 5, bottom - 5, 8, 0xA9151922);
        AlphaMenuRender.roundedRect(panelLeft + sidebarWidth + 5, panelTop + 8,
                panelLeft + sidebarWidth + 6, bottom - 8, 1, 0x66515A68);
        if (isModuleSettingsOpen()) {
            AlphaMenuRender.roundedRect(contentLeft, panelTop + 26, right - 9, bottom - 9, 8, 0x55191D26);
        } else {
            AlphaMenuRender.roundedRect(settingsLeft, panelTop + 29, right - 9, bottom - 9, 8, 0x55191D26);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(ALPHA_LOGO);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawScaledCustomSizeModalRect(panelLeft + 39, panelTop + 8, 0.0F, 0.0F, 1024, 1024, 34, 34, 1024.0F, 1024.0F);
        drawString(fontRendererObj, "MODS", panelLeft + 38, panelTop + 45, 0xFFF0F1F4);
        if (isModuleSettingsOpen()) {
            AlphaModule activeModule = AlphaModuleRegistry.find(selectedModuleId);
            String moduleName = activeModule == null ? "Module" : activeModule.getName();
            drawString(fontRendererObj, "Mods  ›  " + selectedCategory.getTitle()
                    + "  ›  " + moduleName, contentLeft + 52, panelTop + 10, 0xFFB8BDC8);
        } else {
            drawString(fontRendererObj, AlphaModuleRegistry.getForCategory(selectedCategory).size()
                    + " modules", contentLeft + 4, panelTop + 8, 0xFFB8BDC8);
            AlphaMenuRender.roundedRect(contentLeft + 2, panelTop + 24,
                    contentRight - 2, panelTop + 42, 5, 0xAA202630);
            searchField.drawTextBox();
            drawString(fontRendererObj, "BROWSE", panelLeft + 38, panelTop + 61, 0xFFB8BDC8);
            drawString(fontRendererObj,
                    selectedCategory == AlphaModule.Category.DONATOR
                            ? "Donator ranks" : selectedCategory.getTitle().toUpperCase(Locale.ROOT),
                    contentLeft + 3, contentTop - 9, ACCENT);
        }
        if (isModuleSettingsOpen()) {
            drawModuleSettingsPanel(mouseX, mouseY, partialTicks);
        } else {
        drawString(fontRendererObj, "PROFILES",
                settingsLeft + 12, panelTop + 42, 0xFFB8BDC8);
        List<ModuleProfileManager.BhopProfile> profiles = ModuleProfileManager.getProfiles();
        String activeProfile = ModuleProfileManager.getSelectedProfileName();
        int profileTop = panelTop + 67;
        int actionY = panelTop + panelHeight - 49;
        int profileBottom = actionY - 8;
        int visibleProfiles = Math.max(1, (profileBottom - profileTop) / 21);
        drawString(fontRendererObj, "Select a profile", settingsLeft + 12, panelTop + 58, 0xFFF3F4F6);
        if (profiles.isEmpty()) {
            drawString(fontRendererObj, "No profiles available.", settingsLeft + 12, profileTop + 4, 0xFFB8BDC8);
        } else {
            if (hasBhopAccess()) {
                drawString(fontRendererObj, "Active: " + activeProfile, settingsLeft + 12,
                        panelTop + panelHeight - 27, 0xFF9CE2AC);
            }
            if (profiles.size() > visibleProfiles) {
                drawString(fontRendererObj, "Scroll for more (" + (profileScrollOffset + 1) + "-"
                        + Math.min(profiles.size(), profileScrollOffset + visibleProfiles) + "/" + profiles.size() + ")",
                        settingsLeft + 7, panelTop + 68, 0xFFB8BDC8);
            }
        }
        // Keep the community rank directly beneath the fixed Profiles panel.
        // Community Login and Exit remain as the existing top-right icon buttons.
        String rankText = "Alpha Rank: " + (SupabaseCommunityService.isSignedIn()
                ? SupabaseCommunityService.getSignedInRankLabel() : "NO RANK");
        int rankMaxWidth = Math.max(24, panelLeft + panelWidth - 14 - settingsLeft);
        while (rankText.length() > 1 && fontRendererObj.getStringWidth(rankText) > rankMaxWidth) {
            rankText = rankText.substring(0, rankText.length() - 1);
        }
        drawString(fontRendererObj, rankText, settingsLeft + 12, bottom - 19,
                SupabaseCommunityService.isSignedIn() ? ACCENT : 0xFFB8BDC8);
        }
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


    private boolean isModuleSettingsOpen() {
        return selectedModuleId != null && !selectedModuleId.isEmpty()
                && !"staffmods".equals(selectedModuleId);
    }

    private void addModuleSettingsButtons() {
        List<AlphaModule> navigationModules = getFilteredModules();
        int selectedIndex = -1;
        for (int i = 0; i < navigationModules.size(); i++) {
            if (navigationModules.get(i).getId().equals(selectedModuleId)) {
                selectedIndex = i;
                break;
            }
        }
        AlphaMenuButton previousModule = new AlphaMenuButton(SETTINGS_PREVIOUS,
                contentLeft + 8, panelTop + 7, 17, 17, "←");
        previousModule.enabled = selectedIndex > 0;
        buttonList.add(previousModule);
        buttonList.add(new AlphaMenuButton(SETTINGS_BACK,
                contentLeft + 28, panelTop + 7, 17, 17, "→"));

        if ("potion-effects".equals(settingsSubpage)) {
            int x = settingsLeft + 8, y = panelTop + 86;
            int w = Math.max(90, panelLeft + panelWidth - x - 12);
            buttonList.add(new AlphaMenuButton(POTION_MANAGER_BACK, x, panelTop + 52, w, 19, "BACK TO POTION HUD"));
            int listWidth = Math.max(86, (w - 8) / 2);
            for (int slot = 0; slot < POTION_PAGE_SIZE; slot++) {
                int index = potionEffectPage * POTION_PAGE_SIZE + slot;
                if (index >= potionEffectIds.size()) break;
                int effectId = potionEffectIds.get(index).intValue();
                PotionHudInfoEffectSetting setting = PotionHudInfoModule.getInstance().getEffect(effectId);
                String name = PotionHudInfoModule.getPotionName(effectId);
                if (name.length() > 16) name = name.substring(0, 15) + "…";
                buttonList.add(new AlphaMenuButton(POTION_LIST_BASE + slot, x, y + slot * 23, listWidth, 20,
                        name + (setting != null && setting.isEnabled() ? "  ON" : "  OFF"), false, index == potionEffectIndex));
            }
            int pageButtonY = panelTop + 263;
            buttonList.add(new AlphaMenuButton(POTION_PAGE_PREV, x, pageButtonY, 55, 18, "PREV", false, potionEffectPage > 0));
            buttonList.add(new AlphaMenuButton(POTION_PAGE_NEXT, x + 60, pageButtonY, 55, 18, "NEXT", false,
                    (potionEffectPage + 1) * POTION_PAGE_SIZE < potionEffectIds.size()));
            int actionX = x + listWidth + 8, actionW = w - listWidth - 8;
            buttonList.add(new AlphaMenuButton(POTION_TOGGLE, actionX, panelTop + 130, actionW, 20,
                    getSelectedPotionEffect() != null && getSelectedPotionEffect().isEnabled() ? "DISABLE" : "ENABLE"));
            buttonList.add(new AlphaMenuButton(POTION_EDIT, actionX, panelTop + 158, actionW, 20, "CUSTOMIZE"));
            buttonList.add(new AlphaMenuButton(POTION_IMPORT, actionX, panelTop + 186, actionW, 20, "COPY SETTINGS"));
            return;
        }
        if ("potion-effect-edit".equals(settingsSubpage)) {
            int x = settingsLeft + 8, w = Math.max(90, panelLeft + panelWidth - x - 12);
            int leftX = x + 2, rightX = x + w / 2 + 2;
            int[] down = {POTION_NAME_R_DOWN, POTION_NAME_G_DOWN, POTION_NAME_B_DOWN};
            int[] up = {POTION_NAME_R_UP, POTION_NAME_G_UP, POTION_NAME_B_UP};
            int[] timerDown = {POTION_TIMER_R_DOWN, POTION_TIMER_G_DOWN, POTION_TIMER_B_DOWN};
            int[] timerUp = {POTION_TIMER_R_UP, POTION_TIMER_G_UP, POTION_TIMER_B_UP};
            for (int i = 0; i < 3; i++) {
                int y = panelTop + 90 + i * 22;
                buttonList.add(new AlphaMenuButton(down[i], leftX, y, 22, 18, "-"));
                buttonList.add(new AlphaMenuButton(up[i], leftX + 92, y, 22, 18, "+"));
                buttonList.add(new AlphaMenuButton(timerDown[i], rightX, y, 22, 18, "-"));
                buttonList.add(new AlphaMenuButton(timerUp[i], rightX + 92, y, 22, 18, "+"));
            }
            int half = (w - 6) / 2;
            buttonList.add(new AlphaMenuButton(POTION_CHROMA_NAME, x, panelTop + 158, half, 17, "CHROMA NAME: " + (getSelectedPotionEffect() != null && getSelectedPotionEffect().isChromaName() ? "ON" : "OFF")));
            buttonList.add(new AlphaMenuButton(POTION_CHROMA_TIMER, x + half + 6, panelTop + 158, half, 17, "CHROMA TIMER: " + (getSelectedPotionEffect() != null && getSelectedPotionEffect().isChromaTimer() ? "ON" : "OFF")));
            buttonList.add(new AlphaMenuButton(POTION_BOLD, x, panelTop + 177, half, 17, "BOLD: " + (getSelectedPotionEffect() != null && getSelectedPotionEffect().isBold() ? "ON" : "OFF")));
            buttonList.add(new AlphaMenuButton(POTION_ITALIC, x + half + 6, panelTop + 177, half, 17, "ITALIC: " + (getSelectedPotionEffect() != null && getSelectedPotionEffect().isItalic() ? "ON" : "OFF")));
            buttonList.add(new AlphaMenuButton(POTION_UNDERLINE, x, panelTop + 196, half, 17, "UNDERLINE: " + (getSelectedPotionEffect() != null && getSelectedPotionEffect().isUnderlined() ? "ON" : "OFF")));
            buttonList.add(new AlphaMenuButton(POTION_FLICKER, x + half + 6, panelTop + 196, half, 17, "FLICKER: " + (getSelectedPotionEffect() != null && getSelectedPotionEffect().isFlickerWhenLow() ? "ON" : "OFF")));
            buttonList.add(new AlphaMenuButton(POTION_THRESHOLD_DOWN, x, panelTop + 220, 22, 16, "-"));
            buttonList.add(new AlphaMenuButton(POTION_THRESHOLD_UP, x + half - 22, panelTop + 220, 22, 16, "+"));
            buttonList.add(new AlphaMenuButton(POTION_SPEED_DOWN, x + half + 6, panelTop + 220, 22, 16, "-"));
            buttonList.add(new AlphaMenuButton(POTION_SPEED_UP, x + w - 22, panelTop + 220, 22, 16, "+"));
            potionEffectNameField = new GuiTextField(884, fontRendererObj, x + 2, panelTop + 250, w - 4, 17);
            potionEffectNameField.setMaxStringLength(64);
            PotionHudInfoEffectSetting selected = getSelectedPotionEffect();
            potionEffectNameField.setText(selected == null ? "" : selected.getCustomName());
            buttonList.add(new AlphaMenuButton(POTION_EDITOR_SAVE, x, panelTop + 272, half, 16, "SAVE"));
            buttonList.add(new AlphaMenuButton(POTION_EDITOR_BACK, x + half + 6, panelTop + 272, half, 16, "BACK"));
            return;
        }
        if ("potion-effect-import".equals(settingsSubpage)) {
            int x = settingsLeft + 8, w = Math.max(90, panelLeft + panelWidth - x - 12);
            buttonList.add(new AlphaMenuButton(POTION_IMPORT_PREV, x, panelTop + 160, 55, 18, "PREV", false, potionImportSourceIndex > 0));
            buttonList.add(new AlphaMenuButton(POTION_IMPORT_NEXT, x + w - 55, panelTop + 160, 55, 18, "NEXT", false, potionImportSourceIndex < potionImportSourceIds.size() - 1));
            buttonList.add(new AlphaMenuButton(POTION_IMPORT_COPY, x, panelTop + 263, (w - 6) / 2, 18, "COPY"));
            buttonList.add(new AlphaMenuButton(POTION_IMPORT_BACK, x + (w - 6) / 2 + 6, panelTop + 263, (w - 6) / 2, 18, "BACK"));
            return;
        }
        if ("legacy-list".equals(settingsSubpage)) {
            int x = settingsLeft + 8;
            int y = panelTop + 82;
            int totalWidth = Math.max(90, panelLeft + panelWidth - x - 12);
            int columnGap = 6;
            int buttonWidth = (totalWidth - columnGap) / 2;
            int rowSpacing = 23;
            int buttonHeight = 18;
            LegacyPvpSettings.load();
            LegacyPvpFeatureCatalog.Feature[] features = LegacyPvpFeatureCatalog.getAll();
            for (int i = 0; i < features.length; i++) {
                int column = i % 2;
                int row = i / 2;
                int buttonX = x + column * (buttonWidth + columnGap);
                buttonList.add(new AlphaMenuButton(LEGACY_FEATURE_BASE + i, buttonX,
                        y + row * rowSpacing, buttonWidth, buttonHeight, features[i].label));
            }
            return;
        }
        if ("legacy-detail".equals(settingsSubpage)) {
            int x = settingsLeft + 8;
            int y = panelTop + 52;
            buttonList.add(new AlphaMenuButton(LEGACY_DETAIL_BACK, x, y, 116, 18, "BACK TO LEGACY PVP"));
            LegacyPvpFeatureCatalog.Feature feature = getLegacySelectedFeature();
            if (feature != null && isLegacyToggleAvailable(feature.id)) {
                buttonList.add(new AlphaMenuButton(LEGACY_TOGGLE, x, panelTop + 263, 104, 18,
                        getLegacyToggleLabel(feature.id)));
            }
            return;
        }
        if ("keystrokes-colors".equals(settingsSubpage)) {
            int x = settingsLeft + 8;
            int y = panelTop + 254;
            int w = Math.max(90, panelLeft + panelWidth - x - 12);
            buttonList.add(new AlphaMenuButton(COLOR_SAVE, x, y, (w - 6) / 2, 19, "SAVE COLORS"));
            buttonList.add(new AlphaMenuButton(COLOR_BACK, x + (w - 6) / 2 + 6, y, (w - 6) / 2, 19, "CANCEL"));
            int[] colors = {KeystrokesMod.getBackgroundColor(), KeystrokesMod.getPressedBackgroundColor(),
                    KeystrokesMod.getTextColor(), KeystrokesMod.getPressedTextColor(), KeystrokesMod.getBorderColor()};
            for (int i = 0; i < keystrokesColorFields.length; i++) {
                keystrokesColorFields[i] = new GuiTextField(i, fontRendererObj, x + 106, panelTop + 90 + i * 28, w - 112, 18);
                keystrokesColorFields[i].setMaxStringLength(9);
                keystrokesColorFields[i].setText(String.format(Locale.ROOT, "%08X", colors[i]));
                keystrokesColorFields[i].setTextColor(0xFFE7E9EE);
                keystrokesColorFields[i].setDisabledTextColour(0xFF9DA4B1);
            }
            return;
        }
        AlphaModule module = AlphaModuleRegistry.find(selectedModuleId);
        int x = settingsLeft + 8;
        int y = panelTop + 82;
        int w = Math.max(90, panelLeft + panelWidth - x - 12);
        if (module == null || !module.isUnlocked() || !module.isImplemented()) return;
        buttonList.add(new AlphaMenuButton(SETTINGS_BASE, x, y, w, 20,
                module.isEnabled() ? "MODULE: ON" : "MODULE: OFF", module.isEnabled()));
        y += 25;
        String id = module.getId();
        if ("fps".equals(id)) {
            addSettingButton(1,x,y,w,"HUD BACKGROUND: "+onOff(FpsMod.isShowBackground())); y+=24;
            addSettingButton(2,x,y,w,"TEXT COLOR: "+FpsMod.getColorName());
        } else if ("ping".equals(id)) {
            addSettingButton(1,x,y,w,"HUD BACKGROUND: "+onOff(PingMod.isShowBackground())); y+=24;
            addSettingButton(2,x,y,w,"TEXT COLOR: "+PingMod.getColorName());
        } else if ("cps".equals(id)) {
            addSettingButton(1,x,y,w,"HUD BACKGROUND: "+onOff(CpsMod.isShowBackground())); y+=24;
            addSettingButton(2,x,y,w,"TEXT COLOR: "+CpsMod.getColorName()); y+=24;
            addSettingButton(3,x,y,w,"DISPLAY: "+CpsMod.getDisplayModeName());
        } else if ("combo-counter".equals(id)) {
            addSettingButton(1,x,y,w,"HUD BACKGROUND: "+onOff(ComboCounter.getInstance().isShowBackground())); y+=24;
            addSettingButton(2,x,y,w,"TEXT COLOR: "+ComboCounter.getInstance().getColorName());
        } else if ("armor-hud-info".equals(id)) {
            addSettingButton(30,x,y,w,"ITEM NAMES: "+onOff(InfoHudModules.getArmorShowNames())); y+=24;
            addSettingButton(31,x,y,w,"DURABILITY: "+onOff(InfoHudModules.getArmorShowDurability())); y+=24;
            addSettingButton(32,x,y,w,"LAYOUT: "+InfoHudModules.getArmorLayoutName()); y+=24;
            addSettingButton(33,x,y,w,"HUD COLOR: "+InfoHudModules.getArmorColorName());
        } else if ("potion-hud-info".equals(id)) {
            PotionHudInfoModule p=PotionHudInfoModule.getInstance(); p.load();
            y = panelTop + 106;
            addCompactSettingButton(40,x,y,w,"POTION ICONS: "+onOff(p.isRenderIcons())); y+=20;
            addCompactSettingButton(41,x,y,w,"AMPLIFIER LEVELS ABOVE IV: "+onOff(p.isRenderLevelsAbove4())); y+=20;
            addCompactSettingButton(42,x,y,w,"SHOW WHILE CHAT IS OPEN: "+onOff(p.isShowWhileChat())); y+=20;
            addCompactSettingButton(43,x,y,w,"ROMAN NUMERAL LEVELS: "+onOff(p.isRomanNumerals())); y+=20;
            addCompactSettingButton(44,x,y,w/2,"OPACITY -"); addCompactSettingButton(45,x+w/2,y,w-w/2,"OPACITY +"); y+=20;
            addCompactSettingButton(46,x,y,w/2,"SCALE -"); addCompactSettingButton(47,x+w/2,y,w-w/2,"SCALE +"); y+=20;
            addCompactSettingButton(48,x,y,w,"MANAGE POTION EFFECTS");
        } else if ("keystrokes".equals(id)) {
            y = panelTop + 105;
            addCompactSettingButton(10,x,y,w,"SHOW CLICKS: "+onOff(KeystrokesMod.isShowClicks())); y+=17;
            addCompactSettingButton(11,x,y,w,"SHOW LMB/RMB CPS: "+onOff(KeystrokesMod.isShowCps())); y+=17;
            addCompactSettingButton(12,x,y,w,"SHOW SPACE BAR: "+onOff(KeystrokesMod.isShowSpaceBar())); y+=17;
            addCompactSettingButton(13,x,y,w,"SHOW MOVEMENT KEYS: "+onOff(KeystrokesMod.isShowMovementKeys())); y+=17;
            addCompactSettingButton(14,x,y,w,"REPLACE WITH ARROWS: "+onOff(KeystrokesMod.isUseArrows())); y+=17;
            addCompactSettingButton(15,x,y,w,"OUTER BORDER: "+onOff(KeystrokesMod.isBorder())); y+=17;
            addCompactSettingButton(16,x,y,w,"TEXT SHADOW: "+onOff(KeystrokesMod.isTextShadow())); y+=17;
            addCompactSettingButton(17,x,y,w/2,"FADE - ("+KeystrokesMod.getFadeDelay()+" ms)");
            addCompactSettingButton(18,x+w/2,y,w-w/2,"FADE + ("+KeystrokesMod.getFadeDelay()+" ms)"); y+=17;
            addCompactSettingButton(19,x,y,w/2,"BOX - ("+KeystrokesMod.getBoxSize()+")");
            addCompactSettingButton(20,x+w/2,y,w-w/2,"BOX + ("+KeystrokesMod.getBoxSize()+")"); y+=17;
            addCompactSettingButton(21,x,y,w/2,"BORDER - ("+KeystrokesMod.getBorderThickness()+")");
            addCompactSettingButton(22,x+w/2,y,w-w/2,"BORDER + ("+KeystrokesMod.getBorderThickness()+")"); y+=17;
            addCompactSettingButton(23,x,y,w,"CUSTOMIZE COLORS");
        } else if ("legacy-pvp".equals(id)) {
            buttonList.add(new AlphaMenuButton(LEGACY_OPEN, x, y, w, 19, "OPEN LEGACY PVP FEATURES"));
        }
    }

    private void addSettingButton(int id,int x,int y,int width,String label) {
        buttonList.add(new AlphaMenuButton(SETTINGS_BASE+id,x,y,width,19,label));
    }
    private void addCompactSettingButton(int id,int x,int y,int width,String label) {
        buttonList.add(new AlphaMenuButton(SETTINGS_BASE+id,x,y,width,16,label));
    }
    private String onOff(boolean value) { return value ? "ON" : "OFF"; }

    private void drawModuleSettingsPanel(int mouseX,int mouseY,float partialTicks) {
        AlphaModule module=AlphaModuleRegistry.find(selectedModuleId);
        int x=settingsLeft+8, right=panelLeft+panelWidth-10;
        if ("potion-effects".equals(settingsSubpage)) {
            drawString(fontRendererObj, "MODS  >  POTION HUD  >  EFFECTS", x, panelTop + 30, 0xFFB8BDC8);
            drawString(fontRendererObj, "POTION EFFECTS", x, panelTop + 40, ACCENT);
            PotionHudInfoEffectSetting selected = getSelectedPotionEffect();
            if (selected != null && potionEffectIndex < potionEffectIds.size()) {
                int effectId = potionEffectIds.get(potionEffectIndex).intValue();
                drawString(fontRendererObj, PotionHudInfoModule.getPotionName(effectId), x + Math.max(90, (right - x - 8) / 2) + 8, panelTop + 90, 0xFFE7E9EE);
                drawString(fontRendererObj, selected.isEnabled() ? "Visible in HUD" : "Hidden from HUD", x + Math.max(90, (right - x - 8) / 2) + 8, panelTop + 110, selected.isEnabled() ? 0xFF9CE2AC : 0xFFB8BDC8);
                drawWrappedTextLimited("Customize the effect label, colors, text style and low-duration warning.", x + Math.max(90, (right - x - 8) / 2) + 8, panelTop + 142, Math.max(60, right - x - Math.max(90, (right - x - 8) / 2) - 12), 0xFFB8BDC8, 5);
            }
            drawString(fontRendererObj, "PAGE " + (potionEffectPage + 1) + " / " + Math.max(1, (potionEffectIds.size() + POTION_PAGE_SIZE - 1) / POTION_PAGE_SIZE), x, panelTop + 248, 0xFFB8BDC8);
            return;
        }
        if ("potion-effect-edit".equals(settingsSubpage)) {
            PotionHudInfoEffectSetting setting = getSelectedPotionEffect();
            drawString(fontRendererObj, "MODS  >  POTION HUD  >  CUSTOMIZE", x, panelTop + 30, 0xFFB8BDC8);
            drawString(fontRendererObj, "CUSTOMIZE EFFECT", x, panelTop + 40, ACCENT);
            if (setting != null) {
                drawString(fontRendererObj, "NAME COLOR", x + 4, panelTop + 76, 0xFFB8BDC8);
                drawString(fontRendererObj, "TIMER COLOR", x + Math.max(130, (right - x) / 2), panelTop + 76, 0xFFB8BDC8);
                drawRect(x + 4, panelTop + 84, x + Math.max(110, (right - x) / 2 - 8), panelTop + 88, 0xFF000000 | (setting.getRawNameColor() & 0xFFFFFF));
                drawRect(x + Math.max(130, (right - x) / 2), panelTop + 84, right - 4, panelTop + 88, 0xFF000000 | (setting.getRawTimerColor() & 0xFFFFFF));
                String[] rgb = {"R", "G", "B"};
                for (int i = 0; i < 3; i++) {
                    int shift = 16 - i * 8, yy = panelTop + 96 + i * 23;
                    drawString(fontRendererObj, rgb[i] + " " + ((setting.getRawNameColor() >> shift) & 255), x + 34, yy + 5, 0xFFE7E9EE);
                    drawString(fontRendererObj, rgb[i] + " " + ((setting.getRawTimerColor() >> shift) & 255), x + Math.max(160, (right - x) / 2 + 28), yy + 5, 0xFFE7E9EE);
                }
                drawString(fontRendererObj, "Threshold: " + setting.getFlickerStartTime() + " sec", x + 30, panelTop + 224, 0xFFE7E9EE);
                drawString(fontRendererObj, "Speed: " + setting.getFlickerSpeed() + " ms", x + Math.max(150, (right - x) / 2 + 28), panelTop + 224, 0xFFE7E9EE);
                drawString(fontRendererObj, "CUSTOM NAME", x + 2, panelTop + 240, 0xFFB8BDC8);
                if (potionEffectNameField != null) potionEffectNameField.drawTextBox();
            }
            return;
        }
        if ("potion-effect-import".equals(settingsSubpage)) {
            drawString(fontRendererObj, "MODS  >  POTION HUD  >  COPY SETTINGS", x, panelTop + 30, 0xFFB8BDC8);
            drawString(fontRendererObj, "COPY EFFECT SETTINGS", x, panelTop + 40, ACCENT);
            int target = getSelectedPotionEffectId();
            int source = getPotionImportSourceId();
            drawString(fontRendererObj, "TARGET", x + 4, panelTop + 95, 0xFFB8BDC8);
            drawString(fontRendererObj, target < 0 ? "None" : PotionHudInfoModule.getPotionName(target), x + 4, panelTop + 111, 0xFFE7E9EE);
            drawString(fontRendererObj, "SOURCE", x + 4, panelTop + 135, 0xFFB8BDC8);
            drawString(fontRendererObj, source < 0 ? "No source effects" : PotionHudInfoModule.getPotionName(source), x + 4, panelTop + 151, 0xFFE7E9EE);
            drawWrappedTextLimited("Copying replaces the target effect's current settings.", x + 4, panelTop + 190, right - x - 8, 0xFFFFD866, 3);
            if (!potionImportSourceIds.isEmpty()) drawString(fontRendererObj, (potionImportSourceIndex + 1) + " / " + potionImportSourceIds.size(), x + 4, panelTop + 185, 0xFFB8BDC8);
            return;
        }
        if ("legacy-list".equals(settingsSubpage)) {
            drawString(fontRendererObj, "MODS  >  LEGACY PVP", x, panelTop + 30, 0xFFB8BDC8);
            drawString(fontRendererObj, "LEGACY PVP FEATURES", x, panelTop + 40, ACCENT);
            drawWrappedTextLimited("Select a feature to inspect its status, details and available controls.", x, panelTop + 65, right - x - 4, 0xFFB8BDC8, 2);
            return;
        }
        if ("legacy-detail".equals(settingsSubpage)) {
            LegacyPvpFeatureCatalog.Feature feature = getLegacySelectedFeature();
            drawString(fontRendererObj, "MODS  >  LEGACY PVP  >  FEATURE", x, panelTop + 30, 0xFFB8BDC8);
            drawString(fontRendererObj, feature == null ? "FEATURE NOT FOUND" : feature.label.toUpperCase(Locale.ROOT), x, panelTop + 40, ACCENT);
            if (feature != null) {
                String status = LegacyPvpFeatureCatalog.getStatusLabel(feature.status);
                if (("blockhit".equals(feature.id) && !LegacyPvpTransformer.isInstalled())
                        || ("missed-swing-delay".equals(feature.id) && !LegacyPvpTransformer.isMissedSwingDelayInstalled())
                        || ("bow-animation".equals(feature.id) && !LegacyPvpTransformer.isBowAnimationInstalled())
                        || ("sword-swing".equals(feature.id) && !LegacyPvpTransformer.isSwordSwingInstalled())
                        || ("rod-animation".equals(feature.id) && !LegacyPvpTransformer.isRodAnimationInstalled())) status = "HOOK NOT LOADED";
                drawString(fontRendererObj, status, x, panelTop + 76, isLegacyToggleAvailable(feature.id) ? 0xFF9CE2AC : 0xFFFFD866);
                drawWrappedTextLimited(feature.detail, x, panelTop + 100, right - x - 4, 0xFFE7E9EE, 8);
                if (!isLegacyToggleAvailable(feature.id)) {
                    drawWrappedTextLimited("No control is available for this feature in the current client build.", x, panelTop + 250, right - x - 4, 0xFFB8BDC8, 3);
                }
            }
            return;
        }
        if ("keystrokes-colors".equals(settingsSubpage)) {
            drawString(fontRendererObj, "MODS  >  KEYSTROKES  >  COLORS", x, panelTop + 30, 0xFFB8BDC8);
            drawString(fontRendererObj, "KEYSTROKES COLORS", x, panelTop + 40, ACCENT);
            String[] labels = {"BACKGROUND", "PRESSED BACKGROUND", "TEXT", "PRESSED TEXT", "BORDER"};
            for (int i = 0; i < keystrokesColorFields.length; i++) {
                drawString(fontRendererObj, labels[i], x, panelTop + 94 + i * 28, 0xFFE7E9EE);
                if (keystrokesColorFields[i] != null) keystrokesColorFields[i].drawTextBox();
            }
            drawWrappedTextLimited("Enter 8-digit AARRGGBB hex values. A 6-digit RGB value is also accepted.",
                    x, panelTop + 232, right - x - 4, 0xFFB8BDC8, 2);
            return;
        }
        drawString(fontRendererObj,module==null?"MODULE NOT FOUND":module.getName().toUpperCase(Locale.ROOT),x,panelTop+40,ACCENT);
        if(module==null)return;
        String id=module.getId();
        boolean specialized="fps".equals(id)||"ping".equals(id)||"cps".equals(id)||"combo-counter".equals(id)
                ||"armor-hud-info".equals(id)||"potion-hud-info".equals(id)||"keystrokes".equals(id)
                ||"legacy-pvp".equals(id)||"name-viewer".equals(id);
        if(!specialized)drawWrappedTextLimited(module.getDescription(),x,panelTop+111,right-x-4,0xFFB8BDC8,3);
        if(!module.isImplemented())drawString(fontRendererObj,"This module is not implemented yet.",x,panelTop+154,0xFFFFD866);
        else if(!module.isUnlocked())drawString(fontRendererObj,"Your current rank does not unlock this module.",x,panelTop+154,0xFFFFD866);
        if("name-viewer".equals(id))drawWrappedTextLimited("Displays the extra name and rank display above your own player.",x,panelTop+111,right-x-4,0xFFB8BDC8,3);
        if("potion-hud-info".equals(id)){
            PotionHudInfoModule p=PotionHudInfoModule.getInstance();
            drawString(fontRendererObj,"Opacity: "+p.getOpacity()+" / 255",x,panelTop+252,0xFFE7E9EE);
            drawString(fontRendererObj,"Scale: "+String.format(Locale.ROOT,"%.2f",p.getScale()),x,panelTop+266,0xFFE7E9EE);
        }
        if(!specialized && module.isImplemented()){
            List<AlphaModuleFeature> features=module.getFeatures();
            int y=panelTop+150;
            drawString(fontRendererObj,"MODULE FEATURES",x,y,ACCENT); y+=12;
            for(int i=0;i<features.size()&&i<8;i++){
                AlphaModuleFeature f=features.get(i);
                drawString(fontRendererObj,f.getName(),x,y,0xFFE7E9EE);
                String status=f.getStatusLabel();
                drawString(fontRendererObj,status,right-4-fontRendererObj.getStringWidth(status),y,
                        f.isAvailable()?0xFF9CE2AC:0xFFB8BDC8);
                y+=15;
            }
        }
    }

    private void refreshPotionEffectIds() {
        potionEffectIds.clear();
        for (int i = 0; i < Potion.potionTypes.length; i++) if (Potion.potionTypes[i] != null) potionEffectIds.add(Integer.valueOf(i));
        potionEffectIndex = Math.max(0, Math.min(potionEffectIndex, Math.max(0, potionEffectIds.size() - 1)));
        potionEffectPage = Math.max(0, Math.min(potionEffectPage, Math.max(0, (potionEffectIds.size() - 1) / POTION_PAGE_SIZE)));
    }
    private int getSelectedPotionEffectId() {
        return potionEffectIds.isEmpty() ? -1 : potionEffectIds.get(Math.max(0, Math.min(potionEffectIndex, potionEffectIds.size() - 1))).intValue();
    }
    private PotionHudInfoEffectSetting getSelectedPotionEffect() {
        int id = getSelectedPotionEffectId();
        return id < 0 ? null : PotionHudInfoModule.getInstance().getEffect(id);
    }
    private void refreshPotionImportSources() {
        potionImportSourceIds.clear();
        int target = getSelectedPotionEffectId();
        for (int i = 0; i < Potion.potionTypes.length; i++) if (Potion.potionTypes[i] != null && i != target) potionImportSourceIds.add(Integer.valueOf(i));
        potionImportSourceIndex = Math.max(0, Math.min(potionImportSourceIndex, Math.max(0, potionImportSourceIds.size() - 1)));
    }
    private int getPotionImportSourceId() {
        return potionImportSourceIds.isEmpty() ? -1 : potionImportSourceIds.get(Math.max(0, Math.min(potionImportSourceIndex, potionImportSourceIds.size() - 1))).intValue();
    }
    private int adjustPotionColor(int color, int shift, int amount) {
        int value = Math.max(0, Math.min(255, ((color >> shift) & 255) + amount));
        return (color & ~(255 << shift)) | (value << shift);
    }
    private void handlePotionEditorAction(int id) {
        PotionHudInfoEffectSetting setting = getSelectedPotionEffect();
        if (setting == null) return;
        if (potionEffectNameField != null) setting.setCustomName(potionEffectNameField.getText());
        int name = setting.getRawNameColor(), timer = setting.getRawTimerColor();
        switch (id) {
            case POTION_NAME_R_DOWN: setting.setNameColor(adjustPotionColor(name, 16, -16)); break;
            case POTION_NAME_G_DOWN: setting.setNameColor(adjustPotionColor(name, 8, -16)); break;
            case POTION_NAME_B_DOWN: setting.setNameColor(adjustPotionColor(name, 0, -16)); break;
            case POTION_TIMER_R_DOWN: setting.setTimerColor(adjustPotionColor(timer, 16, -16)); break;
            case POTION_TIMER_G_DOWN: setting.setTimerColor(adjustPotionColor(timer, 8, -16)); break;
            case POTION_TIMER_B_DOWN: setting.setTimerColor(adjustPotionColor(timer, 0, -16)); break;
            case POTION_NAME_R_UP: setting.setNameColor(adjustPotionColor(name, 16, 16)); break;
            case POTION_NAME_G_UP: setting.setNameColor(adjustPotionColor(name, 8, 16)); break;
            case POTION_NAME_B_UP: setting.setNameColor(adjustPotionColor(name, 0, 16)); break;
            case POTION_TIMER_R_UP: setting.setTimerColor(adjustPotionColor(timer, 16, 16)); break;
            case POTION_TIMER_G_UP: setting.setTimerColor(adjustPotionColor(timer, 8, 16)); break;
            case POTION_TIMER_B_UP: setting.setTimerColor(adjustPotionColor(timer, 0, 16)); break;
            case POTION_CHROMA_NAME: setting.setChromaName(!setting.isChromaName()); break;
            case POTION_CHROMA_TIMER: setting.setChromaTimer(!setting.isChromaTimer()); break;
            case POTION_BOLD: setting.setBold(!setting.isBold()); break;
            case POTION_ITALIC: setting.setItalic(!setting.isItalic()); break;
            case POTION_UNDERLINE: setting.setUnderlined(!setting.isUnderlined()); break;
            case POTION_FLICKER: setting.setFlickerWhenLow(!setting.isFlickerWhenLow()); break;
            case POTION_THRESHOLD_DOWN: setting.setFlickerStartTime(setting.getFlickerStartTime() - 1); break;
            case POTION_THRESHOLD_UP: setting.setFlickerStartTime(setting.getFlickerStartTime() + 1); break;
            case POTION_SPEED_DOWN: setting.setFlickerSpeed(setting.getFlickerSpeed() - 1); break;
            case POTION_SPEED_UP: setting.setFlickerSpeed(setting.getFlickerSpeed() + 1); break;
        }
    }

    private LegacyPvpFeatureCatalog.Feature getLegacySelectedFeature() {
        for (LegacyPvpFeatureCatalog.Feature feature : LegacyPvpFeatureCatalog.getAll()) {
            if (feature.id.equals(legacySelectedFeatureId)) return feature;
        }
        return null;
    }

    private boolean isLegacyToggleAvailable(String id) {
        if ("blockhit".equals(id)) return LegacyPvpTransformer.isInstalled();
        if ("missed-swing-delay".equals(id)) return LegacyPvpTransformer.isMissedSwingDelayInstalled();
        if ("bow-animation".equals(id)) return LegacyPvpTransformer.isBowAnimationInstalled();
        if ("sword-swing".equals(id)) return LegacyPvpTransformer.isSwordSwingInstalled();
        if ("rod-animation".equals(id)) return LegacyPvpTransformer.isRodAnimationInstalled();
        if ("potion-punch".equals(id)) return true;
        return "camera-bobbing".equals(id);
    }

    private String getLegacyToggleLabel(String id) {
        if ("blockhit".equals(id)) return LegacyPvpSettings.isBlockhitEnabled() ? "BLOCKHIT: ON" : "BLOCKHIT: OFF";
        if ("camera-bobbing".equals(id)) return LegacyPvpSettings.isDisableViewBobbing() ? "BOBBING: OFF" : "BOBBING: ON";
        if ("bow-animation".equals(id)) return LegacyPvpSettings.isLegacyBowAnimationEnabled() ? "LEGACY BOW: ON" : "LEGACY BOW: OFF";
        if ("potion-punch".equals(id)) return LegacyPvpSettings.isPotionPunchAnimationEnabled() ? "POTION PUNCH: ON" : "POTION PUNCH: OFF";
        if ("sword-swing".equals(id)) return LegacyPvpSettings.isLegacySwordSwingEnabled() ? "SWORD SWING: ON" : "SWORD SWING: OFF";
        if ("rod-animation".equals(id)) return LegacyPvpSettings.isLegacyRodAnimationEnabled() ? "ROD ANIMATION: ON" : "ROD ANIMATION: OFF";
        if ("food-drink-animation".equals(id)) return LegacyPvpSettings.isFoodDrinkAnimationEnabled() ? "FOOD/DRINK ANIMATION: ON" : "FOOD/DRINK ANIMATION: OFF";
        return LegacyPvpSettings.isRemoveMissedSwingDelayEnabled() ? "MISS DELAY: OFF" : "MISS DELAY: ON";
    }

    private void toggleLegacyFeature(String id) {
        if ("blockhit".equals(id) && LegacyPvpTransformer.isInstalled()) LegacyPvpSettings.setBlockhitEnabled(!LegacyPvpSettings.isBlockhitEnabled());
        else if ("camera-bobbing".equals(id)) LegacyPvpSettings.setDisableViewBobbing(!LegacyPvpSettings.isDisableViewBobbing());
        else if ("missed-swing-delay".equals(id) && LegacyPvpTransformer.isMissedSwingDelayInstalled()) LegacyPvpSettings.setRemoveMissedSwingDelayEnabled(!LegacyPvpSettings.isRemoveMissedSwingDelayEnabled());
        else if ("bow-animation".equals(id) && LegacyPvpTransformer.isBowAnimationInstalled()) LegacyPvpSettings.setLegacyBowAnimationEnabled(!LegacyPvpSettings.isLegacyBowAnimationEnabled());
        else if ("potion-punch".equals(id)) LegacyPvpSettings.setPotionPunchAnimationEnabled(!LegacyPvpSettings.isPotionPunchAnimationEnabled());
        else if ("sword-swing".equals(id) && LegacyPvpTransformer.isSwordSwingInstalled()) LegacyPvpSettings.setLegacySwordSwingEnabled(!LegacyPvpSettings.isLegacySwordSwingEnabled());
        else if ("rod-animation".equals(id) && LegacyPvpTransformer.isRodAnimationInstalled()) LegacyPvpSettings.setLegacyRodAnimationEnabled(!LegacyPvpSettings.isLegacyRodAnimationEnabled());
        else if ("food-drink-animation".equals(id)) LegacyPvpSettings.setFoodDrinkAnimationEnabled(!LegacyPvpSettings.isFoodDrinkAnimationEnabled());
    }

    private void handleModuleSetting(int setting) {
        AlphaModule module=AlphaModuleRegistry.find(selectedModuleId);
        if(module==null||!module.isUnlocked()||!module.isImplemented())return;
        if(setting==0){module.setEnabled(!module.isEnabled());return;}
        String id=module.getId();
        if("fps".equals(id)){if(setting==1)FpsMod.setShowBackground(!FpsMod.isShowBackground());else if(setting==2)FpsMod.cycleColor();}
        else if("ping".equals(id)){if(setting==1)PingMod.setShowBackground(!PingMod.isShowBackground());else if(setting==2)PingMod.cycleColor();}
        else if("cps".equals(id)){
            if(setting==1)CpsMod.setShowBackground(!CpsMod.isShowBackground());
            else if(setting==2)CpsMod.cycleColor();
            else if(setting==3){int m=CpsMod.getDisplayMode();CpsMod.setDisplayMode(m==CpsMod.MODE_BOTH?CpsMod.MODE_LEFT:m==CpsMod.MODE_LEFT?CpsMod.MODE_RIGHT:CpsMod.MODE_BOTH);}
        } else if("combo-counter".equals(id)){
            if(setting==1)ComboCounter.getInstance().setShowBackground(!ComboCounter.getInstance().isShowBackground());
            else if(setting==2)ComboCounter.getInstance().cycleColor();
        } else if("armor-hud-info".equals(id)){
            if(setting==30)InfoHudModules.toggleArmorShowNames();else if(setting==31)InfoHudModules.toggleArmorShowDurability();
            else if(setting==32)InfoHudModules.cycleArmorLayout();else if(setting==33)InfoHudModules.cycleArmorColor();
        } else if("potion-hud-info".equals(id)){
            PotionHudInfoModule p=PotionHudInfoModule.getInstance();
            if(setting==40)p.setRenderIcons(!p.isRenderIcons());else if(setting==41)p.setRenderLevelsAbove4(!p.isRenderLevelsAbove4());
            else if(setting==42)p.setShowWhileChat(!p.isShowWhileChat());else if(setting==43)p.setRomanNumerals(!p.isRomanNumerals());
            else if(setting==44)p.setOpacity(p.getOpacity()-16);else if(setting==45)p.setOpacity(p.getOpacity()+16);
            else if(setting==46)p.setScale(p.getScale()-0.05F);else if(setting==47)p.setScale(p.getScale()+0.05F);
            else if(setting==48){PotionHudInfoModule.getInstance().load();refreshPotionEffectIds();potionEffectIndex=0;potionEffectPage=0;settingsSubpage="potion-effects";initGui();}
        } else if("keystrokes".equals(id)){
            if(setting==10)KeystrokesMod.setShowClicks(!KeystrokesMod.isShowClicks());else if(setting==11)KeystrokesMod.setShowCps(!KeystrokesMod.isShowCps());
            else if(setting==12)KeystrokesMod.setShowSpaceBar(!KeystrokesMod.isShowSpaceBar());else if(setting==13)KeystrokesMod.setShowMovementKeys(!KeystrokesMod.isShowMovementKeys());
            else if(setting==14)KeystrokesMod.setUseArrows(!KeystrokesMod.isUseArrows());else if(setting==15)KeystrokesMod.setBorder(!KeystrokesMod.isBorder());
            else if(setting==16)KeystrokesMod.setTextShadow(!KeystrokesMod.isTextShadow());else if(setting==17)KeystrokesMod.adjustFadeDelay(-25);
            else if(setting==18)KeystrokesMod.adjustFadeDelay(25);else if(setting==19)KeystrokesMod.adjustBoxSize(-1);
            else if(setting==20)KeystrokesMod.adjustBoxSize(1);else if(setting==21)KeystrokesMod.adjustBorderThickness(-0.5F);
            else if(setting==22)KeystrokesMod.adjustBorderThickness(0.5F);else if(setting==23){settingsSubpage="keystrokes-colors";initGui();}
        }
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
        if (button.id == EXIT) { mc.displayGuiScreen(parent); return; }
        if (button.id == POTION_MANAGER_BACK) { settingsSubpage = ""; initGui(); return; }
        if (button.id == POTION_PAGE_PREV) { potionEffectPage = Math.max(0, potionEffectPage - 1); potionEffectIndex = potionEffectPage * POTION_PAGE_SIZE; initGui(); return; }
        if (button.id == POTION_PAGE_NEXT) { potionEffectPage = Math.min(Math.max(0, (potionEffectIds.size() - 1) / POTION_PAGE_SIZE), potionEffectPage + 1); potionEffectIndex = potionEffectPage * POTION_PAGE_SIZE; initGui(); return; }
        if (button.id >= POTION_LIST_BASE && button.id < POTION_LIST_BASE + POTION_PAGE_SIZE) { int selected = potionEffectPage * POTION_PAGE_SIZE + button.id - POTION_LIST_BASE; if (selected < potionEffectIds.size()) potionEffectIndex = selected; initGui(); return; }
        if (button.id == POTION_TOGGLE) { PotionHudInfoEffectSetting setting = getSelectedPotionEffect(); if (setting != null) setting.setEnabled(!setting.isEnabled()); initGui(); return; }
        if (button.id == POTION_EDIT) { potionEffectNameField = null; settingsSubpage = "potion-effect-edit"; initGui(); return; }
        if (button.id == POTION_IMPORT) { refreshPotionImportSources(); settingsSubpage = "potion-effect-import"; initGui(); return; }
        if (button.id == POTION_EDITOR_BACK) { if (potionEffectNameField != null) { PotionHudInfoEffectSetting setting = getSelectedPotionEffect(); if (setting != null) setting.setCustomName(potionEffectNameField.getText()); } potionEffectNameField = null; settingsSubpage = "potion-effects"; initGui(); return; }
        if (button.id == POTION_EDITOR_SAVE) { if (potionEffectNameField != null) { PotionHudInfoEffectSetting setting = getSelectedPotionEffect(); if (setting != null) setting.setCustomName(potionEffectNameField.getText()); } initGui(); return; }
        if (button.id >= POTION_NAME_R_DOWN && button.id <= POTION_SPEED_UP) { handlePotionEditorAction(button.id); initGui(); return; }
        if (button.id == POTION_IMPORT_PREV) { potionImportSourceIndex = Math.max(0, potionImportSourceIndex - 1); initGui(); return; }
        if (button.id == POTION_IMPORT_NEXT) { potionImportSourceIndex = Math.min(potionImportSourceIds.size() - 1, potionImportSourceIndex + 1); initGui(); return; }
        if (button.id == POTION_IMPORT_COPY) { int targetId = getSelectedPotionEffectId(), sourceId = getPotionImportSourceId(); if (targetId >= 0 && sourceId >= 0) { PotionHudInfoEffectSetting target = PotionHudInfoModule.getInstance().getEffect(targetId); PotionHudInfoEffectSetting source = PotionHudInfoModule.getInstance().getEffect(sourceId); if (target != null && source != null) target.copyFrom(source); } settingsSubpage = "potion-effects"; initGui(); return; }
        if (button.id == POTION_IMPORT_BACK) { settingsSubpage = "potion-effects"; initGui(); return; }
        if (button.id == LEGACY_OPEN && "legacy-pvp".equals(selectedModuleId) && settingsSubpage.isEmpty()) {
            LegacyPvpSettings.load();
            legacySelectedFeatureId = "";
            settingsSubpage = "legacy-list";
            initGui();
            return;
        }
        if (button.id == LEGACY_DETAIL_BACK && "legacy-detail".equals(settingsSubpage)) { settingsSubpage = "legacy-list"; initGui(); return; }
        if (button.id == LEGACY_TOGGLE && "legacy-detail".equals(settingsSubpage)) { toggleLegacyFeature(legacySelectedFeatureId); initGui(); return; }
        if ("legacy-list".equals(settingsSubpage) && button.id >= LEGACY_FEATURE_BASE && button.id < LEGACY_FEATURE_BASE + LegacyPvpFeatureCatalog.getAll().length) {
            int featureIndex = button.id - LEGACY_FEATURE_BASE;
            LegacyPvpFeatureCatalog.Feature[] features = LegacyPvpFeatureCatalog.getAll();
            if (featureIndex >= 0 && featureIndex < features.length) { legacySelectedFeatureId = features[featureIndex].id; settingsSubpage = "legacy-detail"; initGui(); }
            return;
        }
        if (button.id == COLOR_BACK) { Keyboard.enableRepeatEvents(false); settingsSubpage = ""; keystrokesColorFields = new GuiTextField[5]; initGui(); return; }
        if (button.id == COLOR_SAVE) {
            int[] parsed = new int[keystrokesColorFields.length];
            try {
                for (int i = 0; i < keystrokesColorFields.length; i++) {
                    String value = keystrokesColorFields[i].getText().trim();
                    if (value.startsWith("#")) value = value.substring(1);
                    if (value.length() == 6) value = "FF" + value;
                    if (value.length() != 8 || !value.matches("[0-9A-Fa-f]{8}")) return;
                    parsed[i] = (int) Long.parseLong(value, 16);
                }
            } catch (NumberFormatException invalidColor) { return; }
            String[] keys = {"background", "pressedBackground", "text", "pressedText", "border"};
            for (int i = 0; i < keys.length; i++) KeystrokesMod.setColor(keys[i], parsed[i]);
            Keyboard.enableRepeatEvents(false); settingsSubpage = ""; keystrokesColorFields = new GuiTextField[5]; initGui(); return;
        }
        if (button.id == SETTINGS_PREVIOUS) {
            List<AlphaModule> modules = getFilteredModules();
            int currentIndex = -1;
            for (int i = 0; i < modules.size(); i++) {
                if (modules.get(i).getId().equals(selectedModuleId)) { currentIndex = i; break; }
            }
            if (currentIndex > 0) {
                selectedModuleId = modules.get(currentIndex - 1).getId();
                settingsSubpage = "";
                potionEffectNameField = null;
                Keyboard.enableRepeatEvents(false);
                initGui();
            }
            return;
        }
        if (button.id == SETTINGS_BACK) {
            settingsSubpage = "";
            selectedModuleId = "";
            potionEffectNameField = null;
            Keyboard.enableRepeatEvents(false);
            initGui();
            return;
        }
        if (button.id >= SETTINGS_BASE && button.id < SETTINGS_BASE + 60) {
            handleModuleSetting(button.id - SETTINGS_BASE);
            initGui();
            return;
        }
        if (button.id == ACCOUNT) { mc.displayGuiScreen(new CommunityLoginGui(this)); return; }
        if (button.id == WAYPOINTS) {
            mc.displayGuiScreen(new WaypointsGui(this));
            return;
        }
        if (button.id == HUD_EDITOR) {
            mc.displayGuiScreen(new HudEditorGui(this));
            return;
        }
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
            if (PermissionService.hasStaffModsAccess()) {
                mc.displayGuiScreen(new StaffModsMenuGui(this));
            }
            return;
        }
        if (button.id >= DONATOR_SUBFOLDER_BASE && button.id < DONATOR_SUBFOLDER_BASE + 5) {
            AlphaModule.Category[] donorCategories = {
                AlphaModule.Category.SCOUT, AlphaModule.Category.ROGUE, AlphaModule.Category.ELITE,
                AlphaModule.Category.ALPHA, AlphaModule.Category.ALPHA_PLUS
            };
            AlphaModule.Category targetCategory = donorCategories[button.id - DONATOR_SUBFOLDER_BASE];
            if (!hasDonatorFolderAccess(targetCategory)) {
                selectedCategory = AlphaModule.Category.DONATOR;
                selectedModuleId = "";
                scrollOffset = 0;
                initGui();
                return;
            }
            if (targetCategory == AlphaModule.Category.SCOUT) {
                // Open Scout as a separate folder screen so selecting the Scout
                // folder cannot carry the Donator screen's click handling forward
                // into the Pixel Garden module tile.
                mc.displayGuiScreen(new AlphaModMenu(this, targetCategory));
                return;
            }
            selectedCategory = targetCategory;
            selectedModuleId = "";
            scrollOffset = 0;
            initGui();
            return;
        }
        if (button.id >= CATEGORY_BASE
                && button.id < CATEGORY_BASE + AlphaModule.Category.values().length) {
            AlphaModule.Category category = AlphaModule.Category.values()[button.id - CATEGORY_BASE];
            boolean categoryAccess = category == AlphaModule.Category.DONATOR
                    ? hasAnyUnlockedDonatorFolder()
                    : PermissionService.hasAccess(category.getRequiredRank())
                    || (category == AlphaModule.Category.FOUNDER
                    && SupabaseCommunityService.isSignedInFounder());
            // Match the sidebar visibility rule: an unlocked module grants access to its category.
            if (!categoryAccess) {
                for (AlphaModule module : AlphaModuleRegistry.getForCategory(category)) {
                    if (!"staffmods".equals(module.getId()) && module.isUnlocked()) {
                        categoryAccess = true;
                        break;
                    }
                }
            }
            if (categoryAccess) {
                selectedCategory = category;
                selectedModuleId = "";
                scrollOffset = 0;
            }
} else if (button.id >= MODULE_BASE) {
            List<AlphaModule> modules = getFilteredModules();
            int index = button.id - MODULE_BASE;
            if (index >= 0 && index < modules.size()) {
                AlphaModule clickedModule = modules.get(index);
                // Re-check access at the action boundary, not only when drawing the list.
                if (!clickedModule.isUnlocked()) {
                    selectedModuleId = "";
                    initGui();
                    return;
                }
                if ("world-edit-rogue".equals(clickedModule.getId())) {
                    mc.displayGuiScreen(new RogueWorldEditGui(this));
                    return;
                }
                if ("pixelgarden".equals(clickedModule.getId())) {
                    if (PermissionService.hasCommandPermission("menu.pixelgarden", false)) {
                        mc.displayGuiScreen(new PixelGardenGUI(this));
                    }
                    return;
                }
                if ("singleplayer".equals(clickedModule.getId())) {
                    searchFocused = false;
                    if (searchField != null) searchField.setFocused(false);
                    mc.displayGuiScreen(new SingleplayerGui(this));
                    return;
                }
                if ("misc".equals(clickedModule.getId())) {
                    searchFocused = false;
                    if (searchField != null) searchField.setFocused(false);
                    mc.displayGuiScreen(new MiscGui(this));
                    return;
                }
                if ("quality".equals(clickedModule.getId())) {
                    searchFocused = false;
                    if (searchField != null) searchField.setFocused(false);
                    mc.displayGuiScreen(new QualityGui(this));
                    return;
                }
                if ("staffmods".equals(clickedModule.getId())) {
                    if (PermissionService.hasStaffModsAccess()) {
                        mc.displayGuiScreen(new StaffModsMenuGui(this));
                    }
                    return;
                }
                if (button instanceof ModuleTileButton && ((ModuleTileButton) button).wasSwitchClicked()) {
                    if (clickedModule.isImplemented() && clickedModule.isUnlocked()) {
                        clickedModule.setEnabled(!clickedModule.isEnabled());
                    }
                } else {
                    selectedModuleId = clickedModule.getId();
                    // Module settings now open in the right-hand panel of this menu.
                    initGui();
                    return;
                }
            }
        }
        initGui();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if ("potion-effect-edit".equals(settingsSubpage)) {
            if (keyCode == Keyboard.KEY_ESCAPE) { potionEffectNameField = null; settingsSubpage = "potion-effects"; initGui(); return; }
            if (potionEffectNameField != null) { potionEffectNameField.textboxKeyTyped(typedChar, keyCode); return; }
        }
        if ("potion-effects".equals(settingsSubpage) || "potion-effect-import".equals(settingsSubpage)
                || "legacy-list".equals(settingsSubpage) || "legacy-detail".equals(settingsSubpage)) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                if ("legacy-detail".equals(settingsSubpage)) settingsSubpage = "legacy-list";
                else if ("potion-effect-edit".equals(settingsSubpage) || "potion-effect-import".equals(settingsSubpage)) settingsSubpage = "potion-effects";
                else settingsSubpage = "";
                initGui(); return;
            }
        }
        if ("keystrokes-colors".equals(settingsSubpage)) {
            if (keyCode == Keyboard.KEY_ESCAPE) { Keyboard.enableRepeatEvents(false); settingsSubpage = ""; keystrokesColorFields = new GuiTextField[5]; initGui(); return; }
            for (GuiTextField field : keystrokesColorFields) {
                if (field != null && field.isFocused()) { field.textboxKeyTyped(typedChar, keyCode); return; }
            }
            return;
        }
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
        if (keyCode == Keyboard.KEY_ESCAPE && isModuleSettingsOpen()) {
            selectedModuleId = "";
            initGui();
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
        if ("keystrokes-colors".equals(settingsSubpage)) {
            for (GuiTextField field : keystrokesColorFields) if (field != null) field.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if ("potion-effect-edit".equals(settingsSubpage) && potionEffectNameField != null) potionEffectNameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        // Read the accumulated wheel delta before GuiScreen consumes queued events.
        // getEventDWheel() is only valid while Mouse.next() is advancing the event queue;
        // reading it here can return zero/stale data, preventing the list from scrolling.
        int wheel = org.lwjgl.input.Mouse.getDWheel();
        super.handleMouseInput();
        if (wheel != 0) {
            int mouseX = org.lwjgl.input.Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - org.lwjgl.input.Mouse.getEventY() * height / mc.displayHeight - 1;
            if (mouseX >= panelLeft && mouseX <= panelLeft + sidebarWidth
                    && mouseY >= panelTop + 65 && mouseY <= panelTop + panelHeight - 40) {
                int sidebarCount = 0;
                for (AlphaModule.Category category : AlphaModule.Category.values()) {
                    if (isStaffRankCategory(category)
                            || isDonatorRankCategory(category)) continue;
                    boolean allowed = category == AlphaModule.Category.DONATOR
                            ? hasAnyUnlockedDonatorFolder()
                            : PermissionService.hasAccess(category.getRequiredRank());
                    if (!allowed) allowed = hasUnlockedModuleInCategory(category);
                    if (allowed) sidebarCount++;
                }
                if (PermissionService.hasStaffModsAccess()) sidebarCount++;
                int slots = Math.max(1, (panelTop + panelHeight - 43 - (panelTop + 68)) / 23);
                int maxCategoryOffset = Math.max(0, sidebarCount - slots);
                categoryScrollOffset = Math.max(0, Math.min(maxCategoryOffset,
                        categoryScrollOffset + (wheel < 0 ? 1 : -1)));
                initGui();
                return;
            }
            // Scroll the profile list only when the mouse is over the right-hand
            // profile/settings panel. Otherwise the wheel must scroll module tiles.
            if (mouseX >= settingsLeft && mouseX <= panelLeft + panelWidth - 8
                    && mouseY >= panelTop + 62 && mouseY <= panelTop + panelHeight - 42
                    && !isModuleSettingsOpen()) {
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
            // Ignore wheel events outside the module grid; inside it, scroll the
            // complete filtered module list so lower entries such as CPS remain reachable.
            if (mouseX < contentLeft || mouseX > contentRight
                    || mouseY < contentTop || mouseY > contentBottom) return;
            List<AlphaModule> modules = getFilteredModules();
            int maxOffset = Math.max(0, modules.size() - visibleRows() * 2);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + (wheel < 0 ? 2 : -2)));
            initGui();
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
