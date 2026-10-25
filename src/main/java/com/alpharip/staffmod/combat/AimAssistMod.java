package com.alpharip.staffmod.combat;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Initial, deliberately bounded aim correction. It only nudges rotation when
 * the target is already near the crosshair; it does not attack or send packets.
 */
public final class AimAssistMod {
    private static final double RANGE = 4.5D;
    private static final float MAX_START_ANGLE = 16.0F;
    private static final float SMOOTHING = 0.12F;
    private static final float MAX_STEP = 2.2F;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        AlphaModule module = AlphaModuleRegistry.find("aim-assist");
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null
                || module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()) return;

        EntityPlayer target = CombatTargeting.getBestTarget(RANGE, CombatTargeting.Priority.CROSSHAIR);
        if (target == null) return;

        double dx = target.posX - mc.thePlayer.posX;
        double dz = target.posZ - mc.thePlayer.posZ;
        double dy = target.posY + target.getEyeHeight()
                - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float targetPitch = (float) (-(Math.atan2(dy, horizontal) * 180.0D / Math.PI));
        float yawDelta = MathHelper.wrapAngleTo180_float(targetYaw - mc.thePlayer.rotationYaw);
        float pitchDelta = MathHelper.wrapAngleTo180_float(targetPitch - mc.thePlayer.rotationPitch);

        if (Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta) > MAX_START_ANGLE) return;

        float yawStep = clamp(yawDelta * SMOOTHING, -MAX_STEP, MAX_STEP);
        float pitchStep = clamp(pitchDelta * SMOOTHING, -MAX_STEP, MAX_STEP);
        mc.thePlayer.rotationYaw += yawStep;
        mc.thePlayer.rotationPitch = MathHelper.clamp_float(mc.thePlayer.rotationPitch + pitchStep, -90.0F, 90.0F);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
