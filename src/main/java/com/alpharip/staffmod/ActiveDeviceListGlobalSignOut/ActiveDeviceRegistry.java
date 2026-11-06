package com.alpharip.staffmod.activedevicelistsignout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bounded in-memory registry; it stores only caller-supplied device snapshots. */
public final class ActiveDeviceRegistry {
    private final Map<String,ActiveDeviceSession> sessions = new LinkedHashMap<String,ActiveDeviceSession>();
    private int maximumDevices;
    public ActiveDeviceRegistry(int maximumDevices){setMaximumDevices(maximumDevices);}
    public synchronized void setMaximumDevices(int value){if(value<1||value>500)throw new IllegalArgumentException("Invalid device limit.");maximumDevices=value;trim();}
    public synchronized boolean add(ActiveDeviceSession session){
        if(session==null||sessions.containsKey(session.getId()))return false;
        if(sessions.size()>=maximumDevices)return false;
        sessions.put(session.getId(),session);return true;
    }
    public synchronized ActiveDeviceSession get(String id){return sessions.get(id);}
    public synchronized boolean replace(ActiveDeviceSession session){
        if(session==null||!sessions.containsKey(session.getId()))return false;
        sessions.put(session.getId(),session);return true;
    }
    public synchronized List<ActiveDeviceSession> getAll(){return Collections.unmodifiableList(new ArrayList<ActiveDeviceSession>(sessions.values()));}
    public synchronized int size(){return sessions.size();}
    public synchronized void clear(){sessions.clear();}
    private void trim(){while(sessions.size()>maximumDevices){String first=sessions.keySet().iterator().next();sessions.remove(first);}}
}
