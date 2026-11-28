package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Bounded chat transcript and case-insensitive search; chat listener integration is separate. */
public class ChatHistorySearch {
 private final Deque<String> lines=new ArrayDeque<String>();private int capacity=5000;
 public void setCapacity(int n){if(n<1)throw new IllegalArgumentException();capacity=n;trim();}
 public void add(String line){if(line!=null){lines.addLast(line);trim();}}
 public List<String> search(String q){if(q==null||q.trim().isEmpty())return Collections.emptyList();String s=q.toLowerCase(Locale.ROOT);List<String> out=new ArrayList<String>();for(String l:lines)if(l.toLowerCase(Locale.ROOT).contains(s))out.add(l);return Collections.unmodifiableList(out);}
 public List<String> getAll(){return Collections.unmodifiableList(new ArrayList<String>(lines));}
 private void trim(){while(lines.size()>capacity)lines.removeFirst();}
}