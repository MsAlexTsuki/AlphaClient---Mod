package com.alpharip.staffmod.WayPoints;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.util.List;

/** Waypoint display, sorting, favorite and profile controls. */
public final class WaypointVisualSettingsGui extends GuiScreen {
    private static final int GOLD=0xFFFFC83D;
    private static final String[] ICONS={"pin","home","star","death","diamond","chest","event"};
    private static final String[] SORTS={"distance","name","category","modified"};
    private final GuiScreen parent;
    private int left,top,panelWidth,panelHeight;

    public WaypointVisualSettingsGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui() {
        buttonList.clear();
        panelWidth=Math.min(330,width-24);
        panelHeight=Math.min(290,height-24);
        left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        int buttonWidth=panelWidth-32;
        int y=top+44;
        add(1,"Chroma: "+onOff(WaypointSettings.getBoolean("chromaEnabled",false)),y);y+=21;
        add(2,"Beam height: "+beamName(),y);y+=21;
        add(3,"Label size: "+WaypointSettings.getInt("labelSize",100,50,200)+"%",y);y+=21;
        add(4,"Icon: "+iconName(),y);y+=21;
        add(5,"Hide waypoints: "+onOff(WaypointSettings.isTemporarilyHidden()),y);y+=21;
        add(6,"Block highlight: "+onOff(WaypointSettings.getBoolean("blockHighlightEnabled",true)),y);y+=21;
        add(7,favoriteName(),y);y+=21;
        add(8,"Sort: "+sortName(WaypointSettings.getString("sortMode","distance")),y);y+=21;
        add(9,"Profile: "+WaypointSettings.getString("activeProfile","default"),y);y+=21;
        add(10,"Arrival detection: "+onOff(WaypointSettings.getBoolean("arrivalDetectionEnabled",true)),y);
        buttonList.add(new GuiButton(11,left+panelWidth-92,top+panelHeight-23,76,18,"Back"));
    }

    private void add(int id,String label,int y) {
        buttonList.add(new GuiButton(id,left+16,y,panelWidth-32,18,label));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if(button==null)return;
        WaypointManager manager=WaypointManager.getInstance();
        if(button.id==1) {
            WaypointSettings.setBoolean("chromaEnabled",!WaypointSettings.getBoolean("chromaEnabled",false));
        } else if(button.id==2) {
            int current=WaypointSettings.getInt("beamHeight",8,0,32);
            WaypointSettings.setInt("beamHeight",current==0?2:current==2?8:current==8?32:0);
        } else if(button.id==3) {
            int current=WaypointSettings.getInt("labelSize",100,50,200);
            WaypointSettings.setInt("labelSize",current==75?100:current==100?125:current==125?150:75);
        } else if(button.id==4) {
            StaffWaypoint selected=manager.getSelected();
            String current=selected==null?WaypointSettings.getString("defaultIcon","pin"):selected.getIconId();
            String next=nextIcon(current);
            if(selected==null)WaypointSettings.setString("defaultIcon",next);
            else {selected.setIconId(next);manager.saveChanges();}
        } else if(button.id==5) {
            WaypointSettings.setTemporarilyHidden(!WaypointSettings.isTemporarilyHidden());
        } else if(button.id==6) {
            WaypointSettings.setBoolean("blockHighlightEnabled",!WaypointSettings.getBoolean("blockHighlightEnabled",true));
        } else if(button.id==7) {
            StaffWaypoint selected=manager.getSelected();
            if(selected!=null){selected.setFavorite(!selected.isFavorite());manager.saveChanges();}
        } else if(button.id==8) {
            WaypointSettings.setString("sortMode",nextSort(WaypointSettings.getString("sortMode","distance")));
        } else if(button.id==9) {
            List<String> profiles=manager.getProfilesForCurrentServer();
            String current=WaypointSettings.getString("activeProfile","default");
            int index=0;
            for(int i=0;i<profiles.size();i++)if(profiles.get(i).equalsIgnoreCase(current)){index=i;break;}
            manager.setActiveProfile(profiles.get((index+1)%profiles.size()));
        } else if(button.id==10) {
            WaypointSettings.setBoolean("arrivalDetectionEnabled",!WaypointSettings.getBoolean("arrivalDetectionEnabled",true));
        } else if(button.id==11) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    private String favoriteName() {
        StaffWaypoint selected=WaypointManager.getInstance().getSelected();
        return selected==null?"Favorite: select waypoint":"Favorite: "+onOff(selected.isFavorite())+" ("+selected.getLabel()+")";
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
    private static String nextSort(String current) {
        for(int i=0;i<SORTS.length;i++)if(SORTS[i].equalsIgnoreCase(current))return SORTS[(i+1)%SORTS.length];
        return SORTS[0];
    }
    private static String sortName(String mode) {
        if("name".equalsIgnoreCase(mode))return "Name";
        if("category".equalsIgnoreCase(mode))return "Category";
        if("modified".equalsIgnoreCase(mode))return "Recently modified";
        return "Distance";
    }
    private static String onOff(boolean value){return value?"On":"Off";}

    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawRect(0,0,width,height,0x99000000);
        drawRect(left,top,left+panelWidth,top+panelHeight,0xF00B0D12);
        drawRect(left,top,left+panelWidth,top+2,GOLD);
        drawString(fontRendererObj,"WAYPOINT SETTINGS",left+16,top+11,0xFFF0F1F4);
        drawString(fontRendererObj,"Select a waypoint to change its icon or favorite.",left+16,top+26,0xFFB8BDC8);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    @Override public boolean doesGuiPauseGame(){return false;}
}
