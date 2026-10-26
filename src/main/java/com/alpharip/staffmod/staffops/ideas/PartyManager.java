package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local party state model. Invites, networking and party chat are not connected. */
public class PartyManager {
 private String leader="";private final LinkedHashSet<String> members=new LinkedHashSet<String>();
 public boolean create(String leaderName){if(leaderName==null||leaderName.trim().isEmpty())return false;members.clear();leader=leaderName.trim();members.add(leader);return true;}
 public boolean addMember(String name){if(leader.isEmpty()||name==null||name.trim().isEmpty())return false;return members.add(name.trim());}
 public boolean removeMember(String name){if(name==null||name.trim().equalsIgnoreCase(leader))return false;return members.remove(name.trim());}
 public String getLeader(){return leader;}public boolean isLeader(String name){return name!=null&&!leader.isEmpty()&&leader.equalsIgnoreCase(name.trim());}
 public List<String> getMembers(){return Collections.unmodifiableList(new ArrayList<String>(members));}public boolean disband(){if(leader.isEmpty())return false;leader="";members.clear();return true;}
}