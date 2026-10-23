package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Individual Developer Mode tool screens. */
public final class DeveloperToolGui extends AlphaGuiScreen {
    public enum Page { RANK, VERIFICATION, PERMISSIONS, MODULES }

    private static final int BACK = 1;
    private static final int ACCENT = 0xFFFFD866;
    private final DeveloperGui parent;
    private final Page page;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int scrollOffset;
    private final List<String> lines = new ArrayList<String>();

    public DeveloperToolGui(DeveloperGui parent, Page page) {
        this.parent = parent;
        this.page = page;
    }

    private boolean hasAccess() {
        if (!PermissionService.hasAccess(PermissionService.Rank.DEVELOPER)
                || !PermissionService.hasCommandPermission("staffmods.developer", true)
                || !PermissionService.hasCommandPermission("staffmods.developer.mode", true)
                || !DeveloperModeState.isEnabled()) return false;
        String key;
        switch (page) {
            case RANK: key = "staffmods.developer.rank_inspector"; break;
            case VERIFICATION: key = "staffmods.developer.verification"; break;
            case PERMISSIONS: key = "staffmods.developer.permission_inspector"; break;
            case MODULES: key = "staffmods.developer.module_inspector"; break;
            default: return false;
        }
        return PermissionService.hasCommandPermission(key, true);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!hasAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        panelWidth = Math.min(540, Math.max(340, width - 32));
        panelHeight = Math.min(340, Math.max(240, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 92,
                panelTop + panelHeight - 31, 76, 20, "BACK"));
        rebuildLines();
    }

    private void rebuildLines() {
        lines.clear();
        switch (page) {
            case RANK:
                lines.add("Effective rank: " + PermissionService.getEffectiveRank().name());
                lines.add("Rank verification: " + status(PermissionService.isOnlineRankVerified()));
                lines.add("Verified ranks:");
                if (PermissionService.getVerifiedOnlineRanks().isEmpty()) {
                    lines.add("  (none)");
                } else {
                    for (PermissionService.Rank rank : PermissionService.getVerifiedOnlineRanks()) {
                        lines.add("  " + rank.name());
                    }
                }
                break;
            case VERIFICATION:
                lines.add("Community rank verification: " + status(PermissionService.isOnlineRankVerified()));
                lines.add("Module permission verification: " + status(PermissionService.areModEntitlementsVerified()));
                lines.add("Command permission verification: " + status(PermissionService.areCommandPermissionsVerified()));
                lines.add("Session mode: " + PermissionService.getMode().name());
                break;
            case PERMISSIONS:
                lines.add("Permission checks (current account):");
                for (PermissionService.Rank rank : PermissionService.Rank.values()) {
                    if (rank == PermissionService.Rank.PUBLIC
                            || rank == PermissionService.Rank.SCOUT
                            || rank == PermissionService.Rank.ROGUE
                            || rank == PermissionService.Rank.ELITE
                            || rank == PermissionService.Rank.ALPHA
                            || rank == PermissionService.Rank.ALPHA_PLUS
                            || rank == PermissionService.Rank.BETA
                            || rank == PermissionService.Rank.DONATOR
                            || rank == PermissionService.Rank.THE_NIBBLER
                            || rank == PermissionService.Rank.MEDIA
                            || rank == PermissionService.Rank.FAMOUS
                            || rank == PermissionService.Rank.PARTNER) continue;
                    lines.add(rank.name() + ": " + (PermissionService.hasAccess(rank) ? "GRANTED" : "DENIED"));
                }
                lines.add("Command permission API: " + status(PermissionService.areCommandPermissionsVerified()));
                lines.add("Module permission API: " + status(PermissionService.areModEntitlementsVerified()));
                break;
            case MODULES:
                int total = 0;
                int unlocked = 0;
                for (AlphaModule module : AlphaModuleRegistry.getAll()) {
                    total++;
                    if (module.isUnlocked()) unlocked++;
                }
                lines.add("Registered modules: " + total);
                lines.add("Unlocked modules: " + unlocked);
                lines.add("--------------------------------");
                for (AlphaModule module : AlphaModuleRegistry.getAll()) {
                    lines.add(module.getName() + "  |  " + module.getCategory().name()
                            + "  |  " + (module.isUnlocked() ? "UNLOCKED" : "LOCKED"));
                }
                break;
            default:
                break;
        }
    }

    private String status(boolean value) {
        return value ? "VERIFIED" : "NOT VERIFIED";
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!hasAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button != null && button.id == BACK) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!hasAccess()) {
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
        drawString(fontRendererObj, "DEVELOPER  /  " + pageTitle(), panelLeft + 16, panelTop + 34, ACCENT);

        int y = panelTop + 62;
        int maxY = panelTop + panelHeight - 44;
        for (int i = scrollOffset; i < lines.size() && y < maxY; i++) {
            String line = lines.get(i);
            int color = line.endsWith("GRANTED") || line.endsWith("VERIFIED")
                    || line.endsWith("UNLOCKED") ? 0xFF7DDBA3
                    : (line.endsWith("DENIED") || line.endsWith("LOCKED") || line.endsWith("NOT VERIFIED")
                        ? 0xFFFFC56B : 0xFFE7E9EE);
            drawString(fontRendererObj, line, panelLeft + 16, y, color);
            y += 15;
        }
        if (lines.size() > 12) {
            drawString(fontRendererObj, "Scroll with mouse wheel for more entries.",
                    panelLeft + 16, panelTop + panelHeight - 42, 0xFFB8BDC8);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String pageTitle() {
        switch (page) {
            case RANK: return "RANK INSPECTOR";
            case VERIFICATION: return "VERIFICATION";
            case PERMISSIONS: return "PERMISSION INSPECTOR";
            case MODULES: return "MODULE INSPECTOR";
            default: return "TOOLS";
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0 && lines.size() > 12) {
            int max = Math.max(0, lines.size() - 12);
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel < 0 ? 1 : -1)));
        }
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
