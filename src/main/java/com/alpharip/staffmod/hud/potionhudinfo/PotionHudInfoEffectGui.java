// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.potion.Potion;

import java.io.IOException;

/** Per-effect appearance and low-duration warning editor. */
public final class PotionHudInfoEffectGui extends AlphaGuiScreen {
    private static final int NAME_R_DOWN=1, NAME_G_DOWN=2, NAME_B_DOWN=3,
            TIMER_R_DOWN=4, TIMER_G_DOWN=5, TIMER_B_DOWN=6,
            CHROMA_NAME=7, CHROMA_TIMER=8, BOLD=9, ITALIC=10, UNDERLINE=11, FLICKER=12,
            THRESHOLD_DOWN=13, THRESHOLD_UP=14, SPEED_DOWN=15, SPEED_UP=16, SAVE=17, BACK=18,
            NAME_R_UP=19, NAME_G_UP=20, NAME_B_UP=21, TIMER_R_UP=22, TIMER_G_UP=23, TIMER_B_UP=24;
    private final GuiScreen parent;
    private final int effectId;
    private final PotionHudInfoModule module = PotionHudInfoModule.getInstance();
    private PotionHudInfoEffectSetting setting;
    private GuiTextField customName;
    private int nameFieldX, nameFieldY;

    public PotionHudInfoEffectGui(GuiScreen parent, int effectId) { this.parent=parent; this.effectId=effectId; }

    @Override public void initGui() {
        setting = module.getEffect(effectId);
        buttonList.clear();
        int left = width/2 - 205;
        int top = height/2 - 111;
        addColorControls(left+8, top+43, false);
        addColorControls(left+218, top+43, true);
        buttonList.add(new AlphaButton(CHROMA_NAME, left+8, top+88, 196, 21,
                "Rainbow name  " + state(setting.isChromaName()), false, setting.isChromaName()));
        buttonList.add(new AlphaButton(CHROMA_TIMER, left+218, top+88, 196, 21,
                "Rainbow timer  " + state(setting.isChromaTimer()), false, setting.isChromaTimer()));
        buttonList.add(new AlphaButton(BOLD, left+8, top+132, 130, 21, "Bold  " + state(setting.isBold()), false, setting.isBold()));
        buttonList.add(new AlphaButton(ITALIC, left+146, top+132, 130, 21, "Italic  " + state(setting.isItalic()), false, setting.isItalic()));
        buttonList.add(new AlphaButton(UNDERLINE, left+284, top+132, 130, 21, "Underline  " + state(setting.isUnderlined()), false, setting.isUnderlined()));
        buttonList.add(new AlphaButton(FLICKER, left+8, top+176, 196, 21,
                "Low-time warning  " + state(setting.isFlickerWhenLow()), false, setting.isFlickerWhenLow()));
        buttonList.add(new AlphaButton(THRESHOLD_DOWN, left+218, top+176, 42, 21, "−"));
        buttonList.add(new AlphaButton(THRESHOLD_UP, left+370, top+176, 42, 21, "+"));
        buttonList.add(new AlphaButton(SPEED_DOWN, left+218, top+202, 42, 21, "−"));
        buttonList.add(new AlphaButton(SPEED_UP, left+370, top+202, 42, 21, "+"));
        nameFieldX = left+8;
        nameFieldY = top+232;
        customName = new GuiTextField(30, fontRendererObj, nameFieldX, nameFieldY, 250, 19);
        customName.setMaxStringLength(64);
        customName.setText(setting.getCustomName());
        customName.setTextColor(0xFFE7E9EE);
        customName.setDisabledTextColour(0xFFB8BDC8);
        buttonList.add(new AlphaButton(SAVE, left+268, top+231, 76, 21, "SAVE", true));
        buttonList.add(new AlphaButton(BACK, left+350, top+231, 64, 21, "BACK"));
    }

    private void addColorControls(int x, int y, boolean timer) {
        int down = timer ? TIMER_R_DOWN : NAME_R_DOWN;
        int up = timer ? TIMER_R_UP : NAME_R_UP;
        int[] downs = {down, timer ? TIMER_G_DOWN : NAME_G_DOWN, timer ? TIMER_B_DOWN : NAME_B_DOWN};
        int[] ups = {up, timer ? TIMER_G_UP : NAME_G_UP, timer ? TIMER_B_UP : NAME_B_UP};
        String[] channels = {"R", "G", "B"};
        for (int i=0; i<3; i++) {
            int cx = x + i*66;
            buttonList.add(new AlphaButton(downs[i], cx, y+19, 29, 20, channels[i]+"−"));
            buttonList.add(new AlphaButton(ups[i], cx+32, y+19, 29, 20, channels[i]+"+"));
        }
    }

    private String state(boolean value) { return value ? "ON" : "OFF"; }
    private int channel(int color, int shift) { return (color >> shift) & 255; }
    private int adjust(int color, int shift, int amount) {
        int value = Math.max(0, Math.min(255, channel(color, shift)+amount));
        return (color & ~(255 << shift)) | (value << shift);
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null || setting == null) return;
        if (button.id == SAVE || button.id == BACK) {
            setting.setCustomName(customName.getText());
            if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
            initGui();
            return;
        }
        int name = setting.getRawNameColor(), timer = setting.getRawTimerColor();
        switch (button.id) {
            case NAME_R_DOWN: setting.setNameColor(adjust(name,16,-16)); break;
            case NAME_G_DOWN: setting.setNameColor(adjust(name,8,-16)); break;
            case NAME_B_DOWN: setting.setNameColor(adjust(name,0,-16)); break;
            case TIMER_R_DOWN: setting.setTimerColor(adjust(timer,16,-16)); break;
            case TIMER_G_DOWN: setting.setTimerColor(adjust(timer,8,-16)); break;
            case TIMER_B_DOWN: setting.setTimerColor(adjust(timer,0,-16)); break;
            case NAME_R_UP: setting.setNameColor(adjust(name,16,16)); break;
            case NAME_G_UP: setting.setNameColor(adjust(name,8,16)); break;
            case NAME_B_UP: setting.setNameColor(adjust(name,0,16)); break;
            case TIMER_R_UP: setting.setTimerColor(adjust(timer,16,16)); break;
            case TIMER_G_UP: setting.setTimerColor(adjust(timer,8,16)); break;
            case TIMER_B_UP: setting.setTimerColor(adjust(timer,0,16)); break;
            case CHROMA_NAME: setting.setChromaName(!setting.isChromaName()); break;
            case CHROMA_TIMER: setting.setChromaTimer(!setting.isChromaTimer()); break;
            case BOLD: setting.setBold(!setting.isBold()); break;
            case ITALIC: setting.setItalic(!setting.isItalic()); break;
            case UNDERLINE: setting.setUnderlined(!setting.isUnderlined()); break;
            case FLICKER: setting.setFlickerWhenLow(!setting.isFlickerWhenLow()); break;
            case THRESHOLD_DOWN: setting.setFlickerStartTime(setting.getFlickerStartTime()-1); break;
            case THRESHOLD_UP: setting.setFlickerStartTime(setting.getFlickerStartTime()+1); break;
            case SPEED_DOWN: setting.setFlickerSpeed(setting.getFlickerSpeed()-1); break;
            case SPEED_UP: setting.setFlickerSpeed(setting.getFlickerSpeed()+1); break;
            default: return;
        }
        initGui();
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (customName != null && customName.isFocused()) { customName.textboxKeyTyped(typedChar, keyCode); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (customName != null) customName.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x50000000);
        int left=width/2-213, top=height/2-119, right=width/2+213, bottom=height/2+151;
        drawRect(left,top,right,bottom,0xE60A0C12);
        drawRect(left,top,right,top+1,0xFFFFD866);
        drawString(fontRendererObj, "EFFECT APPEARANCE", left+12, top+10, 0xFFF3F4F6);
        drawString(fontRendererObj, PotionHudInfoModule.getPotionName(effectId), left+12, top+23, 0xFFFFD866);
        drawRect(left+8,top+38,left+205,top+40,0x554B515D);
        drawRect(left+218,top+38,left+414,top+40,0x554B515D);
        drawString(fontRendererObj,"NAME COLOR",left+8,top+34,0xFFB8BDC8);
        drawString(fontRendererObj,"TIMER COLOR",left+218,top+34,0xFFB8BDC8);
        drawRect(left+8,top+64,left+204,top+82,0xFF000000 | (setting.getRawNameColor() & 0xFFFFFF));
        drawRect(left+218,top+45,left+414,top+59,0xFF000000 | (setting.getRawTimerColor() & 0xFFFFFF));
        drawCenteredString(fontRendererObj,hex(setting.getRawNameColor()),left+106,top+48,contrast(setting.getRawNameColor()));
        drawCenteredString(fontRendererObj,hex(setting.getRawTimerColor()),left+316,top+48,contrast(setting.getRawTimerColor()));
        drawString(fontRendererObj,"TEXT STYLE",left+8,top+119,0xFFFFD866);
        drawString(fontRendererObj,"LOW-TIME WARNING",left+8,top+163,0xFFFFD866);
        drawString(fontRendererObj,"Alert below",left+218,top+182,0xFFB8BDC8);
        drawString(fontRendererObj,setting.getFlickerStartTime()+" seconds",left+265,top+182,0xFFF3F4F6);
        drawString(fontRendererObj,"Flicker interval",left+218,top+208,0xFFB8BDC8);
        drawString(fontRendererObj,setting.getFlickerSpeed()+" ms",left+315,top+208,0xFFF3F4F6);
        customName.drawTextBox();
        if(customName.getText().isEmpty()) fontRendererObj.drawString("Custom name (leave blank for default)",nameFieldX+5,nameFieldY+6,0xFF777D88);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    private String hex(int color) { return String.format(java.util.Locale.ROOT,"#%06X",color&0xFFFFFF); }
    private int contrast(int color) {
        int r=(color>>16)&255, g=(color>>8)&255, b=color&255;
        return (r*299+g*587+b*114)>145000 ? 0xFF101116 : 0xFFF3F4F6;
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
