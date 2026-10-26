package com.alpharip.staffmod.staffops.ideas;

/** Foundation for per-opponent duel statistics. */
public class OpponentSpecificDuelStatistics {
 private String opponentName=""; private int wins; private int losses;
 public String getOpponentName(){return opponentName;} public void setOpponentName(String v){opponentName=v==null?"":v;} public void recordWin(){wins++;} public void recordLoss(){losses++;} public int getWins(){return wins;} public int getLosses(){return losses;}
}
