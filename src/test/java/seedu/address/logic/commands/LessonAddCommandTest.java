package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code LessonAddCommand}.
 */
public class LessonAddCommandTest {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(10);
    private static final LessonDate DATE = new LessonDate(FUTURE_DATE.toString());
    private static final LessonTime TIME = new LessonTime("16:30");
    private static final LessonDuration DURATION = new LessonDuration(90);
    private static final Subject MATH = new Subject("Math");

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullRequiredField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonAddCommand(null, MATH, DATE, TIME, DURATION, null));
        assertThrows(NullPointerException.class, () -> new LessonAddCommand(INDEX_FIRST_PERSON, MATH, null, TIME,
                DURATION, null));
        assertThrows(NullPointerException.class, () -> new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, null,
                DURATION, null));
        assertThrows(NullPointerException.class, () -> new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME,
                null, null));
    }

    @Test
    public void execute_allFieldsGiven_success() {
        Venue venue = new Venue("Online");
        Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(FUTURE_DATE).withVenue("Online").build();
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME, DURATION, venue);

        assertCommandSuccess(command, model, getSuccessMessage(lesson, "Online"), getExpectedModel(lesson));
    }

    @Test
    public void execute_subjectAndVenueOmitted_usesStudentSubjectAndVenue() {
        Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(FUTURE_DATE)
                .withVenue(ALICE.getVenue().get().value).build();
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, null, DATE, TIME, DURATION, null);

        assertCommandSuccess(command, model, getSuccessMessage(lesson, ALICE.getVenue().get().value),
                getExpectedModel(lesson));
    }

    @Test
    public void execute_studentAndLessonWithoutVenue_showsNoVenue() {
        Person studentWithoutVenue = new PersonBuilder(ALICE).withoutVenue().build();
        model.setPerson(ALICE, studentWithoutVenue);
        Lesson lesson = new LessonBuilder().withStudent(studentWithoutVenue).withDate(FUTURE_DATE).build();
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME, DURATION, null);

        assertCommandSuccess(command, model, getSuccessMessage(lesson, Messages.MESSAGE_NO_VENUE),
                getExpectedModel(lesson));
    }

    @Test
    public void execute_overlappingLesson_successWithWarning() {
        Lesson existing = new LessonBuilder().withStudent(BENSON).withSubject("Physics").withDate(FUTURE_DATE)
                .withTime("17:00").withDuration(60).build();
        model.addLesson(existing);
        Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(FUTURE_DATE)
                .withVenue(ALICE.getVenue().get().value).build();
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME, DURATION, null);

        Model expectedModel = getExpectedModel(lesson);
        String expectedMessage = getSuccessMessage(lesson, ALICE.getVenue().get().value)
                + String.format(LessonCommandUtil.MESSAGE_OVERLAP_WARNING, Messages.format(existing) + ".");

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_backToBackLesson_successWithoutWarning() {
        model.addLesson(new LessonBuilder().withStudent(BENSON).withSubject("Physics").withDate(FUTURE_DATE)
                .withTime("18:00").withDuration(60).build());
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME, DURATION, null);

        assertFalse(executeSuccessfully(command).contains("Warning"));
    }

    @Test
    public void execute_invalidStudentIndex_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        LessonAddCommand command = new LessonAddCommand(outOfBoundIndex, MATH, DATE, TIME, DURATION, null);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_subjectNotTaken_throwsCommandException() {
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, new Subject("Physics"), DATE, TIME,
                DURATION, null);

        assertCommandFailure(command, model,
                String.format(LessonAddCommand.MESSAGE_SUBJECT_NOT_TAKEN, ALICE.getName(), "Physics"));
    }

    @Test
    public void execute_subjectOmittedButStudentTakesSeveral_throwsCommandException() {
        LessonAddCommand command = new LessonAddCommand(INDEX_SECOND_PERSON, null, DATE, TIME, DURATION, null);

        assertCommandFailure(command, model,
                String.format(LessonAddCommand.MESSAGE_SUBJECT_REQUIRED, BENSON.getName(), "Math, Physics"));
    }

    @Test
    public void execute_dateInPast_throwsCommandException() {
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH,
                new LessonDate(LocalDate.now().minusDays(1).toString()), TIME, DURATION, null);

        assertCommandFailure(command, model, LessonAddCommand.MESSAGE_DATE_IN_PAST);
    }

    @Test
    public void execute_dateMoreThanTwoYearsAhead_throwsCommandException() {
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH,
                new LessonDate(LocalDate.now().plusYears(2).plusDays(1).toString()), TIME, DURATION, null);

        assertCommandFailure(command, model, LessonCommandUtil.MESSAGE_DATE_TOO_FAR);
    }

    @Test
    public void execute_lessonRunsPastMidnight_throwsCommandException() {
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, new LessonTime("23:00"),
                new LessonDuration(120), null);

        assertCommandFailure(command, model, Lesson.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void execute_duplicateLesson_throwsCommandException() {
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME, DURATION, null);
        executeSuccessfully(command);

        assertCommandFailure(command, model, LessonCommandUtil.MESSAGE_DUPLICATE_LESSON);
    }

    @Test
    public void equals() {
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME, DURATION, null);

        assertTrue(command.equals(command)); // same object
        assertTrue(command.equals(new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME, DURATION, null)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new LessonAddCommand(INDEX_SECOND_PERSON, MATH, DATE, TIME, DURATION, null)));
        assertFalse(command.equals(new LessonAddCommand(INDEX_FIRST_PERSON, null, DATE, TIME, DURATION, null)));
        assertFalse(command.equals(new LessonAddCommand(INDEX_FIRST_PERSON, MATH, new LessonDate("2030-01-01"),
                TIME, DURATION, null)));
        assertFalse(command.equals(new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, new LessonTime("09:00"),
                DURATION, null)));
        assertFalse(command.equals(new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME,
                new LessonDuration(60), null)));
        assertFalse(command.equals(new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME, DURATION,
                new Venue("Online"))));
    }

    @Test
    public void toStringMethod() {
        LessonAddCommand command = new LessonAddCommand(INDEX_FIRST_PERSON, MATH, DATE, TIME, DURATION, null);
        String expected = LessonAddCommand.class.getCanonicalName() + "{studentIndex=" + INDEX_FIRST_PERSON
                + ", subject=" + MATH + ", date=" + DATE + ", time=" + TIME + ", duration=" + DURATION
                + ", venue=null}";
        assertEquals(expected, command.toString());
    }

    private String executeSuccessfully(LessonAddCommand command) {
        try {
            return command.execute(model).getFeedbackToUser();
        } catch (Exception e) {
            throw new AssertionError("The command should have succeeded", e);
        }
    }

    private String getSuccessMessage(Lesson lesson, String venue) {
        return String.format(LessonAddCommand.MESSAGE_SUCCESS, Messages.format(lesson), venue);
    }

    private Model getExpectedModel(Lesson lesson) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addLesson(lesson);
        return expectedModel;
    }
}
