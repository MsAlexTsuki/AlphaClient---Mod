package com.alpharip.staffmod.buildsymmetryguides;

/** Validated local settings for symmetry calculation and rendering. */
public final class BuildSymmetryGuideConfig {
    private boolean enabled;
    private SymmetryAxis axis = SymmetryAxis.X;
    private int originX;
    private int originZ;
    private boolean showMirrorPreview = true;
    private boolean showAxisLines = true;
    private boolean showConnectionLines = true;
    private int guideDistance = 24;
    private float opacity = 0.75F;
    private SymmetryGuideColor color = SymmetryGuideColor.GOLD;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public SymmetryAxis getAxis() { return axis; }
    public void setAxis(SymmetryAxis value) { axis = value == null ? SymmetryAxis.X : value; }
    public int getOriginX() { return originX; }
    public int getOriginZ() { return originZ; }
    public void setOrigin(int x, int z) { originX = x; originZ = z; }
    public boolean isShowMirrorPreview() { return showMirrorPreview; }
    public void setShowMirrorPreview(boolean value) { showMirrorPreview = value; }
    public boolean isShowAxisLines() { return showAxisLines; }
    public void setShowAxisLines(boolean value) { showAxisLines = value; }
    public boolean isShowConnectionLines() { return showConnectionLines; }
    public void setShowConnectionLines(boolean value) { showConnectionLines = value; }
    public int getGuideDistance() { return guideDistance; }
    public void setGuideDistance(int value) { guideDistance = Math.max(4, Math.min(64, value)); }
    public float getOpacity() { return opacity; }
    public void setOpacity(float value) { opacity = Math.max(0.10F, Math.min(1.0F, value)); }
    public SymmetryGuideColor getColor() { return color; }
    public void setColor(SymmetryGuideColor value) {
        color = value == null ? SymmetryGuideColor.GOLD : value;
    }
}
