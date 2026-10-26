package com.alpharip.staffmod.staffops.ideas;
import java.util.*;import java.util.regex.*;
/** Parses user-configured bounty chat formats into a local list; server-specific patterns must be configured. */
public final class BountyBoardParser {
 public static final String NAME="Bounty Board Parser",CATEGORY="KitMap",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Bounty {public final String player,amount,source;public final long detectedAt;private Bounty(String p,String a,String s){player=p;amount=a;source=s;detectedAt=System.currentTimeMillis();}}
 private final List<Pattern> patterns=new ArrayList<Pattern>();private final Map<String,Bounty> bounties=new LinkedHashMap<String,Bounty>();
 public synchronized void addPattern(String regex){patterns.add(Pattern.compile(regex,Pattern.CASE_INSENSITIVE));}
 /** Patterns must expose player as group 1 and bounty amount as group 2. */
 public synchronized Bounty parse(String chat){if(chat==null)return null;for(Pattern p:patterns){Matcher m=p.matcher(chat);if(m.find()&&m.groupCount()>=2){String player=m.group(1).trim(),amount=m.group(2).trim();if(player.isEmpty()||amount.isEmpty())continue;Bounty b=new Bounty(player,amount,chat);bounties.put(player.toLowerCase(Locale.ROOT),b);return b;}}return null;}
 public synchronized List<Bounty> snapshot(){return Collections.unmodifiableList(new ArrayList<Bounty>(bounties.values()));}
 public synchronized boolean remove(String player){return player!=null&&bounties.remove(player.toLowerCase(Locale.ROOT))!=null;}
}