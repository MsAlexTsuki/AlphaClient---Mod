// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.io.IOException;

/** Spacious per-effect editor with distinct color, typography and warning sections. */
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
    private int panelX,panelY;

    public PotionHudInfoEffectGui(GuiScreen parent,int effectId){this.parent=parent;this.effectId=effectId;}

    @Override public void initGui(){
        setting=module.getEffect(effectId);
        buttonList.clear();
        panelX=(width-620)/2;panelY=(height-450)/2;
        int left=panelX+18,right=panelX+316,top=panelY+88;
        addColorButtons(left+12,top+48,false);
        addColorButtons(right+12,top+48,true);
        buttonList.add(new AlphaButton(CN,left+12,top+91,260,27,"CHROMA NAME     "+state(setting.isChromaName()),false,setting.isChromaName()));
        buttonList.add(new AlphaButton(CT,right+12,top+91,260,27,"CHROMA TIMER     "+state(setting.isChromaTimer()),false,setting.isChromaTimer()));
        buttonList.add(new AlphaButton(BOLD,left+12,top+188,168,27,"BOLD     "+state(setting.isBold()),false,setting.isBold()));
        buttonList.add(new AlphaButton(ITALIC,left+190,top+188,168,27,"ITALIC     "+state(setting.isItalic()),false,setting.isItalic()));
        buttonList.add(new AlphaButton(UNDER,left+368,top+188,168,27,"UNDERLINE     "+state(setting.isUnderlined()),false,setting.isUnderlined()));
        buttonList.add(new AlphaButton(FLICKER,left+12,top+234,260,27,"LOW-TIME FLICKER     "+state(setting.isFlickerWhenLow()),false,setting.isFlickerWhenLow()));
        buttonList.add(new AlphaButton(TD,right+12,top+234,42,27,"−"));
        buttonList.add(new AlphaButton(TU,right+224,top+234,42,27,"+"));
        buttonList.add(new AlphaButton(SD,right+12,top+266,42,27,"−"));
        buttonList.add(new AlphaButton(SU,right+224,top+266,42,27,"+"));
        nameField=new GuiTextField(30,fontRendererObj,panelX+30,panelY+414,390,24);
        nameField.setMaxStringLength(64);
        nameField.setText(setting.getCustomName());
        nameField.setTextColor(0xFFE7E9EE);
        nameField.setDisabledTextColour(0xFF9DA4B1);
        buttonList.add(new AlphaButton(SAVE,panelX+438,panelY+412,82,28,"SAVE",true));
        buttonList.add(new AlphaButton(BACK,panelX+528,panelY+412,74,28,"BACK"));
    }

    private void addColorButtons(int x,int y,boolean timer){
        int[] down=timer?new int[]{TRD,TGD,TBD}:new int[]{NRD,NGD,NBD};
        int[] up=timer?new int[]{TRU,TGU,TBU}:new int[]{NRU,NGU,NBU};
        String[] channel={"RED","GREEN","BLUE"};
        for(int i=0;i<3;i++){
            int cy=y+i*31;
            buttonList.add(new AlphaButton(down[i],x+2,cy,58,25,"−"));
            buttonList.add(new AlphaButton(up[i],x+202,cy,58,25,"+"));
        }
    }
    private String state(boolean b){return b?"ON":"OFF";}
    private int adjust(int color,int shift,int amount){
        int value=Math.max(0,Math.min(255,((color>>shift)&255)+amount));
        return (color&~(255<<shift))|(value<<shift);
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b==null||setting==null)return;
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
        drawRect(0,0,width,height,0x9906080D);
        drawRect(panelX,panelY,panelX+620,panelY+450,0xF00D1017);
        drawRect(panelX,panelY,panelX+620,panelY+2,0xFFFFD866);
        drawString(fontRendererObj,"ALPHA CLIENT",panelX+18,panelY+12,0xFFFFD866);
        drawString(fontRendererObj,"CUSTOMIZE EFFECT",panelX+18,panelY+29,0xFFF3F4F6);
        drawString(fontRendererObj,PotionHudInfoModule.getPotionName(effectId),panelX+18,panelY+46,0xFF9DA4B1);
        drawRect(panelX+18,panelY+65,panelX+602,panelY+66,0x554B515D);

        int lx=panelX+18,rx=panelX+316,top=panelY+78;
        drawRect(lx,top,lx+286,top+148,0x99191D26);
        drawRect(rx,top,rx+286,top+148,0x99191D26);
        drawString(fontRendererObj,"NAME COLOR",lx+12,top+12,0xFFFFD866);
        drawString(fontRendererObj,"TIMER COLOR",rx+12,top+12,0xFFFFD866);
        drawRect(lx+12,top+30,lx+272,top+43,0xFF000000|(setting.getRawNameColor()&0xFFFFFF));
        drawRect(rx+12,top+30,rx+272,top+43,0xFF000000|(setting.getRawTimerColor()&0xFFFFFF));
        drawCenteredString(fontRendererObj,hex(setting.getRawNameColor()),lx+142,top+33,contrast(setting.getRawNameColor()));
        drawCenteredString(fontRendererObj,hex(setting.getRawTimerColor()),rx+142,top+33,contrast(setting.getRawTimerColor()));
        String[] labels={"RED","GREEN","BLUE"};
        for(int i=0;i<3;i++){
            int cy=top+61+i*31;
            drawString(fontRendererObj,labels[i],lx+82,cy+8,0xFFB8BDC8);
            drawString(fontRendererObj,labels[i],rx+82,cy+8,0xFFB8BDC8);
            drawString(fontRendererObj,String.valueOf((setting.getRawNameColor()>>(16-i*8))&255),lx+150,cy+8,0xFFE6E8ED);
            drawString(fontRendererObj,String.valueOf((setting.getRawTimerColor()>>(16-i*8))&255),rx+150,cy+8,0xFFE6E8ED);
        }

        drawRect(lx,top+159,lx+584,top+225,0x99191D26);
        drawString(fontRendererObj,"TEXT STYLE",lx+12,top+169,0xFFFFD866);
        drawString(fontRendererObj,"Choose how the effect name is formatted.",lx+12,top+185,0xFF9DA4B1);
        drawRect(lx,top+230,lx+584,top+318,0x99191D26);
        drawString(fontRendererObj,"LOW-DURATION WARNING",lx+12,top+240,0xFFFFD866);
        drawString(fontRendererObj,"Alert threshold",rx+64,top+243,0xFFB8BDC8);
        drawString(fontRendererObj,setting.getFlickerStartTime()+" sec",rx+150,top+243,0xFFE6E8ED);
        drawString(fontRendererObj,"Flicker speed",rx+64,top+275,0xFFB8BDC8);
        drawString(fontRendererObj,setting.getFlickerSpeed()+" ms",rx+150,top+275,0xFFE6E8ED);
        drawString(fontRendererObj,"CUSTOM EFFECT NAME",panelX+30,panelY+400,0xFFFFD866);
        nameField.drawTextBox();
        if(nameField.getText().isEmpty())drawString(fontRendererObj,"Leave blank to use the default name",panelX+35,panelY+422,0xFF777D88);
        super.drawScreen(mx,my,partial);
    }
    private String hex(int c){return String.format(java.util.Locale.ROOT,"#%06X",c&0xFFFFFF);}
    private int contrast(int c){int r=c>>16&255,g=c>>8&255,b=c&255;return(r*299+g*587+b*114)>145000?0xFF101116:0xFFF3F4F6;}
    @Override public boolean doesGuiPauseGame(){return false;}
}
