package com.alpharip.staffmod.profile;

import com.google.gson.JsonObject;

public class Profile {

    private String profileName;

    /*
     * =========================================================
     * BHOP
     * =========================================================
     */

    private String bhopName = "BHOP";
    private String bhopColor = "#F2EE4D";

    private boolean bhopEnabled = false;
    private boolean shiftUpEnabled = false;
    private int speedLevel = 2;


    /*
     * =========================================================
     * MODULES
     * =========================================================
     *
     * Extra module settings worden hierin opgeslagen zodat
     * nieuwe modules later toegevoegd kunnen worden zonder
     * het profielmodel opnieuw te moeten bouwen.
     *
     * DUBBLE JUMP:
     * "DUBBLE_JUMP"
     *
     * NOFALL:
     * "NOFALL"
     */

    private JsonObject modules = new JsonObject();


    /*
     * =========================================================
     * COLORS
     * =========================================================
     */

    private JsonObject colors = new JsonObject();


    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    public Profile() {
    }


    public Profile(String profileName) {

        this.profileName =
                profileName;
    }


    /*
     * =========================================================
     * PROFILE NAME
     * =========================================================
     */

    public String getProfileName() {

        return profileName;
    }


    public void setProfileName(
            String profileName) {

        this.profileName =
                profileName;
    }


    /*
     * =========================================================
     * BHOP NAME
     * =========================================================
     */

    public String getBhopName() {

        return bhopName;
    }


    public void setBhopName(
            String bhopName) {

        if (bhopName == null
                || bhopName.trim().isEmpty()) {

            this.bhopName =
                    "BHOP";

            return;
        }

        this.bhopName =
                bhopName;
    }


    /*
     * =========================================================
     * BHOP COLOR
     * =========================================================
     */

    public String getBhopColor() {

        return bhopColor;
    }


    public void setBhopColor(
            String color) {

        if (color == null
                || color.trim().isEmpty()) {

            this.bhopColor =
                    "#F2EE4D";

            return;
        }

        this.bhopColor =
                color;
    }


    /*
     * =========================================================
     * BHOP ENABLED
     * =========================================================
     */

    public boolean isBhopEnabled() {

        return bhopEnabled;
    }


    public void setBhopEnabled(
            boolean enabled) {

        this.bhopEnabled =
                enabled;
    }


    /*
     * =========================================================
     * SHIFT UP
     * =========================================================
     */

    public boolean isShiftUpEnabled() {

        return shiftUpEnabled;
    }


    public void setShiftUpEnabled(
            boolean enabled) {

        this.shiftUpEnabled =
                enabled;
    }


    /*
     * =========================================================
     * SPEED
     * =========================================================
     */

    public int getSpeedLevel() {

        return speedLevel;
    }


    public void setSpeedLevel(
            int speedLevel) {

        if (speedLevel < 1) {
            speedLevel = 1;
        }

        if (speedLevel > 20) {
            speedLevel = 20;
        }

        this.speedLevel =
                speedLevel;
    }


    /*
     * =========================================================
     * MODULES
     * =========================================================
     */

    public JsonObject getModules() {

        if (modules == null) {
            modules =
                    new JsonObject();
        }

        return modules;
    }


    public void setModules(
            JsonObject modules) {

        if (modules == null) {
            this.modules =
                    new JsonObject();

            return;
        }

        this.modules =
                modules;
    }


    public boolean getModule(
            String moduleName) {

        if (moduleName == null) {
            return false;
        }

        if (!getModules().has(moduleName)) {
            return false;
        }

        try {

            return getModules()
                    .get(moduleName)
                    .getAsBoolean();

        } catch (Exception ignored) {

            return false;
        }
    }


    public void setModule(
            String moduleName,
            boolean enabled) {

        if (moduleName == null
                || moduleName.trim().isEmpty()) {

            return;
        }

        getModules().addProperty(
                moduleName,
                enabled
        );
    }


    /*
     * =========================================================
     * DUBBLE JUMP
     * =========================================================
     */

    public boolean isDubbleJumpEnabled() {

        return getModule(
                "DUBBLE_JUMP"
        );
    }


    public void setDubbleJumpEnabled(
            boolean enabled) {

        setModule(
                "DUBBLE_JUMP",
                enabled
        );
    }


    /*
     * =========================================================
     * NOFALL
     * =========================================================
     */

    public boolean isNoFallEnabled() {

        return getModule(
                "NOFALL"
        );
    }


    public void setNoFallEnabled(
            boolean enabled) {

        setModule(
                "NOFALL",
                enabled
        );
    }


    /*
     * =========================================================
     * COLORS
     * =========================================================
     */

    public JsonObject getColors() {

        if (colors == null) {
            colors =
                    new JsonObject();
        }

        return colors;
    }


    public void setColors(
            JsonObject colors) {

        if (colors == null) {
            this.colors =
                    new JsonObject();

            return;
        }

        this.colors =
                colors;
    }


    public String getColor(
            String colorName) {

        if (colorName == null) {
            return "#F2EE4D";
        }

        if (!getColors().has(colorName)) {
            return "#F2EE4D";
        }

        try {

            String value =
                    getColors()
                            .get(colorName)
                            .getAsString();

            if (value == null
                    || value.trim().isEmpty()) {

                return "#F2EE4D";
            }

            return value;

        } catch (Exception ignored) {

            return "#F2EE4D";
        }
    }


    public void setColor(
            String colorName,
            String color) {

        if (colorName == null
                || colorName.trim().isEmpty()) {

            return;
        }

        if (color == null
                || color.trim().isEmpty()) {

            color =
                    "#F2EE4D";
        }

        getColors().addProperty(
                colorName,
                color
        );
    }
}