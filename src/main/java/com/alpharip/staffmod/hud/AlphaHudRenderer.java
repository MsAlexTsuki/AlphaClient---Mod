package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/** Minimal, real HUD features used by the initial AlphaClient core. */
public final class AlphaHudRenderer {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.thePlayer == null) {
            return;
        }
        AlphaModule hud = AlphaModuleRegistry.find("alpha_hud");
        if (hud != null && hud.isEnabled()) {
            mc.fontRendererObj.drawStringWithShadow("ALPHA CLIENT",
                    6, 6, 0xFFFFD866);
            mc.fontRendererObj.drawStringWithShadow(PermissionService.getStatusText(),
                    6, 17, 0xFFB8BBC4);
        }
        AlphaModule fps = AlphaModuleRegistry.find("fps_counter");
        if (fps != null && fps.isEnabled()) {
            mc.fontRendererObj.drawStringWithShadow("FPS: " + Minecraft.getDebugFPS(),
                    6, hud != null && hud.isEnabled() ? 29 : 6, 0xFFFFFFFF);
        }

        AlphaModule keystrokes = AlphaModuleRegistry.find("keystrokes");
        if (keystrokes != null && keystrokes.isEnabled()) {
            int x = 8;
            int y = 48;
            drawKey(mc, "W", x + 20, y, Keyboard.isKeyDown(Keyboard.KEY_W));
            drawKey(mc, "A", x, y + 20, Keyboard.isKeyDown(Keyboard.KEY_A));
            drawKey(mc, "S", x + 20, y + 20, Keyboard.isKeyDown(Keyboard.KEY_S));
            drawKey(mc, "D", x + 40, y + 20, Keyboard.isKeyDown(Keyboard.KEY_D));
            drawKey(mc, "SPACE", x, y + 40, Keyboard.isKeyDown(Keyboard.KEY_SPACE), 60);
            drawKey(mc, "SHIFT", x + 64, y + 40,
                    Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), 52);
        }
    }

    private static void drawKey(Minecraft mc, String label, int x, int y, boolean pressed) {
        drawKey(mc, label, x, y, pressed, 18);
    }

    private static void drawKey(Minecraft mc, String label, int x, int y, boolean pressed, int width) {
        int background = pressed ? 0xFFFFD866 : 0xCC202229;
        int foreground = pressed ? 0xFF17191F : 0xFFE8E9ED;
        net.minecraft.client.gui.Gui.drawRect(x, y, x + width, y + 18, background);
        net.minecraft.client.gui.Gui.drawRect(x, y, x + width, y + 1, pressed ? 0xFFFFE9A6 : 0xFF454954);
        mc.fontRendererObj.drawString(label, x + (width - mc.fontRendererObj.getStringWidth(label)) / 2,
                y + 5, foreground);
    }
    }
}