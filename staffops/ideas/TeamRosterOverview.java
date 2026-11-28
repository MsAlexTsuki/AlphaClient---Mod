package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Team-oriented roster maintained from entries supplied by existing staff systems. */
public final class TeamRosterOverview {
 public static final String NAME="Team Roster Overview",CATEGORY="Management",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Member {public final String id,name,team,role;private Member(String i,String n,String t,String r){id=i;name=n;team=t;role=r;}}
 private final Map<String,Member> members=new LinkedHashMap<String,Member>();
 public synchronized void put(String id,String name,String team,String role){String i=req(id);members.put(i,new Member(i,req(name),req(team),req(role)));}
 public synchronized List<Member> byTeam(String team){List<Member> out=new ArrayList<Member>();for(Member m:members.values())if(m.team.equalsIgnoreCase(req(team)))out.add(m);return Collections.unmodifiableList(out);}
 public synchronized List<Member> snapshot(){return Collections.unmodifiableList(new ArrayList<Member>(members.values()));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}