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
            buttonList.add(new AlphaButton(CATEGORY_BASE + i, 12, 76 + i * 32,
                    sidebarWidth - 24, 26, categories[i].getTitle()));
        }

        int listX = sidebarWidth + 12;
        int listWidth = Math.max(160, Math.min(240, width / 3 - 20));
        List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
        int maxVisible = getVisibleModuleCount();
        for (int i = scrollOffset; i < modules.size() && i < scrollOffset + maxVisible; i++) {
            AlphaModule module = modules.get(i);
            buttonList.add(new AlphaButton(MODULE_BASE + i, listX + 5,
                    78 + (i - scrollOffset) * 46, listWidth - 10, 38, module.getName()));
        }

        buttonList.add(new AlphaButton(BACK, 12, footerY, 88, 20, "BACK"));
        buttonList.add(new AlphaButton(COMMUNITY_LOGIN, width - 174, footerY, 160, 20, "COMMUNITY LOGIN"));

        AlphaModule selected = getSelectedModule();
        if (selected != null && selected.isImplemented() && selected.isUnlocked()) {
            buttonList.add(new AlphaButton(TOGGLE_MODULE, width - 174, height - 68, 160, 24,
                    selected.isEnabled() ? "DISABLE MODULE" : "ENABLE MODULE",
                    !selected.isEnabled()));
        }
    }

    private int getVisibleModuleCount() {
        int listBottom = height - 48;
        return Math.max(1, (listBottom - 82) / 46);
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
        drawGradientRect(0, 0, width, height, 0xFF181B22, 0xFF090A0E);

        drawRect(0, 0, 154, height, 0xFF111319);
        drawRect(153, 0, 154, height, 0xFF292D36);
        drawRect(14, 20, 17, 54, 0xFFFFD866);
        drawString(fontRendererObj, "ALPHA", 26, 22, 0xFFFFD866);
        drawString(fontRendererObj, "CLIENT", 26, 36, 0xFFF4F5F8);
        drawString(fontRendererObj, "MODULES", 14, 62, 0xFF686E7A);

        int contentLeft = 174;
        int listX = 166;
        int listWidth = Math.max(160, Math.min(240, width / 3 - 20));
        int detailX = listX + listWidth + 20;
        int detailRight = width - 14;
        int panelTop = 70;
        int panelBottom = height - 42;

        drawString(fontRendererObj, "MODULE LIBRARY", contentLeft, 20, 0xFFFFD866);
        drawString(fontRendererObj, selectedCategory.getTitle(), contentLeft, 37, 0xFFF4F5F8);
        drawString(fontRendererObj, "Choose a module to view its status and features.",
                contentLeft, 52, 0xFF9298A5);

        drawRect(listX, panelTop, listX + listWidth + 8, panelBottom, 0xFF14171D);
        drawRect(listX, panelTop, listX + listWidth + 8, panelTop + 1, 0xFF343944);
        drawRect(detailX, panelTop, detailRight, panelBottom, 0xFF14171D);
        drawRect(detailX, panelTop, detailRight, panelTop + 1, 0xFF343944);

        List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
        int maxVisible = getVisibleModuleCount();
        for (int i = scrollOffset; i < modules.size() && i < scrollOffset + maxVisible; i++) {
            AlphaModule module = modules.get(i);
            int rowY = 78 + (i - scrollOffset) * 46;
            if (module.getId().equals(selectedModuleId)) {
                drawRect(listX + 3, rowY - 2, listX + listWidth + 5, rowY + 40, 0xFF22252D);
                drawRect(listX + 3, rowY - 2, listX + 5, rowY + 40, 0xFFFFD866);
            }
            int statusColor = module.isEnabled() ? 0xFF8DDBA4
                    : module.isImplemented() ? 0xFF9298A5 : 0xFF686E7A;
            drawString(fontRendererObj, module.getStatusLabel(),
                    listX + listWidth - 66, rowY + 4, statusColor);
            String description = module.getDescription();
            int maxChars = Math.max(12, (listWidth - 24) / 6);
            if (description.length() > maxChars) description = description.substring(0, maxChars - 3) + "...";
            drawString(fontRendererObj, description, listX + 12, rowY + 25, 0xFF858B98);
        }

        AlphaModule selected = getSelectedModule();
        if (selected != null) {
            drawString(fontRendererObj, "MODULE DETAILS", detailX + 14, 84, 0xFF686E7A);
            drawString(fontRendererObj, selected.getName(), detailX + 14, 103, 0xFFF4F5F8);
            drawString(fontRendererObj, selected.getDescription(), detailX + 14, 121, 0xFFB8BDC8);
            drawRect(detailX + 14, 145, detailRight - 14, 146, 0xFF292D36);

            drawString(fontRendererObj, "STATUS", detailX + 14, 160, 0xFF686E7A);
            drawString(fontRendererObj, selected.getStatusLabel(), detailX + 14, 174,
                    selected.isEnabled() ? 0xFF8DDBA4 : 0xFFFFD866);
            drawString(fontRendererObj, "ACCESS LEVEL", detailX + 14, 197, 0xFF686E7A);
            drawString(fontRendererObj, selected.getCategory().getRequiredRank().name(),
                    detailX + 14, 211, selected.isUnlocked() ? 0xFF8DDBA4 : 0xFFFF7777);

            drawString(fontRendererObj, "FEATURES", detailX + 14, 239, 0xFFFFD866);
            List<AlphaModuleFeature> features = selected.getFeatures();
            if (features.isEmpty()) {
                String featureText = selected.isImplemented()
                        ? "No additional feature tiers registered."
                        : "Planned feature — not implemented yet.";
                drawString(fontRendererObj, featureText, detailX + 14, 258, 0xFF858B98);
            } else {
                int featureY = 258;
                int maxFeatures = Math.max(1, (height - featureY - 92) / 30);
                for (int i = 0; i < features.size() && i < maxFeatures; i++) {
                    AlphaModuleFeature feature = features.get(i);
                    int color = feature.isAvailable() ? 0xFF8DDBA4
                            : PermissionService.hasAccess(feature.getRequiredRank()) ? 0xFFB8BDC8 : 0xFF777D89;
                    drawString(fontRendererObj, feature.getName(), detailX + 14, featureY, color);
                    drawString(fontRendererObj, feature.getRequiredRank().name() + " / " + feature.getStatusLabel(),
                            detailX + 14, featureY + 12, 0xFF858B98);
                    featureY += 30;
                }
            }
        } else {
            drawString(fontRendererObj, "No modules in this category yet.", detailX + 14, 92, 0xFF858B98);
        }

        drawString(fontRendererObj, "ACCESS  /  " + PermissionService.getStatusText(),
                170, height - 27, PermissionService.getMode() == PermissionService.Mode.OFFLINE_TEST
                        ? 0xFFFFD866 : 0xFF858B98);
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
