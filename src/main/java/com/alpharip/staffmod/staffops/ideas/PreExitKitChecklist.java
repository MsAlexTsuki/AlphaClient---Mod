package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre-Exit Kit Checklist.
 * Purpose: Warn when you leave the spawn region with an incomplete kit.
 * Workflow: You define the spawn bounding box; crossing it with missing armor, low soups, or no pearls triggers a brief warning banner.
 * Dependencies: Client-side only (user-defined region; no hidden server data).
 * Technical direction: Per-tick position check against a stored AABB.
 *
 * Local Java 8 state foundation. Forge event/GUI integration remains a separate step.
 */
public final class PreExitKitChecklist {
    public static final String NAME="Pre-Exit Kit Checklist";
    public static final String CATEGORY="KitMap";
    public static final String STATUS="NEW";
    public static final String PURPOSE="Warn when you leave the spawn region with an incomplete kit.";
    public static final String WORKFLOW="You define the spawn bounding box; crossing it with missing armor, low soups, or no pearls triggers a brief warning banner.";
    public static final String REQUIRED_ACCESS="All users.";
    public static final String DEPENDENCIES="Client-side only (user-defined region; no hidden server data).";
    public static final String TECHNICAL_DIRECTION="Per-tick position check against a stored AABB.";
    public static final boolean REQUIRES_SUPABASE=false;
    public static final boolean REQUIRES_SERVER_SUPPORT=false;
    // TODO(Supabase): if shared data is later needed, document schema and access policies before integration.
    // TODO(Server): keep sensitive multiplayer actions server-authoritative.

    public static final class Item {
        private final String id;
        private String title;
        private String status="open";
        private long updatedAt=System.currentTimeMillis();
        private final Map<String,String> fields=new LinkedHashMap<String,String>();
        private Item(String id,String title){this.id=id;this.title=title;}
        public String getId(){return id;}
        public String getTitle(){return title;}
        public String getStatus(){return status;}
        public long getUpdatedAt(){return updatedAt;}
        public Map<String,String> getFields(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields));}
    }
    private final Map<String,Item> items=new LinkedHashMap<String,Item>();
    public synchronized Item add(String id,String title){String i=text(id,"id"),t=text(title,"title");if(items.containsKey(i))throw new IllegalArgumentException("Duplicate id: "+i);Item x=new Item(i,t);items.put(i,x);return x;}
    public synchronized boolean rename(String id,String title){Item x=items.get(text(id,"id"));if(x==null)return false;x.title=text(title,"title");x.updatedAt=System.currentTimeMillis();return true;}
    public synchronized boolean setStatus(String id,String status){Item x=items.get(text(id,"id"));if(x==null)return false;x.status=text(status,"status");x.updatedAt=System.currentTimeMillis();return true;}
    public synchronized boolean setField(String id,String key,String value){Item x=items.get(text(id,"id"));if(x==null)return false;x.fields.put(text(key,"key"),value==null?"":value);x.updatedAt=System.currentTimeMillis();return true;}
    public synchronized Item get(String id){return items.get(text(id,"id"));}
    public synchronized List<Item> getAll(){return Collections.unmodifiableList(new ArrayList<Item>(items.values()));}
    public synchronized boolean remove(String id){return items.remove(text(id,"id"))!=null;}
    public synchronized int size(){return items.size();}
    private static String text(String s,String n){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException(n+" must not be blank.");return s.trim();}
}
