package com.alpharip.staffmod.RuleReferenceOverlay;
import java.util.*;
public final class RuleMatcher {
 public List<RuleMatch> match(RuleContext context,List<RuleEntry> rules){
  List<RuleMatch> out=new ArrayList<RuleMatch>();if(context==null||rules==null)return out;
  String text=context.getReportText().toLowerCase(Locale.ROOT);
  for(RuleEntry r:rules){if(r==null)continue;int score=r.getCategory()==context.getCategory()?10:0;
   for(String tag:r.getTags())if(!tag.isEmpty()&&text.contains(tag))score+=5;
   if(!r.getTitle().isEmpty()&&text.contains(r.getTitle().toLowerCase(Locale.ROOT)))score+=3;
   if(score>0)out.add(new RuleMatch(r,score));
  }
  Collections.sort(out,new Comparator<RuleMatch>(){public int compare(RuleMatch a,RuleMatch b){return Integer.compare(b.getScore(),a.getScore());}});return out;
 }
}