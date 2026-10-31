package com.alpharip.staffmod.WayPoints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.List;

/** Compact top-right waypoint radar showing nearby local markers and the selected destination. */
public final class MinimapWithWaypoints {
    public boolean enabled = WaypointSettings.getBoolean("minimapEnabled", true);
    public String category = "HUD/navigation";
    public String style = "default";
    private static final int SIZE=92, RANGE=128;
    public void setEnabled(boolean value) { enabled=value; WaypointSettings.setBoolean("minimapEnabled",value); }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if(!enabled||event.type!=RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc=Minecraft.getMinecraft();
        if(mc==null||mc.thePlayer==null||mc.gameSettings.hideGUI||mc.gameSettings.showDebugInfo) return;
        List<StaffWaypoint> points=WaypointManager.getInstance().getForCurrentWorld();
        if(points.isEmpty()) return;
        ScaledResolution scaled=new ScaledResolution(mc);
        int left=scaled.getScaledWidth()-SIZE-8, top=8, centerX=left+SIZE/2, centerY=top+SIZE/2;
        GlStateManager.pushMatrix();
        try {
            Gui.drawRect(left,top,left+SIZE,top+SIZE,0xB00B0D12);
            Gui.drawRect(left,top,left+SIZE,top+1,0xFFFFC83D);
            Gui.drawRect(left,top+SIZE-1,left+SIZE,top+SIZE,0xFF555A63);
            Gui.drawRect(centerX-1,top+8,centerX+1,top+SIZE-8,0x445B606A);
            Gui.drawRect(left+8,centerY-1,left+SIZE-8,centerY+1,0x445B606A);
            Gui.drawRect(centerX-2,centerY-2,centerX+3,centerY+3,0xFFFFFFFF);
            for(StaffWaypoint p:points) {
                double dx=p.getX()-mc.thePlayer.posX, dz=p.getZ()-mc.thePlayer.posZ;
                double distance=Math.sqrt(dx*dx+dz*dz);
                if(distance>RANGE) continue;
                int px=centerX+(int)Math.round(dx/RANGE*(SIZE/2-10));
                int py=centerY+(int)Math.round(dz/RANGE*(SIZE/2-10));
                int color=0xFF000000|(p.getColor()&0xFFFFFF);
                Gui.drawRect(px-2,py-2,px+3,py+3,color);
            }
            RouteBetweenWaypoints route=WaypointManager.getInstance().getRoute();
            if(route.isActive()) {
                StaffWaypoint start=WaypointManager.getInstance().find(route.getStartWaypointId());
                StaffWaypoint end=WaypointManager.getInstance().find(route.getEndWaypointId());
                if(start!=null&&end!=null) {
                    String routeText="ROUTE "+start.getLabel()+" -> "+end.getLabel()+" "+Math.round(route.getDistance(start,end))+"m";
                    mc.fontRendererObj.drawStringWithShadow(routeText,left+5,top+SIZE-10,0xFFFFC83D);
                }
            }
            StaffWaypoint selected=WaypointManager.getInstance().getSelected();
            if(selected!=null&&ServerWaypointIntegration.belongsToCurrentWorld(selected,mc)) {
                String line=selected.getLabel()+"  "+Math.round(mc.thePlayer.getDistance(selected.getX(),selected.getY(),selected.getZ()))+"m";
                int width=mc.fontRendererObj.getStringWidth(line);
                mc.fontRendererObj.drawStringWithShadow(line,Math.max(4,left+(SIZE-width)/2),top+SIZE+3,selected.getColor());
            }
            mc.fontRendererObj.drawStringWithShadow("N",centerX-2,top+3,0xFFE6E8ED);
        } finally { GlStateManager.popMatrix(); }
    }
}
