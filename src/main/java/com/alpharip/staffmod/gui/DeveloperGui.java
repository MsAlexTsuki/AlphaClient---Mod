package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/**
 * Restricted Developer workspace inside StaffMods.
 * Access is granted to Developer, Owner, and Founder ranks only.
 */
public final class DeveloperGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int REFRESH = 2;
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private int panelLeft, panelTop, panelWidth, panelHeight;

    public DeveloperGui(GuiScreen parent) {
        this.parent = parent;
    }

    private boolean hasDeveloperAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.DEVELOPER);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!hasDeveloperAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(520, Math.max(340, width - 32));
        panelHeight = Math.min(300, Math.max(230, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;

        buttonList.add(new AlphaButton(REFRESH, panelLeft + panelWidth - 176,
                panelTop + panelHeight - 31, 76, 20, "REFRESH"));
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 92,
                panelTop + panelHeight - 31, 76, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!hasDeveloperAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == REFRESH) {
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!hasDeveloperAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }

        drawRect(0, 0, width, height, 0x30000000);
        int right = panelLeft + panelWidth;
        int bottom = panelTop + panelHeight;
        drawRect(panelLeft, panelTop, right, bottom, 0xE00B0D12);
        drawRect(panelLeft, panelTop, right, panelTop + 1, ACCENT);
        drawRect(panelLeft, panelTop + 1, panelLeft + 1, bottom - 1, 0x554B515D);
        drawRect(right - 1, panelTop + 1, right, bottom - 1, 0x554B515D);

        drawString(fontRendererObj, "ALPHA CLIENT", panelLeft + 16, panelTop + 14, 0xFFF0F1F4);
        drawString(fontRendererObj, "STAFFMODS  /  DEVELOPER", panelLeft + 16, panelTop + 34, ACCENT);
        drawString(fontRendererObj, "DEVELOPER WORKSPACE", panelLeft + 16, panelTop + 61, 0xFFFFFFFF);
        drawString(fontRendererObj, "Active rank: " + PermissionService.getEffectiveRank().name(),
                panelLeft + 16, panelTop + 82, 0xFFB8BDC8);

        drawStatus("Community rank verification",
                PermissionService.isOnlineRankVerified(), panelLeft + 16, panelTop + 108);
        drawStatus("Module permissions",
                PermissionService.areModEntitlementsVerified(), panelLeft + 16, panelTop + 128);
        drawStatus("Command permissions",
                PermissionService.areCommandPermissionsVerified(), panelLeft + 16, panelTop + 148);

        int totalModules = AlphaModuleRegistry.getAll().size();
        int unlockedModules = 0;
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            if (module.isUnlocked()) unlockedModules++;
        }
        drawString(fontRendererObj, "Registered modules: " + totalModules,
                panelLeft + 16, panelTop + 180, 0xFFE7E9EE);
        drawString(fontRendererObj, "Currently unlocked: " + unlockedModules,
                panelLeft + 16, panelTop + 198, 0xFFE7E9EE);
        drawString(fontRendererObj, "Use /alphahelp open to inspect available commands and access.",
                panelLeft + 16, panelTop + 222, 0xFFB8BDC8);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawStatus(String label, boolean verified, int x, int y) {
        drawString(fontRendererObj, label + ": " + (verified ? "VERIFIED" : "NOT VERIFIED"),
                x, y, verified ? 0xFF7DDBA3 : 0xFFFFC56B);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
