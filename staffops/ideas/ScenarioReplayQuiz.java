package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local training scenarios; all scenario text must be anonymized by the author before adding. */
public final class ScenarioReplayQuiz {
 public static final String NAME="Scenario Replay Quiz",CATEGORY="TrialModerator",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Scenario {public final String id,prompt;public final List<String> choices;public final int answer;private Scenario(String i,String p,List<String> c,int a){id=i;prompt=p;choices=Collections.unmodifiableList(new ArrayList<String>(c));answer=a;}}
 private final Map<String,Scenario> scenarios=new LinkedHashMap<String,Scenario>();
 public synchronized void add(String id,String prompt,List<String> choices,int answer){if(choices==null||choices.size()<2||answer<0||answer>=choices.size())throw new IllegalArgumentException("invalid choices or answer");scenarios.put(req(id),new Scenario(req(id),req(prompt),choices,answer));}
 public synchronized boolean check(String id,int choice){Scenario s=scenarios.get(req(id));if(s==null)throw new IllegalArgumentException("unknown scenario");return s.answer==choice;}
 public synchronized List<Scenario> snapshot(){return Collections.unmodifiableList(new ArrayList<Scenario>(scenarios.values()));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}