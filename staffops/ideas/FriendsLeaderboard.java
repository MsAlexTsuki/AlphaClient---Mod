package com.alpharip.staffmod.staffops.ideas;

/** Foundation for a friends-only leaderboard. TODO(Supabase): fetch friend relationships and privacy-approved stats. */
public class FriendsLeaderboard {
 public static class Entry { public final String playerId; public final long score; public Entry(String id,long s){playerId=id;score=Math.max(0L,s);} }
 private final java.util.List<Entry> entries=new java.util.ArrayList<Entry>();
 public void addEntry(Entry e){if(e!=null&&e.playerId!=null)entries.add(e);}
 public java.util.List<Entry> getEntries(){java.util.List<Entry> out=new java.util.ArrayList<Entry>(entries);java.util.Collections.sort(out,new java.util.Comparator<Entry>(){public int compare(Entry a,Entry b){return Long.compare(b.score,a.score);}});return java.util.Collections.unmodifiableList(out);}
}
