package com.alpharip.staffmod.staffops.ideas;

/** Foundation for comparing two named configuration profiles. */
public class CompareTwoProfilesSideBySide {
 private String firstProfile="",secondProfile="";
 private final java.util.Map<String,String> first=new java.util.LinkedHashMap<String,String>(),second=new java.util.LinkedHashMap<String,String>();
 public void setProfileNames(String a,String b){firstProfile=a==null?"":a;secondProfile=b==null?"":b;}
 public String getFirstProfile(){return firstProfile;} public String getSecondProfile(){return secondProfile;}
 public void setFirstValue(String key,String value){if(key!=null)first.put(key,value);}
 public void setSecondValue(String key,String value){if(key!=null)second.put(key,value);}
 public boolean differs(String key){return !java.util.Objects.equals(first.get(key),second.get(key));}
 public java.util.Map<String,String> getFirstValues(){return java.util.Collections.unmodifiableMap(first);}
 public java.util.Map<String,String> getSecondValues(){return java.util.Collections.unmodifiableMap(second);}
}
