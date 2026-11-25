// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Card-style tile for modules listed inside Quality, Misc, and Singleplayer folders. */
public final class FolderItemTileButton extends GuiButton {
    private static final int SWITCH_WIDTH = 18;
    private static final int SWITCH_HEIGHT = 9;
    private static final int YELLOW = 0xFFFFD866;

    private final String moduleId;
    private final String moduleName;
    private final boolean implemented;
    private final boolean switchVisible;
    private final boolean enabled;
    private final boolean selected;
    private boolean switchClicked;

    public FolderItemTileButton(int id, int x, int y, int width, int height,
                                String moduleId, String moduleName, boolean implemented,
                                boolean switchVisible, boolean enabled, boolean selected) {
        super(id, x, y, width, height, "");
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.implemented = implemented;
        this.switchVisible = switchVisible;
        this.enabled = enabled;
        this.selected = selected;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;

        int background = hovered ? 0xF02A303B : 0xE0181D27;
        int edge = selected || hovered ? YELLOW : implemented ? 0x66515A68 : 0x443B424D;
        AlphaMenuRender.roundedRect(xPosition, yPosition, xPosition + width, yPosition + height, 7, background);
        AlphaMenuRender.roundedOutline(xPosition, yPosition, xPosition + width, yPosition + height, 7, edge);

        if (switchVisible) {
            int sx = xPosition + width - SWITCH_WIDTH - 7;
            int sy = yPosition + 6;
            AlphaMenuRender.roundedRect(sx, sy, sx + SWITCH_WIDTH, sy + SWITCH_HEIGHT, 5,
                    enabled ? YELLOW : 0xFF454A54);
            int knobX = enabled ? sx + SWITCH_WIDTH - 7 : sx + 2;
            AlphaMenuRender.roundedRect(knobX, sy + 1, knobX + 5, sy + SWITCH_HEIGHT - 1, 3, 0xFFF6F6F6);
        } else if (!implemented) {
            AlphaMenuRender.roundedRect(xPosition + width - 12, yPosition + 8,
                    xPosition + width - 7, yPosition + 13, 3, 0xFF687180);
        }

        String icon = iconFor(moduleId);
        int iconWidth = mc.fontRendererObj.getStringWidth(icon);
        mc.fontRendererObj.drawString(icon, xPosition + (width - iconWidth) / 2,
                yPosition + 13, implemented ? 0xFFE5E7EB : 0xFF858C98, false);

        String name = moduleName;
        int maxWidth = width - 10;
        if (mc.fontRendererObj.getStringWidth(name) > maxWidth) {
            while (name.length() > 3 && mc.fontRendererObj.getStringWidth(name + "...") > maxWidth) {
                name = name.substring(0, name.length() - 1);
            }
            name += "...";
        }
        mc.fontRendererObj.drawString(UiText.normalize(name),
                xPosition + (width - mc.fontRendererObj.getStringWidth(name)) / 2,
                yPosition + height - 11, implemented ? 0xFFF0F1F4 : 0xFF858C98, false);
    }

    public boolean wasSwitchClicked() {
        return switchClicked;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean pressed = super.mousePressed(mc, mouseX, mouseY);
        switchClicked = false;
        if (pressed && switchVisible) {
            int sx = xPosition + width - SWITCH_WIDTH - 7;
            int sy = yPosition + 6;
            switchClicked = mouseX >= sx && mouseX < sx + SWITCH_WIDTH
                    && mouseY >= sy && mouseY < sy + SWITCH_HEIGHT;
        }
        return pressed;
    }

    private static String iconFor(String id) {
        if (id.contains("history")) return "◷";
        if (id.contains("inventory")) return "▤";
        if (id.contains("timer") || id.contains("tracker")) return "◷";
        if (id.contains("auto") || id.contains("replant")) return "◆";
        if (id.contains("world") || id.contains("block")) return "▦";
        if (id.contains("mob") || id.contains("tree")) return "✦";
        if (id.contains("enchant") || id.contains("anvil")) return "✧";
        if (id.contains("bright")) return "☼";
        return "◆";
    }
}
