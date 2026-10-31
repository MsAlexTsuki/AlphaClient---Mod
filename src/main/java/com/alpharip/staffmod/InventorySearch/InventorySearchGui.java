package com.alpharip.staffmod.InventorySearch;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
/** GUI toggle for enabling the search overlay. */
public final class InventorySearchGui extends GuiScreen {
 private final GuiScreen parent;
 public InventorySearchGui(GuiScreen parent){this.parent=parent;}
 @Override public void initGui(){buttonList.clear();buttonList.add(new GuiButton(0,width/2-100,height/2-8,200,20,"Search bar: "+(InventorySearchSettings.isEnabled()?"ON":"OFF")));buttonList.add(new GuiButton(1,width/2-100,height/2+22,200,20,"Done"));}
 @Override protected void actionPerformed(GuiButton b)throws IOException{if(b.id==0){InventorySearchSettings.setEnabled(!InventorySearchSettings.isEnabled());initGui();}else if(b.id==1)mc.displayGuiScreen(parent);}
 @Override public void drawScreen(int x,int y,float partial){drawDefaultBackground();drawCenteredString(fontRendererObj,"INVENTORY SEARCH",width/2,height/2-48,0xFFFFFF);drawCenteredString(fontRendererObj,"Filters visible items in inventories and containers.",width/2,height/2-29,0xBBBBBB);super.drawScreen(x,y,partial);}
 @Override public boolean doesGuiPauseGame(){return false;}
}