package com.alpharip.staffmod.BlockPlacementPreview;

import com.alpharip.staffmod.WayPoints.WaypointFileUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

/** Shows a translucent placement volume for the block currently held by the player. */
public final class BlockPlacementPreview {
    private static final BlockPlacementPreview INSTANCE = new BlockPlacementPreview();
    private BlockPlacementPreview() { }
    public static BlockPlacementPreview getInstance() { return INSTANCE; }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event == null || !BlockPlacementPreviewSettings.isEnabled()
                || mc == null || mc.thePlayer == null || mc.theWorld == null
                || mc.getRenderManager() == null) return;

        Preview preview = createPreview(mc.theWorld, mc.thePlayer, mc.objectMouseOver,
                mc.thePlayer.getHeldItem());
        if (preview == null || BlockPlacementPreviewSettings.getOpacity() <= 0.0F) return;
        drawPreview(mc.getRenderManager(), preview, BlockPlacementPreviewSettings.getOpacity(),
                BlockPlacementPreviewSettings.isShowCollision());
    }

    private Preview createPreview(World world, EntityPlayer player, MovingObjectPosition hit, ItemStack held) {
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || hit.getBlockPos() == null || hit.sideHit == null || hit.hitVec == null
                || held == null || !(held.getItem() instanceof ItemBlock)) return null;

        ItemBlock itemBlock = (ItemBlock) held.getItem();
        Block block = itemBlock.getBlock();
        BlockPos clickedPos = hit.getBlockPos();
        IBlockState clickedState = world.getBlockState(clickedPos);
        BlockPos placePos = clickedState.getBlock().isReplaceable(world, clickedPos)
                ? clickedPos : clickedPos.offset(hit.sideHit);

        float hitX = clampToBlock(hit.hitVec.xCoord - clickedPos.getX());
        float hitY = clampToBlock(hit.hitVec.yCoord - clickedPos.getY());
        float hitZ = clampToBlock(hit.hitVec.zCoord - clickedPos.getZ());
        IBlockState state;
        try {
            state = block.getStateFromMeta(itemBlock.getMetadata(held.getMetadata()));
            IBlockState placedState = block.onBlockPlaced(world, placePos, hit.sideHit,
                    hitX, hitY, hitZ, held.getMetadata(), player);
            if (placedState != null) state = placedState;
        } catch (RuntimeException invalidState) {
            return null;
        }
        if (state == null) return null;

        AxisAlignedBB box;
        try {
            box = block.getCollisionBoundingBox(world, placePos, state);
        } catch (RuntimeException invalidBounds) {
            box = null;
        }
        if (box == null) {
            box = AxisAlignedBB.getBoundingBox(placePos.getX(), placePos.getY(), placePos.getZ(),
                    placePos.getX() + 1.0D, placePos.getY() + 1.0D, placePos.getZ() + 1.0D);
        }

        boolean blocked = !block.canPlaceBlockAt(world, placePos);
        if (!blocked) {
            List<AxisAlignedBB> collisions = world.getCollidingBoundingBoxes(player, box);
            blocked = collisions != null && !collisions.isEmpty();
        }
        return new Preview(box, blocked);
    }

    private static float clampToBlock(double value) {
        return (float) Math.max(0.0D, Math.min(1.0D, value));
    }

    private void drawPreview(RenderManager renderManager, Preview preview, float opacity, boolean showCollision) {
        AxisAlignedBB box = preview.box;
        double minX = box.minX - renderManager.viewerPosX;
        double minY = box.minY - renderManager.viewerPosY;
        double minZ = box.minZ - renderManager.viewerPosZ;
        double maxX = box.maxX - renderManager.viewerPosX;
        double maxY = box.maxY - renderManager.viewerPosY;
        double maxZ = box.maxZ - renderManager.viewerPosZ;

        float red = showCollision && preview.blocked ? 1.0F : 0.25F;
        float green = showCollision && preview.blocked ? 0.2F : 0.9F;
        float blue = showCollision && preview.blocked ? 0.2F : 0.45F;
        float alpha = Math.max(0.05F, Math.min(1.0F, opacity));

        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();

            GL11.glColor4f(red, green, blue, alpha);
            drawFaces(minX, minY, minZ, maxX, maxY, maxZ);

            GL11.glLineWidth(2.0F);
            GL11.glColor4f(red, green, blue, Math.min(1.0F, alpha + 0.35F));
            drawEdges(minX, minY, minZ, maxX, maxY, maxZ);
        } finally {
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glLineWidth(1.0F);
            GlStateManager.popMatrix();
        }
    }

    private static void drawFaces(double x0, double y0, double z0, double x1, double y1, double z1) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x0, y0, z0); GL11.glVertex3d(x1, y0, z0); GL11.glVertex3d(x1, y1, z0); GL11.glVertex3d(x0, y1, z0);
        GL11.glVertex3d(x1, y0, z1); GL11.glVertex3d(x0, y0, z1); GL11.glVertex3d(x0, y1, z1); GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x0, y0, z1); GL11.glVertex3d(x0, y0, z0); GL11.glVertex3d(x0, y1, z0); GL11.glVertex3d(x0, y1, z1);
        GL11.glVertex3d(x1, y0, z0); GL11.glVertex3d(x1, y0, z1); GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y1, z0);
        GL11.glVertex3d(x0, y1, z0); GL11.glVertex3d(x1, y1, z0); GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x0, y1, z1);
        GL11.glVertex3d(x0, y0, z1); GL11.glVertex3d(x1, y0, z1); GL11.glVertex3d(x1, y0, z0); GL11.glVertex3d(x0, y0, z0);
        GL11.glEnd();
    }

    private static void drawEdges(double x0, double y0, double z0, double x1, double y1, double z1) {
        GL11.glBegin(GL11.GL_LINES);
        edge(x0,y0,z0,x1,y0,z0); edge(x1,y0,z0,x1,y0,z1); edge(x1,y0,z1,x0,y0,z1); edge(x0,y0,z1,x0,y0,z0);
        edge(x0,y1,z0,x1,y1,z0); edge(x1,y1,z0,x1,y1,z1); edge(x1,y1,z1,x0,y1,z1); edge(x0,y1,z1,x0,y1,z0);
        edge(x0,y0,z0,x0,y1,z0); edge(x1,y0,z0,x1,y1,z0); edge(x1,y0,z1,x1,y1,z1); edge(x0,y0,z1,x0,y1,z1);
        GL11.glEnd();
    }

    private static void edge(double ax,double ay,double az,double bx,double by,double bz) {
        GL11.glVertex3d(ax,ay,az); GL11.glVertex3d(bx,by,bz);
    }

    private static final class Preview {
        final AxisAlignedBB box;
        final boolean blocked;
        Preview(AxisAlignedBB box, boolean blocked) { this.box = box; this.blocked = blocked; }
    }
}
