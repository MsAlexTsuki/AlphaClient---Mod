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

/** Select another effect and copy its complete configuration. */
public final class PotionHudInfoImportGui extends AlphaGuiScreen {
    private static final int PREVIOUS=1, NEXT=2, IMPORT=3, BACK=4;
    private final GuiScreen parent;
    private final int targetId;
    private final List<Integer> sourceIds = new ArrayList<Integer>();
    private final PotionHudInfoModule module = PotionHudInfoModule.getInstance();
    private int sourceIndex;

    public PotionHudInfoImportGui(GuiScreen parent, int targetId) {
        this.parent=parent;
        this.targetId=targetId;
        for (int i=0; i<Potion.potionTypes.length; i++)
            if (Potion.potionTypes[i] != null && i != targetId) sourceIds.add(Integer.valueOf(i));
    }

    private int sourceId() {
        return sourceIds.isEmpty() ? -1 : sourceIds.get(Math.max(0,Math.min(sourceIndex,sourceIds.size()-1))).intValue();
    }

    @Override public void initGui() {
        buttonList.clear();
        int left=width/2-104, top=height/2-30;
        buttonList.add(new AlphaButton(PREVIOUS,left,top+35,42,22,"PREV",false,sourceIndex>0));
        buttonList.add(new AlphaButton(NEXT,left+160,top+35,42,22,"NEXT",false,sourceIndex<sourceIds.size()-1));
        buttonList.add(new AlphaButton(IMPORT,left,top+78,202,24,"COPY SETTINGS",true));
        buttonList.add(new AlphaButton(BACK,width/2-42,top+112,84,20,"BACK"));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (sourceIds.isEmpty()) return;
        if (button.id == PREVIOUS) sourceIndex=Math.max(0,sourceIndex-1);
        else if (button.id == NEXT) sourceIndex=Math.min(sourceIds.size()-1,sourceIndex+1);
        else if (button.id == IMPORT) {
            PotionHudInfoEffectSetting target=module.getEffect(targetId);
            PotionHudInfoEffectSetting source=module.getEffect(sourceId());
            if (target != null && source != null) target.copyFrom(source);
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0,0,width,height,0x50000000);
        int left=width/2-119, top=height/2-91, right=width/2+119, bottom=height/2+91;
        drawRect(left,top,right,bottom,0xE60A0C12);
        drawRect(left,top,right,top+1,0xFFFFD866);
        drawCenteredString(fontRendererObj,"COPY EFFECT SETTINGS",width/2,top+13,0xFFF3F4F6);
        drawCenteredString(fontRendererObj,"Settings will be copied to",width/2,top+42,0xFFB8BDC8);
        drawCenteredString(fontRendererObj,PotionHudInfoModule.getPotionName(targetId),width/2,top+55,0xFFFFD866);
        drawRect(left+12,top+75,right-12,top+76,0x554B515D);
        drawCenteredString(fontRendererObj,"COPY FROM",width/2,top+87,0xFFB8BDC8);
        drawCenteredString(fontRendererObj,sourceId()<0?"No source effects available":PotionHudInfoModule.getPotionName(sourceId()),
                width/2,top+101,0xFFF3F4F6);
        drawCenteredString(fontRendererObj,sourceIds.isEmpty()?"":(sourceIndex+1)+" / "+sourceIds.size(),
                width/2,top+120,0xFFB8BDC8);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
