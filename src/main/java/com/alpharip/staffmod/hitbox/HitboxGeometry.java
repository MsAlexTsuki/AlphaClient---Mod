package com.alpharip.staffmod.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

/**
 * Converts an entity's interpolated bounding box into render-view-relative coordinates.
 */
public final class HitboxGeometry {
    private HitboxGeometry() {
    }

    public static AxisAlignedBB getInterpolatedRelativeBox(Entity entity, Entity renderView, float partialTicks) {
        if (entity == null || renderView == null) {
            return null;
        }

        double viewX = interpolate(renderView.lastTickPosX, renderView.posX, partialTicks);
        double viewY = interpolate(renderView.lastTickPosY, renderView.posY, partialTicks);
        double viewZ = interpolate(renderView.lastTickPosZ, renderView.posZ, partialTicks);

        double entityX = interpolate(entity.lastTickPosX, entity.posX, partialTicks);
        double entityY = interpolate(entity.lastTickPosY, entity.posY, partialTicks);
        double entityZ = interpolate(entity.lastTickPosZ, entity.posZ, partialTicks);

        AxisAlignedBB box = entity.getEntityBoundingBox();
        double offsetX = entityX - entity.posX - viewX;
        double offsetY = entityY - entity.posY - viewY;
        double offsetZ = entityZ - entity.posZ - viewZ;

        return new AxisAlignedBB(
                box.minX + offsetX, box.minY + offsetY, box.minZ + offsetZ,
                box.maxX + offsetX, box.maxY + offsetY, box.maxZ + offsetZ
        );
    }

    private static double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }
}
