package com.alpharip.staffmod.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class CPSTracker {

    /*
     * Laatste 1 seconde voor de actuele CPS.
     */
    private static final long CPS_WINDOW_NANOS =
            1000000000L;

    /*
     * Laatste 5 seconden voor het gemiddelde.
     */
    private static final long AVERAGE_WINDOW_NANOS =
            5000000000L;

    /*
     * Veiligheidslimiet per speler.
     */
    private static final int MAX_EVENTS =
            200;

    private static final Map<Integer, PlayerData> players =
            new ConcurrentHashMap<Integer, PlayerData>();

    private CPSTracker() {
    }

    /*
     * Registreer een swing van een speler.
     */
    public static void recordSwing(int entityId) {

        if (entityId < 0) {
            return;
        }

        long now =
                System.nanoTime();

        PlayerData data =
                players.get(entityId);

        if (data == null) {

            PlayerData newData =
                    new PlayerData();

            PlayerData existing =
                    players.putIfAbsent(
                            entityId,
                            newData
                    );

            if (existing != null) {
                data = existing;
            } else {
                data = newData;
            }
        }

        data.record(now);
    }

    /*
     * Actuele CPS.
     *
     * Dit is het aantal swings in de laatste seconde.
     */
    public static int getCPS(int entityId) {

        PlayerData data =
                players.get(entityId);

        if (data == null) {
            return 0;
        }

        return data.getCPS();
    }

    /*
     * Gemiddelde CPS over maximaal
     * de laatste 5 seconden.
     */
    public static double getAverageCPS(int entityId) {

        PlayerData data =
                players.get(entityId);

        if (data == null) {
            return 0.0D;
        }

        return data.getAverageCPS();
    }

    /*
     * Hoogste gemeten CPS tijdens deze sessie.
     */
    public static int getMaxCPS(int entityId) {

        PlayerData data =
                players.get(entityId);

        if (data == null) {
            return 0;
        }

        return data.getMaxCPS();
    }

    /*
     * Totaal aantal geregistreerde swings.
     */
    public static long getTotalSwings(int entityId) {

        PlayerData data =
                players.get(entityId);

        if (data == null) {
            return 0L;
        }

        return data.getTotalSwings();
    }

    /*
     * Is de speler momenteel aan het klikken/swingen?
     */
    public static boolean isActive(int entityId) {

        PlayerData data =
                players.get(entityId);

        if (data == null) {
            return false;
        }

        return data.isActive();
    }

    /*
     * Verwijder de data van één speler.
     */
    public static void remove(int entityId) {

        players.remove(entityId);
    }

    /*
     * Alles verwijderen.
     *
     * Wordt bijvoorbeeld gebruikt bij disconnect.
     */
    public static void clear() {

        players.clear();
    }

    /*
     * Data voor één speler.
     */
    private static class PlayerData {

        private final ConcurrentLinkedDeque<Long> events =
                new ConcurrentLinkedDeque<Long>();

        private long firstEventTime =
                0L;

        private long lastEventTime =
                0L;

        private long totalSwings =
                0L;

        private int maxCPS =
                0;

        /*
         * Nieuwe swing.
         */
        public synchronized void record(long now) {

            /*
             * Eerste event opslaan.
             */
            if (firstEventTime == 0L) {

                firstEventTime =
                        now;
            }

            lastEventTime =
                    now;

            totalSwings++;

            events.addLast(now);

            /*
             * Oude events verwijderen.
             */
            prune(
                    now
            );

            /*
             * Veiligheidslimiet.
             */
            while (events.size() > MAX_EVENTS) {

                events.pollFirst();
            }

            /*
             * Max CPS opnieuw berekenen.
             */
            int currentCPS =
                    calculateCurrentCPS(
                            now
                    );

            if (currentCPS > maxCPS) {

                maxCPS =
                        currentCPS;
            }
        }

        /*
         * Actuele CPS.
         */
        public synchronized int getCPS() {

            long now =
                    System.nanoTime();

            prune(
                    now
            );

            return calculateCurrentCPS(
                    now
            );
        }

        /*
         * Gemiddelde CPS.
         */
        public synchronized double getAverageCPS() {

            long now =
                    System.nanoTime();

            prune(
                    now
            );

            if (events.isEmpty()) {
                return 0.0D;
            }

            /*
             * We bekijken maximaal 5 seconden.
             */
            long elapsed =
                    now - firstEventTime;

            if (elapsed <= 0L) {
                return 0.0D;
            }

            if (elapsed > AVERAGE_WINDOW_NANOS) {

                elapsed =
                        AVERAGE_WINDOW_NANOS;
            }

            int count =
                    countEventsInWindow(
                            now,
                            AVERAGE_WINDOW_NANOS
                    );

            if (count <= 0) {
                return 0.0D;
            }

            /*
             * Omrekenen naar CPS.
             */
            return
                    ((double) count)
                            /
                            ((double) elapsed
                                    / 1000000000.0D);
        }

        /*
         * Hoogste CPS.
         */
        public synchronized int getMaxCPS() {

            /*
             * Update max met de huidige waarde.
             */
            int current =
                    getCPS();

            if (current > maxCPS) {

                maxCPS =
                        current;
            }

            return maxCPS;
        }

        public synchronized long getTotalSwings() {

            return totalSwings;
        }

        public synchronized boolean isActive() {

            if (lastEventTime == 0L) {
                return false;
            }

            long now =
                    System.nanoTime();

            return
                    now - lastEventTime
                            <= CPS_WINDOW_NANOS;
        }

        /*
         * Bereken CPS binnen één seconde.
         */
        private int calculateCurrentCPS(
                long now) {

            return countEventsInWindow(
                    now,
                    CPS_WINDOW_NANOS
            );
        }

        /*
         * Tel events binnen een bepaalde window.
         */
        private int countEventsInWindow(
                long now,
                long window) {

            int count =
                    0;

            for (Long timestamp : events) {

                if (timestamp == null) {
                    continue;
                }

                long age =
                        now
                                - timestamp.longValue();

                if (age >= 0L
                        && age <= window) {

                    count++;
                }
            }

            return count;
        }

        /*
         * Verwijder alles ouder dan 5 seconden.
         */
        private void prune(long now) {

            Long oldest;

            while (
                    (oldest = events.peekFirst()) != null
                    ) {

                long age =
                        now
                                - oldest.longValue();

                if (age <= AVERAGE_WINDOW_NANOS) {
                    break;
                }

                events.pollFirst();
            }

            /*
             * Als er geen events meer zijn,
             * resetten we de actieve periode.
             */
            if (events.isEmpty()) {

                firstEventTime =
                        0L;

                lastEventTime =
                        0L;
            }
        }
    }
}