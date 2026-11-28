package com.alpharip.staffmod.activedevicelistsignout;

import java.util.ArrayList;
import java.util.List;

/** Coordinates local device records and records sign-out intent without contacting a server. */
public final class ActiveDeviceService {
    private final ActiveDeviceRegistry registry;
    private final ActiveDeviceSettings settings;
    public ActiveDeviceService(ActiveDeviceRegistry registry,ActiveDeviceSettings settings){
        if(registry==null||settings==null)throw new IllegalArgumentException("Registry and settings are required.");
        this.registry=registry;this.settings=settings;registry.setMaximumDevices(settings.getMaximumDevices());
    }
    public boolean register(ActiveDeviceSession session){
        if(!settings.isEnabled())return false;
        String error=ActiveDeviceValidator.validate(session==null?null:session.getDeviceName(),
                session==null?null:session.getPlatform(),session==null?null:session.getAccountLabel(),
                session==null?-1L:session.getCreatedAtMillis(),session==null?-1L:session.getLastSeenAtMillis());
        if(error!=null)throw new IllegalArgumentException(error);
        return registry.add(session);
    }
    public boolean requestSignOut(String id,long now){
        ActiveDeviceSession session=registry.get(id);
        if(session==null||session.isCurrentDevice()||session.getStatus()!=ActiveDeviceStatus.ACTIVE)return false;
        return registry.replace(session.requestSignOut(now));
    }
    public ActiveDeviceSignOutResult requestGlobalSignOut(long now){
        if(!settings.isEnabled())return new ActiveDeviceSignOutResult(0,registry.size(),"Device management is disabled.");
        int requested=0,skipped=0;
        for(ActiveDeviceSession session:registry.getAll()){
            if(session.isCurrentDevice()||session.getStatus()!=ActiveDeviceStatus.ACTIVE){skipped++;continue;}
            if(registry.replace(session.requestSignOut(now)))requested++;else skipped++;
        }
        return new ActiveDeviceSignOutResult(requested,skipped,
                "Local sign-out requests recorded; remote sessions have not been revoked.");
    }
    public boolean confirmRemoteSignOut(String id){
        ActiveDeviceSession session=registry.get(id);
        if(session==null||session.getStatus()!=ActiveDeviceStatus.SIGN_OUT_REQUESTED)return false;
        return registry.replace(session.markSignedOut());
    }
    public List<ActiveDeviceSession> getVisibleDevices(){
        List<ActiveDeviceSession> result=new ArrayList<ActiveDeviceSession>();
        for(ActiveDeviceSession session:registry.getAll())
            if(settings.isShowSignedOut()||session.getStatus()!=ActiveDeviceStatus.SIGNED_OUT)result.add(session);
        return result;
    }
    public ActiveDeviceRegistry getRegistry(){return registry;}
    public ActiveDeviceSettings getSettings(){return settings;}
}
