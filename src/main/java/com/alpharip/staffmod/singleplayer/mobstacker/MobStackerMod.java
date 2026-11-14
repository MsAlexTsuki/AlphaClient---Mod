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

        EntityLiving stackedMob = (EntityLiving) event.entityLiving;
        int stackCount = getStackCount(stackedMob);
        if (stackCount <= 1) {
            return;
        }

        /*
         * Produce one ordinary vanilla death from a temporary copy. The
         * original stacked mob is then kept alive, preserving its live AI
         * state, while its stored stack count is reduced by one.
         */
        EntityLiving singleMobCopy = createSingleMobCopy(stackedMob);
        if (singleMobCopy == null) {
            preserveStackAfterFailedReplacement(event, stackedMob);
            return;
        }

        boolean acceptedDamage = singleMobCopy.attackEntityFrom(event.source, Float.MAX_VALUE);
        if (!acceptedDamage || !singleMobCopy.isDead) {
            singleMobCopy.setDead();
            preserveStackAfterFailedReplacement(event, stackedMob);
            return;
        }

        setStackCount(stackedMob, stackCount - 1);
        stackedMob.setHealth(stackedMob.getMaxHealth());
        event.setCanceled(true);
    }

    private static EntityLiving createSingleMobCopy(EntityLiving source) {
        String entityName = EntityList.getEntityString(source);
        if (entityName == null) {
            return null;
        }

        Entity created = EntityList.createEntityByName(entityName, source.worldObj);
        if (!(created instanceof EntityLiving)) {
            return null;
        }

        NBTTagCompound data = writeEntityData(source);
        // Keep the fresh UUID generated by the copy's constructor.
        data.removeTag("UUIDMost");
        data.removeTag("UUIDLeast");
        created.readFromNBT(data);

        EntityLiving copy = (EntityLiving) created;
        setStackCount(copy, 1);
        copy.setHealth(copy.getMaxHealth());
        copy.getEntityData().setBoolean(SKIP_STACKING_KEY, true);
        copy.setPositionAndRotation(
                source.posX, source.posY, source.posZ,
                source.rotationYaw, source.rotationPitch
        );

        if (!source.worldObj.spawnEntityInWorld(copy)) {
            copy.setDead();
            return null;
        }

        copy.getEntityData().removeTag(SKIP_STACKING_KEY);
        return copy;
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
