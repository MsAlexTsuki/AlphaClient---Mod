package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Computes case age and threshold highlighting; caller supplies case timestamps. */
public final class CaseAgeHighlighter {
 public static final String NAME="Case Age Highlighter",CATEGORY="StaffMods",STATUS="ENH",REQUIRED_ACCESS="Unconfirmed";
 public enum AgeBand { NORMAL, AGING, OVERDUE }
 public AgeBand classify(long createdAt,long now,long agingAfter,long overdueAfter){if(createdAt<0||now<createdAt||agingAfter<0||overdueAfter<agingAfter)throw new IllegalArgumentException("invalid age thresholds");long age=now-createdAt;if(age>=overdueAfter)return AgeBand.OVERDUE;if(age>=agingAfter)return AgeBand.AGING;return AgeBand.NORMAL;}
}