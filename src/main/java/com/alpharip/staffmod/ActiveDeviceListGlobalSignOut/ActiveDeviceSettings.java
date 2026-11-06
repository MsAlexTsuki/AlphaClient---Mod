package com.alpharip.staffmod.activedevicelistsignout;

/** Session-local settings for the device-list preview. */
public final class ActiveDeviceSettings {
    private boolean enabled = true;
    private boolean showSignedOut = true;
    private int maximumDevices = 50;
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean value){enabled=value;}
    public boolean isShowSignedOut(){return showSignedOut;} public void setShowSignedOut(boolean value){showSignedOut=value;}
    public int getMaximumDevices(){return maximumDevices;}
    public void setMaximumDevices(int value){if(value<1||value>500)throw new IllegalArgumentException("Maximum devices must be between 1 and 500.");maximumDevices=value;}
}
