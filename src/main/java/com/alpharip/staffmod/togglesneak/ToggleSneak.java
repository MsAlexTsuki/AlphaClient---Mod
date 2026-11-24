package com.alpharip.staffmod.togglesneak;

/** Local ToggleSneak state; this prototype does not register with AlphaClient Core. */
public final class ToggleSneak {
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
