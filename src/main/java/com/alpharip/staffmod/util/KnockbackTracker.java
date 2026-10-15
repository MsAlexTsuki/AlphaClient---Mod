package com.alpharip.staffmod.util;

import com.alpharip.staffmod.config.HudPositionConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import java.util.HashMap;
import java.util.Map;

public class KnockbackTracker {

    /*
     * =========================
     * SETTINGS
     * =========================
     */

    private static boolean enabled = true;

    private static int hudX = 5;
    private static int hudY = 45;

    /*
     * 0   = 50%
     * 50  = 100%
     * 100 = 150%
     */
    private static int hudScale = 50;


    /*
     * =========================
     * PACKET QUEUE
     * =========================
     *
     * S12 packets komen binnen via
     * de Netty thread.
     *
     * Daarom bewaren we ze eerst.
     *
     * De normale Minecraft tick pakt
     * daarna het packet op wanneer we
     * daadwerkelijk naar die speler kijken.
     */

    private static final Map<Integer, PendingVelocity>
            pendingVelocities =
            new HashMap<Integer, PendingVelocity>();


    /*
     * =========================
     * ACTIVE TRACKING
     * =========================
     */

    private static final Map<Integer, KnockbackData>
            tracking =
            new HashMap<Integer, KnockbackData>();


    /*
     * Packet mag maximaal 300 ms oud zijn
     * voordat we hem negeren.
     */
    private static final long PACKET_TIMEOUT_MS =
            300L;


    /*
     * =========================
     * HUD DATA
     * =========================
     */

    private static String targetName = "-";

    private static double expectedVelocity = 0.0D;

    private static double actualVelocity = 0.0D;

    private static double ratio = 0.0D;

    private static int violationLevel = 0;


    /*
     * =========================
     * TRACK SETTINGS
     * =========================
     */

    private static final int MAX_TRACK_TICKS = 8;

    private static final double SUSPICIOUS_RATIO = 0.40D;


    /*
     * =========================
     * ENABLE
     * =========================
     */

    public static void toggle() {

        enabled = !enabled;

        if (!enabled) {
            clear();
        }
    }

    public static boolean isEnabled() {

        return enabled;
    }

    public static void setEnabled(
            boolean value) {

        enabled = value;

        if (!enabled) {
            clear();
        }
    }


    /*
     * =========================
     * POSITION
     * =========================
     */

    public static int getHudX() {

        return hudX;
    }

    public static int getHudY() {

        return hudY;
    }

    public static void setHudPosition(
            int x,
            int y) {

        hudX = x;
        hudY = y;

        HudPositionConfig.setKnockbackPosition(
                x,
                y
        );
    }

    public static void resetHudPosition() {

        hudX = 5;
        hudY = 45;

        HudPositionConfig.resetKnockbackPosition();
    }

    public static void resetPosition() {

        resetHudPosition();
    }

    public static void loadHudPosition() {

        HudPositionConfig.load();

        hudX =
                HudPositionConfig.getKnockbackX();

        hudY =
                HudPositionConfig.getKnockbackY();

        hudScale =
                HudPositionConfig.getKnockbackScale();
    }


    /*
     * =========================
     * SCALE
     * =========================
     */

    public static int getHudScale() {

        return hudScale;
    }

    public static void setHudScale(
            int scale) {

        if (scale < 0) {
            scale = 0;
        }

        if (scale > 100) {
            scale = 100;
        }

        hudScale = scale;

        HudPositionConfig.setKnockbackScale(
                scale
        );
    }

    public static float getVisualScale() {

        return 0.50F
                + (hudScale / 100.0F);
    }


    /*
     * =========================
     * RECEIVE S12
     * =========================
     *
     * Hier wordt het server packet
     * alleen opgeslagen.
     *
     * We controleren HIER dus NIET
     * welke speler jij aankijkt.
     */

    public static synchronized void receiveVelocity(
            int entityId,
            int motionX,
            int motionY,
            int motionZ) {

        if (!enabled) {
            return;
        }

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.theWorld == null
                || mc.thePlayer == null) {

            return;
        }

        /*
         * Zelf nooit tracken.
         */
        if (entityId
                == mc.thePlayer.getEntityId()) {

            return;
        }

        /*
         * Horizontale velocity.
         */
        double velocityX =
                motionX / 8000.0D;

        double velocityY =
                motionY / 8000.0D;

        double velocityZ =
                motionZ / 8000.0D;

        double expected =
                Math.sqrt(
                        velocityX * velocityX
                                + velocityZ * velocityZ
                );

        /*
         * Geen horizontale knockback.
         */
        if (expected <= 0.0D) {
            return;
        }

        PendingVelocity pending =
                new PendingVelocity();

        pending.entityId =
                entityId;

        pending.motionX =
                velocityX;

        pending.motionY =
                velocityY;

        pending.motionZ =
                velocityZ;

        pending.expected =
                expected;

        pending.timestamp =
                System.currentTimeMillis();

        /*
         * Packet tijdelijk bewaren.
         */
        pendingVelocities.put(
                entityId,
                pending
        );
    }


    /*
     * =========================
     * S12 OVERLOAD
     * =========================
     */

    public static void receiveVelocity(
            S12PacketEntityVelocity packet) {

        if (packet == null) {
            return;
        }

        receiveVelocity(
                packet.getEntityID(),
                packet.getMotionX(),
                packet.getMotionY(),
                packet.getMotionZ()
        );
    }


    /*
     * =========================
     * CLIENT TICK
     * =========================
     */

    public static synchronized void tick() {

        if (!enabled) {
            return;
        }

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.theWorld == null
                || mc.thePlayer == null) {

            return;
        }


        /*
         * =========================
         * CLEAN OLD PACKETS
         * =========================
         */

        long now =
                System.currentTimeMillis();

        Map<Integer, PendingVelocity> pendingCopy =
                new HashMap<Integer, PendingVelocity>(
                        pendingVelocities
                );

        for (
                Map.Entry<Integer, PendingVelocity> entry
                : pendingCopy.entrySet()
                ) {

            PendingVelocity pending =
                    entry.getValue();

            if (pending == null) {
                pendingVelocities.remove(
                        entry.getKey()
                );

                continue;
            }

            if (now - pending.timestamp
                    > PACKET_TIMEOUT_MS) {

                pendingVelocities.remove(
                        entry.getKey()
                );
            }
        }


        /*
         * =========================
         * FIND CURRENT TARGET
         * =========================
         */

        EntityPlayer target =
                getLookedAtPlayer(mc);


        /*
         * Als we naar een speler kijken,
         * controleren we of er net een
         * S12 packet voor die speler
         * binnenkwam.
         */

        if (target != null) {

            int targetId =
                    target.getEntityId();

            PendingVelocity pending =
                    pendingVelocities.get(
                            targetId
                    );

            if (pending != null) {

                startTracking(
                        target,
                        pending
                );

                pendingVelocities.remove(
                        targetId
                );
            }
        }


        /*
         * =========================
         * UPDATE ACTIVE TRACKING
         * =========================
         */

        if (tracking.isEmpty()) {
            return;
        }

        Map<Integer, KnockbackData> copy =
                new HashMap<Integer, KnockbackData>(
                        tracking
                );

        for (
                Map.Entry<Integer, KnockbackData> entry
                : copy.entrySet()
                ) {

            Integer entityId =
                    entry.getKey();

            KnockbackData data =
                    entry.getValue();

            if (data == null) {

                tracking.remove(
                        entityId
                );

                continue;
            }

            EntityPlayer player =
                    findPlayer(
                            data.entityId
                    );

            if (player == null) {

                tracking.remove(
                        entityId
                );

                continue;
            }

            /*
             * Tick verhogen.
             */
            data.ticks++;


            /*
             * =========================
             * ACTUAL VELOCITY
             * =========================
             */

            double actualX =
                    player.motionX;

            double actualZ =
                    player.motionZ;

            double actual =
                    Math.sqrt(
                            actualX * actualX
                                    + actualZ * actualZ
                    );


            /*
             * Hoogste waarde bewaren.
             */
            if (actual >
                    data.actualMaximum) {

                data.actualMaximum =
                        actual;
            }


            /*
             * =========================
             * HUD DATA
             * =========================
             */

            targetName =
                    data.targetName;

            expectedVelocity =
                    data.expectedVelocity;

            actualVelocity =
                    data.actualMaximum;


            /*
             * =========================
             * RATIO
             * =========================
             */

            if (expectedVelocity > 0.0D) {

                ratio =
                        actualVelocity
                                / expectedVelocity;

            } else {

                ratio = 0.0D;
            }


            /*
             * =========================
             * VIOLATION CHECK
             * =========================
             */

            if (actual <
                    data.expectedVelocity
                            * SUSPICIOUS_RATIO) {

                data.suspiciousTicks++;

            } else {

                data.suspiciousTicks = 0;
            }


            if (data.suspiciousTicks >= 2) {

                violationLevel++;

                if (violationLevel > 20) {

                    violationLevel =
                            20;
                }

                data.suspiciousTicks = 0;
            }


            /*
             * =========================
             * STOP TRACKING
             * =========================
             */

            if (data.ticks
                    >= MAX_TRACK_TICKS) {

                tracking.remove(
                        entityId
                );
            }
        }
    }


    /*
     * =========================
     * START TRACKING
     * =========================
     */

    private static void startTracking(
            EntityPlayer player,
            PendingVelocity pending) {

        KnockbackData data =
                new KnockbackData();

        data.entityId =
                player.getEntityId();

        data.targetName =
                player.getName();

        data.expectedVelocity =
                pending.expected;

        data.expectedX =
                pending.motionX;

        data.expectedY =
                pending.motionY;

        data.expectedZ =
                pending.motionZ;

        data.actualMaximum =
                0.0D;

        data.ticks =
                0;

        data.suspiciousTicks =
                0;

        tracking.put(
                data.entityId,
                data
        );

        /*
         * Meteen HUD-data zetten.
         */
        targetName =
                data.targetName;

        expectedVelocity =
                data.expectedVelocity;

        actualVelocity =
                0.0D;

        ratio =
                0.0D;
    }


    /*
     * =========================
     * CURRENT TARGET
     * =========================
     */

    private static EntityPlayer getLookedAtPlayer(
            Minecraft mc) {

        if (mc.objectMouseOver == null) {
            return null;
        }

        if (mc.objectMouseOver.entityHit == null) {
            return null;
        }

        if (!(mc.objectMouseOver.entityHit
                instanceof EntityPlayer)) {

            return null;
        }

        EntityPlayer player =
                (EntityPlayer)
                        mc.objectMouseOver.entityHit;

        if (player == mc.thePlayer) {
            return null;
        }

        if (player.isDead) {
            return null;
        }

        return player;
    }


    /*
     * =========================
     * FIND PLAYER
     * =========================
     */

    private static EntityPlayer findPlayer(
            int entityId) {

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.theWorld == null) {

            return null;
        }

        for (
                Object object
                : mc.theWorld.playerEntities
                ) {

            if (!(object
                    instanceof EntityPlayer)) {

                continue;
            }

            EntityPlayer player =
                    (EntityPlayer) object;

            if (player.getEntityId()
                    == entityId) {

                return player;
            }
        }

        return null;
    }


    /*
     * =========================
     * GETTERS
     * =========================
     */

    public static String getTargetName() {
        return targetName;
    }

    public static double getExpectedVelocity() {
        return expectedVelocity;
    }

    public static double getActualVelocity() {
        return actualVelocity;
    }

    public static double getRatio() {
        return ratio;
    }

    public static int getViolationLevel() {
        return violationLevel;
    }

    public static double getExpectedHorizontal() {
        return expectedVelocity;
    }

    public static double getActualHorizontal() {
        return actualVelocity;
    }


    /*
     * =========================
     * RESET
     * =========================
     */

    public static void reset() {
        clear();
    }

    public static synchronized void clear() {

        pendingVelocities.clear();

        tracking.clear();

        targetName =
                "-";

        expectedVelocity =
                0.0D;

        actualVelocity =
                0.0D;

        ratio =
                0.0D;

        violationLevel =
                0;
    }


    /*
     * =========================
     * PENDING PACKET
     * =========================
     */

    private static class PendingVelocity {

        private int entityId;

        private double motionX;

        private double motionY;

        private double motionZ;

        private double expected;

        private long timestamp;
    }


    /*
     * =========================
     * ACTIVE DATA
     * =========================
     */

    private static class KnockbackData {

        private int entityId;

        private String targetName;

        private double expectedVelocity;

        private double expectedX;

        private double expectedY;

        private double expectedZ;

        private double actualMaximum;

        private int ticks;

        private int suspiciousTicks;
    }
}