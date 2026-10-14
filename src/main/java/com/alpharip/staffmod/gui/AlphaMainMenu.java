package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * AlphaClient's compact main menu.
 *
 * GuiMainMenu supplies Minecraft's existing animated panorama background;
 * this screen layers the Alpha interface over it without replacing the
 * underlying world panorama with a static image.
 */
public final class AlphaMainMenu extends GuiMainMenu {
    private static final int PLAY = 10;
    private static final int MULTIPLAYER = 11;
    private static final int MODULES = 12;
    private static final int OPTIONS = 13;
    private static final int PARTNER = 14;
    private static final int QUIT = 15;

    private static final int ACCENT = 0xFFFFD866;
    private static final ResourceLocation ALPHA_LOGO =
            new ResourceLocation("alphastaffmod", "textures/gui/alpha_launcher_logo.png");
    private int sidebarWidth;
    private int menuButtonWidth;
    private int menuButtonHeight;
    private int menuButtonGap;

    @Override
    public void initGui() {
        // Initialize vanilla panorama state before replacing its default buttons.
        super.initGui();
        buttonList.clear();

        sidebarWidth = Math.max(174, Math.min(204, width / 5));
        menuButtonWidth = sidebarWidth - 40;
        menuButtonHeight = 25;
        menuButtonGap = 7;

        int buttonX = (sidebarWidth - menuButtonWidth) / 2;
        int firstButtonY = Math.max(118, Math.min(154, height / 2 - 58));

        buttonList.add(new AlphaButton(PLAY, buttonX, firstButtonY,
                menuButtonWidth, menuButtonHeight, "PLAY SINGLEPLAYER", true));
        buttonList.add(new AlphaButton(MULTIPLAYER, buttonX, firstButtonY + menuButtonHeight + menuButtonGap,
                menuButtonWidth, menuButtonHeight, "MULTIPLAYER"));
        buttonList.add(new AlphaButton(MODULES, buttonX,
                firstButtonY + (menuButtonHeight + menuButtonGap) * 2,
                menuButtonWidth, menuButtonHeight, "MODULES"));
        buttonList.add(new AlphaButton(OPTIONS, buttonX,
                firstButtonY + (menuButtonHeight + menuButtonGap) * 3,
                menuButtonWidth, menuButtonHeight, "SETTINGS"));
        buttonList.add(new AlphaButton(PARTNER, buttonX,
                firstButtonY + (menuButtonHeight + menuButtonGap) * 4,
                menuButtonWidth, menuButtonHeight, "JOIN ALPHA.RIP"));
        buttonList.add(new AlphaButton(QUIT, 20, height - 37, 64, 19, "QUIT"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        /*
         * Draw the vanilla menu first so its animated Minecraft panorama
         * remains active. The custom interface is layered on top afterwards.
         */
        super.drawScreen(mouseX, mouseY, partialTicks);

        // A light global tint improves text contrast while keeping the scenery visible.
        drawRect(0, 0, width, height, 0x30070A10);
        // Fade the vanilla logo out into the sky rather than placing a large opaque panel over it.
        drawGradientRect(0, 0, width, Math.min(height, 154),
                0xE6101218, 0x00101218);
        drawGradientRect(0, Math.max(0, height - 74), width, height,
                0x00101218, 0xB8101218);

        // Place the real Alpha logo directly over the panorama, without a backing box.
        Minecraft.getMinecraft().getTextureManager().bindTexture(ALPHA_LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawModalRectWithCustomSizedTexture(18, 16, 0, 0, 46, 46, 1238, 1238);
        drawString(fontRendererObj, "ALPHA", 73, 23, ACCENT);
        drawString(fontRendererObj, "CLIENT", 73, 36, 0xFFF5F6F8);
        drawString(fontRendererObj, "MINECRAFT 1.8.9", 20, 69, 0xFFB2B6C0);
        drawString(fontRendererObj, "FORGE EDITION", 20, 81, 0xFFB2B6C0);
        drawString(fontRendererObj, "NAVIGATION", 20, 105, 0xFFB2B6C0);
        drawString(fontRendererObj, "ALPHA.RIP  /  COMMUNITY",
                sidebarWidth + 24, height - 22, 0xFFB2B6C0);
        drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION,
                94, height - 31, 0xFF777D89);

        // Hero copy stays over the scenery, with no oversized central card.
        int heroX = Math.min(width - 220, sidebarWidth + Math.max(28, (width - sidebarWidth) / 12));
        int heroY = Math.max(42, height / 3 - 20);
        drawString(fontRendererObj, "YOUR WORLD.", heroX, heroY, 0xFFF5F6F8);
        drawString(fontRendererObj, "YOUR RULES.", heroX, heroY + 18, ACCENT);
        drawRect(heroX, heroY + 38, heroX + 42, heroY + 40, ACCENT);
        drawString(fontRendererObj, "A cleaner way to play Minecraft.",
                heroX, heroY + 51, 0xFFD0D3DA);
        drawString(fontRendererObj, "Choose a destination from the menu.",
                heroX, heroY + 64, 0xFF9DA3AE);

        // Redraw the custom controls above the tint and background overlays.
        for (GuiButton button : buttonList) {
            button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        }

        GlStateManager.disableLighting();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        switch (button.id) {
            case PLAY:
                minecraft.displayGuiScreen(new GuiSelectWorld(this));
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
            case PARTNER:
                minecraft.displayGuiScreen(new GuiConnecting(this, minecraft,
                        new ServerData("Alpha.Rip", "alpha.rip:25565", false)));
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
