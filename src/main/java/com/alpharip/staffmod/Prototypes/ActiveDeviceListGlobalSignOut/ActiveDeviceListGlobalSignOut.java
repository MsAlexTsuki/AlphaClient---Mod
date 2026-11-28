package com.alpharip.staffmod.activedevicelistsignout;

import java.util.List;

/** Main facade for the local active-device and sign-out workflow prototype. */
public final class ActiveDeviceListGlobalSignOut {
    private final ActiveDeviceSettings settings = new ActiveDeviceSettings();
    private final ActiveDeviceRegistry registry = new ActiveDeviceRegistry(settings.getMaximumDevices());
    private final ActiveDeviceService service = new ActiveDeviceService(registry, settings);

    public boolean isEnabled() { return settings.isEnabled(); }
    public void setEnabled(boolean enabled) { settings.setEnabled(enabled); }
    public ActiveDeviceSettings getSettings() { return settings; }
    public ActiveDeviceRegistry getRegistry() { return registry; }
    public ActiveDeviceService getService() { return service; }
    public List<ActiveDeviceSession> getDevices() { return registry.getAll(); }
    public void clearDevices() { registry.clear(); }
}
