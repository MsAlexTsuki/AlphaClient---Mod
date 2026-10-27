package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores timestamped connection observations for later review. Network sampling/event hooks are not wired. */
public final class ConnectionDiagnosticsTimeline {
    public static final String NAME = "Connection Diagnostics Timeline";
    public static final String CATEGORY = "Diagnostics";
    public enum Type { PING, TIMEOUT, DISCONNECT, RECONNECT, PACKET_GAP, NOTE }
    public static final class Event {
        private final long timestampMillis;
        private final Type type;
        private final int pingMillis;
        private final String details;
        private Event(long t,Type y,int p,String d){timestampMillis=t;type=y;pingMillis=p;details=d;}
        public long getTimestampMillis(){return timestampMillis;}
        public Type getType(){return type;}
        public int getPingMillis(){return pingMillis;}
        public String getDetails(){return details;}
    }
    private final List<Event> events=new ArrayList<Event>();
    private int capacity=1000;
    public synchronized void setCapacity(int n){if(n<1||n>20000)throw new IllegalArgumentException("Capacity must be 1-20000");capacity=n;trim();}
    public synchronized void record(long timestampMillis,Type type,int pingMillis,String details){
        if(type==null)throw new IllegalArgumentException("Type is required");
        events.add(new Event(Math.max(0,timestampMillis),type,pingMillis<0?-1:Math.min(pingMillis,600000),details==null?"":details.trim()));trim();
    }
    private void trim(){while(events.size()>capacity)events.remove(0);}
    public synchronized List<Event> snapshot(){return Collections.unmodifiableList(new ArrayList<Event>(events));}
    public synchronized long count(Type type){long n=0;for(Event e:events)if(e.getType()==type)n++;return n;}
    public synchronized void clear(){events.clear();}
}