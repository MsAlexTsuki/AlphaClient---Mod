package com.alpharip.staffmod.staffops.ideas;
import java.util.LinkedHashMap;
import java.util.Map;
/** Named countdown model; chat regex parsing, configuration and HUD bars are pending. */
public final class ChatTimerHud {
    private final Map<String, Long> deadlines = new LinkedHashMap<String, Long>();
    public void start(String name, long now, long durationMillis) {
        if (name != null && durationMillis >= 0L) deadlines.put(name, now + durationMillis);
    }
    public long getRemainingMillis(String name, long now) {
        Long deadline = deadlines.get(name);
        return deadline == null ? 0L : Math.max(0L, deadline.longValue() - now);
    }
    public void remove(String name) { deadlines.remove(name); }
    public Map<String, Long> getDeadlines() { return new LinkedHashMap<String, Long>(deadlines); }
}
