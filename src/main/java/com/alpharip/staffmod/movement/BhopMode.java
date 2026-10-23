package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.alpharip.staffmod.permissions.PermissionService;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.input.Keyboard;

public class BhopMode {

    private static boolean enabled = false;
    private static boolean shiftUpEnabled = false;
    private static int speedLevel = 2;
    private static String bhopName = "BHOP";
    private static String color = "#55FF55";

    private static final double SHIFT_UP_JUMP = 0.70D;

    /*
     * W + SHIFT maakt de huidige BHOP-snelheid
     * 25% sneller.
     *
     * Bijvoorbeeld:
     *
     * SPEED I   = 0.42 -> 0.525
     * SPEED II  = 0.52 -> 0.650
     * SPEED X   = 1.32 -> 1.650
     * SPEED XX  = 2.32 -> 2.900
     */
    private static final double SHIFT_SPEED_MULTIPLIER = 1.25D;

    private static final double[] SHIFT_FORWARD_JUMPS = {
            0.55D, 0.64D, 0.73D, 0.82D, 0.91D,
            1.00D, 1.09D, 1.18D, 1.27D, 1.36D,
            1.45D, 1.54D, 1.63D, 1.72D, 1.81D,
            1.90D, 1.99D, 2.08D, 2.17D, 2.26D
    };

    private static final double[] SPEEDS = {
            0.42D, 0.52D, 0.62D, 0.72D, 0.82D,
            0.92D, 1.02D, 1.12D, 1.22D, 1.32D,
            1.42D, 1.52D, 1.62D, 1.72D, 1.82D,
            1.92D, 2.02D, 2.12D, 2.22D, 2.32D
    };

    private static final double SPRINT_BOOST = 0.20D;

    public BhopMode() {
        System.out.println("[AlphaStaffMod] BhopMode created!");
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("[AlphaStaffMod] BhopMode registered!");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc == null || mc.thePlayer == null) return;

        EntityPlayer player = mc.thePlayer;

        // Require verified permission at runtime. Saved preferences remain intact while locked.
        if (!PermissionService.hasModAccess("staff.bhop")
                && !PermissionService.hasCommandPermission("command.bhop", false)) return;
        if (!enabled) return;

        if (!player.capabilities.isCreativeMode) return;

        if (mc.currentScreen != null) return;

        boolean leftShift =
                Keyboard.isKeyDown(
                        Keyboard.KEY_LSHIFT
                );

        boolean rightShift =
                Keyboard.isKeyDown(
                        Keyboard.KEY_RSHIFT
                );

        boolean shift =
                shiftUpEnabled
                        && (leftShift || rightShift);


        /*
         * =========================
         * SHIFT UP ONLY
         * =========================
         *
         * SHIFT zonder W:
         *
         * Alleen omhoog.
         *
         * A/D worden hier bewust
         * volledig genegeerd.
         */
        if (shift
                && player.moveForward <= 0.0F) {

            if (player.onGround) {

                player.motionY =
                        SHIFT_UP_JUMP;
            }

            return;
        }


        /*
         * =========================
         * W IS VERPLICHT
         * =========================
         *
         * Zonder W doet BHOP niets.
         *
         * Hierdoor:
         *
         * A       = niets
         * D       = niets
         * A + D   = niets
         * S       = niets
         * S + A   = niets
         * S + D   = niets
         *
         * Alleen W activeert BHOP.
         */
        if (player.moveForward <= 0.0F) {
            return;
        }


        /*
         * =========================
         * W + SHIFT
         * =========================
         *
         * Shift-BHOP.
         */
        if (shift) {

            if (player.onGround) {

                player.motionY =
                        getShiftForwardJump();
            }


            /*
             * Normale BHOP-snelheid x 1.25.
             */
            applySpeed(
                    player,
                    true
            );


            /*
             * Extra Shift-forward boost.
             */
            applyForwardBoost(
                    player,
                    getShiftBoost()
            );

            return;
        }


        /*
         * =========================
         * NORMAL BHOP
         * =========================
         */

        if (player.onGround) {

            player.jump();
        }


        /*
         * Normale BHOP gebruikt alle
         * 20 speed levels.
         *
         * A/D worden genegeerd.
         */
        applySpeed(
                player,
                false
        );


        /*
         * Sprint boost alleen wanneer
         * W wordt gebruikt.
         */
        if (player.isSprinting()) {

            applyForwardBoost(
                    player,
                    SPRINT_BOOST
            );
        }
    }


    private static double getNormalSpeed() {
        return SPEEDS[speedLevel - 1];
    }


    private static double getShiftSpeed() {
        return getNormalSpeed()
                * SHIFT_SPEED_MULTIPLIER;
    }


    private static double getShiftBoost() {

        /*
         * De oude Shift boost-schaal
         * behouden.
         */
        double[] boosts = {
                0.45D, 0.38D, 0.31D, 0.24D, 0.18D,
                0.16D, 0.15D, 0.14D, 0.13D, 0.12D,
                0.11D, 0.10D, 0.09D, 0.08D, 0.07D,
                0.06D, 0.05D, 0.04D, 0.03D, 0.02D
        };

        return boosts[speedLevel - 1];
    }


    private static double getShiftForwardJump() {
        return SHIFT_FORWARD_JUMPS[speedLevel - 1];
    }


    private static void applySpeed(
            EntityPlayer player,
            boolean shift) {

        double speed =
                shift
                        ? getShiftSpeed()
                        : getNormalSpeed();


        /*
         * =========================
         * ALLEEN W
         * =========================
         *
         * moveStrafing wordt hier
         * volledig genegeerd.
         *
         * Daardoor kan A/D de
         * BHOP-richting nooit veranderen.
         */
        if (player.moveForward <= 0.0F) {
            return;
        }


        /*
         * Alleen de huidige
         * kijkrichting gebruiken.
         */
        float yaw =
                player.rotationYaw;


        float yawRadians =
                yaw * 0.017453292F;


        double sin =
                MathHelper.sin(
                        yawRadians
                );

        double cos =
                MathHelper.cos(
                        yawRadians
                );


        /*
         * =========================
         * HORIZONTALE MOTION
         * =========================
         *
         * Altijd recht vooruit in
         * de richting van W.
         *
         * A/D hebben hier geen effect.
         */
        player.motionX =
                -sin * speed;

        player.motionZ =
                cos * speed;
    }


    private static void applyForwardBoost(
            EntityPlayer player,
            double boost) {

        float yaw =
                player.rotationYaw;

        float radians =
                yaw * 0.017453292F;

        double sin =
                MathHelper.sin(
                        radians
                );

        double cos =
                MathHelper.cos(
                        radians
                );

        player.motionX +=
                -sin * boost;

        player.motionZ +=
                cos * boost;
    }


    public static void toggle() {
        enabled = !enabled;
    }


    public static boolean isEnabled() {
        return enabled;
    }


    public static void setEnabled(
            boolean value) {

        // Preserve the user preference; onClientTick enforces live access before running BHOP.
        enabled = value;
    }


    public static boolean isShiftUpEnabled() {
        return shiftUpEnabled;
    }


    public static void setShiftUpEnabled(
            boolean value) {

        shiftUpEnabled = value;
    }


    public static void toggleShiftUp() {
        shiftUpEnabled = !shiftUpEnabled;
    }


    public static int getSpeedLevel() {
        return speedLevel;
    }


    public static void setSpeedLevel(
            int value) {

        if (value < 1) {
            value = 1;
        }

        if (value > 20) {
            value = 20;
        }

        speedLevel = value;
    }


    public static void nextSpeedLevel() {

        speedLevel++;

        if (speedLevel > 20) {
            speedLevel = 1;
        }
    }


    public static void previousSpeedLevel() {

        speedLevel--;

        if (speedLevel < 1) {
            speedLevel = 20;
        }
    }


    public static String getSpeedLevelName() {
        return "SPEED "
                + toRoman(speedLevel);
    }


    private static String toRoman(
            int number) {

        String[] values = {
                "I", "II", "III", "IV", "V",
                "VI", "VII", "VIII", "IX", "X",
                "XI", "XII", "XIII", "XIV", "XV",
                "XVI", "XVII", "XVIII", "XIX", "XX"
        };

        if (number < 1) {
            return values[0];
        }

        if (number > 20) {
            return values[19];
        }

        return values[number - 1];
    }


    public static String getBhopName() {
        return bhopName;
    }


    public static void setBhopName(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            bhopName = "BHOP";

            return;
        }

        bhopName = value;
    }


    public static String getColor() {
        return color;
    }


    public static void setColor(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            color = "#55FF55";

            return;
        }

        color = value;
    }


    public static void loadSelectedProfile() {
        ModuleProfileManager.BhopProfile profile =
                ModuleProfileManager.getSelectedProfile();

        if (profile == null) {
            return;
        }

        // The active profile menu uses ModuleProfileManager, so BHOP must load
        // from that same selected profile instead of the legacy ProfileManager.
        setBhopName(profile.getBhopName());
        setColor(profile.getColor());
        setEnabled(profile.isBhopEnabled());
        setShiftUpEnabled(profile.isShiftUpEnabled());
        setSpeedLevel(profile.getSpeedLevel());
    }
}