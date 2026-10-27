package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Groups key assignments by key code and reports collisions. Does not alter Minecraft key bindings. */
public final class KeybindConflictAnalyzer {
    public static final String NAME = "Keybind Conflict Analyzer";
    public static final String CATEGORY = "Settings";
    private final Map<String,Integer> assignments = new LinkedHashMap<String,Integer>();
    public synchronized void assign(String action, int keyCode) {
        if(action==null || action.trim().isEmpty()) throw new IllegalArgumentException("Action is required");
        assignments.put(action.trim(), keyCode);
    }
    public synchronized boolean remove(String action){return action!=null && assignments.remove(action.trim())!=null;}
    public synchronized Map<Integer,List<String>> getConflicts() {
        Map<Integer,List<String>> grouped=new TreeMap<Integer,List<String>>();
        for(Map.Entry<String,Integer> e:assignments.entrySet()){
            List<String> actions=grouped.get(e.getValue());
            if(actions==null){actions=new ArrayList<String>();grouped.put(e.getValue(),actions);}
            actions.add(e.getKey());
        }
        Map<Integer,List<String>> conflicts=new LinkedHashMap<Integer,List<String>>();
        for(Map.Entry<Integer,List<String>> e:grouped.entrySet()) if(e.getValue().size()>1)
            conflicts.put(e.getKey(),Collections.unmodifiableList(new ArrayList<String>(e.getValue())));
        return Collections.unmodifiableMap(conflicts);
    }
    public synchronized Map<String,Integer> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Integer>(assignments));}
    public synchronized void clear(){assignments.clear();}
}