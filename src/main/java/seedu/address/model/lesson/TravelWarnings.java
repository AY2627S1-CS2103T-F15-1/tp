package seedu.address.model.lesson;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import seedu.address.model.ReadOnlyAddressBook;

/**
 * Offline warnings for insufficient gaps between scheduled physical lessons. The tutor leaves the lesson that ends
 * last before a lesson starts, so each lesson is measured against the latest end of the lessons before it, and
 * overlapping lessons cannot hide a short journey.
 */
public final class TravelWarnings {
    private TravelWarnings() {
    }

    /** Formats relevant warnings without conflating overlapping bookings with travel gaps. */
    public static String format(ReadOnlyAddressBook book, Collection<Lesson> relevant) {
        return lines(book, relevant).stream().map(line -> "\n" + line).collect(Collectors.joining());
    }

    /** Returns one warning line for each relevant lesson that follows the latest-ending earlier one too closely. */
    public static List<String> lines(ReadOnlyAddressBook book, Collection<Lesson> relevant) {
        Set<UUID> ids = relevant.stream().map(Lesson::getId).collect(Collectors.toSet());
        List<Lesson> scheduled = book.getLessonList().stream().filter(Lesson::isScheduled)
                .sorted(Lesson.CHRONOLOGICAL).toList();
        List<String> result = new ArrayList<>();
        LocalDateTime latestEnd = null;
        List<Lesson> endingLast = new ArrayList<>();
        for (Lesson after : scheduled) {
            if (latestEnd != null) {
                warningFor(book, ids, endingLast, latestEnd, after).ifPresent(result::add);
            }
            LocalDateTime end = endOf(after);
            if (latestEnd == null || end.isAfter(latestEnd)) {
                latestEnd = end;
                endingLast.clear();
            }
            if (end.equals(latestEnd)) {
                endingLast.add(after);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Returns the warning for {@code after}, measured from the first of the lessons that end last ({@code
     * endingLast}, all ending at {@code latestEnd}) for which a journey between venues is expected and too short.
     */
    private static Optional<String> warningFor(ReadOnlyAddressBook book, Set<UUID> ids, List<Lesson> endingLast,
            LocalDateTime latestEnd, Lesson after) {
        long gap = Duration.between(latestEnd, startOf(after)).toMinutes();
        if (gap < 0 || gap >= book.getTravelBufferMinutes()) {
            return Optional.empty();
        }
        String to = venueOf(after);
        for (Lesson before : endingLast) {
            String from = venueOf(before);
            if (!ids.contains(before.getId()) && !ids.contains(after.getId())) {
                continue;
            }
            if (from.isEmpty() || to.isEmpty() || from.equalsIgnoreCase(to)
                    || from.equalsIgnoreCase("Online") || to.equalsIgnoreCase("Online")) {
                continue;
            }
            return Optional.of("Travel warning: " + before.getStudentName() + " at " + from + " -> "
                    + after.getStudentName() + " at " + to + " (" + after.getDate() + " " + after.getTime()
                    + "): " + gap + " min available; " + book.getTravelBufferMinutes() + " min required.");
        }
        return Optional.empty();
    }

    private static String venueOf(Lesson lesson) {
        return lesson.getVenue().map(venue -> venue.value.strip()).orElse("");
    }

    private static LocalDateTime startOf(Lesson lesson) {
        return lesson.getDate().value.atTime(lesson.getTime().value);
    }

    private static LocalDateTime endOf(Lesson lesson) {
        return startOf(lesson).plusMinutes(lesson.getDuration().value);
    }
}
