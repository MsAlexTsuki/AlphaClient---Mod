// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.hud.InfoHudModules;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.StatCollector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Own settings and persistence for AlphaClient's independent Potion HUD. */
public final class PotionHudInfoModule {
    private static final PotionHudInfoModule INSTANCE = new PotionHudInfoModule();
    private final Map<Integer, PotionHudInfoEffectSetting> effects = new LinkedHashMap<Integer, PotionHudInfoEffectSetting>();
    private boolean loaded;
    private boolean renderIcons = true;
    private boolean renderLevelsAbove4 = true;
    private boolean showWhileChat;
    private boolean romanNumerals = true;
    private int opacity = 255;

    private PotionHudInfoModule() { }

    public static PotionHudInfoModule getInstance() { return INSTANCE; }

    public synchronized void load() {
        if (loaded) return;
        loaded = true;
        initializeEffects();
        Properties values = new Properties();
        FileInputStream in = null;
        try {
            File file = getFile();
            if (file.isFile()) {
                in = new FileInputStream(file);
                values.load(in);
            }
            renderIcons = bool(values, "renderIcons", renderIcons);
            renderLevelsAbove4 = bool(values, "renderLevelsAbove4", renderLevelsAbove4);
            showWhileChat = bool(values, "showWhileChat", showWhileChat);
            romanNumerals = bool(values, "romanNumerals", romanNumerals);
            opacity = clamp(integer(values, "opacity", opacity), 0, 255);
            for (PotionHudInfoEffectSetting setting : effects.values()) setting.load(values);
            float scale = decimal(values, "scale", HudScaleConfig.get("potion"));
            HudScaleConfig.set("potion", scale);
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not load Potion HUD settings: " + e.getMessage());
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) { }
        }
    }

    private void initializeEffects() {
        Potion[] potions = Potion.potionTypes;
        for (int i = 0; i < potions.length; i++) {
            if (potions[i] != null && !effects.containsKey(Integer.valueOf(i))) {
                effects.put(Integer.valueOf(i), new PotionHudInfoEffectSetting(i));
            }
        }
    }

    private File getFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        File dir = new File(base, "config");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "alphaclient_potionhudinfo.properties");
    }

    public synchronized void save() {
        load();
        Properties values = new Properties();
        values.setProperty("renderIcons", String.valueOf(renderIcons));
        values.setProperty("renderLevelsAbove4", String.valueOf(renderLevelsAbove4));
        values.setProperty("showWhileChat", String.valueOf(showWhileChat));
        values.setProperty("romanNumerals", String.valueOf(romanNumerals));
        values.setProperty("opacity", String.valueOf(opacity));
        values.setProperty("scale", String.valueOf(HudScaleConfig.get("potion")));
        for (PotionHudInfoEffectSetting setting : effects.values()) setting.save(values);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getFile());
            values.store(out, "AlphaClient Potion HUD settings");
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save Potion HUD settings: " + e.getMessage());
        } finally {
            if (out != null) try { out.close(); } catch (Exception ignored) { }
        }
    }

    private static boolean bool(Properties p, String key, boolean fallback) {
        return Boolean.parseBoolean(p.getProperty(key, String.valueOf(fallback)));
    }
    private static int integer(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, String.valueOf(fallback))); }
        catch (Exception ignored) { return fallback; }
    }
    private static float decimal(Properties p, String key, float fallback) {
        try { return Float.parseFloat(p.getProperty(key, String.valueOf(fallback))); }
        catch (Exception ignored) { return fallback; }
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    public synchronized Map<Integer, PotionHudInfoEffectSetting> getEffects() {
        load();
        return java.util.Collections.unmodifiableMap(effects);
    }
    public synchronized PotionHudInfoEffectSetting getEffect(int id) {
        load();
        PotionHudInfoEffectSetting setting = effects.get(Integer.valueOf(id));
        if (setting == null && id >= 0 && id < Potion.potionTypes.length && Potion.potionTypes[id] != null) {
            setting = new PotionHudInfoEffectSetting(id);
            effects.put(Integer.valueOf(id), setting);
        }
        return setting;
    }

    public boolean isRenderIcons() { load(); return renderIcons; }
    public void setRenderIcons(boolean value) { load(); renderIcons = value; save(); }
    public boolean isRenderLevelsAbove4() { load(); return renderLevelsAbove4; }
    public void setRenderLevelsAbove4(boolean value) { load(); renderLevelsAbove4 = value; save(); }
    public boolean isShowWhileChat() { load(); return showWhileChat; }
    public void setShowWhileChat(boolean value) { load(); showWhileChat = value; save(); }
    public boolean isRomanNumerals() { load(); return romanNumerals; }
    public void setRomanNumerals(boolean value) { load(); romanNumerals = value; save(); }
    public int getOpacity() { load(); return opacity; }
    public void setOpacity(int value) { load(); opacity = clamp(value, 0, 255); save(); }
    public float getScale() { load(); return HudScaleConfig.get("potion"); }
    public void setScale(float value) { load(); HudScaleConfig.set("potion", HudScaleConfig.clamp(value)); save(); }

    public static String getPotionName(int id) {
        if (id < 0 || id >= Potion.potionTypes.length || Potion.potionTypes[id] == null) return "Effect " + id;
        return StatCollector.translateToLocal(Potion.potionTypes[id].getName());
    }
}
