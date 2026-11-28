package com.alpharip.staffmod.staffops.ideas;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** First functional Targeting pass: selects and displays the best eligible player. */
public final class TargetingMod {
    private static final double RANGE = 6.0D;
    private EntityPlayer target;

    public static EntityPlayer getTarget() {
        AlphaModule module = AlphaModuleRegistry.find("targeting");
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()) {
            return null;
        }
        return CombatTargeting.getBestTarget(RANGE, CombatTargeting.Priority.CROSSHAIR);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        target = getTarget();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        AlphaModule module = AlphaModuleRegistry.find("targeting");
        Minecraft mc = Minecraft.getMinecraft();
        if (module == null || !module.isEnabled() || !module.isUnlocked()
                || mc == null || mc.thePlayer == null || mc.theWorld == null
                || mc.currentScreen != null || target == null || target.isDead) return;

        double distance = mc.thePlayer.getDistanceToEntity(target);
        String label = "Target: " + target.getName() + "  " + String.format(java.util.Locale.ROOT, "%.1fm", distance);
        mc.fontRendererObj.drawStringWithShadow(label, 6, 6, 0xFFFFD866);
    }
}
