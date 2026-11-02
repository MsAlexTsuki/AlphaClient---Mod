package com.alpharip.staffmod.Minimap;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** In-game settings for the AlphaClient minimap. */
public final class MinimapSettingsGui extends AlphaGuiScreen {
    private final GuiScreen parent;
    private static final int SHAPE = 10, CORNER = 11, SIZE_DOWN = 12, SIZE_UP = 13,
            OFFSET_DOWN = 14, OFFSET_UP = 15, RANGE_DOWN = 16, RANGE_UP = 17, COLOR = 18, BACK = 19;

    public MinimapSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int y = height / 2 - 82;
        buttonList.add(new AlphaButton(SHAPE, center - 100, y, 200, 20, "Shape: " + (MinimapSettings.round() ? "Round" : "Square")));
        y += 24;
        buttonList.add(new GuiButton(CORNER, center - 100, y, 200, 20, "Position: " + title(MinimapSettings.corner())));
        y += 24;
        buttonList.add(new GuiButton(SIZE_DOWN, center - 100, y, 96, 20, "Size -"));
        buttonList.add(new GuiButton(SIZE_UP, center + 4, y, 96, 20, "Size +"));
        y += 24;
        buttonList.add(new GuiButton(OFFSET_DOWN, center - 100, y, 96, 20, "Offset -"));
        buttonList.add(new GuiButton(OFFSET_UP, center + 4, y, 96, 20, "Offset +"));
        y += 24;
        buttonList.add(new GuiButton(RANGE_DOWN, center - 100, y, 96, 20, "Range -"));
        buttonList.add(new GuiButton(RANGE_UP, center + 4, y, 96, 20, "Range +"));
        y += 24;
        buttonList.add(new GuiButton(COLOR, center - 100, y, 200, 20, "Coordinate text color: cycle"));
        y += 24;
        buttonList.add(new GuiButton(BACK, center - 100, y, 200, 20, "Done"));
    }

    private String title(String value) {
        if (MinimapSettings.TOP_LEFT.equals(value)) return "Top Left";
        if (MinimapSettings.BOTTOM_LEFT.equals(value)) return "Bottom Left";
        if (MinimapSettings.BOTTOM_RIGHT.equals(value)) return "Bottom Right";
        return "Top Right";
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case SHAPE:
                MinimapSettings.setString("shape", MinimapSettings.round() ? "square" : "round"); break;
            case CORNER:
                String[] corners = {MinimapSettings.TOP_LEFT, MinimapSettings.TOP_RIGHT, MinimapSettings.BOTTOM_LEFT, MinimapSettings.BOTTOM_RIGHT};
                String current = MinimapSettings.corner();
                int next = 0;
                for (int i = 0; i < corners.length; i++) if (corners[i].equals(current)) next = (i + 1) % corners.length;
                MinimapSettings.setString("corner", corners[next]); break;
            case SIZE_DOWN: MinimapSettings.setInt("size", MinimapSettings.size() - 8); break;
            case SIZE_UP: MinimapSettings.setInt("size", MinimapSettings.size() + 8); break;
            case OFFSET_DOWN: MinimapSettings.setInt("offset", MinimapSettings.offset() - 2); break;
            case OFFSET_UP: MinimapSettings.setInt("offset", MinimapSettings.offset() + 2); break;
            case RANGE_DOWN: MinimapSettings.setInt("range", MinimapSettings.range() - 16); break;
            case RANGE_UP: MinimapSettings.setInt("range", MinimapSettings.range() + 16); break;
            case COLOR:
                int[] colors = {0xFFD866, 0xFFFFFF, 0x55FFFF, 0x55FF55, 0xFF5555};
                int index = 0;
                for (int i = 0; i < colors.length; i++) if (colors[i] == MinimapSettings.coordinateColor()) index = (i + 1) % colors.length;
                MinimapSettings.setInt("coordinateColor", colors[index]); break;
            case BACK:
                mc.displayGuiScreen(parent); return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x88000000);
        int left = width / 2 - 116;
        int top = height / 2 - 112;
        drawRect(left, top, left + 232, top + 232, 0xE00A0C11);
        drawRect(left, top, left + 232, top + 1, 0xFFFFD866);
        drawRect(left, top + 231, left + 232, top + 232, 0x66555A63);
        drawCenteredString(fontRendererObj, "ALPHA MINIMAP SETTINGS", width / 2, top + 8, 0xFFFFD866);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
