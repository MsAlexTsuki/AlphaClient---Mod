// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.io.IOException;

 /** Compact per-effect appearance editor. */
public final class PotionHudInfoEffectGui extends AlphaGuiScreen {
    private static final int NRD=1,NGD=2,NBD=3,TRD=4,TGD=5,TBD=6,
            CN=7,CT=8,BOLD=9,ITALIC=10,UNDER=11,FLICKER=12,
            TD=13,TU=14,SD=15,SU=16,SAVE=17,BACK=18,
            NRU=19,NGU=20,NBU=21,TRU=22,TGU=23,TBU=24;
    private final GuiScreen parent;
    private final int effectId;
    private final PotionHudInfoModule module=PotionHudInfoModule.getInstance();
    private PotionHudInfoEffectSetting setting;
    private GuiTextField nameField;
    private int left,top,right,bottom;

    public PotionHudInfoEffectGui(GuiScreen parent,int effectId){this.parent=parent;this.effectId=effectId;}

    @Override public void initGui(){
        setting=module.getEffect(effectId);buttonList.clear();
        left=width/2-225;right=left+450;top=Math.max(8,height/2-190);bottom=Math.min(height-8,top+380);
        int lx=left+14,rx=left+238,cy=top+72;
        addColorButtons(lx+4,cy,false);addColorButtons(rx+4,cy,true);
        buttonList.add(new AlphaButton(CN,lx+4,top+151,194,19,"CHROMA NAME: "+state(setting.isChromaName()),false,setting.isChromaName()));
        buttonList.add(new AlphaButton(CT,rx+4,top+151,194,19,"CHROMA TIMER: "+state(setting.isChromaTimer()),false,setting.isChromaTimer()));
        buttonList.add(new AlphaButton(BOLD,lx+4,top+184,92,19,"BOLD: "+state(setting.isBold()),false,setting.isBold()));
        buttonList.add(new AlphaButton(ITALIC,lx+106,top+184,92,19,"ITALIC: "+state(setting.isItalic()),false,setting.isItalic()));
        buttonList.add(new AlphaButton(UNDER,rx+4,top+184,194,19,"UNDERLINE: "+state(setting.isUnderlined()),false,setting.isUnderlined()));
        buttonList.add(new AlphaButton(FLICKER,lx+4,top+224,194,19,"LOW-TIME FLICKER: "+state(setting.isFlickerWhenLow()),false,setting.isFlickerWhenLow()));
        buttonList.add(new AlphaButton(TD,rx+4,top+247,24,18,"-"));
        buttonList.add(new AlphaButton(TU,rx+174,top+247,24,18,"+"));
        buttonList.add(new AlphaButton(SD,rx+4,top+275,24,18,"-"));
        buttonList.add(new AlphaButton(SU,rx+174,top+275,24,18,"+"));
        nameField=new GuiTextField(30,fontRendererObj,left+14,bottom-49,300,18);
        nameField.setMaxStringLength(64);nameField.setText(setting.getCustomName());
        nameField.setTextColor(0xFFE7E9EE);nameField.setDisabledTextColour(0xFF9DA4B1);
        buttonList.add(new AlphaButton(SAVE,left+338,bottom-49,52,18,"SAVE",true));
        buttonList.add(new AlphaButton(BACK,left+396,bottom-49,40,18,"BACK"));
    }

    private void addColorButtons(int x,int y,boolean timer){
        int[] down=timer?new int[]{TRD,TGD,TBD}:new int[]{NRD,NGD,NBD};
        int[] up=timer?new int[]{TRU,TGU,TBU}:new int[]{NRU,NGU,NBU};
        for(int i=0;i<3;i++){
            int cy=y+i*22;
            buttonList.add(new AlphaButton(down[i],x,cy,22,18,"-"));
            buttonList.add(new AlphaButton(up[i],x+166,cy,22,18,"+"));
        }
    }
    private String state(boolean b){return b?"ON":"OFF";}
    private int adjust(int color,int shift,int amount){
        int value=Math.max(0,Math.min(255,((color>>shift)&255)+amount));
        return (color&~(255<<shift))|(value<<shift);
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b==null||setting==null)return;
        if(nameField!=null)setting.setCustomName(nameField.getText());
        if(b.id==SAVE||b.id==BACK){
            setting.setCustomName(nameField.getText());
            if(b.id==BACK){mc.displayGuiScreen(parent);return;}
            initGui();return;
        }
        int name=setting.getRawNameColor(),timer=setting.getRawTimerColor();
        switch(b.id){
            case NRD:setting.setNameColor(adjust(name,16,-16));break;
            case NGD:setting.setNameColor(adjust(name,8,-16));break;
            case NBD:setting.setNameColor(adjust(name,0,-16));break;
            case TRD:setting.setTimerColor(adjust(timer,16,-16));break;
            case TGD:setting.setTimerColor(adjust(timer,8,-16));break;
            case TBD:setting.setTimerColor(adjust(timer,0,-16));break;
            case NRU:setting.setNameColor(adjust(name,16,16));break;
            case NGU:setting.setNameColor(adjust(name,8,16));break;
            case NBU:setting.setNameColor(adjust(name,0,16));break;
            case TRU:setting.setTimerColor(adjust(timer,16,16));break;
            case TGU:setting.setTimerColor(adjust(timer,8,16));break;
            case TBU:setting.setTimerColor(adjust(timer,0,16));break;
            case CN:setting.setChromaName(!setting.isChromaName());break;
            case CT:setting.setChromaTimer(!setting.isChromaTimer());break;
            case BOLD:setting.setBold(!setting.isBold());break;
            case ITALIC:setting.setItalic(!setting.isItalic());break;
            case UNDER:setting.setUnderlined(!setting.isUnderlined());break;
            case FLICKER:setting.setFlickerWhenLow(!setting.isFlickerWhenLow());break;
            case TD:setting.setFlickerStartTime(setting.getFlickerStartTime()-1);break;
            case TU:setting.setFlickerStartTime(setting.getFlickerStartTime()+1);break;
            case SD:setting.setFlickerSpeed(setting.getFlickerSpeed()-1);break;
            case SU:setting.setFlickerSpeed(setting.getFlickerSpeed()+1);break;
            default:return;
        }
        initGui();
    }

    @Override protected void keyTyped(char c,int key)throws IOException{
        if(nameField!=null&&nameField.isFocused()){nameField.textboxKeyTyped(c,key);return;}
        super.keyTyped(c,key);
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{
        super.mouseClicked(x,y,button);
        if(nameField!=null)nameField.mouseClicked(x,y,button);
    }

    @Override public void drawScreen(int mx,int my,float partial){
        drawRect(0,0,width,height,0x30000000);
        drawRect(left,top,right,bottom,0xE00A0C11);
        drawRect(left,top,right,top+2,0xFFFFD866);
        drawCenteredString(fontRendererObj,"CUSTOMIZE EFFECT",width/2,top+13,0xFFF3F4F6);
        drawCenteredString(fontRendererObj,PotionHudInfoModule.getPotionName(effectId),width/2,top+29,0xFFB8BDC8);
        drawString(fontRendererObj,"NAME COLOR",left+18,top+51,0xFFFFD866);
        drawString(fontRendererObj,"TIMER COLOR",left+242,top+51,0xFFFFD866);
        drawRect(left+18,top+64,left+212,top+70,0xFF000000|(setting.getRawNameColor()&0xFFFFFF));
        drawRect(left+242,top+64,left+436,top+70,0xFF000000|(setting.getRawTimerColor()&0xFFFFFF));
        String[] labels={"R","G","B"};
        for(int i=0;i<3;i++){
            int y=top+77+i*22;
            drawString(fontRendererObj,labels[i],left+76,y+5,0xFFB8BDC8);
            drawString(fontRendererObj,String.valueOf((setting.getRawNameColor()>>(16-i*8))&255),left+112,y+5,0xFFE6E8ED);
            drawString(fontRendererObj,labels[i],left+300,y+5,0xFFB8BDC8);
            drawString(fontRendererObj,String.valueOf((setting.getRawTimerColor()>>(16-i*8))&255),left+336,y+5,0xFFE6E8ED);
        }
        drawString(fontRendererObj,"TEXT STYLE",left+18,top+174,0xFFFFD866);
        drawString(fontRendererObj,"LOW-DURATION WARNING",left+18,top+214,0xFFFFD866);
        drawString(fontRendererObj,"Threshold: "+setting.getFlickerStartTime()+" sec",right-190,top+252,0xFFB8BDC8);
        drawString(fontRendererObj,"Speed: "+setting.getFlickerSpeed()+" ms",right-190,top+280,0xFFB8BDC8);
        drawString(fontRendererObj,"CUSTOM NAME",left+14,bottom-64,0xFFFFD866);
        nameField.drawTextBox();
        super.drawScreen(mx,my,partial);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}