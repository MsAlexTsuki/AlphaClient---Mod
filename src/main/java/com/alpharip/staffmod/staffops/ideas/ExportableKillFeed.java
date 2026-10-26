package com.alpharip.staffmod.staffops.ideas;

/** Foundation for collecting kill-feed entries for export. */
public class ExportableKillFeed {
 private final java.util.List<String> entries=new java.util.ArrayList<String>();
 public void record(String entry){if(entry!=null&&!entry.trim().isEmpty())entries.add(entry);}
 public java.util.List<String> getEntries(){return java.util.Collections.unmodifiableList(entries);}
 public String exportAsText(){StringBuilder out=new StringBuilder();for(String entry:entries){if(out.length()>0)out.append('\n');out.append(entry);}return out.toString();}
 public void clear(){entries.clear();}
}
