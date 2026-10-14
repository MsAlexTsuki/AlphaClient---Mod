package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.movement.BhopMode;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** New category, module-list, and module-details interface for AlphaClient. */
public final class AlphaModMenu extends GuiScreen {
    private static final int CATEGORY_BASE = 20;
    private static final int MODULE_BASE = 100;
    private static final int BACK = 300;
    private static final int COMMUNITY_LOGIN = 301;
    private static final int TOGGLE_MODULE = 302;

    private final GuiScreen parent;
    private AlphaModule.Category selectedCategory = AlphaModule.Category.PUBLIC;
    private String selectedModuleId = "alpha_hud";
    private int scrollOffset;

    public AlphaModMenu(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int sidebarWidth = 154;
        int footerY = height - 32;

        AlphaModule.Category[] categories = AlphaModule.Category.values();
        for (int i = 0; i < categories.length; i++) {
            buttonList.add(new AlphaButton(CATEGORY_BASE + i, 12, 76 + i * 30,
                    sidebarWidth - 24, 24, categories[i].getTitle()));
        }

        int listX = sidebarWidth + 12;
        int listWidth = Math.max(160, Math.min(240, width / 3 - 20));
        List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
        int maxVisible = getVisibleModuleCount();
        for (int i = scrollOffset; i < modules.size() && i < scrollOffset + maxVisible; i++) {
            AlphaModule module = modules.get(i);
            String label = module.getName() + "  [" + module.getStatusLabel() + "]";
            buttonList.add(new AlphaButton(MODULE_BASE + i, listX, 76 + (i - scrollOffset) * 32,
                    listWidth, 26, label));
        }

        buttonList.add(new AlphaButton(BACK, 12, footerY, 88, 20, "BACK"));
        buttonList.add(new AlphaButton(COMMUNITY_LOGIN, width - 174, footerY, 160, 20, "COMMUNITY LOGIN"));

        AlphaModule selected = getSelectedModule();
        if (selected != null && selected.isImplemented() && selected.isUnlocked()) {
            buttonList.add(new AlphaButton(TOGGLE_MODULE, width - 174, height - 68, 160, 24,
                    selected.isEnabled() ? "DISABLE MODULE" : "ENABLE MODULE"));
        }
    }

    private int getVisibleModuleCount() {
        int listBottom = height - 48;
        return Math.max(1, (listBottom - 82) / 32);
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
        drawGradientRect(0, 0, width, height, 0xFF15171C, 0xFF090A0D);

        // Sidebar and navigation.
        drawRect(0, 0, 154, height, 0xFF101116);
        drawRect(153, 0, 154, height, 0xFF363941);
        drawRect(14, 20, 17, 51, 0xFFFFD866);
        drawString(fontRendererObj, "ALPHA", 26, 22, 0xFFFFD866);
        drawString(fontRendererObj, "MODULES", 26, 35, 0xFFFFFFFF);
        drawString(fontRendererObj, "CATEGORIES", 14, 61, 0xFF858A96);

        drawString(fontRendererObj, "MODULE LIBRARY", 170, 22, 0xFFFFD866);
        drawString(fontRendererObj, selectedCategory.getTitle(), 170, 40, 0xFFFFFFFF);
        drawString(fontRendererObj, "Select a module to inspect its status and features.",
                170, 54, 0xFF858A96);

        int listX = 166;
        int listWidth = Math.max(160, Math.min(240, width / 3 - 20));
        drawRect(listX, 70, listX + listWidth + 8, height - 42, 0x55202229);

        List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
        int maxVisible = getVisibleModuleCount();
        for (int i = scrollOffset; i < modules.size() && i < scrollOffset + maxVisible; i++) {
            AlphaModule module = modules.get(i);
            int rowY = 76 + (i - scrollOffset) * 32;
            if (module.getId().equals(selectedModuleId)) {
                drawRect(listX + 4, rowY - 2, listX + listWidth + 4, rowY + 27, 0x663B3527);
                drawRect(listX + 4, rowY - 2, listX + 6, rowY + 27, 0xFFFFD866);
            }
            drawString(fontRendererObj, module.getDescription(), listX + 10, rowY + 27, 0xFF858A96);
        }

        int detailX = listX + listWidth + 24;
        int detailRight = width - 14;
        drawRect(detailX, 70, detailRight, height - 42, 0x55202229);
        drawRect(detailX, 70, detailRight, 71, 0xFF454954);

        AlphaModule selected = getSelectedModule();
        if (selected != null) {
            drawString(fontRendererObj, "SELECTED MODULE", detailX + 12, 83, 0xFF858A96);
            drawString(fontRendererObj, selected.getName(), detailX + 12, 101, 0xFFFFFFFF);
            drawString(fontRendererObj, selected.getDescription(), detailX + 12, 118, 0xFFB8BBC4);
            drawString(fontRendererObj, "STATUS", detailX + 12, 148, 0xFF858A96);
            drawString(fontRendererObj, selected.getStatusLabel(), detailX + 12, 162,
                    selected.isEnabled() ? 0xFF8DDBA4 : 0xFFFFD866);
            drawString(fontRendererObj, "CATEGORY ACCESS", detailX + 12, 185, 0xFF858A96);
            drawString(fontRendererObj, selected.getCategory().getRequiredRank().name(),
                    detailX + 12, 199, selected.isUnlocked() ? 0xFF8DDBA4 : 0xFFFF7777);

            drawString(fontRendererObj, "FEATURES", detailX + 12, 228, 0xFFFFD866);
            List<AlphaModuleFeature> features = selected.getFeatures();
            if (features.isEmpty()) {
                drawString(fontRendererObj, selected.isImplemented()
                        ? "No additional feature tiers registered."
                        : "This module is planned and not implemented yet.",
                        detailX + 12, 246, 0xFF858A96);
            } else {
                int featureY = 246;
                int maxFeatures = Math.max(1, (height - featureY - 92) / 30);
                for (int i = 0; i < features.size() && i < maxFeatures; i++) {
                    AlphaModuleFeature feature = features.get(i);
                    int color = feature.isAvailable() ? 0xFF8DDBA4
                            : PermissionService.hasAccess(feature.getRequiredRank()) ? 0xFFB8BBC4 : 0xFF777D89;
                    drawString(fontRendererObj, feature.getName(), detailX + 12, featureY, color);
                    drawString(fontRendererObj, feature.getRequiredRank().name() + " / " + feature.getStatusLabel(),
                            detailX + 12, featureY + 12, 0xFF858A96);
                    featureY += 30;
                }
            }
        } else {
            drawString(fontRendererObj, "No modules in this category yet.", detailX + 12, 92, 0xFF858A96);
        }

        drawString(fontRendererObj, "ACCESS: " + PermissionService.getStatusText(),
                170, height - 27, PermissionService.getMode() == PermissionService.Mode.OFFLINE_TEST
                        ? 0xFFFFD866 : 0xFF858A96);
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
        if (button.id >= CATEGORY_BASE && button.id < CATEGORY_BASE + AlphaModule.Category.values().length) {
            selectedCategory = AlphaModule.Category.values()[button.id - CATEGORY_BASE];
            scrollOffset = 0;
            List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
            selectedModuleId = modules.isEmpty() ? "" : modules.get(0).getId();
            initGui();
            return;
        }
        if (button.id == TOGGLE_MODULE) {
            AlphaModule selected = getSelectedModule();
            if (selected != null && selected.isImplemented() && selected.isUnlocked()) {
                selected.setEnabled(!selected.isEnabled());
                if ("staffmods".equals(selected.getId())) {
                    com.alpharip.alphaclient.AlphaClient.syncBhopModuleState();
                }
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
