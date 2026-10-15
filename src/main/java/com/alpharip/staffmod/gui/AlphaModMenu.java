package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** Compact category, module-list, and module-details interface for AlphaClient. */
public final class AlphaModMenu extends GuiScreen {
    private static final int CATEGORY_BASE = 20;
    private static final int MODULE_BASE = 100;
    private static final int BACK = 300;
    private static final int COMMUNITY_LOGIN = 301;
    private static final int TOGGLE_MODULE = 302;
    private static final int STAFF_MENU = 303;

    private static final int ACCENT = 0xFFFFD866;
    private final GuiScreen parent;

    private AlphaModule.Category selectedCategory = AlphaModule.Category.PUBLIC;
    private java.util.List<AlphaModule.Category> visibleCategories;
    private String selectedModuleId = "alpha_hud";
    private int scrollOffset;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int listX;
    private int listWidth;
    private int listTop;
    private int listBottom;
    private int detailX;
    private int detailRight;

    public AlphaModMenu(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        panelWidth = Math.min(720, Math.max(300, width - 28));
        panelHeight = Math.min(410, Math.max(250, height - 34));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;

        int panelRight = panelLeft + panelWidth;
        listX = panelLeft + 16;
        listWidth = Math.max(112, Math.min(210, (panelWidth - 54) / 3));
        listTop = panelTop + 104;
        listBottom = panelTop + panelHeight - 48;
        detailX = listX + listWidth + 14;
        detailRight = panelRight - 16;

        // General is the only top-level collection. Restricted collections live in its footer.
        visibleCategories = new java.util.ArrayList<AlphaModule.Category>();
        visibleCategories.add(AlphaModule.Category.PUBLIC);
        if (selectedCategory != AlphaModule.Category.PUBLIC
                && !PermissionService.hasAccess(selectedCategory.getRequiredRank())) {
            selectedCategory = AlphaModule.Category.PUBLIC;
        }
        if (selectedCategory == AlphaModule.Category.STAFF) selectedCategory = AlphaModule.Category.PUBLIC;
        buttonList.add(new AlphaButton(CATEGORY_BASE, panelLeft + 16, panelTop + 67,
                108, 20, "GENERAL", selectedCategory == AlphaModule.Category.PUBLIC));

        int accessX = panelLeft + 16;
        int accessY = panelTop + panelHeight - 54;
        int accessWidth = 82;
        if (PermissionService.hasAccess(PermissionService.Rank.BETA)) {
            buttonList.add(new AlphaButton(CATEGORY_BASE + 1, accessX, accessY, accessWidth, 18,
                    "BETA"));
            accessX += accessWidth + 5;
        }
        if (PermissionService.hasAccess(PermissionService.Rank.DONATOR)) {
            buttonList.add(new AlphaButton(CATEGORY_BASE + 2, accessX, accessY, accessWidth, 18,
                    "DONATOR"));
            accessX += accessWidth + 5;
        }
        if (PermissionService.hasModAccess("staff.bhop")) {
            buttonList.add(new AlphaButton(STAFF_MENU, accessX, accessY,
                    Math.min(112, panelRight - 16 - accessX), 18, "STAFF MODS"));
        }

        List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
        int rowHeight = 31;
        int rowGap = 5;
        int visibleCount = getVisibleModuleCount();
        for (int i = scrollOffset; i < modules.size() && i < scrollOffset + visibleCount; i++) {
            int rowY = listTop + (i - scrollOffset) * (rowHeight + rowGap);
            buttonList.add(new AlphaButton(MODULE_BASE + i, listX + 4, rowY,
                    listWidth - 8, rowHeight, modules.get(i).getName()));
        }

        buttonList.add(new AlphaButton(BACK, panelLeft + 16, panelTop + panelHeight - 31,
                66, 18, "BACK"));
        buttonList.add(new AlphaButton(COMMUNITY_LOGIN, panelRight - 130,
                panelTop + panelHeight - 31, 114, 18, "COMMUNITY LOGIN"));

        AlphaModule selected = getSelectedModule();
        if (selected != null && selected.isImplemented() && selected.isUnlocked()) {
            buttonList.add(new AlphaButton(TOGGLE_MODULE, detailX, listBottom - 24,
                    Math.min(146, detailRight - detailX), 19,
                    selected.isEnabled() ? "DISABLE MODULE" : "ENABLE MODULE",
                    !selected.isEnabled()));
        }
    }

    private int getVisibleModuleCount() {
        int available = Math.max(31, listBottom - listTop);
        return Math.max(1, available / 36);
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
        drawRect(0, 0, width, height, 0x28000000);

        int panelRight = panelLeft + panelWidth;
        int panelBottom = panelTop + panelHeight;

        // One compact floating surface; the screen itself is no longer filled with large panels.
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xB8101218);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawRect(panelLeft, panelTop + 1, panelLeft + 1, panelBottom - 1, 0x553A3E48);
        drawRect(panelRight - 1, panelTop + 1, panelRight, panelBottom - 1, 0x553A3E48);
        drawRect(panelLeft + 1, panelBottom - 1, panelRight - 1, panelBottom, 0x553A3E48);

        drawString(fontRendererObj, "ALPHA", panelLeft + 16, panelTop + 15, ACCENT);
        drawString(fontRendererObj, "MODS", panelLeft + 16, panelTop + 29, 0xFFF4F5F8);
        drawString(fontRendererObj, "GENERAL COLLECTION", panelLeft + 72,
                panelTop + 29, 0xFFB8BDC8);
        drawString(fontRendererObj, "Select a module to view its status and features.",
                panelLeft + 16, panelTop + 47, 0xFF858B98);

        drawRect(panelLeft + 14, panelTop + 92, panelRight - 14,
                panelTop + 93, 0x33292D36);

        // Compact module list on the left.
        drawString(fontRendererObj, "MODULE LIST", listX + 2, listTop - 15, 0xFF777D89);
        drawRect(listX, listTop - 2, listX + listWidth, listBottom, 0x66141820);

        List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
        int rowHeight = 31;
        int rowGap = 5;
        for (int i = scrollOffset; i < modules.size() && i < scrollOffset + getVisibleModuleCount(); i++) {
            AlphaModule module = modules.get(i);
            int rowY = listTop + (i - scrollOffset) * (rowHeight + rowGap);
            if (module.getId().equals(selectedModuleId)) {
                drawRect(listX + 1, rowY, listX + listWidth - 1, rowY + rowHeight, 0xCC292D36);
                drawRect(listX + 1, rowY, listX + 3, rowY + rowHeight, ACCENT);
            }
            int statusColor = module.isEnabled() ? 0xFF8DDBA4 : 0xFF858B98;
            drawRect(listX + listWidth - 10, rowY + 12, listX + listWidth - 5,
                    rowY + 17, statusColor);
        }

        // Module details use text and dividers instead of a second oversized card.
        drawString(fontRendererObj, "DETAILS", detailX, listTop - 15, 0xFF777D89);
        drawRect(detailX, listTop - 2, detailRight, listBottom, 0x44141820);

        AlphaModule selected = getSelectedModule();
        if (selected != null) {
            drawString(fontRendererObj, selected.getName(), detailX + 12, listTop + 10, 0xFFF4F5F8);
            drawString(fontRendererObj, selected.getDescription(), detailX + 12, listTop + 27,
                    0xFFB8BDC8);
            drawRect(detailX + 12, listTop + 47, detailRight - 12,
                    listTop + 48, 0x553A3E48);

            drawString(fontRendererObj, "STATUS", detailX + 12, listTop + 60, 0xFF777D89);
            drawString(fontRendererObj, selected.getStatusLabel(), detailX + 66, listTop + 60,
                    selected.isEnabled() ? 0xFF8DDBA4 : ACCENT);
            drawString(fontRendererObj, "ACCESS", detailX + 12, listTop + 79, 0xFF777D89);
            drawString(fontRendererObj, selected.getCategory().getRequiredRank().name(),
                    detailX + 66, listTop + 79,
                    selected.isUnlocked() ? 0xFF8DDBA4 : 0xFFFF7777);

            drawString(fontRendererObj, "FEATURES", detailX + 12, listTop + 104, ACCENT);
            List<AlphaModuleFeature> features = selected.getFeatures();
            int featureY = listTop + 122;
            int maxFeatures = Math.max(0, (listBottom - featureY - 34) / 24);
            if (features.isEmpty()) {
                String message = selected.isImplemented()
                        ? "No extra feature tiers registered."
                        : "Planned feature - not implemented yet.";
                drawString(fontRendererObj, message, detailX + 12, featureY, 0xFF858B98);
            } else {
                for (int i = 0; i < features.size() && i < maxFeatures; i++) {
                    AlphaModuleFeature feature = features.get(i);
                    int color = feature.isAvailable() ? 0xFF8DDBA4
                            : PermissionService.hasAccess(feature.getRequiredRank())
                            ? 0xFFB8BDC8 : 0xFF777D89;
                    drawString(fontRendererObj, feature.getName(), detailX + 12, featureY, color);
                    drawString(fontRendererObj,
                            feature.getRequiredRank().name() + " / " + feature.getStatusLabel(),
                            detailX + 12, featureY + 10, 0xFF858B98);
                    featureY += 24;
                }
            }
        } else {
            drawString(fontRendererObj, "No modules in this category yet.",
                    detailX + 12, listTop + 12, 0xFF858B98);
        }

        drawString(fontRendererObj, "ACCESS  /  " + PermissionService.getStatusText(),
                panelLeft + 96, panelTop + panelHeight - 25,
                PermissionService.getMode() == PermissionService.Mode.OFFLINE_TEST
                        ? ACCENT : 0xFF858B98);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == COMMUNITY_LOGIN) {
            mc.displayGuiScreen(new CommunityLoginGui(this));
            return;
        }
        if (button.id == STAFF_MENU) {
            if (PermissionService.hasModAccess("staff.bhop")) mc.displayGuiScreen(new StaffRankMenuGui(this));
            return;
        }
        if (button.id == CATEGORY_BASE) {
            selectedCategory = AlphaModule.Category.PUBLIC;
            scrollOffset = 0;
            List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
            selectedModuleId = modules.isEmpty() ? "" : modules.get(0).getId();
            initGui();
            return;
        }
        if (button.id == CATEGORY_BASE + 1 && PermissionService.hasAccess(PermissionService.Rank.BETA)) {
            selectedCategory = AlphaModule.Category.BETA;
            scrollOffset = 0;
            selectedModuleId = "motion_blur";
            initGui();
            return;
        }
        if (button.id == CATEGORY_BASE + 2 && PermissionService.hasAccess(PermissionService.Rank.DONATOR)) {
            selectedCategory = AlphaModule.Category.DONATOR;
            scrollOffset = 0;
            selectedModuleId = "cosmetics";
            initGui();
            return;
        }
        if (button.id == TOGGLE_MODULE) {
            AlphaModule selected = getSelectedModule();
            if (selected != null && "staffmods".equals(selected.getId())) {
                if (PermissionService.hasModAccess("staff.bhop")) mc.displayGuiScreen(new StaffRankMenuGui(this));
                return;
            }
            if (selected != null && selected.isImplemented() && selected.isUnlocked()) {
                selected.setEnabled(!selected.isEnabled());
            }
            initGui();
            return;
        }
        if (button.id >= MODULE_BASE) {
            List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
            int index = button.id - MODULE_BASE;
            if (index >= 0 && index < modules.size()) {
                selectedModuleId = modules.get(index).getId();
                initGui();
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
            int maxOffset = Math.max(0, modules.size() - getVisibleModuleCount());
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
