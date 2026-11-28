package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.predictions.armor.ArmorDurabilityWarningConfig;
import com.alpharip.staffmod.predictions.armor.ArmorDurabilityWarningRenderer;
import com.alpharip.staffmod.predictions.armor.ArmorDurabilityWarningResult;
import com.alpharip.staffmod.predictions.armor.ArmorDurabilityWarningScanner;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * HUD adapter for the local player's equipped-armor durability warning.
 *
 * This class remains in the hud package because AlphaClient currently imports
 * and registers this exact class from that package.
 */
public final class ArmorDurabilityWarningMod {
    private final ArmorDurabilityWarningConfig config = ArmorDurabilityWarningConfig.defaults();
    private final ArmorDurabilityWarningScanner scanner = new ArmorDurabilityWarningScanner();
    private final ArmorDurabilityWarningRenderer renderer = new ArmorDurabilityWarningRenderer();

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null
                || mc.gameSettings.showDebugInfo) {
            return;
        }

        // The matching module ID must be added to AlphaModuleRegistry before
        // this warning can be controlled through the in-client module menu.
        AlphaModule module = AlphaModuleRegistry.find("armor-durability-warning");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) {
            return;
        }

        ArmorDurabilityWarningResult result =
                scanner.scan(mc.thePlayer.inventory.armorInventory, config);
        if (result.hasWarnings()) {
            renderer.render(mc, result, config);
        }
    }
}
