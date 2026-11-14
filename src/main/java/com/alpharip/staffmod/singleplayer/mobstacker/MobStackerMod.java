// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.mobstacker;

import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleplayer entity stacker. Each stack keeps the serialized state of its
 * remaining mobs so each replacement retains its own vanilla entity data.
 */
public final class MobStackerMod {
    public static final int MAX_STACK_SIZE = 264;
    public static final double MERGE_RADIUS = 20.0D;

    private int playerScanTicks;

    private static final String COUNT = "AlphaMobStackCount";
    private static final String SKIP_MERGE = "AlphaMobStackSkipMerge";
    private static final String NAME_CAPTURED = "AlphaMobStackNameCaptured";
    private static final String ORIGINAL_NAME = "AlphaMobStackOriginalName";
    private static final String ORIGINAL_HAS_CUSTOM_NAME = "AlphaMobStackOriginalHasCustomName";
    private static final String ORIGINAL_ALWAYS_RENDER = "AlphaMobStackOriginalAlwaysRender";

    private static boolean isAvailableAndEnabled() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !mc.isSingleplayer() || mc.getIntegratedServer() == null) return false;
        if (!PermissionService.hasAccess(PermissionService.Rank.SCOUT)) return false;
        AlphaModuleFeature feature = AlphaModuleRegistry.findFeature("mob-stacker");
        return feature != null && feature.isEnabled();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event == null || event.isCanceled() || event.world == null || event.world.isRemote
                || !(event.entity instanceof EntityLiving) || !isAvailableAndEnabled()) return;

        EntityLiving joining = (EntityLiving) event.entity;
        if (joining.isDead) return;

        NBTTagCompound joiningData = joining.getEntityData();
        if (joiningData.getBoolean(SKIP_MERGE)) {
            joiningData.removeTag(SKIP_MERGE);
            return;
        }

        // New mobs are merged by the player-radius scan, not by their
        // distance from another mob. This keeps the 20-block limit centered
        // on the player instead of allowing distant groups to merge.
        if (getStackCount(joining) <= 1) {
            joiningData.setInteger(COUNT, 1);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END || event.player == null
                || event.player.worldObj == null || event.player.worldObj.isRemote
                || !isAvailableAndEnabled()) return;

        if (++playerScanTicks < 10) return;
        playerScanTicks = 0;

        EntityPlayer player = event.player;
        World world = player.worldObj;
        double radiusSq = MERGE_RADIUS * MERGE_RADIUS;
        List<EntityLiving> nearby = world.getEntitiesWithinAABB(EntityLiving.class,
                player.getEntityBoundingBox().expand(MERGE_RADIUS, MERGE_RADIUS, MERGE_RADIUS));

        List<EntityLiving> eligible = new ArrayList<EntityLiving>();
        for (EntityLiving mob : nearby) {
            if (mob != null && !mob.isDead
                    && mob.getDistanceSqToEntity(player) <= radiusSq) {
                eligible.add(mob);
            }
        }

        // Re-check existing entities periodically so mobs and existing stacks
        // are combined as soon as they enter the player's 20-block radius.
        for (int i = 0; i < eligible.size(); i++) {
            EntityLiving target = eligible.get(i);
            if (target == null || target.isDead || getStackCount(target) >= MAX_STACK_SIZE) continue;

            for (int j = i + 1; j < eligible.size(); j++) {
                EntityLiving source = eligible.get(j);
                if (source == null || source.isDead || target.isDead
                        || source.getClass() != target.getClass()
                        || source.getDistanceSqToEntity(player) > radiusSq
                        || target.getDistanceSqToEntity(player) > radiusSq
                        || !hasSameVariant(target, source)) continue;

                if (!mergeStacks(target, source)) continue;
                if (getStackCount(target) >= MAX_STACK_SIZE) break;
            }
        }
    }

    private static boolean mergeStacks(EntityLiving target, EntityLiving source) {
        if (target == null || source == null || target == source || target.isDead || source.isDead
                || target.getClass() != source.getClass() || !hasSameVariant(target, source)) return false;

        int targetCount = getStackCount(target);
        int sourceCount = getStackCount(source);
        int available = MAX_STACK_SIZE - targetCount;
        if (available <= 0 || sourceCount <= 0) return false;

        // Keep one real entity as the representative of each stack. Only the
        // count is transferred; no serialized-member queue is maintained.
        int transferCount = Math.min(sourceCount, available);
        int remainingSourceCount = sourceCount - transferCount;

        target.getEntityData().setInteger(COUNT, targetCount + transferCount);
        updateStackName(target);

        if (remainingSourceCount > 0) {
            source.getEntityData().setInteger(COUNT, remainingSourceCount);
            updateStackName(source);
        } else {
            source.setDead();
        }

        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeath(LivingDeathEvent event) {
        if (event == null || event.isCanceled() || event.entityLiving == null
                || event.entityLiving.worldObj == null || event.entityLiving.worldObj.isRemote
                || !(event.entityLiving instanceof EntityLiving)) return;

        EntityLiving dying = (EntityLiving) event.entityLiving;
        int count = getStackCount(dying);
        if (count <= 1) return;

        int remainingCount = count - 1;
        NBTTagCompound nextMob = dying.serializeNBT();

        // This is a fresh representative of the same mob type and variant,
        // not a queued snapshot. Remove the dying entity's UUID so Minecraft
        // assigns the replacement a new identity.
        nextMob.removeTag("UUIDMost");
        nextMob.removeTag("UUIDLeast");
        nextMob.setFloat("Health", dying.getMaxHealth());

        NBTTagCompound replacementData = nextMob.getCompoundTag("ForgeData");
        replacementData.removeTag(COUNT);
        replacementData.removeTag(SKIP_MERGE);
        replacementData.setInteger(COUNT, remainingCount);
        replacementData.setBoolean(SKIP_MERGE, true);

        Entity replacement;
        try {
            replacement = EntityList.createEntityFromNBT(nextMob, dying.worldObj);
        } catch (RuntimeException ignored) {
            replacement = null;
        }

        if (replacement instanceof EntityLiving) {
            EntityLiving next = (EntityLiving) replacement;
            next.setPositionAndRotation(dying.posX, dying.posY, dying.posZ,
                    dying.rotationYaw, dying.rotationPitch);
            updateStackName(next);

            if (dying.worldObj.spawnEntityInWorld(next)) {
                // Let the original death finish normally so its usual drops
                // and experience are retained. The replacement carries the
                // remaining stack count.
                return;
            }
        }

        // Never lose the complete stack if Minecraft rejects the replacement.
        // Keep the current entity alive as the remaining representative.
        event.setCanceled(true);
        dying.setHealth(dying.getMaxHealth());
        NBTTagCompound dyingData = dying.getEntityData();
        dyingData.setInteger(COUNT, remainingCount);
        updateStackName(dying);
    }

    private static int getStackCount(EntityLiving entity) {
        if (entity == null) return 1;
        int count = entity.getEntityData().getInteger(COUNT);
        return count < 1 ? 1 : Math.min(count, MAX_STACK_SIZE);
    }

    /**
     * Compares the entity-specific NBT while ignoring identity and transient
     * world state. Variant fields such as sheep color, slime size, horse type,
     * villager profession and baby/adult state remain part of the comparison.
     */
    private static boolean hasSameVariant(EntityLiving first, EntityLiving second) {
        if (first == null || second == null || first.getClass() != second.getClass()) return false;

        NBTTagCompound firstVariant = createVariantSnapshot(first);
        NBTTagCompound secondVariant = createVariantSnapshot(second);
        return firstVariant.equals(secondVariant);
    }

    /**
     * Only compare stable identity/variant properties. Full entity NBT also
     * contains mutable state such as equipment, health, effects, age timers,
     * movement and other data that must not prevent otherwise compatible mobs
     * from joining the same stack.
     */
    private static NBTTagCompound createVariantSnapshot(EntityLiving entity) {
        NBTTagCompound entityNbt = entity.serializeNBT();

        NBTTagCompound variant = new NBTTagCompound();
        String[] variantTags = {
                // Shared age / growth state.
                "IsBaby", "Baby", "Age", "GrowingAge",

                // Common vanilla mob variants.
                "Color", "Sheared", "Size", "SkeletonType", "IsVillager",
                "VillagerProfession", "Profession", "RabbitType", "CatType",
                "CollarColor", "Type", "Variant", "Elder", "powered"
        };

        for (String tag : variantTags) {
            if (entityNbt.hasKey(tag)) {
                variant.setTag(tag, entityNbt.getTag(tag).copy());
            }
        }

        // Keep separately named mobs distinct, but ignore the generated stack
        // label so an existing stack remains compatible with a new unnamed mob.
        NBTTagCompound data = entity.getEntityData();
        boolean hasCustomName;
        String customName;
        if (data.getBoolean(NAME_CAPTURED)) {
            hasCustomName = data.getBoolean(ORIGINAL_HAS_CUSTOM_NAME);
            customName = data.getString(ORIGINAL_NAME);
        } else {
            customName = entity.getCustomNameTag();
            hasCustomName = customName != null && !customName.trim().isEmpty();
        }

        variant.setBoolean("AlphaHasCustomName", hasCustomName);
        if (hasCustomName) {
            variant.setString("AlphaCustomName", customName);
        }

        return variant;
    }

    private static void updateStackName(EntityLiving entity) {
        if (entity == null) return;
        NBTTagCompound data = entity.getEntityData();
        int count = getStackCount(entity);

        if (!data.getBoolean(NAME_CAPTURED)) {
            String customName = entity.getCustomNameTag();
            boolean hasCustomName = customName != null && !customName.trim().isEmpty();
            String displayName = hasCustomName ? customName : entity.getName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = EntityList.getEntityString(entity);
            }
            data.setBoolean(NAME_CAPTURED, true);
            data.setString(ORIGINAL_NAME, displayName == null ? "" : displayName);
            data.setBoolean(ORIGINAL_HAS_CUSTOM_NAME, hasCustomName);
            data.setBoolean(ORIGINAL_ALWAYS_RENDER, entity.getAlwaysRenderNameTag());
        }

        if (count > 1) {
            String baseName = data.getString(ORIGINAL_NAME);
            entity.setCustomNameTag(baseName + " x" + count);
            entity.setAlwaysRenderNameTag(true);
        } else {
            String originalName = data.getString(ORIGINAL_NAME);
            boolean hadCustomName = data.getBoolean(ORIGINAL_HAS_CUSTOM_NAME);
            entity.setCustomNameTag(hadCustomName ? originalName : "");
            entity.setAlwaysRenderNameTag(data.getBoolean(ORIGINAL_ALWAYS_RENDER));
            data.removeTag(NAME_CAPTURED);
            data.removeTag(ORIGINAL_NAME);
            data.removeTag(ORIGINAL_HAS_CUSTOM_NAME);
            data.removeTag(ORIGINAL_ALWAYS_RENDER);
        }
    }
}