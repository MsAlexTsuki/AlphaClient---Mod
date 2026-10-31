package com.alpharip.staffmod.WayPoints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import java.util.List;

/** Draws and manages local waypoint pins and labels in the world. */
public final class WorldWaypointIcons {
    private static final WorldWaypointIcons INSTANCE=new WorldWaypointIcons();
    public static WorldWaypointIcons getInstance(){return INSTANCE;}

    private boolean enabled=WaypointSettings.getBoolean("markersEnabled",true);
    public void setEnabled(boolean value){enabled=value;WaypointSettings.setBoolean("markersEnabled",value);}
    public void addWaypoint(String name, String iconId, double x, double y, double z) {
        StaffWaypoint point=WaypointManager.getInstance().add(name,x,y,z);
        point.setIconId(iconId);
        WaypointManager.getInstance().saveChanges();
    }
    public boolean removeWaypoint(String name) { return WaypointManager.getInstance().remove(name); }
    public List<StaffWaypoint> getWaypoints() { return WaypointManager.getInstance().getForCurrentServer(); }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        if(!enabled||mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||event==null) return;
        List<StaffWaypoint> points=WaypointManager.getInstance().getForCurrentWorld();
        if(points.isEmpty()) return;
        RenderManager rm=mc.getRenderManager();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            for(StaffWaypoint point:points) {
                double dx=point.getX()+0.5D-rm.viewerPosX;
                double dy=point.getY()-rm.viewerPosY;
                double dz=point.getZ()+0.5D-rm.viewerPosZ;
                double distanceSq=dx*dx+dy*dy+dz*dz;
                if(distanceSq>256.0D*256.0D) continue;
                float red=((point.getColor()>>16)&255)/255.0F;
                float green=((point.getColor()>>8)&255)/255.0F;
                float blue=(point.getColor()&255)/255.0F;
                GlStateManager.disableTexture2D();
                GL11.glLineWidth(2.0F);
                GL11.glColor4f(red,green,blue,0.9F);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex3d(dx,dy,dz);
                GL11.glVertex3d(dx,dy+1.8D,dz);
                GL11.glEnd();
                GlStateManager.enableTexture2D();

                GlStateManager.pushMatrix();
                try {
                    GlStateManager.translate(dx,dy+2.0D,dz);
                    GlStateManager.rotate(-rm.playerViewY,0.0F,1.0F,0.0F);
                    GlStateManager.rotate(rm.playerViewX,1.0F,0.0F,0.0F);
                    GlStateManager.scale(-0.025F,-0.025F,0.025F);
                    GlStateManager.disableDepth();
                    GlStateManager.enableTexture2D();
                    String label="["+point.getIconId()+"] "+point.getLabel();
                    String distance=Math.round(Math.sqrt(distanceSq))+"m";
                    mc.fontRendererObj.drawStringWithShadow(label,-mc.fontRendererObj.getStringWidth(label)/2,0,point.getColor());
                    mc.fontRendererObj.drawStringWithShadow(distance,-mc.fontRendererObj.getStringWidth(distance)/2,10,0xFFE6E8ED);
                } finally {
                    GlStateManager.enableDepth();
                    GlStateManager.popMatrix();
                }
            }
            RouteBetweenWaypoints route=WaypointManager.getInstance().getRoute();
            if(route.isActive()) {
                StaffWaypoint start=WaypointManager.getInstance().find(route.getStartWaypointId());
                StaffWaypoint end=WaypointManager.getInstance().find(route.getEndWaypointId());
                if(start!=null&&end!=null&&start.getServerKey().equals(end.getServerKey())
                        &&start.getDimensionId()==end.getDimensionId()
                        &&ServerWaypointIntegration.belongsToCurrentWorld(start,mc)) {
                    GlStateManager.disableTexture2D();
                    GL11.glColor4f(1.0F,0.78F,0.24F,0.85F);
                    GL11.glLineWidth(2.0F);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(start.getX()+0.5D-rm.viewerPosX,start.getY()+0.5D-rm.viewerPosY,start.getZ()+0.5D-rm.viewerPosZ);
                    GL11.glVertex3d(end.getX()+0.5D-rm.viewerPosX,end.getY()+0.5D-rm.viewerPosY,end.getZ()+0.5D-rm.viewerPosZ);
                    GL11.glEnd();
                    GlStateManager.enableTexture2D();
                }
            }
        } finally {
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1,1,1,1);
            GlStateManager.popMatrix();
        }
    }
}
