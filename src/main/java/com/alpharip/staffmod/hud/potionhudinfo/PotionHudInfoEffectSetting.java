// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import net.minecraft.util.EnumChatFormatting;
import java.util.Properties;

/** Per-potion appearance and warning configuration. */
public final class PotionHudInfoEffectSetting {
    private final int effectId;
    private int timerColor = 0xFFFFFFFF;
    private int nameColor = 0xFFFFFFFF;
    private boolean chromaName;
    private boolean chromaTimer;
    private boolean enabled = true;
    private boolean bold;
    private boolean italic;
    private boolean underlined;
    private boolean flickerWhenLow = true;
    private int flickerStartTime;
    private int flickerSpeed = 16;
    private String customName = "";

    PotionHudInfoEffectSetting(int effectId) { this.effectId = effectId; }

    private String key(String field) { return "effect." + effectId + "." + field; }
    void load(Properties p) {
        timerColor = integer(p, key("timerColor"), timerColor);
        nameColor = integer(p, key("nameColor"), nameColor);
        chromaName = bool(p, key("chromaName"), chromaName);
        chromaTimer = bool(p, key("chromaTimer"), chromaTimer);
        enabled = bool(p, key("enabled"), enabled);
        bold = bool(p, key("bold"), bold);
        italic = bool(p, key("italic"), italic);
        underlined = bool(p, key("underlined"), underlined);
        flickerWhenLow = bool(p, key("flickerWhenLow"), flickerWhenLow);
        flickerStartTime = Math.max(0, integer(p, key("flickerStartTime"), flickerStartTime));
        flickerSpeed = Math.max(1, integer(p, key("flickerSpeed"), flickerSpeed));
        customName = p.getProperty(key("customName"), customName);
    }
    void save(Properties p) {
        p.setProperty(key("timerColor"), String.valueOf(timerColor));
        p.setProperty(key("nameColor"), String.valueOf(nameColor));
        p.setProperty(key("chromaName"), String.valueOf(chromaName));
        p.setProperty(key("chromaTimer"), String.valueOf(chromaTimer));
        p.setProperty(key("enabled"), String.valueOf(enabled));
        p.setProperty(key("bold"), String.valueOf(bold));
        p.setProperty(key("italic"), String.valueOf(italic));
        p.setProperty(key("underlined"), String.valueOf(underlined));
        p.setProperty(key("flickerWhenLow"), String.valueOf(flickerWhenLow));
        p.setProperty(key("flickerStartTime"), String.valueOf(flickerStartTime));
        p.setProperty(key("flickerSpeed"), String.valueOf(flickerSpeed));
        p.setProperty(key("customName"), customName == null ? "" : customName);
    }
    private static boolean bool(Properties p, String key, boolean fallback) {
        return Boolean.parseBoolean(p.getProperty(key, String.valueOf(fallback)));
    }
    private static int integer(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, String.valueOf(fallback))); }
        catch (Exception ignored) { return fallback; }
    }

    public int getEffectId() { return effectId; }
    public int getTimerColor() { return chromaTimer ? chromaColor() : timerColor; }
    public int getNameColor() { return chromaName ? chromaColor() : nameColor; }
    public int getRawTimerColor() { return timerColor; }
    public int getRawNameColor() { return nameColor; }
    public void setTimerColor(int value) { timerColor = value; changed(); }
    public void setNameColor(int value) { nameColor = value; changed(); }
    public boolean isChromaName() { return chromaName; }
    public void setChromaName(boolean value) { chromaName = value; changed(); }
    public boolean isChromaTimer() { return chromaTimer; }
    public void setChromaTimer(boolean value) { chromaTimer = value; changed(); }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; changed(); }
    public boolean isBold() { return bold; }
    public void setBold(boolean value) { bold = value; changed(); }
    public boolean isItalic() { return italic; }
    public void setItalic(boolean value) { italic = value; changed(); }
    public boolean isUnderlined() { return underlined; }
    public void setUnderlined(boolean value) { underlined = value; changed(); }
    public boolean isFlickerWhenLow() { return flickerWhenLow; }
    public void setFlickerWhenLow(boolean value) { flickerWhenLow = value; changed(); }
    public int getFlickerStartTime() { return flickerStartTime; }
    public void setFlickerStartTime(int value) { flickerStartTime = Math.max(0, value); changed(); }
    public int getFlickerSpeed() { return flickerSpeed; }
    public void setFlickerSpeed(int value) { flickerSpeed = Math.max(1, value); changed(); }
    public String getCustomName() { return customName; }
    public void setCustomName(String value) { customName = value == null ? "" : value; changed(); }

    public String getNameAttachments() {
        StringBuilder value = new StringBuilder();
        if (bold) value.append(EnumChatFormatting.BOLD);
        if (italic) value.append(EnumChatFormatting.ITALIC);
        if (underlined) value.append(EnumChatFormatting.UNDERLINE);
        return value.toString();
    }

    public void copyFrom(PotionHudInfoEffectSetting other) {
        if (other == null) return;
        timerColor = other.timerColor;
        nameColor = other.nameColor;
        chromaName = other.chromaName;
        chromaTimer = other.chromaTimer;
        enabled = other.enabled;
        bold = other.bold;
        italic = other.italic;
        underlined = other.underlined;
        flickerWhenLow = other.flickerWhenLow;
        flickerStartTime = other.flickerStartTime;
        flickerSpeed = other.flickerSpeed;
        customName = other.customName;
        changed();
    }

    private void changed() { PotionHudInfoModule.getInstance().save(); }
    private static int chromaColor() {
        float hue = (System.currentTimeMillis() % 1500L) / 1500.0F;
        return 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, 0.8F, 0.8F) & 0x00FFFFFF);
    }
}
