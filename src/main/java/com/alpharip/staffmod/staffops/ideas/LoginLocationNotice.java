package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Tracks location labels explicitly supplied by a caller; it does not access IP or geolocation services. */
public final class LoginLocationNotice {
    public static final String FEATURE_NAME="Login Location Notice", CATEGORY="Security", STATUS="NEW";
    private final Set<String> known=new HashSet<String>();
    public synchronized boolean record(String location){
        if(location==null||location.trim().isEmpty())throw new IllegalArgumentException("location required");
        return known.add(location.trim().toLowerCase(Locale.ROOT));
    }
    public synchronized boolean isKnown(String location){return location!=null&&known.contains(location.trim().toLowerCase(Locale.ROOT));}
    public synchronized Set<String> knownLocations(){return Collections.unmodifiableSet(new HashSet<String>(known));}
}