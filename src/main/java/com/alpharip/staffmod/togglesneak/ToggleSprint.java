package com.alpharip.staffmod.togglesneak;

/** Local ToggleSprint state; default module enablement matches the original idea scaffold. */
public final class ToggleSprint {
    private boolean enabled;
    private boolean toggled;

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) toggled = false;
    }

    public boolean isToggled() { return enabled && toggled; }

    public void toggle() {
        if (enabled) toggled = !toggled;
    }

    public void reset() { toggled = false; }
}
