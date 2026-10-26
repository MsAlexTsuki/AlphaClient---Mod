package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking tutorial steps and completion. */
public class FirstTimeClientTutorial {
 private final java.util.Set<String> completedSteps=new java.util.LinkedHashSet<String>(); private boolean dismissed;
 public void completeStep(String step){if(step!=null&&!step.trim().isEmpty())completedSteps.add(step);}
 public boolean isStepComplete(String step){return completedSteps.contains(step);}
 public void dismiss(){dismissed=true;} public boolean isDismissed(){return dismissed;}
 public java.util.Set<String> getCompletedSteps(){return java.util.Collections.unmodifiableSet(completedSteps);}
}
