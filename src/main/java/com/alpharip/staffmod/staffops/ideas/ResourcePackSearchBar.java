package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Case-insensitive resource-pack filtering helper. Screen wiring remains to be added. */
public class ResourcePackSearchBar {
 private String query="";
 public void setQuery(String q){query=q==null?"":q.trim();} public String getQuery(){return query;}
 public <T> List<T> filter(Collection<T> packs, java.util.function.Function<T,String> name){List<T> out=new ArrayList<T>();if(packs==null)return out;String q=query.toLowerCase(Locale.ROOT);for(T p:packs){String n=name==null?String.valueOf(p):name.apply(p);if(n!=null&&n.toLowerCase(Locale.ROOT).contains(q))out.add(p);}return Collections.unmodifiableList(out);}
}