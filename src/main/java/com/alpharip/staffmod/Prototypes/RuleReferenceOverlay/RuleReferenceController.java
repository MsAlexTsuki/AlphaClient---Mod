package com.alpharip.staffmod.RuleReferenceOverlay;
public final class RuleReferenceController {
 private final RuleReferenceService service;private final RuleReferenceAccess access;
 public RuleReferenceController(RuleReferenceService service,RuleReferenceAccess access){if(service==null||access==null)throw new IllegalArgumentException("Service and access adapter required");this.service=service;this.access=access;}
 public boolean canView(){return access.canViewRules();}public RuleReferenceResult find(RuleContext context){if(!canView())return new RuleReferenceResult(null);return service.find(context);}public RuleReferenceService getService(){return service;}
}