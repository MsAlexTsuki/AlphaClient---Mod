package com.alpharip.staffmod.RuleReferenceOverlay;
public final class RuleMatch {
 private final RuleEntry rule;private final int score;
 public RuleMatch(RuleEntry rule,int score){if(rule==null)throw new IllegalArgumentException("rule");this.rule=rule;this.score=score;}
 public RuleEntry getRule(){return rule;}public int getScore(){return score;}
}