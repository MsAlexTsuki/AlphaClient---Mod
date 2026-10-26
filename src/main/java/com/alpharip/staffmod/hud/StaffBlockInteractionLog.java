package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Captures recent client-side block interaction events for a compact HUD log. */
public final class StaffBlockInteractionLog {
    private String lastAction = "No block interaction recorded";
    private long lastAt;

    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("staff-block-interaction-log");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled() || mc == null || event.entityPlayer != mc.thePlayer || event.pos == null) return;
        BlockPos p = event.pos;
        lastAction = event.action + " @ " + p.getX() + ", " + p.getY() + ", " + p.getZ();
        lastAt = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onOverlay(net.minecraftforge.client.event.RenderGameOverlayEvent.Text event) {
        if (event.type != net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.TEXT || !enabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        String age = lastAt == 0L ? "never" : ((System.currentTimeMillis() - lastAt) / 1000L) + "s ago";
        mc.fontRendererObj.drawStringWithShadow("Block interaction log", 6, 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow(lastAction + " | " + age, 6, 18, 0xFFFFFFFF);
    }
}
