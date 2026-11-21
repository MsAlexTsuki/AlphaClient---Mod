// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.config.HudScaleConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Global Potion HUD settings. */
public final class PotionHudInfoSettingsGui extends AlphaGuiScreen {
    private static final int ICONS=1, LEVELS=2, CHAT=3, ROMAN=4, OPACITY_DOWN=5, OPACITY_UP=6,
            SCALE_DOWN=7, SCALE_UP=8, EFFECTS=9, BACK=10;
    private final GuiScreen parent;
    private final PotionHudInfoModule settings = PotionHudInfoModule.getInstance();

    public PotionHudInfoSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        settings.load();
        buttonList.clear();
        int x = width / 2 - 110, y = height / 2 - 88;
        buttonList.add(new GuiButton(ICONS, x, y, 220, 20, "RENDER ICONS: " + onOff(settings.isRenderIcons())));
        buttonList.add(new GuiButton(LEVELS, x, y+24, 220, 20, "LEVELS ABOVE IV: " + onOff(settings.isRenderLevelsAbove4())));
        buttonList.add(new GuiButton(CHAT, x, y+48, 220, 20, "SHOW WHILE CHAT: " + onOff(settings.isShowWhileChat())));
        buttonList.add(new GuiButton(ROMAN, x, y+72, 220, 20, "ROMAN NUMERALS: " + onOff(settings.isRomanNumerals())));
        buttonList.add(new GuiButton(OPACITY_DOWN, x, y+100, 105, 20, "OPACITY -"));
        buttonList.add(new GuiButton(OPACITY_UP, x+115, y+100, 105, 20, "OPACITY +"));
        buttonList.add(new GuiButton(SCALE_DOWN, x, y+124, 105, 20, "SCALE -"));
        buttonList.add(new GuiButton(SCALE_UP, x+115, y+124, 105, 20, "SCALE +"));
        buttonList.add(new GuiButton(EFFECTS, x, y+152, 220, 20, "EDIT POTION EFFECTS"));
        buttonList.add(new GuiButton(BACK, width/2-55, y+180, 110, 20, "BACK"));
    }

    private String onOff(boolean value) { return value ? "ON" : "OFF"; }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        switch (button.id) {
            case ICONS: settings.setRenderIcons(!settings.isRenderIcons()); break;
            case LEVELS: settings.setRenderLevelsAbove4(!settings.isRenderLevelsAbove4()); break;
            case CHAT: settings.setShowWhileChat(!settings.isShowWhileChat()); break;
            case ROMAN: settings.setRomanNumerals(!settings.isRomanNumerals()); break;
            case OPACITY_DOWN: settings.setOpacity(settings.getOpacity()-16); break;
            case OPACITY_UP: settings.setOpacity(settings.getOpacity()+16); break;
            case SCALE_DOWN: settings.setScale(settings.getScale()-0.05F); break;
            case SCALE_UP: settings.setScale(settings.getScale()+0.05F); break;
            case EFFECTS: mc.displayGuiScreen(new PotionHudInfoEffectsGui(this)); return;
            case BACK: mc.displayGuiScreen(parent); return;
            default: break;
        }
        initGui();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0,0,width,height,0x9906090F);
        drawCenteredString(fontRendererObj, "POTION HUD", width/2, height/2-112, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "Opacity: " + settings.getOpacity() + " / 255", width/2, height/2+75, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Scale: " + String.format(java.util.Locale.ROOT, "%.2f", settings.getScale()), width/2, height/2+87, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
