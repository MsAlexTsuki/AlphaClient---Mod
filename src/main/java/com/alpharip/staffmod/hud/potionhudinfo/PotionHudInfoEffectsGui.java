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

/** Compact paginated browser for Potion HUD effects. */
public final class PotionHudInfoEffectsGui extends AlphaGuiScreen {
    private static final int FIRST=100,PREV=10,NEXT=11,TOGGLE=12,EDIT=13,IMPORT=14,BACK=15;
    private static final int PAGE_SIZE=7;
    private final GuiScreen parent;
    private final List<Integer> ids=new ArrayList<Integer>();
    private final PotionHudInfoModule settings=PotionHudInfoModule.getInstance();
    private int index,page,left,top,right,bottom;

    public PotionHudInfoEffectsGui(GuiScreen parent){
        this.parent=parent;
        for(int i=0;i<Potion.potionTypes.length;i++)if(Potion.potionTypes[i]!=null)ids.add(Integer.valueOf(i));
    }
    private int currentId(){return ids.isEmpty()?-1:ids.get(Math.max(0,Math.min(index,ids.size()-1))).intValue();}
    private PotionHudInfoEffectSetting current(){return currentId()<0?null:settings.getEffect(currentId());}
    private int pageCount(){return Math.max(1,(ids.size()+PAGE_SIZE-1)/PAGE_SIZE);}

    @Override public void initGui(){
        buttonList.clear();page=Math.max(0,Math.min(page,pageCount()-1));
        left=width/2-225;right=left+450;top=Math.max(8,height/2-166);bottom=Math.min(height-8,top+332);
        int listX=left+12,listY=top+48;
        for(int slot=0;slot<PAGE_SIZE&&page*PAGE_SIZE+slot<ids.size();slot++){
            int item=page*PAGE_SIZE+slot,id=ids.get(item).intValue();
            PotionHudInfoEffectSetting s=settings.getEffect(id);
            String name=PotionHudInfoModule.getPotionName(id);
            if(name.length()>20)name=name.substring(0,19)+"…";
            buttonList.add(new AlphaButton(FIRST+slot,listX,listY+slot*23,198,19,
                    name+(s!=null&&s.isEnabled()?"  ON":"  OFF"),false,item==index));
        }
        buttonList.add(new AlphaButton(PREV,left+12,bottom-25,70,18,"PREV",false,page>0));
        buttonList.add(new AlphaButton(NEXT,left+140,bottom-25,70,18,"NEXT",false,page<pageCount()-1));
        PotionHudInfoEffectSetting selected=current();
        buttonList.add(new AlphaButton(TOGGLE,right-210,top+125,198,20,
                selected!=null&&selected.isEnabled()?"DISABLE EFFECT":"ENABLE EFFECT",false,selected!=null&&selected.isEnabled()));
        buttonList.add(new AlphaButton(EDIT,right-210,top+153,198,20,"CUSTOMIZE",true));
        buttonList.add(new AlphaButton(IMPORT,right-210,top+181,198,20,"COPY SETTINGS"));
        buttonList.add(new AlphaButton(BACK,right-82,bottom-25,70,18,"BACK"));
    }

    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b==null)return;
        if(b.id==BACK){mc.displayGuiScreen(parent);return;}
        if(b.id==PREV){page=Math.max(0,page-1);index=page*PAGE_SIZE;initGui();return;}
        if(b.id==NEXT){page=Math.min(pageCount()-1,page+1);index=page*PAGE_SIZE;initGui();return;}
        if(b.id>=FIRST&&b.id<FIRST+PAGE_SIZE){
            int selected=page*PAGE_SIZE+b.id-FIRST;
            if(selected<ids.size())index=selected;
            initGui();return;
        }
        if(currentId()<0)return;
        if(b.id==TOGGLE&&current()!=null){current().setEnabled(!current().isEnabled());initGui();}
        else if(b.id==EDIT)mc.displayGuiScreen(new PotionHudInfoEffectGui(this,currentId()));
        else if(b.id==IMPORT)mc.displayGuiScreen(new PotionHudInfoImportGui(this,currentId()));
    }

    @Override public void drawScreen(int mx,int my,float partial){
        drawRect(0,0,width,height,0x30000000);
        drawRect(left,top,right,bottom,0xE00A0C11);
        drawRect(left,top,right,top+2,0xFFFFD866);
        drawCenteredString(fontRendererObj,"POTION EFFECTS",width/2,top+14,0xFFF3F4F6);
        drawString(fontRendererObj,"EFFECT LIST",left+14,top+35,0xFFFFD866);
        drawString(fontRendererObj,"SELECTED",left+236,top+48,0xFFFFD866);
        if(currentId()>=0){
            PotionHudInfoEffectSetting s=current();
            drawString(fontRendererObj,PotionHudInfoModule.getPotionName(currentId()),left+236,top+72,0xFFF3F4F6);
            drawString(fontRendererObj,s!=null&&s.isEnabled()?"Visible in HUD":"Hidden from HUD",left+236,top+91,s!=null&&s.isEnabled()?0xFF9CE2AC:0xFFB8BDC8);
        }else drawString(fontRendererObj,"No effects found",left+236,top+72,0xFFB8BDC8);
        drawCenteredString(fontRendererObj,(page+1)+" / "+pageCount(),left+111,bottom-44,0xFF9DA4B1);
        super.drawScreen(mx,my,partial);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}