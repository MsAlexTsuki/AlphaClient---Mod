package com.alpharip.staffmod.togglesneak;

/** ToggleSprint module state. Enabled by default; sprint is not held until activated. */
public final class ToggleSprint extends AbstractToggleMovementModule {
    public ToggleSprint() {
        super(ToggleMovementType.SPRINT);
    }
}
