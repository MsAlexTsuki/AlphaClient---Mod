package com.alpharip.staffmod.chunkborders;

/** Immutable settings snapshot for one world-render pass. */
public final class ChunkBordersSnapshot {
    private final int playerChunkX;
    private final int playerChunkZ;
    private final int renderRadius;
    private final ChunkBorderStyle style;
    private final boolean showVerticalPillars;
    private final boolean showHorizontalGrid;
    private final boolean highlightCurrentChunk;
    private final float opacity;

    ChunkBordersSnapshot(ChunkBordersSettings settings, int playerChunkX, int playerChunkZ) {
        this.playerChunkX = playerChunkX;
        this.playerChunkZ = playerChunkZ;
        this.renderRadius = settings.getRenderRadius();
        this.style = settings.getStyle();
        this.showVerticalPillars = settings.isShowVerticalPillars();
        this.showHorizontalGrid = settings.isShowHorizontalGrid();
        this.highlightCurrentChunk = settings.isHighlightCurrentChunk();
        this.opacity = settings.getOpacity();
    }

    public int getPlayerChunkX() { return playerChunkX; }
    public int getPlayerChunkZ() { return playerChunkZ; }
    public int getRenderRadius() { return renderRadius; }
    public ChunkBorderStyle getStyle() { return style; }
    public boolean isShowVerticalPillars() { return showVerticalPillars; }
    public boolean isShowHorizontalGrid() { return showHorizontalGrid; }
    public boolean isHighlightCurrentChunk() { return highlightCurrentChunk; }
    public float getOpacity() { return opacity; }
}
