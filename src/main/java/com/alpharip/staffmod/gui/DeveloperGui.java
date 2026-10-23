package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import java.io.IOException;

/** Developer Mode gate and launcher for separate diagnostic screens. */
public final class DeveloperGui extends AlphaGuiScreen {
    private static final int TOGGLE = 1;
    private static final int BACK = 2;
    private static final int RANK = 10;
    private static final int VERIFICATION = 11;
    private static final int PERMISSIONS = 12;
    private static final int MODULES = 13;
    private static final int ACCENT = 0xFFFFD866;

    private final net.minecraft.client.gui.GuiScreen parent;
    private int panelLeft, panelTop, panelWidth, panelHeight;

    public DeveloperGui(net.minecraft.client.gui.GuiScreen parent) {
        this.parent = parent;
    }

    private boolean isFounder() {
        return PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                || Boolean.TRUE.equals(PermissionService.getCommandPermissionOverride("staffmods.founder"));
    }

    private boolean hasDeveloperAccess() {
        boolean developerRank = PermissionService.hasAccess(PermissionService.Rank.DEVELOPER)
                || Boolean.TRUE.equals(PermissionService.getCommandPermissionOverride("staffmods.developer"));
        // The Developer rank itself grants access to Developer Mode.
        // Founder access is unconditional; don't let a stale/explicit command
        // permission deny the rank's own folder after the menu has allowed it.
        return isFounder() || developerRank;
    }

    private boolean canOpenTool(String permission) {
        // Developer Mode is the gate for its tools. Tool-specific command
        // overrides previously hid the buttons or immediately closed their pages,
        // even after the Developer folder had granted access.
        return hasDeveloperAccess();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!hasDeveloperAccess()) {
            DeveloperModeState.disable();
            mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(520, Math.max(340, width - 32));
        panelHeight = Math.min(330, Math.max(250, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;

        int left = panelLeft + 16;
        int top = panelTop + 112;
        int buttonWidth = (panelWidth - 40) / 2;
        int buttonHeight = 28;
        if (DeveloperModeState.isEnabled()) {
            if (canOpenTool("staffmods.developer.rank_inspector"))
                buttonList.add(new AlphaButton(RANK, left, top, buttonWidth, buttonHeight, "RANK INSPECTOR"));
            if (canOpenTool("staffmods.developer.verification"))
                buttonList.add(new AlphaButton(VERIFICATION, left + buttonWidth + 8, top, buttonWidth, buttonHeight, "VERIFICATION"));
            if (canOpenTool("staffmods.developer.permission_inspector"))
                buttonList.add(new AlphaButton(PERMISSIONS, left, top + 38, buttonWidth, buttonHeight, "PERMISSION INSPECTOR"));
            if (canOpenTool("staffmods.developer.module_inspector"))
                buttonList.add(new AlphaButton(MODULES, left + buttonWidth + 8, top + 38, buttonWidth, buttonHeight, "MODULE INSPECTOR"));
        }
        buttonList.add(new AlphaButton(TOGGLE, left, panelTop + panelHeight - 34,
                buttonWidth, 22, DeveloperModeState.isEnabled() ? "DISABLE DEVELOPER MODE" : "ENABLE DEVELOPER MODE"));
        buttonList.add(new AlphaButton(BACK, left + buttonWidth + 8, panelTop + panelHeight - 34,
                buttonWidth, 22, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!hasDeveloperAccess()) {
            DeveloperModeState.disable();
            mc.displayGuiScreen(parent);
            return;
        }
        if (button == null) return;
        if (button.id == BACK) {
            DeveloperModeState.disable();
            mc.displayGuiScreen(parent);
        } else if (button.id == TOGGLE) {
            // hasDeveloperAccess() already validated the Developer rank/folder.
            // Do not require a second, separately assigned mode permission to toggle it.
            DeveloperModeState.setEnabled(!DeveloperModeState.isEnabled());
            initGui();
        } else {
            if (!DeveloperModeState.isEnabled()) return;
            switch (button.id) {
                case RANK:
                    mc.displayGuiScreen(new DeveloperToolGui(this, DeveloperToolGui.Page.RANK));
                    break;
                case VERIFICATION:
                    mc.displayGuiScreen(new DeveloperToolGui(this, DeveloperToolGui.Page.VERIFICATION));
                    break;
                case PERMISSIONS:
                    mc.displayGuiScreen(new DeveloperToolGui(this, DeveloperToolGui.Page.PERMISSIONS));
                    break;
                case MODULES:
                    mc.displayGuiScreen(new DeveloperToolGui(this, DeveloperToolGui.Page.MODULES));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!hasDeveloperAccess()) {
            DeveloperModeState.disable();
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
        drawString(fontRendererObj, "DEVELOPER MODE", panelLeft + 16, panelTop + 62, 0xFFFFFFFF);
        boolean enabled = DeveloperModeState.isEnabled();
        drawString(fontRendererObj, "Status: " + (enabled ? "ENABLED" : "DISABLED"),
                panelLeft + 16, panelTop + 82, enabled ? 0xFF7DDBA3 : 0xFFFFC56B);
        drawString(fontRendererObj, enabled
                        ? "Select a separate tool below."
                        : "Enable Developer Mode to unlock the developer folders.",
                panelLeft + 16, panelTop + 96, 0xFFB8BDC8);
        if (!enabled) {
            drawString(fontRendererObj, "Rank Inspector", panelLeft + 24, panelTop + 130, 0xFFB8BDC8);
            drawString(fontRendererObj, "Verification", panelLeft + 24, panelTop + 151, 0xFFB8BDC8);
            drawString(fontRendererObj, "Permission Inspector", panelLeft + 24, panelTop + 172, 0xFFB8BDC8);
            drawString(fontRendererObj, "Module Inspector", panelLeft + 24, panelTop + 193, 0xFFB8BDC8);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            DeveloperModeState.disable();
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
