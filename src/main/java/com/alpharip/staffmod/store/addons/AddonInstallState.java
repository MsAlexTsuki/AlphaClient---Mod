package com.alpharip.staffmod.store.addons;

/** Local catalogue state only; INSTALLED does not mean external code was installed. */
public enum AddonInstallState {
    AVAILABLE,
    INSTALLED,
    DISABLED,
    REMOVED
}
