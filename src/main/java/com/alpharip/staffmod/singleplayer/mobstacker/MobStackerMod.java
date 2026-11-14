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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Singleplayer entity stacker. Each stack keeps the serialized state of its
 * remaining mobs so each replacement retains its own vanilla entity data.
 */
public final class MobStackerMod {
    public static final int MAX_STACK_SIZE = 264;
    public static final double MERGE_RADIUS = 20.0D;

    private int playerScanTicks;

    // Retain raw member data until the original death has fully completed.
    private static final List<PendingSpawn> PENDING_SPAWNS = new ArrayList<PendingSpawn>();

    private static final class PendingSpawn {
        private final World world;
        private final NBTTagCompound entityData;

        private PendingSpawn(World world, NBTTagCompound entityData) {
            this.world = world;
            this.entityData = entityData;
        }
    }

    private static final String COUNT = "AlphaMobStackCount";
    private static final String MEMBERS = "AlphaMobStackMembers";
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

        NBTTagList sourceMembers = getMembers(source);
        if (sourceCount > 1 && sourceMembers.tagCount() != sourceCount - 1) return false;

        int transferCount = Math.min(sourceCount, available);
        NBTTagCompound sourceRepresentative = createMemberSnapshot(source);
        if (sourceRepresentative == null) return false;

        EntityLiving remainderEntity = null;
        NBTTagList remainingMembers = new NBTTagList();
        int remainingCount = sourceCount - transferCount;

        if (remainingCount > 0) {
            // The source representative and the first transferCount - 1
            // stored members move into the target. The next stored member
            // becomes the representative of the source's remaining stack.
            int nextRepresentativeIndex = transferCount - 1;
            if (nextRepresentativeIndex < 0 || nextRepresentativeIndex >= sourceMembers.tagCount()) {
                return false;
            }

            NBTTagCompound nextMember = (NBTTagCompound) sourceMembers
                    .getCompoundTagAt(nextRepresentativeIndex).copy();
            Entity replacement;
            try {
                replacement = EntityList.createEntityFromNBT(nextMember, source.worldObj);
            } catch (RuntimeException ignored) {
                return false;
            }
            if (!(replacement instanceof EntityLiving)) return false;

            remainderEntity = (EntityLiving) replacement;
            remainderEntity.setPositionAndRotation(source.posX, source.posY, source.posZ,
                    source.rotationYaw, source.rotationPitch);

            remainingMembers = (NBTTagList) sourceMembers.copy();
            for (int i = 0; i < transferCount; i++) {
                remainingMembers.removeTag(0);
            }

            NBTTagCompound remainderData = remainderEntity.getEntityData();
            remainderData.setInteger(COUNT, remainingCount);
            remainderData.setTag(MEMBERS, remainingMembers);
            remainderData.setBoolean(SKIP_MERGE, true);
            updateStackName(remainderEntity);

            if (!source.worldObj.spawnEntityInWorld(remainderEntity)) return false;
        }

        NBTTagCompound targetData = target.getEntityData();
        NBTTagList targetMembers = getMembers(target);
        targetMembers.appendTag(sourceRepresentative);

        int storedToTransfer = transferCount - 1;
        for (int i = 0; i < storedToTransfer; i++) {
            targetMembers.appendTag(sourceMembers.getCompoundTagAt(i).copy());
        }

        targetData.setTag(MEMBERS, targetMembers);
        targetData.setInteger(COUNT, targetCount + transferCount);
        updateStackName(target);

        source.setDead();
        return true;
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event == null || event.isCanceled() || event.entityLiving == null
                || event.entityLiving.worldObj == null || event.entityLiving.worldObj.isRemote
                || !(event.entityLiving instanceof EntityLiving)) return;

        EntityLiving dying = (EntityLiving) event.entityLiving;
        int count = getStackCount(dying);
        if (count <= 1) return;

        NBTTagList storedMembers = getMembers(dying);
        if (storedMembers.tagCount() <= 0) {
            // A malformed stack cannot provide a replacement. Let the
            // representative complete its normal death sequence.
            return;
        }

        // Work on a copy so the source stack is never mutated in-place.
        NBTTagCompound nextMember = (NBTTagCompound) storedMembers.getCompoundTagAt(0).copy();
        NBTTagList remainingMembers = (NBTTagList) storedMembers.copy();
        remainingMembers.removeTag(0);

        // Preserve the original entity ID and NBT. Do not round-trip this
        // compound through writeToNBT(), which omits the ID required by
        // EntityList.createEntityFromNBT() in this Minecraft version.
        NBTTagCompound queuedData = (NBTTagCompound) nextMember.copy();
        NBTTagCompound forgeData = queuedData.hasKey("ForgeData", 10)
                ? queuedData.getCompoundTag("ForgeData") : new NBTTagCompound();
        int remainingCount = remainingMembers.tagCount() + 1;
        forgeData.setInteger(COUNT, remainingCount);
        forgeData.setTag(MEMBERS, remainingMembers);
        forgeData.setBoolean(SKIP_MERGE, true);
        queuedData.setTag("ForgeData", forgeData);

        // Spawn on the next server world tick, after vanilla has processed
        // the representative's death and removed it from the world.
        PENDING_SPAWNS.add(new PendingSpawn(dying.worldObj, queuedData));
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END || event.world == null
                || event.world.isRemote || PENDING_SPAWNS.isEmpty()) return;

        Iterator<PendingSpawn> iterator = PENDING_SPAWNS.iterator();
        while (iterator.hasNext()) {
            PendingSpawn pending = iterator.next();
            if (pending.world != event.world) continue;

            Entity replacement;
            try {
                replacement = EntityList.createEntityFromNBT(
                        (NBTTagCompound) pending.entityData.copy(), event.world);
            } catch (RuntimeException ignored) {
                continue;
            }
            if (!(replacement instanceof EntityLiving)) {
                iterator.remove();
                continue;
            }

            try {
                if (event.world.spawnEntityInWorld(replacement)) {
                    iterator.remove();
                }
            } catch (RuntimeException ignored) {
                // Keep the intact serialized member and retry next world tick.
            }
        }
    }

    private static int getStackCount(EntityLiving entity) {
        if (entity == null) return 1;
        int count = entity.getEntityData().getInteger(COUNT);
        return count < 1 ? 1 : Math.min(count, MAX_STACK_SIZE);
    }

    private static NBTTagList getMembers(EntityLiving entity) {
        NBTTagCompound data = entity.getEntityData();
        NBTTagList members = data.getTagList(MEMBERS, 10);
        return members == null ? new NBTTagList() : members;
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

    private static NBTTagCompound createMemberSnapshot(EntityLiving entity) {
        if (entity == null) return null;

        // Entity.writeToNBT() omits the entity ID in Forge 1.8.9.
        // EntityList.createEntityFromNBT() requires that ID to recreate it.
        NBTTagCompound snapshot = entity.serializeNBT();

        // Store the mob's original name, not the generated "Name xCount"
        // label used to display an existing stack.
        NBTTagCompound entityData = entity.getEntityData();
        if (entityData.getBoolean(NAME_CAPTURED)) {
            if (entityData.getBoolean(ORIGINAL_HAS_CUSTOM_NAME)) {
                snapshot.setString("CustomName", entityData.getString(ORIGINAL_NAME));
            } else {
                snapshot.removeTag("CustomName");
            }
            snapshot.setBoolean("CustomNameVisible", entityData.getBoolean(ORIGINAL_ALWAYS_RENDER));
        }

        // Preserve other entity NBT, but never nest this stack's own metadata
        // inside every stored member snapshot.
        if (snapshot.hasKey("ForgeData", 10)) {
            NBTTagCompound forgeData = snapshot.getCompoundTag("ForgeData");
            forgeData.removeTag(COUNT);
            forgeData.removeTag(MEMBERS);
            forgeData.removeTag(SKIP_MERGE);
            forgeData.removeTag(NAME_CAPTURED);
            forgeData.removeTag(ORIGINAL_NAME);
            forgeData.removeTag(ORIGINAL_HAS_CUSTOM_NAME);
            forgeData.removeTag(ORIGINAL_ALWAYS_RENDER);
        }
        return snapshot;
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