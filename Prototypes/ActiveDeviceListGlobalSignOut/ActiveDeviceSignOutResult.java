package com.alpharip.staffmod.activedevicelistsignout;

/** Describes local sign-out requests; it never confirms remote revocation. */
public final class ActiveDeviceSignOutResult {
    private final int requested, skipped;
    private final String message;
    public ActiveDeviceSignOutResult(int requested,int skipped,String message){this.requested=requested;this.skipped=skipped;this.message=message;}
    public int getRequested(){return requested;} public int getSkipped(){return skipped;} public String getMessage(){return message;}
}
