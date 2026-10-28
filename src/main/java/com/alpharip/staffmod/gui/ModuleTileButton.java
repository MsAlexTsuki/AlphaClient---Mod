// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Card-style module control with an icon, label, and real enabled-state switch. */
public final class ModuleTileButton extends GuiButton {
    private final AlphaModule module;
    private final boolean selected;
    private boolean switchClicked;
    private static final int SWITCH_WIDTH = 18;
    private static final int SWITCH_HEIGHT = 9;
    private static final int YELLOW = 0xFFFFD866;

    public ModuleTileButton(int id, int x, int y, int width, int height,
                            AlphaModule module, boolean selected) {
        super(id, x, y, width, height, "");
        this.module = module;
        this.selected = selected;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;

        boolean folder = "quality".equals(module.getId()) || "pixelgarden".equals(module.getId());
        boolean available = module.isImplemented() && module.isUnlocked() && !folder;
        boolean on = available && module.isEnabled();
        int bg = hovered ? 0xF0272B34 : 0xE0181B22;
        int edge = "pixelgarden".equals(module.getId()) || selected || hovered ? YELLOW : 0x664B515D;
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, bg);
        drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, edge);
        drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height, 0xAA090A0E);
        drawRect(xPosition, yPosition, xPosition + 1, yPosition + height, edge);
        drawRect(xPosition + width - 1, yPosition, xPosition + width, yPosition + height, edge);

        if (!folder) {
            String state = module.getStatusLabel();
            int stateColor = "ON".equals(state) ? YELLOW
                    : ("LOCKED".equals(state) || "COMING SOON".equals(state) ? 0xFF8A8F99 : 0xFFB8BDC8);
            mc.fontRendererObj.drawString(UiText.normalize(state), xPosition + 7, yPosition + 4, stateColor, false);
            int sw = SWITCH_WIDTH, sh = SWITCH_HEIGHT;
            int sx = xPosition + width - sw - 7, sy = yPosition + 6;
            int switchBg = on ? YELLOW : 0xFF454A54;
            drawRect(sx, sy, sx + sw, sy + sh, switchBg);
            int knobX = on ? sx + sw - 7 : sx + 2;
            drawRect(knobX, sy + 1, knobX + 5, sy + sh - 1, 0xFFF6F6F6);
        }

        String icon = iconFor(module.getId());
        int iconWidth = mc.fontRendererObj.getStringWidth(icon);
        mc.fontRendererObj.drawString(icon, xPosition + (width - iconWidth) / 2,
                yPosition + 21, 0xFFE5E7EB, false);
        String name = module.getName();
        int maxWidth = width - 10;
        if (mc.fontRendererObj.getStringWidth(name) > maxWidth) {
            while (name.length() > 3 && mc.fontRendererObj.getStringWidth(name + "...") > maxWidth) {
                name = name.substring(0, name.length() - 1);
            }
            name += "...";
        }
        mc.fontRendererObj.drawString(UiText.normalize(name),
                xPosition + (width - mc.fontRendererObj.getStringWidth(name)) / 2,
                yPosition + height - 11, 0xFFF0F1F4, false);
    }

    public boolean wasSwitchClicked() { return switchClicked; }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean pressed = super.mousePressed(mc, mouseX, mouseY);
        switchClicked = false;
        if (pressed && !"quality".equals(module.getId()) && !"pixelgarden".equals(module.getId())) {
            int sx = xPosition + width - SWITCH_WIDTH - 7;
            int sy = yPosition + 6;
            switchClicked = mouseX >= sx && mouseX < sx + SWITCH_WIDTH
                    && mouseY >= sy && mouseY < sy + SWITCH_HEIGHT;
        }
        return pressed;
    }

    private static String iconFor(String id) {
        if ("fps".equals(id) || "fps-display".equals(id)) return "⌁";
        if (id.contains("cps")) return "CPS";
        if (id.contains("key")) return "⌨";
        if (id.contains("direction")) return "✥";
        if (id.contains("name-viewer")) return "N";
        if ("quality".equals(id)) return "✦";
        if ("staffmods".equals(id)) return "⚒";
        return "◆";
    }
}
