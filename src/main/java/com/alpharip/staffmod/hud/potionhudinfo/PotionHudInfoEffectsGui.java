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

/** Paginated effect browser with a dedicated details and actions panel. */
public final class PotionHudInfoEffectsGui extends AlphaGuiScreen {
    private static final int FIRST=100,PREV=10,NEXT=11,TOGGLE=12,EDIT=13,IMPORT=14,BACK=15;
    private static final int PAGE_SIZE=6;
    private final GuiScreen parent;
    private final List<Integer> ids=new ArrayList<Integer>();
    private final PotionHudInfoModule settings=PotionHudInfoModule.getInstance();
    private int index,page,panelX,panelY;

    public PotionHudInfoEffectsGui(GuiScreen parent){
        this.parent=parent;
        for(int i=0;i<Potion.potionTypes.length;i++)if(Potion.potionTypes[i]!=null)ids.add(Integer.valueOf(i));
    }
    private int currentId(){return ids.isEmpty()?-1:ids.get(Math.max(0,Math.min(index,ids.size()-1))).intValue();}
    private PotionHudInfoEffectSetting current(){return currentId()<0?null:settings.getEffect(currentId());}
    private int pageCount(){return Math.max(1,(ids.size()+PAGE_SIZE-1)/PAGE_SIZE);}

    @Override public void initGui(){
        buttonList.clear();
        page=Math.max(0,Math.min(page,pageCount()-1));
        panelX=(width-620)/2;panelY=(height-410)/2;
        int listX=panelX+18,listY=panelY+108;
        for(int slot=0;slot<PAGE_SIZE&&page*PAGE_SIZE+slot<ids.size();slot++){
            int item=page*PAGE_SIZE+slot,id=ids.get(item).intValue();
            PotionHudInfoEffectSetting s=settings.getEffect(id);
            String name=PotionHudInfoModule.getPotionName(id);
            if(name.length()>27)name=name.substring(0,26)+"…";
            buttonList.add(new AlphaButton(FIRST+slot,listX,listY+slot*35,270,29,
                    name+(s!=null&&s.isEnabled()?"     ENABLED":"     DISABLED"),false,item==index));
        }
        buttonList.add(new AlphaButton(PREV,panelX+18,panelY+337,78,25,"PREVIOUS",false,page>0));
        buttonList.add(new AlphaButton(NEXT,panelX+210,panelY+337,78,25,"NEXT",false,page<pageCount()-1));
        PotionHudInfoEffectSetting selected=current();
        buttonList.add(new AlphaButton(TOGGLE,panelX+330,panelY+145,270,30,
                selected!=null&&selected.isEnabled()?"DISABLE THIS EFFECT":"ENABLE THIS EFFECT",false,selected!=null&&selected.isEnabled()));
        buttonList.add(new AlphaButton(EDIT,panelX+330,panelY+188,270,32,"CUSTOMIZE APPEARANCE",true));
        buttonList.add(new AlphaButton(IMPORT,panelX+330,panelY+232,270,30,"COPY SETTINGS FROM…"));
        buttonList.add(new AlphaButton(BACK,panelX+492,panelY+337,108,25,"BACK"));
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
        drawRect(0,0,width,height,0x9906080D);
        drawRect(panelX,panelY,panelX+620,panelY+410,0xF00D1017);
        drawRect(panelX,panelY,panelX+620,panelY+2,0xFFFFD866);
        drawString(fontRendererObj,"ALPHA CLIENT",panelX+18,panelY+13,0xFFFFD866);
        drawString(fontRendererObj,"POTION EFFECTS",panelX+18,panelY+30,0xFFF3F4F6);
        drawString(fontRendererObj,"Choose an effect to enable or customize.",panelX+18,panelY+47,0xFF9DA4B1);
        drawRect(panelX+18,panelY+64,panelX+602,panelY+65,0x554B515D);
        int lx=panelX+18,ly=panelY+76,rx=panelX+312,ry=panelY+76;
        drawRect(lx,ly,lx+288,panelY+326,0x99191D26);
        drawRect(rx,ry,rx+290,panelY+326,0x99191D26);
        drawString(fontRendererObj,"ALL EFFECTS",lx+12,ly+10,0xFFFFD866);
        drawString(fontRendererObj,"SELECTED EFFECT",rx+14,ry+10,0xFFFFD866);
        if(currentId()>=0){
            PotionHudInfoEffectSetting s=current();
            drawCenteredString(fontRendererObj,PotionHudInfoModule.getPotionName(currentId()),rx+145,ry+55,0xFFF3F4F6);
            drawCenteredString(fontRendererObj,s!=null&&s.isEnabled()?"Currently shown in your HUD":"Currently hidden from your HUD",
                    rx+145,ry+76,s!=null&&s.isEnabled()?0xFF9CE2AC:0xFFB8BDC8);
            drawString(fontRendererObj,"Appearance",rx+16,ry+119,0xFFE6E8ED);
            drawString(fontRendererObj,"Set custom name, colors and text style.",rx+16,ry+134,0xFF9DA4B1);
            drawString(fontRendererObj,"Import",rx+16,ry+185,0xFFE6E8ED);
            drawString(fontRendererObj,"Copy another effect's complete settings.",rx+16,ry+200,0xFF9DA4B1);
        }else drawCenteredString(fontRendererObj,"No potion effects found",rx+145,ry+80,0xFFB8BDC8);
        drawString(fontRendererObj,"Page "+(page+1)+" of "+pageCount(),panelX+116,panelY+345,0xFF9DA4B1);
        super.drawScreen(mx,my,partial);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
