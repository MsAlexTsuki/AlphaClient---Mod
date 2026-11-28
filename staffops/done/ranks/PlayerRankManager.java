package com.alpharip.staffmod.ranks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerRankManager {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final String ALPHA_RIP_DOMAIN =
            "alpha.rip";

    private static final Map<String, PlayerRank> ranks =
            new HashMap<String, PlayerRank>();

    /*
     * Welke lokale teamnaam bij welke
     * rank + kleur hoort.
     */
    private static final Map<String, String> teamNameByRankKey =
            new HashMap<String, String>();

    /*
     * Voorkomt dat dezelfde debugmelding
     * iedere client tick opnieuw verschijnt.
     */
    private static final Set<String> debugAppliedPlayers =
            new HashSet<String>();

    private static int nextTeamIndex = 0;

    private static boolean enabled = true;

    private static boolean loaded = false;


    /*
     * =========================================================
     * RANK DATA
     * =========================================================
     */

    public static class PlayerRank {

        private String username;
        private String prefix;
        private String color;

        public PlayerRank() {
        }

        public PlayerRank(
                String username,
                String prefix,
                String color) {

            this.username = username;
            this.prefix = prefix;
            this.color = color;
        }

        public String getUsername() {

            return username;
        }

        public String getPrefix() {

            return prefix;
        }

        public String getColor() {

            return color;
        }
    }


    /*
     * =========================================================
     * ENABLE / DISABLE
     * =========================================================
     */

    public static boolean isEnabled() {

        return enabled;
    }

    public static void toggle() {

        enabled =
                !enabled;

        System.out.println(
                "[AlphaStaffMod] Name Tags: "
                        + (
                        enabled
                                ? "ENABLED"
                                : "DISABLED"
                )
        );

        if (!enabled) {

            removeAllRankTeams();
        }
    }


    /*
     * =========================================================
     * SERVER CHECK
     * =========================================================
     */

    public static boolean isOnAlphaRipServer() {

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null) {
            return false;
        }

        if (mc.getCurrentServerData() == null) {
            return false;
        }

        String serverIp =
                mc.getCurrentServerData().serverIP;

        if (serverIp == null) {
            return false;
        }

        serverIp =
                serverIp
                        .toLowerCase()
                        .trim();

        /*
         * Remove port.
         *
         * alpha.rip:25565
         * ->
         * alpha.rip
         */

        int colon =
                serverIp.indexOf(':');

        if (colon >= 0) {

            serverIp =
                    serverIp.substring(
                            0,
                            colon
                    );
        }

        boolean result =
                serverIp.equals(
                        ALPHA_RIP_DOMAIN
                )
                        || serverIp.endsWith(
                        "."
                                + ALPHA_RIP_DOMAIN
                );

        return result;
    }


    /*
     * =========================================================
     * FILE
     * =========================================================
     */

    private static File getDirectory() {

        Minecraft mc =
                Minecraft.getMinecraft();

        File configDirectory =
                new File(
                        mc.mcDataDir,
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
                "ranks.json"
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

            System.out.println(
                    "[AlphaStaffMod] ranks.json does not exist."
            );

            createExampleFile();

            return;
        }

        readFile(file);
    }


    /*
     * =========================================================
     * RELOAD
     * =========================================================
     */

    public static void reload() {

        System.out.println(
                "[AlphaStaffMod] Reloading ranks.json..."
        );

        loaded = true;

        ranks.clear();

        /*
         * Reset debug state.
         */
        debugAppliedPlayers.clear();

        File file =
                getFile();

        if (!file.exists()) {

            System.out.println(
                    "[AlphaStaffMod] ranks.json not found."
            );

            createExampleFile();

            return;
        }

        readFile(file);
    }


    /*
     * =========================================================
     * READ FILE
     * =========================================================
     */

    private static void readFile(
            File file) {

        try {

            FileReader reader =
                    new FileReader(file);

            JsonElement root =
                    new JsonParser()
                            .parse(reader);

            reader.close();

            ranks.clear();

            if (!root.isJsonArray()) {

                System.out.println(
                        "[AlphaStaffMod] ranks.json is not a JSON array."
                );

                return;
            }

            JsonArray array =
                    root.getAsJsonArray();

            for (JsonElement element :
                    array) {

                if (!element.isJsonObject()) {

                    continue;
                }

                JsonObject object =
                        element
                                .getAsJsonObject();

                if (!object.has("username")
                        || !object.has("prefix")) {

                    continue;
                }

                String username =
                        object
                                .get("username")
                                .getAsString();

                String prefix =
                        object
                                .get("prefix")
                                .getAsString();

                String color =
                        object.has("color")
                                ? object
                                .get("color")
                                .getAsString()
                                : "GOLD";

                if (username == null
                        || username
                        .trim()
                        .isEmpty()) {

                    continue;
                }

                ranks.put(
                        username.toLowerCase(),
                        new PlayerRank(
                                username,
                                prefix,
                                color
                        )
                );
            }

            System.out.println(
                    "[AlphaStaffMod] Loaded "
                            + ranks.size()
                            + " rank(s) from ranks.json."
            );

        } catch (Exception e) {

            System.out.println(
                    "[AlphaStaffMod] Could not load ranks.json."
            );

            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * CREATE EXAMPLE
     * =========================================================
     */

    private static void createExampleFile() {

        try {

            File directory =
                    getDirectory();

            if (!directory.exists()) {

                directory.mkdirs();
            }

            JsonArray array =
                    new JsonArray();

            JsonObject example =
                    new JsonObject();

            example.addProperty(
                    "username",
                    "MsAlexTsuki"
            );

            example.addProperty(
                    "prefix",
                    "[Sr-Mod] "
            );

            example.addProperty(
                    "color",
                    "DARK_BLUE"
            );

            array.add(example);

            FileWriter writer =
                    new FileWriter(
                            getFile()
                    );

            GSON.toJson(
                    array,
                    writer
            );

            writer.flush();

            writer.close();

            System.out.println(
                    "[AlphaStaffMod] Created example ranks.json."
            );

        } catch (Exception e) {

            System.out.println(
                    "[AlphaStaffMod] Could not create ranks.json."
            );

            e.printStackTrace();
        }
    }


    /*
     * =========================================================
     * LOOKUP
     * =========================================================
     */

    public static PlayerRank getRank(
            String username) {

        load();

        if (username == null) {

            return null;
        }

        return ranks.get(
                username.toLowerCase()
        );
    }


    /*
     * =========================================================
     * LOCAL STAFF
     * =========================================================
     */

    public static boolean isLocalPlayerStaff(
            Minecraft mc) {

        if (mc == null
                || mc.thePlayer == null) {

            return false;
        }

        PlayerRank rank =
                getRank(
                        mc.thePlayer.getName()
                );

        return rank != null;
    }


    /*
     * =========================================================
     * CLIENT TICK
     * =========================================================
     */

    @SubscribeEvent
    public void onClientTick(
            TickEvent.ClientTickEvent event) {

        if (event.phase
                != TickEvent.Phase.END) {

            return;
        }

        if (!enabled) {

            return;
        }

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null) {

            return;
        }

        /*
         * -----------------------------------------------------
         * SERVER DEBUG
         * -----------------------------------------------------
         */

        if (!isOnAlphaRipServer()) {

            return;
        }

        /*
         * -----------------------------------------------------
         * LOAD
         * -----------------------------------------------------
         */

        load();

        if (ranks.isEmpty()) {

            return;
        }

        if (mc.theWorld == null
                || mc.thePlayer == null) {

            return;
        }

        /*
         * -----------------------------------------------------
         * LOCAL PLAYER DEBUG
         * -----------------------------------------------------
         */

        String localUsername =
                mc.thePlayer.getName();

        PlayerRank localRank =
                getRank(
                        localUsername
                );

        if (!debugAppliedPlayers.contains(
                "__LOCAL_DEBUG__"
        )) {

            System.out.println(
                    "[AlphaStaffMod] "
                            + "Alpha.RIP detected."
            );

            System.out.println(
                    "[AlphaStaffMod] "
                            + "Local player: "
                            + localUsername
            );

            if (localRank != null) {

                System.out.println(
                        "[AlphaStaffMod] "
                                + "Local rank found: "
                                + localRank.getPrefix()
                                + " / "
                                + localRank.getColor()
                );

            } else {

                System.out.println(
                        "[AlphaStaffMod] "
                                + "Local player is NOT listed in ranks.json."
                );
            }

            debugAppliedPlayers.add(
                    "__LOCAL_DEBUG__"
            );
        }

        /*
         * -----------------------------------------------------
         * ONLY STAFF CLIENTS
         * -----------------------------------------------------
         *
         * Alleen een client waarvan de eigen
         * username in ranks.json staat krijgt
         * de lokale rank-teams toegepast.
         */

        if (localRank == null) {

            return;
        }

        /*
         * -----------------------------------------------------
         * SCOREBOARD
         * -----------------------------------------------------
         */

        Scoreboard scoreboard =
                mc.theWorld.getScoreboard();

        if (scoreboard == null) {

            return;
        }

        /*
         * -----------------------------------------------------
         * APPLY RANKS
         * -----------------------------------------------------
         */

        for (EntityPlayer player :
                mc.theWorld.playerEntities) {

            if (player == null) {

                continue;
            }

            PlayerRank rank =
                    getRank(
                            player.getName()
                    );

            if (rank == null) {

                continue;
            }

            applyRank(
                    scoreboard,
                    player.getName(),
                    rank
            );
        }
    }


    /*
     * =========================================================
     * APPLY RANK
     * =========================================================
     */

    private static void applyRank(
            Scoreboard scoreboard,
            String username,
            PlayerRank rank) {

        if (scoreboard == null
                || username == null
                || rank == null) {

            return;
        }

        String teamName =
                getTeamName(
                        rank
                );

        ScorePlayerTeam team =
                scoreboard.getTeam(
                        teamName
                );

        /*
         * -----------------------------------------------------
         * CREATE TEAM
         * -----------------------------------------------------
         */

        if (team == null) {

            try {

                team =
                        scoreboard.createTeam(
                                teamName
                        );

                team.setNamePrefix(
                        colorCode(
                                rank.getColor()
                        )
                                + rank.getPrefix()
                );

                System.out.println(
                        "[AlphaStaffMod] "
                                + "Created rank team: "
                                + teamName
                                + " -> "
                                + rank.getPrefix()
                                + rank.getColor()
                );

            } catch (Exception e) {

                System.out.println(
                        "[AlphaStaffMod] "
                                + "Could not create rank team: "
                                + teamName
                );

                e.printStackTrace();

                return;
            }
        }

        /*
         * -----------------------------------------------------
         * CURRENT TEAM
         * -----------------------------------------------------
         */

        ScorePlayerTeam currentTeam =
                scoreboard.getPlayersTeam(
                        username
                );

        /*
         * -----------------------------------------------------
         * APPLY
         * -----------------------------------------------------
         */

        if (currentTeam != team) {

            /*
             * Remove from an existing local
             * rank team if necessary.
             */

            if (currentTeam != null
                    && isOurRankTeam(
                    currentTeam
            )) {

                try {

                    scoreboard
                            .removePlayerFromTeam(
                                    username,
                                    currentTeam
                            );

                } catch (Exception ignored) {
                }
            }

            try {

                scoreboard.addPlayerToTeam(
                        username,
                        teamName
                );

                if (!debugAppliedPlayers.contains(
                        username.toLowerCase()
                )) {

                    System.out.println(
                            "[AlphaStaffMod] "
                                    + "Applied rank to "
                                    + username
                                    + ": "
                                    + rank.getPrefix()
                                    + " / "
                                    + rank.getColor()
                                    + " / team="
                                    + teamName
                    );

                    debugAppliedPlayers.add(
                            username.toLowerCase()
                    );
                }

            } catch (Exception e) {

                System.out.println(
                        "[AlphaStaffMod] "
                                + "Could not apply rank to "
                                + username
                );

                e.printStackTrace();
            }
        }
    }


    /*
     * =========================================================
     * CHECK OUR TEAM
     * =========================================================
     */

    private static boolean isOurRankTeam(
            ScorePlayerTeam team) {

        if (team == null) {

            return false;
        }

        String teamName =
                team.getRegisteredName();

        if (teamName == null) {

            return false;
        }

        return teamName.startsWith(
                "alpharank"
        );
    }


    /*
     * =========================================================
     * TEAM NAME
     * =========================================================
     */

    private static String getTeamName(
            PlayerRank rank) {

        String key =
                rank.getColor()
                        + "|"
                        + rank.getPrefix();

        String teamName =
                teamNameByRankKey.get(
                        key
                );

        if (teamName == null) {

            teamName =
                    "alpharank"
                            + nextTeamIndex;

            nextTeamIndex++;

            teamNameByRankKey.put(
                    key,
                    teamName
            );
        }

        return teamName;
    }


    /*
     * =========================================================
     * REMOVE ALL OUR TEAMS
     * =========================================================
     */

    private static void removeAllRankTeams() {

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.theWorld == null) {

            return;
        }

        Scoreboard scoreboard =
                mc.theWorld.getScoreboard();

        if (scoreboard == null) {

            return;
        }

        /*
         * Kopie maken omdat de scoreboard
         * tijdens de iteratie aangepast wordt.
         */

        Set<String> teamNames =
                new HashSet<String>(
                        teamNameByRankKey.values()
                );

        for (String teamName :
                teamNames) {

            ScorePlayerTeam team =
                    scoreboard.getTeam(
                            teamName
                    );

            if (team != null) {

                try {

                    scoreboard.removeTeam(
                            team
                    );

                } catch (Exception ignored) {
                }
            }
        }

        teamNameByRankKey.clear();

        nextTeamIndex = 0;

        debugAppliedPlayers.clear();

        System.out.println(
                "[AlphaStaffMod] Removed all rank teams."
        );
    }


    /*
     * =========================================================
     * COLOR CODE
     * =========================================================
     */

    private static String colorCode(
            String color) {

        if (color == null) {

            return EnumChatFormatting.GOLD
                    .toString();
        }

        try {

            return EnumChatFormatting.valueOf(
                    color.toUpperCase()
            ).toString();

        } catch (Exception e) {

            return EnumChatFormatting.GOLD
                    .toString();
        }
    }
}