// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.mobstacker;

import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;

/**
 * Singleplayer mob stacking.
 *
 * A stack is stored in Forge's persistent entity data, so it is saved with
 * the entity. When a stacked mob dies, it completes its normal vanilla death
 * path and one replacement mob is spawned with the remaining stack count.
 */
public final class MobStackerMod {
    static final String STACK_COUNT_KEY = "AlphaClientMobStackCount";
    private static final String SKIP_STACKING_KEY = "AlphaClientMobStackSkipJoin";

    static {
        MobStackerNetwork.initialize();
    }

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
        int normalizedCount = Math.max(1, count);
        int previousCount = getStackCount(entity);
        entity.getEntityData().setInteger(STACK_COUNT_KEY, normalizedCount);
        if (previousCount != normalizedCount) {
            MobStackerNetwork.sync(entity, normalizedCount);
        }
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
         * Let the killed mob complete its normal vanilla death so its drops
         * and experience are produced exactly once. Replace the remaining
         * members with one fresh mob carrying the reduced stack count.
         */
        EntityLiving replacement = createStackReplacement(stackedMob, stackCount - 1);
        if (replacement == null) {
            preserveStackAfterFailedCopy(event, stackedMob);
            return;
        }

        // The original death is intentionally not canceled.
    }

    private static EntityLiving createStackReplacement(EntityLiving source, int remainingCount) {
        String entityName = EntityList.getEntityString(source);
        if (entityName == null || remainingCount < 1) {
            return null;
        }

        Entity created = EntityList.createEntityByName(entityName, source.worldObj);
        if (!(created instanceof EntityLiving)) {
            return null;
        }

        NBTTagCompound data = writeEntityData(source);
        // Keep the fresh UUID generated by the replacement's constructor.
        data.removeTag("UUIDMost");
        data.removeTag("UUIDLeast");
        created.readFromNBT(data);

        EntityLiving replacement = (EntityLiving) created;
        setStackCount(replacement, remainingCount);
        replacement.setHealth(replacement.getMaxHealth());
        replacement.getEntityData().setBoolean(SKIP_STACKING_KEY, true);
        replacement.setPositionAndRotation(
                source.posX, source.posY, source.posZ,
                source.rotationYaw, source.rotationPitch
        );

        if (!source.worldObj.spawnEntityInWorld(replacement)) {
            replacement.setDead();
            return null;
        }

        replacement.getEntityData().removeTag(SKIP_STACKING_KEY);
        MobStackerNetwork.sync(replacement, remainingCount);
        return replacement;
    }

    private static NBTTagCompound writeEntityData(EntityLiving entity) {
        NBTTagCompound data = new NBTTagCompound();
        entity.writeToNBT(data);
        return data;
    }

    private static void preserveStackAfterFailedCopy(LivingDeathEvent event, EntityLiving dying) {
        event.setCanceled(true);
        dying.setHealth(dying.getMaxHealth());
        System.err.println("[AlphaClient] Mob Stacker could not create a temporary mob copy; "
                + "the existing stack was preserved.");
    }

    /**
     * Re-check nearby mobs after movement as well as when they first spawn.
     * Running every five world ticks lets mobs that land near one another
     * merge without waiting for another spawn event.
     */
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isFeatureEnabled()
                || !isSingleplayerWorld(event.world)
                || event.world.getTotalWorldTime() % 5L != 0L) {
            return;
        }

        List<Entity> loadedEntities = new ArrayList<Entity>(event.world.loadedEntityList);
        for (Entity entity : loadedEntities) {
            if (!(entity instanceof EntityLiving)) {
                continue;
            }

            EntityLiving incoming = (EntityLiving) entity;
            if (incoming.isDead || !incoming.isEntityAlive()) {
                continue;
            }

            int incomingCount = getStackCount(incoming);
            if (incomingCount >= MobStackerSettings.MAX_STACK_SIZE) {
                continue;
            }

            AxisAlignedBB searchArea = incoming.getEntityBoundingBox().expand(
                    MobStackerSettings.STACK_RADIUS,
                    MobStackerSettings.STACK_RADIUS,
                    MobStackerSettings.STACK_RADIUS
            );
            List<EntityLiving> nearby = event.world.getEntitiesWithinAABB(EntityLiving.class, searchArea);

            for (EntityLiving existing : nearby) {
                // Only merge into an older entity. This prevents the same pair
                // from transferring its count in both directions during one scan.
                if (existing == incoming || existing.isDead || !existing.isEntityAlive()
                        || existing.getEntityId() >= incoming.getEntityId()
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
                    incoming.setDead();
                    break;
                }
            }

            if (!incoming.isDead && incomingCount > 0) {
                setStackCount(incoming, incomingCount);
                // Re-send persisted stacks after the entity has entered the client world.
                if (incomingCount > 1) {
                    MobStackerNetwork.sync(incoming, incomingCount);
                }
            }
        }
    }

    private static boolean isClientMobStackerEnabled() {
        Minecraft minecraft = Minecraft.getMinecraft();
        AlphaModuleFeature feature = AlphaModuleRegistry.findFeature("mob-stacker");
        return minecraft != null && minecraft.isSingleplayer()
                && minecraft.theWorld != null && minecraft.thePlayer != null
                && feature != null && feature.isEnabled();
    }

    /**
     * Replace custom vanilla mob labels with the Mob Stacker label, then draw
     * the same label for every rendered mob (including mobs without custom names).
     */
    @SubscribeEvent
    public void onMobNameTagPre(RenderLivingEvent.Specials.Pre event) {
        if (event.entity instanceof EntityLiving && isClientMobStackerEnabled()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderMobNameTag(RenderLivingEvent.Post event) {
        if (!(event.entity instanceof EntityLiving) || !isClientMobStackerEnabled()) {
            return;
        }

        EntityLiving entity = (EntityLiving) event.entity;
        if (entity.isInvisible() || entity.isDead) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.fontRendererObj == null || minecraft.getRenderManager() == null) {
            return;
        }

        String mobName = EntityList.getEntityString(entity);
        if (mobName == null || mobName.trim().isEmpty()) {
            mobName = entity.getName();
        }
        String label = mobName + " x" + getStackCount(entity);
        int halfWidth = minecraft.fontRendererObj.getStringWidth(label) / 2;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(event.x, event.y + entity.height + 0.5D, event.z);
            GlStateManager.rotate(-minecraft.getRenderManager().playerViewY,
                    0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(minecraft.getRenderManager().playerViewX,
                    1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-0.026666668F, -0.026666668F, 0.026666668F);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            Gui.drawRect(-halfWidth - 2, -2, halfWidth + 2,
                    minecraft.fontRendererObj.FONT_HEIGHT + 2, 0x50000000);
            minecraft.fontRendererObj.drawStringWithShadow(label, -halfWidth, 0, 0xFFFFFFFF);
        } finally {
            GlStateManager.popMatrix();
            GL11.glPopAttrib();
        }
    }
}
