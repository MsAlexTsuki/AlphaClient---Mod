package com.alpharip.staffmod.activedevicelistsignout;

import java.util.UUID;

/** Immutable locally supplied device-session snapshot; it is not proof of authentication. */
public final class ActiveDeviceSession {
    private final String id, deviceName, platform, accountLabel;
    private final long createdAtMillis, lastSeenAtMillis;
    private final boolean currentDevice;
    private final ActiveDeviceStatus status;
    private final long signOutRequestedAtMillis;

    public ActiveDeviceSession(String deviceName, String platform, String accountLabel,
            long createdAtMillis, long lastSeenAtMillis, boolean currentDevice) {
        this(UUID.randomUUID().toString(), deviceName, platform, accountLabel, createdAtMillis,
                lastSeenAtMillis, currentDevice, ActiveDeviceStatus.ACTIVE, 0L);
    }

    private ActiveDeviceSession(String id, String deviceName, String platform, String accountLabel,
            long createdAtMillis, long lastSeenAtMillis, boolean currentDevice,
            ActiveDeviceStatus status, long signOutRequestedAtMillis) {
        this.id=id; this.deviceName=deviceName; this.platform=platform; this.accountLabel=accountLabel;
        this.createdAtMillis=createdAtMillis; this.lastSeenAtMillis=lastSeenAtMillis;
        this.currentDevice=currentDevice; this.status=status;
        this.signOutRequestedAtMillis=signOutRequestedAtMillis;
    }
    public String getId(){return id;} public String getDeviceName(){return deviceName;}
    public String getPlatform(){return platform;} public String getAccountLabel(){return accountLabel;}
    public long getCreatedAtMillis(){return createdAtMillis;} public long getLastSeenAtMillis(){return lastSeenAtMillis;}
    public boolean isCurrentDevice(){return currentDevice;} public ActiveDeviceStatus getStatus(){return status;}
    public long getSignOutRequestedAtMillis(){return signOutRequestedAtMillis;}
    public ActiveDeviceSession requestSignOut(long now) {
        if (currentDevice || status != ActiveDeviceStatus.ACTIVE) return this;
        return new ActiveDeviceSession(id,deviceName,platform,accountLabel,createdAtMillis,lastSeenAtMillis,
                false,ActiveDeviceStatus.SIGN_OUT_REQUESTED,now);
    }
    public ActiveDeviceSession markSignedOut() {
        if (currentDevice || status == ActiveDeviceStatus.SIGNED_OUT) return this;
        return new ActiveDeviceSession(id,deviceName,platform,accountLabel,createdAtMillis,lastSeenAtMillis,
                false,ActiveDeviceStatus.SIGNED_OUT,signOutRequestedAtMillis);
    }
}
