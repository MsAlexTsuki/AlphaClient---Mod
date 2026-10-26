package com.alpharip.staffmod.staffops.ideas;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
/** Local response-time metrics foundation; shared aggregation is not implemented. */
public final class ResponseTimeMeter {
 public static final String NAME="Response Time Meter", CATEGORY="Senior Staff", STATUS="NEW";
 public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=false;
 // TODO(Supabase): add approved shared response records when the owner configures Supabase.
 private final Map<String,Long> samples=new LinkedHashMap<String,Long>();
 public synchronized void record(String caseId,long millis){if(caseId==null||caseId.trim().isEmpty())throw new IllegalArgumentException("caseId");if(millis<0)throw new IllegalArgumentException("millis");samples.put(caseId.trim(),millis);}
 public synchronized Map<String,Long> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Long>(samples));}
 public synchronized double averageMillis(){if(samples.isEmpty())return 0D;long total=0;for(Long n:samples.values())total+=n;return (double)total/samples.size();}
}