// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.hud.InfoHudModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Live and HUD-editor renderer for Potion HUD. */
public final class PotionHudInfoRenderer {
    private static final ResourceLocation INVENTORY = new ResourceLocation("textures/gui/container/inventory.png");
    private PotionHudInfoRenderer() { }

    public static void render(Minecraft mc, boolean preview) {
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        PotionHudInfoModule settings = PotionHudInfoModule.getInstance();
        settings.load();
        if (!preview && mc.currentScreen instanceof GuiChat && !settings.isShowWhileChat()) return;

        Collection<PotionEffect> active = mc.thePlayer.getActivePotionEffects();
        List<PotionEffect> visible = new ArrayList<PotionEffect>();
        for (PotionEffect effect : active) {
            Potion potion = potion(effect.getPotionID());
            PotionHudInfoEffectSetting effectSetting = settings.getEffect(effect.getPotionID());
            if (potion == null || effectSetting == null || !effectSetting.isEnabled()) continue;
            if (!settings.isRenderLevelsAbove4() && effect.getAmplifier() >= 4) continue;
            visible.add(effect);
        }
        if (preview && visible.isEmpty()) addPreview(visible);

        int x = InfoHudModules.getPotionX();
        int y = InfoHudModules.getPotionY();
        boolean mirrored = x > new ScaledResolution(mc).getScaledWidth() / 2;
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(settings.getScale(), settings.getScale(), 1.0F);
        int row = 0;
        for (PotionEffect effect : visible) {
            Potion potion = potion(effect.getPotionID());
            PotionHudInfoEffectSetting effectSetting = settings.getEffect(effect.getPotionID());
            if (potion == null || effectSetting == null || !effectSetting.isEnabled()) { row++; continue; }
            if (shouldFlicker(effect, effectSetting)) { row++; continue; }

            int cursor = 0;
            if (settings.isRenderIcons() && potion.hasStatusIcon()) {
                drawIcon(mc, potion, mirrored ? -18 : 0, row * 20, settings.getOpacity());
                cursor = mirrored ? -22 : 22;
            }
            String custom = effectSetting.getCustomName();
            String name = custom == null || custom.trim().isEmpty()
                    ? PotionHudInfoModule.getPotionName(effect.getPotionID()) : custom;
            String level = amplifier(effect, settings.isRomanNumerals());
            String timer = Potion.getDurationString(effect);
            String styledName = effectSetting.getNameAttachments() + name + level;
            int nameColor = withOpacity(effectSetting.getNameColor(), settings.getOpacity());
            int timerColor = withOpacity(effectSetting.getTimerColor(), settings.getOpacity());
            int nameWidth = mc.fontRendererObj.getStringWidth(styledName);
            int timerWidth = mc.fontRendererObj.getStringWidth(timer);
            int baseX = mirrored ? cursor - nameWidth - timerWidth - 5 : cursor;
            int textY = row * 20 + 5;
            mc.fontRendererObj.drawStringWithShadow(styledName, baseX, textY, nameColor);
            int timerX = mirrored ? cursor - timerWidth : baseX + nameWidth + 5;
            mc.fontRendererObj.drawStringWithShadow(timer, timerX, textY, timerColor);
            row++;
        }
        GL11.glPopMatrix();
    }

    private static void addPreview(List<PotionEffect> visible) {
        int[] ids = {1, 10, 19};
        for (int id : ids) if (potion(id) != null) visible.add(new PotionEffect(id, 2460, 1));
    }
    private static Potion potion(int id) {
        return id >= 0 && id < Potion.potionTypes.length ? Potion.potionTypes[id] : null;
    }
    private static String amplifier(PotionEffect effect, boolean roman) {
        int level = effect.getAmplifier() + 1;
        if (level <= 1) return "";
        if (!roman) return " " + level;
        switch (level) {
            case 2: return " II";
            case 3: return " III";
            case 4: return " IV";
            case 5: return " V";
            default: return " " + level;
        }
    }
    private static boolean shouldFlicker(PotionEffect effect, PotionHudInfoEffectSetting setting) {
        if (!setting.isFlickerWhenLow() || setting.getFlickerStartTime() <= 0) return false;
        int seconds = effect.getDuration() / 20;
        if (seconds > setting.getFlickerStartTime()) return false;
        return (System.currentTimeMillis() / Math.max(1, setting.getFlickerSpeed())) % 2L == 0L;
    }
    private static int withOpacity(int color, int opacity) {
        return (Math.max(0, Math.min(255, opacity)) << 24) | (color & 0x00FFFFFF);
    }
    private static void drawIcon(Minecraft mc, Potion potion, int x, int y, int opacity) {
        mc.getTextureManager().bindTexture(INVENTORY);
        int icon = potion.getStatusIconIndex();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, Math.max(0, Math.min(255, opacity)) / 255.0F);
        Gui.drawModalRectWithCustomSizedTexture(x, y, (icon % 8) * 18, 198 + (icon / 8) * 18, 18, 18, 256F, 256F);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }
}
