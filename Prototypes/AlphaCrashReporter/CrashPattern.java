package com.alpharip.staffmod.AlphaCrashReporter;
public final class CrashPattern {
 private final String fingerprint,title;private final int count;private final long first,last;
 public CrashPattern(String f,int c,long first,long last,String title){fingerprint=f;count=c;this.first=first;this.last=last;this.title=title;}
 public String getFingerprint(){return fingerprint;}public int getOccurrences(){return count;}public long getFirstSeen(){return first;}
 public long getLastSeen(){return last;}public String getExampleTitle(){return title;}
}
