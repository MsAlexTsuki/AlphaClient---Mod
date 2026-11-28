package com.alpharip.staffmod.clutch;

/** Immutable manually supplied clutch highlight. */
public final class ClutchHighlight {
    private final ClutchHighlightType type;
    private final String description;
    private final long timestamp;

    public ClutchHighlight(ClutchHighlightType type, String description, long timestamp) {
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        if (description == null || description.trim().isEmpty()) throw new IllegalArgumentException("description cannot be empty");
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
    }

    public ClutchHighlightType getType() { return type; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
}
