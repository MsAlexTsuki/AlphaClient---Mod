package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Non-binding hints from approved anonymised historical data; never applies a sanction. */
public final class SanctionSuggestionHints {
 public static final String NAME="Sanction Suggestion Hints", CATEGORY="Senior Staff", STATUS="NEW";
 public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=true;
 // TODO(Supabase): retrieve approved anonymised aggregates under owner-configured access policies.
 // TODO(Server): final permissions and sanctions must be validated by the server/plugin.
 public static final class Hint {
  public final String ruleKey, suggestedRange; public final int sampleSize;
  public Hint(String key,String range,int size){if(key==null||key.trim().isEmpty())throw new IllegalArgumentException("ruleKey");ruleKey=key.trim();suggestedRange=range==null?"":range.trim();sampleSize=Math.max(0,size);}
 }
 private final List<Hint> hints=new ArrayList<Hint>();
 public synchronized void addLocalHint(Hint hint){if(hint==null)throw new IllegalArgumentException("hint");hints.add(hint);}
 public synchronized List<Hint> getHints(){return Collections.unmodifiableList(new ArrayList<Hint>(hints));}
}