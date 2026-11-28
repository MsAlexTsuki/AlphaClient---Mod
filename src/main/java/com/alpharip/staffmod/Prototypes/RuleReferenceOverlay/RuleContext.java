package com.alpharip.staffmod.RuleReferenceOverlay;
public final class RuleContext {
 private final RuleCategory category;private final String reportText;
 public RuleContext(RuleCategory category,String reportText){if(category==null)throw new IllegalArgumentException("category");this.category=category;this.reportText=reportText==null?"":reportText;}
 public RuleCategory getCategory(){return category;}public String getReportText(){return reportText;}
}