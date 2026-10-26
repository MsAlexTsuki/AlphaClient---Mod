package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Hitbox inspection utility foundation; Minecraft hooks, permissions, UI, and tests are not yet connected. */
public final class StaffHitboxViewer {
    private boolean enabled;
    private final List<String> samples = new ArrayList<String>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public void recordSample(String sample) { if (sample != null && !sample.trim().isEmpty()) samples.add(sample); }
    public List<String> getSamples() { return Collections.unmodifiableList(samples); }
    public void clearSamples() { samples.clear(); }
}
