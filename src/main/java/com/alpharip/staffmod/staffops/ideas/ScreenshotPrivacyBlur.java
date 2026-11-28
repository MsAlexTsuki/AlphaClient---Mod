package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Redacts supplied names in text or metadata; pixel-level screenshot blur requires a later image layer. */
public final class ScreenshotPrivacyBlur {
    public static final String FEATURE_NAME="Screenshot Privacy Blur", CATEGORY="Security", STATUS="ENH";
    public String redactText(String text,Collection<String> names){
        if(text==null)return "";String out=text;
        if(names!=null)for(String name:names)if(name!=null&&!name.trim().isEmpty())out=out.replace(name,"[PLAYER]");
        return out;
    }
}