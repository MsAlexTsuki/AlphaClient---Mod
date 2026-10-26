package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Adjusts difficulty using a rolling score window. Drill execution is supplied by the practice UI. */
public final class DrillDifficultyLadder {
 public static final String NAME="Drill Difficulty Ladder",CATEGORY="Practice",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Deque<Integer> scores=new ArrayDeque<Integer>();private final int window;private int level=1;
 public DrillDifficultyLadder(){this(5);}public DrillDifficultyLadder(int window){if(window<1)throw new IllegalArgumentException("window must be positive");this.window=window;}
 public synchronized int recordScore(int score){if(score<0||score>100)throw new IllegalArgumentException("score must be 0..100");scores.addLast(score);while(scores.size()>window)scores.removeFirst();if(scores.size()==window){double avg=average();if(avg>=85)level=Math.min(10,level+1);else if(avg<45)level=Math.max(1,level-1);}return level;}
 public synchronized int getLevel(){return level;}public synchronized double average(){if(scores.isEmpty())return 0;int sum=0;for(int s:scores)sum+=s;return (double)sum/scores.size();}
 public synchronized void reset(){scores.clear();level=1;}
}