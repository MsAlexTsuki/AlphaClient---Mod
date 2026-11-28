package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Defines reusable practice scenarios. Launching gameplay scenarios and UI integration are not wired. */
public final class CustomPracticeScenarioBuilder {
    public static final String NAME = "Custom Practice Scenario Builder";
    public static final String CATEGORY = "Practice";
    public static final class Step {
        public final String id, instruction;
        public final int targetCount;
        public Step(String id,String instruction,int targetCount){
            this.id=text(id,"step id");this.instruction=text(instruction,"instruction");
            if(targetCount<1)throw new IllegalArgumentException("Target count must be positive");
            this.targetCount=targetCount;
        }
    }
    public static final class Scenario {
        public final String id,name,description;
        public final int timeLimitSeconds;
        private final List<Step> steps;
        private Scenario(String i,String n,String d,int t,List<Step>s){id=i;name=n;description=d;timeLimitSeconds=t;steps=Collections.unmodifiableList(new ArrayList<Step>(s));}
        public List<Step> getSteps(){return steps;}
    }
    private final Map<String,Scenario> scenarios=new LinkedHashMap<String,Scenario>();
    public synchronized Scenario save(String id,String name,String description,int timeLimitSeconds,List<Step> steps){
        String key=text(id,"scenario id");String title=text(name,"scenario name");
        if(timeLimitSeconds<0||timeLimitSeconds>86400)throw new IllegalArgumentException("Time limit must be 0-86400 seconds");
        if(steps==null||steps.isEmpty())throw new IllegalArgumentException("At least one step is required");
        Scenario s=new Scenario(key,title,description==null?"":description.trim(),timeLimitSeconds,steps);scenarios.put(key,s);return s;
    }
    public synchronized Scenario get(String id){return scenarios.get(id);}
    public synchronized boolean remove(String id){return scenarios.remove(id)!=null;}
    public synchronized List<Scenario> snapshot(){return Collections.unmodifiableList(new ArrayList<Scenario>(scenarios.values()));}
    private static String text(String s,String label){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException(label+" is required");return s.trim();}
}