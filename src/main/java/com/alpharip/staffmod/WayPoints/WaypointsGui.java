package com.alpharip.staffmod.WayPoints;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Mouse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/** Compact Alpha-styled local waypoint manager screen. */
public final class WaypointsGui extends GuiScreen {
    private static final int GOLD=0xFFFFC83D;
    private static final int[] WAYPOINT_COLORS={
            0xFFFF5555,0xFFFFAA00,0xFFFFFF55,0xFF55FF55,0xFF55FFFF,
            0xFF5555FF,0xFFAA55FF,0xFFFF55FF,0xFFFFFFFF,0xFFFFC83D
    };
    private final GuiScreen parent;
    private GuiTextField nameField;
    private String selectedId="";
    private String status="Click a waypoint to select it.";
    private int left,top,panelWidth=390,panelHeight=300,listTop,paletteTop;
    private int scroll;
    private int selectedColor=GOLD;

    public WaypointsGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui() {
        buttonList.clear();
        panelWidth=Math.min(430,width-24);
        panelHeight=Math.min(300,height-24);
        left=(width-panelWidth)/2; top=(height-panelHeight)/2;
        nameField=new GuiTextField(20,fontRendererObj,left+14,top+50,panelWidth-28,18);
        nameField.setMaxStringLength(40);
        paletteTop=top+72;
        listTop=top+98;
        int y=top+panelHeight-30, gap=5;
        int w=(panelWidth-28-gap*5)/6;
        buttonList.add(new GuiButton(1,left+14,y,w,20,"Create"));
        buttonList.add(new GuiButton(2,left+14+(w+gap),y,w,20,"Navigate"));
        buttonList.add(new GuiButton(3,left+14+2*(w+gap),y,w,20,"Delete"));
        buttonList.add(new GuiButton(4,left+14+3*(w+gap),y,w,20,"Export"));
        buttonList.add(new GuiButton(5,left+14+4*(w+gap),y,w,20,"Import"));
        buttonList.add(new GuiButton(7,left+14+5*(w+gap),y,w,20,"Options"));
        buttonList.add(new GuiButton(6,left+panelWidth-66,top+9,52,18,"Close"));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if(button==null)return;
        WaypointManager manager=WaypointManager.getInstance();
        try {
            if(button.id==1) createWaypoint();
            else if(button.id==2) {
                StaffWaypoint point=manager.find(selectedId);
                if(point==null){status="Select a waypoint first.";return;}
                manager.select(point.getId()); status="Navigation target: "+point.getLabel();
            } else if(button.id==3) {
                if(manager.remove(selectedId)){selectedId="";status="Waypoint deleted.";}else status="Select a waypoint first.";
            } else if(button.id==4) {
                File file=new ShareWaypointsWithFriends().exportForSharing(manager.getForCurrentServer());
                status="Exported to "+file.getName();
            } else if(button.id==5) {
                List<StaffWaypoint> points=new WaypointImportExport().read(manager.getExportFile());
                int added=manager.addAllImported(points);status="Imported "+added+" new waypoint(s); skipped "+(points.size()-added)+" duplicate(s).";
            } else if(button.id==6) mc.displayGuiScreen(parent);
            else if(button.id==7) mc.displayGuiScreen(new WaypointVisualSettingsGui(this));
        } catch(Exception e){status="Waypoint action failed: "+e.getMessage();}
    }

    private void createWaypoint() {
        if(mc.thePlayer==null){status="Join a world before creating a waypoint.";return;}
        String name=nameField.getText().trim();
        if(name.isEmpty())name="Waypoint "+(WaypointManager.getInstance().getForCurrentServer().size()+1);
        StaffWaypoint point=WaypointManager.getInstance().add(name,mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ);
        point.setColor(selectedColor);
        point.setIconId(WaypointSettings.getString("defaultIcon","pin"));
        WaypointManager.getInstance().select(point.getId());
        WaypointManager.getInstance().saveChanges();
        selectedId=point.getId();nameField.setText("");status="Created "+point.getLabel()+".";
    }

    @Override protected void keyTyped(char typedChar,int keyCode)throws IOException {
        if(nameField!=null&&nameField.isFocused()) {
            if(keyCode==1){mc.displayGuiScreen(parent);return;}
            if(keyCode==28){createWaypoint();return;}
            nameField.textboxKeyTyped(typedChar,keyCode);
            return;
        }
        if(keyCode==1){mc.displayGuiScreen(parent);return;}
        super.keyTyped(typedChar,keyCode);
    }

    @Override protected void mouseClicked(int mouseX,int mouseY,int mouseButton)throws IOException {
        super.mouseClicked(mouseX,mouseY,mouseButton);
        if(nameField!=null)nameField.mouseClicked(mouseX,mouseY,mouseButton);
        int paletteX=left+52;
        for(int i=0;i<WAYPOINT_COLORS.length;i++) {
            int x=paletteX+i*16;
            if(mouseX>=x&&mouseX<x+13&&mouseY>=paletteTop&&mouseY<paletteTop+13) {
                selectedColor=WAYPOINT_COLORS[i];
                StaffWaypoint selected=WaypointManager.getInstance().find(selectedId);
                if(selected!=null) {
                    selected.setColor(selectedColor);
                    WaypointManager.getInstance().saveChanges();
                    status="Color updated for "+selected.getLabel()+".";
                } else status="Color selected for the next waypoint.";
                return;
            }
        }
        List<StaffWaypoint> points=WaypointManager.getInstance().getSortedCurrentWorld();
        int row=(mouseY-listTop)/16;
        int visible=Math.max(0,(top+panelHeight-57-listTop)/16);
        if(mouseX>=left+12&&mouseX<=left+panelWidth-12&&mouseY>=listTop&&row>=0&&row<visible&&row+scroll<points.size()) {
            StaffWaypoint point=points.get(row+scroll);
            selectedId=point.getId();selectedColor=point.getColor();
            WaypointManager.getInstance().select(point.getId());
            status="Selected "+point.getLabel()+".";
        }
    }

    @Override public void handleMouseInput()throws IOException {
        super.handleMouseInput();
        int wheel=Mouse.getDWheel();
        if(wheel!=0) {
            int visible=Math.max(0,(top+panelHeight-57-listTop)/16);
            int max=Math.max(0,WaypointManager.getInstance().getSortedCurrentWorld().size()-visible);
            scroll=Math.max(0,Math.min(max,scroll+(wheel>0?-1:1)));
        }
    }

    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawRect(0,0,width,height,0x99000000);
        drawRect(left,top,left+panelWidth,top+panelHeight,0xF00B0D12);
        drawRect(left,top,left+panelWidth,top+2,GOLD);
        drawRect(left+1,top+1,left+2,top+panelHeight-1,0x664B515D);
        drawString(fontRendererObj,"ALPHA  /  WAYPOINTS",left+14,top+12,0xFFF0F1F4);
        drawString(fontRendererObj,"LOCAL MARKERS  ·  SORTED BY DISTANCE",left+14,top+30,GOLD);
        drawString(fontRendererObj,"Name",left+14,top+42,0xFFB8BDC8);
        nameField.drawTextBox();
        drawString(fontRendererObj,"Color",left+14,paletteTop+2,0xFFB8BDC8);
        for(int i=0;i<WAYPOINT_COLORS.length;i++) {
            int x=left+52+i*16;
            int color=WAYPOINT_COLORS[i];
            if(color==selectedColor) drawRect(x-2,paletteTop-2,x+15,paletteTop+15,0xFFFFFFFF);
            drawRect(x,paletteTop,x+13,paletteTop+13,color);
        }
        drawRect(left+12,listTop-3,left+panelWidth-12,top+panelHeight-39,0x55212630);
        List<StaffWaypoint> points=WaypointManager.getInstance().getSortedCurrentWorld();
        int visible=Math.max(0,(top+panelHeight-57-listTop)/16);
        for(int i=scroll;i<points.size()&&i<scroll+visible;i++) {
            StaffWaypoint p=points.get(i);int y=listTop+(i-scroll)*16;
            if(p.getId().equals(selectedId))drawRect(left+13,y-1,left+panelWidth-13,y+14,0xAA363B45);
            drawRect(left+18,y+3,left+24,y+9,WorldWaypointIcons.getDisplayColor(p));
            String line=p.getLabel()+"   "+(int)Math.floor(p.getX())+", "+(int)Math.floor(p.getY())+", "+(int)Math.floor(p.getZ());
            int max=panelWidth-76;if(fontRendererObj.getStringWidth(line)>max)line=fontRendererObj.trimStringToWidth(line,max-8)+"…";
            fontRendererObj.drawStringWithShadow(line,left+29,y+2,0xFFE6E8ED);
        }
        drawString(fontRendererObj,status,left+14,top+panelHeight-48,0xFFB8BDC8);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    @Override public boolean doesGuiPauseGame(){return false;}
}
