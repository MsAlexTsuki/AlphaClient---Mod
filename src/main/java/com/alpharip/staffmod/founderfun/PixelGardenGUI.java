// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/**
 * First playable Pixel Garden screen.
 * Cloud persistence is intentionally kept separate from this initial gameplay pass.
 */
public final class PixelGardenGUI extends GuiScreen {
    private static final int BUY_SEEDS = 1;
    private static final int SELL_ONE = 2;
    private static final int SELL_ALL = 3;
    private static final int BACK_BUTTON = 4;
    private static final int PLOT_BUTTON_BASE = 100;
    private final GuiScreen parent;
    private final PixelGardenGame game = PixelGardenGame.getInstance();

    public PixelGardenGUI(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int gridWidth = Math.min(300, width - 24);
        int gap = 6;
        int plotWidth = Math.max(70, (gridWidth - gap * 2) / 3);
        int plotHeight = 28;
        int startX = (width - (plotWidth * 3 + gap * 2)) / 2;
        int startY = 108;
        List<PixelGardenGame.Plot> plots = game.getPlots();
        long now = System.currentTimeMillis();

        for (int i = 0; i < plots.size(); i++) {
            PixelGardenGame.Plot plot = plots.get(i);
            String label;
            switch (plot.getState(now)) {
                case LOCKED:
                    label = "LOCKED";
                    break;
                case EMPTY:
                    label = "EMPTY";
                    break;
                case GROWING:
                    long minutes = (plot.getRemainingMillis(now) + 59999L) / 60000L;
                    label = "Growing " + minutes + "m";
                    break;
                case READY:
                    label = "HARVEST";
                    break;
                default:
                    label = "PLOT";
                    break;
            }
            int x = startX + (i % 3) * (plotWidth + gap);
            int y = startY + (i / 3) * (plotHeight + gap);
            buttonList.add(new AlphaButton(PLOT_BUTTON_BASE + i, x, y, plotWidth, plotHeight,
                    (i + 1) + ". " + label));
        }

        int controlsY = startY + 3 * (plotHeight + gap) + 5;
        int controlGap = 5;
        int controlWidth = Math.max(66, (gridWidth - controlGap * 2) / 3);
        int controlsX = (width - (controlWidth * 3 + controlGap * 2)) / 2;
        buttonList.add(new AlphaButton(BUY_SEEDS, controlsX, controlsY, controlWidth, 20, "BUY SEEDS (5)"));
        buttonList.add(new AlphaButton(SELL_ONE, controlsX + controlWidth + controlGap,
                controlsY, controlWidth, 20, "SELL 1"));
        buttonList.add(new AlphaButton(SELL_ALL, controlsX + (controlWidth + controlGap) * 2,
                controlsY, controlWidth, 20, "SELL ALL"));
        buttonList.add(new AlphaButton(BACK_BUTTON, width / 2 - 45, height - 28, 90, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == BUY_SEEDS) {
            game.buyWheatSeeds(5);
            initGui();
            return;
        }
        if (button.id == SELL_ONE) {
            game.sellWheat(1);
            initGui();
            return;
        }
        if (button.id == SELL_ALL) {
            game.sellWheat(game.getWheatInventory());
            initGui();
            return;
        }
        if (button.id >= PLOT_BUTTON_BASE && button.id < PLOT_BUTTON_BASE + game.getPlots().size()) {
            int plotIndex = button.id - PLOT_BUTTON_BASE;
            long now = System.currentTimeMillis();
            PixelGardenGame.Plot plot = game.getPlots().get(plotIndex);
            if (plot.getState(now) == PixelGardenGame.PlotState.EMPTY) {
                game.plant(plotIndex, now);
            } else if (plot.getState(now) == PixelGardenGame.PlotState.READY) {
                game.harvest(plotIndex, now);
            }
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xD90A0C11);
        drawRect(width / 2 - 150, 34, width / 2 + 150, 36, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "PIXEL GARDEN", width / 2, 45, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Coins: " + game.getCoins()
                + "   Wheat seeds: " + game.getWheatSeeds(), width / 2, 65, 0xFFF0F1F4);
        drawCenteredString(fontRendererObj, "Wheat inventory: " + game.getWheatInventory()
                + "   Sell value: " + PixelGardenGame.WHEAT_SELL_PRICE + " coins each",
                width / 2, 79, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Plant an empty plot; click HARVEST when it is ready.",
                width / 2, 94, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Growth time: 20 minutes", width / 2, 242, 0xFF9CE2AC);
        drawCenteredString(fontRendererObj, "Prototype: account-linked cloud saving is not connected yet.",
                width / 2, 256, 0xFF8F96A3);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
