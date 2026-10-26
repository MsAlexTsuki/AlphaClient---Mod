package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Stores named quick replies; buttons and chat sending are not connected. */
public class FaqReplyButtons {
 private boolean enabled=true;private final LinkedHashMap<String,String> replies=new LinkedHashMap<String,String>();public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public void put(String label,String reply){if(label==null||label.trim().isEmpty()||reply==null||reply.trim().isEmpty())throw new IllegalArgumentException("Label and reply required");replies.put(label.trim(),reply.trim());}
 public String getReply(String label){return replies.get(label);}public boolean remove(String label){return replies.remove(label)!=null;}public Map<String,String> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(replies));}
}