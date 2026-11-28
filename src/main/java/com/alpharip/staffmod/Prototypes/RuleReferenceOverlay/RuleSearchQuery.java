package com.alpharip.staffmod.RuleReferenceOverlay;
public final class RuleSearchQuery {
 private final RuleCategory category;private final String text;
 public RuleSearchQuery(RuleCategory category,String text){this.category=category;this.text=text==null?"":text.trim().toLowerCase(java.util.Locale.ROOT);}
 public RuleCategory getCategory(){return category;}public String getText(){return text;}
}