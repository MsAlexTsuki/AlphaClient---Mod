package com.alpharip.staffmod.RuleReferenceOverlay;
import java.util.*;
public final class RuleReferenceService {
 private final RuleRepository repository;private final RuleMatcher matcher;private final RuleReferenceConfig config;
 public RuleReferenceService(RuleRepository repository,RuleMatcher matcher,RuleReferenceConfig config){if(repository==null||matcher==null||config==null)throw new IllegalArgumentException("Dependencies required");this.repository=repository;this.matcher=matcher;this.config=config;}
 public RuleReferenceResult find(RuleContext context){if(!config.isEnabled()||context==null)return new RuleReferenceResult(Collections.<RuleMatch>emptyList());List<RuleMatch> all=matcher.match(context,repository.getAll());return new RuleReferenceResult(all.subList(0,Math.min(config.getMaxResults(),all.size())));}
 public List<RuleEntry> search(RuleSearchQuery query){List<RuleEntry> out=new ArrayList<RuleEntry>();if(query==null)return out;for(RuleEntry r:repository.getAll())if((query.getCategory()==null||r.getCategory()==query.getCategory())&&(query.getText().isEmpty()||r.getTitle().toLowerCase(Locale.ROOT).contains(query.getText())||r.getDescription().toLowerCase(Locale.ROOT).contains(query.getText())))out.add(r);return Collections.unmodifiableList(out);}
}