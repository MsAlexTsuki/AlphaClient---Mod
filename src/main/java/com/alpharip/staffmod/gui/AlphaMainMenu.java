// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Random;

/**
 * Custom AlphaClient main menu with a procedurally rendered snowy fortress
 * and continuously animated snowfall.
 */
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
            new ResourceLocation("alphastaffmod", "textures/gui/alpha_client_logo.png");

    private static final int SNOW_COUNT = 150;
    private final Snowflake[] snowflakes = new Snowflake[SNOW_COUNT];

    private boolean accountMenuOpen;
    private int buttonWidth;
    private int buttonHeight;
    private int buttonGap;
    private int menuLeft;
    private int menuTop;
    private boolean compactLayout;
    private long snowfallStartTime;

    @Override
    public void initGui() {
        buttonList.clear();

        compactLayout = width < 900 || height < 540;
        buttonWidth = Math.min(compactLayout ? 280 : 320, Math.max(180, width - 48));
        buttonHeight = height < 360 ? 22 : height < 420 ? 26 : 32;
        buttonGap = height < 360 ? 3 : height < 420 ? 5 : 8;

        int menuHeight = 5 * buttonHeight + 4 * buttonGap;
        int logoWidth = getLogoWidth();
        int logoHeight = logoWidth * 448 / 512;
        int logoBottom = Math.max(8, height / 22) + logoHeight;
        int bottomMargin = height < 360 ? 18 : 34;
        int logoGap = height < 360 ? 10 : 20;
        int verticalBias = height < 360 ? 42 : 60;
        int preferredTop = Math.max(logoBottom + logoGap, (height - menuHeight) / 2 + verticalBias);
        menuTop = Math.min(preferredTop, Math.max(12, height - menuHeight - bottomMargin));
        menuLeft = (width - buttonWidth) / 2;

        buttonList.add(new AlphaButton(PLAY, menuLeft, menuTop, buttonWidth, buttonHeight, "Play", true));
        buttonList.add(new AlphaButton(MULTIPLAYER, menuLeft, menuTop + buttonHeight + buttonGap,
                buttonWidth, buttonHeight, "Multiplayer"));
        buttonList.add(new AlphaButton(MODULES, menuLeft, menuTop + 2 * (buttonHeight + buttonGap),
                buttonWidth, buttonHeight, "Mods"));
        buttonList.add(new AlphaButton(OPTIONS, menuLeft, menuTop + 3 * (buttonHeight + buttonGap),
                buttonWidth, buttonHeight, "Settings"));
        buttonList.add(new AlphaButton(QUIT, menuLeft, menuTop + 4 * (buttonHeight + buttonGap),
                buttonWidth, buttonHeight, "Quit"));

        buttonList.add(new AlphaButton(ACCOUNT_MENU, 24, 22, 132, 27,
                accountMenuOpen ? "Account  -" : "Account  +"));
        if (accountMenuOpen) {
            buttonList.add(new AlphaButton(ACCOUNT_SWITCHER, 24, 55, 132, 25, "Account switcher"));
        }

        if (snowflakes[0] == null) {
            initializeSnowfall();
        }
        if (snowfallStartTime == 0L) {
            snowfallStartTime = System.currentTimeMillis();
        }
    }

    private int getLogoWidth() {
        int available = Math.max(72, width - 48);
        int preferred = compactLayout ? 142 : 188;
        if (height < 360) {
            preferred = Math.min(84, Math.max(56, (height - 160) * 512 / 448));
        } else if (height < 420) {
            preferred = 112;
        }
        return Math.min(preferred, available);
    }

    private void initializeSnowfall() {
        Random random = new Random(189);
        for (int i = 0; i < snowflakes.length; i++) {
            snowflakes[i] = new Snowflake(
                    random.nextFloat(),
                    random.nextFloat(),
                    18.0F + random.nextFloat() * 48.0F,
                    4.0F + random.nextFloat() * 22.0F,
                    1 + random.nextInt(3),
                    random.nextFloat() * 6.2831855F
            );
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawAlphaBackground();
        drawLogo();

        if (height >= 390) {
            drawString(fontRendererObj, "ALPHA CLIENT", 12, height - 28, ACCENT);
            drawString(fontRendererObj, "v" + com.alpharip.staffmod.BuildInfo.VERSION
                    + "  /  FORGE 1.8.9", 12, height - 16, 0xFFD0D5DF);
            String serverLabel = "Alpha.Rip";
            drawString(fontRendererObj, serverLabel,
                    width - fontRendererObj.getStringWidth(serverLabel) - 12,
                    height - 20, 0xFFE2E5EC);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawLogo() {
        int logoWidth = getLogoWidth();
        int logoHeight = logoWidth * 448 / 512;
        int logoX = (width - logoWidth) / 2;
        int logoY = Math.max(8, height / 22);

        Minecraft.getMinecraft().getTextureManager().bindTexture(ALPHA_LOGO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(logoX, logoY, 0.0F, 0.0F, 512, 448,
                logoWidth, logoHeight, 512.0F, 448.0F);
        GlStateManager.disableBlend();

    }

    private void drawAlphaBackground() {
        drawGradientRect(0, 0, width, height, 0xFF64738B, 0xFF101722);

        // Distant winter sky and mountain silhouettes.
        drawRect(px(0.04F), py(0.25F), px(0.25F), py(0.48F), 0xFF39485B);
        drawRect(px(0.08F), py(0.20F), px(0.21F), py(0.38F), 0xFF46566A);
        drawRect(px(0.73F), py(0.22F), px(0.94F), py(0.49F), 0xFF354457);
        drawRect(px(0.78F), py(0.17F), px(0.91F), py(0.39F), 0xFF46566A);
        drawRect(px(0.0F), py(0.48F), width, py(0.78F), 0xFF263444);

        // Moonlight and distant stars.
        drawGradientRect(px(0.70F), py(0.04F), px(0.98F), py(0.42F),
                0x335F7898, 0x00101825);
        drawRect(px(0.82F), py(0.12F), px(0.845F), py(0.16F), 0x99E6EDF5);
        drawRect(px(0.828F), py(0.105F), px(0.84F), py(0.17F), 0x55FFFFFF);
        drawRect(px(0.12F), py(0.12F), px(0.122F), py(0.124F), 0xCCFFFFFF);
        drawRect(px(0.36F), py(0.18F), px(0.362F), py(0.184F), 0xAAFFFFFF);
        drawRect(px(0.62F), py(0.10F), px(0.623F), py(0.104F), 0xAAFFFFFF);
        drawRect(px(0.92F), py(0.28F), px(0.923F), py(0.284F), 0xAAFFFFFF);

        drawFortress();

        // Snow-covered foreground and frozen water.
        drawGradientRect(0, py(0.76F), width, height, 0xB9DCE5EF, 0xE9081724);
        drawRect(0, py(0.79F), width, py(0.805F), 0x66DDEAF4);
        drawRect(0, py(0.88F), width, py(0.885F), 0x335B829E);
        drawRect(px(0.03F), py(0.83F), px(0.25F), py(0.88F), 0x553D5267);
        drawRect(px(0.72F), py(0.82F), px(0.98F), py(0.89F), 0x55405267);
        drawRect(px(0.0F), py(0.94F), px(0.34F), height, 0x443C5368);
        drawRect(px(0.68F), py(0.93F), width, height, 0x443C5368);

        drawSnowfall();
    }

    private void drawFortress() {
        int baseY = py(0.76F);

        // Main keep.
        drawRect(px(0.29F), py(0.39F), px(0.71F), baseY, 0xFF202B38);
        drawRect(px(0.31F), py(0.37F), px(0.69F), py(0.41F), 0xFF354354);
        drawRect(px(0.33F), py(0.34F), px(0.67F), py(0.38F), 0xFF263442);
        drawRect(px(0.35F), py(0.32F), px(0.65F), py(0.35F), 0xFFD9E2EA);
        drawRect(px(0.37F), py(0.30F), px(0.63F), py(0.33F), 0xFFECF1F5);

        // Battlements along the keep.
        for (int i = 0; i < 9; i++) {
            int x = px(0.31F + i * 0.043F);
            drawRect(x, py(0.35F), x + Math.max(5, px(0.022F)), py(0.39F), 0xFF2D3B4A);
            drawRect(x, py(0.345F), x + Math.max(5, px(0.022F)), py(0.352F), 0xFFDCE5ED);
        }

        // Side towers and central gatehouse.
        drawTower(0.245F, 0.73F, 0.115F, 0.43F, 0);
        drawTower(0.755F, 0.73F, 0.115F, 0.43F, 1);
        drawTower(0.405F, 0.70F, 0.075F, 0.31F, 2);
        drawTower(0.595F, 0.70F, 0.075F, 0.31F, 3);
        drawTower(0.50F, 0.70F, 0.105F, 0.37F, 4);

        // Fortress walls and snow ledges.
        drawRect(px(0.16F), py(0.60F), px(0.84F), py(0.76F), 0xFF273544);
        drawRect(px(0.16F), py(0.59F), px(0.84F), py(0.605F), 0xFFD8E2EB);
        drawRect(px(0.18F), py(0.57F), px(0.82F), py(0.59F), 0xFF354658);
        for (int i = 0; i < 18; i++) {
            int x = px(0.17F + i * 0.037F);
            drawRect(x, py(0.565F), x + Math.max(4, px(0.018F)), py(0.60F), 0xFF2D3C4B);
            drawRect(x, py(0.562F), x + Math.max(4, px(0.018F)), py(0.568F), 0xFFDCE5ED);
        }

        // Gate opening, illuminated from within.
        drawRect(px(0.455F), py(0.58F), px(0.545F), py(0.77F), 0xFF101923);
        drawRect(px(0.465F), py(0.60F), px(0.535F), py(0.77F), 0xFF604A2C);
        drawRect(px(0.478F), py(0.615F), px(0.522F), py(0.77F), 0xFFB77B32);
        drawRect(px(0.49F), py(0.625F), px(0.51F), py(0.77F), 0xFFFFD77C);
        drawRect(px(0.45F), py(0.575F), px(0.55F), py(0.59F), 0xFFE5EDF4);

        // Warm windows and lanterns.
        drawWindow(0.335F, 0.47F);
        drawWindow(0.385F, 0.51F);
        drawWindow(0.615F, 0.51F);
        drawWindow(0.665F, 0.47F);
        drawWindow(0.475F, 0.44F);
        drawWindow(0.525F, 0.44F);
        drawLantern(0.19F, 0.68F);
        drawLantern(0.81F, 0.68F);
    }

    private void drawTower(float center, float base, float towerWidth, float towerHeight, int variant) {
        int w = Math.max(12, px(towerWidth));
        int h = Math.max(24, py(towerHeight));
        int x = px(center) - w / 2;
        int y = py(base) - h;
        int edge = Math.max(2, w / 12);

        drawRect(x, y + h / 8, x + w, py(base), 0xFF1D2936);
        drawRect(x + edge, y + h / 8, x + w - edge, py(base), 0xFF354454);
        drawRect(x + edge, y + h / 8, x + edge + 2, py(base), 0xFF526174);
        drawRect(x + w - edge - 2, y + h / 8, x + w - edge, py(base), 0xFF182330);

        // Stepped, snow-covered battlement roof.
        drawRect(x - edge, y + h / 10, x + w + edge, y + h / 5, 0xFF273646);
        drawRect(x - edge, y + h / 10, x + w + edge, y + h / 10 + Math.max(3, h / 45), 0xFFDCE6EF);
        drawRect(x + w / 8, y + h / 18, x + w - w / 8, y + h / 10, 0xFF344455);
        drawRect(x + w / 8, y + h / 18, x + w - w / 8, y + h / 18 + Math.max(3, h / 50), 0xFFE9F0F5);
        drawRect(x + w / 3, y, x + 2 * w / 3, y + h / 18, 0xFF3B4B5D);
        drawRect(x + w / 3, y, x + 2 * w / 3, y + Math.max(3, h / 60), 0xFFF2F5F8);

        // Narrow golden slit windows.
        int windowY = y + h / 2;
        drawRect(x + w / 3, windowY, x + 2 * w / 3, windowY + Math.max(8, h / 7), 0xFF8C642F);
        drawRect(x + w / 3 + 2, windowY + 2, x + 2 * w / 3 - 2,
                windowY + Math.max(7, h / 7) - 2, 0xFFFFD47A);
        drawRect(x + w / 3, windowY + h / 3, x + 2 * w / 3, windowY + h / 3 + 2, 0xFFFFE7A8);

        if (variant == 4) {
            drawRect(x + w / 2 - 2, y - Math.max(10, h / 9),
                    x + w / 2 + 2, y + 1, 0xFF1C2632);
            drawRect(x + w / 2 + 2, y - Math.max(10, h / 9),
                    x + w / 2 + w / 4, y - Math.max(5, h / 18), ACCENT);
        }
    }

    private void drawWindow(float center, float top) {
        int x = px(center);
        int y = py(top);
        int w = Math.max(7, px(0.014F));
        int h = Math.max(12, py(0.045F));
        drawRect(x - w, y - 2, x + w, y + h, 0xFF172330);
        drawRect(x - w + 2, y, x + w - 2, y + h - 2, 0xFF9C6C32);
        drawRect(x - w + 4, y + 2, x + w - 4, y + h - 4, 0xFFFFD783);
        drawRect(x - 1, y, x + 1, y + h, 0xFF6B512E);
    }

    private void drawLantern(float center, float top) {
        int x = px(center);
        int y = py(top);
        int size = Math.max(5, px(0.012F));
        drawRect(x - size, y, x + size, y + size * 2, 0xFF172330);
        drawRect(x - size + 2, y + 2, x + size - 2, y + size * 2 - 2, 0xFFFFD36F);
        drawRect(x - 1, y - size, x + 1, y + size * 2, 0xFF172330);
        drawRect(x - size / 2, y - size - 2, x + size / 2, y - size, ACCENT);
    }

    private void drawSnowfall() {
        float elapsed = (System.currentTimeMillis() - snowfallStartTime) / 1000.0F;
        for (Snowflake snow : snowflakes) {
            float yPosition = (snow.startY * height + elapsed * snow.speed) % (height + 12.0F) - 6.0F;
            float xPosition = snow.x * width
                    + (float) Math.sin(elapsed * 0.65F + snow.phase) * snow.drift;
            int x = (int) xPosition;
            int y = (int) yPosition;
            int color = snow.size == 1 ? 0x99F3F7FC : 0xDDF7FAFF;
            drawRect(x, y, x + snow.size, y + snow.size, color);
            if (snow.size >= 3) {
                drawRect(x - 1, y + 1, x + snow.size + 1, y + 2, 0x66FFFFFF);
            }
        }
    }

    private int px(float fraction) {
        return Math.round(width * fraction);
    }

    private int py(float fraction) {
        return Math.round(height * fraction);
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
                minecraft.displayGuiScreen(new GuiOptions(this, minecraft.gameSettings));
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

    private static final class Snowflake {
        private final float x;
        private final float startY;
        private final float speed;
        private final float drift;
        private final int size;
        private final float phase;

        private Snowflake(float x, float startY, float speed, float drift, int size, float phase) {
            this.x = x;
            this.startY = startY;
            this.speed = speed;
            this.drift = drift;
            this.size = size;
            this.phase = phase;
        }
    }
}
