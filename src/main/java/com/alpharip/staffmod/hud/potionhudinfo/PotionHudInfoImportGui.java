// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.potion.Potion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Clear source selection before copying a potion effect configuration. */
public final class PotionHudInfoImportGui extends AlphaGuiScreen {
    private static final int PREV=1,NEXT=2,COPY=3,BACK=4;
    private final GuiScreen parent;
    private final int targetId;
    private final List<Integer> sources=new ArrayList<Integer>();
    private final PotionHudInfoModule module=PotionHudInfoModule.getInstance();
    private int sourceIndex,panelX,panelY;

    public PotionHudInfoImportGui(GuiScreen parent,int targetId){
        this.parent=parent;this.targetId=targetId;
        for(int i=0;i<Potion.potionTypes.length;i++)if(Potion.potionTypes[i]!=null&&i!=targetId)sources.add(Integer.valueOf(i));
    }
    private int sourceId(){return sources.isEmpty()?-1:sources.get(Math.max(0,Math.min(sourceIndex,sources.size()-1))).intValue();}

    @Override public void initGui(){
        buttonList.clear();
        panelX=(width-560)/2;panelY=(height-340)/2;
        buttonList.add(new AlphaButton(PREV,panelX+34,panelY+223,100,28,"PREVIOUS",false,sourceIndex>0));
        buttonList.add(new AlphaButton(NEXT,panelX+426,panelY+223,100,28,"NEXT",false,sourceIndex<sources.size()-1));
        buttonList.add(new AlphaButton(COPY,panelX+290,panelY+278,236,32,"COPY SETTINGS",true));
        buttonList.add(new AlphaButton(BACK,panelX+34,panelY+278,100,32,"BACK"));
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b==null)return;
        if(b.id==BACK){mc.displayGuiScreen(parent);return;}
        if(b.id==PREV){sourceIndex=Math.max(0,sourceIndex-1);initGui();return;}
        if(b.id==NEXT){sourceIndex=Math.min(sources.size()-1,sourceIndex+1);initGui();return;}
        if(b.id==COPY&&sourceId()>=0){
            PotionHudInfoEffectSetting target=module.getEffect(targetId),source=module.getEffect(sourceId());
            if(target!=null&&source!=null)target.copyFrom(source);
            mc.displayGuiScreen(parent);
        }
    }

    @Override public void drawScreen(int mx,int my,float partial){
        drawRect(0,0,width,height,0x9906080D);
        drawRect(panelX,panelY,panelX+560,panelY+340,0xF00D1017);
        drawRect(panelX,panelY,panelX+560,panelY+2,0xFFFFD866);
        drawString(fontRendererObj,"ALPHA CLIENT",panelX+18,panelY+14,0xFFFFD866);
        drawString(fontRendererObj,"COPY EFFECT SETTINGS",panelX+18,panelY+32,0xFFF3F4F6);
        drawString(fontRendererObj,"Choose another effect as the source for these settings.",panelX+18,panelY+50,0xFF9DA4B1);
        drawRect(panelX+18,panelY+70,panelX+542,panelY+71,0x554B515D);
        drawRect(panelX+24,panelY+92,panelX+536,panelY+199,0x99191D26);
        drawString(fontRendererObj,"COPY TO",panelX+44,panelY+110,0xFFFFD866);
        drawString(fontRendererObj,PotionHudInfoModule.getPotionName(targetId),panelX+44,panelY+132,0xFFF3F4F6);
        drawString(fontRendererObj,"COPY FROM",panelX+44,panelY+157,0xFFFFD866);
        drawString(fontRendererObj,sourceId()<0?"No source effects available":PotionHudInfoModule.getPotionName(sourceId()),panelX+44,panelY+179,0xFFF3F4F6);
        drawCenteredString(fontRendererObj,sources.isEmpty()?"No other effects available":(sourceIndex+1)+" / "+sources.size(),
                panelX+280,panelY+244,0xFF9DA4B1);
        drawString(fontRendererObj,"The target effect's settings will be replaced.",panelX+34,panelY+258,0xFFB8BDC8);
        super.drawScreen(mx,my,partial);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
