package com.alpharip.staffmod.waypoints;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Central management view for death waypoints from every stored server and profile. */
public final class DeathWaypointsGui extends AlphaGuiScreen {
    private static final int GOLD=0xFFFFD866;
    private final GuiScreen parent;
    private String selectedId="";
    private String status="";
    private int left,top,panelWidth,panelHeight,listTop,listBottom,scroll;

    public DeathWaypointsGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui() {
        buttonList.clear();
        panelWidth=Math.min(570,width-24);
        panelHeight=Math.min(340,height-24);
        left=(width-panelWidth)/2;
        top=(height-panelHeight)/2;
        listTop=top+66;
        listBottom=top+panelHeight-52;
        buttonList.add(new AlphaButton(1,left+14,top+panelHeight-31,112,20,"Delete Selected",!selectedId.isEmpty()));
        buttonList.add(new AlphaButton(2,left+panelWidth-66,top+10,52,18,"Back"));
    }

    private List<StaffWaypoint> getDeathWaypoints() {
        List<StaffWaypoint> result=new ArrayList<StaffWaypoint>();
        for(StaffWaypoint point:WaypointManager.getInstance().getAll())
            if(point.isDeathWaypoint())result.add(point);
        Collections.sort(result,new Comparator<StaffWaypoint>() {
            @Override public int compare(StaffWaypoint a,StaffWaypoint b) {
                int server=a.getServerKey().compareToIgnoreCase(b.getServerKey());
                if(server!=0)return server;
                int world=a.getWorldKey().compareToIgnoreCase(b.getWorldKey());
                if(world!=0)return world;
                return a.getLabel().compareToIgnoreCase(b.getLabel());
            }
        });
        return result;
    }

    @Override protected void actionPerformed(GuiButton button) {
        if(button==null)return;
        if(button.id==2){mc.displayGuiScreen(parent);return;}
        if(button.id==1) {
            if(WaypointManager.getInstance().removeDeathWaypointById(selectedId)) {
                selectedId="";
                status="Death waypoint deleted.";
            } else status="Select a death waypoint first.";
            scroll=0;
        }
        initGui();
    }

    @Override protected void mouseClicked(int mouseX,int mouseY,int mouseButton)throws IOException {
        super.mouseClicked(mouseX,mouseY,mouseButton);
        List<StaffWaypoint> points=getDeathWaypoints();
        int row=(mouseY-listTop)/31;
        int visible=Math.max(0,(listBottom-listTop)/31);
        if(mouseX>=left+12&&mouseX<=left+panelWidth-12&&mouseY>=listTop&&mouseY<listBottom
                &&row>=0&&row<visible&&row+scroll<points.size()) {
            selectedId=points.get(row+scroll).getId();
            status="Selected "+points.get(row+scroll).getLabel()+".";
            initGui();
        }
    }

    @Override public void handleMouseInput()throws IOException {
        super.handleMouseInput();
        int wheel=Mouse.getDWheel();
        if(wheel!=0) {
            int visible=Math.max(0,(listBottom-listTop)/31);
            int max=Math.max(0,getDeathWaypoints().size()-visible);
            scroll=Math.max(0,Math.min(max,scroll+(wheel>0?-1:1)));
        }
    }

    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawRect(left,top,left+panelWidth,top+panelHeight,0xD90A0C11);
        drawRect(left,top,left+panelWidth,top+2,GOLD);
        drawString(fontRendererObj,"ALPHA  /  DEATH WAYPOINTS",left+14,top+12,0xFFF0F1F4);
        drawString(fontRendererObj,"All saved death waypoints · all servers and profiles",left+14,top+30,0xFFB8BDC8);
        List<StaffWaypoint> points=getDeathWaypoints();
        drawRect(left+12,listTop-5,left+panelWidth-12,listBottom,0x55212630);
        int visible=Math.max(0,(listBottom-listTop)/31);
        for(int i=scroll;i<points.size()&&i<scroll+visible;i++) {
            StaffWaypoint point=points.get(i);
            int y=listTop+(i-scroll)*31;
            if(point.getId().equals(selectedId))drawRect(left+13,y-1,left+panelWidth-13,y+28,0xAA363B45);
            drawRect(left+18,y+5,left+24,y+11,0xFFFF5555);
            String label=fontRendererObj.trimStringToWidth(point.getLabel(),Math.max(40,panelWidth-250));
            drawString(fontRendererObj,label,left+30,y+2,0xFFE6E8ED);
            String context=point.getServerKey()+"  |  "+point.getWorldKey()+"  |  Dim "+point.getDimensionId();
            drawString(fontRendererObj,fontRendererObj.trimStringToWidth(context,panelWidth-55),left+30,y+15,0xFFB8BDC8);
            String coords=(int)Math.floor(point.getX())+", "+(int)Math.floor(point.getY())+", "+(int)Math.floor(point.getZ());
            int coordsWidth=fontRendererObj.getStringWidth(coords);
            drawString(fontRendererObj,coords,left+panelWidth-22-coordsWidth,y+7,0xFFFFD866);
        }
        if(points.isEmpty())drawString(fontRendererObj,"No death waypoints saved.",left+20,listTop+10,0xFFB8BDC8);
        if(!status.isEmpty())drawString(fontRendererObj,status,left+140,top+panelHeight-25,0xFFB8BDC8);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    @Override public void keyTyped(char typedChar,int keyCode)throws IOException {
        if(keyCode==1){mc.displayGuiScreen(parent);return;}
        super.keyTyped(typedChar,keyCode);
    }

    @Override public boolean doesGuiPauseGame(){return false;}
}