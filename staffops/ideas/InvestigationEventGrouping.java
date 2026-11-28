package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Groups caller-supplied events by their type label, preserving encounter order. */
public final class InvestigationEventGrouping {
 public <T extends TypedEvent> Map<String,List<T>> groupByType(Iterable<T> events){Map<String,List<T>> groups=new LinkedHashMap<String,List<T>>();if(events!=null)for(T event:events){if(event==null)continue;String key=event.getType()==null||event.getType().trim().isEmpty()?"Unknown":event.getType().trim();if(!groups.containsKey(key))groups.put(key,new ArrayList<T>());groups.get(key).add(event);}return groups;}
 public interface TypedEvent {String getType();}
}