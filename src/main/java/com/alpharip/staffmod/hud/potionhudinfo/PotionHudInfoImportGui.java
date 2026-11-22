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

/** Compact source selection for copying potion effect settings. */
public final class PotionHudInfoImportGui extends AlphaGuiScreen {
    private static final int PREV=1,NEXT=2,COPY=3,BACK=4;
    private final GuiScreen parent;
    private final int targetId;
    private final List<Integer> sources=new ArrayList<Integer>();
    private final PotionHudInfoModule module=PotionHudInfoModule.getInstance();
    private int sourceIndex,left,top,right,bottom;

    public PotionHudInfoImportGui(GuiScreen parent,int targetId){
        this.parent=parent;this.targetId=targetId;
        for(int i=0;i<Potion.potionTypes.length;i++)if(Potion.potionTypes[i]!=null&&i!=targetId)sources.add(Integer.valueOf(i));
    }
    private int sourceId(){return sources.isEmpty()?-1:sources.get(Math.max(0,Math.min(sourceIndex,sources.size()-1))).intValue();}

    @Override public void initGui(){
        buttonList.clear();
        left=width/2-175;right=left+350;top=Math.max(10,height/2-112);bottom=Math.min(height-10,top+224);
        buttonList.add(new AlphaButton(PREV,left+18,top+155,64,18,"PREV",false,sourceIndex>0));
        buttonList.add(new AlphaButton(NEXT,right-82,top+155,64,18,"NEXT",false,sourceIndex<sources.size()-1));
        buttonList.add(new AlphaButton(COPY,right-82,bottom-25,64,18,"COPY",true));
        buttonList.add(new AlphaButton(BACK,left+18,bottom-25,64,18,"BACK"));
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
        drawRect(0,0,width,height,0x30000000);
        drawRect(left,top,right,bottom,0xE00A0C11);
        drawRect(left,top,right,top+2,0xFFFFD866);
        drawCenteredString(fontRendererObj,"COPY EFFECT SETTINGS",width/2,top+15,0xFFF3F4F6);
        drawString(fontRendererObj,"TARGET",left+20,top+49,0xFFFFD866);
        drawString(fontRendererObj,PotionHudInfoModule.getPotionName(targetId),left+20,top+63,0xFFF3F4F6);
        drawString(fontRendererObj,"SOURCE",left+20,top+91,0xFFFFD866);
        drawString(fontRendererObj,sourceId()<0?"No source effects available":PotionHudInfoModule.getPotionName(sourceId()),left+20,top+105,0xFFF3F4F6);
        drawCenteredString(fontRendererObj,sources.isEmpty()?"No sources":(sourceIndex+1)+" / "+sources.size(),width/2,top+160,0xFF9DA4B1);
        drawString(fontRendererObj,"Target settings will be replaced.",left+20,top+133,0xFFB8BDC8);
        super.drawScreen(mx,my,partial);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}