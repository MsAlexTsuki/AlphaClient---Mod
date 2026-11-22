// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import java.util.Locale;

/** Main Potion HUD settings, arranged as a clear two-panel AlphaClient workspace. */
public final class PotionHudInfoSettingsGui extends AlphaGuiScreen {
    private static final int ICONS=1, LEVELS=2, CHAT=3, ROMAN=4, OPACITY_DOWN=5,
            OPACITY_UP=6, SCALE_DOWN=7, SCALE_UP=8, EFFECTS=9, BACK=10;
    private final GuiScreen parent;
    private final PotionHudInfoModule settings=PotionHudInfoModule.getInstance();
    private int panelX, panelY;

    public PotionHudInfoSettingsGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui(){
        settings.load();
        buttonList.clear();
        panelX=(width-600)/2;
        panelY=(height-390)/2;
        int left=panelX+18, right=panelX+312, y=panelY+126;
        buttonList.add(new AlphaButton(ICONS,left,y,270,28,"Potion icons     "+state(settings.isRenderIcons()),false,settings.isRenderIcons()));
        buttonList.add(new AlphaButton(LEVELS,left,y+38,270,28,"Amplifier levels above IV     "+state(settings.isRenderLevelsAbove4()),false,settings.isRenderLevelsAbove4()));
        buttonList.add(new AlphaButton(CHAT,left,y+76,270,28,"Visible while chat is open     "+state(settings.isShowWhileChat()),false,settings.isShowWhileChat()));
        buttonList.add(new AlphaButton(ROMAN,left,y+114,270,28,"Roman numeral levels     "+state(settings.isRomanNumerals()),false,settings.isRomanNumerals()));
        buttonList.add(new AlphaButton(OPACITY_DOWN,right+8,y-54,54,26,"−"));
        buttonList.add(new AlphaButton(OPACITY_UP,right+208,y-54,54,26,"+"));
        buttonList.add(new AlphaButton(SCALE_DOWN,right+8,y+92,54,26,"−"));
        buttonList.add(new AlphaButton(SCALE_UP,right+208,y+92,54,26,"+"));
        buttonList.add(new AlphaButton(EFFECTS,panelX+312,panelY+316,270,32,"MANAGE POTION EFFECTS",true));
        buttonList.add(new AlphaButton(BACK,panelX+18,panelY+316,110,32,"BACK"));
    }
    private String state(boolean value){return value?"ON":"OFF";}

    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b==null)return;
        switch(b.id){
            case ICONS:settings.setRenderIcons(!settings.isRenderIcons());break;
            case LEVELS:settings.setRenderLevelsAbove4(!settings.isRenderLevelsAbove4());break;
            case CHAT:settings.setShowWhileChat(!settings.isShowWhileChat());break;
            case ROMAN:settings.setRomanNumerals(!settings.isRomanNumerals());break;
            case OPACITY_DOWN:settings.setOpacity(settings.getOpacity()-16);break;
            case OPACITY_UP:settings.setOpacity(settings.getOpacity()+16);break;
            case SCALE_DOWN:settings.setScale(settings.getScale()-0.05F);break;
            case SCALE_UP:settings.setScale(settings.getScale()+0.05F);break;
            case EFFECTS:mc.displayGuiScreen(new PotionHudInfoEffectsGui(this));return;
            case BACK:mc.displayGuiScreen(parent);return;
            default:return;
        }
        initGui();
    }

    @Override public void drawScreen(int mx,int my,float partial){
        drawRect(0,0,width,height,0x9906080D);
        drawRect(panelX,panelY,panelX+600,panelY+390,0xF00D1017);
        drawRect(panelX,panelY,panelX+600,panelY+2,0xFFFFD866);
        drawString(fontRendererObj,"ALPHA CLIENT",panelX+18,panelY+14,0xFFFFD866);
        drawString(fontRendererObj,"POTION HUD",panelX+18,panelY+31,0xFFF3F4F6);
        drawString(fontRendererObj,"Configure how active effects appear on your screen.",panelX+18,panelY+48,0xFF9DA4B1);
        drawRect(panelX+18,panelY+69,panelX+582,panelY+70,0x554B515D);
        int left=panelX+18,right=panelX+312,top=panelY+80;
        drawRect(left,top,left+270,panelY+299,0x99191D26);
        drawRect(right,top,right+270,panelY+299,0x99191D26);
        drawString(fontRendererObj,"DISPLAY",left+12,top+12,0xFFFFD866);
        drawString(fontRendererObj,"Choose which information is shown.",left+12,top+27,0xFF9DA4B1);
        drawString(fontRendererObj,"LAYOUT",right+12,top+12,0xFFFFD866);
        drawString(fontRendererObj,"Adjust the HUD's overall appearance.",right+12,top+27,0xFF9DA4B1);
        drawString(fontRendererObj,"OPACITY",right+12,top+54,0xFFE6E8ED);
        drawCenteredString(fontRendererObj,settings.getOpacity()+" / 255",right+135,top+104,0xFFFFD866);
        drawString(fontRendererObj,"SIZE",right+12,top+128,0xFFE6E8ED);
        drawCenteredString(fontRendererObj,String.format(Locale.ROOT,"%.2f×",settings.getScale()),right+135,top+178,0xFFFFD866);
        super.drawScreen(mx,my,partial);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
