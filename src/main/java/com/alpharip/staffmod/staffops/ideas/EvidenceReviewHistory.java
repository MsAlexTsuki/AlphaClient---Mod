package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local append-only review history for this object instance. */
public final class EvidenceReviewHistory {
 private final List<Entry> entries=new ArrayList<Entry>();
 public void record(String status,String actorId,String note,long timestampMillis){entries.add(new Entry(status,actorId,note,timestampMillis));}
 public List<Entry> getEntries(){return Collections.unmodifiableList(new ArrayList<Entry>(entries));}
 public static final class Entry {private final String status,actorId,note;private final long timestampMillis;private Entry(String status,String actorId,String note,long timestampMillis){this.status=status==null?"":status;this.actorId=actorId==null?"":actorId;this.note=note==null?"":note;this.timestampMillis=timestampMillis;}public String getStatus(){return status;}public String getActorId(){return actorId;}public String getNote(){return note;}public long getTimestampMillis(){return timestampMillis;}}
}