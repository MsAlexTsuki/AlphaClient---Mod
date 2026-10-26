package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Trial Progress Scorecard. Purpose: Track competencies and sign-offs during trial. Workflow: Checklist of skills with mentor sign-off dates and notes. Dependencies: Supabase. Technical direction: Server stores sign-offs; client displays. ---
 * Local Java 8 state foundation; Forge event and GUI integration remain separate.
 */
public final class TrialProgressScorecard {
 public static final String NAME="Trial Progress Scorecard", CATEGORY="Trial Moderator", STATUS="NEW", PURPOSE="Track competencies and sign-offs during trial.", WORKFLOW="Checklist of skills with mentor sign-off dates and notes.", REQUIRED_ACCESS="Trial sees own; mentors and Management see assigned.", DEPENDENCIES="Supabase.", TECHNICAL_DIRECTION="Server stores sign-offs; client displays. ---";
 public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=false;
 // TODO(Supabase): add authenticated integration and appropriate RLS policies when configured by the owner.
 // TODO(Server): keep sensitive multiplayer actions server-authoritative.
 public static final class Item {
  private final String id; private String title,status="open"; private long updatedAt=System.currentTimeMillis(); private final Map<String,String> fields=new LinkedHashMap<String,String>();
  private Item(String id,String title){this.id=id;this.title=title;} public String getId(){return id;} public String getTitle(){return title;} public String getStatus(){return status;} public long getUpdatedAt(){return updatedAt;} public Map<String,String> getFields(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields));}
 }
 private final Map<String,Item> items=new LinkedHashMap<String,Item>();
 public synchronized Item add(String id,String title){String i=text(id,"id"),t=text(title,"title");if(items.containsKey(i))throw new IllegalArgumentException("Duplicate id: "+i);Item x=new Item(i,t);items.put(i,x);return x;}
 public synchronized boolean rename(String id,String title){Item x=items.get(text(id,"id"));if(x==null)return false;x.title=text(title,"title");x.updatedAt=System.currentTimeMillis();return true;}
 public synchronized boolean setStatus(String id,String status){Item x=items.get(text(id,"id"));if(x==null)return false;x.status=text(status,"status");x.updatedAt=System.currentTimeMillis();return true;}
 public synchronized boolean setField(String id,String key,String value){Item x=items.get(text(id,"id"));if(x==null)return false;x.fields.put(text(key,"key"),value==null?"":value);x.updatedAt=System.currentTimeMillis();return true;}
 public synchronized Item get(String id){return items.get(text(id,"id"));} public synchronized List<Item> getAll(){return Collections.unmodifiableList(new ArrayList<Item>(items.values()));} public synchronized boolean remove(String id){return items.remove(text(id,"id"))!=null;} public synchronized int size(){return items.size();}
 private static String text(String s,String n){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException(n+" must not be blank.");return s.trim();}
}
