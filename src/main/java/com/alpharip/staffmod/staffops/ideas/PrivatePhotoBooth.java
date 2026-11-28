package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Stores screenshot-scene presets; camera, HUD hiding and capture hooks require client integration. */
public final class PrivatePhotoBooth {
 public static final String NAME="Private Photo Booth",CATEGORY="OwnerFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Scene {public final String name,worldHint;public final boolean hideHud,hideNames;private Scene(String n,String w,boolean h,boolean names){name=n;worldHint=w;hideHud=h;hideNames=names;}}
 private final Map<String,Scene> scenes=new LinkedHashMap<String,Scene>();
 public synchronized void save(String name,String worldHint,boolean hideHud,boolean hideNames){String n=req(name);scenes.put(n,new Scene(n,worldHint==null?"":worldHint.trim(),hideHud,hideNames));}
 public synchronized Scene get(String name){return scenes.get(req(name));}
 public synchronized List<Scene> scenes(){return Collections.unmodifiableList(new ArrayList<Scene>(scenes.values()));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("scene name required");return s.trim();}
}