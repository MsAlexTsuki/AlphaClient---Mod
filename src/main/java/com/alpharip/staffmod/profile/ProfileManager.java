package com.alpharip.staffmod.profile;

import com.alpharip.staffmod.movement.BhopMode;

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
import java.util.List;

public final class ProfileManager {

    /*
     * =========================================================
     * INSTANCE
     * =========================================================
     */

    private static ProfileManager instance;


    /*
     * =========================================================
     * FILE
     * =========================================================
     */

    private static final String FILE_NAME =
            "profiles.json";

    private static final String OLD_FILE_NAME =
            "bhop_profiles.json";


    /*
     * =========================================================
     * DEFAULTS
     * =========================================================
     */

    private static final String DEFAULT_PROFILE =
            "Default";

    private static final String DEFAULT_BHOP_NAME =
            "BHOP";

    private static final String DEFAULT_COLOR =
            "#F2EE4D";


    /*
     * =========================================================
     * MODULE NAMES
     * =========================================================
     *
     * LONGJUMP blijft de interne naam voor Dubble Jump.
     * Dit behouden we voor backwards compatibility.
     */

/*
     * =========================================================
     * GSON
     * =========================================================
     */

    private final Gson gson =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();


    /*
     * =========================================================
     * DATA
     * =========================================================
     */

    private final List<Profile> profiles =
            new ArrayList<Profile>();

    private String selectedProfile =
            DEFAULT_PROFILE;


    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    private ProfileManager() {
    }


    /*
     * =========================================================
     * INSTANCE
     * =========================================================
     */

    public static ProfileManager getInstance() {

        if (instance == null) {

            instance =
                    new ProfileManager();
        }

        return instance;
    }


    /*
     * =========================================================
     * DIRECTORY
     * =========================================================
     */

    private File getDirectory() {

        File configDirectory =
                new File(
                        System.getProperty("user.dir"),
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


    private File getProfileFile() {

        return new File(
                getDirectory(),
                FILE_NAME
        );
    }


    private File getOldProfileFile() {

        return new File(
                getDirectory(),
                OLD_FILE_NAME
        );
    }


    /*
     * =========================================================
     * LOAD
     * =========================================================
     */

    public void load() {

        profiles.clear();

        File file =
                getProfileFile();

        /*
         * New profile file exists.
         */
        if (file.exists()) {

            if (loadFile(file)) {

                ensureDefaultProfile();
                save();
                applySelectedProfile();

                return;
            }
        }


        /*
         * No new profile file.
         *
         * Try to migrate the existing BHOP profile file.
         */
        File oldFile =
                getOldProfileFile();

        if (oldFile.exists()) {

            if (migrateOldProfiles(oldFile)) {

                ensureDefaultProfile();
                save();
                applySelectedProfile();

                return;
            }
        }


        /*
         * Completely new installation.
         */
        createDefaultProfile();

        save();

        applySelectedProfile();
    }


    /*
     * =========================================================
     * LOAD FILE
     * =========================================================
     */

    private boolean loadFile(
            File file) {

        FileReader reader = null;

        try {

            reader =
                    new FileReader(file);

            JsonObject root =
                    new JsonParser()
                            .parse(reader)
                            .getAsJsonObject();

            if (root.has("selectedProfile")) {

                selectedProfile =
                        root.get(
                                "selectedProfile"
                        ).getAsString();
            }

            if (root.has("profiles")) {

                JsonArray array =
                        root.getAsJsonArray(
                                "profiles"
                        );

                for (JsonElement element : array) {

                    if (!element.isJsonObject()) {
                        continue;
                    }

                    Profile profile =
                            gson.fromJson(
                                    element,
                                    Profile.class
                            );

                    if (profile == null) {
                        continue;
                    }

                    if (profile.getProfileName() == null
                            || profile.getProfileName()
                            .trim()
                            .isEmpty()) {

                        continue;
                    }

                    /*
                     * Oude profielen krijgen de nieuwe
                     * modulevelden zonder bestaande
                     * waarden te overschrijven.
                     */
                    ensureModuleDefaults(profile);

                    profiles.add(profile);
                }
            }

            return !profiles.isEmpty();

        } catch (Exception exception) {

            System.out.println(
                    "[AlphaStaffMod] Could not load profiles.json: "
                            + exception.getMessage()
            );

            return false;

        } finally {

            if (reader != null) {

                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
        }
    }


    /*
     * =========================================================
     * OLD BHOP PROFILE MIGRATION
     * =========================================================
     */

    private boolean migrateOldProfiles(
            File file) {

        FileReader reader = null;

        try {

            reader =
                    new FileReader(file);

            JsonObject root =
                    new JsonParser()
                            .parse(reader)
                            .getAsJsonObject();


            /*
             * Existing selected profile.
             */
            if (root.has("selectedProfile")) {

                selectedProfile =
                        root.get(
                                "selectedProfile"
                        ).getAsString();
            }


            /*
             * Existing profiles.
             */
            if (root.has("profiles")) {

                JsonArray array =
                        root.getAsJsonArray(
                                "profiles"
                        );

                for (JsonElement element : array) {

                    if (!element.isJsonObject()) {
                        continue;
                    }

                    JsonObject old =
                            element.getAsJsonObject();

                    Profile profile =
                            new Profile();


                    /*
                     * Profile name.
                     */
                    if (old.has("profileName")) {

                        profile.setProfileName(
                                old.get(
                                        "profileName"
                                ).getAsString()
                        );
                    }


                    /*
                     * BHOP name.
                     */
                    if (old.has("bhopName")) {

                        profile.setBhopName(
                                old.get(
                                        "bhopName"
                                ).getAsString()
                        );
                    }


                    /*
                     * BHOP enabled.
                     */
                    if (old.has("bhopEnabled")) {

                        profile.setBhopEnabled(
                                old.get(
                                        "bhopEnabled"
                                ).getAsBoolean()
                        );
                    }


                    /*
                     * Shift-Up.
                     */
                    if (old.has("shiftUpEnabled")) {

                        profile.setShiftUpEnabled(
                                old.get(
                                        "shiftUpEnabled"
                                ).getAsBoolean()
                        );
                    }


                    /*
                     * Speed.
                     */
                    if (old.has("speedLevel")) {

                        profile.setSpeedLevel(
                                old.get(
                                        "speedLevel"
                                ).getAsInt()
                        );
                    }


                    /*
                     * Old generic modules.
                     */
                    if (old.has("modules")
                            && old.get("modules")
                            .isJsonObject()) {

                        profile.setModules(
                                old.getAsJsonObject(
                                        "modules"
                                )
                        );
                    }


                    /*
                     * Old colors.
                     */
                    if (old.has("modColors")
                            && old.get("modColors")
                            .isJsonObject()) {

                        JsonObject oldColors =
                                old.getAsJsonObject(
                                        "modColors"
                                );

                        copyColor(
                                oldColors,
                                profile,
                                "BHOP_PROFILE_NAME"
                        );

                        copyColor(
                                oldColors,
                                profile,
                                "BHOP_SPEED"
                        );

                        copyColor(
                                oldColors,
                                profile,
                                "CPS"
                        );

                        copyColor(
                                oldColors,
                                profile,
                                "KNOCKBACK"
                        );
                    }


                    /*
                     * The old profile itself contained
                     * the BHOP color as well.
                     */
                    if (old.has("color")) {

                        profile.setBhopColor(
                                old.get(
                                        "color"
                                ).getAsString()
                        );

                        profile.setColor(
                                "BHOP_PROFILE_NAME",
                                old.get(
                                        "color"
                                ).getAsString()
                        );
                    }


                    /*
                     * Make sure the profile has valid
                     * module defaults.
                     */
                    ensureModuleDefaults(profile);


                    if (profile.getProfileName() != null
                            && !profile.getProfileName()
                            .trim()
                            .isEmpty()) {

                        profiles.add(profile);
                    }
                }
            }

            return !profiles.isEmpty();

        } catch (Exception exception) {

            System.out.println(
                    "[AlphaStaffMod] Could not migrate old profiles: "
                            + exception.getMessage()
            );

            return false;

        } finally {

            if (reader != null) {

                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
        }
    }


    private void copyColor(
            JsonObject source,
            Profile profile,
            String key) {

        if (!source.has(key)) {
            return;
        }

        try {

            String value =
                    source.get(key)
                            .getAsString();

            profile.setColor(
                    key,
                    value
            );

        } catch (Exception ignored) {
        }
    }


    /*
     * =========================================================
     * MODULE DEFAULTS
     * =========================================================
     *
     * Bestaande profielen worden aangevuld met:
     *
     * LONGJUMP = false
     * NOFALL   = false
     *
     * Alleen als deze nog niet bestaan.
     */

    private void ensureModuleDefaults(Profile profile) {
        // Legacy module flags are ignored; profile serialization stays backward-compatible.
    }


    /*
     * =========================================================
     * SAVE
     * =========================================================
     */

    public void save() {

        FileWriter writer = null;

        try {

            JsonObject root =
                    new JsonObject();

            root.addProperty(
                    "selectedProfile",
                    selectedProfile
            );


            JsonArray array =
                    new JsonArray();

            for (Profile profile : profiles) {

                array.add(
                        gson.toJsonTree(profile)
                );
            }

            root.add(
                    "profiles",
                    array
            );


            writer =
                    new FileWriter(
                            getProfileFile()
                    );

            gson.toJson(
                    root,
                    writer
            );

        } catch (Exception exception) {

            System.out.println(
                    "[AlphaStaffMod] Could not save profiles.json: "
                            + exception.getMessage()
            );

        } finally {

            if (writer != null) {

                try {
                    writer.close();
                } catch (Exception ignored) {
                }
            }
        }
    }


    /*
     * =========================================================
     * DEFAULT PROFILE
     * =========================================================
     */

    private void createDefaultProfile() {

        profiles.clear();

        Profile profile =
                new Profile(
                        DEFAULT_PROFILE
                );

        profile.setBhopName(
                DEFAULT_BHOP_NAME
        );

        profile.setBhopColor(
                DEFAULT_COLOR
        );

        profile.setBhopEnabled(
                false
        );

        profile.setShiftUpEnabled(
                false
        );

        profile.setSpeedLevel(
                2
        );


profile.setColor(
                "BHOP_PROFILE_NAME",
                DEFAULT_COLOR
        );

        profile.setColor(
                "BHOP_SPEED",
                DEFAULT_COLOR
        );

        profile.setColor(
                "CPS",
                DEFAULT_COLOR
        );

        profile.setColor(
                "KNOCKBACK",
                DEFAULT_COLOR
        );

        profiles.add(profile);

        selectedProfile =
                DEFAULT_PROFILE;
    }


    private void ensureDefaultProfile() {

        if (profiles.isEmpty()) {

            createDefaultProfile();

            return;
        }


        if (getProfile(DEFAULT_PROFILE) == null) {

            if (selectedProfile == null
                    || getProfile(selectedProfile) == null) {

                selectedProfile =
                        profiles.get(0)
                                .getProfileName();
            }

        } else {

            if (selectedProfile == null
                    || getProfile(selectedProfile) == null) {

                selectedProfile =
                        DEFAULT_PROFILE;
            }
        }


        /*
         * Ensure every loaded profile has the
         * new module defaults.
         */
        for (Profile profile : profiles) {

            ensureModuleDefaults(profile);
        }
    }


    /*
     * =========================================================
     * PROFILE ACCESS
     * =========================================================
     */

    public List<Profile> getProfiles() {

        return new ArrayList<Profile>(
                profiles
        );
    }


    public Profile getSelectedProfile() {

        return getProfile(
                selectedProfile
        );
    }


    public String getSelectedProfileName() {

        return selectedProfile;
    }


    public Profile getProfile(
            String name) {

        if (name == null) {
            return null;
        }

        for (Profile profile : profiles) {

            if (profile != null
                    && name.equalsIgnoreCase(
                    profile.getProfileName()
            )) {

                return profile;
            }
        }

        return null;
    }


    /*
     * =========================================================
     * SELECT
     * =========================================================
     */

    public boolean selectProfile(
            String name) {

        Profile profile =
                getProfile(name);

        if (profile == null) {
            return false;
        }


        /*
         * Save the profile we are leaving first.
         *
         * Hierdoor blijven de actuele Dubble Jump en
         * NoFall instellingen van het oude profiel behouden.
         */
        saveCurrentProfile();


        selectedProfile =
                profile.getProfileName();


        /*
         * Apply gebruikt directe assignments.
         * Het nieuwe profiel overschrijft dus de oude
         * module-status volledig.
         */
        applySelectedProfile();

        save();

        return true;
    }


    /*
     * =========================================================
     * CREATE
     * =========================================================
     */

    public Profile createProfile(
            String name) {

        if (name == null
                || name.trim().isEmpty()) {

            return null;
        }

        name =
                name.trim();

        if (getProfile(name) != null) {
            return null;
        }

        Profile profile =
                new Profile(name);


        profile.setBhopName(
                DEFAULT_BHOP_NAME
        );

        profile.setBhopColor(
                DEFAULT_COLOR
        );

        profile.setBhopEnabled(
                false
        );

        profile.setShiftUpEnabled(
                false
        );

        profile.setSpeedLevel(
                2
        );


profile.setColor(
                "BHOP_PROFILE_NAME",
                DEFAULT_COLOR
        );

        profile.setColor(
                "BHOP_SPEED",
                DEFAULT_COLOR
        );

        profile.setColor(
                "CPS",
                DEFAULT_COLOR
        );

        profile.setColor(
                "KNOCKBACK",
                DEFAULT_COLOR
        );

        profiles.add(profile);

        save();

        return profile;
    }


    /*
     * =========================================================
     * DELETE
     * =========================================================
     */

    public boolean deleteProfile(
            String name) {

        if (profiles.size() <= 1) {
            return false;
        }

        Profile profile =
                getProfile(name);

        if (profile == null) {
            return false;
        }

        profiles.remove(profile);

        if (profile.getProfileName()
                .equalsIgnoreCase(
                        selectedProfile
                )) {

            selectedProfile =
                    profiles.get(0)
                            .getProfileName();
        }

        applySelectedProfile();

        save();

        return true;
    }


    /*
     * =========================================================
     * SAVE CURRENT SETTINGS
     * =========================================================
     */

    public void saveCurrentProfile() {

        Profile profile =
                getSelectedProfile();

        if (profile == null) {
            return;
        }


        /*
         * =====================================================
         * BHOP
         * =====================================================
         */

        profile.setBhopName(
                BhopMode.getBhopName()
        );

        profile.setBhopEnabled(
                BhopMode.isEnabled()
        );

        profile.setShiftUpEnabled(
                BhopMode.isShiftUpEnabled()
        );

        profile.setSpeedLevel(
                BhopMode.getSpeedLevel()
        );

        profile.setBhopColor(
                BhopMode.getColor()
        );


        /*
         * =====================================================
         * SAVE
         * =====================================================
         */

        save();
    }


    /*
     * =========================================================
     * APPLY SELECTED PROFILE
     * =========================================================
     *
     * Dit is het centrale punt waar de geselecteerde
     * profielinstellingen naar de daadwerkelijke modules
     * worden gestuurd.
     *
     * Er wordt NOOIT toggle() gebruikt.
     */

    public void applySelectedProfile() {

        Profile profile =
                getSelectedProfile();

        if (profile == null) {
            return;
        }


        /*
         * Zorg dat het profiel de benodigde modulevelden heeft.
         */
        ensureModuleDefaults(profile);


        /*
         * =====================================================
         * BHOP
         * =====================================================
         */

        BhopMode.setBhopName(
                profile.getBhopName()
        );

        BhopMode.setColor(
                profile.getBhopColor()
        );

        BhopMode.setEnabled(
                profile.isBhopEnabled()
        );

        BhopMode.setShiftUpEnabled(
                profile.isShiftUpEnabled()
        );

        BhopMode.setSpeedLevel(
                profile.getSpeedLevel()
        );


}


    /*
     * =========================================================
     * MODULE ACCESS
     * =========================================================
     */

    public boolean isModuleEnabled(
            String moduleName) {

        Profile profile =
                getSelectedProfile();

        if (profile == null) {
            return false;
        }

        return profile.getModule(
                moduleName
        );
    }


    public void setModuleEnabled(
            String moduleName,
            boolean enabled) {

        Profile profile =
                getSelectedProfile();

        if (profile == null) {
            return;
        }


        profile.setModule(
                moduleName,
                enabled
        );


        save();
    }


    /*
     * =========================================================
     * COLORS
     * =========================================================
     */

    public String getColor(
            String colorName) {

        Profile profile =
                getSelectedProfile();

        if (profile == null) {
            return DEFAULT_COLOR;
        }

        return profile.getColor(
                colorName
        );
    }


    public void setColor(
            String colorName,
            String color) {

        Profile profile =
                getSelectedProfile();

        if (profile == null) {
            return;
        }

        profile.setColor(
                colorName,
                color
        );

        save();
    }
}