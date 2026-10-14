package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central registry and render dispatcher for independent AlphaClient HUD widgets.
 * Widgets are intentionally client-side and only render during the in-game HUD pass.
 */
public final class AlphaHudManager {
    private final List<AlphaHudWidget> widgets = new ArrayList<AlphaHudWidget>();

    public AlphaHudManager() {
        register(new BrandWidget());
        register(new FpsWidget());
        register(new KeystrokesWidget());
    }

    public void register(AlphaHudWidget widget) {
        if (widget == null) return;
        for (AlphaHudWidget existing : widgets) {
            if (existing.getId().equals(widget.getId())) return;
        }
        widgets.add(widget);
    }

    public List<AlphaHudWidget> getWidgets() {
        return Collections.unmodifiableList(widgets);
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.gameSettings == null) return;
        for (AlphaHudWidget widget : widgets) {
            AlphaModule module = AlphaModuleRegistry.find(widget.getModuleId());
            if (module != null && module.isImplemented() && module.isEnabled() && module.isUnlocked()) {
                widget.render(minecraft, minecraft.displayWidth, minecraft.displayHeight, event.partialTicks);
            }
        }
    }

    private static final class BrandWidget implements AlphaHudWidget {
        public String getId() { return "brand"; }
        public String getModuleId() { return "alpha_hud"; }

        public void render(Minecraft mc, int screenWidth, int screenHeight, float partialTicks) {
            Gui.drawRect(7, 7, 118, 34, 0xA815171D);
            Gui.drawRect(7, 7, 9, 34, 0xFFFFD866);
            mc.fontRendererObj.drawStringWithShadow("ALPHA", 15, 11, 0xFFFFD866);
            mc.fontRendererObj.drawStringWithShadow(PermissionService.getStatusText(), 15, 22, 0xFFE0E2E8);
        }
    }

    private static final class FpsWidget implements AlphaHudWidget {
        public String getId() { return "fps"; }
        public String getModuleId() { return "fps_counter"; }

        public void render(Minecraft mc, int screenWidth, int screenHeight, float partialTicks) {
            String value = "FPS  " + Minecraft.getDebugFPS();
            int width = mc.fontRendererObj.getStringWidth(value);
            int x = 7;
            int y = AlphaModuleRegistry.find("alpha_hud") != null
                    && AlphaModuleRegistry.find("alpha_hud").isEnabled() ? 40 : 7;
            Gui.drawRect(x, y, x + width + 14, y + 18, 0xA815171D);
            Gui.drawRect(x, y, x + 2, y + 18, 0xFFFFD866);
            mc.fontRendererObj.drawStringWithShadow(value, x + 7, y + 5, 0xFFFFFFFF);
        }
    }

    private static final class KeystrokesWidget implements AlphaHudWidget {
        public String getId() { return "keystrokes"; }
        public String getModuleId() { return "keystrokes"; }

        public void render(Minecraft mc, int screenWidth, int screenHeight, float partialTicks) {
            int x = 8;
            int y = 54;
            drawKey(mc, "W", x + 22, y, Keyboard.isKeyDown(Keyboard.KEY_W), 20);
            drawKey(mc, "A", x, y + 22, Keyboard.isKeyDown(Keyboard.KEY_A), 20);
            drawKey(mc, "S", x + 22, y + 22, Keyboard.isKeyDown(Keyboard.KEY_S), 20);
            drawKey(mc, "D", x + 44, y + 22, Keyboard.isKeyDown(Keyboard.KEY_D), 20);
            drawKey(mc, "SPACE", x, y + 44, Keyboard.isKeyDown(Keyboard.KEY_SPACE), 64);
            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            drawKey(mc, "SHIFT", x + 68, y + 44, shift, 54);
        }

        private static void drawKey(Minecraft mc, String label, int x, int y, boolean pressed, int width) {
            int background = pressed ? 0xFFFFD866 : 0xCC17191F;
            int foreground = pressed ? 0xFF17191F : 0xFFE8E9ED;
            Gui.drawRect(x, y, x + width, y + 19, background);
            Gui.drawRect(x, y, x + width, y + 1, pressed ? 0xFFFFE9A6 : 0xFF454954);
            mc.fontRendererObj.drawString(label,
                    x + (width - mc.fontRendererObj.getStringWidth(label)) / 2,
                    y + 6, foreground);
        }
    }
}
