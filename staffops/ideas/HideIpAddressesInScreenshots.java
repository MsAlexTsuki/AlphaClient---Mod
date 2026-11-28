package com.alpharip.staffmod.staffops.ideas;

/** Foundation for screenshot privacy settings that can mask known IP-address strings. */
public class HideIpAddressesInScreenshots {
 private boolean enabled=true; private String replacement="[hidden]";
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public String getReplacement(){return replacement;} public void setReplacement(String v){replacement=v==null?"[hidden]":v;}
 public String maskKnownAddress(String text,String address){if(!enabled||text==null||address==null||address.isEmpty())return text;return text.replace(address,replacement);}
}
