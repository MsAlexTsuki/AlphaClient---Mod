package com.alpharip.staffmod.waypoints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
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
    public List<StaffWaypoint> getWaypoints() { return WaypointManager.getInstance().getForCurrentWorld(); }

    public static int getDisplayColor(StaffWaypoint point) {
        if(point==null)return 0xFFFFC83D;
        if(!point.isChromaEnabled())return point.getColor();
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

    private static void drawGroundCircle(double centerX,double centerY,double centerZ,
                                          float red,float green,float blue) {
        double radius=0.58D;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        // Keep depth testing enabled so terrain and solid blocks occlude the circle.
        // Disable depth writes only, preventing this translucent marker from affecting later rendering.
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        try {
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
        } finally {
            GL11.glLineWidth(1.0F);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F,1.0F,1.0F,1.0F);
        }
    }

    private static void drawBeam(double x,double y,double z,double height,float red,float green,float blue) {
        final double radius=0.105D;
        final int segments=32;
        boolean cullingEnabled=GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glColor4f(red,green,blue,0.62F);
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for(int i=0;i<=segments;i++) {
            double angle=Math.PI*2.0D*i/segments;
            double offsetX=Math.cos(angle)*radius;
            double offsetZ=Math.sin(angle)*radius;
            GL11.glNormal3d(Math.cos(angle),0.0D,Math.sin(angle));
            GL11.glVertex3d(x+offsetX,y,z+offsetZ);
            GL11.glVertex3d(x+offsetX,y+height,z+offsetZ);
        }
        GL11.glEnd();
        if(cullingEnabled) GL11.glEnable(GL11.GL_CULL_FACE);
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    private static void drawWaypointLabel(Minecraft mc,StaffWaypoint point,RenderManager rm,
                                          double dx,double dy,double dz,
                                          double distance,int displayColor) {
        // Minecraft's normal world projection uses the configured render distance as
        // its far clipping plane. Waypoint labels need a longer projection so they remain
        // visible even when their waypoint is beyond the terrain render distance.
        int previousMatrixMode=GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        FloatBuffer projection=BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX,projection);
        float projectionA=projection.get(10);
        float projectionB=projection.get(14);
        float nearPlane=projectionB/(projectionA-1.0F);
        boolean extendedProjection=nearPlane>0.0F && nearPlane<4096.0F;
        if(extendedProjection) {
            float farPlane=4096.0F;
            projection.put(10,-(farPlane+nearPlane)/(farPlane-nearPlane));
            projection.put(14,-(2.0F*farPlane*nearPlane)/(farPlane-nearPlane));
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadMatrix(projection);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }

        GlStateManager.pushMatrix();
        try {
            // Keep the nameplate close to the waypoint itself.
            GlStateManager.translate(dx,dy+2.0D,dz);
            GlStateManager.rotate(-rm.playerViewY,0.0F,1.0F,0.0F);
            GlStateManager.rotate(rm.playerViewX,1.0F,0.0F,0.0F);
            float baseScale=0.035F*(WaypointSettings.getInt("labelSize",100,50,200)/100.0F);
            // Compensate for perspective so the label stays approximately the same screen size.
            // The scale at 10 blocks is the reference size; it grows linearly beyond that distance.
            float distanceScale=(float)Math.max(1.0D,distance/10.0D);
            float labelScale=baseScale*distanceScale;
            GlStateManager.scale(-labelScale,-labelScale,labelScale);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            GlStateManager.color(1.0F,1.0F,1.0F,1.0F);
            String label=point.getLabel();
            String distanceText=Math.round(Math.sqrt(
                    (point.getX()-mc.thePlayer.posX)*(point.getX()-mc.thePlayer.posX)
                    +(point.getY()-mc.thePlayer.posY)*(point.getY()-mc.thePlayer.posY)
                    +(point.getZ()-mc.thePlayer.posZ)*(point.getZ()-mc.thePlayer.posZ)))+"m";
            int boxWidth=Math.max(mc.fontRendererObj.getStringWidth(label),
                    mc.fontRendererObj.getStringWidth(distanceText))+16;
            int boxHeight=mc.fontRendererObj.FONT_HEIGHT*2+12;
            double x0=-boxWidth/2.0D, x1=boxWidth/2.0D;
            GlStateManager.disableTexture2D();
            GL11.glColor4f(0.025F,0.032F,0.048F,0.96F);
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
            mc.fontRendererObj.drawStringWithShadow(distanceText,
                    -mc.fontRendererObj.getStringWidth(distanceText)/2,mc.fontRendererObj.FONT_HEIGHT+3,0xFFE1E5EC);
        } finally {
            GlStateManager.color(1.0F,1.0F,1.0F,1.0F);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
            if(extendedProjection) {
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPopMatrix();
            }
            GL11.glMatrixMode(previousMatrixMode);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        if(!enabled||mc==null||mc.thePlayer==null||mc.theWorld==null||mc.fontRendererObj==null||event==null) return;
        List<StaffWaypoint> points=WaypointManager.getInstance().getForCurrentWorld();
        if(points.isEmpty()) return;
        RenderManager rm=mc.getRenderManager();

        // Preserve the OpenGL state owned by Minecraft and other render hooks.
        boolean blendEnabled=GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depthEnabled=GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean lightingEnabled=GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean textureEnabled=GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean cullingEnabled=GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depthMask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int blendSrcRgb=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int blendDstRgb=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int blendSrcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int blendDstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        FloatBuffer previousColor=BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR,previousColor);
        float previousRed=previousColor.get(0),previousGreen=previousColor.get(1);
        float previousBlue=previousColor.get(2),previousAlpha=previousColor.get(3);
        FloatBuffer previousLineWidth=BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_LINE_WIDTH,previousLineWidth);
        float lineWidth=previousLineWidth.get(0);

        GlStateManager.pushMatrix();
        try {
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770,771,1,0);
            if(!WaypointSettings.isTemporarilyHidden()) {
            for(StaffWaypoint point:points) {
                // Use the center of the waypoint's block for every marker element.
                // This keeps the beam, ground circle and nametag on one shared vertical axis.
                double markerX=Math.floor(point.getX())+0.5D;
                double markerY=Math.floor(point.getY())+0.035D;
                double markerZ=Math.floor(point.getZ())+0.5D;
                double dx=markerX-rm.viewerPosX;
                double dy=markerY-rm.viewerPosY;
                double dz=markerZ-rm.viewerPosZ;
                double distanceSq=dx*dx+dy*dy+dz*dz;
                double distance=Math.sqrt(distanceSq);

                // Hide the complete waypoint marker when the player is within five blocks.
                if(distance<=5.0D) continue;
                double maxWaypointRenderDistance=WaypointSettings.getBoolean("maxWaypointRenderDistance",true)
                        ?500.0D:3000.0D;
                if(distance>maxWaypointRenderDistance) continue;

                int displayColor=getDisplayColor(point);
                float red=((displayColor>>16)&255)/255.0F;
                float green=((displayColor>>8)&255)/255.0F;
                float blue=(displayColor&255)/255.0F;

                // Grow the cylindrical beam linearly with distance, reaching 32 blocks at 300.
                double beamHeight=Math.min(32.0D,distance*(32.0D/300.0D));
                if(beamHeight>0.0D) drawBeam(dx,dy,dz,beamHeight,red,green,blue);
                if(WaypointSettings.getBoolean("blockHighlightEnabled",true))
                    drawGroundCircle(dx,dy,dz,red,green,blue);
                drawWaypointLabel(mc,point,rm,dx,dy,dz,distance,displayColor);
            }
            }
            StaffWaypoint navigationTarget=WaypointManager.getInstance().getSelected();
            List<Vec3> navigationPath=WaypointPathfinder.getInstance().getPathPoints();
            if(navigationTarget!=null
                    &&ServerWaypointIntegration.belongsToCurrentWorld(navigationTarget,mc)
                    &&navigationPath.size()>=2) {
                double playerX=mc.thePlayer.lastTickPosX
                        +(mc.thePlayer.posX-mc.thePlayer.lastTickPosX)*event.partialTicks;
                double playerY=mc.thePlayer.lastTickPosY
                        +(mc.thePlayer.posY-mc.thePlayer.lastTickPosY)*event.partialTicks
                        +0.08D;
                double playerZ=mc.thePlayer.lastTickPosZ
                        +(mc.thePlayer.posZ-mc.thePlayer.lastTickPosZ)*event.partialTicks;

                GlStateManager.disableTexture2D();
                GlStateManager.enableBlend();

                // The navigation route must remain visible when its path crosses
                // hills, terrain or other solid blocks.
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);

                double routeStartY = playerY + 0.25D;
                // Bright blue outer glow for strong contrast against terrain.
                GL11.glLineWidth(11.0F);
                GL11.glColor4f(0.0F,0.55F,1.0F,0.95F);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(playerX-rm.viewerPosX,routeStartY-rm.viewerPosY,playerZ-rm.viewerPosZ);
                for(Vec3 point:navigationPath)
                    GL11.glVertex3d(point.xCoord-rm.viewerPosX,
                            point.yCoord+0.25D-rm.viewerPosY,point.zCoord-rm.viewerPosZ);
                GL11.glEnd();

                // Solid electric-cyan core.
                GL11.glLineWidth(5.5F);
                GL11.glColor4f(0.0F,1.0F,1.0F,1.0F);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(playerX-rm.viewerPosX,routeStartY-rm.viewerPosY,playerZ-rm.viewerPosZ);
                for(Vec3 point:navigationPath)
                    GL11.glVertex3d(point.xCoord-rm.viewerPosX,
                            point.yCoord+0.25D-rm.viewerPosY,point.zCoord-rm.viewerPosZ);
                GL11.glEnd();

                GL11.glLineWidth(1.0F);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.color(1.0F,1.0F,1.0F,1.0F);
                GlStateManager.enableTexture2D();
            }
            List<StaffWaypoint> routePoints=WaypointManager.getInstance().getRouteWaypoints();
            if(routePoints.size()>=2) {
                boolean sameWorld=true;
                for(StaffWaypoint point:routePoints)
                    if(!ServerWaypointIntegration.belongsToCurrentWorld(point,mc)){sameWorld=false;break;}
                if(sameWorld) {
                    GlStateManager.disableTexture2D();
                    // Keep the route blue while allowing terrain and blocks to occlude it.
                    GlStateManager.enableDepth();
                    GlStateManager.depthMask(false);
                    GL11.glColor4f(0.0F,0.45F,1.0F,1.0F);
                    GL11.glLineWidth(3.0F);
                    GL11.glBegin(GL11.GL_LINES);
                    for(int i=1;i<routePoints.size();i++) {
                        StaffWaypoint start=routePoints.get(i-1),end=routePoints.get(i);
                        GL11.glVertex3d(start.getX()+0.5D-rm.viewerPosX,start.getY()+0.5D-rm.viewerPosY,start.getZ()+0.5D-rm.viewerPosZ);
                        GL11.glVertex3d(end.getX()+0.5D-rm.viewerPosX,end.getY()+0.5D-rm.viewerPosY,end.getZ()+0.5D-rm.viewerPosZ);
                    }
                    GL11.glEnd();
                    GL11.glLineWidth(1.0F);
                    GlStateManager.depthMask(true);
                    GlStateManager.color(1.0F,1.0F,1.0F,1.0F);
                    GlStateManager.enableTexture2D();
                }
            }
        } finally {
            GlStateManager.popMatrix();

            // Restore the exact state that was active before waypoint rendering.
            GlStateManager.depthMask(depthMask);
            if(depthEnabled) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
            if(lightingEnabled) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
            if(textureEnabled) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
            if(blendEnabled) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
            GlStateManager.tryBlendFuncSeparate(blendSrcRgb,blendDstRgb,blendSrcAlpha,blendDstAlpha);
            if(cullingEnabled) GL11.glEnable(GL11.GL_CULL_FACE); else GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glLineWidth(lineWidth);
            GlStateManager.color(previousRed,previousGreen,previousBlue,previousAlpha);
        }
    }
}