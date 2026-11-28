package com.alpharip.staffmod.staffops.ideas;

/**
 * Foundation for rendering dropped items as flat 2D sprites.
 * Minecraft entity-renderer hooks are still required for in-game behavior.
 */
public final class TwoDItems {
    public static final String FEATURE_NAME = "2D Items";
    private boolean enabled = false;
    private boolean applyToDroppedItems = true;
    private boolean applyToItemFrames = false;
    private boolean keepVanillaItemScale = true;
    private float scale = 1.0F;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public boolean isApplyToDroppedItems() { return applyToDroppedItems; }
    public void setApplyToDroppedItems(boolean value) { applyToDroppedItems = value; }
    public boolean isApplyToItemFrames() { return applyToItemFrames; }
    public void setApplyToItemFrames(boolean value) { applyToItemFrames = value; }
    public boolean isKeepVanillaItemScale() { return keepVanillaItemScale; }
    public void setKeepVanillaItemScale(boolean value) { keepVanillaItemScale = value; }
    public float getScale() { return scale; }
    public void setScale(float value) {
        if (!Float.isNaN(value) && !Float.isInfinite(value)) {
            scale = Math.max(0.25F, Math.min(3.0F, value));
        }
    }
}
