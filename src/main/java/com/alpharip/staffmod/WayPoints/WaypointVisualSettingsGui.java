package com.alpharip.staffmod.WayPoints;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Visual options for waypoint markers. */
public final class WaypointVisualSettingsGui extends GuiScreen {
    private static final int GOLD=0xFFFFC83D;
    private static final String[] ICONS={"pin","home","star","death","diamond","chest","event"};
    private final GuiScreen parent;
    private int left,top,panelWidth,panelHeight;

    public WaypointVisualSettingsGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui() {
        buttonList.clear();
        panelWidth=Math.min(330,width-24);
        panelHeight=Math.min(270,height-24);
        left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        int buttonWidth=panelWidth-32;
        int y=top+44;
        buttonList.add(new GuiButton(1,left+16,y,buttonWidth,20,"Chroma: "+onOff(WaypointSettings.getBoolean("chromaEnabled",false))));y+=25;
        buttonList.add(new GuiButton(2,left+16,y,buttonWidth,20,"Beam height: "+beamName()));y+=25;
        buttonList.add(new GuiButton(3,left+16,y,buttonWidth,20,"Label size: "+WaypointSettings.getInt("labelSize",100,50,200)+"%"));y+=25;
        buttonList.add(new GuiButton(4,left+16,y,buttonWidth,20,"Icon: "+iconName()));y+=25;
        buttonList.add(new GuiButton(5,left+16,y,buttonWidth,20,"Hide waypoints: "+onOff(WaypointSettings.isTemporarilyHidden())));y+=25;
        buttonList.add(new GuiButton(6,left+16,y,buttonWidth,20,"Block highlight: "+onOff(WaypointSettings.getBoolean("blockHighlightEnabled",true))));
        buttonList.add(new GuiButton(7,left+panelWidth-92,top+panelHeight-28,76,20,"Back"));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if(button==null)return;
        if(button.id==1) {
            WaypointSettings.setBoolean("chromaEnabled",!WaypointSettings.getBoolean("chromaEnabled",false));
        } else if(button.id==2) {
            int current=WaypointSettings.getInt("beamHeight",8,0,32);
            int next=current==0?2:current==2?8:current==8?32:0;
            WaypointSettings.setInt("beamHeight",next);
        } else if(button.id==3) {
            int current=WaypointSettings.getInt("labelSize",100,50,200);
            int next=current==75?100:current==100?125:current==125?150:75;
            WaypointSettings.setInt("labelSize",next);
        } else if(button.id==4) {
            StaffWaypoint selected=WaypointManager.getInstance().getSelected();
            String current=selected==null?WaypointSettings.getString("defaultIcon","pin"):selected.getIconId();
            String next=nextIcon(current);
            if(selected==null)WaypointSettings.setString("defaultIcon",next);
            else {
                selected.setIconId(next);
                WaypointManager.getInstance().saveChanges();
            }
        } else if(button.id==5) {
            WaypointSettings.setTemporarilyHidden(!WaypointSettings.isTemporarilyHidden());
        } else if(button.id==6) {
            WaypointSettings.setBoolean("blockHighlightEnabled",!WaypointSettings.getBoolean("blockHighlightEnabled",true));
        } else if(button.id==7) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    private String beamName() {
        int height=WaypointSettings.getInt("beamHeight",8,0,32);
        return height==0?"Off":height+" blocks";
    }
    private String iconName() {
        StaffWaypoint selected=WaypointManager.getInstance().getSelected();
        return selected==null?WaypointSettings.getString("defaultIcon","pin")+" (default)":selected.getIconId();
    }
    private static String nextIcon(String current) {
        for(int i=0;i<ICONS.length;i++)if(ICONS[i].equalsIgnoreCase(current))return ICONS[(i+1)%ICONS.length];
        return ICONS[0];
    }
    private static String onOff(boolean value){return value?"On":"Off";}

    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawRect(0,0,width,height,0x99000000);
        drawRect(left,top,left+panelWidth,top+panelHeight,0xF00B0D12);
        drawRect(left,top,left+panelWidth,top+2,GOLD);
        drawString(fontRendererObj,"WAYPOINT DISPLAY",left+16,top+14,0xFFF0F1F4);
        drawString(fontRendererObj,"Icon changes the selected waypoint, or the default.",left+16,top+30,0xFFB8BDC8);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    @Override public boolean doesGuiPauseGame(){return false;}
}
