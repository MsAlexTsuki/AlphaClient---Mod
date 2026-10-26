package com.alpharip.staffmod.staffops.ideas;

/** Small permission gate for callers; permission sources must be supplied externally. */
public class DetectionPermissionGuard {
    public boolean isAllowed(boolean hasPermission) {
        return hasPermission;
    }
}
