// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.mobstacker;

import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleplayer entity stacker. Each stack keeps the serialized state of its
 * remaining mobs so each replacement retains its own vanilla entity data.
 */
public final class MobStackerMod {
    public static final int MAX_STACK_SIZE = 264;
    public static final double MERGE_RADIUS = 20.0D;

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

        int joiningCount = getStackCount(joining);
        // A persisted stack is already a complete group. Do not collapse it
        // into another stack during chunk loading.
        if (joiningCount > 1) return;

        World world = event.world;
        double radiusSq = MERGE_RADIUS * MERGE_RADIUS;
        List<EntityLiving> nearby = world.getEntitiesWithinAABB(EntityLiving.class,
                joining.getEntityBoundingBox().expand(MERGE_RADIUS, MERGE_RADIUS, MERGE_RADIUS));

        EntityLiving target = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        for (EntityLiving candidate : nearby) {
            if (candidate == null || candidate == joining || candidate.isDead
                    || candidate.getClass() != joining.getClass()) continue;

            int count = getStackCount(candidate);
            if (count >= MAX_STACK_SIZE) continue;

            NBTTagList candidateMembers = getMembers(candidate);
            if (count > 1 && candidateMembers.tagCount() != count - 1) continue;
            if (!hasSameVariant(candidate, joining)) continue;

            double distanceSq = candidate.getDistanceSqToEntity(joining);
            if (distanceSq <= radiusSq && distanceSq < nearestDistanceSq) {
                target = candidate;
                nearestDistanceSq = distanceSq;
            }
        }

        if (target == null) {
            joiningData.setInteger(COUNT, 1);
            return;
        }

        NBTTagCompound memberSnapshot = createMemberSnapshot(joining);
        if (memberSnapshot == null) return;

        NBTTagCompound targetData = target.getEntityData();
        NBTTagList members = getMembers(target);
        members.appendTag(memberSnapshot);
        targetData.setTag(MEMBERS, members);
        targetData.setInteger(COUNT, getStackCount(target) + 1);
        updateStackName(target);
        event.setCanceled(true);
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

        Entity replacement;
        try {
            replacement = EntityList.createEntityFromNBT(nextMember, dying.worldObj);
        } catch (RuntimeException ignored) {
            return;
        }
        if (!(replacement instanceof EntityLiving)) return;

        replacement.setPositionAndRotation(dying.posX, dying.posY, dying.posZ,
                dying.rotationYaw, dying.rotationPitch);

        NBTTagCompound replacementData = replacement.getEntityData();
        int remainingCount = remainingMembers.tagCount() + 1;
        replacementData.setInteger(COUNT, remainingCount);
        replacementData.setTag(MEMBERS, remainingMembers);
        replacementData.setBoolean(SKIP_MERGE, true);
        updateStackName((EntityLiving) replacement);

        // The member snapshots include the entity ID through serializeNBT(),
        // allowing EntityList.createEntityFromNBT() to reconstruct the mob.
        // Do not cancel the original death and do not retry invalid snapshots.
        dying.worldObj.spawnEntityInWorld(replacement);
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