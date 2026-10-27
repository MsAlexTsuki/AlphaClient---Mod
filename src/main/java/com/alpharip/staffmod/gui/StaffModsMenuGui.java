// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.movement.STapAssistance;
import com.alpharip.staffmod.movement.WTapAssistance;
import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * StaffMods folder browser. Rank folders contain the modules assigned to them.
 * Existing module settings screens are reused instead of creating duplicate settings.
 */
public final class StaffModsMenuGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int CATEGORY_BASE = 20;
    private static final int MODULE_BASE = 50;
    private static final int ACCENT = 0xFFFFD866;

    // Keep every rank folder in canonical hierarchy order. Visibility is inherited downward.
    // Display ranks from lowest to highest. Backend rank priority remains unchanged.
    private static final PermissionService.Rank[] RANKS = {
        PermissionService.Rank.PUBLIC,
        PermissionService.Rank.T_MOD,
        PermissionService.Rank.MOD,
        PermissionService.Rank.SR_MOD,
        PermissionService.Rank.ADMIN,
        PermissionService.Rank.SR_ADMIN,
        PermissionService.Rank.MANAGEMENT,
        PermissionService.Rank.DEVELOPER,
        PermissionService.Rank.OWNER,
        PermissionService.Rank.FOUNDER
    };
    private static final String[] LABELS = {
        "GENERAL", "T-MOD", "MOD", "SR MOD",
        "ADMIN", "SR ADMIN", "MANAGEMENT", "DEVELOPER", "OWNER", "FOUNDER"
    };

    private final GuiScreen parent;
    private PermissionService.Rank selectedRank = PermissionService.Rank.PUBLIC;
    private PermissionService.Rank openFolderRank;
    private boolean managementSubfolders;
    private boolean donatorSubfolders;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int contentLeft, contentTop, contentRight, contentBottom;
    private int scrollOffset;
    private int rankScrollOffset;
    private int visibleRows;
    private List<AlphaModule> visibleModules = new ArrayList<AlphaModule>();

    public StaffModsMenuGui(GuiScreen parent) {
        this.parent = parent;
    }

    /**
     * Opens one existing StaffMods folder from a client command.
     * Folder access is checked here using the same checks as the visible folder browser.
     */
    public boolean openFolderCommand(String folderKey) {
        if (folderKey == null) return false;
        String key = folderKey.toLowerCase(java.util.Locale.ROOT).replace("-", "").replace("_", "").replace(" ", "");
        Boolean folderOverride = PermissionService.getCommandPermissionOverride(folderPermissionKey(key));
        if (Boolean.FALSE.equals(folderOverride)) return false;
        if (folderOverride == null && !PermissionService.hasStaffModsAccess()) return false;
        PermissionService.Rank rank;
        if ("general".equals(key)) rank = PermissionService.Rank.PUBLIC;
        else if ("beta".equals(key)) rank = PermissionService.Rank.BETA;
        else if ("tmod".equals(key) || "trialmoderator".equals(key)) rank = PermissionService.Rank.T_MOD;
        else if ("mod".equals(key) || "moderator".equals(key)) rank = PermissionService.Rank.MOD;
        else if ("srmod".equals(key) || "seniormod".equals(key)) rank = PermissionService.Rank.SR_MOD;
        else if ("admin".equals(key)) rank = PermissionService.Rank.ADMIN;
        else if ("sradmin".equals(key) || "senioradmin".equals(key)) rank = PermissionService.Rank.SR_ADMIN;
        else if ("management".equals(key)) {
            if (!hasFolderAccess(PermissionService.Rank.MANAGEMENT)) return false;
            managementSubfolders = true;
            donatorSubfolders = false;
            openFolderRank = null;
            return true;
        } else if ("platformadmin".equals(key) || "platadmin".equals(key)) rank = PermissionService.Rank.PLAT_ADMIN;
        else if ("manager".equals(key)) rank = PermissionService.Rank.MANAGER;
        else if ("developer".equals(key)) rank = PermissionService.Rank.DEVELOPER;
        else if ("owner".equals(key)) rank = PermissionService.Rank.OWNER;
        else if ("founder".equals(key)) rank = PermissionService.Rank.FOUNDER;
        else if ("donator".equals(key)) {
            if (!isVisibleRank(PermissionService.Rank.DONATOR)) return false;
            donatorSubfolders = true;
            managementSubfolders = false;
            openFolderRank = null;
            return true;
        } else if ("scout".equals(key)) rank = PermissionService.Rank.SCOUT;
        else if ("rogue".equals(key)) rank = PermissionService.Rank.ROGUE;
        else if ("elite".equals(key)) rank = PermissionService.Rank.ELITE;
        else if ("alpha".equals(key)) rank = PermissionService.Rank.ALPHA;
        else if ("alphaplus".equals(key)) rank = PermissionService.Rank.ALPHA_PLUS;
        else return false;

        if (rank != PermissionService.Rank.PUBLIC
                && folderOverride == null && !hasFolderAccess(rank)) return false;
        selectedRank = rank;
        openFolderRank = rank;
        managementSubfolders = false;
        donatorSubfolders = false;
        scrollOffset = 0;
        return true;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!PermissionService.hasStaffModsAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        panelWidth = Math.min(560, Math.max(360, width - 32));
        panelHeight = Math.min(330, Math.max(230, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        contentLeft = panelLeft + 16;
        contentRight = panelLeft + panelWidth - 16;
        contentTop = panelTop + 76;
        contentBottom = panelTop + panelHeight - 43;

        if (openFolderRank == null) {
            int gap = 8;
            int columns = 3;
            int tileWidth = (contentRight - contentLeft - gap * (columns - 1)) / columns;
            int tileHeight = 38;
            if (managementSubfolders) {
                if (hasFolderAccess(PermissionService.Rank.PLAT_ADMIN)) {
                    buttonList.add(new AlphaButton(CATEGORY_BASE + 100, contentLeft, contentTop,
                            tileWidth, tileHeight, "PLATFORM ADMIN"));
                }
                if (hasFolderAccess(PermissionService.Rank.MANAGER)) {
                    buttonList.add(new AlphaButton(CATEGORY_BASE + 101, contentLeft + tileWidth + gap, contentTop,
                            tileWidth, tileHeight, "MANAGER"));
                }
            } else if (donatorSubfolders) {
                addDonatorSubfolderButton(0, PermissionService.Rank.SCOUT, "SCOUT", contentLeft, tileWidth, tileHeight);
                addDonatorSubfolderButton(1, PermissionService.Rank.ROGUE, "ROGUE", contentLeft + tileWidth + gap, tileWidth, tileHeight);
                addDonatorSubfolderButton(2, PermissionService.Rank.ELITE, "ELITE", contentLeft + (tileWidth + gap) * 2, tileWidth, tileHeight);
                addDonatorSubfolderButton(3, PermissionService.Rank.ALPHA, "ALPHA", contentLeft, tileWidth, tileHeight, contentTop + tileHeight + gap);
                addDonatorSubfolderButton(4, PermissionService.Rank.ALPHA_PLUS, "ALPHA+", contentLeft + tileWidth + gap, tileWidth, tileHeight, contentTop + tileHeight + gap);
            } else {
                List<Integer> visibleRankIndexes = new ArrayList<Integer>();
                for (int i = 0; i < RANKS.length; i++) {
                    if (isVisibleRank(RANKS[i])) visibleRankIndexes.add(i);
                }
                int rowHeight = tileHeight + gap;
                int visibleRankRows = Math.max(1, (contentBottom - contentTop) / rowHeight);
                int totalRankRows = (visibleRankIndexes.size() + columns - 1) / columns;
                rankScrollOffset = Math.max(0, Math.min(rankScrollOffset,
                        Math.max(0, totalRankRows - visibleRankRows)));
                int start = rankScrollOffset * columns;
                int end = Math.min(visibleRankIndexes.size(), start + visibleRankRows * columns);
                for (int index = start; index < end; index++) {
                    int rankIndex = visibleRankIndexes.get(index);
                    int local = index - start;
                    int x = contentLeft + (local % columns) * (tileWidth + gap);
                    int y = contentTop + (local / columns) * rowHeight;
                    buttonList.add(new AlphaButton(CATEGORY_BASE + rankIndex, x, y, tileWidth, tileHeight,
                            LABELS[rankIndex]));
                }
            }
        } else {
            visibleModules = getModulesForSelectedRank();
            int gap = 8;
            int columns = 3;
            int tileHeight = 36;
            int rowHeight = tileHeight + gap;
            visibleRows = Math.max(1, (contentBottom - contentTop) / rowHeight);
            int totalRows = (visibleModules.size() + columns - 1) / columns;
            int maxOffset = Math.max(0, totalRows - visibleRows);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
            int tileWidth = (contentRight - contentLeft - gap * (columns - 1)) / columns;
            int startIndex = scrollOffset * columns;
            int endIndex = Math.min(visibleModules.size(), startIndex + visibleRows * columns);
            for (int i = startIndex; i < endIndex; i++) {
                int column = (i - startIndex) % columns;
                int row = (i - startIndex) / columns;
                int x = contentLeft + column * (tileWidth + gap);
                int y = contentTop + row * rowHeight;
                AlphaModule module = visibleModules.get(i);
                buttonList.add(new AlphaButton(MODULE_BASE + i, x, y,
                        tileWidth, tileHeight, module.getName(), false, isModuleActive(module)));
            }
            if (selectedRank == PermissionService.Rank.PUBLIC) {
                int utilityIndex = visibleModules.size();
                int utilityRow = utilityIndex / columns - scrollOffset;
                if (utilityRow >= 0 && utilityRow < visibleRows) {
                    int x = contentLeft + (utilityIndex % columns) * (tileWidth + gap);
                    int y = contentTop + utilityRow * rowHeight;
                    buttonList.add(new AlphaButton(MODULE_BASE + 100, x, y,
                            tileWidth, tileHeight, "UTILITIES"));
                }

                // Staff Operations uses the same AlphaButton styling as General.
                // It is a separate hub so Bhop stays only in General.
                int operationsIndex = visibleModules.size() + 1;
                int operationsRow = operationsIndex / columns - scrollOffset;
                if (operationsRow >= 0 && operationsRow < visibleRows
                        && PermissionService.hasStaffModsAccess()) {
                    int x = contentLeft + (operationsIndex % columns) * (tileWidth + gap);
                    int y = contentTop + operationsRow * rowHeight;
                    buttonList.add(new AlphaButton(MODULE_BASE + 101, x, y,
                            tileWidth, tileHeight, "STAFF OPERATIONS", true));
                }
            }
        }

        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 94,
                panelTop + panelHeight - 31, 80, 20, "BACK"));
    }

    /** Uses each module's real runtime state for StaffMods active highlighting. */
    private boolean isModuleActive(AlphaModule module) {
        if (module == null) return false;
        switch (module.getId()) {
            case "staffmods":
                return BhopMode.isEnabled();
            case "w-tap-assistance":
                return WTapAssistance.isEnabled();
            case "s-tap-assistance":
                return STapAssistance.isEnabled();
            default:
                return module.isEnabled();
        }
    }

    private void addDonatorSubfolderButton(int index, PermissionService.Rank rank, String label,
                                           int x, int tileWidth, int tileHeight) {
        addDonatorSubfolderButton(index, rank, label, x, tileWidth, tileHeight, contentTop);
    }

    private void addDonatorSubfolderButton(int index, PermissionService.Rank rank, String label,
                                           int x, int tileWidth, int tileHeight, int y) {
        if (hasFolderAccess(rank)) {
            buttonList.add(new AlphaButton(CATEGORY_BASE + 102 + index, x, y, tileWidth, tileHeight, label));
        }
    }

    private boolean isVisibleRank(PermissionService.Rank rank) {
        if (rank == PermissionService.Rank.DEVELOPER) return hasFolderAccess(rank);
        Boolean openOverride = PermissionService.getCommandPermissionOverride("staffmods.open");
        Boolean explicitFolderPermission = PermissionService.getCommandPermissionOverride(folderPermissionKey(rank));
        if (Boolean.FALSE.equals(openOverride)) return Boolean.TRUE.equals(explicitFolderPermission);
        if (explicitFolderPermission != null) return explicitFolderPermission.booleanValue();
        // General is always available. Rank access keeps the existing hierarchy;
        // an explicit per-mod grant can independently reveal its assigned folder.
        if (rank == PermissionService.Rank.PUBLIC) return true;
        if (rank == PermissionService.Rank.DEVELOPER) return PermissionService.hasAccess(PermissionService.Rank.DEVELOPER);
        if (rank == PermissionService.Rank.FOUNDER) {
            return hasFolderAccess(rank);
        }
        // Beta and Donator are independent access folders, not staff hierarchy tiers.
        // Show them only to their rank or to accounts with a grant for a mod in that folder.
        if (rank == PermissionService.Rank.DONATOR) {
            Boolean override = PermissionService.getCommandPermissionOverride("menu.donator");
            if (override != null) return override.booleanValue();
            // Keep the Donator entry point exclusive to verified Partner accounts by default.
            return PermissionService.isOnlineRankVerified()
                    && PermissionService.getVerifiedOnlineRanks().contains(PermissionService.Rank.PARTNER);
        }
        if (rank == PermissionService.Rank.BETA) {
            return hasFolderAccess(rank);
        }
        if (rank == PermissionService.Rank.MANAGEMENT) {
            if (PermissionService.hasAccess(PermissionService.Rank.PLAT_ADMIN)
                    || PermissionService.hasAccess(PermissionService.Rank.MANAGER)) return true;
            return hasExplicitGrantInFolder(rank);
        }
        PermissionService.Rank effective = PermissionService.getEffectiveRank();
        return hierarchyPriority(effective) >= hierarchyPriority(rank) || hasExplicitGrantInFolder(rank);
    }

    private boolean hasExplicitGrantInFolder(PermissionService.Rank rank) {
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            if (module.getCategory() == AlphaModule.Category.FOUNDER) continue;
            // Bhop's staff.bhop grant unlocks only StaffMods -> General -> Bhop.
            // It must never reveal the T-Mod (or any other rank) folder.
            if ("staffmods".equals(module.getId())) continue;
            PermissionService.Rank required = module.getCategory().getRequiredRank();
            boolean matches = required == rank
                    || (rank == PermissionService.Rank.MANAGEMENT
                        && (required == PermissionService.Rank.PLAT_ADMIN
                            || required == PermissionService.Rank.MANAGER));
            if (matches && module.hasExplicitModAccess()) return true;
        }
        return false;
    }

    private boolean isFounderAccount() {
        return PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                || Boolean.TRUE.equals(PermissionService.getCommandPermissionOverride("staffmods.founder"));
    }

    private boolean hasFolderAccess(PermissionService.Rank rank) {
        if (isFounderAccount()) return true;
        String key = folderPermissionKey(rank);
        Boolean override = PermissionService.getCommandPermissionOverride(key);
        if (override != null) return override.booleanValue();
        return PermissionService.hasAccess(rank) || hasExplicitGrantInFolder(rank);
    }

    private String folderPermissionKey(PermissionService.Rank rank) {
        if (rank == null) return "staffmods.open";
        switch (rank) {
            case PUBLIC: return "staffmods.general";
            case BETA: return "menu.beta";
            case DONATOR: return "menu.donator";
            case SCOUT: return "menu.scout";
            case ROGUE: return "menu.rogue";
            case ELITE: return "menu.elite";
            case ALPHA: return "menu.alpha";
            case ALPHA_PLUS: return "menu.alphaplus";
            case T_MOD: return "staffmods.tmod";
            case MOD: return "staffmods.mod";
            case SR_MOD: return "staffmods.srmod";
            case ADMIN: return "staffmods.admin";
            case SR_ADMIN: return "staffmods.sradmin";
            case PLAT_ADMIN: return "staffmods.platformadmin";
            case MANAGEMENT: return "staffmods.management";
            case MANAGER: return "staffmods.manager";
            case DEVELOPER: return "staffmods.developer";
            case OWNER: return "staffmods.owner";
            case FOUNDER: return "staffmods.founder";
            default: return "staffmods.open";
        }
    }

    private String folderPermissionKey(String normalizedFolder) {
        if ("general".equals(normalizedFolder)) return "staffmods.general";
        if ("beta".equals(normalizedFolder)) return "menu.beta";
        if ("donator".equals(normalizedFolder)) return "menu.donator";
        if ("scout".equals(normalizedFolder)) return "menu.scout";
        if ("rogue".equals(normalizedFolder)) return "menu.rogue";
        if ("elite".equals(normalizedFolder)) return "menu.elite";
        if ("alpha".equals(normalizedFolder)) return "menu.alpha";
        if ("alphaplus".equals(normalizedFolder)) return "menu.alphaplus";
        if ("tmod".equals(normalizedFolder) || "trialmoderator".equals(normalizedFolder)) return "staffmods.tmod";
        if ("mod".equals(normalizedFolder) || "moderator".equals(normalizedFolder)) return "staffmods.mod";
        if ("srmod".equals(normalizedFolder) || "seniormoderator".equals(normalizedFolder)) return "staffmods.srmod";
        if ("admin".equals(normalizedFolder)) return "staffmods.admin";
        if ("sradmin".equals(normalizedFolder) || "senioradmin".equals(normalizedFolder)) return "staffmods.sradmin";
        if ("platformadmin".equals(normalizedFolder) || "platadmin".equals(normalizedFolder)) return "staffmods.platformadmin";
        if ("management".equals(normalizedFolder)) return "staffmods.management";
        if ("manager".equals(normalizedFolder)) return "staffmods.manager";
        if ("developer".equals(normalizedFolder)) return "staffmods.developer";
        if ("owner".equals(normalizedFolder)) return "staffmods.owner";
        if ("founder".equals(normalizedFolder)) return "staffmods.founder";
        return "staffmods.open";
    }

    private int hierarchyPriority(PermissionService.Rank rank) {
        switch (rank) {
            case FOUNDER: return 18;
            case OWNER: return 17;
            case DEVELOPER: return 16;
            case MANAGER: return 15;
            case PLAT_ADMIN:
            case MANAGEMENT: return 14;
            case SR_ADMIN: return 13;
            case ADMIN: return 12;
            case SR_MOD: return 11;
            case MOD: return 10;
            case T_MOD: return 9;
            case THE_NIBBLER: return 8;
            case BETA: return 7;
            case ALPHA_PLUS: return 6;
            case ALPHA: return 5;
            case ELITE: return 4;
            case ROGUE: return 3;
            case SCOUT: return 2;
            case PUBLIC: return 0;
            default: return -1;
        }
    }

    private List<AlphaModule> getModulesForSelectedRank() {
        List<AlphaModule> modules = new ArrayList<AlphaModule>();

        if (selectedRank == PermissionService.Rank.PUBLIC) {
            // Bhop is only exposed in General and only when its explicit permission allows it.
            AlphaModule bhop = AlphaModuleRegistry.find("staffmods");
            if (bhop != null && bhop.isUnlocked()) modules.add(bhop);
            return modules;
        }

        // Owner and Founder are permission tiers, not destinations for ordinary modules.
        // Their own folders must not duplicate modules from lower-rank folders.
        // Founder-only modules remain in Founder; Owner has no Owner-specific module list.
        if (selectedRank == PermissionService.Rank.OWNER) {
            return modules;
        }

        // Beta and Donator modules belong only in their own folders. Rank access
        // or an explicit grant unlocks only the matching module, not the whole category.
        if (selectedRank == PermissionService.Rank.BETA
                || selectedRank == PermissionService.Rank.DONATOR
                || selectedRank == PermissionService.Rank.SCOUT
                || selectedRank == PermissionService.Rank.ROGUE
                || selectedRank == PermissionService.Rank.ELITE
                || selectedRank == PermissionService.Rank.ALPHA
                || selectedRank == PermissionService.Rank.ALPHA_PLUS) {
            AlphaModule.Category selectedCategory = categoryForRank(selectedRank);
            for (AlphaModule module : AlphaModuleRegistry.getAll()) {
                if (module.getCategory() == selectedCategory && module.isUnlocked()) {
                    modules.add(module);
                }
            }
            return modules;
        }

        int selectedLevel = staffFolderLevel(selectedRank);
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            if ("staffmods".equals(module.getId())) continue;

            // AimAssist is listed only in the authenticated Founder's folder.
            if ("aim-assist".equals(module.getId())) {
                if (selectedRank == PermissionService.Rank.FOUNDER
                        && com.alpharip.staffmod.permissions.SupabaseCommunityService.isSignedInFounder()
                        && module.isImplemented() && module.isUnlocked()) {
                    modules.add(module);
                }
                continue;
            }
            if ("w-tap-assistance".equals(module.getId())) {
                if (selectedRank == PermissionService.Rank.FOUNDER
                        && PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                        && module.isUnlocked()) {
                    modules.add(module);
                }
                continue;
            }

            if (!module.isUnlocked()) continue;
            if (module.getCategory() == AlphaModule.Category.FOUNDER) {
                if (selectedRank == PermissionService.Rank.FOUNDER
                        && PermissionService.hasAccess(PermissionService.Rank.FOUNDER)) modules.add(module);
                continue;
            }

            // Founder can open all lower-rank folders through rank access, but
            // ordinary modules are shown only in their assigned folder.
            // Founder-only modules were handled above and stay exclusive.
            PermissionService.Rank required = module.getCategory().getRequiredRank();
            int moduleLevel = staffFolderLevel(required);
            if (moduleLevel >= 0 && selectedLevel >= moduleLevel) {
                modules.add(module);
            }
        }
        return modules;
    }

    private int staffFolderLevel(PermissionService.Rank rank) {
        switch (rank) {
            case FOUNDER: return 9;
            case BETA:
            case DONATOR: return -1;
            case OWNER: return 8;
            case MANAGER: return 7;
            case PLAT_ADMIN:
            case MANAGEMENT: return 6;
            case SR_ADMIN: return 5;
            case ADMIN: return 4;
            case SR_MOD: return 3;
            case MOD: return 2;
            case T_MOD: return 1;
            default: return -1;
        }
    }

    private AlphaModule.Category categoryForRank(PermissionService.Rank rank) {
        switch (rank) {
            case BETA: return AlphaModule.Category.BETA;
            case SCOUT: return AlphaModule.Category.SCOUT;
            case ROGUE: return AlphaModule.Category.ROGUE;
            case ELITE: return AlphaModule.Category.ELITE;
            case ALPHA: return AlphaModule.Category.ALPHA;
            case ALPHA_PLUS: return AlphaModule.Category.ALPHA_PLUS;
            case DONATOR: return AlphaModule.Category.DONATOR;
            case THE_NIBBLER: return AlphaModule.Category.T_MOD;
            case T_MOD: return AlphaModule.Category.T_MOD;
            case MOD: return AlphaModule.Category.MOD;
            case SR_MOD: return AlphaModule.Category.SR_MOD;
            case ADMIN: return AlphaModule.Category.ADMIN;
            case SR_ADMIN: return AlphaModule.Category.SR_ADMIN;
            case MANAGEMENT: return AlphaModule.Category.MANAGEMENT;
            case PLAT_ADMIN: return AlphaModule.Category.PLAT_ADMIN;
            case MANAGER: return AlphaModule.Category.MANAGER;
            case OWNER: return AlphaModule.Category.OWNER;
            case FOUNDER: return AlphaModule.Category.FOUNDER;
            default: return null;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (!PermissionService.hasStaffModsAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (button.id == BACK) {
            // Always replace the current screen rather than reinitializing a stale button list.
            // Folder contents -> fresh rank chooser; rank chooser -> parent Mods menu.
            if (openFolderRank != null) {
                if (selectedRank == PermissionService.Rank.PLAT_ADMIN
                        || selectedRank == PermissionService.Rank.MANAGER) {
                    StaffModsMenuGui previous = new StaffModsMenuGui(parent);
                    previous.managementSubfolders = true;
                    mc.displayGuiScreen(previous);
                } else if (isDonatorRank(selectedRank)) {
                    StaffModsMenuGui previous = new StaffModsMenuGui(parent);
                    previous.donatorSubfolders = true;
                    mc.displayGuiScreen(previous);
                } else {
                    mc.displayGuiScreen(new StaffModsMenuGui(parent));
                }
            } else if (managementSubfolders || donatorSubfolders) {
                mc.displayGuiScreen(new StaffModsMenuGui(parent));
            } else {
                mc.displayGuiScreen(parent);
            }
            return;
        }

        if (openFolderRank == null && donatorSubfolders
                && button.id >= CATEGORY_BASE + 102 && button.id <= CATEGORY_BASE + 106) {
            PermissionService.Rank[] donorRanks = {
                PermissionService.Rank.SCOUT, PermissionService.Rank.ROGUE, PermissionService.Rank.ELITE,
                PermissionService.Rank.ALPHA, PermissionService.Rank.ALPHA_PLUS
            };
            PermissionService.Rank rank = donorRanks[button.id - (CATEGORY_BASE + 102)];
            if (hasFolderAccess(rank)) {
                selectedRank = rank;
                openFolderRank = rank;
                donatorSubfolders = false;
                scrollOffset = 0;
                initGui();
            }
            return;
        }

        if (openFolderRank == null && managementSubfolders
                && (button.id == CATEGORY_BASE + 100 || button.id == CATEGORY_BASE + 101)) {
            PermissionService.Rank rank = button.id == CATEGORY_BASE + 100
                    ? PermissionService.Rank.PLAT_ADMIN : PermissionService.Rank.MANAGER;
            if (hasFolderAccess(rank)) {
                selectedRank = rank;
                openFolderRank = rank;
                managementSubfolders = false;
                scrollOffset = 0;
                initGui();
            }
            return;
        }

        if (openFolderRank == null && !managementSubfolders
                && button.id >= CATEGORY_BASE && button.id < CATEGORY_BASE + RANKS.length) {
            PermissionService.Rank rank = RANKS[button.id - CATEGORY_BASE];
            if (rank == PermissionService.Rank.PUBLIC
                    || hasFolderAccess(rank)) {
                if (rank == PermissionService.Rank.DEVELOPER) {
                    // Founder must be able to open Developer even when no separate
                    // Developer rank or folder override is stored for the account.
                    boolean developerAccess = PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                            || Boolean.TRUE.equals(PermissionService.getCommandPermissionOverride("staffmods.founder"))
                            || PermissionService.hasAccess(PermissionService.Rank.DEVELOPER)
                            || Boolean.TRUE.equals(PermissionService.getCommandPermissionOverride("staffmods.developer"));
                    if (developerAccess) {
                        // hasFolderAccess(rank) has already passed above. Carry that
                        // authorization into DeveloperGui instead of re-checking it
                        // against a narrower set of rank flags.
                        mc.displayGuiScreen(new DeveloperGui(this, true));
                    }
                } else if (rank == PermissionService.Rank.MANAGEMENT) {
                    managementSubfolders = true;
                    initGui();
                } else if (rank == PermissionService.Rank.DONATOR) {
                    donatorSubfolders = true;
                    initGui();
                } else {
                    selectedRank = rank;
                    openFolderRank = rank;
                    scrollOffset = 0;
                    initGui();
                }
            }
            return;
        }

        if (openFolderRank != null && button.id == MODULE_BASE + 100
                && selectedRank == PermissionService.Rank.PUBLIC) {
            mc.displayGuiScreen(new UtilitiesGui(this));
            return;
        }

        if (openFolderRank != null && button.id == MODULE_BASE + 101
                && selectedRank == PermissionService.Rank.PUBLIC) {
            if (PermissionService.hasStaffModsAccess()) {
                mc.displayGuiScreen(new StaffOperationsGui(this));
            }
            return;
        }

        if (openFolderRank != null && button.id >= MODULE_BASE && button.id < MODULE_BASE + 100) {
            // Re-check access at click time; hiding a folder in the UI is not security.
            if (selectedRank == PermissionService.Rank.FOUNDER
                    && !PermissionService.hasAccess(PermissionService.Rank.FOUNDER)) return;
            int index = button.id - MODULE_BASE;
            if (index < 0 || index >= visibleModules.size()) return;
            AlphaModule module = visibleModules.get(index);
            if ("aim-assist".equals(module.getId())) {
                if (selectedRank == PermissionService.Rank.FOUNDER
                        && com.alpharip.staffmod.permissions.SupabaseCommunityService.isSignedInFounder()
                        && module.isImplemented() && module.isUnlocked()) {
                    mc.displayGuiScreen(new AimAssistSettingsGui(this));
                }
                return;
            }
            if ("w-tap-assistance".equals(module.getId())) {
                if (selectedRank == PermissionService.Rank.FOUNDER
                        && PermissionService.hasAccess(PermissionService.Rank.FOUNDER)) {
                    mc.displayGuiScreen(new WTapSettingsGui(this));
                }
                return;
            }
            if ("s-tap-assistance".equals(module.getId())) {
                if (selectedRank == PermissionService.Rank.FOUNDER
                        && PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                        && module.isImplemented() && module.isUnlocked()) {
                    mc.displayGuiScreen(new STapSettingsGui(this));
                }
                return;
            }
            if (!module.isUnlocked()) return;
            if ("staffmods".equals(module.getId())) {
                mc.displayGuiScreen(new BhopSettingsGui(this));
                return;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        int right = panelLeft + panelWidth;
        int bottom = panelTop + panelHeight;
        drawRect(panelLeft, panelTop, right, bottom, 0xE00B0D12);
        drawRect(panelLeft, panelTop, right, panelTop + 1, ACCENT);
        drawRect(panelLeft, panelTop + 1, panelLeft + 1, bottom - 1, 0x554B515D);
        drawRect(right - 1, panelTop + 1, right, bottom - 1, 0x554B515D);

        drawString(fontRendererObj, "ALPHA CLIENT", panelLeft + 14, panelTop + 14, 0xFFF0F1F4);
        drawString(fontRendererObj, "STAFFMODS", panelLeft + 14, panelTop + 34, ACCENT);
        // AlphaButton renders each label itself. Do not draw a second copy of
        // folder/module cards here; that caused overlapping duplicate text.
        if (openFolderRank != null) {
            String folderLabel = folderLabel(selectedRank);
            drawString(fontRendererObj, folderLabel + "  /  MODS",
                    contentLeft, panelTop + 57, ACCENT);
        } else if (managementSubfolders) {
            drawString(fontRendererObj, "MANAGEMENT", contentLeft, panelTop + 57, ACCENT);
        } else {
            drawString(fontRendererObj, "STAFF RANKS MODS", contentLeft, panelTop + 57, 0xFFB8BDC8);
        }
        drawString(fontRendererObj, "RANK ACCESS: " + PermissionService.getEffectiveRank().name(),
                panelLeft + 14, bottom - 22, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawFolderCards(int mouseX, int mouseY) {
        int gap = 8;
        int columns = 3;
        int tileWidth = (contentRight - contentLeft - gap * (columns - 1)) / columns;
        int tileHeight = 38;
        int index = 0;
        for (int i = 0; i < RANKS.length; i++) {
            PermissionService.Rank rank = RANKS[i];
            if (!isVisibleRank(rank)) continue;
            int x = contentLeft + (index % columns) * (tileWidth + gap);
            int y = contentTop + (index / columns) * (tileHeight + gap);
            drawCard(x, y, tileWidth, tileHeight, LABELS[i], mouseX, mouseY);
            index++;
        }
    }

    private void drawManagementSubfolders(int mouseX, int mouseY) {
        int gap = 8;
        int columns = 3;
        int tileWidth = (contentRight - contentLeft - gap * (columns - 1)) / columns;
        int tileHeight = 38;
        if (hasFolderAccess(PermissionService.Rank.PLAT_ADMIN)) {
            drawCard(contentLeft, contentTop, tileWidth, tileHeight, "PLATFORM ADMIN", mouseX, mouseY);
        }
        if (hasFolderAccess(PermissionService.Rank.MANAGER)) {
            drawCard(contentLeft + tileWidth + gap, contentTop, tileWidth, tileHeight, "MANAGER", mouseX, mouseY);
        }
    }

    private void drawDonatorSubfolders(int mouseX, int mouseY) {
        int gap = 8;
        int columns = 3;
        int tileWidth = (contentRight - contentLeft - gap * (columns - 1)) / columns;
        int tileHeight = 38;
        PermissionService.Rank[] ranks = {
            PermissionService.Rank.SCOUT, PermissionService.Rank.ROGUE, PermissionService.Rank.ELITE,
            PermissionService.Rank.ALPHA, PermissionService.Rank.ALPHA_PLUS
        };
        String[] labels = { "SCOUT", "ROGUE", "ELITE", "ALPHA", "ALPHA+" };
        for (int i = 0; i < ranks.length; i++) {
            if (!hasFolderAccess(ranks[i])) continue;
            int x = contentLeft + (i % columns) * (tileWidth + gap);
            int y = contentTop + (i / columns) * (tileHeight + gap);
            drawCard(x, y, tileWidth, tileHeight, labels[i], mouseX, mouseY);
        }
    }

    private boolean isDonatorRank(PermissionService.Rank rank) {
        return rank == PermissionService.Rank.SCOUT || rank == PermissionService.Rank.ROGUE
                || rank == PermissionService.Rank.ELITE || rank == PermissionService.Rank.ALPHA
                || rank == PermissionService.Rank.ALPHA_PLUS;
    }

    private void drawFolderContents(int mouseX, int mouseY) {
        for (int i = scrollOffset; i < visibleModules.size() && i < scrollOffset + visibleRows; i++) {
            int y = contentTop + (i - scrollOffset) * 44;
            drawCard(contentLeft, y, contentRight - contentLeft, 36,
                    visibleModules.get(i).getName(), mouseX, mouseY);
        }
        if (selectedRank == PermissionService.Rank.PUBLIC
                && visibleModules.size() - scrollOffset < visibleRows) {
            int y = contentTop + (visibleModules.size() - scrollOffset) * 44;
            drawCard(contentLeft, y, contentRight - contentLeft, 36,
                    "UTILITIES", mouseX, mouseY);
        }
        if (visibleModules.isEmpty() && selectedRank != PermissionService.Rank.PUBLIC) {
            drawString(fontRendererObj, "No modules are registered in this folder yet.",
                    contentLeft, contentTop + 8, 0xFFB8BDC8);
        }
    }

    private void drawCard(int x, int y, int w, int h, String title, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int background = hovered ? 0xFF252A33 : 0xD9161820;
        int border = hovered ? ACCENT : 0x664B515D;
        drawRect(x, y, x + w, y + h, background);
        drawRect(x, y, x + w, y + 1, border);
        drawRect(x, y + h - 1, x + w, y + h, border);
        drawRect(x, y, x + 1, y + h, border);
        drawRect(x + w - 1, y, x + w, y + h, border);
        int titleX = x + (w - fontRendererObj.getStringWidth(title)) / 2;
        int titleY = y + (h - fontRendererObj.FONT_HEIGHT) / 2;
        drawString(fontRendererObj, title, titleX, titleY, 0xFFFFFFFF);
    }

    private String folderLabel(PermissionService.Rank rank) {
        if (rank == PermissionService.Rank.PLAT_ADMIN) return "PLATFORM ADMIN";
        if (rank == PermissionService.Rank.MANAGER) return "MANAGER";
        if (rank == PermissionService.Rank.SCOUT) return "SCOUT";
        if (rank == PermissionService.Rank.ROGUE) return "ROGUE";
        if (rank == PermissionService.Rank.ELITE) return "ELITE";
        if (rank == PermissionService.Rank.ALPHA) return "ALPHA";
        if (rank == PermissionService.Rank.ALPHA_PLUS) return "ALPHA+";
        return LABELS[indexOf(rank)];
    }

    private int indexOf(PermissionService.Rank rank) {
        for (int i = 0; i < RANKS.length; i++) if (RANKS[i] == rank) return i;
        return 0;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && openFolderRank == null && !managementSubfolders && !donatorSubfolders
                && Mouse.getX() * width / mc.displayWidth >= contentLeft
                && Mouse.getX() * width / mc.displayWidth <= contentRight
                && height - Mouse.getY() * height / mc.displayHeight - 1 >= contentTop
                && height - Mouse.getY() * height / mc.displayHeight - 1 <= contentBottom) {
            int visibleCount = 0;
            for (PermissionService.Rank rank : RANKS) if (isVisibleRank(rank)) visibleCount++;
            int visibleRankRows = Math.max(1, (contentBottom - contentTop) / 46);
            int maxRankOffset = Math.max(0, (visibleCount + 2) / 3 - visibleRankRows);
            rankScrollOffset = Math.max(0, Math.min(maxRankOffset, rankScrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
            return;
        }
        if (openFolderRank != null && wheel != 0) {
            int columns = 3;
            int totalRows = (visibleModules.size() + columns - 1) / columns;
            int maxOffset = Math.max(0, totalRows - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            // Escape always closes the entire GUI, regardless of the current folder.
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
