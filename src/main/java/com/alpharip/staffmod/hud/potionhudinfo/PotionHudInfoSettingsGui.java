// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.Locale;

/** Global Potion HUD settings, styled consistently with the AlphaClient menu. */
public final class PotionHudInfoSettingsGui extends AlphaGuiScreen {
    private static final int ICONS=1, LEVELS=2, CHAT=3, ROMAN=4, OPACITY_DOWN=5, OPACITY_UP=6,
            SCALE_DOWN=7, SCALE_UP=8, EFFECTS=9, BACK=10;
    private final GuiScreen parent;
    private final PotionHudInfoModule settings = PotionHudInfoModule.getInstance();

    public PotionHudInfoSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        settings.load();
        buttonList.clear();
        int left = width / 2 - 104;
        int top = height / 2 - 102;
        buttonList.add(new AlphaButton(ICONS, left, top + 34, 202, 22,
                "Show potion icons  " + state(settings.isRenderIcons()), false, settings.isRenderIcons()));
        buttonList.add(new AlphaButton(LEVELS, left, top + 60, 202, 22,
                "Show amplifier above IV  " + state(settings.isRenderLevelsAbove4()), false, settings.isRenderLevelsAbove4()));
        buttonList.add(new AlphaButton(CHAT, left, top + 86, 202, 22,
                "Show while chat is open  " + state(settings.isShowWhileChat()), false, settings.isShowWhileChat()));
        buttonList.add(new AlphaButton(ROMAN, left, top + 112, 202, 22,
                "Roman numeral levels  " + state(settings.isRomanNumerals()), false, settings.isRomanNumerals()));
        buttonList.add(new AlphaButton(OPACITY_DOWN, left, top + 151, 96, 22, "Opacity  −"));
        buttonList.add(new AlphaButton(OPACITY_UP, left + 106, top + 151, 96, 22, "Opacity  +"));
        buttonList.add(new AlphaButton(SCALE_DOWN, left, top + 177, 96, 22, "Size  −"));
        buttonList.add(new AlphaButton(SCALE_UP, left + 106, top + 177, 96, 22, "Size  +"));
        buttonList.add(new AlphaButton(EFFECTS, left, top + 211, 202, 23, "POTION EFFECTS", true));
        buttonList.add(new AlphaButton(BACK, width / 2 - 43, top + 240, 86, 20, "BACK"));
    }

    private String state(boolean value) { return value ? "ON" : "OFF"; }

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
            default: return;
        }
        initGui();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x50000000);
        int left = width / 2 - 119, top = height / 2 - 119, right = width / 2 + 119, bottom = height / 2 + 162;
        drawRect(left, top, right, bottom, 0xE60A0C12);
        drawRect(left, top, right, top + 1, 0xFFFFD866);
        drawRect(left, top + 1, left + 1, bottom - 1, 0x554B515D);
        drawRect(right - 1, top + 1, right, bottom - 1, 0x554B515D);
        drawCenteredString(fontRendererObj, "POTION HUD", width / 2, top + 10, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "DISPLAY", width / 2, top + 25, 0xFFFFD866);
        drawString(fontRendererObj, "LAYOUT", left + 12, top + 121, 0xFFFFD866);
        drawString(fontRendererObj, "Opacity", left + 12, top + 158, 0xFFB8BDC8);
        drawString(fontRendererObj, settings.getOpacity() + " / 255", left + 132, top + 158, 0xFFF3F4F6);
        drawString(fontRendererObj, "HUD size", left + 12, top + 184, 0xFFB8BDC8);
        drawString(fontRendererObj, String.format(Locale.ROOT, "%.2f×", settings.getScale()), left + 132, top + 184, 0xFFF3F4F6);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
