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
        int coordinateHeight = mc.fontRendererObj.FONT_HEIGHT * 2 + 9;
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
            drawCompass(left, top, size, centerX, centerY);
            drawPlayerArrow(centerX, centerY, mc.thePlayer.rotationYaw, 6);
            drawCoordinatePanel(mc, left, top, size);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    /** Draws the same live map at a larger size inside the fullscreen map screen. */
    public void drawFullscreenMap(int screenWidth, int screenHeight) {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) return;

        int size = Math.min(320, Math.min(screenWidth - 48, screenHeight - 92));
        size = Math.max(112, size);
        int left = (screenWidth - size) / 2;
        int top = (screenHeight - size) / 2 + 8;
        int centerX = left + size / 2;
        int centerY = top + size / 2;
        int mapCenterX = MathHelper.floor_double(mc.thePlayer.posX) + MinimapSettings.panX();
        int mapCenterZ = MathHelper.floor_double(mc.thePlayer.posZ) + MinimapSettings.panZ();
        int range = MinimapSettings.fullscreenRange();
        GlStateManager.pushMatrix();
        try {
            drawFrame(left, top, size, centerX, centerY);
            drawTerrain(mc, left, top, size, centerX, centerY, mapCenterX, mapCenterZ, range);
            drawEntities(mc, centerX, centerY, size, mapCenterX, mapCenterZ, range);
            drawWaypoints(mc, centerX, centerY, size, mapCenterX, mapCenterZ, range);
            drawBorder(left, top, size);
            int playerRadius = size / 2 - 5;
            int playerX = centerX + (int)Math.round((mc.thePlayer.posX - mapCenterX) / range * playerRadius);
            int playerY = centerY + (int)Math.round((mc.thePlayer.posZ - mapCenterZ) / range * playerRadius);
            drawCompass(left, top, size, centerX, centerY);
            drawPlayerArrow(playerX, playerY, mc.thePlayer.rotationYaw, 7);
            String coords = "Center " + mapCenterX + " " + MathHelper.floor_double(mc.thePlayer.posY) + " " + mapCenterZ
                    + "  |  Zoom " + range + " blocks";
            drawInfoPanel(left, top + size + 3, size, coords, nearestWaypointText(mc));
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void drawBorder(int left, int top, int size) {
        if (MinimapSettings.round()) {
            int cx = left + size / 2;
            int cy = top + size / 2;
            int radius = size / 2 - 1;
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GL11.glLineWidth(1.5F);
            GL11.glColor4f(1.0F, 0.78F, 0.24F, 0.95F);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            for (int i = 0; i < 64; i++) {
                double angle = Math.PI * 2.0D * i / 64.0D;
                GL11.glVertex2d(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius);
            }
            GL11.glEnd();
            GL11.glLineWidth(1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
            return;
        }
        Gui.drawRect(left - 1, top - 1, left + size + 1, top, 0xAA000000);
        Gui.drawRect(left - 1, top + size, left + size + 1, top + size + 1, 0xAA000000);
        Gui.drawRect(left - 1, top, left, top + size, 0xAA000000);
        Gui.drawRect(left + size, top, left + size + 1, top + size, 0xAA000000);
        Gui.drawRect(left, top, left + size, top + 2, 0xFFFFC83D);
        Gui.drawRect(left, top + size - 1, left + size, top + size, 0xAA080A0F);
        Gui.drawRect(left, top, left + 1, top + size, 0x66555A63);
        Gui.drawRect(left + size - 1, top, left + size, top + size, 0x66555A63);
    }

    private void drawCompass(int left, int top, int size, int cx, int cy) {
        int text = 0xFFE8EAF0;
        int muted = 0xFF9CA3AF;
        drawCompassLabel("N", cx, top + 4, text);
        drawCompassLabel("E", left + size - 8, cy - 3, muted);
        drawCompassLabel("S", cx, top + size - 10, muted);
        drawCompassLabel("W", left + 3, cy - 3, muted);
    }

    private void drawCompassLabel(String label, int x, int y, int color) {
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(label, x, y, color);
    }

    private void drawPlayerArrow(int x, int y, float yaw, int radius) {
        double angle = Math.toRadians(yaw);
        double forwardX = -Math.sin(angle);
        double forwardY = Math.cos(angle);
        double rightX = Math.cos(angle);
        double rightY = Math.sin(angle);
        double tipX = x + forwardX * radius;
        double tipY = y + forwardY * radius;
        double baseX = x - forwardX * radius * 0.72D;
        double baseY = y - forwardY * radius * 0.72D;
        double wing = radius * 0.68D;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glColor4f(0.035F, 0.045F, 0.065F, 1.0F);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2d(tipX, tipY);
        GL11.glVertex2d(baseX + rightX * wing, baseY + rightY * wing);
        GL11.glVertex2d(baseX - rightX * wing, baseY - rightY * wing);
        GL11.glEnd();

        double innerTipX = x + forwardX * (radius - 1.2D);
        double innerTipY = y + forwardY * (radius - 1.2D);
        double innerBaseX = x - forwardX * (radius * 0.55D);
        double innerBaseY = y - forwardY * (radius * 0.55D);
        GL11.glColor4f(1.0F, 0.78F, 0.24F, 1.0F);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2d(innerTipX, innerTipY);
        GL11.glVertex2d(innerBaseX + rightX * wing * 0.72D, innerBaseY + rightY * wing * 0.72D);
        GL11.glVertex2d(innerBaseX - rightX * wing * 0.72D, innerBaseY - rightY * wing * 0.72D);
        GL11.glEnd();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
    }

    private void drawCircle(int x, int y, float radius, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glColor4f(((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F, ((color >>> 24) & 255) / 255.0F);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x, y);
        for (int i = 0; i <= 16; i++) {
            double angle = Math.PI * 2.0D * i / 16.0D;
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }
        GL11.glEnd();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
    }

    private void drawDiamond(int x, int y, float radius, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GL11.glColor4f(((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F, ((color >>> 24) & 255) / 255.0F);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(x, y - radius);
        GL11.glVertex2d(x + radius, y);
        GL11.glVertex2d(x, y + radius);
        GL11.glVertex2d(x - radius, y);
        GL11.glVertex2d(x, y - radius);
        GL11.glEnd();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
    }

    private void drawCoordinatePanel(Minecraft mc, int left, int top, int size) {
        String coords = MathHelper.floor_double(mc.thePlayer.posX) + " "
                + MathHelper.floor_double(mc.thePlayer.posY) + " "
                + MathHelper.floor_double(mc.thePlayer.posZ);
        drawInfoPanel(left, top + size + 3, size, coords, nearestWaypointText(mc));
    }

    private void drawInfoPanel(int left, int top, int width, String primary, String secondary) {
        int lineHeight = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        Gui.drawRect(left, top - 1, left + width, top + lineHeight * 2 + 5, 0xD9080A0F);
        Gui.drawRect(left, top - 1, left + width, top, 0xFFFFC83D);
        int color = 0xFF000000 | (MinimapSettings.coordinateColor() & 0xFFFFFF);
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRendererObj.drawStringWithShadow(primary,
                left + (width - mc.fontRendererObj.getStringWidth(primary)) / 2, top + 1, color);
        mc.fontRendererObj.drawStringWithShadow(secondary,
                left + (width - mc.fontRendererObj.getStringWidth(secondary)) / 2,
                top + lineHeight + 2, 0xFFB8BEC8);
    }

    private String nearestWaypointText(Minecraft mc) {
        StaffWaypoint nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (StaffWaypoint point : WaypointManager.getInstance().getForCurrentWorld()) {
            double dx = point.getX() - mc.thePlayer.posX;
            double dy = point.getY() - mc.thePlayer.posY;
            double dz = point.getZ() - mc.thePlayer.posZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < nearestDistance) {
                nearest = point;
                nearestDistance = distance;
            }
        }
        if (nearest == null) return "No nearby waypoints";
        String label = nearest.getLabel();
        if (label.length() > 12) label = label.substring(0, 11) + "...";
        return label + "  " + (int)Math.round(nearestDistance) + "m";
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
        drawTerrain(mc, left, top, size, cx, cy, MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.posZ), MinimapSettings.range());
    }

    private void drawTerrain(Minecraft mc, int left, int top, int size, int cx, int cy, int playerX, int playerZ, int range) {
        int radius = size / 2 - 3;
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
        drawEntities(mc, cx, cy, size, MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.posZ), MinimapSettings.range());
    }

    private void drawEntities(Minecraft mc, int cx, int cy, int size, double mapCenterX, double mapCenterZ, int range) {
        int radius = size / 2 - 5;
        for (Object object : mc.theWorld.loadedEntityList) {
            if (!(object instanceof Entity) || object == mc.thePlayer) continue;
            Entity entity = (Entity) object;
            if (!(entity instanceof EntityLivingBase) || entity.isDead) continue;
            double dx = entity.posX - mapCenterX;
            double dz = entity.posZ - mapCenterZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > range) continue;
            int px = cx + (int) Math.round(dx / range * radius);
            int py = cy + (int) Math.round(dz / range * radius);
            int mapDx = px - cx, mapDy = py - cy;
            if (MinimapSettings.round() && mapDx * mapDx + mapDy * mapDy > radius * radius) continue;
            if (entity instanceof EntityPlayer) {
                drawDiamond(px, py, 3.5F, 0xFF171A20);
                drawDiamond(px, py, 2.5F, 0xFFFF6262);
            } else {
                drawCircle(px, py, 3.2F, 0xFF171A20);
                drawCircle(px, py, 2.2F, 0xFFFFB454);
            }
        }
    }

    private void drawWaypoints(Minecraft mc, int cx, int cy, int size) {
        drawWaypoints(mc, cx, cy, size, MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.posZ), MinimapSettings.range());
    }

    private void drawWaypoints(Minecraft mc, int cx, int cy, int size, double mapCenterX, double mapCenterZ, int range) {
        int radius = size / 2 - 7;
        List<StaffWaypoint> points = WaypointManager.getInstance().getForCurrentWorld();
        for (StaffWaypoint point : points) {
            double dx = point.getX() - mapCenterX;
            double dz = point.getZ() - mapCenterZ;
            if (Math.sqrt(dx * dx + dz * dz) > range) continue;
            int px = cx + (int) Math.round(dx / range * radius);
            int py = cy + (int) Math.round(dz / range * radius);
            int mapDx = px - cx, mapDy = py - cy;
            if (MinimapSettings.round() && mapDx * mapDx + mapDy * mapDy > radius * radius) continue;
            drawCircle(px, py, 4.2F, 0xEE11151C);
            drawCircle(px, py, 3.0F, point.getColor());
            String icon = waypointIcon(point);
            int iconWidth = mc.fontRendererObj.getStringWidth(icon);
            mc.fontRendererObj.drawStringWithShadow(icon, px - iconWidth / 2, py - 4, 0xFF101318);
        }
    }

    private String waypointIcon(StaffWaypoint point) {
        if (point.isDeathWaypoint()) return "!";
        String id = point.getIconId();
        if ("home".equalsIgnoreCase(id)) return "H";
        if ("star".equalsIgnoreCase(id)) return "*";
        if ("death".equalsIgnoreCase(id)) return "!";
        if ("diamond".equalsIgnoreCase(id)) return "D";
        if ("chest".equalsIgnoreCase(id)) return "C";
        if ("event".equalsIgnoreCase(id)) return "E";
        return "P";
    }
}
