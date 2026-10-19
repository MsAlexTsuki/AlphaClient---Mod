package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.gui.ModernButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Details screen opened by selecting an individual Legacy PvP feature. */
public final class LegacyPvpFeatureGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int TOGGLE = 2;
    private final GuiScreen parent;
    private final String featureId;

    public LegacyPvpFeatureGui(GuiScreen parent, String featureId) {
        this.parent = parent;
        this.featureId = featureId == null ? "" : featureId;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        LegacyPvpSettings.load();
        LegacyPvpFeatureCatalog.Feature feature = findFeature();
        if (feature != null && isToggleAvailable(feature.id)) {
            buttonList.add(new ModernButton(TOGGLE, width / 2 - 90, height - 62, 180, 20,
                    getToggleLabel(feature.id)));
        }
        buttonList.add(new ModernButton(BACK, width / 2 - 70, height - 32, 140, 20, "BACK"));
    }

    private LegacyPvpFeatureCatalog.Feature findFeature() {
        for (LegacyPvpFeatureCatalog.Feature feature : LegacyPvpFeatureCatalog.getAll()) {
            if (feature.id.equals(featureId)) return feature;
        }
        return null;
    }

    private boolean isConfigurable(String id) {
        return "blockhit".equals(id) || "camera-bobbing".equals(id)
                || "missed-swing-delay".equals(id) || "bow-animation".equals(id)
                || "potion-punch".equals(id) || "food-drink-animation".equals(id);
    }

    private boolean isToggleAvailable(String id) {
        if ("blockhit".equals(id)) return LegacyPvpTransformer.isInstalled();
        if ("missed-swing-delay".equals(id)) return LegacyPvpTransformer.isMissedSwingDelayInstalled();
        if ("bow-animation".equals(id)) return LegacyPvpTransformer.isBowAnimationInstalled();
        if ("potion-punch".equals(id)) return true;
        return "camera-bobbing".equals(id);
    }

    private String getToggleLabel(String id) {
        if ("blockhit".equals(id)) {
            return LegacyPvpSettings.isBlockhitEnabled() ? "BLOCKHIT: ON" : "BLOCKHIT: OFF";
        }
        if ("camera-bobbing".equals(id)) {
            return LegacyPvpSettings.isDisableViewBobbing() ? "BOBBING: OFF" : "BOBBING: ON";
        }
        if ("bow-animation".equals(id)) {
            return LegacyPvpSettings.isLegacyBowAnimationEnabled() ? "LEGACY BOW: ON" : "LEGACY BOW: OFF";
        }
        if ("potion-punch".equals(id)) {
            return LegacyPvpSettings.isPotionPunchAnimationEnabled() ? "POTION PUNCH: ON" : "POTION PUNCH: OFF";
        }
        if ("food-drink-animation".equals(id)) {
            return LegacyPvpSettings.isFoodDrinkAnimationEnabled() ? "FOOD/DRINK ANIMATION: ON" : "FOOD/DRINK ANIMATION: OFF";
        }
        return LegacyPvpSettings.isRemoveMissedSwingDelayEnabled() ? "MISS DELAY: OFF" : "MISS DELAY: ON";
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == TOGGLE) {
            if ("blockhit".equals(featureId)) {
                if (LegacyPvpTransformer.isInstalled()) {
                    LegacyPvpSettings.setBlockhitEnabled(!LegacyPvpSettings.isBlockhitEnabled());
                }
            } else if ("camera-bobbing".equals(featureId)) {
                LegacyPvpSettings.setDisableViewBobbing(!LegacyPvpSettings.isDisableViewBobbing());
            } else if ("missed-swing-delay".equals(featureId)
                    && LegacyPvpTransformer.isMissedSwingDelayInstalled()) {
                LegacyPvpSettings.setRemoveMissedSwingDelayEnabled(
                        !LegacyPvpSettings.isRemoveMissedSwingDelayEnabled());
            } else if ("bow-animation".equals(featureId)
                    && LegacyPvpTransformer.isBowAnimationInstalled()) {
                LegacyPvpSettings.setLegacyBowAnimationEnabled(
                        !LegacyPvpSettings.isLegacyBowAnimationEnabled());
            } else if ("potion-punch".equals(featureId)) {
                LegacyPvpSettings.setPotionPunchAnimationEnabled(
                        !LegacyPvpSettings.isPotionPunchAnimationEnabled());
            } else if ("food-drink-animation".equals(featureId)) {
                LegacyPvpSettings.setFoodDrinkAnimationEnabled(
                        !LegacyPvpSettings.isFoodDrinkAnimationEnabled());
            }
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x88000000);
        int panelWidth = Math.min(380, width - 20);
        int left = (width - panelWidth) / 2;
        int right = left + panelWidth;
        int top = 28;
        int bottom = height - 20;
        drawRect(left, top, right, bottom, 0xDD0C0C0C);
        drawRect(left, top, right, top + 2, 0xFFFFD866);

        LegacyPvpFeatureCatalog.Feature feature = findFeature();
        if (feature == null) {
            drawCenteredString(fontRendererObj, "FEATURE NOT FOUND", width / 2, top + 22, 0xFFFFFFFF);
        } else {
            drawCenteredString(fontRendererObj, feature.label.toUpperCase(), width / 2, top + 18, 0xFFFFFFFF);
            String status = LegacyPvpFeatureCatalog.getStatusLabel(feature.status);
            if ("blockhit".equals(feature.id) && !LegacyPvpTransformer.isInstalled()) {
                status = "HOOK NOT LOADED";
            } else if ("missed-swing-delay".equals(feature.id)
                    && !LegacyPvpTransformer.isMissedSwingDelayInstalled()) {
                status = "HOOK NOT LOADED";
            } else if ("bow-animation".equals(feature.id)
                    && !LegacyPvpTransformer.isBowAnimationInstalled()) {
                status = "HOOK NOT LOADED";
            }
            drawCenteredString(fontRendererObj, status, width / 2, top + 38,
                    isToggleAvailable(feature.id) ? 0xFF9CE2AC : 0xFFFFD866);
            drawWrapped(feature.detail, left + 16, top + 66, panelWidth - 32, 0xFFE7E9EE);
            if ("blockhit".equals(feature.id)) {
                drawWrapped("This option controls the local legacy swing animation. Server-side combat behavior remains server-authoritative.",
                        left + 16, top + 112, panelWidth - 32, 0xFFB8BDC8);
                if (!LegacyPvpTransformer.isInstalled()) {
                    drawCenteredString(fontRendererObj, "The render hook is not loaded; this setting cannot be changed here.",
                            width / 2, bottom - 88, 0xFFFFD866);
                }
            } else if ("camera-bobbing".equals(feature.id)) {
                drawWrapped("When enabled, camera bobbing is temporarily disabled. Turning this option off restores the previous Minecraft setting.",
                        left + 16, top + 112, panelWidth - 32, 0xFFB8BDC8);
            } else if ("missed-swing-delay".equals(feature.id)) {
                drawWrapped("When enabled, this clears the local click delay only while aiming at air. Block and entity targets keep their normal timing. It does not enable blockhit while looking at air.",
                        left + 16, top + 112, panelWidth - 32, 0xFFB8BDC8);
                if (!LegacyPvpTransformer.isMissedSwingDelayInstalled()) {
                    drawCenteredString(fontRendererObj, "The Minecraft tick hook is not loaded; vanilla timing is unchanged.",
                            width / 2, bottom - 88, 0xFFFFD866);
                }
            } else if ("bow-animation".equals(feature.id)) {
                drawWrapped("When enabled, bows, food and drinks use the 1.7.10 swing movement; the bow also stretches around the model midpoint while drawing. Turn it off to restore vanilla 1.8.9 use transforms.",
                        left + 16, top + 112, panelWidth - 32, 0xFFB8BDC8);
                if (!LegacyPvpTransformer.isBowAnimationInstalled()) {
                    drawCenteredString(fontRendererObj, "The bow render hook is not loaded; vanilla bow rendering is unchanged.",
                            width / 2, bottom - 88, 0xFFFFD866);
                }
            } else if ("food-drink-animation".equals(feature.id)) {
                drawWrapped("This toggle controls the legacy first-person swing while eating food or drinking a potion. It is independent of the bow animation setting.",
                        left + 16, top + 112, panelWidth - 32, 0xFFB8BDC8);
            } else if ("potion-punch".equals(feature.id)) {
                drawWrapped("When enabled, left-clicking while holding a splash potion and keeping right-click held queues a local arm swing. The normal Minecraft mouse events are left untouched; the server still decides whether an attack succeeds.",
                        left + 16, top + 112, panelWidth - 32, 0xFFB8BDC8);
            } else {
                drawWrapped("This feature does not have a working client-side implementation yet, so no toggle is exposed.",
                        left + 16, top + 112, panelWidth - 32, 0xFFB8BDC8);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawWrapped(String text, int x, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty()) return;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fontRendererObj.getStringWidth(candidate) > maxWidth && line.length() > 0) {
                drawString(fontRendererObj, line.toString(), x, y, color);
                y += 12;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) drawString(fontRendererObj, line.toString(), x, y, color);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
