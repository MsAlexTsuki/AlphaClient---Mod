package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local group-chat metadata and message history. Sending/receiving messages is not connected. */
public class FriendGroupChat {
 private String name;private final LinkedHashSet<String> members=new LinkedHashSet<String>();private final List<String> messages=new ArrayList<String>();
 public FriendGroupChat(String name){if(name==null||name.trim().isEmpty())throw new IllegalArgumentException("Group name required");this.name=name.trim();}
 public String getName(){return name;}public void rename(String n){if(n==null||n.trim().isEmpty())throw new IllegalArgumentException("Group name required");name=n.trim();}
 public boolean addMember(String n){return n!=null&&!n.trim().isEmpty()&&members.add(n.trim());}public boolean removeMember(String n){return n!=null&&members.remove(n.trim());}
 public Set<String> getMembers(){return Collections.unmodifiableSet(new LinkedHashSet<String>(members));}
 public void recordMessage(String sender,String text){if(sender==null||sender.trim().isEmpty()||text==null)return;messages.add(sender.trim()+": "+text);if(messages.size()>500)messages.remove(0);}
 public List<String> getMessages(){return Collections.unmodifiableList(new ArrayList<String>(messages));}public void clearHistory(){messages.clear();}
}