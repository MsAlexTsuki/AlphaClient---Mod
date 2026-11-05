package com.alpharip.staffmod.Palette;

import com.alpharip.staffmod.gui.AlphaClientHomeMenu;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Searchable action browser built against the current AlphaClient development registries. */
public final class PaletteGui extends GuiScreen {
    private static final int ACCENT = 0xFFFFC83D;
    private static final int ROW_HEIGHT = 31;
    private static final String[] CATEGORIES = {
            "All", "Recent", "Favorites", "Modules", "Screens", "Waypoints", "Commands"
    };

    private final GuiScreen parent;
    private final List<Action> actions = new ArrayList<Action>();

    private GuiTextField searchField;
    private String category = "All";
    private String status = "Search AlphaClient modules, screens, and commands.";
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int listTop;
    private int listBottom;
    private int scroll;
    private int selected;

    public PaletteGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        panelWidth = Math.min(590, Math.max(280, width - 24));
        panelHeight = Math.min(420, Math.max(220, height - 24));
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        listTop = top + 104;
        listBottom = top + panelHeight - 32;

        searchField = new GuiTextField(0, fontRendererObj, left + 14, top + 43,
                panelWidth - 28, 20);
        searchField.setMaxStringLength(100);
        searchField.setFocused(true);

        buildActions();
        Keyboard.enableRepeatEvents(true);
        clampSelection();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }

        if ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && keyCode == Keyboard.KEY_F) {
            toggleSelectedFavorite();
            return;
        }

        if (keyCode == Keyboard.KEY_UP) {
            moveSelection(-1);
            return;
        }
        if (keyCode == Keyboard.KEY_DOWN) {
            moveSelection(1);
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            executeSelected();
            return;
        }

        searchField.textboxKeyTyped(typedChar, keyCode);
        selected = 0;
        scroll = 0;
        clampSelection();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) {
            return;
        }
        if (selectCategory(mouseX, mouseY)) {
            return;
        }
        if (mouseY < listTop || mouseY >= listBottom) {
            return;
        }

        List<Action> visible = getFilteredActions();
        int index = scroll + (mouseY - listTop) / ROW_HEIGHT;
        if (index < 0 || index >= visible.size()) {
            return;
        }

        selected = index;
        Action action = visible.get(index);
        if (mouseX >= left + panelWidth - 43) {
            toggleFavorite(action);
        } else {
            executeSelected();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        // Capture the current event delta before GuiScreen processes the event.
        // The superclass can consume/reset the wheel event in the 1.8.9 input path.
        int wheel = Mouse.getEventDWheel();
        super.handleMouseInput();
        if (wheel == 0) {
            return;
        }

        int maximum = Math.max(0, getFilteredActions().size() - visibleRows());
        scroll = Math.max(0, Math.min(maximum, scroll + (wheel > 0 ? -1 : 1)));
        clampSelection();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);

        drawString("COMMAND PALETTE", left + 14, top + 12, 0xFFF2F3F5);
        drawString("Search current AlphaClient development features", left + 14,
                top + 28, 0xFFB8BDC8);
        searchField.drawTextBox();
        drawCategoryTabs(mouseX, mouseY);
        drawRect(left + 10, listTop - 4, left + panelWidth - 10, listBottom, 0x55212630);

        List<Action> visible = getFilteredActions();
        clampSelection();
        if (visible.isEmpty()) {
            drawString(emptyMessage(), left + 20, listTop + 8, 0xFFB8BDC8);
        } else {
            for (int row = 0; row < visibleRows(); row++) {
                int index = scroll + row;
                if (index >= visible.size()) {
                    break;
                }

                Action action = visible.get(index);
                int rowY = listTop + row * ROW_HEIGHT;
                if (index == selected) {
                    drawRect(left + 12, rowY, left + panelWidth - 12,
                            rowY + ROW_HEIGHT - 1, 0xAA363B45);
                }

                drawString(trim(action.name, panelWidth - 62), left + 20, rowY + 4,
                        actionColor(action));
                drawString(trim(action.description, panelWidth - 62), left + 20,
                        rowY + 17, 0xFFB8BDC8);

                boolean favorite = PaletteStore.getInstance().isFavorite(action.id);
                drawString(favorite ? "*" : "+", left + panelWidth - 31,
                        rowY + 9, favorite ? ACCENT : 0xFF858B96);
            }
        }

        if (visible.size() > visibleRows()) {
            int trackTop = listTop;
            int trackHeight = listBottom - listTop;
            int thumbHeight = Math.max(18, trackHeight * visibleRows() / visible.size());
            int maxScroll = Math.max(1, visible.size() - visibleRows());
            int thumbTop = trackTop + (trackHeight - thumbHeight) * scroll / maxScroll;
            drawRect(left + panelWidth - 7, trackTop, left + panelWidth - 4, listBottom, 0xFF303641);
            drawRect(left + panelWidth - 7, thumbTop, left + panelWidth - 4,
                    thumbTop + thumbHeight, ACCENT);
        }

        String help = "Mouse Wheel / Up/Down: Navigate  Enter/Click: Run  +: Pin  Ctrl+F: Pin selected  Esc: Close";
        drawString(trim(status + "   " + help, panelWidth - 28), left + 14,
                top + panelHeight - 18, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void buildActions() {
        actions.clear();
        boolean founderSession = SupabaseCommunityService.isSignedInFounder();

        addScreen("home", "Open AlphaClient Home", "Open the live home overlay and HUD editor.", null);
        addScreen("general", "Open General", "Browse public General modules.", "/general");
        addScreen("quality-screen", "Open Quality", "Open the current Quality settings screen.", "/quality");
        addScreen("hud-editor", "Open HUD Editor", "Move and configure supported HUD elements.", "/hudeditor");
        addScreen("help-screen", "Open AlphaClient Help", "Open the current AlphaClient help interface.", "/alphahelp open");
        addScreen("profiles-screen", "Open Profiles", "Manage saved AlphaClient profiles.", "/profiles");
        if (SupabaseCommunityService.hasStaffOperationsAccess()) {
            addScreen("staff-operations", "Open Staff Operations", "Open Staff Operations.", "/staffoperations");
        }
        if (PermissionService.hasStaffModsAccess()) {
            addScreen("staff-mods", "Open StaffMods", "Open the StaffMods menu.", "/staffmods");
            addScreen("staff-general", "Open StaffMods General", "Open StaffMods -> General.", "/staffgeneral");
        }
        addScreen("waypoints-screen", "Open Waypoints", "Manage saved waypoints for the active profile.", "/waypoint");
        addScreen("waypoint-visuals", "Open Waypoint Display Settings", "Configure waypoint markers, beams, labels, and sorting.", "/waypoint visuals");
        addScreen("minimap-settings", "Open Minimap Settings", "Configure the current AlphaClient minimap.", "/minimap settings");

        addCommand("alpha-help", "AlphaClient Help Command", "Show the command help available to your account.", "/alphaclient help");
        addCommand("fps-settings", "FPS HUD Settings", "Open FPS counter settings.", "/fpshud settings");
        addCommand("cps-settings", "CPS HUD Settings", "Open CPS counter settings.", "/cpshud settings");
        addCommand("potion-hud-settings", "Potion HUD Settings", "Open Potion HUD settings.", "/pothud settings");
        addCommand("autogg-command", "Auto GG", "Run the current Auto GG command.", "/autogg");
        addCommand("autoreconnect-command", "Auto Reconnect", "Run the current Auto Reconnect command.", "/autoreconnect");
        addCommand("playtime-command", "Playtime Tracker", "Show or manage the Playtime Tracker.", "/playtime");
        addCommand("session-timer-command", "Session Timer", "Show or manage the Session Timer.", "/sessiontimer");
        addCommand("afk-timer-command", "AFK Timer", "Show or manage the AFK Timer.", "/afktimer");
        addCommand("fullbright-toggle", "Toggle Fullbright", "Toggle the current Fullbright setting.", "/fullbright toggle");
        addCommand("autotool-command", "Open AutoTool Command", "Open the current AutoTool command route.", "/autotool");
        addCommand("inventory-search-command", "Open Inventory Search", "Open Inventory Search from the current development command.", "/inventorysearch");
        addCommand("world-edit-help", "World Edit Command Help", "Show the current singleplayer World Edit command help.", "//acwe help");
        addCommand("minimap-toggle", "Toggle Minimap", "Toggle the current Minimap module state.", "/minimap toggle");
        addCommand("waypoint-list", "List Current Waypoints", "List waypoints in the active world and profile.", "/waypoint list");
        addCommand("waypoint-history", "Show Coordinate History", "Show recently recorded coordinates.", "/waypoint history");
        addCommand("waypoint-export", "Export Waypoints", "Create a local waypoint share file.", "/waypoint export");
        addCommand("waypoint-help", "Waypoint Command Help", "Show supported waypoint commands.", "/waypoint help");

        addRankCommands();

        if (SupabaseCommunityService.isSignedInFounder()) {
            addCommand("founder-permissions", "Founder Permission Help", "Show Founder-only permission manager help.", "/alphaperms help");
            addCommand("founder-aimassist", "AimAssist Help", "Show Founder-only AimAssist command help.", "/aimassist help");
            addCommand("founder-easter-eggs", "Easter Egg Help", "Show Founder-only Easter Egg command help.", "/easteregg help");
        }

        if (PermissionService.hasCommandPermission("menu.pixelgarden", false)) {
            addCommand("pixel-garden", "Open Pixel Garden", "Open Pixel Garden when the account has its required permission.", "/pixelgarden");
        }

        Set<String> seenModules = new HashSet<String>();
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            if (module == null || !seenModules.add(module.getId())) {
                continue;
            }

            // Bhop is intentionally managed only from StaffMods -> General.
            if ("staffmods".equals(module.getId())) {
                continue;
            }

            // Keep the Palette aligned with the module registry's effective permission checks.
            // Locked modules and unavailable features are omitted instead of exposing their names.
            if (!module.isUnlocked()) {
                continue;
            }

            String moduleDescription = "[" + module.getCategory().getTitle() + "] " + module.getDescription();
            if (!module.isImplemented()) {
                addStatus("module-status:" + module.getId(), module.getName(),
                        moduleDescription + " Status: " + module.getStatusLabel() + ".", "Modules");
            } else {
                addModuleToggle(module);
                addCommand("module-settings:" + module.getId(),
                        "Configure " + module.getName(),
                        "Open the current settings route for " + module.getName() + ".",
                        "/alphaclient:settings " + module.getId());
            }

            for (AlphaModuleFeature feature : module.getFeatures()) {
                if (feature == null || !feature.isAvailable()) {
                    continue;
                }
                String description = module.getName() + " feature: " + feature.getDescription()
                        + " Status: " + feature.getStatusLabel() + ".";
                if (feature.isImplemented()) {
                    addFeatureToggle(module, feature, description);
                } else {
                    addStatus("feature-status:" + feature.getId(), feature.getName(),
                            description, "Modules");
                }
            }
        }
    }

    private void addRankCommands() {
        if (hasRankFolderAccess(PermissionService.Rank.BETA, "menu.beta")) {
            addCommand("rank-beta", "Open Beta Folder", "Open the Beta folder.", "/beta");
        }
        if (hasRankFolderAccess(PermissionService.Rank.DONATOR, "menu.donator")) {
            addCommand("rank-donator", "Open Donator Folder", "Open the Donator folder.", "/donator");
        }
        if (hasRankFolderAccess(PermissionService.Rank.SCOUT, "menu.scout")) {
            addCommand("rank-scout", "Open Scout Folder", "Open the Scout folder.", "/scout");
        }
        if (hasRankFolderAccess(PermissionService.Rank.ROGUE, "menu.rogue")) {
            addCommand("rank-rogue", "Open Rogue Folder", "Open the Rogue folder.", "/rogue");
        }
        if (hasRankFolderAccess(PermissionService.Rank.ELITE, "menu.elite")) {
            addCommand("rank-elite", "Open Elite Folder", "Open the Elite folder.", "/elite");
        }
        if (hasRankFolderAccess(PermissionService.Rank.ALPHA, "menu.alpha")) {
            addCommand("rank-alpha", "Open Alpha Folder", "Open the Alpha folder.", "/alpha");
        }
        if (hasRankFolderAccess(PermissionService.Rank.ALPHA_PLUS, "menu.alphaplus")) {
            addCommand("rank-alpha-plus", "Open Alpha+ Folder", "Open the Alpha+ folder.", "/alphaplus");
        }

        if (!PermissionService.hasStaffModsAccess()) {
            return;
        }

        if (hasStaffFolderAccess("staff-tmod", "staffmods.tmod")) {
            addCommand("staff-tmod", "Open Trial Moderator Folder", "Open the T-Mod staff folder.", "/stafftmod");
        }
        if (hasStaffFolderAccess("staff-mod", "staffmods.mod")) {
            addCommand("staff-mod", "Open Moderator Folder", "Open the Moderator staff folder.", "/staffmod");
        }
        if (hasStaffFolderAccess("staff-srmod", "staffmods.srmod")) {
            addCommand("staff-srmod", "Open Sr Moderator Folder", "Open the Sr Moderator staff folder.", "/staffsrmod");
        }
        if (hasStaffFolderAccess("staff-admin", "staffmods.admin")) {
            addCommand("staff-admin", "Open Admin Folder", "Open the Admin staff folder.", "/staffadmin");
        }
        if (hasStaffFolderAccess("staff-sradmin", "staffmods.sradmin")) {
            addCommand("staff-sradmin", "Open Sr Admin Folder", "Open the Sr Admin staff folder.", "/staffsradmin");
        }
        if (hasStaffFolderAccess("staff-platform-admin", "staffmods.platformadmin")) {
            addCommand("staff-platform-admin", "Open Platform Admin Folder", "Open the Platform Admin staff folder.", "/staffplatformadmin");
        }
        if (hasStaffFolderAccess("staff-management", "staffmods.management")) {
            addCommand("staff-management", "Open Management Folder", "Open the shared Management folder.", "/staffmanagement");
        }
        if (hasStaffFolderAccess("staff-manager", "staffmods.manager")) {
            addCommand("staff-manager", "Open Manager Folder", "Open the Manager staff folder.", "/staffmanager");
        }
        if (hasStaffFolderAccess("staff-developer", "staffmods.developer")) {
            addCommand("staff-developer", "Open Developer Folder", "Open the Developer staff folder.", "/staffmods developer");
        }
        if (hasStaffFolderAccess("staff-owner", "staffmods.owner")) {
            addCommand("staff-owner", "Open Owner Folder", "Open the Owner staff folder. Founder access is not included.", "/staffowner");
        }
        if (hasStaffFolderAccess("staff-founder", "staffmods.founder")) {
            addCommand("staff-founder", "Open Founder Folder", "Open the Founder-only staff folder.", "/stafffounder");
        }
    }

    private boolean hasRankFolderAccess(PermissionService.Rank rank, String permissionKey) {
        Boolean override = PermissionService.getCommandPermissionOverride(permissionKey);
        return override != null ? override.booleanValue() : PermissionService.hasAccess(rank);
    }

    private void addScreen(String id, String name, String description, String command) {
        actions.add(new Action(id, name, description, "Screens", command, null, null, false));
    }

    private void addCommand(String id, String name, String description, String command) {
        actions.add(new Action(id, name, description,
                id.startsWith("waypoint-") ? "Waypoints" : "Commands",
                command, null, null, false));
    }

    private void addStatus(String id, String name, String description, String category) {
        actions.add(new Action(id, name, description, category, null, null, null, true));
    }

    private void addModuleToggle(AlphaModule module) {
        actions.add(new Action("module-toggle:" + module.getId(),
                "Toggle " + module.getName(),
                "Status: " + module.getStatusLabel() + ". " + module.getDescription(),
                "Modules", null, module, null, false));
    }

    private void addFeatureToggle(AlphaModule module, AlphaModuleFeature feature, String description) {
        actions.add(new Action("feature-toggle:" + feature.getId(),
                "Toggle " + feature.getName(), description, "Modules",
                null, module, feature, false));
    }

    private List<Action> getFilteredActions() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<String> recent = PaletteStore.getInstance().getRecentIds();
        Set<String> favorites = new HashSet<String>(PaletteStore.getInstance().getFavoriteIds());
        List<Action> result = new ArrayList<Action>();

        for (Action action : actions) {
            if (!isActionAvailable(action)) {
                continue;
            }
            if (!"All".equals(category) && !"Recent".equals(category)
                    && !"Favorites".equals(category) && !category.equals(action.category)) {
                continue;
            }
            if ("Recent".equals(category) && !recent.contains(action.id)) {
                continue;
            }
            if ("Favorites".equals(category) && !favorites.contains(action.id)) {
                continue;
            }

            action.score = query.isEmpty() ? 0 : score(action, query);
            if (action.score != Integer.MAX_VALUE) {
                result.add(action);
            }
        }

        final boolean hasQuery = !query.isEmpty();
        final boolean recentOnly = "Recent".equals(category);
        final boolean favoritesOnly = "Favorites".equals(category);
        Collections.sort(result, new Comparator<Action>() {
            @Override
            public int compare(Action first, Action second) {
                if (hasQuery && first.score != second.score) {
                    return Integer.compare(first.score, second.score);
                }
                if (recentOnly) {
                    int recentOrder = recent.indexOf(first.id) - recent.indexOf(second.id);
                    if (recentOrder != 0) {
                        return recentOrder;
                    }
                }
                boolean firstFavorite = favorites.contains(first.id);
                boolean secondFavorite = favorites.contains(second.id);
                if (firstFavorite != secondFavorite) {
                    return firstFavorite ? -1 : 1;
                }
                if (favoritesOnly) {
                    int favoriteOrder = PaletteStore.getInstance().favoriteIndex(first.id)
                            - PaletteStore.getInstance().favoriteIndex(second.id);
                    if (favoriteOrder != 0) {
                        return favoriteOrder;
                    }
                }
                return first.name.compareToIgnoreCase(second.name);
            }
        });
        return result;
    }

    private boolean isActionAvailable(Action action) {
        if (action == null) return false;
        if (isFounderOnlyAction(action) && !SupabaseCommunityService.isSignedInFounder()) return false;

        if (action.module != null && !action.module.isUnlocked()) return false;
        if (action.feature != null && !action.feature.isAvailable()) return false;

        if ("staff-operations".equals(action.id)) {
            return SupabaseCommunityService.hasStaffOperationsAccess();
        }
        if ("staff-mods".equals(action.id)) {
            return PermissionService.hasStaffModsAccess();
        }
        if ("staff-general".equals(action.id)) {
            return PermissionService.hasStaffModsAccess()
                    && !Boolean.FALSE.equals(PermissionService.getCommandPermissionOverride("staffmods.general"));
        }

        String staffFolderPermission = staffFolderPermissionForAction(action.id);
        if (staffFolderPermission != null) {
            return hasStaffFolderAccess(action.id, staffFolderPermission);
        }

        PermissionService.Rank requiredRank = rankRequiredByAction(action.id);
        if (requiredRank != null && !PermissionService.hasAccess(requiredRank)) {
            return false;
        }

        if (action.id.startsWith("module-settings:")) {
            String moduleId = action.id.substring("module-settings:".length());
            AlphaModule module = AlphaModuleRegistry.find(moduleId);
            if (module == null || !module.isUnlocked()) return false;
        }

        if ("pixel-garden".equals(action.id)) {
            return PermissionService.hasCommandPermission("menu.pixelgarden", false);
        }

        String permissionKey = commandPermissionForAction(action.id);
        if (permissionKey != null) {
            Boolean override = PermissionService.getCommandPermissionOverride(permissionKey);
            if (Boolean.FALSE.equals(override)) return false;
        }
        return true;
    }

    private PermissionService.Rank rankRequiredByAction(String id) {
        if ("rank-beta".equals(id)) return PermissionService.Rank.BETA;
        if ("rank-donator".equals(id)) return PermissionService.Rank.DONATOR;
        if ("rank-scout".equals(id)) return PermissionService.Rank.SCOUT;
        if ("rank-rogue".equals(id)) return PermissionService.Rank.ROGUE;
        if ("rank-elite".equals(id)) return PermissionService.Rank.ELITE;
        if ("rank-alpha".equals(id)) return PermissionService.Rank.ALPHA;
        if ("rank-alpha-plus".equals(id)) return PermissionService.Rank.ALPHA_PLUS;
        return null;
    }

    private String staffFolderPermissionForAction(String id) {
        if ("staff-tmod".equals(id)) return "staffmods.tmod";
        if ("staff-mod".equals(id)) return "staffmods.mod";
        if ("staff-srmod".equals(id)) return "staffmods.srmod";
        if ("staff-admin".equals(id)) return "staffmods.admin";
        if ("staff-sradmin".equals(id)) return "staffmods.sradmin";
        if ("staff-platform-admin".equals(id)) return "staffmods.platformadmin";
        if ("staff-management".equals(id)) return "staffmods.management";
        if ("staff-manager".equals(id)) return "staffmods.manager";
        if ("staff-developer".equals(id)) return "staffmods.developer";
        if ("staff-owner".equals(id)) return "staffmods.owner";
        if ("staff-founder".equals(id)) return "staffmods.founder";
        return null;
    }

    private boolean hasStaffFolderAccess(String actionId, String permissionKey) {
        if (!PermissionService.hasStaffModsAccess()) return false;
        if ("staff-founder".equals(actionId)) {
            return SupabaseCommunityService.isSignedInFounder();
        }

        Boolean override = PermissionService.getCommandPermissionOverride(permissionKey);
        if (Boolean.FALSE.equals(override)) return false;
        if (Boolean.TRUE.equals(override)) return true;

        if ("staff-management".equals(actionId)) {
            if (PermissionService.hasAccess(PermissionService.Rank.PLAT_ADMIN)
                    || PermissionService.hasAccess(PermissionService.Rank.MANAGER)) return true;
            return hasExplicitModuleGrant(PermissionService.Rank.PLAT_ADMIN)
                    || hasExplicitModuleGrant(PermissionService.Rank.MANAGER);
        }

        PermissionService.Rank required = PermissionService.Rank.T_MOD;
        if ("staff-mod".equals(actionId)) required = PermissionService.Rank.MOD;
        else if ("staff-srmod".equals(actionId)) required = PermissionService.Rank.SR_MOD;
        else if ("staff-admin".equals(actionId)) required = PermissionService.Rank.ADMIN;
        else if ("staff-sradmin".equals(actionId)) required = PermissionService.Rank.SR_ADMIN;
        else if ("staff-platform-admin".equals(actionId)) required = PermissionService.Rank.PLAT_ADMIN;
        else if ("staff-manager".equals(actionId)) required = PermissionService.Rank.MANAGER;
        else if ("staff-developer".equals(actionId)) required = PermissionService.Rank.DEVELOPER;
        else if ("staff-owner".equals(actionId)) required = PermissionService.Rank.OWNER;
        return PermissionService.hasAccess(required) || hasExplicitModuleGrant(required);
    }

    private boolean hasExplicitModuleGrant(PermissionService.Rank required) {
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            if (module == null || module.getCategory() == AlphaModule.Category.FOUNDER
                    || "staffmods".equals(module.getId())) continue;
            if (module.getCategory().getRequiredRank() == required && module.hasExplicitModAccess()) {
                return true;
            }
        }
        return false;
    }

    private String commandPermissionForAction(String id) {
        if ("general".equals(id)) return "menu.general";
        if ("rank-beta".equals(id)) return "menu.beta";
        if ("rank-donator".equals(id)) return "menu.donator";
        if ("rank-scout".equals(id)) return "menu.scout";
        if ("rank-rogue".equals(id)) return "menu.rogue";
        if ("rank-elite".equals(id)) return "menu.elite";
        if ("rank-alpha".equals(id)) return "menu.alpha";
        if ("rank-alpha-plus".equals(id)) return "menu.alphaplus";
        if ("alpha-help".equals(id) || "help-screen".equals(id)) return "command.help";
        if ("hud-editor".equals(id)) return "command.hudeditor";
        if ("profiles-screen".equals(id)) return "command.profiles.list";
        if ("fps-settings".equals(id)) return "command.fpshud";
        if ("cps-settings".equals(id)) return "command.cpshud";
        if ("potion-hud-settings".equals(id)) return "hud.potion";
        if (id.startsWith("module-settings:")) return "command.settings";
        return null;
    }

    private boolean isFounderOnlyAction(Action action) {
        if (action.module != null && action.module.getCategory() == AlphaModule.Category.FOUNDER) {
            return true;
        }
        if (action.id.startsWith("founder-") || "staff-founder".equals(action.id)) {
            return true;
        }

        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            if (module == null || module.getCategory() != AlphaModule.Category.FOUNDER) {
                continue;
            }
            String moduleId = module.getId();
            if (action.id.equals("module-status:" + moduleId)
                    || action.id.equals("module-settings:" + moduleId)) {
                return true;
            }
            for (AlphaModuleFeature feature : module.getFeatures()) {
                if (feature != null && (action.id.equals("feature-status:" + feature.getId())
                        || action.id.equals("feature-toggle:" + feature.getId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private int score(Action action, String query) {
        String name = action.name.toLowerCase(Locale.ROOT);
        String searchable = (name + " " + action.description + " " + action.category + " "
                + (action.command == null ? "" : action.command)).toLowerCase(Locale.ROOT);

        if (name.equals(query)) {
            return 0;
        }
        if (name.startsWith(query)) {
            return 10 + name.length() - query.length();
        }
        int position = name.indexOf(query);
        if (position >= 0) {
            return 30 + position;
        }
        position = searchable.indexOf(query);
        if (position >= 0) {
            return 60 + position;
        }
        int fuzzy = subsequenceScore(query, name);
        if (fuzzy != Integer.MAX_VALUE) {
            return 100 + fuzzy;
        }
        fuzzy = subsequenceScore(query, searchable);
        return fuzzy == Integer.MAX_VALUE ? Integer.MAX_VALUE : 500 + fuzzy;
    }

    private int subsequenceScore(String query, String value) {
        int previous = -1;
        int gaps = 0;
        for (int index = 0; index < query.length(); index++) {
            int next = value.indexOf(query.charAt(index), previous + 1);
            if (next < 0) {
                return Integer.MAX_VALUE;
            }
            if (previous >= 0) {
                gaps += next - previous - 1;
            }
            previous = next;
        }
        return gaps;
    }

    private void executeSelected() {
        List<Action> visible = getFilteredActions();
        if (selected < 0 || selected >= visible.size()) {
            return;
        }

        Action action = visible.get(selected);
        if (!isActionAvailable(action)) {
            status = isFounderOnlyAction(action)
                    ? "This action requires the authenticated Founder account."
                    : "Your current AlphaClient permissions do not allow this action.";
            return;
        }
        if (action.statusOnly) {
            status = action.description;
            return;
        }

        if (action.module != null && action.feature == null) {
            boolean enabled = !action.module.isConfiguredEnabled();
            action.module.setEnabled(enabled);
            status = action.module.getName() + (enabled ? " enabled." : " disabled.");
            PaletteStore.getInstance().recordUsed(action.id);
            return;
        }

        if (action.feature != null) {
            boolean enabled = !action.feature.isConfiguredEnabled();
            action.feature.setEnabled(enabled);
            status = action.feature.getName() + (enabled ? " enabled." : " disabled.");
            PaletteStore.getInstance().recordUsed(action.id);
            return;
        }

        PaletteStore.getInstance().recordUsed(action.id);
        if ("home".equals(action.id)) {
            mc.displayGuiScreen(new AlphaClientHomeMenu());
            return;
        }

        if (action.command != null) {
            if (mc == null || mc.thePlayer == null) {
                status = "Join a world before running this command.";
                return;
            }
            mc.displayGuiScreen(null);
            mc.thePlayer.sendChatMessage(action.command);
        }
    }

    private void toggleSelectedFavorite() {
        List<Action> visible = getFilteredActions();
        if (selected >= 0 && selected < visible.size()) {
            toggleFavorite(visible.get(selected));
        }
    }

    private void toggleFavorite(Action action) {
        PaletteStore store = PaletteStore.getInstance();
        if (!store.isWritable()) {
            status = "Palette history could not be read; saved pins were left unchanged.";
            return;
        }

        boolean wasFavorite = store.isFavorite(action.id);
        store.toggleFavorite(action.id);
        boolean isFavorite = store.isFavorite(action.id);
        if (wasFavorite == isFavorite) {
            status = "Could not save the favorite change.";
        } else {
            status = (isFavorite ? "Pinned " : "Unpinned ") + action.name + ".";
        }
        searchField.setFocused(true);
        clampSelection();
    }

    private void moveSelection(int direction) {
        List<Action> visible = getFilteredActions();
        if (visible.isEmpty()) {
            selected = 0;
            scroll = 0;
            return;
        }

        selected = Math.max(0, Math.min(visible.size() - 1, selected + direction));
        if (selected < scroll) {
            scroll = selected;
        } else if (selected >= scroll + visibleRows()) {
            scroll = selected - visibleRows() + 1;
        }
    }

    private void clampSelection() {
        List<Action> visible = getFilteredActions();
        if (visible.isEmpty()) {
            selected = 0;
            scroll = 0;
            return;
        }

        selected = Math.max(0, Math.min(selected, visible.size() - 1));
        scroll = Math.max(0, Math.min(scroll, Math.max(0, visible.size() - visibleRows())));
        if (selected < scroll) {
            scroll = selected;
        }
        if (selected >= scroll + visibleRows()) {
            scroll = selected - visibleRows() + 1;
        }
    }

    private int visibleRows() {
        return Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
    }

    private String emptyMessage() {
        if ("Favorites".equals(category)) {
            return "No pinned actions. Use + beside an action to pin it.";
        }
        if ("Recent".equals(category)) {
            return "No recent actions yet.";
        }
        return "No matching actions.";
    }

    private int actionColor(Action action) {
        if (action.statusOnly) {
            return 0xFFE38B7C;
        }
        if (action.module != null && action.feature == null) {
            return action.module.isConfiguredEnabled() ? 0xFF77DD88 : 0xFFE6C66B;
        }
        if (action.feature != null) {
            return action.feature.isConfiguredEnabled() ? 0xFF77DD88 : 0xFFE6C66B;
        }
        return 0xFFF0F1F4;
    }

    private void drawCategoryTabs(int mouseX, int mouseY) {
        int x = left + 14;
        for (int index = 0; index < CATEGORIES.length; index++) {
            String value = CATEGORIES[index];
            int row = index < 4 ? 0 : 1;
            if (index == 4) {
                x = left + 14;
            }

            int y = top + 69 + row * 16;
            String label = "Favorites".equals(value) ? "Fav" : value;
            int tabWidth = fontRendererObj.getStringWidth(label) + 12;
            int color = category.equals(value) ? 0xFF3A3F49 : 0xAA222630;
            drawRect(x, y, x + tabWidth, y + 14, color);
            fontRendererObj.drawString(label, x + 6, y + 3,
                    category.equals(value) ? ACCENT : 0xFFD6D9E0);
            x += tabWidth + 4;
        }
    }

    private boolean selectCategory(int mouseX, int mouseY) {
        int x = left + 14;
        for (int index = 0; index < CATEGORIES.length; index++) {
            String value = CATEGORIES[index];
            int row = index < 4 ? 0 : 1;
            if (index == 4) {
                x = left + 14;
            }

            int y = top + 69 + row * 16;
            String label = "Favorites".equals(value) ? "Fav" : value;
            int tabWidth = fontRendererObj.getStringWidth(label) + 12;
            if (mouseX >= x && mouseX < x + tabWidth && mouseY >= y && mouseY < y + 14) {
                category = value;
                selected = 0;
                scroll = 0;
                searchField.setFocused(true);
                clampSelection();
                return true;
            }
            x += tabWidth + 4;
        }
        return false;
    }

    private String trim(String value, int maxWidth) {
        if (value == null) {
            return "";
        }
        String result = value;
        while (result.length() > 3 && fontRendererObj.getStringWidth(result) > maxWidth) {
            result = result.substring(0, result.length() - 4) + "...";
        }
        return result;
    }

    private void drawString(String value, int x, int y, int color) {
        fontRendererObj.drawStringWithShadow(value, x, y, color);
    }

    private static final class Action {
        private final String id;
        private final String name;
        private final String description;
        private final String category;
        private final String command;
        private final AlphaModule module;
        private final AlphaModuleFeature feature;
        private final boolean statusOnly;
        private int score;

        private Action(String id, String name, String description, String category,
                       String command, AlphaModule module, AlphaModuleFeature feature,
                       boolean statusOnly) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.command = command;
            this.module = module;
            this.feature = feature;
            this.statusOnly = statusOnly;
        }
    }
}
