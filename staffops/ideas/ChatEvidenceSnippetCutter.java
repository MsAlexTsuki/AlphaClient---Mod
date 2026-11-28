package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Selects a bounded range from captured chat lines. Chat capture is an external event hook. */
public final class ChatEvidenceSnippetCutter {
 public static final String NAME="Chat Evidence Snippet Cutter",CATEGORY="StaffMods",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final List<String> lines=new ArrayList<String>();private final int maxLines;
 public ChatEvidenceSnippetCutter(){this(500);}public ChatEvidenceSnippetCutter(int max){if(max<1)throw new IllegalArgumentException("max must be positive");maxLines=max;}
 public synchronized void append(String line){if(line==null)return;lines.add(line);while(lines.size()>maxLines)lines.remove(0);}
 public synchronized String cut(int startInclusive,int endExclusive){if(startInclusive<0||endExclusive>lines.size()||startInclusive>=endExclusive)throw new IndexOutOfBoundsException("Invalid snippet range");StringBuilder b=new StringBuilder();for(int i=startInclusive;i<endExclusive;i++){if(b.length()>0)b.append('\n');b.append(lines.get(i));}return b.toString();}
 public synchronized List<String> snapshot(){return Collections.unmodifiableList(new ArrayList<String>(lines));}
}