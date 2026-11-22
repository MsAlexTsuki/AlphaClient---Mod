// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import java.util.Locale;

/** Compact AlphaClient-style settings for the Potion HUD. */
public final class PotionHudInfoSettingsGui extends AlphaGuiScreen {
    private static final int ICONS=1, LEVELS=2, CHAT=3, ROMAN=4, OPACITY_DOWN=5,
            OPACITY_UP=6, SCALE_DOWN=7, SCALE_UP=8, EFFECTS=9, BACK=10;
    private final GuiScreen parent;
    private final PotionHudInfoModule settings=PotionHudInfoModule.getInstance();
    private int left,top,right,bottom;

    public PotionHudInfoSettingsGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui(){
        settings.load();
        buttonList.clear();
        left=width/2-160;right=left+320;top=Math.max(10,height/2-132);bottom=Math.min(height-10,top+264);
        int x=left+16,y=top+58,w=288;
        buttonList.add(new AlphaButton(ICONS,x,y,w,20,"POTION ICONS: "+state(settings.isRenderIcons()),settings.isRenderIcons()));
        buttonList.add(new AlphaButton(LEVELS,x,y+25,w,20,"AMPLIFIER LEVELS ABOVE IV: "+state(settings.isRenderLevelsAbove4()),settings.isRenderLevelsAbove4()));
        buttonList.add(new AlphaButton(CHAT,x,y+50,w,20,"SHOW WHILE CHAT IS OPEN: "+state(settings.isShowWhileChat()),settings.isShowWhileChat()));
        buttonList.add(new AlphaButton(ROMAN,x,y+75,w,20,"ROMAN NUMERAL LEVELS: "+state(settings.isRomanNumerals()),settings.isRomanNumerals()));
        buttonList.add(new AlphaButton(OPACITY_DOWN,left+16,top+181,24,18,"-"));
        buttonList.add(new AlphaButton(OPACITY_UP,left+280,top+181,24,18,"+"));
        buttonList.add(new AlphaButton(SCALE_DOWN,left+16,top+210,24,18,"-"));
        buttonList.add(new AlphaButton(SCALE_UP,left+280,top+210,24,18,"+"));
        buttonList.add(new AlphaButton(EFFECTS,left+116,bottom-25,188,18,"POTION EFFECTS",true));
        buttonList.add(new AlphaButton(BACK,left+16,bottom-25,76,18,"BACK"));
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
        drawRect(0,0,width,height,0x30000000);
        drawRect(left,top,right,bottom,0xE00A0C11);
        drawRect(left,top,right,top+2,0xFFFFD866);
        drawCenteredString(fontRendererObj,"POTION HUD",width/2,top+15,0xFFF3F4F6);
        drawCenteredString(fontRendererObj,"DISPLAY",width/2,top+39,0xFFFFD866);
        drawString(fontRendererObj,"OPACITY",left+48,top+186,0xFFB8BDC8);
        drawCenteredString(fontRendererObj,settings.getOpacity()+" / 255",width/2,top+186,0xFFE6E8ED);
        drawString(fontRendererObj,"SIZE",left+48,top+215,0xFFB8BDC8);
        drawCenteredString(fontRendererObj,String.format(Locale.ROOT,"%.2f x",settings.getScale()),width/2,top+215,0xFFE6E8ED);
        super.drawScreen(mx,my,partial);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}