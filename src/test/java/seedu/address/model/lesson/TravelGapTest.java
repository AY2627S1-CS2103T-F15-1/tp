package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.LessonBuilder;

/** Covers the latest-end rule, physical-venue exceptions and inclusive buffer boundaries. */
public class TravelGapTest {
    @Test
    public void find_differentPhysicalVenues_warnsWithoutBlocking() {
        Lesson earlier = lesson("10:00", 60, "Bishan");
        Lesson next = lesson("11:15", 60, "Toa Payoh");
        assertEquals(List.of(new TravelGap(earlier, next, 15, 30)), TravelGap.find(List.of(next, earlier), 30));
        assertTrue(earlier.isScheduled());
        assertTrue(next.isScheduled());
    }

    @Test
    public void find_gapExactlyBufferOrDisabled_doesNotWarn() {
        List<Lesson> lessons = List.of(lesson("10:00", 60, "A"), lesson("11:30", 60, "B"));
        assertTrue(TravelGap.find(lessons, 30).isEmpty());
        assertTrue(TravelGap.find(lessons, 0).isEmpty());
        assertEquals(1, TravelGap.find(lessons, 31).size());
    }

    @Test
    public void find_backToBackDifferentVenues_warnsButDoesNotOverlap() {
        Lesson earlier = lesson("10:00", 60, "A");
        Lesson next = lesson("11:00", 60, "B");
        assertFalse(earlier.overlaps(next));
        assertEquals(0, TravelGap.find(List.of(earlier, next), 30).getFirst().minutes());
    }

    @Test
    public void find_onlineUnnamedAndSameVenue_neverWarn() {
        String[][] venues = {{"Online", "B"}, {"A", "online"}, {"A", "a"}, {null, "B"}, {"A", null}};
        for (String[] pair : venues) {
            assertTrue(TravelGap.find(List.of(lesson("10:00", 60, pair[0]),
                    lesson("11:00", 60, pair[1])), 180).isEmpty());
        }
    }

    @Test
    public void find_usesLatestEndNotLatestStart() {
        Lesson longLesson = lesson("10:00", 120, "A");
        Lesson shortLesson = lesson("11:00", 30, "B");
        Lesson next = lesson("12:15", 60, "C");
        List<TravelGap> gaps = TravelGap.find(List.of(shortLesson, next, longLesson), 30);
        assertEquals(List.of(new TravelGap(longLesson, next, 15, 30)), gaps);
    }

    @Test
    public void find_latestEndOverlapsNext_doesNotReportAnEarlierJourney() {
        assertTrue(TravelGap.find(List.of(lesson("10:00", 180, "A"), lesson("11:00", 60, "B"),
                lesson("12:15", 60, "C")), 30).isEmpty());
    }

    @Test
    public void find_tiedEnds_usesFirstDifferentPhysicalVenue() {
        Lesson sameVenue = lesson("10:00", 120, "C");
        Lesson differentVenue = lesson("11:00", 60, "B");
        Lesson next = lesson("12:15", 60, "C");
        assertEquals(List.of(new TravelGap(differentVenue, next, 15, 30)),
                TravelGap.find(List.of(sameVenue, differentVenue, next), 30));
    }

    @Test
    public void find_ignoresOtherDatesAndNonScheduledLessons() {
        Lesson next = lesson("11:15", 60, "B");
        Lesson earlier = lesson("10:00", 60, "A");
        for (LessonStatus status : List.of(LessonStatus.CANCELLED, LessonStatus.COMPLETED, LessonStatus.MISSED)) {
            Lesson inactive = new LessonBuilder(earlier).withStatus(status).build();
            assertTrue(TravelGap.find(List.of(inactive, next), 30).isEmpty());
        }
        Lesson yesterday = new LessonBuilder(earlier).withDate(earlier.getDate().value.minusDays(1)).build();
        assertTrue(TravelGap.find(List.of(yesterday, next), 30).isEmpty());
    }

    @Test
    public void find_midnightEnd_doesNotWrapIntoPreviousDay() {
        assertTrue(TravelGap.find(List.of(lesson("22:00", 120, "A"), lesson("23:00", 60, "B")), 180).isEmpty());
    }

    @Test
    public void buffer_rejectsOutOfRangeValues() {
        assertTrue(TravelGap.isValidBuffer(0));
        assertTrue(TravelGap.isValidBuffer(180));
        assertFalse(TravelGap.isValidBuffer(-1));
        assertFalse(TravelGap.isValidBuffer(181));
        assertThrows(IllegalArgumentException.class, () -> TravelGap.find(List.of(), -1));
    }

    private static Lesson lesson(String time, int duration, String venue) {
        LessonBuilder builder = new LessonBuilder().withTime(time).withDuration(duration);
        return (venue == null ? builder : builder.withVenue(venue)).build();
    }
}
