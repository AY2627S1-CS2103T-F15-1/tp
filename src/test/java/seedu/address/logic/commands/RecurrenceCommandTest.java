package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class RecurrenceCommandTest {
    private final ModelManager model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();
    private final Person student = new PersonBuilder().withSubjects("Math").build();

    private CommandResult run(String input) throws Exception {
        return parser.parseCommand(input).execute(model);
    }

    @Test
    public void repeat_finiteBatch_refreshesIndicesAndRejectsDuplicateAtomically() throws Exception {
        model.addPerson(student);
        run("lesson add st/1 d/" + LocalDate.now().plusWeeks(5) + " t/10:00");
        CommandResult result = run("lesson repeat st/1 d/today t/10:00 count/3 every/2");
        assertEquals(4, model.getAddressBook().getLessonList().size());
        assertTrue(result.getDisplayedLessons().isPresent());
    }

    @Test
    public void repeat_duplicateAtEnd_keepsEntireOriginal() throws Exception {
        model.addPerson(student);
        run("lesson add st/1 d/" + LocalDate.now().plusWeeks(4) + " t/10:00");
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(Exception.class, () -> run("lesson repeat st/1 d/today t/10:00 count/3 every/2"));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void seriesCancel_historyMoveAndChargeGuard_validateWholeBatch() throws Exception {
        model.addPerson(student);
        run("lesson repeat st/1 d/today t/10:00 count/3");
        run("charge l/1 amt/50");
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(Exception.class, () ->
                new LessonSeriesCancelCommand(Index.fromOneBased(3), midnight()).execute(model));
        assertEquals(before, model.getAddressBook());
        run("charge reverse l/1");
        run("lesson done 1");
        run("lesson move 2 d/" + LocalDate.now().plusDays(8));
        assertThrows(Exception.class, () -> run("lesson series cancel 2"));
        new LessonSeriesCancelCommand(Index.fromOneBased(2), midnight()).execute(model);
        assertEquals(LessonStatus.COMPLETED, model.getFilteredLessonList().get(0).getStatus());
        assertEquals(2, model.getAddressBook().getLessonList().stream().filter(Lesson::isCancelled).count());
        assertEquals(2, model.getAddressBook().getLedgerEntries().size());
    }

    private Clock midnight() {
        ZoneId zone = ZoneId.of("Asia/Singapore");
        return Clock.fixed(LocalDate.now().atStartOfDay(zone).toInstant(), zone);
    }

    @Test
    public void seriesCancel_startTimeBoundary_preservesEarlierToday() throws Exception {
        model.addPerson(student);
        run("lesson repeat st/1 d/today t/10:00 count/3");
        ZoneId zone = ZoneId.of("Asia/Singapore");
        Clock afterStart = Clock.fixed(LocalDate.now().atTime(10, 1).atZone(zone).toInstant(), zone);
        new LessonSeriesCancelCommand(Index.fromOneBased(1), afterStart).execute(model);
        assertEquals(LessonStatus.SCHEDULED, model.getFilteredLessonList().getFirst().getStatus());
        assertEquals(2, model.getAddressBook().getLessonList().stream().filter(Lesson::isCancelled).count());
        Clock atStart = Clock.fixed(LocalDate.now().atTime(10, 0).atZone(zone).toInstant(), zone);
        new LessonSeriesCancelCommand(Index.fromOneBased(1), atStart).execute(model);
        assertTrue(model.getAddressBook().getLessonList().stream().allMatch(Lesson::isCancelled));
    }

    @Test
    public void repeat_hiddenLessonList_explainsHowToNumberNewLessons() throws Exception {
        model.addPerson(student);
        model.updateFilteredLessonList(unused -> false);
        CommandResult result = run("lesson repeat st/1 d/tmr t/10:00 count/2");
        assertFalse(result.getFeedbackToUser().contains("No lessons in the current list"));
        assertTrue(result.getFeedbackToUser().contains("lesson list st/1"));
        assertTrue(result.getDisplayedLessons().isEmpty());
        assertTrue(model.getFilteredLessonList().isEmpty());
        assertEquals(2, model.getAddressBook().getLessonList().size());
    }

    @Test
    public void repeat_longOverlappingSeries_summarizesWarnings() throws Exception {
        model.addPerson(student);
        run("lesson repeat st/1 d/tmr t/10:00 count/52");
        String feedback = run("lesson repeat st/1 d/tmr t/10:30 count/52 v/Tampines").getFeedbackToUser();
        assertTrue(feedback.contains("more occurrences overlap"));
        assertTrue(feedback.split("overlaps", -1).length - 1 <= 5);
        assertEquals(104, model.getAddressBook().getLessonList().size());
    }

    @Test
    public void seriesCancel_singleOccurrence_usesSingularWording() throws Exception {
        model.addPerson(student);
        run("lesson repeat st/1 d/tmr t/10:00 count/2");
        run("lesson cancel 2");
        String feedback = run("lesson series cancel 1 confirm").getFeedbackToUser();
        assertTrue(feedback.startsWith("Cancelled 1 series occurrence."));
    }

    @Test
    public void repeat_boundsOverlapAndArchivedStudent_enforceContract() throws Exception {
        model.addPerson(student);
        for (String arguments : new String[] {"count/1", "count/53", "count/2 every/0",
            "count/2 every/13", "count/52 every/12"}) {
            assertThrows(Exception.class, () -> run("lesson repeat st/1 d/today t/10:00 " + arguments));
            assertTrue(model.getAddressBook().getLessonList().isEmpty());
        }
        run("lesson add st/1 d/" + LocalDate.now() + " t/10:30");
        assertTrue(run("lesson repeat st/1 d/today t/10:00 count/2").getFeedbackToUser().contains("overlaps"));
        run("archive 1");
        run("list archived/");
        assertThrows(Exception.class, () -> run("lesson repeat st/1 d/tmr t/10:00 count/2"));
    }
}
