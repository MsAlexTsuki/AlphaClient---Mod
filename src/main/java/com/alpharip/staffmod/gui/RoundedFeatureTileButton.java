// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Rounded feature card scoped to Quality, Singleplayer and Misc pages. */
public final class RoundedFeatureTileButton extends GuiButton {
    private static final int YELLOW = 0xFFFFD866;
    private static final int SWITCH_WIDTH = 18;
    private static final int SWITCH_HEIGHT = 9;
    private final String name;
    private final String icon;
    private final String status;
    private final boolean active;
    private boolean switchClicked;

    public RoundedFeatureTileButton(int id, int x, int y, int width, int height,
                                    String name, String icon, String status,
                                    boolean active, boolean clickable) {
        super(id, x, y, width, height, "");
        this.name = name;
        this.icon = icon;
        this.status = status;
        this.active = active;
        this.enabled = clickable;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        AlphaMenuRender.roundedRect(xPosition, yPosition, xPosition + width, yPosition + height,
                7.0F, hovered ? 0xF02A303B : 0xE0181D27);
        AlphaMenuRender.roundedOutline(xPosition, yPosition, xPosition + width, yPosition + height,
                7.0F, hovered ? YELLOW : 0x66515A68);

        if (status != null) {
            int stateColor = "ON".equals(status) ? YELLOW
                    : ("PLANNED".equals(status) || "LOCKED".equals(status) ? 0xFF8A8F99 : 0xFFB8BDC8);
            mc.fontRendererObj.drawString(UiText.normalize(status),
                    xPosition + 7, yPosition + 4, stateColor, false);
        }
        if (hasSwitch()) {
            int sx = xPosition + width - SWITCH_WIDTH - 7;
            int sy = yPosition + 6;
            AlphaMenuRender.roundedRect(sx, sy, sx + SWITCH_WIDTH, sy + SWITCH_HEIGHT,
                    5.0F, active ? YELLOW : 0xFF454A54);
            int knobX = active ? sx + SWITCH_WIDTH - 7 : sx + 2;
            AlphaMenuRender.roundedRect(knobX, sy + 1, knobX + 5, sy + SWITCH_HEIGHT - 1,
                    3.0F, 0xFFF6F6F6);
        }

        int iconWidth = mc.fontRendererObj.getStringWidth(icon);
        mc.fontRendererObj.drawString(icon, xPosition + (width - iconWidth) / 2,
                yPosition + Math.max(10, (height - 8) / 2 - 1), 0xFFE5E7EB, false);
        String label = name;
        int maxWidth = width - 10;
        if (mc.fontRendererObj.getStringWidth(label) > maxWidth) {
            while (label.length() > 3 && mc.fontRendererObj.getStringWidth(label + "...") > maxWidth) {
                label = label.substring(0, label.length() - 1);
            }
            label += "...";
        }
        mc.fontRendererObj.drawString(UiText.normalize(label),
                xPosition + (width - mc.fontRendererObj.getStringWidth(label)) / 2,
                yPosition + height - 10, 0xFFF0F1F4, false);
    }

    public boolean wasSwitchClicked() { return switchClicked; }

    private boolean hasSwitch() { return "ON".equals(status) || "OFF".equals(status); }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean pressed = super.mousePressed(mc, mouseX, mouseY);
        switchClicked = false;
        if (pressed && hasSwitch()) {
            int sx = xPosition + width - SWITCH_WIDTH - 7;
            int sy = yPosition + 6;
            switchClicked = mouseX >= sx && mouseX < sx + SWITCH_WIDTH
                    && mouseY >= sy && mouseY < sy + SWITCH_HEIGHT;
        }
        return pressed;
    }
}
