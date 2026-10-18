package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class LessonBackdatingTest {
    private final ModelManager model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();
    private final Person student = new PersonBuilder().withSubjects("Math").build();

    private CommandResult run(String input) throws Exception {
        return parser.parseCommand(input).execute(model);
    }

    private static String daysFromToday(int days) {
        return LocalDate.now().plusDays(days).toString();
    }

    @Test
    public void lessonAdd_pastDateWithoutFlag_isStillRejected() {
        model.addPerson(student);
        CommandException rejection = assertThrows(CommandException.class, () ->
                run("lesson add st/1 d/" + daysFromToday(-1) + " t/10:00"));
        assertEquals(LessonAddCommand.MESSAGE_DATE_IN_PAST, rejection.getMessage());
        assertTrue(rejection.getMessage().contains("past/"));
        assertTrue(model.getAddressBook().getLessonList().isEmpty());
    }

    @Test
    public void lessonAdd_pastDateWithFlag_recordsLessonThatCanBeCompleted() throws Exception {
        model.addPerson(student);
        run("lesson add st/1 d/" + daysFromToday(-3) + " t/10:00 past/");
        assertEquals(1, model.getAddressBook().getLessonList().size());
        assertEquals(LessonStatus.SCHEDULED, model.getAddressBook().getLessonList().getFirst().getStatus());
        run("lesson done 1 n/Covered fractions");
        Lesson lesson = model.getAddressBook().getLessonList().getFirst();
        assertEquals(LessonStatus.COMPLETED, lesson.getStatus());
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void lessonAdd_flagWithTodayAliasOrFutureDate_isAccepted() throws Exception {
        model.addPerson(student);
        run("lesson add st/1 d/today t/09:00 past/");
        run("lesson add st/1 d/" + daysFromToday(5) + " t/09:00 past/");
        assertEquals(2, model.getAddressBook().getLessonList().size());
    }

    @Test
    public void lessonAdd_flagStillEnforcesEveryOtherRule() throws Exception {
        model.addPerson(student);
        String past = daysFromToday(-2);
        run("lesson add st/1 d/" + past + " t/10:00 past/");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String input : new String[] {
            "lesson add st/1 d/" + past + " t/10:00 past/",
            "lesson add st/1 d/" + past + " t/23:30 dur/60 past/",
            "lesson add st/1 s/Physics d/" + past + " t/12:00 past/",
            "lesson add st/1 d/" + LocalDate.now().plusYears(2).plusDays(1) + " t/12:00 past/",
            "lesson add st/1 d/" + LocalDate.now().minusYears(2).minusDays(1) + " t/12:00 past/"}) {
            assertThrows(CommandException.class, () -> run(input), input);
            assertEquals(before, model.getAddressBook());
        }
        run("archive 1");
        run("list archived/");
        assertThrows(CommandException.class, () -> run("lesson add st/1 d/" + past + " t/14:00 past/"));
    }

    @Test
    public void lessonAdd_oldestAllowedBackdate_isAccepted() throws Exception {
        model.addPerson(student);
        run("lesson add st/1 d/" + LocalDate.now().minusYears(2) + " t/10:00 past/");
        assertEquals(1, model.getAddressBook().getLessonList().size());
    }

    @Test
    public void lessonAdd_flagTakingAValue_isAParseError() {
        model.addPerson(student);
        assertThrows(ParseException.class, () -> run("lesson add st/1 d/today t/10:00 past/yes"));
        assertThrows(ParseException.class, () -> run("lesson add st/1 d/today t/10:00 past/ past/"));
        assertFalse(model.getAddressBook().getLessonList().stream().findAny().isPresent());
    }
}
