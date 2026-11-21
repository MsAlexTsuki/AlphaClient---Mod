// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.potion.Potion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Select, enable, disable, and edit individual potion effects. */
public final class PotionHudInfoEffectsGui extends AlphaGuiScreen {
    private static final int PREV=1, NEXT=2, TOGGLE=3, EDIT=4, IMPORT=5, BACK=6;
    private final GuiScreen parent;
    private final List<Integer> ids = new ArrayList<Integer>();
    private int index;
    private final PotionHudInfoModule settings = PotionHudInfoModule.getInstance();

    public PotionHudInfoEffectsGui(GuiScreen parent) {
        this.parent = parent;
        Potion[] potions = Potion.potionTypes;
        for (int i=0;i<potions.length;i++) if (potions[i] != null) ids.add(Integer.valueOf(i));
    }
    private int currentId() { return ids.isEmpty() ? -1 : ids.get(Math.max(0, Math.min(index, ids.size()-1))).intValue(); }
    private PotionHudInfoEffectSetting current() { return settings.getEffect(currentId()); }
    private String name(int id) { return PotionHudInfoModule.getPotionName(id); }

    @Override public void initGui() {
        buttonList.clear();
        PotionHudInfoEffectSetting value = current();
        int x=width/2-115, y=height/2-45;
        buttonList.add(new GuiButton(PREV,x,y,35,20,"<"));
        buttonList.add(new GuiButton(NEXT,x+195,y,35,20,">"));
        buttonList.add(new GuiButton(TOGGLE,x,y+30,230,20,value == null ? "NO EFFECT" : "EFFECT: "+(value.isEnabled()?"ON":"OFF")));
        buttonList.add(new GuiButton(EDIT,x,y+54,230,20,"EDIT SELECTED EFFECT"));
        buttonList.add(new GuiButton(IMPORT,x,y+78,230,20,"IMPORT SETTINGS FROM EFFECT"));
        buttonList.add(new GuiButton(BACK,width/2-55,y+108,110,20,"BACK"));
    }
    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if(button==null)return;
        if(button.id==BACK){mc.displayGuiScreen(parent);return;}
        if(ids.isEmpty())return;
        if(button.id==PREV){index=(index+ids.size()-1)%ids.size();}
        else if(button.id==NEXT){index=(index+1)%ids.size();}
        else if(button.id==TOGGLE && current()!=null){current().setEnabled(!current().isEnabled());}
        else if(button.id==EDIT){mc.displayGuiScreen(new PotionHudInfoEffectGui(this,currentId()));return;}
        else if(button.id==IMPORT){mc.displayGuiScreen(new PotionHudInfoImportGui(this,currentId()));return;}
        initGui();
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks){
        drawRect(0,0,width,height,0x9906090F);
        drawCenteredString(fontRendererObj,"POTION EFFECTS",width/2,height/2-85,0xFFFFFFFF);
        drawCenteredString(fontRendererObj,ids.isEmpty()?"No potion effects found":name(currentId()),width/2,height/2-65,0xFFFFD866);
        drawCenteredString(fontRendererObj,(index+1)+" / "+ids.size(),width/2,height/2-52,0xFFB8BDC8);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
