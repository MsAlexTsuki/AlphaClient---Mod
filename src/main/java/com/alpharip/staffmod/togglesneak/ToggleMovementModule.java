package com.alpharip.staffmod.togglesneak;

/** Common contract for an independently enabled movement toggle module. */
public interface ToggleMovementModule {
    ToggleMovementType getMovementType();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    boolean isToggled();
    void toggle();
    void reset();
}
