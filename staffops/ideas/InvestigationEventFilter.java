package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Filters supplied events by optional type and inclusive time bounds. */
public final class InvestigationEventFilter {
 public <T extends EventData> List<T> filter(List<T> events,String type,long fromMillis,long toMillis){List<T> result=new ArrayList<T>();if(events==null)return result;for(T event:events){if(event==null)continue;if(type!=null&&!type.trim().isEmpty()&&!type.equalsIgnoreCase(event.getType()))continue;if(event.getTimestampMillis()<fromMillis||event.getTimestampMillis()>toMillis)continue;result.add(event);}return Collections.unmodifiableList(result);}
 public interface EventData {String getType();long getTimestampMillis();}
}