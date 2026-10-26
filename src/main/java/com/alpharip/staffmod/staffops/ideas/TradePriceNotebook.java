package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local personal price notes; persistence and GUI are integration work. */
public final class TradePriceNotebook {
 public static final String NAME="Trade Price Notebook",CATEGORY="Economy",STATUS="ENH",REQUIRED_ACCESS="Unconfirmed";
 public static final class Price {public final String item,currency,note;public final double amount;public final long updatedAt;private Price(String i,double a,String c,String n){item=i;amount=a;currency=c;note=n;updatedAt=System.currentTimeMillis();}}
 private final Map<String,Price> prices=new LinkedHashMap<String,Price>();
 public synchronized void put(String item,double amount,String currency,String note){String k=text(item);if(amount<0||Double.isNaN(amount)||Double.isInfinite(amount))throw new IllegalArgumentException("invalid amount");String c=text(currency);prices.put(k.toLowerCase(Locale.ROOT),new Price(k,amount,c,note==null?"":note));}
 public synchronized Price get(String item){return prices.get(text(item).toLowerCase(Locale.ROOT));}
 public synchronized boolean remove(String item){return prices.remove(text(item).toLowerCase(Locale.ROOT))!=null;}
 public synchronized List<Price> snapshot(){return Collections.unmodifiableList(new ArrayList<Price>(prices.values()));}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value must not be blank");return s.trim();}
}