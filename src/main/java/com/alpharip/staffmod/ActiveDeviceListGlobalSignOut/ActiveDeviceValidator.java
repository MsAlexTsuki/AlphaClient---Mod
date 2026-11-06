package com.alpharip.staffmod.activedevicelistsignout;

public final class ActiveDeviceValidator {
    private ActiveDeviceValidator() { }
    public static String validate(String deviceName, String platform, String accountLabel,
            long createdAtMillis, long lastSeenAtMillis) {
        if (blank(deviceName)) return "Device name is required.";
        if (deviceName.trim().length() > 64) return "Device name must be 64 characters or fewer.";
        if (blank(platform)) return "Platform is required.";
        if (platform.trim().length() > 48) return "Platform must be 48 characters or fewer.";
        if (accountLabel != null && accountLabel.length() > 96) return "Account label must be 96 characters or fewer.";
        if (createdAtMillis < 0L || lastSeenAtMillis < createdAtMillis) return "Device timestamps are invalid.";
        return null;
    }
    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
