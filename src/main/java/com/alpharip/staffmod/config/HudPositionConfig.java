// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class HudPositionConfig {

    private static final String FILE_NAME =
            "alphastaffmod_hud.properties";

    /*
     * =========================
     * DEFAULTS
     * =========================
     */

    private static final int DEFAULT_CPS_X = 5;
    private static final int DEFAULT_CPS_Y = 28;
    private static final int DEFAULT_CPS_SCALE = 50;

    private static final int DEFAULT_KNOCKBACK_X = 5;
    private static final int DEFAULT_KNOCKBACK_Y = 45;
    private static final int DEFAULT_KNOCKBACK_SCALE = 50;

    private static final int DEFAULT_BHOP_X = 5;
    private static final int DEFAULT_BHOP_Y = 100;
    private static final int DEFAULT_BHOP_SCALE = 50;

    private static final int DEFAULT_FPS_X = 6;
    private static final int DEFAULT_FPS_Y = 6;
    private static final int DEFAULT_PING_X = 6;
    private static final int DEFAULT_PING_Y = 22;
    private static final int DEFAULT_PING_COLOR_INDEX = 0;
    private static final boolean DEFAULT_PING_SHOW_BACKGROUND = true;
    private static final int DEFAULT_FPS_COLOR_INDEX = 0;
    private static final boolean DEFAULT_FPS_SHOW_BACKGROUND = true;
    private static final int DEFAULT_CPS_COLOR_INDEX = 0;
    private static final int DEFAULT_CPS_DISPLAY_MODE = 0;
    private static final boolean DEFAULT_CPS_SHOW_BACKGROUND = true;


    /*
     * =========================
     * VALUES
     * =========================
     */

    private static int cpsX =
            DEFAULT_CPS_X;

    private static int cpsY =
            DEFAULT_CPS_Y;

    private static int cpsScale =
            DEFAULT_CPS_SCALE;

    private static int knockbackX =
            DEFAULT_KNOCKBACK_X;

    private static int knockbackY =
            DEFAULT_KNOCKBACK_Y;

    private static int knockbackScale =
            DEFAULT_KNOCKBACK_SCALE;

    private static int bhopX =
            DEFAULT_BHOP_X;

    private static int bhopY =
            DEFAULT_BHOP_Y;

    private static int bhopScale =
            DEFAULT_BHOP_SCALE;

    private static int fpsX = DEFAULT_FPS_X;
    private static int fpsY = DEFAULT_FPS_Y;
    private static int fpsColorIndex = DEFAULT_FPS_COLOR_INDEX;
    private static boolean fpsShowBackground = DEFAULT_FPS_SHOW_BACKGROUND;
    private static int pingX = DEFAULT_PING_X;
    private static int pingY = DEFAULT_PING_Y;
    private static int pingColorIndex = DEFAULT_PING_COLOR_INDEX;
    private static boolean pingShowBackground = DEFAULT_PING_SHOW_BACKGROUND;
    private static int cpsColorIndex = DEFAULT_CPS_COLOR_INDEX;
    private static int cpsDisplayMode = DEFAULT_CPS_DISPLAY_MODE;
    private static boolean cpsShowBackground = DEFAULT_CPS_SHOW_BACKGROUND;

    private static boolean loaded =
            false;


    /*
     * =========================
     * FILE
     * =========================
     *
     * Lunar's werkdirectory wordt
     * gebruikt als basisdirectory.
     *
     * Er wordt dus geen gebruik
     * gemaakt van:
     *
     * Minecraft.getMinecraft()
     */

    private static File getFile() {

        File workingDirectory =
                new File(
                        System.getProperty(
                                "user.dir"
                        )
                );

        File configDirectory =
                new File(
                        workingDirectory,
                        "config"
                );

        if (!configDirectory.exists()) {

            configDirectory.mkdirs();
        }

        return new File(
                configDirectory,
                FILE_NAME
        );
    }


    /*
     * =========================
     * LOAD
     * =========================
     */

    public static void load() {

        if (loaded) {

            return;
        }

        loaded = true;

        File file =
                getFile();

        if (!file.exists()) {

            save();

            return;
        }

        Properties properties =
                new Properties();

        FileInputStream input =
                null;

        try {

            input =
                    new FileInputStream(
                            file
                    );

            properties.load(input);


            /*
             * =========================
             * CPS
             * =========================
             */

            cpsX =
                    getInt(
                            properties,
                            "cpsX",
                            DEFAULT_CPS_X
                    );

            cpsY =
                    getInt(
                            properties,
                            "cpsY",
                            DEFAULT_CPS_Y
                    );

            cpsScale =
                    getScale(
                            properties,
                            "cpsScale",
                            DEFAULT_CPS_SCALE
                    );
            cpsColorIndex = getInt(properties, "cpsColorIndex", DEFAULT_CPS_COLOR_INDEX);
            if (cpsColorIndex < 0 || cpsColorIndex > 9) cpsColorIndex = DEFAULT_CPS_COLOR_INDEX;
            cpsDisplayMode = getInt(properties, "cpsDisplayMode", DEFAULT_CPS_DISPLAY_MODE);
            if (cpsDisplayMode < 0 || cpsDisplayMode > 2) cpsDisplayMode = DEFAULT_CPS_DISPLAY_MODE;
            cpsShowBackground = Boolean.parseBoolean(properties.getProperty("cpsShowBackground", String.valueOf(DEFAULT_CPS_SHOW_BACKGROUND)));


            /*
             * =========================
             * KNOCKBACK
             * =========================
             */

            knockbackX =
                    getInt(
                            properties,
                            "knockbackX",
                            DEFAULT_KNOCKBACK_X
                    );

            knockbackY =
                    getInt(
                            properties,
                            "knockbackY",
                            DEFAULT_KNOCKBACK_Y
                    );

            knockbackScale =
                    getScale(
                            properties,
                            "knockbackScale",
                            DEFAULT_KNOCKBACK_SCALE
                    );


            /*
             * =========================
             * BHOP
             * =========================
             */

            bhopX =
                    getInt(
                            properties,
                            "bhopX",
                            DEFAULT_BHOP_X
                    );

            bhopY =
                    getInt(
                            properties,
                            "bhopY",
                            DEFAULT_BHOP_Y
                    );

            bhopScale =
                    getScale(
                            properties,
                            "bhopScale",
                            DEFAULT_BHOP_SCALE
                    );

            fpsX = getInt(properties, "fpsX", DEFAULT_FPS_X);
            fpsY = getInt(properties, "fpsY", DEFAULT_FPS_Y);
            fpsColorIndex = getInt(properties, "fpsColorIndex", DEFAULT_FPS_COLOR_INDEX);
            if (fpsColorIndex < 0 || fpsColorIndex > 9) fpsColorIndex = DEFAULT_FPS_COLOR_INDEX;
            fpsShowBackground = Boolean.parseBoolean(properties.getProperty("fpsShowBackground", String.valueOf(DEFAULT_FPS_SHOW_BACKGROUND)));
            pingX = getInt(properties, "pingX", DEFAULT_PING_X);
            pingY = getInt(properties, "pingY", DEFAULT_PING_Y);
            pingColorIndex = getInt(properties, "pingColorIndex", DEFAULT_PING_COLOR_INDEX);
            if (pingColorIndex < 0 || pingColorIndex > 9) pingColorIndex = DEFAULT_PING_COLOR_INDEX;
            pingShowBackground = Boolean.parseBoolean(properties.getProperty("pingShowBackground", String.valueOf(DEFAULT_PING_SHOW_BACKGROUND)));

        } catch (Exception e) {

            System.out.println(
                    "[AlphaStaffMod] Could not load HUD configuration."
            );

            resetValues();

        } finally {

            if (input != null) {

                try {

                    input.close();

                } catch (IOException ignored) {
                }
            }
        }
    }


    /*
     * =========================
     * SAVE
     * =========================
     */

    public static void save() {

        Properties properties =
                new Properties();


        /*
         * =========================
         * CPS
         * =========================
         */

        properties.setProperty(
                "cpsX",
                String.valueOf(cpsX)
        );

        properties.setProperty(
                "cpsY",
                String.valueOf(cpsY)
        );

        properties.setProperty(
                "cpsScale",
                String.valueOf(cpsScale)
        );
        properties.setProperty("cpsColorIndex", String.valueOf(cpsColorIndex));
        properties.setProperty("cpsDisplayMode", String.valueOf(cpsDisplayMode));
        properties.setProperty("cpsShowBackground", String.valueOf(cpsShowBackground));


        /*
         * =========================
         * KNOCKBACK
         * =========================
         */

        properties.setProperty(
                "knockbackX",
                String.valueOf(knockbackX)
        );

        properties.setProperty(
                "knockbackY",
                String.valueOf(knockbackY)
        );

        properties.setProperty(
                "knockbackScale",
                String.valueOf(knockbackScale)
        );


        /*
         * =========================
         * BHOP
         * =========================
         */

        properties.setProperty(
                "bhopX",
                String.valueOf(bhopX)
        );

        properties.setProperty(
                "bhopY",
                String.valueOf(bhopY)
        );

        properties.setProperty(
                "bhopScale",
                String.valueOf(bhopScale)
        );

        properties.setProperty("fpsX", String.valueOf(fpsX));
        properties.setProperty("fpsY", String.valueOf(fpsY));
        properties.setProperty("fpsColorIndex", String.valueOf(fpsColorIndex));
        properties.setProperty("fpsShowBackground", String.valueOf(fpsShowBackground));
        properties.setProperty("pingX", String.valueOf(pingX));
        properties.setProperty("pingY", String.valueOf(pingY));
        properties.setProperty("pingColorIndex", String.valueOf(pingColorIndex));
        properties.setProperty("pingShowBackground", String.valueOf(pingShowBackground));


        FileOutputStream output =
                null;

        try {

            output =
                    new FileOutputStream(
                            getFile()
                    );

            properties.store(
                    output,
                    "Alpha Staff Mod HUD configuration"
            );

        } catch (Exception e) {

            System.out.println(
                    "[AlphaStaffMod] Could not save HUD configuration."
            );

        } finally {

            if (output != null) {

                try {

                    output.close();

                } catch (IOException ignored) {
                }
            }
        }
    }


    /*
     * =========================
     * INTEGER
     * =========================
     */

    private static int getInt(
            Properties properties,
            String key,
            int fallback) {

        try {

            return Integer.parseInt(
                    properties.getProperty(
                            key,
                            String.valueOf(fallback)
                    )
            );

        } catch (Exception e) {

            return fallback;
        }
    }


    /*
     * =========================
     * SCALE
     * =========================
     */

    private static int getScale(
            Properties properties,
            String key,
            int fallback) {

        int value =
                getInt(
                        properties,
                        key,
                        fallback
                );

        if (value < 0) {

            value = 0;
        }

        if (value > 100) {

            value = 100;
        }

        return value;
    }


    /*
     * =========================
     * CPS
     * =========================
     */

    public static int getCpsX() {

        load();

        return cpsX;
    }

    public static int getCpsY() {

        load();

        return cpsY;
    }

    public static int getCpsScale() {

        load();

        return cpsScale;
    }

    public static void setCpsPosition(
            int x,
            int y) {

        load();

        cpsX = x;
        cpsY = y;

        save();
    }

    public static void setCpsScale(
            int scale) {

        load();

        cpsScale =
                clampScale(scale);

        save();
    }

    public static void resetCpsPosition() {

        cpsX =
                DEFAULT_CPS_X;

        cpsY =
                DEFAULT_CPS_Y;

        save();
    }

    public static void resetCps() {

        cpsX =
                DEFAULT_CPS_X;

        cpsY =
                DEFAULT_CPS_Y;

        cpsScale =
                DEFAULT_CPS_SCALE;

        save();
    }


    /*
     * =========================
     * KNOCKBACK
     * =========================
     */

    public static int getKnockbackX() {

        load();

        return knockbackX;
    }

    public static int getKnockbackY() {

        load();

        return knockbackY;
    }

    public static int getKnockbackScale() {

        load();

        return knockbackScale;
    }

    public static void setKnockbackPosition(
            int x,
            int y) {

        load();

        knockbackX = x;
        knockbackY = y;

        save();
    }

    public static void setKnockbackScale(
            int scale) {

        load();

        knockbackScale =
                clampScale(scale);

        save();
    }

    public static void resetKnockbackPosition() {

        knockbackX =
                DEFAULT_KNOCKBACK_X;

        knockbackY =
                DEFAULT_KNOCKBACK_Y;

        save();
    }

    public static void resetKnockback() {

        knockbackX =
                DEFAULT_KNOCKBACK_X;

        knockbackY =
                DEFAULT_KNOCKBACK_Y;

        knockbackScale =
                DEFAULT_KNOCKBACK_SCALE;

        save();
    }


    /*
     * =========================
     * BHOP
     * =========================
     */

    public static int getBhopX() {

        load();

        return bhopX;
    }

    public static int getBhopY() {

        load();

        return bhopY;
    }

    public static int getBhopScale() {

        load();

        return bhopScale;
    }

    public static void setBhopPosition(
            int x,
            int y) {

        load();

        bhopX = x;
        bhopY = y;

        save();
    }

    public static void setBhopScale(
            int scale) {

        load();

        bhopScale =
                clampScale(scale);

        save();
    }

    public static void resetBhopPosition() {

        bhopX =
                DEFAULT_BHOP_X;

        bhopY =
                DEFAULT_BHOP_Y;

        save();
    }

    public static void resetBhop() {

        bhopX =
                DEFAULT_BHOP_X;

        bhopY =
                DEFAULT_BHOP_Y;

        bhopScale =
                DEFAULT_BHOP_SCALE;

        save();
    }


    public static int getCpsColorIndex() { load(); return cpsColorIndex; }

    public static void setCpsColorIndex(int index) { load(); cpsColorIndex = Math.max(0, Math.min(9, index)); save(); }

    public static int getCpsDisplayMode() { load(); return cpsDisplayMode; }

    public static void setCpsDisplayMode(int mode) { load(); cpsDisplayMode = Math.max(0, Math.min(2, mode)); save(); }

    public static boolean isCpsShowBackground() { load(); return cpsShowBackground; }

    public static void setCpsShowBackground(boolean value) { load(); cpsShowBackground = value; save(); }

    public static boolean isFpsShowBackground() { load(); return fpsShowBackground; }

    public static void setFpsShowBackground(boolean value) { load(); fpsShowBackground = value; save(); }

    public static int getPingX() { load(); return pingX; }
    public static int getPingY() { load(); return pingY; }
    public static int getPingColorIndex() { load(); return pingColorIndex; }
    public static boolean isPingShowBackground() { load(); return pingShowBackground; }
    public static void setPingPosition(int x, int y) { load(); pingX = Math.max(0, x); pingY = Math.max(0, y); save(); }
    public static void setPingColorIndex(int index) { load(); pingColorIndex = Math.max(0, Math.min(9, index)); save(); }
    public static void setPingShowBackground(boolean value) { load(); pingShowBackground = value; save(); }

    public static int getFpsX() {
        load();
        return fpsX;
    }

    public static int getFpsY() {
        load();
        return fpsY;
    }

    public static void setFpsPosition(int x, int y) {
        load();
        fpsX = Math.max(0, x);
        fpsY = Math.max(0, y);
        save();
    }

    public static int getFpsColorIndex() {
        load();
        return fpsColorIndex;
    }

    public static void setFpsColorIndex(int index) {
        load();
        fpsColorIndex = Math.max(0, Math.min(9, index));
        save();
    }


    /*
     * =========================
     * RESET EVERYTHING
     * =========================
     */

    public static void resetAll() {
        load();
        resetValues();
        fpsX = DEFAULT_FPS_X;
        fpsY = DEFAULT_FPS_Y;
        pingX = DEFAULT_PING_X;
        pingY = DEFAULT_PING_Y;
        pingColorIndex = DEFAULT_PING_COLOR_INDEX;
        pingShowBackground = DEFAULT_PING_SHOW_BACKGROUND;
        save();
    }

    private static void resetValues() {

        cpsX =
                DEFAULT_CPS_X;

        cpsY =
                DEFAULT_CPS_Y;

        cpsScale =
                DEFAULT_CPS_SCALE;

        knockbackX =
                DEFAULT_KNOCKBACK_X;

        knockbackY =
                DEFAULT_KNOCKBACK_Y;

        knockbackScale =
                DEFAULT_KNOCKBACK_SCALE;

        bhopX =
                DEFAULT_BHOP_X;

        bhopY =
                DEFAULT_BHOP_Y;

        bhopScale =
                DEFAULT_BHOP_SCALE;

        fpsX = DEFAULT_FPS_X;
        fpsY = DEFAULT_FPS_Y;
        fpsShowBackground = DEFAULT_FPS_SHOW_BACKGROUND;
        pingX = DEFAULT_PING_X;
        pingY = DEFAULT_PING_Y;
        pingColorIndex = DEFAULT_PING_COLOR_INDEX;
        pingShowBackground = DEFAULT_PING_SHOW_BACKGROUND;
        cpsColorIndex = DEFAULT_CPS_COLOR_INDEX;
        cpsDisplayMode = DEFAULT_CPS_DISPLAY_MODE;
        cpsShowBackground = DEFAULT_CPS_SHOW_BACKGROUND;
    }


    /*
     * =========================
     * CLAMP
     * =========================
     */

    private static int clampScale(
            int scale) {

        if (scale < 0) {

            return 0;
        }

        if (scale > 100) {

            return 100;
        }

        return scale;
    }
}