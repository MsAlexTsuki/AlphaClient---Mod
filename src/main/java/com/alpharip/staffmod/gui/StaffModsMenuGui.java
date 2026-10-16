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

/**
 * StaffMods folder browser. Rank folders contain the modules assigned to them.
 * Existing module settings screens are reused instead of creating duplicate settings.
 */
public final class StaffModsMenuGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int CATEGORY_BASE = 20;
    private static final int MODULE_BASE = 50;
    private static final int ACCENT = 0xFFFFD866;

    private static final PermissionService.Rank[] RANKS = {
        PermissionService.Rank.PUBLIC,
        PermissionService.Rank.T_MOD,
        PermissionService.Rank.MOD,
        PermissionService.Rank.SR_MOD,
        PermissionService.Rank.ADMIN,
        PermissionService.Rank.SR_ADMIN,
        PermissionService.Rank.MANAGEMENT,
        PermissionService.Rank.OWNER,
        PermissionService.Rank.FOUNDER
    };
    private static final String[] LABELS = {
        "GENERAL", "T-MOD", "MOD", "SR MOD", "ADMIN",
        "SR ADMIN", "MANAGEMENT", "OWNER", "FOUNDER"
    };

    private final GuiScreen parent;
    private PermissionService.Rank selectedRank = PermissionService.Rank.PUBLIC;
    private PermissionService.Rank openFolderRank;
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
            int index = 0;
            for (int i = 0; i < RANKS.length; i++) {
                PermissionService.Rank rank = RANKS[i];
                if (rank != PermissionService.Rank.PUBLIC && !PermissionService.hasAccess(rank)) {
                    continue;
                }
                int x = contentLeft + (index % columns) * (tileWidth + gap);
                int y = contentTop + (index / columns) * (tileHeight + gap);
                buttonList.add(new AlphaButton(CATEGORY_BASE + i, x, y, tileWidth, tileHeight,
                        LABELS[i]));
                index++;
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

    private List<AlphaModule> getModulesForSelectedRank() {
        List<AlphaModule> modules = new ArrayList<AlphaModule>();
        if (selectedRank == PermissionService.Rank.PUBLIC) {
            // BHOP remains available from General through its existing settings screen.
            AlphaModule bhop = AlphaModuleRegistry.find("staffmods");
            if (bhop != null) modules.add(bhop);
            return modules;
        }

        AlphaModule.Category category = categoryForRank(selectedRank);
        if (category == null) return modules;
        for (AlphaModule module : AlphaModuleRegistry.getForCategory(category)) {
            if (PermissionService.hasAccess(selectedRank)
                    && PermissionService.hasAccess(category.getRequiredRank())) {
                modules.add(module);
            }
        }
        return modules;
    }

    private AlphaModule.Category categoryForRank(PermissionService.Rank rank) {
        switch (rank) {
            case T_MOD: return AlphaModule.Category.T_MOD;
            case MOD: return AlphaModule.Category.MOD;
            case SR_MOD: return AlphaModule.Category.SR_MOD;
            case ADMIN: return AlphaModule.Category.ADMIN;
            case SR_ADMIN: return AlphaModule.Category.SR_ADMIN;
            case MANAGEMENT: return AlphaModule.Category.MANAGEMENT;
            case OWNER: return AlphaModule.Category.OWNER;
            case FOUNDER: return AlphaModule.Category.FOUNDER;
            default: return null;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        if (button.id == BACK) {
            if (openFolderRank != null) {
                openFolderRank = null;
                scrollOffset = 0;
                initGui();
            } else {
                mc.displayGuiScreen(parent);
            }
            return;
        }

        if (openFolderRank == null && button.id >= CATEGORY_BASE && button.id < CATEGORY_BASE + RANKS.length) {
            PermissionService.Rank rank = RANKS[button.id - CATEGORY_BASE];
            if (rank == PermissionService.Rank.PUBLIC || PermissionService.hasAccess(rank)) {
                selectedRank = rank;
                openFolderRank = rank;
                scrollOffset = 0;
                initGui();
            }
            return;
        }

        if (openFolderRank != null && button.id == MODULE_BASE + 100
                && selectedRank == PermissionService.Rank.PUBLIC) {
            mc.displayGuiScreen(new UtilitiesGui(this));
            return;
        }

        if (openFolderRank != null && button.id >= MODULE_BASE && button.id < MODULE_BASE + 100) {
            int index = button.id - MODULE_BASE;
            if (index < 0 || index >= visibleModules.size()) return;
            AlphaModule module = visibleModules.get(index);
            if (selectedRank != PermissionService.Rank.PUBLIC
                    && !PermissionService.hasAccess(module.getCategory().getRequiredRank())) return;
            if ("staffmods".equals(module.getId())) {
                mc.displayGuiScreen(new BhopSettingsGui(this));
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
            drawString(fontRendererObj, LABELS[indexOf(selectedRank)] + "  /  MODS",
                    contentLeft, panelTop + 57, ACCENT);
            drawFolderContents(mouseX, mouseY);
        } else {
            drawString(fontRendererObj, "SELECT A FOLDER", contentLeft, panelTop + 57, 0xFFB8BDC8);
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
            if (rank != PermissionService.Rank.PUBLIC && !PermissionService.hasAccess(rank)) continue;
            int x = contentLeft + (index % columns) * (tileWidth + gap);
            int y = contentTop + (index / columns) * (tileHeight + gap);
            drawCard(x, y, tileWidth, tileHeight, LABELS[i], mouseX, mouseY);
            index++;
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
            // Escape always closes the entire StaffMods menu and returns directly to gameplay.
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
