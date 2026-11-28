package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Associates a cosmetic sticker identifier with a bookmark identifier. */
public final class BookmarkStickers {
 public static final String FEATURE_NAME="Bookmark Stickers",CATEGORY="Donator Scout",STATUS="NEW";
 private final Map<String,String> stickers=new HashMap<String,String>();
 public void setSticker(String bookmarkId,String sticker){stickers.put(req(bookmarkId),req(sticker));}
 public void removeSticker(String bookmarkId){if(bookmarkId!=null)stickers.remove(bookmarkId);}
 public String getSticker(String bookmarkId){return stickers.get(bookmarkId);}
 public Map<String,String> all(){return Collections.unmodifiableMap(new HashMap<String,String>(stickers));}
 private String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}