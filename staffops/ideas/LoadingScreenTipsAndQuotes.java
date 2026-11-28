package com.alpharip.staffmod.staffops.ideas;

/** Foundation for rotating loading-screen tips and quotes. */
public class LoadingScreenTipsAndQuotes {
 private final java.util.List<String> entries=new java.util.ArrayList<String>(); private int lastIndex=-1;
 public void add(String entry){if(entry!=null&&!entry.trim().isEmpty())entries.add(entry);}
 public String next(){if(entries.isEmpty())return null;lastIndex=(lastIndex+1)%entries.size();return entries.get(lastIndex);}
 public java.util.List<String> getEntries(){return java.util.Collections.unmodifiableList(entries);}
}
