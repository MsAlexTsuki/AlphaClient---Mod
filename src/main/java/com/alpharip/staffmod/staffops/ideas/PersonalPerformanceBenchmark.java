package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Compares manually supplied performance samples for named client configurations; no system sampling is wired. */
public final class PersonalPerformanceBenchmark {
    public static final String NAME = "Personal Performance Benchmark";
    public static final String CATEGORY = "Performance";
    public static final class Result {
        public final String profile;
        public final long capturedAtMillis;
        public final double averageFps, averageFrameTimeMillis, memoryUsedMiB;
        private Result(String p,long t,double f,double ft,double m){profile=p;capturedAtMillis=t;averageFps=f;averageFrameTimeMillis=ft;memoryUsedMiB=m;}
    }
    private final Map<String,List<Result>> results=new LinkedHashMap<String,List<Result>>();
    public synchronized Result record(String profile,long timestamp,double fps,double frameTime,double memoryMiB){
        if(profile==null||profile.trim().isEmpty())throw new IllegalArgumentException("Profile is required");
        if(!valid(fps)||!valid(frameTime)||!valid(memoryMiB)||fps<0||frameTime<0||memoryMiB<0)throw new IllegalArgumentException("Measurements must be finite and non-negative");
        String key=profile.trim();List<Result> list=results.get(key);if(list==null){list=new ArrayList<Result>();results.put(key,list);}
        Result r=new Result(key,Math.max(0,timestamp),fps,frameTime,memoryMiB);list.add(r);return r;
    }
    private static boolean valid(double n){return !Double.isNaN(n)&&!Double.isInfinite(n);}
    public synchronized List<Result> get(String profile){List<Result> l=results.get(profile);return l==null?Collections.<Result>emptyList():Collections.unmodifiableList(new ArrayList<Result>(l));}
    public synchronized Map<String,List<Result>> snapshot(){Map<String,List<Result>> copy=new LinkedHashMap<String,List<Result>>();for(Map.Entry<String,List<Result>>e:results.entrySet())copy.put(e.getKey(),Collections.unmodifiableList(new ArrayList<Result>(e.getValue())));return Collections.unmodifiableMap(copy);}
    public synchronized void clear(String profile){results.remove(profile);}
}