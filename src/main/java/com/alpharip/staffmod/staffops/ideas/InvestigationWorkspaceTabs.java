package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local workspace tab registry and selected-tab state. */
public final class InvestigationWorkspaceTabs {
 private final List<String> tabs=new ArrayList<String>();
 private String selected;
 public void addTab(String id){if(id==null||id.trim().isEmpty())throw new IllegalArgumentException("Tab id must not be blank");if(!tabs.contains(id))tabs.add(id);if(selected==null)selected=id;}
 public boolean select(String id){if(!tabs.contains(id))return false;selected=id;return true;}
 public String getSelected(){return selected;}
 public List<String> getTabs(){return Collections.unmodifiableList(new ArrayList<String>(tabs));}
 public boolean removeTab(String id){boolean removed=tabs.remove(id);if(removed&&id!=null&&id.equals(selected))selected=tabs.isEmpty()?null:tabs.get(0);return removed;}
}