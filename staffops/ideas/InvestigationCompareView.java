package com.alpharip.staffmod.staffops.ideas;

/** Holds two item references and comparison labels for a future UI. */
public final class InvestigationCompareView {
 private String leftReference="",rightReference="",leftLabel="",rightLabel="";
 public void setComparison(String leftReference,String rightReference,String leftLabel,String rightLabel){this.leftReference=clean(leftReference);this.rightReference=clean(rightReference);this.leftLabel=clean(leftLabel);this.rightLabel=clean(rightLabel);}
 private static String clean(String value){return value==null?"":value;}
 public String getLeftReference(){return leftReference;} public String getRightReference(){return rightReference;} public String getLeftLabel(){return leftLabel;} public String getRightLabel(){return rightLabel;}
 public boolean isReady(){return !leftReference.isEmpty()&&!rightReference.isEmpty();}
}