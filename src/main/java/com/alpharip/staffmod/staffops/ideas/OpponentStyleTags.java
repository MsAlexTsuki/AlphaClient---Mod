package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** User-managed style tags associated with an opponent key; integrates with Opponent History separately. */
public final class OpponentStyleTags {
 public static final String NAME="Opponent Style Tags",CATEGORY="Practice",STATUS="ENH",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Set<String>> tags=new LinkedHashMap<String,Set<String>>();
 public synchronized void add(String opponent,String tag){String o=text(opponent),t=text(tag).toLowerCase(Locale.ROOT);tags.computeIfAbsent(o,k->new LinkedHashSet<String>()).add(t);}
 public synchronized void remove(String opponent,String tag){Set<String>s=tags.get(text(opponent));if(s!=null){s.remove(text(tag).toLowerCase(Locale.ROOT));if(s.isEmpty())tags.remove(text(opponent));}}
 public synchronized Set<String> get(String opponent){Set<String>s=tags.get(text(opponent));return Collections.unmodifiableSet(s==null?new LinkedHashSet<String>():new LinkedHashSet<String>(s));}
 public synchronized Map<String,Set<String>> snapshot(){Map<String,Set<String>> out=new LinkedHashMap<String,Set<String>>();for(Map.Entry<String,Set<String>>e:tags.entrySet())out.put(e.getKey(),Collections.unmodifiableSet(new LinkedHashSet<String>(e.getValue())));return Collections.unmodifiableMap(out);}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}