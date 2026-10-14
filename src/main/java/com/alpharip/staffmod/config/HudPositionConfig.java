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
    private static final int DEFAULT_CPS_Y = 5;
    private static final int DEFAULT_CPS_SCALE = 50;

    private static final int DEFAULT_KNOCKBACK_X = 5;
    private static final int DEFAULT_KNOCKBACK_Y = 45;
    private static final int DEFAULT_KNOCKBACK_SCALE = 50;

    private static final int DEFAULT_BHOP_X = 5;
    private static final int DEFAULT_BHOP_Y = 100;
    private static final int DEFAULT_BHOP_SCALE = 50;


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


    /*
     * =========================
     * RESET EVERYTHING
     * =========================
     */

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