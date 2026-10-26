package com.alpharip.staffmod.staffops.ideas;

/** Foundation for a combined health, hunger and experience HUD display. */
public class CombinedHealthHungerAndXpBar {
 private boolean enabled=true; private float health,maxHealth=20f; private int hunger=20; private float xpProgress; private int xpLevel;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void update(float health,float maxHealth,int hunger,float xpProgress,int xpLevel){this.maxHealth=Math.max(1f,maxHealth);this.health=Math.max(0f,Math.min(this.maxHealth,health));this.hunger=Math.max(0,Math.min(20,hunger));this.xpProgress=Math.max(0f,Math.min(1f,xpProgress));this.xpLevel=Math.max(0,xpLevel);}
 public float getHealth(){return health;} public float getMaxHealth(){return maxHealth;} public int getHunger(){return hunger;} public float getXpProgress(){return xpProgress;} public int getXpLevel(){return xpLevel;}
}
