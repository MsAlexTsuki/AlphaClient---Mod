package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Tracks the ordered set of social badges chosen for display. */
public final class SocialBadgeShowcase {
 public static final String FEATURE_NAME="Social Badge Showcase",CATEGORY="Donator Alpha",STATUS="NEW";
 private final LinkedHashSet<String> visible=new LinkedHashSet<String>();
 public void show(String badge){visible.add(req(badge));} public void hide(String badge){if(badge!=null)visible.remove(badge.trim());}
 public List<String> getVisible(){return Collections.unmodifiableList(new ArrayList<String>(visible));}
 private String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("badge required");return s.trim();}
}