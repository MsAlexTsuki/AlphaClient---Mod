package com.alpharip.staffmod.staffops.ideas;

/** Presentation model for key/value metadata displayed beside evidence. */
public final class EvidenceMetadataPanel {
 private final java.util.Map<String,String> values=new java.util.LinkedHashMap<String,String>();
 public void put(String key,String value){if(key==null||key.trim().isEmpty())throw new IllegalArgumentException("Metadata key must not be blank");values.put(key.trim(),value==null?"":value);}
 public String get(String key){return values.get(key);}
 public java.util.Map<String,String> getValues(){return java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<String,String>(values));}
 public void clear(){values.clear();}
}