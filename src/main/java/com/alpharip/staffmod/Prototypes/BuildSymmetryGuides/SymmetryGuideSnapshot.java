package com.alpharip.staffmod.buildsymmetryguides;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable render-ready state for one targeted source block. */
public final class SymmetryGuideSnapshot {
    private final BlockCoordinates source;
    private final BlockCoordinates origin;
    private final List<SymmetryGuidePoint> mirrorPoints;
    private final boolean showMirrorPreview;
    private final boolean showAxisLines;
    private final boolean showConnectionLines;
    private final int guideDistance;
    private final float opacity;
    private final int colorRgb;
    private final SymmetryAxis axis;

    public SymmetryGuideSnapshot(BlockCoordinates source, BlockCoordinates origin,
            List<SymmetryGuidePoint> points, BuildSymmetryGuideConfig config) {
        if (source == null || origin == null || config == null) {
            throw new IllegalArgumentException("snapshot input");
        }
        this.source = source;
        this.origin = origin;
        this.mirrorPoints = Collections.unmodifiableList(new ArrayList<SymmetryGuidePoint>(
                points == null ? Collections.<SymmetryGuidePoint>emptyList() : points));
        this.showMirrorPreview = config.isShowMirrorPreview();
        this.showAxisLines = config.isShowAxisLines();
        this.showConnectionLines = config.isShowConnectionLines();
        this.guideDistance = config.getGuideDistance();
        this.opacity = config.getOpacity();
        this.colorRgb = config.getColor().getRgb();
        this.axis = config.getAxis();
    }

    public BlockCoordinates getSource() { return source; }
    public BlockCoordinates getOrigin() { return origin; }
    public List<SymmetryGuidePoint> getMirrorPoints() { return mirrorPoints; }
    public boolean isShowMirrorPreview() { return showMirrorPreview; }
    public boolean isShowAxisLines() { return showAxisLines; }
    public boolean isShowConnectionLines() { return showConnectionLines; }
    public int getGuideDistance() { return guideDistance; }
    public float getOpacity() { return opacity; }
    public int getColorRgb() { return colorRgb; }
    public SymmetryAxis getAxis() { return axis; }
}
