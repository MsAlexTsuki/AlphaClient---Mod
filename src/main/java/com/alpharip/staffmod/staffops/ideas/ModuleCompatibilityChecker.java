package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Checks explicitly registered module dependencies and conflicts; no automatic module toggling is performed. */
public final class ModuleCompatibilityChecker {
    public static final String NAME = "Module Compatibility Checker";
    public static final String CATEGORY = "Client Utility";
    public enum Severity { INFO, WARNING, ERROR }
    public static final class Issue {
        public final Severity severity;
        public final String module, relatedModule, message;
        private Issue(Severity s,String m,String r,String msg){severity=s;module=m;relatedModule=r;message=msg;}
    }
    private final Map<String,Set<String>> requires=new LinkedHashMap<String,Set<String>>();
    private final Map<String,Set<String>> conflicts=new LinkedHashMap<String,Set<String>>();
    public synchronized void require(String module,String dependency){link(requires,module,dependency);}
    public synchronized void conflict(String module,String incompatible){link(conflicts,module,incompatible);link(conflicts,incompatible,module);}
    private static void link(Map<String,Set<String>> map,String a,String b){
        a=text(a);b=text(b);Set<String>s=map.get(a);if(s==null){s=new LinkedHashSet<String>();map.put(a,s);}s.add(b);
    }
    public synchronized List<Issue> check(Set<String> enabledModules){
        Set<String> active=enabledModules==null?Collections.<String>emptySet():new LinkedHashSet<String>(enabledModules);
        List<Issue> out=new ArrayList<Issue>();
        for(String m:active){
            for(String dep:requires.containsKey(m)?requires.get(m):Collections.<String>emptySet())
                if(!active.contains(dep))out.add(new Issue(Severity.ERROR,m,dep,"Required module is not enabled"));
            for(String other:conflicts.containsKey(m)?conflicts.get(m):Collections.<String>emptySet())
                if(active.contains(other)&&m.compareTo(other)<0)out.add(new Issue(Severity.WARNING,m,other,"Both conflicting modules are enabled"));
        }
        return Collections.unmodifiableList(out);
    }
    private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("Module name is required");return s.trim();}
}