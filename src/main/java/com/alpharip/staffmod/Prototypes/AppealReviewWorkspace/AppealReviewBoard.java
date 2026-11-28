package com.alpharip.staffmod.AppealReviewWorkspace;
import java.util.*;
public final class AppealReviewBoard {
 private final Map<String,AppealCase> cases=new LinkedHashMap<String,AppealCase>();
 private final Map<String,List<Object>> related=new LinkedHashMap<String,List<Object>>();
 public synchronized void add(AppealCase a){if(a==null||cases.containsKey(a.getId()))throw new IllegalArgumentException("Appeal missing or duplicate");cases.put(a.getId(),a);}
 public synchronized AppealCase get(String id){return cases.get(id);}
 public synchronized boolean remove(String id){related.remove(id);return cases.remove(id)!=null;}
 public synchronized List<AppealCase> getAll(){return Collections.unmodifiableList(new ArrayList<AppealCase>(cases.values()));}
 public synchronized void addRelated(String id,Object item){if(!cases.containsKey(id)||item==null)throw new IllegalArgumentException("Appeal and record required");List<Object> list=related.get(id);if(list==null){list=new ArrayList<Object>();related.put(id,list);}list.add(item);}
 public synchronized List<Object> getRelated(String id){List<Object> list=related.get(id);return Collections.unmodifiableList(list==null?new ArrayList<Object>():new ArrayList<Object>(list));}
 public synchronized void replaceCases(List<AppealCase> values){cases.clear();related.clear();if(values!=null)for(AppealCase a:values)if(a!=null&&!cases.containsKey(a.getId()))cases.put(a.getId(),a);}
}