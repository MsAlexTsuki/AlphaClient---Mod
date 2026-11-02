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

    private static void drawGroundCircle(StaffWaypoint point,RenderManager rm,float red,float green,float blue) {
        double centerX=Math.floor(point.getX())+0.5D-rm.viewerPosX;
        double centerY=Math.floor(point.getY())+0.035D-rm.viewerPosY;
        double centerZ=Math.floor(point.getZ())+0.5D-rm.viewerPosZ;
        double radius=0.58D;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GL11.glColor4f(red,green,blue,0.20F);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3d(centerX,centerY,centerZ);
        for(int i=0;i<=48;i++) {
            double angle=Math.PI*2.0D*i/48.0D;
            GL11.glVertex3d(centerX+Math.cos(angle)*radius,centerY,centerZ+Math.sin(angle)*radius);
        }
        GL11.glEnd();
        GL11.glLineWidth(3.5F);
        GL11.glColor4f(red,green,blue,0.95F);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for(int i=0;i<48;i++) {
            double angle=Math.PI*2.0D*i/48.0D;
            GL11.glVertex3d(centerX+Math.cos(angle)*radius,centerY+0.006D,centerZ+Math.sin(angle)*radius);
        }
        GL11.glEnd();
        GL11.glLineWidth(1.0F);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
    }

    private static void drawBeam(double x,double y,double z,int height,float red,float green,float blue) {
        double halfWidth=0.105D;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GL11.glColor4f(red,green,blue,0.58F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x-halfWidth,y,z); GL11.glVertex3d(x+halfWidth,y,z);
        GL11.glVertex3d(x+halfWidth,y+height,z); GL11.glVertex3d(x-halfWidth,y+height,z);
        GL11.glVertex3d(x,y,z-halfWidth); GL11.glVertex3d(x,y,z+halfWidth);
        GL11.glVertex3d(x,y+height,z+halfWidth); GL11.glVertex3d(x,y+height,z-halfWidth);
        GL11.glEnd();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    private static void drawWaypointLabel(Minecraft mc,StaffWaypoint point,RenderManager rm,
                                          double dx,double dy,double dz,int beamHeight,int displayColor) {
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(dx,dy+Math.max(2.0D,beamHeight+0.35D),dz);
            GlStateManager.rotate(-rm.playerViewY,0.0F,1.0F,0.0F);
            GlStateManager.rotate(rm.playerViewX,1.0F,0.0F,0.0F);
            float labelScale=0.035F*(WaypointSettings.getInt("labelSize",100,50,200)/100.0F);
            GlStateManager.scale(-labelScale,-labelScale,labelScale);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            String label=point.getLabel();
            String distance=Math.round(Math.sqrt(
                    (point.getX()-mc.thePlayer.posX)*(point.getX()-mc.thePlayer.posX)
                    +(point.getY()-mc.thePlayer.posY)*(point.getY()-mc.thePlayer.posY)
                    +(point.getZ()-mc.thePlayer.posZ)*(point.getZ()-mc.thePlayer.posZ)))+"m";
            int boxWidth=Math.max(mc.fontRendererObj.getStringWidth(label),
                    mc.fontRendererObj.getStringWidth(distance))+16;
            int boxHeight=mc.fontRendererObj.FONT_HEIGHT*2+12;
            double x0=-boxWidth/2.0D, x1=boxWidth/2.0D;
            GlStateManager.disableTexture2D();
            GL11.glColor4f(0.035F,0.045F,0.065F,0.88F);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d(x0,-5,0); GL11.glVertex3d(x1,-5,0);
            GL11.glVertex3d(x1,boxHeight-5,0); GL11.glVertex3d(x0,boxHeight-5,0);
            GL11.glEnd();
            GL11.glColor4f(((displayColor>>16)&255)/255.0F,((displayColor>>8)&255)/255.0F,
                    (displayColor&255)/255.0F,1.0F);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d(x0,-5,0.001D); GL11.glVertex3d(x1,-5,0.001D);
            GL11.glVertex3d(x1,-2,0.001D); GL11.glVertex3d(x0,-2,0.001D);
            GL11.glEnd();
            GlStateManager.enableTexture2D();
            mc.fontRendererObj.drawStringWithShadow(label,
                    -mc.fontRendererObj.getStringWidth(label)/2,0,0xFFFFFFFF);
            mc.fontRendererObj.drawStringWithShadow(distance,
                    -mc.fontRendererObj.getStringWidth(distance)/2,mc.fontRendererObj.FONT_HEIGHT+3,0xFFE1E5EC);
        } finally {
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }
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
                if(beamHeight>0) drawBeam(dx,dy,dz,beamHeight,red,green,blue);
                if(WaypointSettings.getBoolean("blockHighlightEnabled",true))
                    drawGroundCircle(point,rm,red,green,blue);
                drawWaypointLabel(mc,point,rm,dx,dy,dz,beamHeight,displayColor);
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
