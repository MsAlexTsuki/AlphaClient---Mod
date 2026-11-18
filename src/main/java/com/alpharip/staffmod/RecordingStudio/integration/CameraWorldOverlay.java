package com.alpharip.staffmod.RecordingStudio.integration;
import com.alpharip.staffmod.RecordingStudio.camera.CameraController;import com.alpharip.staffmod.RecordingStudio.camera.CameraPose;
import net.minecraft.client.Minecraft;import net.minecraftforge.client.event.RenderWorldLastEvent;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import org.lwjgl.opengl.GL11;
/** Draws a world-space camera marker and viewing direction so the camera position is visible in-game. */
public final class CameraWorldOverlay {
 private final CameraController camera;public CameraWorldOverlay(CameraController c){if(c==null)throw new IllegalArgumentException("camera");camera=c;}
 @SubscribeEvent public void onRenderWorld(RenderWorldLastEvent event){if(!camera.isActive())return;Minecraft mc=Minecraft.getMinecraft();CameraPose p=camera.getPose();if(mc.theWorld==null||mc.getRenderViewEntity()==null)return;double x=p.x-mc.getRenderManager().viewerPosX,y=p.y-mc.getRenderManager().viewerPosY,z=p.z-mc.getRenderManager().viewerPosZ;double yaw=Math.toRadians(p.yaw),pitch=Math.toRadians(p.pitch);double dx=-Math.sin(yaw)*Math.cos(pitch),dy=-Math.sin(pitch),dz=Math.cos(yaw)*Math.cos(pitch);GL11.glPushMatrix();GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_LINE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);try{GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glLineWidth(2.0F);GL11.glColor4f(1.0F,0.78F,0.12F,1.0F);double s=0.22;GL11.glBegin(GL11.GL_LINES);
 line(x-s,y-s,z-s,x+s,y-s,z-s);line(x+s,y-s,z-s,x+s,y+s,z-s);line(x+s,y+s,z-s,x-s,y+s,z-s);line(x-s,y+s,z-s,x-s,y-s,z-s);
 line(x-s,y-s,z+s,x+s,y-s,z+s);line(x+s,y-s,z+s,x+s,y+s,z+s);line(x+s,y+s,z+s,x-s,y+s,z+s);line(x-s,y+s,z+s,x-s,y-s,z+s);
 line(x-s,y-s,z-s,x-s,y-s,z+s);line(x+s,y-s,z-s,x+s,y-s,z+s);line(x+s,y+s,z-s,x+s,y+s,z+s);line(x-s,y+s,z-s,x-s,y+s,z+s);
 GL11.glColor4f(0.2F,0.85F,1.0F,1.0F);double length=1.4;line(x,y,z,x+dx*length,y+dy*length,z+dz*length);
 double rightX=Math.cos(yaw),rightZ=Math.sin(yaw);double half=.38;double fx=x+dx*length,fy=y+dy*length,fz=z+dz*length;double upX=-Math.sin(yaw)*Math.sin(pitch),upY=Math.cos(pitch),upZ=Math.cos(yaw)*Math.sin(pitch);double lx=fx-rightX*half-upX*half,ly=fy-upY*half,lz=fz-rightZ*half-upZ*half;double rx=fx+rightX*half-upX*half,ry=fy-upY*half,lrz=fz+rightZ*half-upZ*half;double ux=fx+upX*half,uy=fy+upY*half,uz=fz+upZ*half;double bx=fx-upX*half,by=fy-upY*half,bz=fz-upZ*half;line(lx,ly,lz,rx,ry,lrz);line(rx,ry,lrz,ux,uy,uz);line(ux,uy,uz,bx,by,bz);line(bx,by,bz,lx,ly,lz);GL11.glEnd();
 }finally{GL11.glPopAttrib();GL11.glPopMatrix();}}
 private static void line(double ax,double ay,double az,double bx,double by,double bz){GL11.glVertex3d(ax,ay,az);GL11.glVertex3d(bx,by,bz);}
}
