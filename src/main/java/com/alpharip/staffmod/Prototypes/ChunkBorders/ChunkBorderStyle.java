package com.alpharip.staffmod.chunkborders;

/** Visual presets used by the chunk boundary renderer. */
public enum ChunkBorderStyle {
    DEFAULT("Default", 0xFFFF5555, 0xFF55AAFF, 1.5F),
    MINIMAL("Minimal", 0xFF66CC99, 0xFF66CC99, 1.0F),
    HIGH_CONTRAST("High contrast", 0xFFFFFF00, 0xFFFF55FF, 2.0F);

    private final String displayName;
    private final int verticalRgb;
    private final int horizontalRgb;
    private final float lineWidth;

    ChunkBorderStyle(String displayName, int verticalRgb,
            int horizontalRgb, float lineWidth) {
        this.displayName = displayName;
        this.verticalRgb = verticalRgb;
        this.horizontalRgb = horizontalRgb;
        this.lineWidth = lineWidth;
    }

    public String getDisplayName() { return displayName; }
    public int getVerticalRgb() { return verticalRgb; }
    public int getHorizontalRgb() { return horizontalRgb; }
    public float getLineWidth() { return lineWidth; }

    public ChunkBorderStyle next() {
        ChunkBorderStyle[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static ChunkBorderStyle fromName(String name) {
        if (name != null) {
            for (ChunkBorderStyle value : values()) {
                if (value.name().equalsIgnoreCase(name)
                        || value.displayName.equalsIgnoreCase(name)) return value;
            }
        }
        return DEFAULT;
    }
}
