// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.potion.Potion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Select a source effect and import its complete appearance configuration. */
public final class PotionHudInfoImportGui extends AlphaGuiScreen {
    private static final int PREVIOUS=1, NEXT=2, IMPORT=3, BACK=4;
    private final GuiScreen parent;
    private final int targetId;
    private final List<Integer> sourceIds = new ArrayList<Integer>();
    private int sourceIndex;
    private final PotionHudInfoModule module = PotionHudInfoModule.getInstance();

    public PotionHudInfoImportGui(GuiScreen parent,int targetId){
        this.parent=parent;
        this.targetId=targetId;
        for(int i=0;i<Potion.potionTypes.length;i++) if(Potion.potionTypes[i]!=null && i!=targetId) sourceIds.add(Integer.valueOf(i));
    }
    private int sourceId(){return sourceIds.isEmpty()?-1:sourceIds.get(Math.max(0,Math.min(sourceIndex,sourceIds.size()-1))).intValue();}
    @Override public void initGui(){
        buttonList.clear();
        int x=width/2-110,y=height/2-24;
        buttonList.add(new GuiButton(PREVIOUS,x,y,40,20,"<"));
        buttonList.add(new GuiButton(NEXT,x+180,y,40,20,">"));
        buttonList.add(new GuiButton(IMPORT,x,y+32,220,20,"IMPORT SELECTED SETTINGS"));
        buttonList.add(new GuiButton(BACK,width/2-55,y+60,110,20,"BACK"));
    }
    @Override protected void actionPerformed(GuiButton button)throws IOException{
        if(button==null)return;
        if(button.id==BACK){mc.displayGuiScreen(parent);return;}
        if(sourceIds.isEmpty())return;
        if(button.id==PREVIOUS)sourceIndex=(sourceIndex+sourceIds.size()-1)%sourceIds.size();
        else if(button.id==NEXT)sourceIndex=(sourceIndex+1)%sourceIds.size();
        else if(button.id==IMPORT){
            PotionHudInfoEffectSetting target=module.getEffect(targetId);
            PotionHudInfoEffectSetting source=module.getEffect(sourceId());
            if(target!=null&&source!=null)target.copyFrom(source);
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks){
        drawRect(0,0,width,height,0x9906090F);
        drawCenteredString(fontRendererObj,"IMPORT EFFECT SETTINGS",width/2,height/2-55,0xFFFFFFFF);
        drawCenteredString(fontRendererObj,"Target: "+PotionHudInfoModule.getPotionName(targetId),width/2,height/2-36,0xFFB8BDC8);
        drawCenteredString(fontRendererObj,sourceId()<0?"No source effects available":PotionHudInfoModule.getPotionName(sourceId()),width/2,height/2-20,0xFFFFD866);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
