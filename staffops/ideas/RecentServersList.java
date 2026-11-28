package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Bounded MRU server history; recording connection events and persistence are not connected. */
public class RecentServersList {
 private final LinkedList<String> addresses=new LinkedList<String>();private int limit=10;
 public void setLimit(int n){if(n<1||n>100)throw new IllegalArgumentException("Limit must be 1-100");limit=n;trim();}
 public void record(String address){if(address==null||address.trim().isEmpty())return;String a=address.trim();addresses.remove(a);addresses.addFirst(a);trim();}
 private void trim(){while(addresses.size()>limit)addresses.removeLast();}
 public List<String> getRecent(){return Collections.unmodifiableList(new ArrayList<String>(addresses));}public void clear(){addresses.clear();}
}