package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/** Minimal text-based WASD and jump keystrokes HUD for Minecraft 1.8.9. */
public final class KeystrokesHud {
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) return;
        AlphaModule module = AlphaModuleRegistry.find("keystrokes");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        String line = key(Keyboard.KEY_W, "W") + " "
                + key(Keyboard.KEY_A, "A") + " "
                + key(Keyboard.KEY_S, "S") + " "
                + key(Keyboard.KEY_D, "D") + "  "
                + key(Keyboard.KEY_SPACE, "JUMP");
        float scale = HudScaleConfig.get("keystrokes");
        GlStateManager.pushMatrix();
        GlStateManager.translate(6.0F, 66.0F, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        mc.fontRendererObj.drawStringWithShadow(line, 0, 0, 0xFFFFFFFF);
        GlStateManager.popMatrix();
    }

    private static String key(int keyCode, String label) {
        return Keyboard.isKeyDown(keyCode) ? "[" + label + "]" : label;
    }
}
