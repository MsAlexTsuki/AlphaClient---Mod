package com.alpharip.staffmod.RuleReferenceOverlay;
import java.util.*;
public final class RuleEntry {
 private final String id,title,description,source; private final RuleCategory category; private final RuleSeverity severity; private final Set<String> tags;
 public RuleEntry(String id,String title,String description,String source,RuleCategory category,RuleSeverity severity,Collection<String> tags){
  this.id=required(id,"id");this.title=required(title,"title");this.description=description==null?"":description;this.source=source==null?"":source;
  if(category==null||severity==null)throw new IllegalArgumentException("Category and severity are required");this.category=category;this.severity=severity;
  Set<String> copy=new LinkedHashSet<String>();if(tags!=null)for(String tag:tags)if(tag!=null&&!tag.trim().isEmpty())copy.add(tag.trim().toLowerCase(Locale.ROOT));this.tags=Collections.unmodifiableSet(copy);
 }
 private static String required(String s,String n){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException(n+" is required");return s.trim();}
 public String getId(){return id;}public String getTitle(){return title;}public String getDescription(){return description;}public String getSource(){return source;}public RuleCategory getCategory(){return category;}public RuleSeverity getSeverity(){return severity;}public Set<String> getTags(){return tags;}
}