package com.alpharip.staffmod.AlphaCrashReporter;
public final class SuggestedFix {
 private final String id,title,description;private final CrashCategory category;private final int confidence;
 public SuggestedFix(String id,String title,String description,CrashCategory category,int confidence){this.id=id;this.title=title;this.description=description;this.category=category;this.confidence=Math.max(0,Math.min(100,confidence));}
 public String getId(){return id;}public String getTitle(){return title;}public String getDescription(){return description;}
 public CrashCategory getCategory(){return category;}public int getConfidence(){return confidence;}
}
