package seedu.address.model.lesson;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import seedu.address.model.ReadOnlyAddressBook;

/** Offline warnings for insufficient gaps between consecutive scheduled physical lessons. */
public final class TravelWarnings {
    private TravelWarnings() {
    }

    /** Formats relevant warnings without conflating overlapping bookings with travel gaps. */
    public static String format(ReadOnlyAddressBook book, Collection<Lesson> relevant) {
        Set<UUID> ids = relevant.stream().map(Lesson::getId).collect(Collectors.toSet());
        List<Lesson> scheduled = book.getLessonList().stream().filter(Lesson::isScheduled)
                .sorted(Lesson.CHRONOLOGICAL).toList();
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < scheduled.size(); i++) {
            Lesson before = scheduled.get(i - 1);
            Lesson after = scheduled.get(i);
            if (!ids.contains(before.getId()) && !ids.contains(after.getId())) {
                continue;
            }
            String from = before.getVenue().map(venue -> venue.value.strip()).orElse("");
            String to = after.getVenue().map(venue -> venue.value.strip()).orElse("");
            if (from.isEmpty() || to.isEmpty() || from.equalsIgnoreCase(to)
                    || from.equalsIgnoreCase("Online") || to.equalsIgnoreCase("Online")) {
                continue;
            }
            long gap = Duration.between(before.getDate().value.atTime(before.getTime().value)
                    .plusMinutes(before.getDuration().value), after.getDate().value.atTime(after.getTime().value))
                    .toMinutes();
            if (gap >= 0 && gap < book.getTravelBufferMinutes()) {
                result.append("\nTravel warning: ").append(before.getStudentName()).append(" at ").append(from)
                        .append(" -> ").append(after.getStudentName()).append(" at ").append(to)
                        .append(" (").append(after.getDate()).append(" ").append(after.getTime()).append("): ")
                        .append(gap).append(" min available; ").append(book.getTravelBufferMinutes())
                        .append(" min required.");
            }
        }
        return result.toString();
    }
}
