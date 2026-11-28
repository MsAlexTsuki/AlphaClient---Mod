package com.alpharip.staffmod.staffops.ideas;

/** Foundation for CPS test results and history. */
public class CPSTestWithHistory {
 public static class Result { public final long durationMillis; public final int clicks; public Result(long d,int c){durationMillis=Math.max(1L,d);clicks=Math.max(0,c);} public double getCps(){return clicks*1000.0/durationMillis;} }
 private final java.util.List<Result> history=new java.util.ArrayList<Result>();
 public void addResult(Result r){if(r!=null)history.add(r);} public java.util.List<Result> getHistory(){return java.util.Collections.unmodifiableList(new java.util.ArrayList<Result>(history));}
}
