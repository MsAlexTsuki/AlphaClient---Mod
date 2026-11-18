package com.alpharip.staffmod.RecordingStudio.ui;
import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;import com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioRuntime;import net.minecraft.client.gui.*;import java.io.IOException;
/** Standalone Recording Studio screen for manual Minecraft recordings. */
public final class RecordingStudioScreen extends GuiScreen {
 private final RecordingStudioController controller;private final RecordingStudioRuntime runtime;private boolean leaving;
 public RecordingStudioScreen(RecordingStudioController c,RecordingStudioRuntime r){if(c==null||r==null)throw new IllegalArgumentException();controller=c;runtime=r;}
 public boolean doesGuiPauseGame(){return false;}
 public void initGui(){leaving=false;buttonList.clear();int x=width/2,y=height/2-48;buttonList.add(new GuiButton(0,x-125,y,120,20,"Start Recording"));buttonList.add(new GuiButton(1,x+5,y,120,20,"Pause"));buttonList.add(new GuiButton(2,x-125,y+25,120,20,"Resume"));buttonList.add(new GuiButton(3,x+5,y+25,120,20,"Stop"));buttonList.add(new GuiButton(4,x-125,y+58,120,20,"Timeline"));buttonList.add(new GuiButton(5,x+5,y+58,120,20,"Save Project"));}
 protected void actionPerformed(GuiButton b)throws IOException{switch(b.id){case 0:controller.start();break;case 1:controller.pause();break;case 2:controller.resume();break;case 3:controller.stop();break;case 4:mc.displayGuiScreen(new TimelineEditorScreen(this,controller.getTimeline()));break;case 5:controller.save();break;default:break;}}
 protected void keyTyped(char c,int k)throws IOException{if(k==1){leaving=true;mc.displayGuiScreen(null);return;}super.keyTyped(c,k);}
 public void onGuiClosed(){runtime.shutdown();}
 public void drawScreen(int mx,int my,float pt){drawDefaultBackground();drawCenteredString(fontRendererObj,"RECORDING STUDIO",width/2,24,0xFFC83D);drawCenteredString(fontRendererObj,controller.getProject().getName(),width/2,42,0xFFFFFF);drawCenteredString(fontRendererObj,"Status: "+controller.getStatus(),width/2,height/2+58,0xBBBBBB);drawCenteredString(fontRendererObj,"Frames captured: "+controller.getCapture().getFrameCount(),width/2,height/2+72,0xBBBBBB);drawCenteredString(fontRendererObj,"Timeline duration: "+controller.getTimeline().duration()+" ms",width/2,height/2+86,0xBBBBBB);super.drawScreen(mx,my,pt);}
}