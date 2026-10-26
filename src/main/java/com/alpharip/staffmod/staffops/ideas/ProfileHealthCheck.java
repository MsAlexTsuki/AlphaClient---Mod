package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Checks caller-provided profile settings for absent keys and blank values. */
public final class ProfileHealthCheck {
    public static final String FEATURE_NAME="Profile Health Check", CATEGORY="Cross-Category", STATUS="NEW";
    public List<String> inspect(Map<String,String> settings,Collection<String> requiredKeys){
        Map<String,String> map=settings==null?Collections.<String,String>emptyMap():settings;
        List<String> issues=new ArrayList<String>();
        if(requiredKeys!=null)for(String key:requiredKeys)if(key!=null&&(!map.containsKey(key)||map.get(key)==null||map.get(key).trim().isEmpty()))issues.add(key);
        Collections.sort(issues);return Collections.unmodifiableList(issues);
    }
}