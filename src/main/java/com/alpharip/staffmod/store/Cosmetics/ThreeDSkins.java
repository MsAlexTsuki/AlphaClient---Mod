package com.alpharip.staffmod.staffops.ideas;

/**
 * Foundation for optional 3D skin details.
 * Player-model and skin-renderer hooks are still required for in-game behavior.
 */
public final class ThreeDSkins {
    public static final String FEATURE_NAME = "3D Skins";
    private boolean enabled = false;
    private boolean hatLayer = true;
    private boolean jacketLayer = true;
    private boolean leftSleeve = true;
    private boolean rightSleeve = true;
    private boolean leftTrouserLeg = true;
    private boolean rightTrouserLeg = true;
    private boolean showOwnSkin = true;
    private boolean showOtherPlayers = true;
    private float depth = 1.0F;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public boolean isHatLayer() { return hatLayer; }
    public void setHatLayer(boolean value) { hatLayer = value; }
    public boolean isJacketLayer() { return jacketLayer; }
    public void setJacketLayer(boolean value) { jacketLayer = value; }
    public boolean isLeftSleeve() { return leftSleeve; }
    public void setLeftSleeve(boolean value) { leftSleeve = value; }
    public boolean isRightSleeve() { return rightSleeve; }
    public void setRightSleeve(boolean value) { rightSleeve = value; }
    public boolean isLeftTrouserLeg() { return leftTrouserLeg; }
    public void setLeftTrouserLeg(boolean value) { leftTrouserLeg = value; }
    public boolean isRightTrouserLeg() { return rightTrouserLeg; }
    public void setRightTrouserLeg(boolean value) { rightTrouserLeg = value; }
    public boolean isShowOwnSkin() { return showOwnSkin; }
    public void setShowOwnSkin(boolean value) { showOwnSkin = value; }
    public boolean isShowOtherPlayers() { return showOtherPlayers; }
    public void setShowOtherPlayers(boolean value) { showOtherPlayers = value; }
    public float getDepth() { return depth; }
    public void setDepth(float value) {
        if (!Float.isNaN(value) && !Float.isInfinite(value)) {
            depth = Math.max(0.0F, Math.min(2.0F, value));
        }
    }
}
