package com.alpharip.staffmod.staffdashboardexpansion.reviewcalendar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local in-memory review storage, date queries, and reminder calculation. */
public final class StaffReviewCalendarManager {
    private final Map<Integer, StaffReview> reviews = new LinkedHashMap<Integer, StaffReview>();
    private int nextId = 1;

    public synchronized StaffReview create(LocalDate date, java.time.LocalTime time, String title,
                                           String staffName, String notes, int reminderDaysBefore) {
        StaffReview review = new StaffReview(nextId++, date, time, title, staffName, notes, reminderDaysBefore);
        reviews.put(review.getId(), review);
        return review;
    }

    public synchronized StaffReview update(int id, LocalDate date, java.time.LocalTime time,
                                           String title, String staffName, String notes,
                                           int reminderDaysBefore) {
        StaffReview review = get(id);
        if (review == null) throw new IllegalArgumentException("Review not found.");
        review.updateDetails(date, time, title, staffName, notes, reminderDaysBefore);
        return review;
    }

    public synchronized StaffReview get(int id) { return reviews.get(id); }

    public synchronized boolean remove(int id) { return reviews.remove(id) != null; }

    public synchronized void setStatus(int id, StaffReviewStatus status) {
        StaffReview review = get(id);
        if (review == null) throw new IllegalArgumentException("Review not found.");
        review.setStatus(status);
    }

    public synchronized List<StaffReview> getAll() {
        List<StaffReview> result = new ArrayList<StaffReview>(reviews.values());
        sort(result);
        return Collections.unmodifiableList(result);
    }

    public synchronized List<StaffReview> getForDate(LocalDate date) {
        List<StaffReview> result = new ArrayList<StaffReview>();
        if (date == null) return Collections.unmodifiableList(result);
        for (StaffReview review : reviews.values()) if (date.equals(review.getDate())) result.add(review);
        sort(result);
        return Collections.unmodifiableList(result);
    }

    public synchronized List<StaffReview> getForMonth(int year, int month) {
        List<StaffReview> result = new ArrayList<StaffReview>();
        for (StaffReview review : reviews.values()) {
            if (review.getDate().getYear() == year && review.getDate().getMonthValue() == month) result.add(review);
        }
        sort(result);
        return Collections.unmodifiableList(result);
    }

    public synchronized List<StaffReview> getForYear(int year) {
        List<StaffReview> result = new ArrayList<StaffReview>();
        for (StaffReview review : reviews.values()) if (review.getDate().getYear() == year) result.add(review);
        sort(result);
        return Collections.unmodifiableList(result);
    }

    public synchronized int countForDate(LocalDate date) { return getForDate(date).size(); }
    public synchronized int countForYear(int year) { return getForYear(year).size(); }

    public synchronized List<StaffReviewReminder> getReminders(LocalDate today) {
        if (today == null) throw new IllegalArgumentException("Current date is required.");
        List<StaffReviewReminder> result = new ArrayList<StaffReviewReminder>();
        for (StaffReview review : reviews.values()) {
            if (review.getStatus() != StaffReviewStatus.SCHEDULED || review.isReminderAcknowledged()) continue;
            LocalDate reminderDate = review.getReminderDate();
            if (!today.isBefore(reminderDate)) {
                boolean overdue = review.getDate().isBefore(today);
                result.add(new StaffReviewReminder(review.getId(), reminderDate,
                        review.getTitle() + " — " + review.getStaffName(), overdue));
            }
        }
        Collections.sort(result, new Comparator<StaffReviewReminder>() {
            @Override public int compare(StaffReviewReminder a, StaffReviewReminder b) {
                return a.getReminderDate().compareTo(b.getReminderDate());
            }
        });
        return Collections.unmodifiableList(result);
    }

    public synchronized void acknowledgeReminder(int reviewId) {
        StaffReview review = get(reviewId);
        if (review == null) throw new IllegalArgumentException("Review not found.");
        review.acknowledgeReminder();
    }

    public synchronized int size() { return reviews.size(); }
    public synchronized void clear() { reviews.clear(); nextId = 1; }

    private void sort(List<StaffReview> items) {
        Collections.sort(items, new Comparator<StaffReview>() {
            @Override public int compare(StaffReview a, StaffReview b) {
                int date = a.getDate().compareTo(b.getDate());
                if (date != 0) return date;
                if (a.getTime() == null && b.getTime() != null) return -1;
                if (a.getTime() != null && b.getTime() == null) return 1;
                if (a.getTime() == null) return Integer.compare(a.getId(), b.getId());
                int time = a.getTime().compareTo(b.getTime());
                return time != 0 ? time : Integer.compare(a.getId(), b.getId());
            }
        });
    }
}
