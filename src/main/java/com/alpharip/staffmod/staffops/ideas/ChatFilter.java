package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Case-insensitive chat keyword matching; listener and visual highlighting need integration. */
public class ChatFilter {
 private final Set<String> words=new LinkedHashSet<String>();private boolean hideNonMatches;
 public void addKeyword(String s){words.add(req(s).toLowerCase(Locale.ROOT));}public void removeKeyword(String s){words.remove(req(s).toLowerCase(Locale.ROOT));}
 public boolean matches(String line){if(line==null)return false;String s=line.toLowerCase(Locale.ROOT);for(String w:words)if(s.contains(w))return true;return false;}
 public boolean shouldDisplay(String line){return !hideNonMatches||matches(line);}public void setHideNonMatches(boolean v){hideNonMatches=v;}
 public Set<String> getKeywords(){return Collections.unmodifiableSet(new LinkedHashSet<String>(words));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();return s.trim();}
}