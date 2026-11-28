package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Pearl landing marker model; detection and in-world rendering are pending. */
public final class PearlEcho {
    public static final class Marker {
        public final String player;
        public final double x, y, z;
        public final long timestamp;
        public Marker(String player, double x, double y, double z, long timestamp) {
            this.player = player == null ? "" : player; this.x = x; this.y = y; this.z = z; this.timestamp = timestamp;
        }
        public long ageMillis(long now) { return Math.max(0L, now - timestamp); }
    }
    private final List<Marker> markers = new ArrayList<Marker>();
    public void add(Marker marker) { if (marker != null) markers.add(marker); }
    public void removeExpired(long now, long lifetimeMillis) {
        for (int i = markers.size() - 1; i >= 0; i--) if (now - markers.get(i).timestamp > lifetimeMillis) markers.remove(i);
    }
    public List<Marker> getMarkers() { return Collections.unmodifiableList(new ArrayList<Marker>(markers)); }
}
