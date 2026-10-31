package com.alpharip.staffmod.BlockPlacementPreview;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

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

        AxisAlignedBB collisionBox;
        try {
            collisionBox = block.getCollisionBoundingBox(world, placePos, state);
        } catch (RuntimeException invalidBounds) {
            collisionBox = null;
        }

        // Collision boxes are ideal when present. For non-solid blocks, derive visual bounds
        // from the vanilla baked model so plants, torches, rails, and similar blocks preview
        // as their actual shape instead of an unrelated full cube.
        AxisAlignedBB displayBox = collisionBox != null
                ? collisionBox : getBakedModelBounds(world, placePos, state);
        if (displayBox == null) {
            displayBox = AxisAlignedBB.getBoundingBox(placePos.getX(), placePos.getY(), placePos.getZ(),
                    placePos.getX() + 1.0D, placePos.getY() + 1.0D, placePos.getZ() + 1.0D);
        }

        boolean blocked = !block.canPlaceBlockAt(world, placePos);
        if (!blocked && collisionBox != null) {
            List<AxisAlignedBB> collisions = world.getCollidingBoundingBoxes(player, collisionBox);
            blocked = collisions != null && !collisions.isEmpty();
        }
        if (!blocked && collisionBox != null) {
            List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, collisionBox);
            if (entities != null) {
                for (Entity entity : entities) {
                    AxisAlignedBB entityBox = entity == null ? null : entity.getEntityBoundingBox();
                    if (entityBox != null && entityBox.intersectsWith(collisionBox)) {
                        blocked = true;
                        break;
                    }
                }
            }
        }
        return new Preview(displayBox, blocked);
    }

    private static AxisAlignedBB getBakedModelBounds(World world, BlockPos pos, IBlockState state) {
        try {
            BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBakedModel model = dispatcher.getModelFromBlockState(state, world, pos);
            if (model == null) return null;

            double[] bounds = {
                    Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY
            };
            for (EnumFacing face : EnumFacing.values()) {
                for (BakedQuad quad : model.getFaceQuads(face)) {
                    includeQuad(quad, bounds);
                }
            }
            for (BakedQuad quad : model.getGeneralQuads()) {
                includeQuad(quad, bounds);
            }

            if (Double.isInfinite(bounds[0])) return null;
            return AxisAlignedBB.getBoundingBox(
                    pos.getX() + bounds[0], pos.getY() + bounds[1], pos.getZ() + bounds[2],
                    pos.getX() + bounds[3], pos.getY() + bounds[4], pos.getZ() + bounds[5]);
        } catch (RuntimeException unavailableModel) {
            return null;
        }
    }

    /** Baked block vertex data uses the BLOCK vertex format: seven integers per vertex. */
    private static void includeQuad(BakedQuad quad, double[] bounds) {
        int[] vertices = quad.getVertexData();
        for (int i = 0; i + 2 < vertices.length; i += 7) {
            double x = Float.intBitsToFloat(vertices[i]);
            double y = Float.intBitsToFloat(vertices[i + 1]);
            double z = Float.intBitsToFloat(vertices[i + 2]);
            if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)
                    || Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) continue;
            bounds[0] = Math.min(bounds[0], x);
            bounds[1] = Math.min(bounds[1], y);
            bounds[2] = Math.min(bounds[2], z);
            bounds[3] = Math.max(bounds[3], x);
            bounds[4] = Math.max(bounds[4], y);
            bounds[5] = Math.max(bounds[5], z);
        }
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
        RenderState previous = RenderState.capture();

        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();

            GlStateManager.color(red, green, blue, alpha);
            drawFaces(minX, minY, minZ, maxX, maxY, maxZ);

            GL11.glLineWidth(2.0F);
            GlStateManager.color(red, green, blue, Math.min(1.0F, alpha + 0.35F));
            drawEdges(minX, minY, minZ, maxX, maxY, maxZ);
        } finally {
            GlStateManager.popMatrix();
            previous.restore();
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

    private static final class RenderState {
        final boolean blend, texture, lighting, depth, cull, depthMask;
        final int blendSourceRgb, blendDestinationRgb, blendSourceAlpha, blendDestinationAlpha;
        final float red, green, blue, alpha, lineWidth;

        private RenderState(boolean blend, boolean texture, boolean lighting, boolean depth,
                            boolean cull, boolean depthMask, int sourceRgb, int destinationRgb,
                            int sourceAlpha, int destinationAlpha, float red, float green,
                            float blue, float alpha, float lineWidth) {
            this.blend=blend; this.texture=texture; this.lighting=lighting; this.depth=depth;
            this.cull=cull; this.depthMask=depthMask;
            this.blendSourceRgb=sourceRgb; this.blendDestinationRgb=destinationRgb;
            this.blendSourceAlpha=sourceAlpha; this.blendDestinationAlpha=destinationAlpha;
            this.red=red; this.green=green; this.blue=blue; this.alpha=alpha; this.lineWidth=lineWidth;
        }

        static RenderState capture() {
            FloatBuffer color=BufferUtils.createFloatBuffer(4);
            GL11.glGetFloat(GL11.GL_CURRENT_COLOR,color);
            FloatBuffer line=BufferUtils.createFloatBuffer(1);
            GL11.glGetFloat(GL11.GL_LINE_WIDTH,line);
            ByteBuffer depthWrite=BufferUtils.createByteBuffer(1);
            GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK,depthWrite);
            return new RenderState(GL11.glIsEnabled(GL11.GL_BLEND),GL11.glIsEnabled(GL11.GL_TEXTURE_2D),
                    GL11.glIsEnabled(GL11.GL_LIGHTING),GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
                    GL11.glIsEnabled(GL11.GL_CULL_FACE),depthWrite.get(0)!=0,
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),
                    color.get(0),color.get(1),color.get(2),color.get(3),line.get(0));
        }

        void restore() {
            setEnabled(GL11.GL_BLEND,blend);
            setEnabled(GL11.GL_TEXTURE_2D,texture);
            setEnabled(GL11.GL_LIGHTING,lighting);
            setEnabled(GL11.GL_DEPTH_TEST,depth);
            setEnabled(GL11.GL_CULL_FACE,cull);
            GlStateManager.depthMask(depthMask);
            GlStateManager.tryBlendFuncSeparate(blendSourceRgb,blendDestinationRgb,blendSourceAlpha,blendDestinationAlpha);
            GlStateManager.color(red,green,blue,alpha);
            GL11.glLineWidth(lineWidth);
        }

        private static void setEnabled(int capability, boolean enabled) {
            if (capability==GL11.GL_BLEND) { if(enabled)GlStateManager.enableBlend();else GlStateManager.disableBlend(); }
            else if (capability==GL11.GL_TEXTURE_2D) { if(enabled)GlStateManager.enableTexture2D();else GlStateManager.disableTexture2D(); }
            else if (capability==GL11.GL_LIGHTING) { if(enabled)GlStateManager.enableLighting();else GlStateManager.disableLighting(); }
            else if (capability==GL11.GL_DEPTH_TEST) { if(enabled)GlStateManager.enableDepth();else GlStateManager.disableDepth(); }
            else if (capability==GL11.GL_CULL_FACE) { if(enabled)GlStateManager.enableCull();else GlStateManager.disableCull(); }
        }
    }

    private static final class Preview {
        final AxisAlignedBB box;
        final boolean blocked;
        Preview(AxisAlignedBB box, boolean blocked) { this.box = box; this.blocked = blocked; }
    }
}
