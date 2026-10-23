package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Restricted Alpha Developer Studio entry point for Developer, Owner and Founder.
 * This first workspace provides a unified tool catalogue and live client diagnostics.
 */
public final class AlphaDeveloperStudioGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOOL_BASE = 100;
    private static final int TOOL_COUNT = 17;
    private static final int ACCENT = 0xFFFFD866;

    private static final String[] TOOLS = {
        "Developer Console",
        "Developer Command Palette",
        "Developer Event Viewer",
        "Developer Debug Overlay",
        "Developer Test Mode",
        "Developer Sandbox Mode",
        "Developer Profiler",
        "Developer Mod Inspector",
        "Developer Module Inspector",
        "Developer Dependency Graph",
        "Developer Config Inspector",
        "Developer Error Database",
        "Developer Exception Viewer",
        "Developer Stack Trace Viewer",
        "Developer Log Viewer",
        "Developer Diagnostic Bundle",
        "Alpha Developer Studio"
    };

    private static final String[] DESCRIPTIONS = {
        "Central workspace for developer commands and diagnostic output.",
        "Searchable entry point for available developer actions.",
        "Inspect client events and their order when event instrumentation is available.",
        "Show live client diagnostics without covering the entire game.",
        "Run controlled checks against supported client systems.",
        "Keep experiments isolated from normal client settings and profiles.",
        "Inspect frame timing and client performance measurements.",
        "Inspect registered mods and available metadata.",
        "Inspect Alpha modules, categories, unlock state and descriptions.",
        "Explore declared relationships between modules and their dependencies.",
        "Inspect supported client configuration values.",
        "Review collected and classified client errors.",
        "Inspect captured exceptions and their messages.",
        "Inspect call stacks captured when an error occurs.",
        "Search and filter available client log output.",
        "Collect diagnostic information into an exportable report.",
        "Unified entry point for the Alpha Client developer workspace."
    };

    private final GuiScreen parent;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int selectedTool;
    private int listTop, listBottom;
    private int scrollOffset;
    private String status = "Developer workspace ready.";

    public AlphaDeveloperStudioGui(GuiScreen parent) {
        this.parent = parent;
    }

    private boolean hasDeveloperSuiteAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.DEVELOPER)
                || PermissionService.hasAccess(PermissionService.Rank.OWNER)
                || PermissionService.hasAccess(PermissionService.Rank.FOUNDER);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!hasDeveloperSuiteAccess()) {
            DeveloperModeState.disable();
            mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(700, Math.max(420, width - 20));
        panelHeight = Math.min(410, Math.max(260, height - 20));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        listTop = panelTop + 62;
        listBottom = panelTop + panelHeight - 42;

        int listX = panelLeft + 14;
        int listWidth = Math.max(150, Math.min(225, panelWidth / 3));
        int rowHeight = 23;
        int visibleRows = Math.max(1, (listBottom - listTop) / rowHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, TOOLS.length - visibleRows)));
        for (int i = scrollOffset; i < TOOLS.length && i < scrollOffset + visibleRows; i++) {
            int row = i - scrollOffset;
            buttonList.add(new AlphaButton(TOOL_BASE + i, listX, listTop + row * rowHeight,
                    listWidth, 20, TOOLS[i], i == selectedTool));
        }
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 94,
                panelTop + panelHeight - 31, 80, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!hasDeveloperSuiteAccess()) {
            DeveloperModeState.disable();
            mc.displayGuiScreen(parent);
            return;
        }
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id >= TOOL_BASE && button.id < TOOL_BASE + TOOL_COUNT) {
            selectedTool = button.id - TOOL_BASE;
            status = selectedTool == 8
                    ? "Module registry refreshed."
                    : "Selected " + TOOLS[selectedTool] + ".";
            initGui();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (!hasDeveloperSuiteAccess()) {
            DeveloperModeState.disable();
            mc.displayGuiScreen(parent);
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!hasDeveloperSuiteAccess()) {
            DeveloperModeState.disable();
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!hasDeveloperSuiteAccess()) {
            DeveloperModeState.disable();
            mc.displayGuiScreen(parent);
            return;
        }
        drawRect(0, 0, width, height, 0x35000000);
        int right = panelLeft + panelWidth;
        int bottom = panelTop + panelHeight;
        drawRect(panelLeft, panelTop, right, bottom, 0xF00B0D12);
        drawRect(panelLeft, panelTop, right, panelTop + 1, ACCENT);
        drawRect(panelLeft, panelTop + 1, panelLeft + 1, bottom - 1, 0x554B515D);
        drawRect(right - 1, panelTop + 1, right, bottom - 1, 0x554B515D);

        drawString(fontRendererObj, "ALPHA DEVELOPER STUDIO", panelLeft + 14, panelTop + 13, 0xFFF0F1F4);
        drawString(fontRendererObj, "RESTRICTED WORKSPACE  /  DEVELOPER · OWNER · FOUNDER",
                panelLeft + 14, panelTop + 30, ACCENT);
        drawString(fontRendererObj, "TOOLS", panelLeft + 14, panelTop + 49, 0xFFB8BDC8);

        int dividerX = panelLeft + 14 + Math.max(150, Math.min(225, panelWidth / 3)) + 12;
        drawRect(dividerX, panelTop + 48, dividerX + 1, bottom - 40, 0x554B515D);
        int detailX = dividerX + 12;
        int detailWidth = right - detailX - 14;
        int y = panelTop + 58;

        drawString(fontRendererObj, TOOLS[selectedTool], detailX, y, ACCENT);
        y += 20;
        y = drawWrapped(DESCRIPTIONS[selectedTool], detailX, y, detailWidth, 0xFFE1E4EA, 3) + 8;
        drawString(fontRendererObj, "LIVE CLIENT STATUS", detailX, y, 0xFFB8BDC8);
        y += 14;
        drawString(fontRendererObj, "Minecraft: " + Minecraft.getMinecraft().getVersion(), detailX, y, 0xFFF0F1F4);
        y += 12;
        drawString(fontRendererObj, "Effective rank: " + PermissionService.getEffectiveRank().name(), detailX, y, 0xFFF0F1F4);
        y += 12;
        drawString(fontRendererObj, "Rank verified: " + PermissionService.isOnlineRankVerified(), detailX, y, 0xFFF0F1F4);
        y += 12;
        drawString(fontRendererObj, "Mod permissions verified: " + PermissionService.areModEntitlementsVerified(), detailX, y, 0xFFF0F1F4);
        y += 18;

        if (selectedTool == 8 || selectedTool == 7 || selectedTool == 9) {
            drawString(fontRendererObj, "REGISTERED ALPHA MODULES", detailX, y, 0xFFB8BDC8);
            y += 14;
            List<AlphaModule> modules = AlphaModuleRegistry.getAll();
            int maxLines = Math.max(0, (bottom - 47 - y) / 12);
            for (int i = 0; i < modules.size() && i < maxLines; i++) {
                AlphaModule module = modules.get(i);
                String line = module.getName() + "  [" + module.getId() + "]  "
                        + module.getCategory().name() + "  " + (module.isUnlocked() ? "UNLOCKED" : "LOCKED");
                drawString(fontRendererObj, clip(line, detailWidth), detailX, y, module.isUnlocked() ? 0xFF9CE2AC : 0xFFB8BDC8);
                y += 12;
            }
            if (modules.size() > maxLines) {
                drawString(fontRendererObj, (modules.size() - maxLines) + " additional module(s) not shown.",
                        detailX, Math.min(bottom - 48, y), 0xFFB8BDC8);
            }
        } else {
            drawString(fontRendererObj, "Workspace status: catalogue entry selected", detailX, y, 0xFFB8BDC8);
            y += 14;
            drawWrapped("This entry is registered in the suite. Its dedicated diagnostic workflow is not yet wired into this first workspace build.",
                    detailX, y, detailWidth, 0xFFFFC56B, 3);
        }

        drawString(fontRendererObj, status, panelLeft + 14, bottom - 27, 0xFF9CE2AC);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int drawWrapped(String text, int x, int y, int maxWidth, int color, int maxLines) {
        List<String> lines = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fontRendererObj.getStringWidth(candidate) > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        int count = Math.min(maxLines, lines.size());
        for (int i = 0; i < count; i++) {
            String value = lines.get(i);
            if (i == maxLines - 1 && lines.size() > maxLines) {
                while (value.length() > 3 && fontRendererObj.getStringWidth(value + "...") > maxWidth) {
                    value = value.substring(0, value.length() - 1);
                }
                value += "...";
            }
            drawString(fontRendererObj, value, x, y + i * 10, color);
        }
        return y + count * 10;
    }

    private String clip(String value, int maxWidth) {
        if (value == null) return "";
        String result = value;
        while (result.length() > 3 && fontRendererObj.getStringWidth(result) > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result.equals(value) ? result : result + "...";
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (keyCode == Keyboard.KEY_UP) {
            scrollOffset = Math.max(0, scrollOffset - 1);
            initGui();
            return;
        }
        if (keyCode == Keyboard.KEY_DOWN) {
            int visibleRows = Math.max(1, (listBottom - listTop) / 23);
            scrollOffset = Math.min(Math.max(0, TOOLS.length - visibleRows), scrollOffset + 1);
            initGui();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
