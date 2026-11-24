package com.alpharip.staffmod.togglesneak;

/** Local ToggleSprint state; module is available immediately after its activation key is assigned. */
public final class ToggleSprint {
    private boolean enabled = true;
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
