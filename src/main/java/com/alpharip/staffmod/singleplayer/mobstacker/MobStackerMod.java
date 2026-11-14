// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.mobstacker;

import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
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
import net.minecraft.client.Minecraft;

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeath(LivingDeathEvent event) {
        if (event == null || event.isCanceled() || event.entityLiving == null
                || event.entityLiving.worldObj == null || event.entityLiving.worldObj.isRemote
                || !(event.entityLiving instanceof EntityLiving)) return;

        EntityLiving dying = (EntityLiving) event.entityLiving;
        int count = getStackCount(dying);
        if (count <= 1) return;

        NBTTagCompound data = dying.getEntityData();
        NBTTagList members = getMembers(dying);
        if (members.tagCount() < 1) return;

        NBTTagCompound nextMember = (NBTTagCompound) members.getCompoundTagAt(0).copy();
        members.removeTag(0);

        Entity replacement = EntityList.createEntityFromNBT(nextMember, dying.worldObj);
        if (!(replacement instanceof EntityLiving)) {
            // Keep the current mob alive rather than silently losing the rest
            // of the stack if a replacement cannot be reconstructed.
            event.setCanceled(true);
            dying.setHealth(dying.getMaxHealth());
            return;
        }

        replacement.setPositionAndRotation(dying.posX, dying.posY, dying.posZ,
                dying.rotationYaw, dying.rotationPitch);

        NBTTagCompound replacementData = replacement.getEntityData();
        replacementData.setInteger(COUNT, count - 1);
        replacementData.setTag(MEMBERS, members);
        replacementData.setBoolean(SKIP_MERGE, true);
        updateStackName((EntityLiving) replacement);

        if (!dying.worldObj.spawnEntityInWorld(replacement)) {
            event.setCanceled(true);
            dying.setHealth(dying.getMaxHealth());
        }
        // The dying entity is allowed to complete its normal death sequence:
        // vanilla handles one mob's drops, XP and death effects.
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

    private static NBTTagCompound createMemberSnapshot(EntityLiving entity) {
        if (entity == null) return null;
        NBTTagCompound snapshot = new NBTTagCompound();
        entity.writeToNBT(snapshot);

        // Preserve other mods' ForgeData, but never nest this stack's own
        // counter/queue inside every stored member snapshot.
        if (snapshot.hasKey("ForgeData", 10)) {
            NBTTagCompound forgeData = snapshot.getCompoundTag("ForgeData");
            forgeData.removeTag(COUNT);
            forgeData.removeTag(MEMBERS);
            forgeData.removeTag(SKIP_MERGE);
            forgeData.removeTag(NAME_CAPTURED);
            forgeData.removeTag(ORIGINAL_NAME);
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
            String baseName = customName == null || customName.trim().isEmpty()
                    ? entity.getName() : customName;
            if (baseName == null || baseName.trim().isEmpty()) {
                baseName = EntityList.getEntityString(entity);
            }
            data.setBoolean(NAME_CAPTURED, true);
            data.setString(ORIGINAL_NAME, baseName == null ? "" : baseName);
            data.setBoolean(ORIGINAL_ALWAYS_RENDER, entity.getAlwaysRenderNameTag());
        }

        if (count > 1) {
            String baseName = data.getString(ORIGINAL_NAME);
            entity.setCustomNameTag(baseName + " x" + count);
            entity.setAlwaysRenderNameTag(true);
        } else {
            String originalName = data.getString(ORIGINAL_NAME);
            entity.setCustomNameTag(originalName.isEmpty() ? null : originalName);
            entity.setAlwaysRenderNameTag(data.getBoolean(ORIGINAL_ALWAYS_RENDER));
            data.removeTag(NAME_CAPTURED);
            data.removeTag(ORIGINAL_NAME);
            data.removeTag(ORIGINAL_ALWAYS_RENDER);
        }
    }
}
