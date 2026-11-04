package com.alpharip.staffmod.waypoints;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;

/** General waypoint display and arrival options. */
public final class WaypointVisualSettingsGui extends AlphaGuiScreen {
    private static final String[] ICONS={"pin","home","star","death","diamond","chest","event"};
    private final GuiScreen parent;
    private int left,top,panelWidth,panelHeight;

    public WaypointVisualSettingsGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui() {
        buttonList.clear();
        panelWidth=Math.min(330,width-24);
        panelHeight=Math.min(244,height-24);
        left=(width-panelWidth)/2;
        top=(height-panelHeight)/2;
        int y=top+43;
        add(1,"Label Size: "+WaypointSettings.getInt("labelSize",100,50,200)+"%",y);y+=24;
        add(2,"Icon: "+iconName(),y);y+=24;
        add(3,"Ground Circle: "+onOff(WaypointSettings.getBoolean("blockHighlightEnabled",true)),y);y+=24;
        add(4,"Arrival Detection: "+onOff(WaypointSettings.getBoolean("arrivalDetectionEnabled",true)),y);y+=24;
        add(5,"Max Waypoint Render Distance: "+onOff(WaypointSettings.getBoolean("maxWaypointRenderDistance",true)),y);y+=24;
        add(6,"Reset Options",y);
        buttonList.add(new AlphaButton(7,left+panelWidth-92,top+panelHeight-25,76,18,"Back"));
    }

    private void add(int id,String label,int y) {
        buttonList.add(new AlphaButton(id,left+16,y,panelWidth-32,19,label));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if(button==null)return;
        WaypointManager manager=WaypointManager.getInstance();
        if(button.id==1) {
            int current=WaypointSettings.getInt("labelSize",100,50,200);
            WaypointSettings.setInt("labelSize",current==75?100:current==100?125:current==125?150:75);
        } else if(button.id==2) {
            StaffWaypoint selected=manager.getSelected();
            String current=selected==null?WaypointSettings.getString("defaultIcon","pin"):selected.getIconId();
            String next=nextIcon(current);
            if(selected==null)WaypointSettings.setString("defaultIcon",next);
            else {selected.setIconId(next);manager.saveChanges();}
        } else if(button.id==3) {
            WaypointSettings.setBoolean("blockHighlightEnabled",
                    !WaypointSettings.getBoolean("blockHighlightEnabled",true));
        } else if(button.id==4) {
            WaypointSettings.setBoolean("arrivalDetectionEnabled",
                    !WaypointSettings.getBoolean("arrivalDetectionEnabled",true));
        } else if(button.id==5) {
            WaypointSettings.setBoolean("maxWaypointRenderDistance",
                    !WaypointSettings.getBoolean("maxWaypointRenderDistance",true));
        } else if(button.id==6) {
            WaypointSettings.resetOptions();
        } else if(button.id==7) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    private String iconName() {
        StaffWaypoint selected=WaypointManager.getInstance().getSelected();
        return selected==null?WaypointSettings.getString("defaultIcon","pin")+" (default)":selected.getIconId();
    }

    private static String nextIcon(String current) {
        for(int i=0;i<ICONS.length;i++)
            if(ICONS[i].equalsIgnoreCase(current))return ICONS[(i+1)%ICONS.length];
        return ICONS[0];
    }

    private static String onOff(boolean value){return value?"On":"Off";}

    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawRect(0,0,width,height,0x18000000);
        drawRect(left,top,left+panelWidth,top+panelHeight,0xD90A0C11);
        drawRect(left,top,left+panelWidth,top+2,0xFFFFD866);
        drawString(fontRendererObj,"WAYPOINT OPTIONS",left+16,top+11,0xFFF0F1F4);
        drawString(fontRendererObj,"General display and arrival settings",left+16,top+26,0xFFB8BDC8);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    @Override public boolean doesGuiPauseGame(){return false;}
}
