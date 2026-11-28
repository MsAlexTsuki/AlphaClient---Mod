package com.alpharip.staffmod.RuleReferenceOverlay;
import java.util.*;
public final class RuleReferenceResult {
 private final List<RuleMatch> matches;
 public RuleReferenceResult(List<RuleMatch> matches){this.matches=Collections.unmodifiableList(new ArrayList<RuleMatch>(matches==null?Collections.<RuleMatch>emptyList():matches));}
 public List<RuleMatch> getMatches(){return matches;}public boolean isEmpty(){return matches.isEmpty();}
}