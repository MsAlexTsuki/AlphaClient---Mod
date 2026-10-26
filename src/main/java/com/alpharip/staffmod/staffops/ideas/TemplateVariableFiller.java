package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Replaces explicitly supplied {variable} placeholders in staff templates. */
public final class TemplateVariableFiller {
 public static final String NAME="Template Variable Filler",CATEGORY="StaffMods",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public String fill(String template,Map<String,String> values){if(template==null)throw new IllegalArgumentException("template required");if(values==null)throw new IllegalArgumentException("values required");String result=template;for(Map.Entry<String,String> e:values.entrySet()){String key=e.getKey();if(key==null||!key.matches("[A-Za-z][A-Za-z0-9_]*"))continue;result=result.replace("{"+key+"}",e.getValue()==null?"":e.getValue());}return result;}
 public Set<String> unresolved(String template){Set<String> out=new LinkedHashSet<String>();if(template==null)return out;java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\{([A-Za-z][A-Za-z0-9_]*)\\}").matcher(template);while(m.find())out.add(m.group(1));return Collections.unmodifiableSet(out);}
}