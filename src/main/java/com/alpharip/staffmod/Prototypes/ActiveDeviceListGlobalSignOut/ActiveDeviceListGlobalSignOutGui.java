package com.alpharip.staffmod.activedevicelistsignout;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Minecraft 1.8.9 preview. Actions change local prototype records only. */
public final class ActiveDeviceListGlobalSignOutGui extends GuiScreen {
    private final GuiScreen parent;
    private final ActiveDeviceService service;
    private int selectedIndex=-1, scrollOffset;
    private String message="";
    public ActiveDeviceListGlobalSignOutGui(GuiScreen parent,ActiveDeviceService service){
        if(service==null)throw new IllegalArgumentException("Device service is required.");
        this.parent=parent;this.service=service;
    }
    @Override public void initGui(){
        buttonList.clear();
        buttonList.add(new GuiButton(0,width/2-154,height-30,100,20,"Sign Out Device"));
        buttonList.add(new GuiButton(1,width/2-50,height-30,100,20,"Sign Out Others"));
        buttonList.add(new GuiButton(2,width/2+54,height-30,100,20,"Back"));
    }
    @Override protected void actionPerformed(GuiButton button)throws IOException{
        List<ActiveDeviceSession> list=service.getVisibleDevices();
        if(button.id==0&&selectedIndex>=0&&selectedIndex<list.size()){
            boolean ok=service.requestSignOut(list.get(selectedIndex).getId(),System.currentTimeMillis());
            message=ok?"Local sign-out request recorded.":"This device or session cannot be signed out here.";
        }else if(button.id==1){
            ActiveDeviceSignOutResult result=service.requestGlobalSignOut(System.currentTimeMillis());
            message=result.getMessage()+" Requested: "+result.getRequested()+", skipped: "+result.getSkipped()+".";
        }else if(button.id==2)mc.displayGuiScreen(parent);
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{
        super.mouseClicked(x,y,button);
        if(button==0&&y>=48&&y<height-38)selectedIndex=scrollOffset+(y-48)/30;
    }
    @Override public void handleMouseInput()throws IOException{
        super.handleMouseInput();
        int wheel=org.lwjgl.input.Mouse.getEventDWheel();
        int max=Math.max(0,service.getVisibleDevices().size()-Math.max(1,(height-90)/30));
        if(wheel!=0)scrollOffset=Math.max(0,Math.min(max,scrollOffset+(wheel>0?-1:1)));
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks){
        drawDefaultBackground();
        drawCenteredString(fontRendererObj,"ACTIVE DEVICES",width/2,14,0xFFFFFFFF);
        drawCenteredString(fontRendererObj,"Local preview only - no remote session is terminated",width/2,28,0xFFFFD866);
        List<ActiveDeviceSession> list=service.getVisibleDevices();
        int rows=Math.max(0,(height-90)/30);
        for(int i=0;i<rows&&scrollOffset+i<list.size();i++){
            int index=scrollOffset+i;ActiveDeviceSession s=list.get(index);int y=48+i*30;
            drawRect(12,y,width-12,y+27,index==selectedIndex?0xAA665522:0x88303030);
            String label=s.getDeviceName()+" | "+s.getPlatform()+" | "+s.getStatus().name();
            drawString(fontRendererObj,fontRendererObj.trimStringToWidth(label,width-36),18,y+3,0xFFFFFFFF);
            String lastSeen=new SimpleDateFormat("dd-MM-yyyy HH:mm",Locale.ENGLISH).format(new Date(s.getLastSeenAtMillis()));
            drawString(fontRendererObj,"Last seen: "+lastSeen+(s.isCurrentDevice()?" | CURRENT DEVICE":""),18,y+15,0xFFB8BDC8);
        }
        if(list.isEmpty())drawCenteredString(fontRendererObj,"No device records supplied",width/2,height/2,0xFFB8BDC8);
        drawString(fontRendererObj,fontRendererObj.trimStringToWidth(message,width-24),12,height-43,0xFFFFD866);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
