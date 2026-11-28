package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local chronological index of snapshot metadata; no image bytes are retained. */
public final class EvidenceSnapshotHistory {
 private final List<Entry> entries = new ArrayList<Entry>();
 public void add(String reference, long timestampMillis, String note) { entries.add(new Entry(reference, timestampMillis, note)); }
 public List<Entry> getEntries() { return Collections.unmodifiableList(new ArrayList<Entry>(entries)); }
 public static final class Entry {
  private final String reference; private final long timestampMillis; private final String note;
  private Entry(String reference, long timestampMillis, String note) { this.reference=reference==null?"":reference; this.timestampMillis=timestampMillis; this.note=note==null?"":note; }
  public String getReference(){return reference;} public long getTimestampMillis(){return timestampMillis;} public String getNote(){return note;}
 }
}