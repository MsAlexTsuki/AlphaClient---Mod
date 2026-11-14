// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.mobstacker;

import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.UUID;

/**
 * Singleplayer mob stacking.
 *
 * A stack is stored in Forge's persistent entity data, so it is saved with
 * the entity. Each member still dies through the normal vanilla death path:
 * a replacement entity carries the remaining count while the dying entity
 * produces its ordinary drops and experience.
 */
public final class MobStackerMod {
    private static final String STACK_COUNT_KEY = "AlphaClientMobStackCount";
    private static final String SKIP_STACKING_KEY = "AlphaClientMobStackSkipJoin";

    private static boolean isFeatureEnabled() {
        AlphaModuleFeature feature = AlphaModuleRegistry.findFeature("mob-stacker");
        return feature != null && feature.isEnabled();
    }

    private static boolean isSingleplayerWorld(World world) {
        Minecraft minecraft = Minecraft.getMinecraft();
        return world != null && !world.isRemote
                && minecraft != null && minecraft.isIntegratedServerRunning();
    }

    private static int getStackCount(EntityLiving entity) {
        NBTTagCompound data = entity.getEntityData();
        int count = data.getInteger(STACK_COUNT_KEY);
        return count < 1 ? 1 : count;
    }

    private static void setStackCount(EntityLiving entity, int count) {
        entity.getEntityData().setInteger(STACK_COUNT_KEY, Math.max(1, count));
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!isFeatureEnabled() || !isSingleplayerWorld(event.world)
                || !(event.entity instanceof EntityLiving)) {
            return;
        }

        EntityLiving incoming = (EntityLiving) event.entity;
        NBTTagCompound incomingData = incoming.getEntityData();
        if (incomingData.getBoolean(SKIP_STACKING_KEY)) {
            return;
        }

        int incomingCount = getStackCount(incoming);
        AxisAlignedBB searchArea = incoming.getEntityBoundingBox().expand(
                MobStackerSettings.STACK_RADIUS,
                MobStackerSettings.STACK_RADIUS,
                MobStackerSettings.STACK_RADIUS
        );
        List<EntityLiving> nearby = event.world.getEntitiesWithinAABB(EntityLiving.class, searchArea);

        for (EntityLiving existing : nearby) {
            if (existing == incoming || existing.isDead || !existing.isEntityAlive()
                    || existing.getClass() != incoming.getClass()) {
                continue;
            }

            if (incoming.getDistanceSqToEntity(existing)
                    > MobStackerSettings.STACK_RADIUS * MobStackerSettings.STACK_RADIUS) {
                continue;
            }

            int existingCount = getStackCount(existing);
            int availableSpace = MobStackerSettings.MAX_STACK_SIZE - existingCount;
            if (availableSpace <= 0) {
                continue;
            }

            int transferred = Math.min(availableSpace, incomingCount);
            setStackCount(existing, existingCount + transferred);
            incomingCount -= transferred;

            if (incomingCount <= 0) {
                event.setCanceled(true);
                return;
            }
        }

        setStackCount(incoming, incomingCount);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!isSingleplayerWorld(event.entityLiving.worldObj)
                || !(event.entityLiving instanceof EntityLiving)) {
            return;
        }

        EntityLiving dying = (EntityLiving) event.entityLiving;
        int stackCount = getStackCount(dying);
        if (stackCount <= 1) {
            return;
        }

        /*
         * Let the original entity complete its normal death sequence. The
         * replacement represents the remaining mobs and is excluded from
         * join-time stacking while it is inserted into the world.
         */
        Entity replacementEntity = EntityList.createEntityFromNBT(writeEntityData(dying), dying.worldObj);
        if (!(replacementEntity instanceof EntityLiving)) {
            preserveStackAfterFailedReplacement(event, dying);
            return;
        }

        EntityLiving replacement = (EntityLiving) replacementEntity;
        replacement.setUniqueId(UUID.randomUUID());
        replacement.setHealth(replacement.getMaxHealth());
        setStackCount(replacement, stackCount - 1);
        replacement.getEntityData().setBoolean(SKIP_STACKING_KEY, true);
        replacement.setPositionAndRotation(
                dying.posX, dying.posY, dying.posZ,
                dying.rotationYaw, dying.rotationPitch
        );

        if (!dying.worldObj.spawnEntityInWorld(replacement)) {
            preserveStackAfterFailedReplacement(event, dying);
            return;
        }

        replacement.getEntityData().removeTag(SKIP_STACKING_KEY);
    }

    private static NBTTagCompound writeEntityData(EntityLiving entity) {
        NBTTagCompound data = new NBTTagCompound();
        entity.writeToNBT(data);
        return data;
    }

    private static void preserveStackAfterFailedReplacement(LivingDeathEvent event, EntityLiving dying) {
        event.setCanceled(true);
        dying.setHealth(dying.getMaxHealth());
        System.err.println("[AlphaClient] Mob Stacker could not create a replacement entity; "
                + "the existing stack was preserved.");
    }
}
