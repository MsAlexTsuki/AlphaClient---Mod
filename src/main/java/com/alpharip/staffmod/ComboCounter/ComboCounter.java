package com.alpharip.staffmod.ComboCounter;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Counts confirmed consecutive hits and resets when the local player is damaged. */
public final class ComboCounter {
    private static ComboCounter instance;
    private static final long HIT_CONFIRMATION_TIMEOUT_MS = 1000L;
    private static final long COMBO_IDLE_TIMEOUT_MS = 20000L;
    private static final long COMBO_IDLE_TIMEOUT_MS = 20000L;
    private static final String[] COLOR_NAMES = {
            "White", "Red", "Orange", "Yellow", "Green",
            "Cyan", "Blue", "Purple", "Pink", "Rainbow"
    };
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55,
            0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA
    };

    private final Map<UUID, PendingHit> pendingHits = new HashMap<UUID, PendingHit>();
    private int combo;
    private long lastConfirmedHitAt;
    private long lastConfirmedHitAt;
    private int hudX = 5;
    private int hudY = 44;
    private int colorIndex;
    private boolean showBackground = true;
    private boolean loaded;

    public ComboCounter() {
        instance = this;
    }

    public static ComboCounter getInstance() { return instance; }

    public void load() {
        if (loaded) return;
        hudX = 5;
        hudY = 44;
        colorIndex = 0;
        showBackground = true;
        loaded = true;
    }

    private boolean isAvailable() {
        AlphaModule module = AlphaModuleRegistry.find("combo-counter");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (event == null || event.isCanceled() || !isAvailable()
                || !(event.target instanceof EntityPlayer)) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer attacker = event.entityPlayer;
        EntityPlayer target = (EntityPlayer) event.target;
        if (mc == null || mc.thePlayer == null || mc.theWorld == null
                || attacker != mc.thePlayer || target == attacker
                || target.worldObj != mc.theWorld) return;

        // Wait for the target's hurt animation so missed swings do not count.
        pendingHits.put(target.getUniqueID(),
                new PendingHit(target, target.hurtTime, System.currentTimeMillis()));
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event == null || event.entityLiving == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || event.entityLiving != mc.thePlayer) return;

        // Any damage to the local player breaks the current combo.
        combo = 0;
        lastConfirmedHitAt = 0L;
        pendingHits.clear();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || !isAvailable()) {
            pendingHits.clear();
            combo = 0;
            lastConfirmedHitAt = 0L;
            return;
        }

        long now = System.currentTimeMillis();
        if (combo > 0 && lastConfirmedHitAt > 0L
                && now - lastConfirmedHitAt >= COMBO_IDLE_TIMEOUT_MS) {
            combo = 0;
            lastConfirmedHitAt = 0L;
        }

        Iterator<Map.Entry<UUID, PendingHit>> iterator = pendingHits.entrySet().iterator();
        while (iterator.hasNext()) {
            PendingHit pending = iterator.next().getValue();
            EntityPlayer target = pending.target;
            if (target == null || target.isDead || target.worldObj != mc.theWorld
                    || now - pending.createdAt > HIT_CONFIRMATION_TIMEOUT_MS) {
                iterator.remove();
                continue;
            }

            if (target.hurtTime > pending.lastObservedHurtTime) {
                combo++;
                lastConfirmedHitAt = now;
                iterator.remove();
            } else {
                pending.lastObservedHurtTime = target.hurtTime;
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        load();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.gameSettings.showDebugInfo
                || mc.thePlayer == null || !isAvailable()) return;

        String text = getDisplayText();
        float scale = HudScaleConfig.get("comboCounter");
        GlStateManager.pushMatrix();
        GlStateManager.translate(hudX, hudY, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        int width = mc.fontRendererObj.getStringWidth(text) + 10;
        if (showBackground) {
            Gui.drawRect(-3, -3, width, mc.fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            Gui.drawRect(-3, -3, width, -2, 0xFFFFD866);
        }
        mc.fontRendererObj.drawStringWithShadow(text, 0, 0, getTextColor());
        GlStateManager.popMatrix();
    }

    public int getCombo() { return combo; }
    public String getDisplayText() { return "Combo: " + combo; }
    public int getHudX() { load(); return hudX; }
    public int getHudY() { load(); return hudY; }

    public void setHudPosition(int x, int y) {
        load();
        hudX = Math.max(0, x);
        hudY = Math.max(0, y);
        saveProfileSettings();
    }

    public void resetHudSettings() {
        load();
        hudX = 5;
        hudY = 44;
        colorIndex = 0;
        showBackground = true;
        HudScaleConfig.set("comboCounter", 1.0F);
        saveProfileSettings();
    }

    public boolean isShowBackground() { load(); return showBackground; }

    public void setShowBackground(boolean value) {
        load();
        showBackground = value;
        saveProfileSettings();
    }

    public String getColorName() { load(); return COLOR_NAMES[colorIndex]; }

    public void cycleColor() {
        load();
        colorIndex = (colorIndex + 1) % COLOR_NAMES.length;
        saveProfileSettings();
    }

    public int getTextColor() {
        load();
        if (colorIndex == COLOR_NAMES.length - 1) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0F;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);
        }
        return COLORS[colorIndex];
    }

    /** Persist this HUD's settings in the selected AlphaClient profile. */
    public void saveProfileSettings() {
        if (ModuleProfileManager.isApplyingModuleStates()) return;
        JsonObject settings = ModuleProfileManager.getSelectedModule("combo-counter");
        settings.addProperty("hudX", hudX);
        settings.addProperty("hudY", hudY);
        settings.addProperty("colorIndex", colorIndex);
        settings.addProperty("showBackground", showBackground);
        settings.addProperty("scale", HudScaleConfig.get("comboCounter"));
        ModuleProfileManager.setSelectedModule("combo-counter", settings);
    }

    /** Restore this HUD's settings when the active profile changes. */
    public void applyProfileSettings(JsonObject settings) {
        load();
        int x = 5, y = 44, savedColor = 0;
        boolean background = true;
        float scale = 1.0F;
        try { if (settings != null && settings.has("hudX")) x = settings.get("hudX").getAsInt(); } catch (Exception ignored) { }
        try { if (settings != null && settings.has("hudY")) y = settings.get("hudY").getAsInt(); } catch (Exception ignored) { }
        try { if (settings != null && settings.has("colorIndex")) savedColor = settings.get("colorIndex").getAsInt(); } catch (Exception ignored) { }
        try { if (settings != null && settings.has("showBackground")) background = settings.get("showBackground").getAsBoolean(); } catch (Exception ignored) { }
        try { if (settings != null && settings.has("scale")) scale = settings.get("scale").getAsFloat(); } catch (Exception ignored) { }
        hudX = Math.max(0, x);
        hudY = Math.max(0, y);
        colorIndex = Math.max(0, Math.min(COLOR_NAMES.length - 1, savedColor));
        showBackground = background;
        HudScaleConfig.set("comboCounter", scale);
    }


    private static final class PendingHit {
        private final EntityPlayer target;
        private int lastObservedHurtTime;
        private final long createdAt;

        private PendingHit(EntityPlayer target, int hurtTime, long createdAt) {
            this.target = target;
            this.lastObservedHurtTime = hurtTime;
            this.createdAt = createdAt;
        }
    }

}
