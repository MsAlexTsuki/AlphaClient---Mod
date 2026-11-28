package com.alpharip.staffmod.staffops.ideas;

/** Foundation for state used by a loading-screen Snake minigame. */
public class LoadingScreenSnakeMinigame {
 private boolean enabled; private int score; private boolean gameOver;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getScore(){return score;} public void addScore(int points){if(!gameOver&&points>0)score+=points;}
 public boolean isGameOver(){return gameOver;} public void endGame(){gameOver=true;}
 public void reset(){score=0;gameOver=false;}
}
