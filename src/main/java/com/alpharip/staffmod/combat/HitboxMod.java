package com.alpharip.staffmod.combat;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Visualizes existing player bounding boxes; it never changes collision geometry. */
public final class HitboxMod {
    private static final double RANGE = 32.0D;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        AlphaModule module = AlphaModuleRegistry.find("hitbox");
        Minecraft mc = Minecraft.getMinecraft();
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()
                || mc == null || mc.thePlayer == null || mc.theWorld == null || mc.getRenderViewEntity() == null) return;

        Entity view = mc.getRenderViewEntity();
        double viewX = view.lastTickPosX + (view.posX - view.lastTickPosX) * event.partialTicks;
        double viewY = view.lastTickPosY + (view.posY - view.lastTickPosY) * event.partialTicks;
        double viewZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * event.partialTicks;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glLineWidth(1.4F);
            GL11.glColor4f(1.0F, 0.78F, 0.20F, 0.95F);
            GL11.glBegin(GL11.GL_LINES);

            for (Object object : mc.theWorld.loadedEntityList) {
                if (!(object instanceof EntityPlayer)) continue;
                EntityPlayer player = (EntityPlayer) object;
                if (player == mc.thePlayer || player.isDead || !player.isEntityAlive()
                        || mc.thePlayer.getDistanceToEntity(player) > RANGE) continue;

                double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks - viewX;
                double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks - viewY;
                double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks - viewZ;
                AxisAlignedBB box = player.getEntityBoundingBox();
                double minX = box.minX - player.posX + x;
                double maxX = box.maxX - player.posX + x;
                double minY = box.minY - player.posY + y;
                double maxY = box.maxY - player.posY + y;
                double minZ = box.minZ - player.posZ + z;
                double maxZ = box.maxZ - player.posZ + z;
                drawBox(minX, minY, minZ, maxX, maxY, maxZ);
            }
            GL11.glEnd();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void drawBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        // Bottom rectangle
        edge(x1,y1,z1, x2,y1,z1); edge(x2,y1,z1, x2,y1,z2);
        edge(x2,y1,z2, x1,y1,z2); edge(x1,y1,z2, x1,y1,z1);
        // Top rectangle
        edge(x1,y2,z1, x2,y2,z1); edge(x2,y2,z1, x2,y2,z2);
        edge(x2,y2,z2, x1,y2,z2); edge(x1,y2,z2, x1,y2,z1);
        // Vertical edges
        edge(x1,y1,z1, x1,y2,z1); edge(x2,y1,z1, x2,y2,z1);
        edge(x2,y1,z2, x2,y2,z2); edge(x1,y1,z2, x1,y2,z2);
    }

    private static void edge(double x1, double y1, double z1, double x2, double y2, double z2) {
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y2, z2);
    }
}
