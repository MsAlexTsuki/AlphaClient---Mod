package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory dependency links between evidence items; no dependency validation is implied. */
public final class EvidenceDependencyGraph {
 private final List<Dependency> dependencies=new ArrayList<Dependency>();
 public void add(String item,String dependsOn){if(blank(item)||blank(dependsOn))throw new IllegalArgumentException("Both item references are required");dependencies.add(new Dependency(item,dependsOn));}
 public List<Dependency> getDependencies(){return Collections.unmodifiableList(new ArrayList<Dependency>(dependencies));}
 private static boolean blank(String s){return s==null||s.trim().isEmpty();}
 public static final class Dependency {private final String item,dependsOn;private Dependency(String item,String dependsOn){this.item=item;this.dependsOn=dependsOn;}public String getItem(){return item;}public String getDependsOn(){return dependsOn;}}
}