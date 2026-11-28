package com.alpharip.staffmod.togglesneak;

/** Shared state transitions used by ToggleSneak and ToggleSprint. */
public abstract class AbstractToggleMovementModule implements ToggleMovementModule {
    private final ToggleMovementType movementType;
    private boolean enabled = true;
    private boolean toggled;

    protected AbstractToggleMovementModule(ToggleMovementType movementType) {
        if (movementType == null) {
            throw new IllegalArgumentException("Movement type must not be null.");
        }
        this.movementType = movementType;
    }

    @Override
    public final ToggleMovementType getMovementType() {
        return movementType;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) reset();
    }

    @Override
    public final boolean isToggled() {
        return enabled && toggled;
    }

    @Override
    public final void toggle() {
        if (enabled) toggled = !toggled;
    }

    @Override
    public final void reset() {
        toggled = false;
    }
}
