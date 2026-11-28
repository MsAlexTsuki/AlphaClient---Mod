package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Case-insensitive search over text provided by the caller. */
public final class InvestigationEventSearch {
 public List<String> search(List<String> entries,String query){List<String> result=new ArrayList<String>();if(entries==null||query==null||query.trim().isEmpty())return result;String needle=query.trim().toLowerCase(java.util.Locale.ROOT);for(String entry:entries)if(entry!=null&&entry.toLowerCase(java.util.Locale.ROOT).contains(needle))result.add(entry);return Collections.unmodifiableList(result);}
}