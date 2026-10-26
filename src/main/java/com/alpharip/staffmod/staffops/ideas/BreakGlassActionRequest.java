package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Creates a local request for emergency access review; it never grants emergency access itself. */
public final class BreakGlassActionRequest {
 public static final String NAME="Break-Glass Action Request", CATEGORY="Management", STATUS="NEW";
 public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=true;
 // TODO(Supabase): the owner will add shared request storage and review workflow later.
 // TODO(Server): emergency access must be approved and enforced by a trusted server/plugin.
 public static final class Request {
  public final String id, reason; public final long createdAt;
  private Request(String id,String reason){this.id=id;this.reason=reason;this.createdAt=System.currentTimeMillis();}
 }
 private final Map<String,Request> requests=new LinkedHashMap<String,Request>();
 public synchronized Request create(String id,String reason){if(blank(id)||blank(reason))throw new IllegalArgumentException("id and reason required");if(requests.containsKey(id.trim()))throw new IllegalArgumentException("duplicate request id");Request r=new Request(id.trim(),reason.trim());requests.put(r.id,r);return r;}
 public synchronized List<Request> snapshot(){return Collections.unmodifiableList(new ArrayList<Request>(requests.values()));}
 private static boolean blank(String s){return s==null||s.trim().isEmpty();}
}