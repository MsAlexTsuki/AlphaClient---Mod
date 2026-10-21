package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
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
public final class StaffModsMenuGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int CATEGORY_BASE = 20;
    private static final int MODULE_BASE = 50;
    private static final int ACCENT = 0xFFFFD866;

    // Keep every rank folder in canonical hierarchy order. Visibility is inherited downward.
    private static final PermissionService.Rank[] RANKS = {
        PermissionService.Rank.PUBLIC,
        PermissionService.Rank.FOUNDER,
        PermissionService.Rank.OWNER,
        PermissionService.Rank.MANAGEMENT,
        PermissionService.Rank.SR_ADMIN,
        PermissionService.Rank.ADMIN,
        PermissionService.Rank.SR_MOD,
        PermissionService.Rank.MOD,
        PermissionService.Rank.T_MOD
    };
    private static final String[] LABELS = {
        "GENERAL", "FOUNDER", "OWNER", "MANAGEMENT",
        "SR ADMIN", "ADMIN", "SR MOD", "MOD", "T-MOD"
    };

    private final GuiScreen parent;
    private PermissionService.Rank selectedRank = PermissionService.Rank.PUBLIC;
    private PermissionService.Rank openFolderRank;
    private boolean managementSubfolders;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int contentLeft, contentTop, contentRight, contentBottom;
    private int scrollOffset;
    private int visibleRows;
    private List<AlphaModule> visibleModules = new ArrayList<AlphaModule>();

    public StaffModsMenuGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
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
                if (PermissionService.hasAccess(PermissionService.Rank.PLAT_ADMIN)) {
                    buttonList.add(new AlphaButton(CATEGORY_BASE + 100, contentLeft, contentTop,
                            tileWidth, tileHeight, "PLATFORM ADMIN"));
                }
                if (PermissionService.hasAccess(PermissionService.Rank.MANAGER)) {
                    buttonList.add(new AlphaButton(CATEGORY_BASE + 101, contentLeft + tileWidth + gap, contentTop,
                            tileWidth, tileHeight, "MANAGER"));
                }
            } else {
                int index = 0;
                for (int i = 0; i < RANKS.length; i++) {
                    PermissionService.Rank rank = RANKS[i];
                    if (!isVisibleRank(rank)) {
                        continue;
                    }
                    int x = contentLeft + (index % columns) * (tileWidth + gap);
                    int y = contentTop + (index / columns) * (tileHeight + gap);
                    buttonList.add(new AlphaButton(CATEGORY_BASE + i, x, y, tileWidth, tileHeight,
                            LABELS[i]));
                    index++;
                }
            }
        } else {
            visibleModules = getModulesForSelectedRank();
            int gap = 8;
            int tileHeight = 36;
            int rowHeight = tileHeight + gap;
            visibleRows = Math.max(1, (contentBottom - contentTop) / rowHeight);
            int maxOffset = Math.max(0, visibleModules.size() - visibleRows);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
            int tileWidth = contentRight - contentLeft;
            for (int i = scrollOffset; i < visibleModules.size() && i < scrollOffset + visibleRows; i++) {
                int y = contentTop + (i - scrollOffset) * rowHeight;
                buttonList.add(new AlphaButton(MODULE_BASE + i, contentLeft, y,
                        tileWidth, tileHeight, visibleModules.get(i).getName()));
            }
            if (selectedRank == PermissionService.Rank.PUBLIC) {
                int y = contentTop + (visibleModules.size() - scrollOffset) * rowHeight;
                if (visibleModules.size() - scrollOffset < visibleRows) {
                    buttonList.add(new AlphaButton(MODULE_BASE + 100, contentLeft, y,
                            tileWidth, tileHeight, "UTILITIES"));
                }
            }
        }

        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 94,
                panelTop + panelHeight - 31, 80, 20, "BACK"));
    }

    private boolean isVisibleRank(PermissionService.Rank rank) {
        // General is always available. Every rank can see its own folder and all folders
        // below it in the canonical hierarchy; higher folders remain hidden.
        if (rank == PermissionService.Rank.PUBLIC) return true;
        if (rank == PermissionService.Rank.FOUNDER) {
            return PermissionService.hasAccess(PermissionService.Rank.FOUNDER);
        }
        if (rank == PermissionService.Rank.MANAGEMENT) {
            return PermissionService.hasAccess(PermissionService.Rank.PLAT_ADMIN)
                    || PermissionService.hasAccess(PermissionService.Rank.MANAGER);
        }
        PermissionService.Rank effective = PermissionService.getEffectiveRank();
        return hierarchyPriority(effective) >= hierarchyPriority(rank);
    }

    private int hierarchyPriority(PermissionService.Rank rank) {
        switch (rank) {
            case FOUNDER: return 17;
            case OWNER: return 16;
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

        // Founder sees every registered module except Bhop, which belongs only in General.
        // Owner sees every non-Founder module. Other staff ranks see modules belonging
        // to their own staff level and every lower staff level, not just directly assigned ranks.
        int selectedLevel = staffFolderLevel(selectedRank);
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            if ("staffmods".equals(module.getId())) continue;
            if (!module.isUnlocked()) continue;

            if (module.getCategory() == AlphaModule.Category.FOUNDER) {
                if (selectedRank == PermissionService.Rank.FOUNDER) modules.add(module);
                continue;
            }

            if (selectedRank == PermissionService.Rank.FOUNDER
                    || selectedRank == PermissionService.Rank.OWNER) {
                modules.add(module);
                continue;
            }

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
            case ALPHA_PLUS:
            case ALPHA:
            case ELITE:
            case ROGUE:
            case SCOUT: return AlphaModule.Category.DONATOR;
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

        if (button.id == BACK) {
            // Always replace the current screen rather than reinitializing a stale button list.
            // Folder contents -> fresh rank chooser; rank chooser -> parent Mods menu.
            if (openFolderRank != null) {
                if (selectedRank == PermissionService.Rank.PLAT_ADMIN
                        || selectedRank == PermissionService.Rank.MANAGER) {
                    StaffModsMenuGui previous = new StaffModsMenuGui(parent);
                    previous.managementSubfolders = true;
                    mc.displayGuiScreen(previous);
                } else {
                    mc.displayGuiScreen(new StaffModsMenuGui(parent));
                }
            } else if (managementSubfolders) {
                mc.displayGuiScreen(new StaffModsMenuGui(parent));
            } else {
                mc.displayGuiScreen(parent);
            }
            return;
        }

        if (openFolderRank == null && managementSubfolders
                && (button.id == CATEGORY_BASE + 100 || button.id == CATEGORY_BASE + 101)) {
            PermissionService.Rank rank = button.id == CATEGORY_BASE + 100
                    ? PermissionService.Rank.PLAT_ADMIN : PermissionService.Rank.MANAGER;
            if (PermissionService.hasAccess(rank)) {
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
            if (rank == PermissionService.Rank.PUBLIC || PermissionService.hasAccess(rank)) {
                if (rank == PermissionService.Rank.MANAGEMENT) {
                    managementSubfolders = true;
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

        if (openFolderRank != null && button.id >= MODULE_BASE && button.id < MODULE_BASE + 100) {
            // Re-check access at click time; hiding a folder in the UI is not security.
            if (selectedRank == PermissionService.Rank.FOUNDER
                    && !PermissionService.hasAccess(PermissionService.Rank.FOUNDER)) return;
            int index = button.id - MODULE_BASE;
            if (index < 0 || index >= visibleModules.size()) return;
            AlphaModule module = visibleModules.get(index);
            if ("w-tap-assistance".equals(module.getId())) {
                if (selectedRank == PermissionService.Rank.FOUNDER
                        && PermissionService.hasAccess(PermissionService.Rank.FOUNDER)) {
                    mc.displayGuiScreen(new WTapSettingsGui(this));
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
        if (openFolderRank != null) {
            String folderLabel = selectedRank == PermissionService.Rank.PLAT_ADMIN
                    ? "PLATFORM ADMIN" : selectedRank == PermissionService.Rank.MANAGER
                    ? "MANAGER" : LABELS[indexOf(selectedRank)];
            drawString(fontRendererObj, folderLabel + "  /  MODS",
                    contentLeft, panelTop + 57, ACCENT);
            drawFolderContents(mouseX, mouseY);
        } else if (managementSubfolders) {
            drawString(fontRendererObj, "MANAGEMENT", contentLeft, panelTop + 57, ACCENT);
            drawManagementSubfolders(mouseX, mouseY);
        } else {
            drawString(fontRendererObj, "STAFF RANKS MODS", contentLeft, panelTop + 57, 0xFFB8BDC8);
            drawFolderCards(mouseX, mouseY);
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
        if (PermissionService.hasAccess(PermissionService.Rank.PLAT_ADMIN)) {
            drawCard(contentLeft, contentTop, tileWidth, tileHeight, "PLATFORM ADMIN", mouseX, mouseY);
        }
        if (PermissionService.hasAccess(PermissionService.Rank.MANAGER)) {
            drawCard(contentLeft + tileWidth + gap, contentTop, tileWidth, tileHeight, "MANAGER", mouseX, mouseY);
        }
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

    private int indexOf(PermissionService.Rank rank) {
        for (int i = 0; i < RANKS.length; i++) if (RANKS[i] == rank) return i;
        return 0;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (openFolderRank != null && wheel != 0) {
            int maxOffset = Math.max(0, visibleModules.size() - visibleRows);
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
