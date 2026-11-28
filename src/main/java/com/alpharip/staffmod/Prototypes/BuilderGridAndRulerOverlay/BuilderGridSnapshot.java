package com.alpharip.staffmod.buildergridandruleroverlay;

/** Immutable state captured for one world-render and HUD pass. */
public final class BuilderGridSnapshot {
    private final boolean gridEnabled;
    private final boolean rulerEnabled;
    private final int gridSpacing;
    private final int areaRadius;
    private final int anchorX;
    private final int anchorY;
    private final int anchorZ;
    private final int playerX;
    private final int playerY;
    private final int playerZ;

    BuilderGridSnapshot(BuilderGridSettings settings, BuilderGridAnchor anchor,
            int playerX, int playerY, int playerZ) {
        this.gridEnabled = settings.isGridEnabled();
        this.rulerEnabled = settings.isRulerEnabled();
        this.gridSpacing = settings.getGridSpacing();
        this.areaRadius = settings.getAreaRadius();
        this.anchorX = anchor.getX();
        this.anchorY = anchor.getY();
        this.anchorZ = anchor.getZ();
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerZ = playerZ;
    }

    public boolean isGridEnabled() { return gridEnabled; }
    public boolean isRulerEnabled() { return rulerEnabled; }
    public int getGridSpacing() { return gridSpacing; }
    public int getAreaRadius() { return areaRadius; }
    public int getAnchorX() { return anchorX; }
    public int getAnchorY() { return anchorY; }
    public int getAnchorZ() { return anchorZ; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getPlayerZ() { return playerZ; }
    public int getOffsetX() { return playerX - anchorX; }
    public int getOffsetY() { return playerY - anchorY; }
    public int getOffsetZ() { return playerZ - anchorZ; }
}
