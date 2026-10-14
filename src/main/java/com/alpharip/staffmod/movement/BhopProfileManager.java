package com.alpharip.staffmod.movement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 *
 * Central profile manager for all Staff Mods.
 *
 * The class keeps the existing BHOP fields for backwards
 * compatibility, while also providing a generic "modules"
 * section for CPS, Knockback, mod colors and future modules.
 */
public final class BhopProfileManager {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final List<BhopProfile> profiles =
            new ArrayList<BhopProfile>();

    private static String selectedProfile =
            "Default";

    private static boolean loaded =
            false;

    /*
     * =========================================================
     * MOD COLORS
     * =========================================================
     */

    private static final String MOD_COLORS_MODULE =
            "modColors";

    private static final String DEFAULT_COLOR =
            "#F2EE4D";


    private BhopProfileManager() {
    }


    /*
     * =========================================================
     * PROFILE
     * =========================================================
     */

    public static class BhopProfile {

        private String profileName;

        /*
         * Existing BHOP settings.
         *
         * These remain here so existing profiles continue
         * to work.
         */
        private String bhopName;
        private String color;

        private boolean bhopEnabled;
        private boolean shiftUpEnabled;

        private int speedLevel;

        /*
         * Generic module settings.
         *
         * Example:
         *
         * modules: {
         *     cpsHud: {...},
         *     knockback: {...},
         *     modColors: {
         *         CPS: "#F2EE4D",
         *         BHOP_PROFILE_NAME: "#FFFFFF",
         *         BHOP_SPEED: "#55FF55"
         *     }
         * }
         */
        private JsonObject modules =
                new JsonObject();

        public BhopProfile() {
        }

        public BhopProfile(
                String profileName,
                String bhopName,
                String color,
                boolean bhopEnabled,
                boolean shiftUpEnabled,
                int speedLevel) {

            this.profileName =
                    profileName;

            this.bhopName =
                    bhopName;

            this.color =
                    color;

            this.bhopEnabled =
                    bhopEnabled;

            this.shiftUpEnabled =
                    shiftUpEnabled;

            this.speedLevel =
                    speedLevel;

            this.modules =
                    new JsonObject();
        }

        public String getProfileName() {
            return profileName;
        }

        public String getBhopName() {
            return bhopName;
        }

        public String getColor() {
            return color;
        }

        public boolean isBhopEnabled() {
            return bhopEnabled;
        }

        public boolean isShiftUpEnabled() {
            return shiftUpEnabled;
        }

        public int getSpeedLevel() {
            return speedLevel;
        }

        public JsonObject getModules() {

            if (modules == null) {

                modules =
                        new JsonObject();
            }

            return modules;
        }


        /*
         * =====================================================
         * GENERIC MODULE SETTINGS
         * =====================================================
         */

        public JsonObject getModule(
                String moduleName) {

            if (moduleName == null
                    || moduleName.trim().isEmpty()) {

                return new JsonObject();
            }

            moduleName =
                    moduleName.trim();

            JsonElement element =
                    getModules().get(
                            moduleName
                    );

            if (element != null
                    && element.isJsonObject()) {

                return element.getAsJsonObject();
            }

            JsonObject module =
                    new JsonObject();

            getModules().add(
                    moduleName,
                    module
            );

            return module;
        }


        public void setModule(
                String moduleName,
                JsonObject settings) {

            if (moduleName == null
                    || moduleName.trim().isEmpty()) {

                return;
            }

            if (settings == null) {

                settings =
                        new JsonObject();
            }

            getModules().add(
                    moduleName.trim(),
                    settings
            );
        }
    }


    /*
     * =========================================================
     * FILE LOCATION
     * =========================================================
     */

    private static File getDirectory() {

        File configDirectory =
                new File(
                        System.getProperty(
                                "user.dir"
                        ),
                        "config"
                );

        File directory =
                new File(
                        configDirectory,
                        "alphastaffmod"
                );

        if (!directory.exists()) {

            directory.mkdirs();
        }

        return directory;
    }


    private static File getFile() {

        return new File(
                getDirectory(),
                "bhop_profiles.json"
        );
    }


    /*
     * =========================================================
     * LEGACY MOD COLOR FILE
     * =========================================================
     *
     * Wordt alleen gebruikt om bestaande
     * modcolors.json instellingen één keer
     * naar profielen te migreren.
     */

    private static File getLegacyColorFile() {

        return new File(
                getDirectory(),
                "modcolors.json"
        );
    }


    /*
     * =========================================================
     * LOAD
     * =========================================================
     */

    public static void load() {

        if (loaded) {
            return;
        }

        loaded = true;

        File file =
                getFile();

        if (!file.exists()) {

            createDefaultProfile();

            migrateProfileColors();

            save();

            return;
        }

        try {

            FileReader reader =
                    new FileReader(file);

            JsonElement root =
                    new JsonParser().parse(reader);

            reader.close();

            if (!root.isJsonObject()) {

                createDefaultProfile();

                migrateProfileColors();

                save();

                return;
            }

            JsonObject object =
                    root.getAsJsonObject();

            if (object.has(
                    "selectedProfile"
            )) {

                selectedProfile =
                        object.get(
                                "selectedProfile"
                        ).getAsString();
            }

            profiles.clear();

            if (object.has("profiles")
                    && object.get(
                    "profiles"
            ).isJsonArray()) {

                JsonArray array =
                        object.getAsJsonArray(
                                "profiles"
                        );

                for (JsonElement element :
                        array) {

                    if (!element.isJsonObject()) {
                        continue;
                    }

                    JsonObject profileObject =
                            element.getAsJsonObject();

                    String profileName =
                            getString(
                                    profileObject,
                                    "profileName",
                                    "Default"
                            );

                    String bhopName =
                            getString(
                                    profileObject,
                                    "bhopName",
                                    "BHOP"
                            );

                    String color =
                            getString(
                                    profileObject,
                                    "color",
                                    DEFAULT_COLOR
                            );

                    boolean bhopEnabled =
                            getBoolean(
                                    profileObject,
                                    "bhopEnabled",
                                    false
                            );

                    boolean shiftUpEnabled =
                            getBoolean(
                                    profileObject,
                                    "shiftUpEnabled",
                                    false
                            );

                    /*
                     * Definitive BHOP system:
                     *
                     * Speed I - XX
                     */
                    int speedLevel =
                            getInt(
                                    profileObject,
                                    "speedLevel",
                                    2
                            );

                    if (speedLevel < 1
                            || speedLevel > 20) {

                        speedLevel = 2;
                    }

                    BhopProfile profile =
                            new BhopProfile(
                                    profileName,
                                    bhopName,
                                    color,
                                    bhopEnabled,
                                    shiftUpEnabled,
                                    speedLevel
                            );

                    /*
                     * Generic module data.
                     *
                     * Old profiles simply don't have
                     * this field, which is fine.
                     */
                    if (profileObject.has(
                            "modules"
                    )
                            && profileObject.get(
                            "modules"
                    ).isJsonObject()) {

                        profile.modules =
                                profileObject
                                        .getAsJsonObject(
                                                "modules"
                                        );
                    }

                    profiles.add(
                            profile
                    );
                }
            }

            if (profiles.isEmpty()) {

                createDefaultProfile();

                migrateProfileColors();

                save();

                return;
            }

            if (findProfile(
                    selectedProfile
            ) == null) {

                selectedProfile =
                        profiles.get(0)
                                .getProfileName();
            }

            /*
             * Zorg dat ieder profiel zijn
             * eigen modColors-sectie heeft.
             *
             * Bestaande waarden worden nooit
             * overschreven.
             */
            boolean colorsChanged =
                    migrateProfileColors();

            if (colorsChanged) {

                save();
            }

        } catch (Exception e) {

            System.out.println(
                    "[AlphaStaffMod] Could not load profiles."
            );

            e.printStackTrace();

            profiles.clear();

            createDefaultProfile();

            migrateProfileColors();

            save();
        }
    }


    /*
     * =========================================================
     * SAVE
     * =========================================================
     */

    public static void save() {

        File file =
                getFile();

        try {

            JsonObject root =
                    new JsonObject();

            root.addProperty(
                    "selectedProfile",
                    selectedProfile
            );

            JsonArray array =
                    new JsonArray();

            for (BhopProfile profile :
                    profiles) {

                JsonObject object =
                        new JsonObject();

                object.addProperty(
                        "profileName",
                        profile.getProfileName()
                );

                /*
                 * Existing BHOP data.
                 */
                object.addProperty(
                        "bhopName",
                        profile.getBhopName()
                );

                object.addProperty(
                        "color",
                        profile.getColor()
                );

                object.addProperty(
                        "bhopEnabled",
                        profile.isBhopEnabled()
                );

                object.addProperty(
                        "shiftUpEnabled",
                        profile.isShiftUpEnabled()
                );

                object.addProperty(
                        "speedLevel",
                        profile.getSpeedLevel()
                );

                /*
                 * Generic module data.
                 */
                object.add(
                        "modules",
                        profile.getModules()
                );

                array.add(
                        object
                );
            }

            root.add(
                    "profiles",
                    array
            );

            FileWriter writer =
                    new FileWriter(file);

            GSON.toJson(
                    root,
                    writer
            );

            writer.flush();
            writer.close();

        } catch (Exception e) {

            System.out.println(
                    "[AlphaStaffMod] Could not save profiles."
            );

            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * DEFAULT PROFILE
     * =========================================================
     */

    private static void createDefaultProfile() {

        if (findProfile("Default") != null) {
            return;
        }

        BhopProfile profile =
                new BhopProfile(
                        "Default",
                        "BHOP",
                        DEFAULT_COLOR,
                        false,
                        false,
                        2
                );

        profiles.add(
                profile
        );

        selectedProfile =
                "Default";
    }


    /*
     * =========================================================
     * PROFILE ACCESS
     * =========================================================
     */

    public static List<BhopProfile> getProfiles() {

        load();

        return new ArrayList<BhopProfile>(
                profiles
        );
    }


    public static String getSelectedProfileName() {

        load();

        return selectedProfile;
    }


    public static BhopProfile getSelectedProfile() {

        load();

        return findProfile(
                selectedProfile
        );
    }


    private static BhopProfile findProfile(
            String name) {

        if (name == null) {
            return null;
        }

        for (BhopProfile profile :
                profiles) {

            if (profile.getProfileName()
                    .equalsIgnoreCase(name)) {

                return profile;
            }
        }

        return null;
    }


    /*
     * =========================================================
     * SELECT PROFILE
     * =========================================================
     */

    public static boolean selectProfile(
            String name) {

        load();

        BhopProfile profile =
                findProfile(name);

        if (profile == null) {
            return false;
        }

        selectedProfile =
                profile.getProfileName();

        save();

        /*
         * Immediately load the newly selected profile into BHOP.
         * This keeps speed, toggle state and Jump Boost profile-specific.
         */
        BhopMode.loadSelectedProfile();

        return true;
    }


    /*
     * =========================================================
     * CREATE / SAVE PROFILE
     * =========================================================
     */

    public static boolean saveProfile(
            String profileName,
            String bhopName,
            String color,
            boolean bhopEnabled,
            boolean shiftUpEnabled,
            int speedLevel) {

        load();

        if (profileName == null) {
            return false;
        }

        profileName =
                profileName.trim();

        if (profileName.isEmpty()) {
            return false;
        }

        if (bhopName == null
                || bhopName.trim().isEmpty()) {

            bhopName =
                    "BHOP";
        }

        if (color == null
                || color.trim().isEmpty()) {

            color =
                    DEFAULT_COLOR;
        }

        if (speedLevel < 1
                || speedLevel > 20) {

            speedLevel = 2;
        }

        BhopProfile existing =
                findProfile(
                        profileName
                );

        /*
         * =====================================================
         * BESTAAND PROFIEL
         * =====================================================
         *
         * Bij een bestaand profiel blijven de bestaande
         * module-instellingen behouden.
         *
         * Hierdoor blijven de eigen kleuren van dat
         * profiel gewoon bestaan.
         */

        if (existing != null) {

            existing.bhopName =
                    bhopName.trim();

            existing.color =
                    color.trim();

            existing.bhopEnabled =
                    bhopEnabled;

            existing.shiftUpEnabled =
                    shiftUpEnabled;

            existing.speedLevel =
                    speedLevel;

            selectedProfile =
                    profileName;

            ensureProfileModColors(
                    existing,
                    readLegacyModColors()
            );

            save();

            return true;
        }

        /*
         * =====================================================
         * NIEUW PROFIEL
         * =====================================================
         *
         * Andere toekomstige module-instellingen mogen
         * worden overgenomen, maar modColors wordt
         * ALTIJD volledig opnieuw aangemaakt.
         *
         * Daardoor start elk nieuw profiel met:
         *
         * #F2EE4D
         */

        JsonObject preservedModules =
                null;

        BhopProfile currentProfile =
                findProfile(
                        selectedProfile
                );

        if (currentProfile != null) {

            preservedModules =
                    cloneJsonObject(
                            currentProfile.getModules()
                    );
        }

        if (preservedModules == null) {

            preservedModules =
                    new JsonObject();
        }

        /*
         * Oude kleurinstellingen van het
         * vorige profiel verwijderen.
         */
        preservedModules.remove(
                MOD_COLORS_MODULE
        );

        BhopProfile newProfile =
                new BhopProfile(
                        profileName,
                        bhopName.trim(),
                        DEFAULT_COLOR,
                        bhopEnabled,
                        shiftUpEnabled,
                        speedLevel
                );

        newProfile.modules =
                preservedModules;

        /*
         * Nieuw profiel krijgt zijn eigen
         * standaard kleuren.
         */
        createDefaultModColors(
                newProfile
        );

        profiles.add(
                newProfile
        );

        selectedProfile =
                profileName;

        save();

        return true;
    }


    /*
     * =========================================================
     * CREATE DEFAULT MOD COLORS
     * =========================================================
     *
     * Wordt gebruikt wanneer een volledig
     * nieuw profiel wordt aangemaakt.
     */

    private static void createDefaultModColors(
            BhopProfile profile) {

        if (profile == null) {
            return;
        }

        JsonObject colors =
                new JsonObject();

        colors.addProperty(
                "CPS",
                DEFAULT_COLOR
        );

        colors.addProperty(
                "BHOP_PROFILE_NAME",
                DEFAULT_COLOR
        );

        colors.addProperty(
                "BHOP_SPEED",
                DEFAULT_COLOR
        );

        colors.addProperty(
                "KNOCKBACK",
                DEFAULT_COLOR
        );

        profile.getModules().add(
                MOD_COLORS_MODULE,
                colors
        );
    }


    /*
     * =========================================================
     * DELETE PROFILE
     * =========================================================
     */

    public static boolean deleteProfile(
            String name) {

        load();

        BhopProfile profile =
                findProfile(name);

        if (profile == null) {
            return false;
        }

        if (profile.getProfileName()
                .equalsIgnoreCase("Default")) {

            return false;
        }

        profiles.remove(profile);

        if (selectedProfile
                .equalsIgnoreCase(name)) {

            if (!profiles.isEmpty()) {

                selectedProfile =
                        profiles.get(0)
                                .getProfileName();

            } else {

                createDefaultProfile();
            }
        }

        save();

        return true;
    }


    /*
     * =========================================================
     * SELECTED MOD COLORS
     * =========================================================
     */

    public static JsonObject getSelectedModColors() {

        load();

        BhopProfile profile =
                findProfile(
                        selectedProfile
                );

        if (profile == null) {

            return new JsonObject();
        }

        JsonElement element =
                profile.getModules()
                        .get(
                                MOD_COLORS_MODULE
                        );

        if (element != null
                && element.isJsonObject()) {

            return element.getAsJsonObject();
        }

        return new JsonObject();
    }


    /*
     * =========================================================
     * GET SELECTED MOD COLOR
     * =========================================================
     */

    public static String getSelectedModColor(
            String modName,
            String fallback) {

        if (modName == null
                || modName.trim().isEmpty()) {

            return fallback;
        }

        if (fallback == null
                || fallback.trim().isEmpty()) {

            fallback =
                    DEFAULT_COLOR;
        }

        JsonObject colors =
                getSelectedModColors();

        String key =
                modName.trim()
                        .toUpperCase();

        if (!colors.has(key)
                || colors.get(key) == null
                || !colors.get(key).isJsonPrimitive()) {

            return fallback;
        }

        String value;

        try {

            value =
                    colors.get(
                            key
                    ).getAsString();

        } catch (Exception e) {

            return fallback;
        }

        value =
                normalizeColor(value);

        if (value == null) {

            return fallback;
        }

        return value;
    }


    /*
     * =========================================================
     * ENSURE SELECTED MOD COLOR
     * =========================================================
     */

    public static void ensureSelectedModColor(
            String modName,
            String defaultColor) {

        if (modName == null
                || modName.trim().isEmpty()) {

            return;
        }

        load();

        BhopProfile profile =
                findProfile(
                        selectedProfile
                );

        if (profile == null) {
            return;
        }

        String key =
                modName.trim()
                        .toUpperCase();

        JsonObject colors =
                getOrCreateModColors(
                        profile
                );

        if (colors.has(key)) {
            return;
        }

        String value =
                normalizeColor(
                        defaultColor
                );

        if (value == null) {

            value =
                    DEFAULT_COLOR;
        }

        colors.addProperty(
                key,
                value
        );

        save();
    }


    /*
     * =========================================================
     * SET SELECTED MOD COLOR
     * =========================================================
     */

    public static void setSelectedModColor(
            String modName,
            String color) {

        if (modName == null
                || modName.trim().isEmpty()) {

            return;
        }

        load();

        BhopProfile profile =
                findProfile(
                        selectedProfile
                );

        if (profile == null) {
            return;
        }

        String normalizedColor =
                normalizeColor(
                        color
                );

        if (normalizedColor == null) {

            normalizedColor =
                    DEFAULT_COLOR;
        }

        String key =
                modName.trim()
                        .toUpperCase();

        JsonObject colors =
                getOrCreateModColors(
                        profile
                );

        colors.addProperty(
                key,
                normalizedColor
        );

        /*
         * Houd de oude BHOP color property
         * synchroon met de profielnaamkleur
         * voor backwards compatibility.
         */
        if ("BHOP_PROFILE_NAME".equals(key)) {

            profile.color =
                    normalizedColor;
        }

        save();
    }


    /*
     * =========================================================
     * SET MULTIPLE SELECTED MOD COLORS
     * =========================================================
     */

    public static void setSelectedModColors(
            Map<String, String> newColors) {

        if (newColors == null
                || newColors.isEmpty()) {

            return;
        }

        load();

        BhopProfile profile =
                findProfile(
                        selectedProfile
                );

        if (profile == null) {
            return;
        }

        JsonObject colors =
                getOrCreateModColors(
                        profile
                );

        for (Map.Entry<String, String> entry :
                newColors.entrySet()) {

            String name =
                    entry.getKey();

            if (name == null
                    || name.trim().isEmpty()) {

                continue;
            }

            String normalizedColor =
                    normalizeColor(
                            entry.getValue()
                    );

            if (normalizedColor == null) {

                continue;
            }

            String key =
                    name.trim()
                            .toUpperCase();

            colors.addProperty(
                    key,
                    normalizedColor
            );

            if ("BHOP_PROFILE_NAME".equals(key)) {

                profile.color =
                        normalizedColor;
            }
        }

        save();
    }


    /*
     * =========================================================
     * SELECTED MODULE
     * =========================================================
     *
     * Bestaande generic API blijft bestaan.
     */

    public static JsonObject getSelectedModule(
            String moduleName) {

        load();

        BhopProfile profile =
                getSelectedProfile();

        if (profile == null) {

            return new JsonObject();
        }

        return profile.getModule(
                moduleName
        );
    }


    public static void setSelectedModule(
            String moduleName,
            JsonObject settings) {

        load();

        BhopProfile profile =
                getSelectedProfile();

        if (profile == null) {
            return;
        }

        profile.setModule(
                moduleName,
                settings
        );

        save();
    }


    /*
     * =========================================================
     * MIGRATE PROFILE COLORS
     * =========================================================
     *
     * Bestaande modcolors.json waarden worden
     * alleen toegevoegd wanneer een profiel
     * de betreffende kleur nog niet heeft.
     */

    private static boolean migrateProfileColors() {

        Map<String, String> legacyColors =
                readLegacyModColors();

        boolean changed =
                false;

        for (BhopProfile profile :
                profiles) {

            changed =
                    ensureProfileModColors(
                            profile,
                            legacyColors
                    )
                            || changed;
        }

        return changed;
    }


    /*
     * =========================================================
     * ENSURE PROFILE COLORS
     * =========================================================
     */

    private static boolean ensureProfileModColors(
            BhopProfile profile,
            Map<String, String> legacyColors) {

        if (profile == null) {
            return false;
        }

        boolean changed =
                false;

        JsonObject colors =
                getOrCreateModColors(
                        profile
                );

        /*
         * =========================
         * CPS
         * =========================
         */

        changed =
                ensureColor(
                        colors,
                        "CPS",
                        getLegacyColor(
                                legacyColors,
                                "CPS",
                                DEFAULT_COLOR
                        )
                )
                        || changed;


        /*
         * =========================
         * BHOP PROFILE NAME
         * =========================
         */

        String legacyBhopColor =
                getLegacyColor(
                        legacyColors,
                        "BHOP",
                        DEFAULT_COLOR
                );

        changed =
                ensureColor(
                        colors,
                        "BHOP_PROFILE_NAME",
                        getLegacyColor(
                                legacyColors,
                                "BHOP_PROFILE_NAME",
                                legacyBhopColor
                        )
                )
                        || changed;


        /*
         * =========================
         * BHOP SPEED
         * =========================
         */

        changed =
                ensureColor(
                        colors,
                        "BHOP_SPEED",
                        getLegacyColor(
                                legacyColors,
                                "BHOP_SPEED",
                                legacyBhopColor
                        )
                )
                        || changed;


        /*
         * =========================
         * KNOCKBACK
         * =========================
         */

        changed =
                ensureColor(
                        colors,
                        "KNOCKBACK",
                        getLegacyColor(
                                legacyColors,
                                "KNOCKBACK",
                                DEFAULT_COLOR
                        )
                )
                        || changed;


        /*
         * =========================
         * FUTURE MOD COLORS
         * =========================
         *
         * Eventuele bestaande toekomstige
         * waarden uit modcolors.json worden
         * eveneens gemigreerd.
         */

        if (legacyColors != null) {

            for (Map.Entry<String, String> entry :
                    legacyColors.entrySet()) {

                String name =
                        entry.getKey();

                if (name == null
                        || name.trim().isEmpty()) {

                    continue;
                }

                if ("BHOP".equalsIgnoreCase(name)) {
                    continue;
                }

                String value =
                        normalizeColor(
                                entry.getValue()
                        );

                if (value == null) {
                    continue;
                }

                changed =
                        ensureColor(
                                colors,
                                name,
                                value
                        )
                                || changed;
            }
        }

        return changed;
    }


    /*
     * =========================================================
     * GET OR CREATE MOD COLORS
     * =========================================================
     */

    private static JsonObject getOrCreateModColors(
            BhopProfile profile) {

        if (profile == null) {

            return new JsonObject();
        }

        JsonElement element =
                profile.getModules()
                        .get(
                                MOD_COLORS_MODULE
                        );

        if (element != null
                && element.isJsonObject()) {

            return element.getAsJsonObject();
        }

        JsonObject colors =
                new JsonObject();

        profile.getModules().add(
                MOD_COLORS_MODULE,
                colors
        );

        return colors;
    }


    /*
     * =========================================================
     * ENSURE COLOR
     * =========================================================
     */

    private static boolean ensureColor(
            JsonObject colors,
            String name,
            String value) {

        if (colors == null
                || name == null
                || name.trim().isEmpty()) {

            return false;
        }

        String key =
                name.trim()
                        .toUpperCase();

        if (colors.has(key)) {

            JsonElement existing =
                    colors.get(key);

            if (existing != null
                    && existing.isJsonPrimitive()) {

                String existingValue;

                try {

                    existingValue =
                            normalizeColor(
                                    existing.getAsString()
                            );

                } catch (Exception e) {

                    existingValue =
                            null;
                }

                if (existingValue != null) {

                    return false;
                }
            }
        }

        String normalized =
                normalizeColor(
                        value
                );

        if (normalized == null) {

            normalized =
                    DEFAULT_COLOR;
        }

        colors.addProperty(
                key,
                normalized
        );

        return true;
    }


    /*
     * =========================================================
     * LEGACY COLOR
     * =========================================================
     */

    private static String getLegacyColor(
            Map<String, String> colors,
            String key,
            String fallback) {

        if (colors == null) {
            return fallback;
        }

        String value =
                colors.get(
                        key.toUpperCase()
                );

        if (value == null) {
            return fallback;
        }

        String normalized =
                normalizeColor(
                        value
                );

        if (normalized == null) {

            return fallback;
        }

        return normalized;
    }


    /*
     * =========================================================
     * READ LEGACY COLORS
     * =========================================================
     */

    private static Map<String, String> readLegacyModColors() {

        Map<String, String> result =
                new LinkedHashMap<String, String>();

        try {

            File file =
                    getLegacyColorFile();

            if (!file.exists()) {

                return result;
            }

            FileReader reader =
                    new FileReader(file);

            JsonElement root =
                    new JsonParser().parse(
                            reader
                    );

            reader.close();

            if (!root.isJsonObject()) {

                return result;
            }

            JsonObject object =
                    root.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry :
                    object.entrySet()) {

                if (entry.getValue() == null
                        || !entry.getValue().isJsonPrimitive()) {

                    continue;
                }

                String name =
                        entry.getKey();

                if (name == null
                        || name.trim().isEmpty()) {

                    continue;
                }

                String value;

                try {

                    value =
                            entry.getValue()
                                    .getAsString();

                } catch (Exception e) {

                    continue;
                }

                value =
                        normalizeColor(
                                value
                        );

                if (value == null) {
                    continue;
                }

                result.put(
                        name.trim().toUpperCase(),
                        value
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "[AlphaStaffMod] Could not migrate modcolors.json."
            );

            e.printStackTrace();
        }

        return result;
    }


    /*
     * =========================================================
     * CLONE JSON OBJECT
     * =========================================================
     */

    private static JsonObject cloneJsonObject(
            JsonObject source) {

        if (source == null) {

            return new JsonObject();
        }

        try {

            return new JsonParser()
                    .parse(
                            source.toString()
                    )
                    .getAsJsonObject();

        } catch (Exception e) {

            return new JsonObject();
        }
    }


    /*
     * =========================================================
     * NORMALIZE COLOR
     * =========================================================
     */

    private static String normalizeColor(
            String value) {

        if (value == null) {

            return null;
        }

        String color =
                value.trim()
                        .toUpperCase();

        if (color.isEmpty()) {

            return null;
        }

        if (!color.startsWith("#")) {

            color =
                    "#" + color;
        }

        if (color.length() != 7) {

            return null;
        }

        for (int i = 1;
             i < color.length();
             i++) {

            char c =
                    color.charAt(i);

            boolean valid =
                    (c >= '0'
                            && c <= '9')
                            || (c >= 'A'
                            && c <= 'F');

            if (!valid) {

                return null;
            }
        }

        return color;
    }


    /*
     * =========================================================
     * JSON HELPERS
     * =========================================================
     */

    private static String getString(
            JsonObject object,
            String key,
            String fallback) {

        try {

            if (object.has(key)) {

                return object.get(key)
                        .getAsString();
            }

        } catch (Exception ignored) {
        }

        return fallback;
    }


    private static boolean getBoolean(
            JsonObject object,
            String key,
            boolean fallback) {

        try {

            if (object.has(key)) {

                return object.get(key)
                        .getAsBoolean();
            }

        } catch (Exception ignored) {
        }

        return fallback;
    }


    private static int getInt(
            JsonObject object,
            String key,
            int fallback) {

        try {

            if (object.has(key)) {

                return object.get(key)
                        .getAsInt();
            }

        } catch (Exception ignored) {
        }

        return fallback;
    }
}
