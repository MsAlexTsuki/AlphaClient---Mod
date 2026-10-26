package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Configurable command syntax reference. It does not execute moderation commands. */
public final class CommandSyntaxHelper {
 public static final String NAME="Command Syntax Helper",CATEGORY="TrialModerator",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,String> syntax=new LinkedHashMap<String,String>();
 public synchronized void put(String command,String usage){syntax.put(req(command).toLowerCase(Locale.ROOT),req(usage));}
 public synchronized String lookup(String command){return syntax.get(req(command).toLowerCase(Locale.ROOT));}
 public synchronized Map<String,String> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(syntax));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}