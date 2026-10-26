package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Quiz item model. Video playback, clip sourcing, and interactive UI are not wired here. */
public final class HitSelectionQuiz {
 public static final String NAME="Hit Selection Quiz",CATEGORY="Practice",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Question {public final String id,clipReference,prompt;public final long correctTimestampMs;public final long toleranceMs;private Question(String i,String c,String p,long t,long tol){id=i;clipReference=c;prompt=p;correctTimestampMs=t;toleranceMs=tol;}}
 private final Map<String,Question> questions=new LinkedHashMap<String,Question>();
 public synchronized void add(String id,String clip,String prompt,long correctMs,long toleranceMs){if(correctMs<0||toleranceMs<0)throw new IllegalArgumentException("timestamps must be non-negative");questions.put(text(id),new Question(text(id),text(clip),text(prompt),correctMs,toleranceMs));}
 public synchronized boolean answer(String id,long selectedMs){Question q=questions.get(text(id));if(q==null)throw new IllegalArgumentException("Unknown question");return Math.abs(selectedMs-q.correctTimestampMs)<=q.toleranceMs;}
 public synchronized List<Question> snapshot(){return Collections.unmodifiableList(new ArrayList<Question>(questions.values()));}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}