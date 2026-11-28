package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Task list state for a future HUD widget. */
public class HudTodoList {public static final class Task {public final long id;public final String text;public final boolean done;private Task(long i,String t,boolean d){id=i;text=t;done=d;}}
 private final List<Task> tasks=new ArrayList<Task>();private long nextId=1;
 public long add(String text){if(text==null||text.trim().isEmpty())throw new IllegalArgumentException();long id=nextId++;tasks.add(new Task(id,text.trim(),false));return id;}
 public boolean setDone(long id,boolean done){for(int i=0;i<tasks.size();i++)if(tasks.get(i).id==id){Task t=tasks.get(i);tasks.set(i,new Task(t.id,t.text,done));return true;}return false;}
 public boolean remove(long id){for(Iterator<Task> it=tasks.iterator();it.hasNext();)if(it.next().id==id){it.remove();return true;}return false;}
 public List<Task> getTasks(){return Collections.unmodifiableList(new ArrayList<Task>(tasks));}
}