package com.alpharip.staffmod.staffops.ideas;
/** Redacts common sensitive text patterns before local export; pattern matching is not exhaustive. */
public final class PrivacyRedactionGuard {
 public static final String NAME="Privacy Redaction Guard", CATEGORY="Security", STATUS="ENHANCEMENT";
 public static final boolean REQUIRES_SUPABASE=false, REQUIRES_SERVER_SUPPORT=false;
 public String redact(String input){if(input==null)return null;String v=input;
 v=v.replaceAll("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b","[REDACTED]");
 v=v.replaceAll("(?i)(token|password|secret)\\s*[:=]\\s*[^\\s,;]+","$1=[REDACTED]");
 v=v.replaceAll("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b","[REDACTED]");return v;}
}