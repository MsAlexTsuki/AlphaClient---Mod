package com.alpharip.staffmod.staffops.ideas;

/** Stores two cosmetic effect IDs and their blend percentage; no effects are rendered. */
public final class CosmeticMixer {
 public static final String FEATURE_NAME="Cosmetic Mixer",CATEGORY="Donator Alpha+",STATUS="NEW";
 private String first,second; private int blendPercent=50;
 public void setEffects(String a,String b){first=required(a);second=required(b);}
 public String getFirst(){return first;} public String getSecond(){return second;}
 public int getBlendPercent(){return blendPercent;} public void setBlendPercent(int v){if(v<0||v>100)throw new IllegalArgumentException("blend must be 0-100");blendPercent=v;}
 private String required(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("effect required");return s.trim();}
}