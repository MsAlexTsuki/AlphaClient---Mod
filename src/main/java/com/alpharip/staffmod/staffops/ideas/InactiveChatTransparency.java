package com.alpharip.staffmod.staffops.ideas;

/** Foundation for Inactive Chat Transparency; configuration/state only, runtime integration is not implemented here. */
public class InactiveChatTransparency {
    public boolean enabled = true;
    public float activeOpacity = 1.0f;
    public float inactiveOpacity = 0.65f;
    public int inactiveDelaySeconds = 3;
}
