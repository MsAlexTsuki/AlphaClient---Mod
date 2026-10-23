package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.developer.DeveloperDiagnostics;
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
import java.io.File;

/**
 * Restricted Alpha Developer Studio entry point for Developer, Owner and Founder.
 * This first workspace provides a unified tool catalogue and live client diagnostics.
 */
public final class AlphaDeveloperStudioGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOOL_BASE = 100;
    private static final int TOOL_COUNT = 17;
    private static final int ACCENT = 0xFFFFD866;
    private static final int RUN_TESTS = 20;
    private static final int TOGGLE_TEST = 21;
    private static final int TOGGLE_SANDBOX = 22;
    private static final int EXPORT_BUNDLE = 23;
    private static final int TOGGLE_OVERLAY = 24;

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
    private String paletteQuery = "";
    private int reportScroll;

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
        listBottom = panelTop + panelHeight - 82;

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
        buttonList.add(new AlphaButton(TOGGLE_OVERLAY, panelLeft + panelWidth - 122, panelTop + 8, 108, 20,
                DeveloperDiagnostics.isDebugOverlayEnabled() ? "OVERLAY: ON" : "OVERLAY: OFF"));
        buttonList.add(new AlphaButton(RUN_TESTS, panelLeft + 14, panelTop + panelHeight - 58, 84, 20, "RUN TESTS"));
        buttonList.add(new AlphaButton(TOGGLE_TEST, panelLeft + 104, panelTop + panelHeight - 58, 84, 20,
                DeveloperDiagnostics.isTestMode() ? "TEST: ON" : "TEST: OFF"));
        buttonList.add(new AlphaButton(TOGGLE_SANDBOX, panelLeft + 194, panelTop + panelHeight - 58, 100, 20,
                DeveloperDiagnostics.isSandboxMode() ? "SANDBOX: ON" : "SANDBOX: OFF"));
        buttonList.add(new AlphaButton(EXPORT_BUNDLE, panelLeft + 300, panelTop + panelHeight - 58, 112, 20, "EXPORT REPORT"));
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
        if (button.id == TOGGLE_OVERLAY) {
            DeveloperDiagnostics.setDebugOverlayEnabled(!DeveloperDiagnostics.isDebugOverlayEnabled());
            status = "Developer Debug Overlay " + (DeveloperDiagnostics.isDebugOverlayEnabled() ? "enabled." : "disabled.");
            initGui();
            return;
        }
        if (button.id == RUN_TESTS) {
            status = DeveloperDiagnostics.runTests().replace("\\n", " ");
            reportScroll = 0;
            return;
        }
        if (button.id == TOGGLE_TEST) {
            DeveloperDiagnostics.setTestMode(!DeveloperDiagnostics.isTestMode());
            status = "Developer Test Mode " + (DeveloperDiagnostics.isTestMode() ? "enabled." : "disabled.");
            initGui();
            return;
        }
        if (button.id == TOGGLE_SANDBOX) {
            DeveloperDiagnostics.setSandboxMode(!DeveloperDiagnostics.isSandboxMode());
            status = "Sandbox flag " + (DeveloperDiagnostics.isSandboxMode() ? "enabled." : "disabled.") + " (isolated diagnostics only).";
            initGui();
            return;
        }
        if (button.id == EXPORT_BUNDLE) {
            try {
                File file = DeveloperDiagnostics.exportDiagnosticBundle();
                status = "Diagnostic bundle saved: " + file.getName();
            } catch (Exception error) {
                DeveloperDiagnostics.captureException("Diagnostic Bundle Export", error);
                status = "Export failed: " + error.getMessage();
            }
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
        drawRect(dividerX, panelTop + 48, dividerX + 1, bottom - 80, 0x554B515D);
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

        String report = getSelectedReport();
        if (selectedTool == 1) {
            drawString(fontRendererObj, "Search: " + (paletteQuery.isEmpty() ? "(type to filter actions)" : paletteQuery), detailX, y, 0xFFB8BDC8);
            y += 14;
        }
        drawString(fontRendererObj, "REPORT / LIVE OUTPUT", detailX, y, 0xFFB8BDC8);
        y += 14;
        List<String> lines = wrapReport(report, detailWidth);
        int maxLines = Math.max(1, (bottom - 50 - y) / 10);
        int start = Math.min(reportScroll, Math.max(0, lines.size() - maxLines));
        int shown = 0;
        for (int i = start; i < lines.size() && shown < maxLines; i++, shown++) {
            drawString(fontRendererObj, clip(lines.get(i), detailWidth), detailX, y, 0xFFE1E4EA);
            y += 10;
        }
        if (lines.size() > maxLines) {
            drawString(fontRendererObj, "Use Page Up / Page Down to scroll output (" + lines.size() + " lines)",
                    detailX, bottom - 46, 0xFFB8BDC8);
        }

        drawString(fontRendererObj, status, panelLeft + 14, bottom - 27, 0xFF9CE2AC);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }


    private String getSelectedReport() {
        switch (selectedTool) {
            case 0: return "Alpha Developer Console\\n" + join(DeveloperDiagnostics.logs()) + "\\nType /alphaclient to access client commands. Use RUN TESTS and EXPORT REPORT below.";
            case 1:
                StringBuilder palette = new StringBuilder("Available actions (type to filter):\\n");
                for (int i = 0; i < TOOLS.length; i++) {
                    if (TOOLS[i].toLowerCase().contains(paletteQuery.toLowerCase())) palette.append("- ").append(TOOLS[i]).append("\\n");
                }
                palette.append("\\nActions: run tests, toggle test/sandbox mode, export diagnostic bundle.");
                return palette.toString();
            case 2: return "Recent client events\\n" + join(DeveloperDiagnostics.events());
            case 3: return "Debug overlay: " + DeveloperDiagnostics.isDebugOverlayEnabled() + "\\nUse OVERLAY ON/OFF at the top right to toggle the live in-game overlay.\\n" + DeveloperDiagnostics.profilerReport();
            case 4: return "TEST MODE: " + DeveloperDiagnostics.isTestMode() + "\\n" + DeveloperDiagnostics.lastTestReport();
            case 5: return "SANDBOX MODE: " + DeveloperDiagnostics.isSandboxMode() + "\\nSandbox is an isolated developer flag; it does not alter live module/config state.";
            case 6: return DeveloperDiagnostics.profilerReport();
            case 7: return DeveloperDiagnostics.modReport();
            case 8: return DeveloperDiagnostics.moduleReport();
            case 9: return DeveloperDiagnostics.dependencyReport();
            case 10: return DeveloperDiagnostics.configReport();
            case 11: return DeveloperDiagnostics.errorReport() + "\\n\\nERROR DATABASE (session memory)\\n" + join(DeveloperDiagnostics.errors());
            case 12: return "Captured exceptions\\n" + join(DeveloperDiagnostics.exceptions());
            case 13: return "Captured stack traces\\n" + join(DeveloperDiagnostics.stackTraces());
            case 14: return "Client logs\\n" + join(DeveloperDiagnostics.logs());
            case 15: return "Diagnostic bundle export uses the EXPORT REPORT button below.\\nExports locally to Minecraft/logs; no data is uploaded.\\n" + DeveloperDiagnostics.profilerReport();
            default: return "Alpha Developer Studio\\n17 registered developer tools.\\n" + DeveloperDiagnostics.profilerReport();
        }
    }

    private String join(List<String> values) {
        StringBuilder out = new StringBuilder();
        for (String value : values) out.append(value).append("\\n");
        return out.toString();
    }

    private List<String> wrapReport(String report, int maxWidth) {
        List<String> lines = new ArrayList<String>();
        if (report == null) return lines;
        for (String paragraph : report.split("\\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (fontRendererObj.getStringWidth(candidate) > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else line = new StringBuilder(candidate);
            }
            if (line.length() > 0) lines.add(line.toString());
        }
        return lines;
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
        if (selectedTool == 1 && keyCode != Keyboard.KEY_ESCAPE && keyCode != Keyboard.KEY_RETURN) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (!paletteQuery.isEmpty()) paletteQuery = paletteQuery.substring(0, paletteQuery.length() - 1);
            } else if (!Character.isISOControl(typedChar)) {
                paletteQuery += typedChar;
            }
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
        if (keyCode == Keyboard.KEY_PRIOR) {
            reportScroll = Math.max(0, reportScroll - 8);
            return;
        }
        if (keyCode == Keyboard.KEY_NEXT) {
            reportScroll += 8;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
