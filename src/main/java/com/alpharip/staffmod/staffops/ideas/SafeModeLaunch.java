package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Describes a safe-mode launch plan. Actual module loading is owned by a future integration layer. */
public final class SafeModeLaunch {
    public static final String FEATURE_NAME="Safe Mode Launch", CATEGORY="Security", STATUS="NEW";
    private boolean requested;
    private final Set<String> excluded=new LinkedHashSet<String>();
    public boolean isRequested(){return requested;}
    public void setRequested(boolean value){requested=value;}
    public void excludeModule(String id){if(id!=null&&!id.trim().isEmpty())excluded.add(id.trim());}
    public void allowModule(String id){if(id!=null)excluded.remove(id.trim());}
    public Set<String> getExcludedModules(){return Collections.unmodifiableSet(new LinkedHashSet<String>(excluded));}
    public boolean shouldLoad(String id){return !requested||id==null||!excluded.contains(id.trim());}
}