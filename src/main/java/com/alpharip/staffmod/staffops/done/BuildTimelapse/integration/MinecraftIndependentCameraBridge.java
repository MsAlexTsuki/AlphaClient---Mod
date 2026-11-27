package com.alpharip.staffmod.BuildTimelapse.integration;

import com.alpharip.staffmod.RecordingStudio.camera.CameraPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

/**
 * Client-only render-view camera. The entity is never added to the world and the
 * real player is never moved, rotated, or given camera input.
 */
@SideOnly(Side.CLIENT)
public final class MinecraftIndependentCameraBridge implements MinecraftCameraBridge {
    private final Minecraft minecraft;
    private Entity originalRenderViewEntity;
    private EntityOtherPlayerMP cameraEntity;
    private World cameraWorld;
    private float originalFov;
    private boolean originalFovCaptured;

    public MinecraftIndependentCameraBridge(Minecraft minecraft) {
        if (minecraft == null) throw new IllegalArgumentException("minecraft");
        this.minecraft = minecraft;
    }

    @Override
    public boolean isAvailable() {
        return minecraft.theWorld != null && minecraft.thePlayer != null
                && minecraft.theWorld.isRemote;
    }

    @Override
    public CameraPose getInitialPose() {
        if (!isAvailable()) return CameraPose.origin();
        return new CameraPose(
                minecraft.thePlayer.posX, minecraft.thePlayer.posY, minecraft.thePlayer.posZ,
                minecraft.thePlayer.rotationYaw, minecraft.thePlayer.rotationPitch, 0.0F, 70.0F
        );
    }

    @Override
    public Object getOriginalRenderViewEntity() {
        return originalRenderViewEntity;
    }

    @Override
    public void activate(CameraPose pose) throws Exception {
        requireClientThread();
        if (pose == null) throw new IllegalArgumentException("pose");
        if (!isAvailable()) throw new IllegalStateException("Minecraft client world is not available");
        if (cameraEntity != null) {
            apply(pose);
            return;
        }

        Entity original = minecraft.getRenderViewEntity();
        if (original == null) original = minecraft.thePlayer;
        originalRenderViewEntity = original;
        cameraWorld = minecraft.theWorld;
        if (minecraft.gameSettings != null) {
            originalFov = minecraft.gameSettings.fovSetting;
            originalFovCaptured = true;
        }

        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "AlphaBuildCamera");
            EntityOtherPlayerMP created = new EntityOtherPlayerMP(cameraWorld, profile);
            created.noClip = true;
            created.motionX = created.motionY = created.motionZ = 0.0D;
            created.setInvisible(true);
            applyPose(created, pose);
            applyFov(pose);
            cameraEntity = created;
            minecraft.setRenderViewEntity(created);
        } catch (Throwable failure) {
            try {
                minecraft.setRenderViewEntity(originalRenderViewEntity);
            } catch (Throwable restoreFailure) {
                failure.addSuppressed(restoreFailure);
            } finally {
                cameraEntity = null;
                cameraWorld = null;
                originalRenderViewEntity = null;
                restoreFov();
            }
            if (failure instanceof Exception) throw (Exception) failure;
            if (failure instanceof Error) throw (Error) failure;
            throw new RuntimeException(failure);
        }
    }

    @Override
    public void apply(CameraPose pose) throws Exception {
        requireClientThread();
        if (pose == null) throw new IllegalArgumentException("pose");
        if (cameraEntity == null || cameraWorld == null) {
            throw new IllegalStateException("Independent camera is not active");
        }
        if (minecraft.theWorld != cameraWorld) {
            throw new IllegalStateException("Minecraft world changed while camera was active");
        }
        if (minecraft.getRenderViewEntity() != cameraEntity) {
            throw new IllegalStateException("Render-view ownership changed while camera was active");
        }
        applyPose(cameraEntity, pose);
        applyFov(pose);
    }

    private void applyFov(CameraPose pose) {
        if (minecraft.gameSettings != null) {
            minecraft.gameSettings.fovSetting = Math.max(1.0F, Math.min(179.0F, pose.fov));
        }
    }

    private void restoreFov() {
        if (originalFovCaptured && minecraft.gameSettings != null) {
            minecraft.gameSettings.fovSetting = originalFov;
        }
        originalFovCaptured = false;
    }

    private static void applyPose(EntityOtherPlayerMP entity, CameraPose pose) {
        entity.setPositionAndRotation(pose.x, pose.y, pose.z, pose.yaw, pose.pitch);
        entity.prevPosX = entity.lastTickPosX = entity.posX;
        entity.prevPosY = entity.lastTickPosY = entity.posY;
        entity.prevPosZ = entity.lastTickPosZ = entity.posZ;
        entity.prevRotationYaw = entity.rotationYaw;
        entity.prevRotationPitch = entity.rotationPitch;
        entity.setRotationYawHead(pose.yaw);
        entity.motionX = entity.motionY = entity.motionZ = 0.0D;
    }

    @Override
    public void deactivate() throws Exception {
        requireClientThread();
        Entity restore = originalRenderViewEntity;
        try {
            if (restore != null) {
                if (minecraft.theWorld == null || restore.getEntityWorld() != minecraft.theWorld) {
                    restore = minecraft.thePlayer;
                }
                if (restore != null) minecraft.setRenderViewEntity(restore);
            } else if (minecraft.thePlayer != null) {
                minecraft.setRenderViewEntity(minecraft.thePlayer);
            }
        } finally {
            restoreFov();
            cameraEntity = null;
            cameraWorld = null;
            originalRenderViewEntity = null;
        }
    }

    private void requireClientThread() {
        if (!minecraft.isCallingFromMinecraftThread()) {
            throw new IllegalStateException("Camera bridge must run on the Minecraft client thread");
        }
    }
}
