package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Metadata index for replay/video clips; launcher UI and filesystem scanning are not connected. */
public class LauncherClipLibrary {
 public static final class Clip {public final String path,title,server;public final long createdAt,durationMillis;Clip(String p,String t,String s,long c,long d){path=p;title=t;server=s;createdAt=c;durationMillis=d;}}
 private final LinkedHashMap<String,Clip> clips=new LinkedHashMap<String,Clip>();
 public void add(String path,String title,String server,long createdAt,long durationMillis){if(path==null||path.trim().isEmpty())throw new IllegalArgumentException("Path required");clips.put(path,new Clip(path,title==null?"":title,server==null?"":server,createdAt,Math.max(0,durationMillis)));}
 public boolean remove(String path){return clips.remove(path)!=null;}
 public List<Clip> search(String query){String q=query==null?"":query.toLowerCase(Locale.ROOT);List<Clip> out=new ArrayList<Clip>();for(Clip c:clips.values())if(c.title.toLowerCase(Locale.ROOT).contains(q)||c.server.toLowerCase(Locale.ROOT).contains(q)||c.path.toLowerCase(Locale.ROOT).contains(q))out.add(c);return Collections.unmodifiableList(out);}
}