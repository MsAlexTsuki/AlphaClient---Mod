package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/**
 * Per-module fallback settings screen. Modules with specialized controls keep
 * their dedicated screens; this screen prevents other module tiles from only
 * selecting a tile without opening a settings page.
 */
public final class ModuleSettingsGui extends AlphaGuiScreen {
    private static final int TOGGLE_MODULE = 1;
    private static final int BACK = 2;

    private final GuiScreen parent;
    private final String moduleId;

    public ModuleSettingsGui(GuiScreen parent, AlphaModule module) {
        this.parent = parent;
        this.moduleId = module == null ? "" : module.getId();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        AlphaModule module = getModule();
        if (module == null) {
            buttonList.add(new AlphaButton(BACK, width / 2 - 60, height - 30, 120, 20, "BACK"));
            return;
        }

        int centerX = width / 2;
        boolean canToggle = module.isImplemented() && module.isUnlocked();
        buttonList.add(new AlphaButton(TOGGLE_MODULE, centerX - 82, height - 58, 164, 20,
                canToggle ? (module.isEnabled() ? "DISABLE MODULE" : "ENABLE MODULE")
                        : (module.isUnlocked() ? "COMING SOON" : "LOCKED"), false));
        buttonList.get(0).enabled = canToggle;
        buttonList.add(new AlphaButton(BACK, centerX - 60, height - 31, 120, 20, "BACK"));
    }

    private AlphaModule getModule() {
        return com.alpharip.staffmod.modules.AlphaModuleRegistry.find(moduleId);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == TOGGLE_MODULE) {
            AlphaModule module = getModule();
            if (module != null && module.isImplemented() && module.isUnlocked()) {
                module.setEnabled(!module.isEnabled());
            }
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x88000000);
        int panelWidth = Math.min(360, width - 20);
        int left = (width - panelWidth) / 2;
        int right = left + panelWidth;
        int top = 22;
        int bottom = height - 20;

        drawRect(left, top, right, bottom, 0xDD0C0C0C);
        drawRect(left, top, right, top + 2, 0xFFFFD866);

        AlphaModule module = getModule();
        if (module == null) {
            drawCenteredString(fontRendererObj, "MODULE NOT FOUND", width / 2, top + 18, 0xFFFFFFFF);
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        drawCenteredString(fontRendererObj, module.getName().toUpperCase(), width / 2, top + 14, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, module.getStatusLabel(), width / 2, top + 31,
                module.isEnabled() ? 0xFF9CE2AC : 0xFFB8BDC8);

        drawWrapped(module.getDescription(), left + 14, top + 52, panelWidth - 28, 0xFFB8BDC8, 3);

        int featureY = top + 100;
        drawString(fontRendererObj, "MODULE FEATURES", left + 14, featureY, 0xFFFFD866);
        featureY += 15;
        List<AlphaModuleFeature> features = module.getFeatures();
        if (features.isEmpty()) {
            drawString(fontRendererObj, "No additional feature settings are registered.",
                    left + 14, featureY, 0xFFB8BDC8);
        } else {
            int maxRows = Math.max(0, (height - featureY - 100) / 25);
            int rows = Math.min(features.size(), maxRows);
            for (int i = 0; i < rows; i++) {
                AlphaModuleFeature feature = features.get(i);
                drawRect(left + 10, featureY + i * 25, right - 10, featureY + i * 25 + 22,
                        (i % 2 == 0) ? 0x332A2D35 : 0x221A1C22);
                drawString(fontRendererObj, fit(feature.getName(), panelWidth / 2 - 25),
                        left + 16, featureY + i * 25 + 3, 0xFFE7E9EE);
                String status = feature.getStatusLabel();
                drawString(fontRendererObj, status,
                        right - 16 - fontRendererObj.getStringWidth(status),
                        featureY + i * 25 + 3,
                        feature.isAvailable() ? 0xFF9CE2AC : 0xFFB8BDC8);
                drawWrapped(feature.getDescription(), left + 16, featureY + i * 25 + 12,
                        panelWidth - 32, 0xFF9298A4, 1);
            }
            if (features.size() > rows) {
                drawString(fontRendererObj, (features.size() - rows) + " more features not shown",
                        left + 14, featureY + rows * 25 + 2, 0xFF9298A4);
            }
        }

        if (!module.isImplemented()) {
            drawCenteredString(fontRendererObj, "This module is not implemented yet.",
                    width / 2, bottom - 80, 0xFFFFD866);
        } else if (!module.isUnlocked()) {
            drawCenteredString(fontRendererObj, "Your current rank does not unlock this module.",
                    width / 2, bottom - 80, 0xFFFFD866);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String fit(String text, int maxWidth) {
        if (fontRendererObj.getStringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        return fontRendererObj.trimStringToWidth(text,
                Math.max(0, maxWidth - fontRendererObj.getStringWidth(ellipsis))) + ellipsis;
    }

    private void drawWrapped(String text, int x, int y, int maxWidth, int color, int maxLines) {
        if (text == null || text.isEmpty() || maxLines <= 0) return;
        StringBuilder line = new StringBuilder();
        int lines = 0;
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fontRendererObj.getStringWidth(candidate) > maxWidth && line.length() > 0) {
                drawString(fontRendererObj, line.toString(), x, y, color);
                y += 10;
                lines++;
                if (lines >= maxLines) return;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0 && lines < maxLines) {
            drawString(fontRendererObj, fit(line.toString(), maxWidth), x, y, color);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
