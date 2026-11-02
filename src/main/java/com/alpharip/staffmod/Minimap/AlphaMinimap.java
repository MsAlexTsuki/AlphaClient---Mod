package com.alpharip.staffmod.Minimap;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.waypoints.StaffWaypoint;
import com.alpharip.staffmod.waypoints.WaypointManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.List;

/**
 * Client-side terrain minimap. It samples only already-loaded client chunks;
 * it never requests or generates chunks to populate the map.
 */
public final class AlphaMinimap {
    private static final AlphaMinimap INSTANCE = new AlphaMinimap();
    private int refreshTicks;
    private int[][] cachedColors = new int[0][0];
    private int cachedCenterX = Integer.MIN_VALUE;
    private int cachedCenterZ = Integer.MIN_VALUE;
    private int cachedDimension = Integer.MIN_VALUE;
    private int cachedRange = -1;
    private boolean cachedRound;

    private AlphaMinimap() { }
    public static AlphaMinimap getInstance() { return INSTANCE; }

    public static boolean isEnabled() {
        AlphaModule module = AlphaModuleRegistry.find("minimap");
        return module != null && module.isImplemented() && module.isEnabled();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null
                || mc.gameSettings.hideGUI || mc.gameSettings.showDebugInfo) return;

        ScaledResolution scaled = new ScaledResolution(mc);
        int size = MinimapSettings.size();
        int offset = MinimapSettings.offset();
        String corner = MinimapSettings.corner();
        int left = corner.endsWith("left") ? offset : scaled.getScaledWidth() - size - offset;
        int coordinateHeight = mc.fontRendererObj.FONT_HEIGHT + 4;
        int top = corner.startsWith("bottom")
                ? scaled.getScaledHeight() - size - offset - coordinateHeight : offset;
        int centerX = left + size / 2;
        int centerY = top + size / 2;

        GlStateManager.pushMatrix();
        try {
            drawFrame(left, top, size, centerX, centerY);
            drawTerrain(mc, left, top, size, centerX, centerY);
            drawEntities(mc, centerX, centerY, size);
            drawWaypoints(mc, centerX, centerY, size);
            drawBorder(left, top, size);
            Gui.drawRect(centerX - 2, centerY - 2, centerX + 3, centerY + 3, 0xFFFFFFFF);
            mc.fontRendererObj.drawStringWithShadow("N", centerX - 3, top + 3, 0xFFE6E8ED);
            String coords = MathHelper.floor_double(mc.thePlayer.posX) + " "
                    + MathHelper.floor_double(mc.thePlayer.posY) + " "
                    + MathHelper.floor_double(mc.thePlayer.posZ);
            int color = 0xFF000000 | (MinimapSettings.coordinateColor() & 0xFFFFFF);
            mc.fontRendererObj.drawStringWithShadow(coords,
                    centerX - mc.fontRendererObj.getStringWidth(coords) / 2,
                    top + size + 3, color);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void drawBorder(int left, int top, int size) {
        if (MinimapSettings.round()) return;
        Gui.drawRect(left, top, left + size, top + 1, 0xFFFFC83D);
        Gui.drawRect(left, top, left + 1, top + size, 0x66555A63);
        Gui.drawRect(left + size - 1, top, left + size, top + size, 0x66555A63);
        Gui.drawRect(left, top + size - 1, left + size, top + size, 0xFF555A63);
    }

    private void drawFrame(int left, int top, int size, int cx, int cy) {
        int background = 0xD9080A0F;
        if (MinimapSettings.round()) {
            int radius = size / 2;
            for (int y = -radius; y <= radius; y++) {
                int half = (int) Math.sqrt(Math.max(0, radius * radius - y * y));
                Gui.drawRect(cx - half, cy + y, cx + half + 1, cy + y + 1, background);
            }
        } else {
            Gui.drawRect(left, top, left + size, top + size, background);
            Gui.drawRect(left, top, left + size, top + 1, 0xFFFFC83D);
            Gui.drawRect(left, top + size - 1, left + size, top + size, 0xFF555A63);
        }
    }

    private void drawTerrain(Minecraft mc, int left, int top, int size, int cx, int cy) {
        int playerX = MathHelper.floor_double(mc.thePlayer.posX);
        int playerZ = MathHelper.floor_double(mc.thePlayer.posZ);
        int radius = size / 2 - 3;
        int range = MinimapSettings.range();
        boolean round = MinimapSettings.round();
        if (++refreshTicks >= 10 || cachedCenterX != playerX || cachedCenterZ != playerZ
                || cachedDimension != mc.thePlayer.dimension || cachedColors.length != size
                || cachedRange != range || cachedRound != round) {
            refreshTicks = 0;
            cachedCenterX = playerX; cachedCenterZ = playerZ;
            cachedDimension = mc.thePlayer.dimension;
            cachedRange = range;
            cachedRound = round;
            cachedColors = new int[size][size];
            for (int py = 0; py < size; py++) {
                for (int px = 0; px < size; px++) {
                    int dx = px - size / 2;
                    int dz = py - size / 2;
                    if (MinimapSettings.round() && dx * dx + dz * dz > radius * radius) continue;
                    double rx = (dx * range / (double) radius);
                    double rz = (dz * range / (double) radius);
                    int worldX = playerX + (int) Math.round(rx);
                    int worldZ = playerZ + (int) Math.round(rz);
                    cachedColors[py][px] = sampleLoadedSurface(mc, worldX, worldZ, MathHelper.floor_double(mc.thePlayer.posY));
                }
            }
        }
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                int color = cachedColors[py][px];
                if (color == 0) continue;
                int dx = px - size / 2, dz = py - size / 2;
                if (MinimapSettings.round() && dx * dx + dz * dz > radius * radius) continue;
                float red = ((color >> 16) & 255) / 255.0F;
                float green = ((color >> 8) & 255) / 255.0F;
                float blue = (color & 255) / 255.0F;
                double x1 = left + px, y1 = top + py, x2 = x1 + 1, y2 = y1 + 1;
                renderer.pos(x1, y2, 0).color(red, green, blue, 1.0F).endVertex();
                renderer.pos(x2, y2, 0).color(red, green, blue, 1.0F).endVertex();
                renderer.pos(x2, y1, 0).color(red, green, blue, 1.0F).endVertex();
                renderer.pos(x1, y1, 0).color(red, green, blue, 1.0F).endVertex();
            }
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        // Cardinal crosshair remains visible above the terrain layer.
        Gui.drawRect(cx - 1, cy - radius, cx + 1, cy + radius, 0x334B515D);
        Gui.drawRect(cx - radius, cy - 1, cx + radius, cy + 1, 0x334B515D);
    }

    private int sampleLoadedSurface(Minecraft mc, int x, int z, int playerY) {
        if (!mc.theWorld.isBlockLoaded(new BlockPos(x, 0, z))) return 0;
        Chunk chunk = mc.theWorld.getChunkFromBlockCoords(new BlockPos(x, 0, z));
        if (chunk == null || !chunk.isLoaded()) return 0;
        int surfaceY = chunk.getHeightValue(x & 15, z & 15);
        if (surfaceY <= 0) return 0xFF26303A;

        int sampleY = Math.min(255, surfaceY - 1);
        // When the player is underground, map the local cave floor instead of
        // continuing to show the distant surface above the cave.
        if (playerY < surfaceY - 4) {
            int minimumY = Math.max(1, playerY - 8);
            for (int scanY = Math.min(255, playerY); scanY >= minimumY; scanY--) {
                BlockPos candidatePos = new BlockPos(x, scanY, z);
                IBlockState candidate = chunk.getBlockState(candidatePos);
                if (candidate.getBlock().getMaterial().blocksMovement()) {
                    sampleY = scanY;
                    break;
                }
            }
        }

        BlockPos pos = new BlockPos(x, sampleY, z);
        IBlockState state = chunk.getBlockState(pos);
        int rgb = state.getBlock().getMapColor(state).colorValue;
        if (state.getBlock().getMaterial().isLiquid()) rgb = 0xFF315D9A;
        else rgb = shade(rgb, sampleY);
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    private int shade(int rgb, int height) {
        float factor = 0.82F + Math.min(0.28F, Math.max(0, height) / 900.0F);
        int r = Math.min(255, (int) (((rgb >> 16) & 255) * factor));
        int g = Math.min(255, (int) (((rgb >> 8) & 255) * factor));
        int b = Math.min(255, (int) ((rgb & 255) * factor));
        return (r << 16) | (g << 8) | b;
    }

    private void drawEntities(Minecraft mc, int cx, int cy, int size) {
        int range = MinimapSettings.range();
        int radius = size / 2 - 5;
        for (Object object : mc.theWorld.loadedEntityList) {
            if (!(object instanceof Entity) || object == mc.thePlayer) continue;
            Entity entity = (Entity) object;
            if (!(entity instanceof EntityLivingBase) || entity.isDead) continue;
            double dx = entity.posX - mc.thePlayer.posX;
            double dz = entity.posZ - mc.thePlayer.posZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > range) continue;
            int px = cx + (int) Math.round(dx / range * radius);
            int py = cy + (int) Math.round(dz / range * radius);
            int mapDx = px - cx, mapDy = py - cy;
            if (MinimapSettings.round() && mapDx * mapDx + mapDy * mapDy > radius * radius) continue;
            int color = entity instanceof EntityPlayer ? 0xFFFF5555 : 0xFFFFAA44;
            Gui.drawRect(px - 2, py - 2, px + 3, py + 3, color);
        }
    }

    private void drawWaypoints(Minecraft mc, int cx, int cy, int size) {
        int range = MinimapSettings.range();
        int radius = size / 2 - 7;
        List<StaffWaypoint> points = WaypointManager.getInstance().getForCurrentWorld();
        for (StaffWaypoint point : points) {
            double dx = point.getX() - mc.thePlayer.posX;
            double dz = point.getZ() - mc.thePlayer.posZ;
            if (Math.sqrt(dx * dx + dz * dz) > range) continue;
            int px = cx + (int) Math.round(dx / range * radius);
            int py = cy + (int) Math.round(dz / range * radius);
            int mapDx = px - cx, mapDy = py - cy;
            if (MinimapSettings.round() && mapDx * mapDx + mapDy * mapDy > radius * radius) continue;
            Gui.drawRect(px - 2, py - 2, px + 3, py + 3, point.getColor());
        }
    }
}
