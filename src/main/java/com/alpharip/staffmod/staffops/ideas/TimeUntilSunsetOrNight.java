package com.alpharip.staffmod.staffops.ideas;

/** Foundation for displaying time remaining until sunset or night, given world-time ticks. */
public class TimeUntilSunsetOrNight {
 private long worldTimeTicks;
 public void setWorldTimeTicks(long ticks){worldTimeTicks=Math.floorMod(ticks,24000L);}
 public long getWorldTimeTicks(){return worldTimeTicks;}
 public long ticksUntilNight(){long nightStart=13000L;return worldTimeTicks<nightStart?nightStart-worldTimeTicks:24000L-worldTimeTicks+nightStart;}
 public long ticksUntilSunset(){long sunset=12000L;return worldTimeTicks<sunset?sunset-worldTimeTicks:24000L-worldTimeTicks+sunset;}
}
