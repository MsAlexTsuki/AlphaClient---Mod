package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Offers local wording checks and reusable neutral phrasing hints. */
public final class WarningWordingCoach {
 public static final String NAME="Warning Wording Coach",CATEGORY="TrialModerator",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private static final String[] FLAGS={"idiot","stupid","shut up","i hate you"};
 public List<String> review(String message){if(message==null)throw new IllegalArgumentException("message required");List<String> tips=new ArrayList<String>();String lower=message.toLowerCase(Locale.ROOT);for(String f:FLAGS)if(lower.contains(f))tips.add("Avoid insulting language; describe the observed behaviour instead.");if(!lower.contains("please")&&!lower.contains("stop"))tips.add("State the behaviour and the requested change clearly.");if(message.trim().length()<12)tips.add("Add enough context for the player to understand the warning.");return Collections.unmodifiableList(tips);}
 public String template(String player,String rule,String action){return "Warning for "+req(player)+": please stop "+req(rule)+". Further violations may result in "+req(action)+".";}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}