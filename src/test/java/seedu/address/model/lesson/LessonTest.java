package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalLessons.ALICE_MATH;
import static seedu.address.testutil.TypicalLessons.BENSON_PHYSICS;
import static seedu.address.testutil.TypicalLessons.CARL_ENGLISH;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Venue;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

public class LessonTest {

    @Test
    public void constructor_nullRequiredField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Lesson(null, ALICE_MATH.getStudentPhone(),
                ALICE_MATH.getSubject(), ALICE_MATH.getDate(), ALICE_MATH.getTime(), ALICE_MATH.getDuration()));
        assertThrows(NullPointerException.class, () -> new Lesson(ALICE_MATH.getStudentName(),
                ALICE_MATH.getStudentPhone(), ALICE_MATH.getSubject(), ALICE_MATH.getDate(), ALICE_MATH.getTime(),
                ALICE_MATH.getDuration(), null, null, null, null));
    }

    @Test
    public void constructor_lessonRunsPastMidnight_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Lesson.MESSAGE_CONSTRAINTS, ()
                -> new LessonBuilder().withTime("23:00").withDuration(120).build());
    }

    @Test
    public void constructor_lessonEndsAtMidnight_success() {
        Lesson lesson = new LessonBuilder().withTime("22:00").withDuration(120).build();
        assertEquals(LocalTime.MIDNIGHT, lesson.getEndTime());
    }

    @Test
    public void constructor_requiredFieldsOnly_scheduledLessonWithoutOptionalFields() {
        Lesson lesson = new Lesson(ALICE_MATH.getStudentName(), ALICE_MATH.getStudentPhone(),
                ALICE_MATH.getSubject(), ALICE_MATH.getDate(), ALICE_MATH.getTime(), ALICE_MATH.getDuration());
        assertTrue(lesson.isScheduled());
        assertEquals(Optional.empty(), lesson.getVenue());
        assertEquals(Optional.empty(), lesson.getNotes());
        assertEquals(Optional.empty(), lesson.getCancelReason());
    }

    @Test
    public void isValidTimeRange() {
        // ends before midnight
        assertTrue(Lesson.isValidTimeRange(new LessonTime("16:30"), new LessonDuration(90)));

        // ends exactly at midnight
        assertTrue(Lesson.isValidTimeRange(new LessonTime("23:00"), new LessonDuration(60)));

        // ends after midnight
        assertFalse(Lesson.isValidTimeRange(new LessonTime("23:00"), new LessonDuration(75)));
        assertFalse(Lesson.isValidTimeRange(new LessonTime("23:45"), new LessonDuration(30)));
    }

    @Test
    public void getters_returnFieldValues() {
        Lesson lesson = new LessonBuilder().withVenue("Online").withStatus(LessonStatus.CANCELLED)
                .withNotes("Covered algebra").withCancelReason("Student unwell").build();

        assertEquals(LessonBuilder.DEFAULT_STUDENT_NAME, lesson.getStudentName().fullName);
        assertEquals(LessonBuilder.DEFAULT_STUDENT_PHONE, lesson.getStudentPhone().value);
        assertEquals(LessonBuilder.DEFAULT_SUBJECT, lesson.getSubject().value);
        assertEquals(new LessonDate(LessonBuilder.DEFAULT_DATE), lesson.getDate());
        assertEquals(new LessonTime(LessonBuilder.DEFAULT_TIME), lesson.getTime());
        assertEquals(new LessonDuration(LessonBuilder.DEFAULT_DURATION), lesson.getDuration());
        assertEquals(LocalTime.of(18, 0), lesson.getEndTime());
        assertEquals(Optional.of(new Venue("Online")), lesson.getVenue());
        assertEquals(LessonStatus.CANCELLED, lesson.getStatus());
        assertEquals(Optional.of("Covered algebra"), lesson.getNotes());
        assertEquals(Optional.of("Student unwell"), lesson.getCancelReason());
    }

    @Test
    public void isScheduled() {
        assertTrue(ALICE_MATH.isScheduled());
        assertFalse(new LessonBuilder(ALICE_MATH).withStatus(LessonStatus.COMPLETED).build().isScheduled());
        assertFalse(new LessonBuilder(ALICE_MATH).withStatus(LessonStatus.CANCELLED).build().isScheduled());
        assertFalse(new LessonBuilder(ALICE_MATH).withStatus(LessonStatus.MISSED).build().isScheduled());
    }

    @Test
    public void isWith() {
        // the student of the lesson -> returns true
        assertTrue(ALICE_MATH.isWith(ALICE));

        // the student with a different level and rate -> returns true
        assertTrue(ALICE_MATH.isWith(new PersonBuilder(ALICE).withLevel("S5").withRate("90").build()));

        // another student -> returns false
        assertFalse(ALICE_MATH.isWith(BENSON));

        // same name but different phone -> returns false
        assertFalse(ALICE_MATH.isWith(new PersonBuilder(ALICE).withPhone("81234567").build()));
    }

    @Test
    public void withStudent_returnsLessonWithNewStudentAndSameDetails() {
        Lesson lesson = new LessonBuilder(ALICE_MATH).withStatus(LessonStatus.COMPLETED).withNotes("Covered algebra")
                .build();
        Person editedAlice = new PersonBuilder(ALICE).withName("Alice Pauline Tan").withPhone("81234567").build();

        Lesson editedLesson = lesson.withStudent(editedAlice);

        assertTrue(editedLesson.isWith(editedAlice));
        assertEquals(new LessonBuilder(lesson).withStudent(editedAlice).build(), editedLesson);
    }

    @Test
    public void isSameLesson() {
        // same object -> returns true
        assertTrue(ALICE_MATH.isSameLesson(ALICE_MATH));

        // null -> returns false
        assertFalse(ALICE_MATH.isSameLesson(null));

        // same identity fields, other attributes different -> returns true
        Lesson editedLesson = new LessonBuilder(ALICE_MATH).withVenue("Online").withStatus(LessonStatus.COMPLETED)
                .withNotes("Covered algebra").build();
        assertTrue(ALICE_MATH.isSameLesson(editedLesson));

        // student name differs in case -> returns true
        editedLesson = new LessonBuilder(ALICE_MATH).withStudent(ALICE.getName().fullName.toUpperCase(),
                ALICE.getPhone().value).build();
        assertTrue(ALICE_MATH.isSameLesson(editedLesson));

        // subject differs in case -> returns true
        editedLesson = new LessonBuilder(ALICE_MATH).withSubject("MATH").build();
        assertTrue(ALICE_MATH.isSameLesson(editedLesson));

        // different student name -> returns false
        editedLesson = new LessonBuilder(ALICE_MATH).withStudent(BENSON).build();
        assertFalse(ALICE_MATH.isSameLesson(editedLesson));

        // different student phone -> returns false
        editedLesson = new LessonBuilder(ALICE_MATH).withStudent(ALICE.getName().fullName, "81234567").build();
        assertFalse(ALICE_MATH.isSameLesson(editedLesson));

        // different subject -> returns false
        editedLesson = new LessonBuilder(ALICE_MATH).withSubject("Physics").build();
        assertFalse(ALICE_MATH.isSameLesson(editedLesson));

        // different date -> returns false
        editedLesson = new LessonBuilder(ALICE_MATH).withDate("2026-10-13").build();
        assertFalse(ALICE_MATH.isSameLesson(editedLesson));

        // different time -> returns false
        editedLesson = new LessonBuilder(ALICE_MATH).withTime("16:31").build();
        assertFalse(ALICE_MATH.isSameLesson(editedLesson));

        // different duration -> returns false
        editedLesson = new LessonBuilder(ALICE_MATH).withDuration(60).build();
        assertFalse(ALICE_MATH.isSameLesson(editedLesson));
    }

    @Test
    public void overlaps() {
        // starts during the other lesson -> returns true, in both directions
        assertTrue(ALICE_MATH.overlaps(BENSON_PHYSICS));
        assertTrue(BENSON_PHYSICS.overlaps(ALICE_MATH));

        // same time -> returns true
        assertTrue(ALICE_MATH.overlaps(new LessonBuilder(ALICE_MATH).withSubject("Physics").build()));

        // one lesson inside the other -> returns true
        Lesson insideLesson = new LessonBuilder().withTime("16:45").withDuration(30).build();
        assertTrue(ALICE_MATH.overlaps(insideLesson));
        assertTrue(insideLesson.overlaps(ALICE_MATH));

        // overlaps by one quarter hour -> returns true
        assertTrue(ALICE_MATH.overlaps(new LessonBuilder().withTime("17:45").withDuration(60).build()));

        // starts when the other lesson ends -> returns false, in both directions
        Lesson backToBackLesson = new LessonBuilder().withTime("18:00").withDuration(60).build();
        assertFalse(ALICE_MATH.overlaps(backToBackLesson));
        assertFalse(backToBackLesson.overlaps(ALICE_MATH));

        // different date -> returns false
        assertFalse(ALICE_MATH.overlaps(CARL_ENGLISH));
        assertFalse(ALICE_MATH.overlaps(new LessonBuilder(BENSON_PHYSICS).withDate("2026-10-13").build()));

        // cancelled lesson -> returns false, in both directions
        Lesson cancelledLesson = new LessonBuilder(BENSON_PHYSICS).withStatus(LessonStatus.CANCELLED).build();
        assertFalse(ALICE_MATH.overlaps(cancelledLesson));
        assertFalse(cancelledLesson.overlaps(ALICE_MATH));

        // lesson that ends at midnight does not overlap with the lesson that starts at midnight the next day
        Lesson lateLesson = new LessonBuilder().withTime("23:00").withDuration(60).build();
        Lesson earlyLessonNextDay = new LessonBuilder().withDate("2026-10-13").withTime("00:00").build();
        assertFalse(lateLesson.overlaps(earlyLessonNextDay));

        // lesson that ends at midnight overlaps with a lesson that starts before it ends
        assertTrue(lateLesson.overlaps(new LessonBuilder().withTime("22:30").withDuration(60).build()));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Lesson aliceMathCopy = new LessonBuilder(ALICE_MATH).build();
        assertTrue(ALICE_MATH.equals(aliceMathCopy));

        // same object -> returns true
        assertTrue(ALICE_MATH.equals(ALICE_MATH));

        // null -> returns false
        assertFalse(ALICE_MATH.equals(null));

        // different type -> returns false
        assertFalse(ALICE_MATH.equals(5));

        // different lesson -> returns false
        assertFalse(ALICE_MATH.equals(BENSON_PHYSICS));

        // different student name -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withStudent(BENSON).build()));

        // different student phone -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withStudent(ALICE.getName().fullName,
                "81234567").build()));

        // different subject -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withSubject("Physics").build()));

        // different date -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withDate("2026-10-13").build()));

        // different time -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withTime("16:45").build()));

        // different duration -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withDuration(60).build()));

        // different venue -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withVenue("Online").build()));

        // missing venue -> returns false
        assertFalse(ALICE_MATH.equals(new Lesson(ALICE_MATH.getStudentName(), ALICE_MATH.getStudentPhone(),
                ALICE_MATH.getSubject(), ALICE_MATH.getDate(), ALICE_MATH.getTime(), ALICE_MATH.getDuration())));

        // different status -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withStatus(LessonStatus.MISSED).build()));

        // different notes -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withNotes("Covered algebra").build()));

        // different cancel reason -> returns false
        assertFalse(ALICE_MATH.equals(new LessonBuilder(ALICE_MATH).withCancelReason("Student unwell").build()));
    }

    @Test
    public void hashCode_equalLessons_sameHashCode() {
        assertEquals(ALICE_MATH.hashCode(), new LessonBuilder(ALICE_MATH).build().hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = Lesson.class.getCanonicalName() + "{studentName=" + ALICE_MATH.getStudentName()
                + ", studentPhone=" + ALICE_MATH.getStudentPhone() + ", subject=" + ALICE_MATH.getSubject()
                + ", date=" + ALICE_MATH.getDate() + ", time=" + ALICE_MATH.getTime()
                + ", duration=" + ALICE_MATH.getDuration() + ", venue=" + ALICE_MATH.getVenue().get()
                + ", status=" + ALICE_MATH.getStatus() + ", notes=null, cancelReason=null}";
        assertEquals(expected, ALICE_MATH.toString());
    }

    @Test
    public void name_hasSameNameIgnoringCase_isSameStudentName() {
        // the student's name keeps the case it was entered in, while the identity ignores it
        Lesson upperCaseLesson = new LessonBuilder(ALICE_MATH)
                .withStudent(ALICE.getName().fullName.toUpperCase(), ALICE.getPhone().value).build();
        assertEquals(new Name(ALICE.getName().fullName.toUpperCase()), upperCaseLesson.getStudentName());
        assertTrue(upperCaseLesson.isWith(ALICE));
    }
}
