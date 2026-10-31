package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Draws a clean white plus over the screen center as a first crosshair customization. */
public final class CrosshairCustomizerMod {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null) return;
        AlphaModule module = AlphaModuleRegistry.find("crosshair-customizer");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        int cx = event.resolution.getScaledWidth() / 2;
        int cy = event.resolution.getScaledHeight() / 2;
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.9F);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(cx - 4, cy); GL11.glVertex2f(cx - 1, cy);
        GL11.glVertex2f(cx + 1, cy); GL11.glVertex2f(cx + 4, cy);
        GL11.glVertex2f(cx, cy - 4); GL11.glVertex2f(cx, cy - 1);
        GL11.glVertex2f(cx, cy + 1); GL11.glVertex2f(cx, cy + 4);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}
