package com.alpharip.staffmod.TimeTill;

/** Converts remaining Minecraft world ticks into a compact HUD countdown. */
public final class TimeTillFormatter {
    private TimeTillFormatter() { }

    public static long ticksUntil(long worldTimeTicks, TimeTillSettings.Target target) {
        long timeOfDay = Math.floorMod(worldTimeTicks, 24000L);
        long targetTick = target == TimeTillSettings.Target.NIGHT ? 13000L : 12000L;

        if (timeOfDay < targetTick) {
            return targetTick - timeOfDay;
        }
        return 24000L - timeOfDay + targetTick;
    }

    public static String format(long remainingTicks) {
        long totalSeconds = (remainingTicks + 19L) / 20L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return minutes + "m " + seconds + "s";
    }
}
