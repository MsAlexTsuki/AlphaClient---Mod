package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Compares two flat configuration snapshots without writing either snapshot. */
public final class ConfigChangeDiff {
    public static final String FEATURE_NAME="Config Change Diff", CATEGORY="Security", STATUS="NEW";
    public enum ChangeType { ADDED, REMOVED, MODIFIED }
    public static final class Change {
        public final String key, before, after; public final ChangeType type;
        private Change(String k,String b,String a,ChangeType t){key=k;before=b;after=a;type=t;}
    }
    public List<Change> compare(Map<String,String> before, Map<String,String> after) {
        Map<String,String> a=before==null?Collections.<String,String>emptyMap():before;
        Map<String,String> b=after==null?Collections.<String,String>emptyMap():after;
        Set<String> keys=new TreeSet<String>(); keys.addAll(a.keySet()); keys.addAll(b.keySet());
        List<Change> out=new ArrayList<Change>();
        for(String key:keys){boolean hasA=a.containsKey(key),hasB=b.containsKey(key);String x=a.get(key),y=b.get(key);
            if(!hasA)out.add(new Change(key,null,y,ChangeType.ADDED));
            else if(!hasB)out.add(new Change(key,x,null,ChangeType.REMOVED));
            else if(!Objects.equals(x,y))out.add(new Change(key,x,y,ChangeType.MODIFIED));
        } return Collections.unmodifiableList(out);
    }
}