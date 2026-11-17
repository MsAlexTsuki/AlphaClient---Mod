package com.alpharip.staffmod.AlphaCrashReporter;
import java.util.regex.*;
/** Best-effort redaction, not a guarantee that every possible secret is removed. */
public final class CrashPrivacyFilter {
 private static final Pattern[] PATTERNS={
  Pattern.compile("(?i)(access[_-]?token|refresh[_-]?token|client[_-]?secret|password|authorization)(\\s*[=:]\\s*)([^\\s,;]+)"),
  Pattern.compile("(?i)(api[_-]?key)(\\s*[=:]\\s*)([^\\s,;]+)"),
  Pattern.compile("(?i)(bearer)(\\s+[A-Za-z0-9._~+/=-]+)")
 };
 public String redact(String text){
  if(text==null)return "";String out=text;
  out=PATTERNS[0].matcher(out).replaceAll("$1$2[REDACTED]");
  out=PATTERNS[1].matcher(out).replaceAll("$1$2[REDACTED]");
  out=PATTERNS[2].matcher(out).replaceAll("$1 [REDACTED]");
  String home=System.getProperty("user.home");if(home!=null&&!home.isEmpty())out=out.replace(home,"<USER_HOME>");
  return out;
 }
}
