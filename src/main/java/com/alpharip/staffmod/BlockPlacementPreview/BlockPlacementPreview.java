package com.alpharip.staffmod.BlockPlacementPreview;

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
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import java.util.List;

/**
 * Renders a translucent placement preview for the ItemBlock held by the player.
 *
 * Register this instance on MinecraftForge.EVENT_BUS to enable rendering.
 */
public final class BlockPlacementPreview {
    private static final BlockPlacementPreview INSTANCE = new BlockPlacementPreview();

    private BlockPlacementPreview() {
    }

    public static BlockPlacementPreview getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event == null || !BlockPlacementPreviewSettings.isEnabled()
                || minecraft == null || minecraft.theWorld == null
                || minecraft.thePlayer == null) {
            return;
        }

        ItemStack heldStack = minecraft.thePlayer.getHeldItem();
        MovingObjectPosition target = minecraft.objectMouseOver;
        PreviewData preview = createPreview(minecraft.theWorld, minecraft.thePlayer, target, heldStack);
        if (preview == null) {
            return;
        }

        float opacity = BlockPlacementPreviewSettings.getOpacity();
        if (opacity <= 0.0F) {
            return;
        }

        drawPreview(minecraft.getRenderManager(), preview, opacity, BlockPlacementPreviewSettings.isShowCollision());
    }

    private PreviewData createPreview(World world, EntityPlayer player,
                                      MovingObjectPosition target, ItemStack heldStack) {
        if (target == null
                || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || target.getBlockPos() == null
                || target.sideHit == null
                || target.hitVec == null
                || heldStack == null
                || !(heldStack.getItem() instanceof ItemBlock)) {
            return null;
        }

        ItemBlock itemBlock = (ItemBlock) heldStack.getItem();
        Block block = itemBlock.getBlock();
        BlockPos clickedPosition = target.getBlockPos();
        IBlockState clickedState = world.getBlockState(clickedPosition);

        boolean replacingBlock = clickedState.getBlock().isReplaceable(world, clickedPosition);
        BlockPos placementPosition = replacingBlock
                ? clickedPosition
                : clickedPosition.offset(target.sideHit);

        int metadata = itemBlock.getMetadata(heldStack.getMetadata());
        IBlockState placementState;
        try {
            placementState = block.getStateFromMeta(metadata);
            IBlockState calculatedState = block.onBlockPlaced(
                    world,
                    placementPosition,
                    target.sideHit,
                    clamp(target.hitVec.xCoord - clickedPosition.getX()),
                    clamp(target.hitVec.yCoord - clickedPosition.getY()),
                    clamp(target.hitVec.zCoord - clickedPosition.getZ()),
                    metadata,
                    player
            );
            if (calculatedState != null) {
                placementState = calculatedState;
            }
        } catch (RuntimeException invalidPlacementState) {
            return null;
        }

        if (placementState == null) {
            return null;
        }

        AxisAlignedBB collisionBox;
        try {
            collisionBox = block.getCollisionBoundingBox(world, placementPosition, placementState);
        } catch (RuntimeException invalidBounds) {
            collisionBox = null;
        }

        // Blocks without collision bounds still receive a useful one-block placement preview.
        AxisAlignedBB renderBox = collisionBox != null
                ? collisionBox
                : AxisAlignedBB.fromBounds(
                        placementPosition.getX(), placementPosition.getY(), placementPosition.getZ(),
                        placementPosition.getX() + 1.0D,
                        placementPosition.getY() + 1.0D,
                        placementPosition.getZ() + 1.0D
                );

        boolean blocked = !block.canPlaceBlockAt(world, placementPosition);
        if (!blocked && collisionBox != null) {
            List<AxisAlignedBB> collisions = world.getCollidingBoundingBoxes(player, collisionBox);
            if (collisions != null && !collisions.isEmpty()) {
                blocked = containsBlockingCollision(collisions, clickedPosition, replacingBlock, clickedState, world);
            }
        }

        return new PreviewData(renderBox, blocked);
    }

    private boolean containsBlockingCollision(List<AxisAlignedBB> collisions,
                                              BlockPos clickedPosition,
                                              boolean replacingBlock,
                                              IBlockState clickedState,
                                              World world) {
        AxisAlignedBB replacedBlockBox = null;
        if (replacingBlock) {
            try {
                replacedBlockBox = clickedState.getBlock().getCollisionBoundingBox(
                        world, clickedPosition, clickedState);
            } catch (RuntimeException ignored) {
                // A missing collision box does not prevent checking the remaining collisions.
            }
        }

        for (AxisAlignedBB collision : collisions) {
            if (collision == null) {
                continue;
            }
            if (replacedBlockBox != null && sameBounds(collision, replacedBlockBox)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static boolean sameBounds(AxisAlignedBB first, AxisAlignedBB second) {
        return first.minX == second.minX && first.minY == second.minY && first.minZ == second.minZ
                && first.maxX == second.maxX && first.maxY == second.maxY && first.maxZ == second.maxZ;
    }

    private static float clamp(double value) {
        return (float) Math.max(0.0D, Math.min(1.0D, value));
    }

    private void drawPreview(RenderManager renderManager, PreviewData preview, float opacity, boolean showCollision) {
        if (renderManager == null || preview == null || preview.box == null) {
            return;
        }

        AxisAlignedBB box = preview.box;
        double minX = box.minX - renderManager.viewerPosX;
        double minY = box.minY - renderManager.viewerPosY;
        double minZ = box.minZ - renderManager.viewerPosZ;
        double maxX = box.maxX - renderManager.viewerPosX;
        double maxY = box.maxY - renderManager.viewerPosY;
        double maxZ = box.maxZ - renderManager.viewerPosZ;

        float red = showCollision && preview.blocked ? 1.0F : 0.25F;
        float green = showCollision && preview.blocked ? 0.18F : 0.9F;
        float blue = showCollision && preview.blocked ? 0.18F : 0.45F;
        float alpha = Math.max(0.05F, Math.min(1.0F, opacity));

        RenderState previousState = RenderState.capture();
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
            previousState.restore();
        }
    }

    private static void drawFaces(double x0, double y0, double z0,
                                  double x1, double y1, double z1) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x0, y0, z0); GL11.glVertex3d(x1, y0, z0);
        GL11.glVertex3d(x1, y1, z0); GL11.glVertex3d(x0, y1, z0);
        GL11.glVertex3d(x1, y0, z1); GL11.glVertex3d(x0, y0, z1);
        GL11.glVertex3d(x0, y1, z1); GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x0, y0, z1); GL11.glVertex3d(x0, y0, z0);
        GL11.glVertex3d(x0, y1, z0); GL11.glVertex3d(x0, y1, z1);
        GL11.glVertex3d(x1, y0, z0); GL11.glVertex3d(x1, y0, z1);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y1, z0);
        GL11.glVertex3d(x0, y1, z0); GL11.glVertex3d(x1, y1, z0);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x0, y1, z1);
        GL11.glVertex3d(x0, y0, z1); GL11.glVertex3d(x1, y0, z1);
        GL11.glVertex3d(x1, y0, z0); GL11.glVertex3d(x0, y0, z0);
        GL11.glEnd();
    }

    private static void drawEdges(double x0, double y0, double z0,
                                  double x1, double y1, double z1) {
        GL11.glBegin(GL11.GL_LINES);
        edge(x0, y0, z0, x1, y0, z0);
        edge(x1, y0, z0, x1, y0, z1);
        edge(x1, y0, z1, x0, y0, z1);
        edge(x0, y0, z1, x0, y0, z0);
        edge(x0, y1, z0, x1, y1, z0);
        edge(x1, y1, z0, x1, y1, z1);
        edge(x1, y1, z1, x0, y1, z1);
        edge(x0, y1, z1, x0, y1, z0);
        edge(x0, y0, z0, x0, y1, z0);
        edge(x1, y0, z0, x1, y1, z0);
        edge(x1, y0, z1, x1, y1, z1);
        edge(x0, y0, z1, x0, y1, z1);
        GL11.glEnd();
    }

    private static void edge(double ax, double ay, double az,
                             double bx, double by, double bz) {
        GL11.glVertex3d(ax, ay, az);
        GL11.glVertex3d(bx, by, bz);
    }

    private static final class PreviewData {
        private final AxisAlignedBB box;
        private final boolean blocked;

        private PreviewData(AxisAlignedBB box, boolean blocked) {
            this.box = box;
            this.blocked = blocked;
        }
    }

    private static final class RenderState {
        private final boolean blend;
        private final boolean texture;
        private final boolean lighting;
        private final boolean depth;
        private final boolean cull;
        private final boolean depthMask;
        private final int blendSourceRgb;
        private final int blendDestinationRgb;
        private final int blendSourceAlpha;
        private final int blendDestinationAlpha;
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;
        private final float lineWidth;

        private RenderState(boolean blend, boolean texture, boolean lighting, boolean depth,
                            boolean cull, boolean depthMask, int blendSourceRgb,
                            int blendDestinationRgb, int blendSourceAlpha, int blendDestinationAlpha,
                            float red, float green, float blue, float alpha, float lineWidth) {
            this.blend = blend;
            this.texture = texture;
            this.lighting = lighting;
            this.depth = depth;
            this.cull = cull;
            this.depthMask = depthMask;
            this.blendSourceRgb = blendSourceRgb;
            this.blendDestinationRgb = blendDestinationRgb;
            this.blendSourceAlpha = blendSourceAlpha;
            this.blendDestinationAlpha = blendDestinationAlpha;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.lineWidth = lineWidth;
        }

        private static RenderState capture() {
            // LWJGL 2 requires 16-element buffers for these state queries, even when fewer values are returned.
            FloatBuffer color = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
            FloatBuffer width = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_LINE_WIDTH, width);
            ByteBuffer depthWrite = BufferUtils.createByteBuffer(16);
            GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK, depthWrite);

            return new RenderState(
                    GL11.glIsEnabled(GL11.GL_BLEND),
                    GL11.glIsEnabled(GL11.GL_TEXTURE_2D),
                    GL11.glIsEnabled(GL11.GL_LIGHTING),
                    GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
                    GL11.glIsEnabled(GL11.GL_CULL_FACE),
                    depthWrite.get(0) != 0,
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),
                    GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),
                    GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),
                    color.get(0), color.get(1), color.get(2), color.get(3),
                    width.get(0)
            );
        }

        private void restore() {
            setEnabled(GL11.GL_BLEND, blend);
            setEnabled(GL11.GL_TEXTURE_2D, texture);
            setEnabled(GL11.GL_LIGHTING, lighting);
            setEnabled(GL11.GL_DEPTH_TEST, depth);
            setEnabled(GL11.GL_CULL_FACE, cull);
            GlStateManager.depthMask(depthMask);
            GlStateManager.tryBlendFuncSeparate(
                    blendSourceRgb, blendDestinationRgb, blendSourceAlpha, blendDestinationAlpha);
            GlStateManager.color(red, green, blue, alpha);
            GL11.glLineWidth(lineWidth);
        }

        private static void setEnabled(int capability, boolean enabled) {
            if (capability == GL11.GL_BLEND) {
                if (enabled) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
            } else if (capability == GL11.GL_TEXTURE_2D) {
                if (enabled) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
            } else if (capability == GL11.GL_LIGHTING) {
                if (enabled) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
            } else if (capability == GL11.GL_DEPTH_TEST) {
                if (enabled) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
            } else if (capability == GL11.GL_CULL_FACE) {
                if (enabled) GlStateManager.enableCull(); else GlStateManager.disableCull();
            }
        }
    }
}