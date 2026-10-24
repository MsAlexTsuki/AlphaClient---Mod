package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.InfoHudModules;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Settings screen for Armor HUD and Potion HUD. */
public final class InfoHudSettingsGui extends AlphaGuiScreen {
    private static final int TOGGLE = 1, PRIMARY = 2, COLOR = 3, BACK = 4, NAMES = 5, DURABILITY = 6;
    private final GuiScreen parent;
    private final boolean armor;

    public InfoHudSettingsGui(GuiScreen parent, boolean armor) {
        this.parent = parent;
        this.armor = armor;
    }

    private AlphaModule module() {
        return AlphaModuleRegistry.find(armor ? "armor-hud-info" : "potion-hud-info");
    }

    @Override public void initGui() {
        buttonList.clear();
        AlphaModule m = module();
        String state = m != null && m.isEnabled() ? "DISABLE MODULE" : "ENABLE MODULE";
        buttonList.add(new AlphaButton(TOGGLE, width / 2 - 100, height / 2 - (armor ? 55 : 34), 200, 20, state));
        if (armor) {
            buttonList.add(new AlphaButton(NAMES, width / 2 - 100, height / 2 - 29, 200, 20,
                    "ITEM NAMES: " + (InfoHudModules.getArmorShowNames() ? "ON" : "OFF")));
            buttonList.add(new AlphaButton(DURABILITY, width / 2 - 100, height / 2 - 3, 200, 20,
                    "DURABILITY: " + (InfoHudModules.getArmorShowDurability() ? "ON" : "OFF")));
            buttonList.add(new AlphaButton(PRIMARY, width / 2 - 100, height / 2 + 23, 200, 20,
                    "LAYOUT: " + InfoHudModules.getArmorLayoutName()));
            buttonList.add(new AlphaButton(COLOR, width / 2 - 100, height / 2 + 49, 200, 20, "HUD COLOR: " + InfoHudModules.getArmorColorName()));
            buttonList.add(new AlphaButton(BACK, width / 2 - 60, height / 2 + 76, 120, 20, "BACK"));
        } else {
            buttonList.add(new AlphaButton(PRIMARY, width / 2 - 100, height / 2 - 8, 200, 20,
                    "MODE: " + InfoHudModules.getPotionModeName()));
            buttonList.add(new AlphaButton(COLOR, width / 2 - 100, height / 2 + 18, 200, 20, "TEXT COLOR: " + InfoHudModules.getPotionColorName()));
            buttonList.add(new AlphaButton(BACK, width / 2 - 60, height / 2 + 48, 120, 20, "BACK"));
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (button.id == TOGGLE) {
            AlphaModule m = module();
            if (m != null && m.isUnlocked() && m.isImplemented()) m.setEnabled(!m.isEnabled());
        } else if (button.id == NAMES && armor) {
            InfoHudModules.toggleArmorShowNames();
        } else if (button.id == DURABILITY && armor) {
            InfoHudModules.toggleArmorShowDurability();
        } else if (button.id == PRIMARY) {
            if (armor) InfoHudModules.cycleArmorLayout(); else InfoHudModules.cyclePotionMode();
        } else if (button.id == COLOR) {
            if (armor) InfoHudModules.cycleArmorColor(); else InfoHudModules.cyclePotionColor();
        }
        initGui();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x9906090F);
        int panelW = Math.min(390, width - 24), left = (width - panelW) / 2;
        drawRect(left, height / 2 - 105, left + panelW, height / 2 + (armor ? 105 : 82), 0xE10D1017);
        drawRect(left, height / 2 - 105, left + panelW, height / 2 - 103, 0xFFFFD866);
        drawCenteredString(fontRendererObj, armor ? "ARMOR HUD" : "POTION HUD", width / 2, height / 2 - 88, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, armor
                ? "Held item + helmet, chestplate, leggings and boots"
                : "Cycle through icons, all effects, buffs and debuffs", width / 2, height / 2 - 69, 0xFFB8BDC8);
        if (armor) {
            drawCenteredString(fontRendererObj, "Background: always OFF", width / 2, height / 2 + 95, 0xFF9CE2AC);
        } else {
            drawCenteredString(fontRendererObj, "Text color preview", width / 2, height / 2 + 74, 0xFFB8BDC8);
            drawCenteredString(fontRendererObj, "Speed II  1:23", width / 2, height / 2 + 87, InfoHudModules.getPotionColor());
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
