package com.alpharip.staffmod.waypoints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import java.util.List;
import java.awt.Color;

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

    public static int getDisplayColor(StaffWaypoint point) {
        if(point==null)return 0xFFFFC83D;
        if(!WaypointSettings.getBoolean("chromaEnabled",false))return point.getColor();
        long offset=point.getId()==null?0L:point.getId().hashCode();
        float hue=(float)(((System.currentTimeMillis()/12L+offset)%360L+360L)%360L)/360.0F;
        return Color.HSBtoRGB(hue,0.9F,1.0F);
    }

    private static String iconText(String id) {
        if("home".equalsIgnoreCase(id))return "H";
        if("star".equalsIgnoreCase(id))return "*";
        if("death".equalsIgnoreCase(id))return "!";
        if("diamond".equalsIgnoreCase(id))return "<>";
        if("chest".equalsIgnoreCase(id))return "C";
        if("event".equalsIgnoreCase(id))return "E";
        if(id==null||id.trim().isEmpty()||"pin".equalsIgnoreCase(id))return "P";
        String custom=id.trim();
        return custom.length()>4?custom.substring(0,4):custom;
    }

    private static void drawBlockOutline(StaffWaypoint point,RenderManager rm,float red,float green,float blue) {
        double x0=Math.floor(point.getX())-rm.viewerPosX, x1=x0+1.0D;
        double y0=Math.floor(point.getY())-rm.viewerPosY, y1=y0+1.0D;
        double z0=Math.floor(point.getZ())-rm.viewerPosZ, z1=z0+1.0D;
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(red,green,blue,0.95F);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x0,y0,z0); GL11.glVertex3d(x1,y0,z0);
        GL11.glVertex3d(x0,y0,z1); GL11.glVertex3d(x1,y0,z1);
        GL11.glVertex3d(x0,y1,z0); GL11.glVertex3d(x1,y1,z0);
        GL11.glVertex3d(x0,y1,z1); GL11.glVertex3d(x1,y1,z1);
        GL11.glVertex3d(x0,y0,z0); GL11.glVertex3d(x0,y1,z0);
        GL11.glVertex3d(x1,y0,z0); GL11.glVertex3d(x1,y1,z0);
        GL11.glVertex3d(x0,y0,z1); GL11.glVertex3d(x0,y1,z1);
        GL11.glVertex3d(x1,y0,z1); GL11.glVertex3d(x1,y1,z1);
        GL11.glVertex3d(x0,y0,z0); GL11.glVertex3d(x0,y0,z1);
        GL11.glVertex3d(x1,y0,z0); GL11.glVertex3d(x1,y0,z1);
        GL11.glVertex3d(x0,y1,z0); GL11.glVertex3d(x0,y1,z1);
        GL11.glVertex3d(x1,y1,z0); GL11.glVertex3d(x1,y1,z1);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        if(!enabled||WaypointSettings.isTemporarilyHidden()||mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||event==null) return;
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
                int displayColor=getDisplayColor(point);
                float red=((displayColor>>16)&255)/255.0F;
                float green=((displayColor>>8)&255)/255.0F;
                float blue=(displayColor&255)/255.0F;
                int beamHeight=WaypointSettings.getInt("beamHeight",8,0,32);
                if(beamHeight>0) {
                    GlStateManager.disableTexture2D();
                    GL11.glLineWidth(2.0F);
                    GL11.glColor4f(red,green,blue,0.9F);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(dx,dy,dz);
                    GL11.glVertex3d(dx,dy+beamHeight,dz);
                    GL11.glEnd();
                    GlStateManager.enableTexture2D();
                }
                if(WaypointSettings.getBoolean("blockHighlightEnabled",true))
                    drawBlockOutline(point,rm,red,green,blue);


                GlStateManager.pushMatrix();
                try {
                    GlStateManager.translate(dx,dy+Math.max(2.0D,beamHeight+0.2D),dz);
                    GlStateManager.rotate(-rm.playerViewY,0.0F,1.0F,0.0F);
                    GlStateManager.rotate(rm.playerViewX,1.0F,0.0F,0.0F);
                    float labelScale=0.025F*(WaypointSettings.getInt("labelSize",100,50,200)/100.0F);
                    GlStateManager.scale(-labelScale,-labelScale,labelScale);
                    GlStateManager.disableDepth();
                    GlStateManager.enableTexture2D();
                    String label="["+iconText(point.getIconId())+"] "+point.getLabel();
                    String distance=Math.round(Math.sqrt(distanceSq))+"m";
                    int labelColor=displayColor;
                    mc.fontRendererObj.drawStringWithShadow(label,-mc.fontRendererObj.getStringWidth(label)/2,0,labelColor);
                    mc.fontRendererObj.drawStringWithShadow(distance,-mc.fontRendererObj.getStringWidth(distance)/2,mc.fontRendererObj.FONT_HEIGHT+2,0xFFE6E8ED);
                } finally {
                    GlStateManager.enableDepth();
                    GlStateManager.popMatrix();
                }
            }
            List<StaffWaypoint> routePoints=WaypointManager.getInstance().getRouteWaypoints();
            if(routePoints.size()>=2) {
                boolean sameWorld=true;
                for(StaffWaypoint point:routePoints)
                    if(!ServerWaypointIntegration.belongsToCurrentWorld(point,mc)){sameWorld=false;break;}
                if(sameWorld) {
                    GlStateManager.disableTexture2D();
                    GL11.glColor4f(1.0F,0.78F,0.24F,0.85F);
                    GL11.glLineWidth(2.0F);
                    GL11.glBegin(GL11.GL_LINES);
                    for(int i=1;i<routePoints.size();i++) {
                        StaffWaypoint start=routePoints.get(i-1),end=routePoints.get(i);
                        GL11.glVertex3d(start.getX()+0.5D-rm.viewerPosX,start.getY()+0.5D-rm.viewerPosY,start.getZ()+0.5D-rm.viewerPosZ);
                        GL11.glVertex3d(end.getX()+0.5D-rm.viewerPosX,end.getY()+0.5D-rm.viewerPosY,end.getZ()+0.5D-rm.viewerPosZ);
                    }
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
