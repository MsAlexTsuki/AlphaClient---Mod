// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.io.IOException;

/** Per-effect colors, chroma, typography, custom name, and low-time flicker settings. */
public final class PotionHudInfoEffectGui extends AlphaGuiScreen {
    private static final int NAME_R_DOWN=1, NAME_G_DOWN=2, NAME_B_DOWN=3,
            TIMER_R_DOWN=4, TIMER_G_DOWN=5, TIMER_B_DOWN=6,
            CHROMA_NAME=7, CHROMA_TIMER=8, BOLD=9, ITALIC=10, UNDERLINE=11, FLICKER=12,
            THRESHOLD_DOWN=13, THRESHOLD_UP=14, SPEED_DOWN=15, SPEED_UP=16, SAVE=17, BACK=18,
            NAME_R_UP=19, NAME_G_UP=20, NAME_B_UP=21, TIMER_R_UP=22, TIMER_G_UP=23, TIMER_B_UP=24;
    private final GuiScreen parent;
    private final int effectId;
    private GuiTextField customName;
    private final PotionHudInfoModule module = PotionHudInfoModule.getInstance();
    private PotionHudInfoEffectSetting setting;
    private int nameFieldX, nameFieldY;

    public PotionHudInfoEffectGui(GuiScreen parent,int effectId){this.parent=parent;this.effectId=effectId;}
    @Override public void initGui(){
        setting=module.getEffect(effectId);
        buttonList.clear();
        int x=width/2-145,y=height/2-112;
        addColorPair(NAME_R_DOWN,NAME_R_UP,x,y,"N-R");
        addColorPair(NAME_G_DOWN,NAME_G_UP,x+96,y,"N-G");
        addColorPair(NAME_B_DOWN,NAME_B_UP,x+192,y,"N-B");
        addColorPair(TIMER_R_DOWN,TIMER_R_UP,x,y+24,"T-R");
        addColorPair(TIMER_G_DOWN,TIMER_G_UP,x+96,y+24,"T-G");
        addColorPair(TIMER_B_DOWN,TIMER_B_UP,x+192,y+24,"T-B");
        buttonList.add(new GuiButton(CHROMA_NAME,x,y+52,140,20,"NAME CHROMA: "+onOff(setting.isChromaName())));
        buttonList.add(new GuiButton(CHROMA_TIMER,x+144,y+52,140,20,"TIMER CHROMA: "+onOff(setting.isChromaTimer())));
        buttonList.add(new GuiButton(BOLD,x,y+76,92,20,"BOLD: "+onOff(setting.isBold())));
        buttonList.add(new GuiButton(ITALIC,x+96,y+76,92,20,"ITALIC: "+onOff(setting.isItalic())));
        buttonList.add(new GuiButton(UNDERLINE,x+192,y+76,92,20,"UNDERLINE: "+onOff(setting.isUnderlined())));
        buttonList.add(new GuiButton(FLICKER,x,y+100,284,20,"FLICKER WHEN LOW: "+onOff(setting.isFlickerWhenLow())));
        buttonList.add(new GuiButton(THRESHOLD_DOWN,x,y+124,140,20,"THRESHOLD -"));
        buttonList.add(new GuiButton(THRESHOLD_UP,x+144,y+124,140,20,"THRESHOLD +"));
        buttonList.add(new GuiButton(SPEED_DOWN,x,y+148,140,20,"FLICKER SPEED -"));
        buttonList.add(new GuiButton(SPEED_UP,x+144,y+148,140,20,"FLICKER SPEED +"));
        nameFieldX=x; nameFieldY=y+176;
        customName=new GuiTextField(30,fontRendererObj,nameFieldX,nameFieldY,284,18);
        customName.setMaxStringLength(64);
        customName.setText(setting.getCustomName());
        buttonList.add(new GuiButton(SAVE,x,y+200,140,20,"SAVE NAME"));
        buttonList.add(new GuiButton(BACK,x+144,y+200,140,20,"BACK"));
    }
    private void addColorPair(int down,int up,int x,int y,String label){
        buttonList.add(new GuiButton(down,x,y,44,20,label+" -"));
        buttonList.add(new GuiButton(up,x+46,y,44,20,label+" +"));
    }
    private String onOff(boolean value){return value?"ON":"OFF";}
    private int channel(int color,int shift){return (color>>shift)&255;}
    private int adjust(int color,int shift,int amount){int c=Math.max(0,Math.min(255,channel(color,shift)+amount));return (color&~(255<<shift))|(c<<shift);}
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b==null)return;
        int name=setting.getRawNameColor(),timer=setting.getRawTimerColor();
        switch(b.id){
            case NAME_R_DOWN:setting.setNameColor(adjust(name,16,-16));break;
            case NAME_G_DOWN:setting.setNameColor(adjust(name,8,-16));break;
            case NAME_B_DOWN:setting.setNameColor(adjust(name,0,-16));break;
            case TIMER_R_DOWN:setting.setTimerColor(adjust(timer,16,-16));break;
            case TIMER_G_DOWN:setting.setTimerColor(adjust(timer,8,-16));break;
            case TIMER_B_DOWN:setting.setTimerColor(adjust(timer,0,-16));break;
            case NAME_R_UP:setting.setNameColor(adjust(name,16,16));break;
            case NAME_G_UP:setting.setNameColor(adjust(name,8,16));break;
            case NAME_B_UP:setting.setNameColor(adjust(name,0,16));break;
            case TIMER_R_UP:setting.setTimerColor(adjust(timer,16,16));break;
            case TIMER_G_UP:setting.setTimerColor(adjust(timer,8,16));break;
            case TIMER_B_UP:setting.setTimerColor(adjust(timer,0,16));break;
            case CHROMA_NAME:setting.setChromaName(!setting.isChromaName());break;
            case CHROMA_TIMER:setting.setChromaTimer(!setting.isChromaTimer());break;
            case BOLD:setting.setBold(!setting.isBold());break;
            case ITALIC:setting.setItalic(!setting.isItalic());break;
            case UNDERLINE:setting.setUnderlined(!setting.isUnderlined());break;
            case FLICKER:setting.setFlickerWhenLow(!setting.isFlickerWhenLow());break;
            case THRESHOLD_DOWN:setting.setFlickerStartTime(setting.getFlickerStartTime()-1);break;
            case THRESHOLD_UP:setting.setFlickerStartTime(setting.getFlickerStartTime()+1);break;
            case SPEED_DOWN:setting.setFlickerSpeed(setting.getFlickerSpeed()-1);break;
            case SPEED_UP:setting.setFlickerSpeed(setting.getFlickerSpeed()+1);break;
            case SAVE:setting.setCustomName(customName.getText());break;
            case BACK:setting.setCustomName(customName.getText());mc.displayGuiScreen(parent);return;
            default:break;
        }
        initGui();
    }
    @Override protected void keyTyped(char typedChar,int keyCode)throws IOException{
        if(customName!=null && customName.isFocused()){customName.textboxKeyTyped(typedChar,keyCode);return;}
        super.keyTyped(typedChar,keyCode);
    }
    @Override protected void mouseClicked(int mouseX,int mouseY,int mouseButton)throws IOException{
        super.mouseClicked(mouseX,mouseY,mouseButton);
        if(customName!=null)customName.mouseClicked(mouseX,mouseY,mouseButton);
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks){
        drawRect(0,0,width,height,0x9906090F);
        drawCenteredString(fontRendererObj,"EFFECT SETTINGS",width/2,height/2-137,0xFFFFFFFF);
        drawCenteredString(fontRendererObj,"Name RGB: "+hex(setting.getRawNameColor())+"   Timer RGB: "+hex(setting.getRawTimerColor()),width/2,height/2-126,0xFFB8BDC8);
        drawCenteredString(fontRendererObj,"Low-time threshold: "+setting.getFlickerStartTime()+"s   Flicker interval: "+setting.getFlickerSpeed()+"ms",width/2,height/2+73,0xFFB8BDC8);
        customName.drawTextBox();
        if(customName.getText().isEmpty())fontRendererObj.drawStringWithShadow("Custom effect name (blank = default)",nameFieldX+4,nameFieldY+5,0xFF777777);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }
    private String hex(int color){return String.format(java.util.Locale.ROOT,"#%06X",color&0xFFFFFF);}
    @Override public boolean doesGuiPauseGame(){return false;}
}
