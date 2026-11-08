package com.alpharip.staffmod.BlockPlacementPreview;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.EnumFacing;
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

        drawPreview(minecraft, preview, opacity, BlockPlacementPreviewSettings.isShowCollision());
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

        boolean blocked = !block.canPlaceBlockAt(world, placementPosition);
        if (!blocked && collisionBox != null) {
            List<AxisAlignedBB> collisions = world.getCollidingBoundingBoxes(player, collisionBox);
            if (collisions != null && !collisions.isEmpty()) {
                blocked = containsBlockingCollision(collisions, clickedPosition, replacingBlock, clickedState, world);
            }
        }

        return new PreviewData(placementState, placementPosition, collisionBox, blocked);
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

    private void drawPreview(Minecraft minecraft, PreviewData preview, float opacity, boolean showCollision) {
        if (minecraft == null || minecraft.theWorld == null || preview == null || preview.state == null) {
            return;
        }

        RenderManager renderManager = minecraft.getRenderManager();
        if (renderManager == null) {
            return;
        }

        RenderState previousState = RenderState.capture();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();

            drawBlockModel(minecraft, preview, opacity, renderManager);

            if (showCollision && preview.collisionBox != null) {
                AxisAlignedBB box = preview.collisionBox;
                double minX = box.minX - renderManager.viewerPosX;
                double minY = box.minY - renderManager.viewerPosY;
                double minZ = box.minZ - renderManager.viewerPosZ;
                double maxX = box.maxX - renderManager.viewerPosX;
                double maxY = box.maxY - renderManager.viewerPosY;
                double maxZ = box.maxZ - renderManager.viewerPosZ;

                GlStateManager.disableTexture2D();
                GlStateManager.disableLighting();
                GL11.glLineWidth(2.0F);
                if (preview.blocked) {
                    GlStateManager.color(1.0F, 0.12F, 0.12F, 0.95F);
                } else {
                    GlStateManager.color(0.15F, 0.85F, 1.0F, 0.95F);
                }
                drawEdges(minX, minY, minZ, maxX, maxY, maxZ);
            }
        } finally {
            GlStateManager.popMatrix();
            previousState.restore();
        }
    }

    /**
     * Draws the baked Minecraft block model at the target position. The collision
     * outline is rendered separately so visual geometry and physical bounds remain distinct.
     */
    private void drawBlockModel(Minecraft minecraft, PreviewData preview, float opacity,
                                RenderManager renderManager) {
        Block block = preview.state.getBlock();
        if (block.getRenderType() != 3) {
            return;
        }

        IBakedModel model;
        try {
            model = minecraft.getBlockRendererDispatcher().getModelFromBlockState(
                    preview.state, minecraft.theWorld, preview.position);
        } catch (RuntimeException invalidModel) {
            return;
        }
        if (model == null) {
            return;
        }

        minecraft.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, Math.max(0.05F, Math.min(1.0F, opacity)));

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(
                    preview.position.getX() - renderManager.viewerPosX,
                    preview.position.getY() - renderManager.viewerPosY,
                    preview.position.getZ() - renderManager.viewerPosZ);

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer renderer = tessellator.getWorldRenderer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

            for (EnumFacing facing : EnumFacing.values()) {
                renderModelQuads(renderer, model.getFaceQuads(facing), block, minecraft, preview, opacity);
            }
            renderModelQuads(renderer, model.getGeneralQuads(), block, minecraft, preview, opacity);

            tessellator.draw();
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void renderModelQuads(WorldRenderer renderer, List<BakedQuad> quads, Block block,
                                  Minecraft minecraft, PreviewData preview, float opacity) {
        int alpha = (int) (Math.max(0.05F, Math.min(1.0F, opacity)) * 255.0F);
        for (BakedQuad quad : quads) {
            int color = 0x00FFFFFF;
            if (quad.hasTintIndex()) {
                color = block.colorMultiplier(minecraft.theWorld, preview.position, quad.getTintIndex());
            }
            color = (alpha << 24) | (color & 0x00FFFFFF);

            renderer.addVertexData(quad.getVertexData());
            renderer.putColor4(color);
            if (quad.getFace() != null) {
                net.minecraft.util.Vec3i normal = quad.getFace().getDirectionVec();
                renderer.putNormal((float) normal.getX(), (float) normal.getY(), (float) normal.getZ());
            }
        }
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
        private final IBlockState state;
        private final BlockPos position;
        private final AxisAlignedBB collisionBox;
        private final boolean blocked;

        private PreviewData(IBlockState state, BlockPos position, AxisAlignedBB collisionBox, boolean blocked) {
            this.state = state;
            this.position = position;
            this.collisionBox = collisionBox;
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
        private final int textureBinding;

        private RenderState(boolean blend, boolean texture, boolean lighting, boolean depth,
                            boolean cull, boolean depthMask, int blendSourceRgb,
                            int blendDestinationRgb, int blendSourceAlpha, int blendDestinationAlpha,
                            float red, float green, float blue, float alpha, float lineWidth,
                            int textureBinding) {
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
            this.textureBinding = textureBinding;
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
                    width.get(0),
                    GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
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
            GlStateManager.bindTexture(textureBinding);
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