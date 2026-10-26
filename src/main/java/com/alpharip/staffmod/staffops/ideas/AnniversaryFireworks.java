package com.alpharip.staffmod.staffops.ideas;

import java.time.LocalDate;
import java.util.*;

/** Matches a supplied date to configured anniversary dates; no fireworks are rendered here. */
public final class AnniversaryFireworks {
 public static final String FEATURE_NAME="Anniversary Fireworks",CATEGORY="Founder Fun",STATUS="NEW";
 private final Set<MonthDay> dates=new HashSet<MonthDay>();
 public void addDate(MonthDay date){if(date==null)throw new IllegalArgumentException("date required");dates.add(date);}
 public void removeDate(MonthDay date){dates.remove(date);}
 public boolean isAnniversary(LocalDate date){return date!=null&&dates.contains(MonthDay.from(date));}
 public Set<MonthDay> getDates(){return Collections.unmodifiableSet(new HashSet<MonthDay>(dates));}
}