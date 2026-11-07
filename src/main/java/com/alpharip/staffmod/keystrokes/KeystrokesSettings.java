// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.keystrokes;

import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Local persistent appearance and visibility settings for the public Keystrokes HUD. */
public final class KeystrokesSettings {
    private static final Properties VALUES = new Properties();
    private static boolean loaded;
    private static int x = 8, y = 80, backgroundAlpha = 150;
    private static int activeColor = 0xFFFFD866, inactiveColor = 0xFF343943, textColor = 0xFFFFFFFF;
    private static boolean background = true, rounded = true;
    private static boolean showMovement = true, showSpace = true, showMouse = true, showCps = true;

    private KeystrokesSettings() { }

    private static File getFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient_keystrokes.properties");
    }

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        File file = getFile();
        if (!file.isFile()) return;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            VALUES.load(in);
            x = integer("x", x); y = integer("y", y);
            backgroundAlpha = clamp(integer("backgroundAlpha", backgroundAlpha), 0, 255);
            activeColor = color("activeColor", activeColor);
            inactiveColor = color("inactiveColor", inactiveColor);
            textColor = color("textColor", textColor);
            background = bool("background", background); rounded = bool("rounded", rounded);
            showMovement = bool("showMovement", showMovement); showSpace = bool("showSpace", showSpace);
            showMouse = bool("showMouse", showMouse); showCps = bool("showCps", showCps);
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not load Keystrokes settings: " + e.getMessage());
        } finally { if (in != null) try { in.close(); } catch (Exception ignored) { } }
    }

    private static int integer(String key, int fallback) { try { return Integer.parseInt(VALUES.getProperty(key, String.valueOf(fallback))); } catch (Exception e) { return fallback; } }
    private static int color(String key, int fallback) { try { return (int)Long.parseLong(VALUES.getProperty(key, String.valueOf(fallback)), 16); } catch (Exception e) { return fallback; } }
    private static boolean bool(String key, boolean fallback) { return Boolean.parseBoolean(VALUES.getProperty(key, String.valueOf(fallback))); }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    public static synchronized void save() {
        load();
        VALUES.setProperty("x", String.valueOf(x)); VALUES.setProperty("y", String.valueOf(y));
        VALUES.setProperty("backgroundAlpha", String.valueOf(backgroundAlpha));
        VALUES.setProperty("activeColor", Integer.toHexString(activeColor));
        VALUES.setProperty("inactiveColor", Integer.toHexString(inactiveColor));
        VALUES.setProperty("textColor", Integer.toHexString(textColor));
        VALUES.setProperty("background", String.valueOf(background)); VALUES.setProperty("rounded", String.valueOf(rounded));
        VALUES.setProperty("showMovement", String.valueOf(showMovement)); VALUES.setProperty("showSpace", String.valueOf(showSpace));
        VALUES.setProperty("showMouse", String.valueOf(showMouse)); VALUES.setProperty("showCps", String.valueOf(showCps));
        File file = getFile(); File parent = file.getParentFile(); if (parent != null && !parent.exists()) parent.mkdirs();
        FileOutputStream out = null;
        try { out = new FileOutputStream(file); VALUES.store(out, "AlphaClient Keystrokes HUD settings"); }
        catch (Exception e) { System.err.println("[AlphaClient] Could not save Keystrokes settings: " + e.getMessage()); }
        finally { if (out != null) try { out.close(); } catch (Exception ignored) { } }
    }

    public static int getX() { load(); return x; } public static int getY() { load(); return y; }
    public static void setPosition(int nx, int ny) { load(); x = Math.max(0, nx); y = Math.max(0, ny); save(); }
    public static boolean hasBackground() { load(); return background; } public static void toggleBackground() { load(); background = !background; save(); }
    public static int getBackgroundAlpha() { load(); return backgroundAlpha; }
    public static void adjustBackgroundAlpha(int delta) { load(); backgroundAlpha = clamp(backgroundAlpha + delta, 0, 255); save(); }
    public static boolean isRounded() { load(); return rounded; } public static void toggleRounded() { load(); rounded = !rounded; save(); }
    public static int getActiveColor() { load(); return activeColor; } public static int getInactiveColor() { load(); return inactiveColor; } public static int getTextColor() { load(); return textColor; }
    public static void cycleActiveColor() { load(); activeColor = next(activeColor); save(); }
    public static void cycleInactiveColor() { load(); inactiveColor = next(inactiveColor); save(); }
    public static void cycleTextColor() { load(); textColor = next(textColor); save(); }
    private static int next(int color) { int[] colors = {0xFFFFD866,0xFF55FF55,0xFF55FFFF,0xFFFF5555,0xFFFFFFFF,0xFFAA55FF}; for(int i=0;i<colors.length;i++) if(colors[i]==color) return colors[(i+1)%colors.length]; return colors[0]; }
    public static boolean showMovement() { load(); return showMovement; } public static boolean showSpace() { load(); return showSpace; } public static boolean showMouse() { load(); return showMouse; } public static boolean showCps() { load(); return showCps; }
    public static void toggleMovement() { load(); showMovement=!showMovement; save(); } public static void toggleSpace() { load(); showSpace=!showSpace; save(); }
    public static void toggleMouse() { load(); showMouse=!showMouse; save(); } public static void toggleCps() { load(); showCps=!showCps; save(); }
}
