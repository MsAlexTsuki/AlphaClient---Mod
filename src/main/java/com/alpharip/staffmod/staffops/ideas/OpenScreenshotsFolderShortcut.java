package com.alpharip.staffmod.staffops.ideas;
import java.io.File;
/** Resolves the screenshot folder. OS folder opening and key binding remain to be connected. */
public class OpenScreenshotsFolderShortcut {
 private boolean enabled=true;private String customPath="";
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public void setCustomPath(String p){customPath=p==null?"":p.trim();}
 public File resolve(File gameDirectory){if(gameDirectory==null)throw new IllegalArgumentException("Game directory required");return (customPath.isEmpty()?new File(gameDirectory,"screenshots"):new File(customPath)).getAbsoluteFile();}
}