package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Calculates recurring access-review due times; scheduling notifications is integration work. */
public final class AccessReviewReminder {
 public static final String NAME="Access Review Reminder",CATEGORY="OwnerStaffing",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private long intervalMillis,lastReview;
 public synchronized void configure(long intervalMillis){if(intervalMillis<1)throw new IllegalArgumentException("interval must be positive");this.intervalMillis=intervalMillis;}
 public synchronized void markReviewed(long time){if(time<0)throw new IllegalArgumentException("time must be non-negative");lastReview=time;}
 public synchronized long dueAt(){if(intervalMillis<=0||lastReview<=0)return -1;return lastReview+intervalMillis;}
 public synchronized boolean isDue(long now){return dueAt()>=0&&now>=dueAt();}
}