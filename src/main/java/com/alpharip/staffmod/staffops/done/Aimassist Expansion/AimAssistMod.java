package com.alpharip.staffmod.aimassistexpansion;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Standalone expansion prototype. It is intentionally not registered with AlphaModule,
 * AlphaClient Core, Supabase, or the existing AimAssist implementation.
 */
public final class AimAssistMod {
    private final AimAssistSettings settings = new AimAssistSettings();
    private final AimAssistController controller = new AimAssistController();

    public AimAssistSettings getSettings() { return settings; }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        controller.update(mc, settings);
    }
}
