package com.alpharip.staffmod.founderfun;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
public final class PixelGardenQuestRemoveConfirmGUI extends GuiScreen {
 private static final int YES=1, NO=2;
 private static final long DELAY=3500L;
 private final GuiScreen parent;
 private final Runnable onConfirm;
 private final long openedAt=System.currentTimeMillis();
 public PixelGardenQuestRemoveConfirmGUI(GuiScreen parent,Runnable onConfirm){this.parent=parent;this.onConfirm=onConfirm;}
 @Override public void initGui(){buttonList.clear();buttonList.add(new AlphaButton(YES,width/2-110,height/2+28,100,24,"Yes",false));buttonList.add(new AlphaButton(NO,width/2+10,height/2+28,100,24,"No",true));}
 @Override protected void actionPerformed(GuiButton button)throws IOException{if(button==null||mc==null)return;if(button.id==NO)mc.displayGuiScreen(parent);else if(button.id==YES&&System.currentTimeMillis()-openedAt>=DELAY){if(onConfirm!=null)onConfirm.run();mc.displayGuiScreen(parent);}}
 @Override public void drawScreen(int mouseX,int mouseY,float partialTicks){drawDefaultBackground();drawRect(width/2-170,height/2-48,width/2+170,height/2+70,0xF010141C);drawRect(width/2-170,height/2-48,width/2+170,height/2-46,0xFFFFC83D);drawCenteredString(fontRendererObj,"REMOVE QUEST?",width/2,height/2-30,0xFFFFC83D);drawCenteredString(fontRendererObj,"Running away from a Quest? But you will lose rewards!",width/2,height/2-8,0xFFFFFFFF);long remaining=Math.max(0L,DELAY-(System.currentTimeMillis()-openedAt));drawCenteredString(fontRendererObj,remaining>0L?"Yes becomes available in "+String.format(java.util.Locale.ROOT,"%.1f",remaining/1000.0D)+"s":"Confirm quest removal",width/2,height/2+12,0xFFB7C4D5);for(Object item:buttonList){GuiButton b=(GuiButton)item;if(b.id==YES)b.enabled=remaining==0L;}super.drawScreen(mouseX,mouseY,partialTicks);}
}