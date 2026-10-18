package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

public class ChargeCommandTest {
    private final ModelManager model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();
    private final Person student = new PersonBuilder().withRate("70.01").build();

    private Lesson lesson(LocalDate date, LessonStatus status) {
        if (model.getAddressBook().getPersonList().isEmpty()) {
            model.addPerson(student);
        }
        Lesson lesson = new LessonBuilder().withStudent(student).withDate(date).withStatus(status).build()
                .withStudent(student);
        model.addLesson(lesson);
        return lesson;
    }

    private CommandResult run(String input) throws Exception {
        return parser.parseCommand(input).execute(model);
    }

    @Test
    public void done_roundingNoteCorrectionAndRateEdit_preserveSingleSnapshot() throws Exception {
        Lesson lesson = lesson(LocalDate.now(), LessonStatus.SCHEDULED);
        run("lesson done 1 n/First notes");
        LedgerEntry original = model.getAddressBook().getLedgerEntries().getFirst();
        assertEquals(lesson.getId(), original.lessonId());
        assertEquals(new BigDecimal("105.02"), original.amount());
        model.setPerson(student, new PersonBuilder(student).withRate("100").build());
        run("lesson done 1 n/Corrected notes");
        run("lesson done 1");
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
        assertEquals(original, model.getAddressBook().getLedgerEntries().getFirst());
        run("charge reverse l/1");
        run("lesson done 1 n/Another correction");
        assertEquals(2, model.getAddressBook().getLedgerEntries().size());
        assertThrows(CommandException.class, () -> run("charge reverse l/1"));
        run("charge l/1 amt/80");
        assertEquals(3, model.getAddressBook().getLedgerEntries().size());
        assertEquals(new BigDecimal("80.00"), model.getAddressBook().getLedgerEntries().getLast().amount());
    }

    @Test
    public void manual_chargeThenCancelOrMove_requiresExplicitReversal() throws Exception {
        lesson(LocalDate.now().minusDays(1), LessonStatus.SCHEDULED);
        run("charge l/1 amt/50");
        assertThrows(CommandException.class, () -> run("charge l/1"));
        assertThrows(CommandException.class, () -> run("lesson cancel 1"));
        assertThrows(CommandException.class, () -> run("lesson move 1 d/" + LocalDate.now().plusDays(1)));
        run("charge reverse l/1");
        run("lesson cancel 1");
        assertThrows(CommandException.class, () -> run("charge l/1 amt/10"));
        assertEquals(2, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void charge_futureOrCancelled_rejectsWithoutMutation() throws Exception {
        lesson(LocalDate.now().plusDays(1), LessonStatus.SCHEDULED);
        assertThrows(CommandException.class, () -> run("charge l/1 amt/10"));
        assertThrows(CommandException.class, () -> run("lesson done 1"));
        run("lesson cancel 1");
        assertThrows(CommandException.class, () -> run("charge l/1"));
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
    }

    @Test
    public void charge_missedAndZeroRate_manualOverrideAndNoAutomaticZero() throws Exception {
        lesson(LocalDate.now(), LessonStatus.MISSED);
        model.setPerson(student, new PersonBuilder(student).withRate("0").build());
        assertThrows(CommandException.class, () -> run("charge l/1"));
        assertTrue(run("lesson done 1").getFeedbackToUser().contains("$0.00"));
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        run("charge l/1 amt/25");
        assertEquals(new BigDecimal("25.00"), model.getAddressBook().getLedgerEntries().getFirst().amount());
    }
}
