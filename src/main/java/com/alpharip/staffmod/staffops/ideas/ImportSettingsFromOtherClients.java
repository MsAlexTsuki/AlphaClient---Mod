package com.alpharip.staffmod.staffops.ideas;

/** Foundation for importing externally parsed client settings. */
public class ImportSettingsFromOtherClients {
 private String sourceClient=""; private int importedSettingsCount; private boolean validated;
 public String getSourceClient(){return sourceClient;} public void setSourceClient(String v){sourceClient=v==null?"":v;}
 public int getImportedSettingsCount(){return importedSettingsCount;}
 public void setImportedSettingsCount(int v){importedSettingsCount=Math.max(0,v);}
 public boolean isValidated(){return validated;} public void setValidated(boolean v){validated=v;}
}
