package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.model.lesson.Lesson;

public class HomeworkTest {

    private static final UUID STUDENT = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_STUDENT = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    private static Homework assigned(String description, LocalDate dueDate) {
        return Homework.assign(STUDENT, description, dueDate);
    }

    @Test
    public void assign_startsAssignedWithoutFeedbackAndWithAFreshIdentity() {
        Homework first = assigned("Algebra Q1-10", TODAY);
        Homework second = assigned("Algebra Q1-10", TODAY);

        assertEquals(HomeworkStatus.ASSIGNED, first.getStatus());
        assertTrue(first.getFeedback().isEmpty());
        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    public void constructor_description_isTrimmedAndRepeatedSpacesAreCollapsed() {
        assertEquals("Ex 7.2 Q1-15", assigned("  Ex   7.2    Q1-15  ", TODAY).getDescription());
    }

    @Test
    public void constructor_descriptionOfTheLimits_isAccepted() {
        assertEquals(1, assigned("x", TODAY).getDescription().length());
        assertEquals(200, assigned("x".repeat(200), TODAY).getDescription().length());
        assertEquals(200, assigned("  " + "x".repeat(200) + "  ", TODAY).getDescription().length());
    }

    @Test
    public void constructor_invalidDescription_throwsIllegalArgumentException() {
        String[] invalidDescriptions = {"", "   ", "x".repeat(201), "two\nlines", "tab\tinside", "bell\u0007"};
        for (String description : invalidDescriptions) {
            Executable assigning = () -> assigned(description, TODAY);
            assertThrows(IllegalArgumentException.class, Homework.DESCRIPTION_CONSTRAINTS, assigning);
        }
    }

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Homework.assign(null, "x", TODAY));
        assertThrows(NullPointerException.class, () -> Homework.assign(STUDENT, null, TODAY));
        assertThrows(NullPointerException.class, () -> Homework.assign(STUDENT, "x", null));
        assertThrows(NullPointerException.class, () -> new Homework(null, STUDENT, "x", TODAY,
                HomeworkStatus.ASSIGNED, null));
        assertThrows(NullPointerException.class, () -> new Homework(UUID.randomUUID(), STUDENT, "x", TODAY,
                null, null));
    }

    @Test
    public void constructor_feedback_isValidatedAndNormalized() {
        UUID id = UUID.randomUUID();

        Homework item = new Homework(id, STUDENT, "x", TODAY, HomeworkStatus.SUBMITTED, "  Sign   errors  ");

        assertEquals("Sign errors", item.getFeedback().orElseThrow());
        Executable emptyFeedback = () -> new Homework(id, STUDENT, "x", TODAY, HomeworkStatus.SUBMITTED, "");
        Executable longFeedback = () -> new Homework(id, STUDENT, "x", TODAY, HomeworkStatus.SUBMITTED,
                "y".repeat(1001));
        assertThrows(IllegalArgumentException.class, Lesson.MESSAGE_NOTES_CONSTRAINTS, emptyFeedback);
        assertThrows(IllegalArgumentException.class, Lesson.MESSAGE_NOTES_CONSTRAINTS, longFeedback);
        assertEquals(1000, new Homework(id, STUDENT, "x", TODAY, HomeworkStatus.SUBMITTED, "y".repeat(1000))
                .getFeedback().orElseThrow().length());
    }

    @Test
    public void isOverdue_onlyAssignedHomeworkAfterItsDueDate() {
        assertTrue(assigned("x", TODAY.minusDays(1)).isOverdue(TODAY));
        assertFalse(assigned("x", TODAY).isOverdue(TODAY));
        assertFalse(assigned("x", TODAY.plusDays(1)).isOverdue(TODAY));
        assertFalse(assigned("x", TODAY.minusDays(30)).withStatus(HomeworkStatus.SUBMITTED).isOverdue(TODAY));
        assertFalse(assigned("x", TODAY.minusDays(30)).withStatus(HomeworkStatus.REVIEWED).isOverdue(TODAY));
    }

    @Test
    public void isOutstanding_untilReviewed() {
        Homework item = assigned("x", TODAY);

        assertTrue(item.isOutstanding());
        assertTrue(item.withStatus(HomeworkStatus.SUBMITTED).isOutstanding());
        assertFalse(item.withStatus(HomeworkStatus.REVIEWED).isOutstanding());
    }

    @Test
    public void edit_keepsIdentityStatusAndFeedback() {
        Homework submitted = assigned("x", TODAY).withStatus(HomeworkStatus.SUBMITTED).withFeedback("Good");

        Homework edited = submitted.edit("y", TODAY.plusDays(3));

        assertEquals(submitted.getId(), edited.getId());
        assertEquals(submitted.getStudentId(), edited.getStudentId());
        assertEquals(HomeworkStatus.SUBMITTED, edited.getStatus());
        assertEquals("Good", edited.getFeedback().orElseThrow());
        assertEquals("y", edited.getDescription());
        assertEquals(TODAY.plusDays(3), edited.getDueDate());
    }

    @Test
    public void withStatus_movingBackwardKeepsTheFeedback() {
        Homework reviewed = assigned("x", TODAY).withStatus(HomeworkStatus.REVIEWED).withFeedback("Good");

        Homework backToAssigned = reviewed.withStatus(HomeworkStatus.ASSIGNED);

        assertEquals(HomeworkStatus.ASSIGNED, backToAssigned.getStatus());
        assertEquals("Good", backToAssigned.getFeedback().orElseThrow());
    }

    @Test
    public void withFeedback_requiresSubmissionAndReplacesEarlierFeedback() {
        Homework item = assigned("x", TODAY);

        assertThrows(IllegalArgumentException.class, "Cannot record feedback on homework that has not been "
                + "submitted.", () -> item.withFeedback("Too early"));
        assertThrows(NullPointerException.class, () -> item.withStatus(HomeworkStatus.SUBMITTED).withFeedback(null));

        Homework first = item.withStatus(HomeworkStatus.SUBMITTED).withFeedback("First");
        assertEquals("Second", first.withFeedback("Second").getFeedback().orElseThrow());
        assertEquals("Third", first.withStatus(HomeworkStatus.REVIEWED).withFeedback("Third")
                .getFeedback().orElseThrow());
    }

    @Test
    public void isSameAssignment_ignoresLetterCaseOfTheDescriptionOnly() {
        Homework original = assigned("Algebra Q1-10", TODAY);

        assertTrue(original.isSameAssignment(assigned("Algebra Q1-10", TODAY)));
        assertTrue(original.isSameAssignment(assigned("algebra q1-10", TODAY)));
        assertTrue(original.isSameAssignment(assigned("ALGEBRA  Q1-10", TODAY)));
        assertFalse(original.isSameAssignment(assigned("Algebra Q1-11", TODAY)));
        assertFalse(original.isSameAssignment(assigned("Algebra Q1-10", TODAY.plusDays(1))));
        assertFalse(original.isSameAssignment(Homework.assign(OTHER_STUDENT, "Algebra Q1-10", TODAY)));
    }

    @Test
    public void isSameAssignment_includesReviewedHomework() {
        Homework reviewed = assigned("Algebra", TODAY).withStatus(HomeworkStatus.REVIEWED);

        assertTrue(reviewed.isSameAssignment(assigned("Algebra", TODAY)));
    }

    @Test
    public void equalsAndHashCode_useIdentityAndEveryField() {
        Homework item = assigned("x", TODAY);

        assertEquals(item, item);
        assertEquals(item, item.edit("x", TODAY));
        assertEquals(item.hashCode(), item.edit("x", TODAY).hashCode());
        assertNotEquals(item, null);
        assertNotEquals(item, "x");
        assertNotEquals(item, assigned("x", TODAY));
        assertNotEquals(item, item.edit("y", TODAY));
        assertNotEquals(item, item.edit("x", TODAY.plusDays(1)));
        assertNotEquals(item, item.withStatus(HomeworkStatus.SUBMITTED));
        assertNotEquals(item.withStatus(HomeworkStatus.SUBMITTED).withFeedback("a"),
                item.withStatus(HomeworkStatus.SUBMITTED));
    }
}
