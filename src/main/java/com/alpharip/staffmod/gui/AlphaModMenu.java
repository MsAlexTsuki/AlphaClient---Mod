package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** Category-based module browser for AlphaClient. */
public final class AlphaModMenu extends GuiScreen {
    private static final int BACK = 1;
    private static final int COMMUNITY_LOGIN = 2;
    private static final int CATEGORY_BASE = 20;
    private static final int MODULE_BASE = 100;
    private final GuiScreen parent;
    private AlphaModule.Category selectedCategory = AlphaModule.Category.PUBLIC;
    private int scrollOffset = 0;

    public AlphaModMenu(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = 14;
        int y = 58;
        int categoryWidth = Math.max(95, Math.min(130, (width - 40) / 4));
        AlphaModule.Category[] categories = AlphaModule.Category.values();
        for (int i = 0; i < categories.length; i++) {
            int x = left + i * (categoryWidth + 5);
            buttonList.add(new AlphaButton(CATEGORY_BASE + i, x, y, categoryWidth, 22,
                    categories[i].getTitle()));
        }
        buttonList.add(new GuiButton(BACK, 14, height - 32, 100, 20, "BACK"));
        addModuleButtons();
    }

    private void addModuleButtons() {
        for (int i = buttonList.size() - 1; i >= 0; i--) {
            if (buttonList.get(i).id >= MODULE_BASE) {
                buttonList.remove(i);
            }
        }
        List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
        int y = 95;
        int visibleIndex = 0;
        for (int i = scrollOffset; i < modules.size() && visibleIndex < 7; i++, visibleIndex++) {
            AlphaModule module = modules.get(i);
            String label = module.getName() + "  [" + module.getStatusLabel() + "]";
            buttonList.add(new GuiButton(MODULE_BASE + i, 18, y + visibleIndex * 32,
                    Math.max(220, Math.min(380, width - 36)), 25, label));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF17191F, 0xFF0B0C10);
        drawString(fontRendererObj, "ALPHA CLIENT  /  MOD MENU", 18, 18, 0xFFFFD866);
        drawString(fontRendererObj, "Access: " + PermissionService.getStatusText(),
                18, 36, PermissionService.getMode() == PermissionService.Mode.OFFLINE_TEST
                        ? 0xFFFFD866 : 0xFFB8BBC4);
        drawString(fontRendererObj, selectedCategory.getTitle(), 18, 87, 0xFFFFFFFF);
        List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
        int y = 105;
        for (int i = scrollOffset; i < modules.size() && i < scrollOffset + 7; i++) {
            AlphaModule module = modules.get(i);
            drawString(fontRendererObj, module.getDescription(), 30, y + (i - scrollOffset) * 32 + 15,
                    0xFF9DA2AE);
        }
        drawString(fontRendererObj,
                "Online ranks stay locked until verified by the Alpha community service.",
                18, height - 48, 0xFF777D89);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) {
            return;
        }
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
            initGui();
            return;
        }
        if (button.id >= MODULE_BASE) {
            List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
            int index = button.id - MODULE_BASE;
            if (index >= 0 && index < modules.size()) {
                AlphaModule module = modules.get(index);
                if (!module.isUnlocked() || !module.isImplemented()) {
                    return;
                }
                boolean next = !module.isEnabled();
                module.setEnabled(next);
                if ("bhop".equals(module.getId())) {
                    com.alpharip.staffmod.movement.BhopMode.setEnabled(next);
                }
            }
            initGui();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            List<AlphaModule> modules = AlphaModuleRegistry.getForCategory(selectedCategory);
            int maxOffset = Math.max(0, modules.size() - 7);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}