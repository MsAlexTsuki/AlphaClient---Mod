package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Claim-region model; /f map parsing and translucent world rendering are pending. */
public final class ClaimEcho {
    public static final class Region {
        public final int minX, minZ, maxX, maxZ;
        public Region(int x1, int z1, int x2, int z2) {
            minX = Math.min(x1, x2); maxX = Math.max(x1, x2);
            minZ = Math.min(z1, z2); maxZ = Math.max(z1, z2);
        }
    }
    private final List<Region> regions = new ArrayList<Region>();
    public void addRegion(Region region) { if (region != null) regions.add(region); }
    public void clear() { regions.clear(); }
    public List<Region> getRegions() { return Collections.unmodifiableList(new ArrayList<Region>(regions)); }
}
