package com.alpharip.staffmod.staffops.ideas;

/** Foundation for recording setting changes and undoing the latest change. */
public class SettingsHistoryWithUndo {
 public static final class Change { public final String key,oldValue,newValue; public Change(String k,String o,String n){key=k;oldValue=o;newValue=n;} }
 private final java.util.Deque<Change> history=new java.util.ArrayDeque<Change>();
 public void record(String key,String oldValue,String newValue){if(key!=null)history.push(new Change(key,oldValue,newValue));}
 public Change undo(){return history.isEmpty()?null:history.pop();}
 public int size(){return history.size();}
}
