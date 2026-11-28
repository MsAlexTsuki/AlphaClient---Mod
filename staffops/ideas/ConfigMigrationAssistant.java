package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Maps old config keys to new keys and previews a migration without writing files. */
public final class ConfigMigrationAssistant {
    public static final String NAME = "Config Migration Assistant";
    public static final String CATEGORY = "Configuration";
    public static final class Result {
        private final Map<String,String> migrated;
        private final List<String> unmappedKeys;
        private Result(Map<String,String> m,List<String> u){migrated=Collections.unmodifiableMap(new LinkedHashMap<String,String>(m));unmappedKeys=Collections.unmodifiableList(new ArrayList<String>(u));}
        public Map<String,String> getMigrated(){return migrated;}
        public List<String> getUnmappedKeys(){return unmappedKeys;}
    }
    private final Map<String,String> keyMappings=new LinkedHashMap<String,String>();
    public synchronized void mapKey(String oldKey,String newKey){
        if(blank(oldKey)||blank(newKey))throw new IllegalArgumentException("Both keys are required");
        keyMappings.put(oldKey.trim(),newKey.trim());
    }
    public synchronized boolean removeMapping(String oldKey){return oldKey!=null&&keyMappings.remove(oldKey.trim())!=null;}
    public synchronized Result preview(Map<String,String> oldConfig){
        Map<String,String> source=oldConfig==null?Collections.<String,String>emptyMap():oldConfig;
        Map<String,String> migrated=new LinkedHashMap<String,String>();List<String> unmapped=new ArrayList<String>();
        for(Map.Entry<String,String> e:source.entrySet()){
            String target=keyMappings.get(e.getKey());
            if(target==null)unmapped.add(e.getKey());else migrated.put(target,e.getValue());
        }
        return new Result(migrated,unmapped);
    }
    public synchronized Map<String,String> mappings(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(keyMappings));}
    private static boolean blank(String s){return s==null||s.trim().isEmpty();}
}