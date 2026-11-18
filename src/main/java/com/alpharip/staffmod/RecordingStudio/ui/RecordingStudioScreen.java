package com.alpharip.staffmod.RecordingStudio.ui;
import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;import net.minecraft.client.gui.*;import java.io.IOException;
/** Standalone Recording Studio screen for manual Minecraft recordings. */
public final class RecordingStudioScreen extends GuiScreen {
 private final RecordingStudioController controller;private boolean leaving;
 public RecordingStudioScreen(RecordingStudioController c){if(c==null)throw new IllegalArgumentException("controller");controller=c;}
 public boolean doesGuiPauseGame(){return false;}
 public void initGui(){leaving=false;buttonList.clear();int x=width/2,y=height/2-48;buttonList.add(new GuiButton(0,x-125,y,120,20,"Start Recording"));buttonList.add(new GuiButton(1,x+5,y,120,20,"Pause"));buttonList.add(new GuiButton(2,x-125,y+25,120,20,"Resume"));buttonList.add(new GuiButton(3,x+5,y+25,120,20,"Stop"));buttonList.add(new GuiButton(4,x-60,y+58,120,20,"Save Project"));}
 protected void actionPerformed(GuiButton b)throws IOException{switch(b.id){case 0:controller.start();break;case 1:controller.pause();break;case 2:controller.resume();break;case 3:controller.stop();break;case 4:controller.save();break;default:break;}}
 protected void keyTyped(char c,int k)throws IOException{if(k==1){leaving=true;mc.displayGuiScreen(null);return;}super.keyTyped(c,k);}
 public void onGuiClosed(){if(!leaving)controller.shutdown();}
 public void drawScreen(int mx,int my,float pt){drawDefaultBackground();drawCenteredString(fontRendererObj,"RECORDING STUDIO",width/2,24,0xFFC83D);drawCenteredString(fontRendererObj,controller.getProject().getName(),width/2,42,0xFFFFFF);drawCenteredString(fontRendererObj,"Status: "+controller.getStatus(),width/2,height/2+58,0xBBBBBB);drawCenteredString(fontRendererObj,"Frames captured: "+controller.getCapture().getFrameCount(),width/2,height/2+72,0xBBBBBB);super.drawScreen(mx,my,pt);}
}