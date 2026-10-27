package seedu.address.model.lesson;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;

/** An insufficient gap between lessons at different physical venues, calculated without network access. */
public record TravelGap(Lesson previous, Lesson next, int minutes, int buffer) {

    public static final int DEFAULT_BUFFER = 30;
    public static final String MESSAGE_CONSTRAINTS = "Travel buffer must be a whole number from 0 to 180.";

    /** Rejects missing lessons, negative gaps and unsupported buffers at the model boundary. */
    public TravelGap {
        requireAllNonNull(previous, next);
        if (minutes < 0) {
            throw new IllegalArgumentException("Travel gap must not be negative.");
        }
        if (!isValidBuffer(buffer)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
    }

    /** Returns whether the travel buffer is within its supported inclusive range. */
    public static boolean isValidBuffer(int value) {
        return value >= 0 && value <= 180;
    }

    /**
     * Finds each journey from the latest-ending preceding scheduled lesson on the same date.
     * An overlapping lesson takes precedence over a travel warning. Ties retain chronological order.
     */
    public static List<TravelGap> find(List<Lesson> lessons, int buffer) {
        if (!isValidBuffer(buffer)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        List<Lesson> scheduled = lessons.stream().filter(Lesson::isScheduled).sorted(Lesson.CHRONOLOGICAL).toList();
        List<TravelGap> gaps = new ArrayList<>();
        List<Lesson> latest = new ArrayList<>();
        int latestEnd = -1;
        for (int first = 0; first < scheduled.size();) {
            Lesson next = scheduled.get(first);
            if (first == 0 || !scheduled.get(first - 1).getDate().equals(next.getDate())) {
                latest.clear();
                latestEnd = -1;
            }
            int after = first + 1;
            while (after < scheduled.size() && scheduled.get(after).getDate().equals(next.getDate())
                    && scheduled.get(after).getTime().equals(next.getTime())) {
                after++;
            }
            // Lessons starting simultaneously are not predecessors of one another.
            List<Lesson> group = scheduled.subList(first, after);
            int gap = startMinute(next) - latestEnd;
            if (latestEnd >= 0 && gap >= 0 && gap < buffer) {
                for (Lesson destination : group) {
                    latest.stream().filter(lesson -> requiresTravel(lesson, destination)).findFirst()
                            .ifPresent(lesson -> gaps.add(new TravelGap(lesson, destination, gap, buffer)));
                }
            }
            for (Lesson lesson : group) {
                int end = endMinute(lesson);
                if (end > latestEnd) {
                    latest.clear();
                    latestEnd = end;
                }
                if (end == latestEnd) {
                    latest.add(lesson);
                }
            }
            first = after;
        }
        return List.copyOf(gaps);
    }

    private static boolean requiresTravel(Lesson previous, Lesson next) {
        String from = previous.getVenue().map(venue -> venue.value).orElse("");
        String to = next.getVenue().map(venue -> venue.value).orElse("");
        return !from.isBlank() && !to.isBlank() && !from.equalsIgnoreCase("Online")
                && !to.equalsIgnoreCase("Online") && !from.equalsIgnoreCase(to);
    }

    private static int startMinute(Lesson lesson) {
        return lesson.getTime().value.toSecondOfDay() / 60;
    }

    private static int endMinute(Lesson lesson) {
        return startMinute(lesson) + lesson.getDuration().value;
    }
}
