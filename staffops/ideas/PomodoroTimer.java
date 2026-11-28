package com.alpharip.staffmod.staffops.ideas;
/** Monotonic-clock Pomodoro timer model; notifications and display need integration. */
public class PomodoroTimer {public enum Phase{WORK,SHORT_BREAK,LONG_BREAK}
 private Phase phase=Phase.WORK;private long duration=1500000L,start=-1;private int completed;
 public void configure(Phase p,long millis){if(p==null||millis<1)throw new IllegalArgumentException();phase=p;duration=millis;start=-1;}
 public void start(long now){if(now<0)throw new IllegalArgumentException();start=now;}
 public long remaining(long now){return start<0?duration:Math.max(0,duration-(now-start));}
 public boolean isFinished(long now){return start>=0&&remaining(now)==0;}
 public Phase finish(long now){if(!isFinished(now))throw new IllegalStateException("Timer is not finished");if(phase==Phase.WORK)completed++;start=-1;return phase;}
 public int getCompletedWorkSessions(){return completed;}public Phase getPhase(){return phase;}
}