package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/** Fully custom AlphaClient menu; deliberately does not render GuiMainMenu or its panorama. */
public final class AlphaMainMenu extends AlphaGuiScreen {
    private static final int PLAY = 10;
    private static final int MULTIPLAYER = 11;
    private static final int MODULES = 12;
    private static final int OPTIONS = 13;
    private static final int QUIT = 15;
    private static final int ACCOUNT_MENU = 16;
    private static final int ACCOUNT_SWITCHER = 17;

    private static final int ACCENT = 0xFFFFD866;
    private static final ResourceLocation ALPHA_LOGO =
            new ResourceLocation("alphastaffmod", "textures/gui/alpha-client-icon.png");

    private boolean accountMenuOpen;
    private int buttonWidth;
    private int buttonHeight;
    private int buttonGap;
    private int menuLeft;
    private int menuTop;
    private boolean compactLayout;

    @Override
    public void initGui() {
        buttonList.clear();

        compactLayout = width < 1000 || height < 620;
        buttonWidth = Math.min(204, Math.max(150, width - 48));
        buttonHeight = height < 420 ? 26 : 32;
        buttonGap = 0;

        // Compact Lunar-inspired composition: logo above one prominent Mods entry.
        menuLeft = (width - buttonWidth) / 2;
        menuTop = Math.min(height - buttonHeight - 30, Math.max(120, height / 2 + 36));

        buttonList.add(new AlphaButton(MODULES, menuLeft, menuTop, buttonWidth, buttonHeight, "MODS", true));
        buttonList.add(new AlphaButton(ACCOUNT_MENU, 24, 22, 90, 24,
                accountMenuOpen ? "ACCOUNT -" : "ACCOUNT"));
        if (accountMenuOpen) {
            buttonList.add(new AlphaButton(ACCOUNT_SWITCHER, 24, 51, 132, 25, "Account switcher"));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawAlphaBackground();

        // Compact Lunar-inspired composition: centered Alpha logo and one Mods button.
        int logoWidth = Math.min(164, Math.max(100, width - 40));
        int logoHeight = logoWidth * 448 / 512;
        int logoX = (width - logoWidth) / 2;
        int logoY = Math.max(16, menuTop - logoHeight - 54);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ALPHA_LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(logoX, logoY, 0.0F, 0.0F, 512, 448,
                logoWidth, logoHeight, 512.0F, 448.0F);
        GlStateManager.disableBlend();
        drawCenteredString(fontRendererObj, "ALPHA CLIENT", width / 2,
                logoY + logoHeight + 5, 0xFFF3F0E8);

        // Render enabled HUD previews in their saved screen positions.
        drawHudPreview();

        if (height >= 390) {
            drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION
                    + " / FORGE 1.8.9", 12, height - 16, 0xFF969DAA);
            drawString(fontRendererObj, "ALPHA.RIP", width - fontRendererObj.getStringWidth("ALPHA.RIP") - 12,
                    height - 16, 0xFFB9BEC8);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHudPreview() {
        try {
            com.alpharip.staffmod.hud.InfoHudModules.load();
            com.alpharip.staffmod.hud.InfoHudModules.renderEditorPreviews();
            if (com.alpharip.staffmod.hud.AfkTimerHud.isEnabled()) {
                com.alpharip.staffmod.hud.AfkTimerHud.renderEditorPreview();
            }
            if (com.alpharip.staffmod.hud.SessionTimerHud.isEnabled()) {
                com.alpharip.staffmod.hud.SessionTimerHud.renderEditorPreview();
            }
            if (com.alpharip.staffmod.hud.PlaytimeTrackerHud.isEnabled()) {
                com.alpharip.staffmod.hud.PlaytimeTrackerHud.renderEditorPreview();
            }
            com.alpharip.staffmod.modules.AlphaModule fpsModule =
                    com.alpharip.staffmod.modules.AlphaModuleRegistry.find("fps");
            if (fpsModule != null && fpsModule.isEnabled() && fpsModule.isUnlocked()) {
                net.minecraft.client.renderer.GlStateManager.pushMatrix();
                net.minecraft.client.renderer.GlStateManager.translate(
                        com.alpharip.staffmod.fps.FpsMod.getHudX(),
                        com.alpharip.staffmod.fps.FpsMod.getHudY(), 0.0F);
                fontRendererObj.drawStringWithShadow("FPS " + Minecraft.getDebugFPS(), 0, 0,
                        com.alpharip.staffmod.fps.FpsMod.getTextColor());
                net.minecraft.client.renderer.GlStateManager.popMatrix();
            }
            com.alpharip.staffmod.modules.AlphaModule pingModule =
                    com.alpharip.staffmod.modules.AlphaModuleRegistry.find("ping");
            if (pingModule != null && pingModule.isEnabled() && pingModule.isUnlocked()) {
                net.minecraft.client.renderer.GlStateManager.pushMatrix();
                net.minecraft.client.renderer.GlStateManager.translate(
                        com.alpharip.staffmod.ping.PingMod.getHudX(),
                        com.alpharip.staffmod.ping.PingMod.getHudY(), 0.0F);
                fontRendererObj.drawStringWithShadow(com.alpharip.staffmod.ping.PingMod.getDisplayText(),
                        0, 0, com.alpharip.staffmod.ping.PingMod.getTextColor());
                net.minecraft.client.renderer.GlStateManager.popMatrix();
            }
        } catch (RuntimeException ignored) {
            // A HUD preview error must not prevent the main menu from opening.
        }
    }

    private void drawAlphaBackground() {
        drawRect(0, 0, width, height, 0xFF090B0F);
        drawGradientRect(0, 0, width, height, 0xB8171A21, 0xC9090A0D);
        drawGradientRect(0, height / 3, width, height, 0x081D2028, 0x00090A0D);
        drawRect(0, 0, width, 1, 0x66FFD866);
        drawRect(0, height - 1, width, height, 0x33252017);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        switch (button.id) {
            case PLAY:
                minecraft.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case ACCOUNT_MENU:
                accountMenuOpen = !accountMenuOpen;
                initGui();
                break;
            case ACCOUNT_SWITCHER:
                minecraft.displayGuiScreen(new AccountInfoGui(this));
                break;
            case MULTIPLAYER:
                minecraft.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case MODULES:
                minecraft.displayGuiScreen(new AlphaModMenu(this));
                break;
            case OPTIONS:
                minecraft.displayGuiScreen(new AlphaSettingsMenu(this));
                break;
            case QUIT:
                minecraft.shutdown();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
