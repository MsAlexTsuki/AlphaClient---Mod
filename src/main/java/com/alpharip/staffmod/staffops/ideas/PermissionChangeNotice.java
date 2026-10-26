package com.alpharip.staffmod.staffops.ideas;

/** Creates readable summaries for permission changes; delivery is left to a later integration layer. */
public final class PermissionChangeNotice {
    public static final String FEATURE_NAME="Permission Change Notice", CATEGORY="Security", STATUS="NEW";
    public String format(String subject,String permission,boolean granted,String actor){
        return required(subject,"subject")+" was "+(granted?"granted":"revoked")+" permission "+required(permission,"permission")
            +(actor==null||actor.trim().isEmpty()?"":" by "+actor.trim())+".";
    }
    private String required(String v,String field){if(v==null||v.trim().isEmpty())throw new IllegalArgumentException(field+" is required");return v.trim();}
}