package com.alpharip.staffmod.togglesneak;

/** ToggleSneak module state. Enabled by default; sneak is not held until activated. */
public final class ToggleSneak extends AbstractToggleMovementModule {
    public ToggleSneak() {
        super(ToggleMovementType.SNEAK);
    }
}
