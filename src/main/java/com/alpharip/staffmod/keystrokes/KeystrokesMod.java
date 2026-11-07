// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.keystrokes;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Public General-category Keystrokes HUD module. */
public final class KeystrokesMod {
    public KeystrokesMod() { KeystrokesSettings.load(); }

    public static boolean isEnabled() {
        AlphaModule module = AlphaModuleRegistry.find("keystrokes");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.gameSettings.showDebugInfo) return;
        KeystrokesRenderer.render(mc, KeystrokesSettings.getX(), KeystrokesSettings.getY(), HudScaleConfig.get("keystrokes"));
    }

    public static void renderEditorPreview() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) KeystrokesRenderer.render(mc, KeystrokesSettings.getX(), KeystrokesSettings.getY(), HudScaleConfig.get("keystrokes"));
    }
}
