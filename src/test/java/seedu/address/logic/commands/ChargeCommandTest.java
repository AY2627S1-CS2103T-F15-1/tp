package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

public class ChargeCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);

    private final Person student = new PersonBuilder().withName("Priya Kumar").withRate("50").build();
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(student);
    }

    private Lesson addLesson(LessonStatus status, String date, int minutes) {
        Lesson lesson = new LessonBuilder().withStudent(student).withStatus(status).withDate(date)
                .withDuration(minutes).build();
        model.addLesson(lesson);
        return lesson;
    }

    private ChargeCommand charge(BigDecimal amount) {
        return new ChargeCommand(INDEX_FIRST_PERSON, amount, false, CLOCK);
    }

    private ChargeCommand reverse() {
        return new ChargeCommand(INDEX_FIRST_PERSON, null, true, CLOCK);
    }

    private BigDecimal balance() {
        return Ledger.balance(model.getAddressBook(), student);
    }

    @Test
    public void constructor_invalidArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new ChargeCommand(null, null));
        assertThrows(IllegalArgumentException.class, () -> charge(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> charge(new BigDecimal("1.001")));
        Executable reversalWithAmount = () -> new ChargeCommand(INDEX_FIRST_PERSON, BigDecimal.TEN, true, CLOCK);
        assertThrows(IllegalArgumentException.class, reversalWithAmount);
    }

    @Test
    public void execute_completedLesson_chargesTheStudentsFee() throws Exception {
        addLesson(LessonStatus.COMPLETED, "2026-10-18", 90);

        CommandResult result = charge(null).execute(model);

        assertEquals("Charge of $75.00 recorded for Priya Kumar — Math, Sun 18 Oct 2026.",
                result.getFeedbackToUser());
        assertEquals(new BigDecimal("75.00"), balance());
    }

    @Test
    public void execute_missedLessonWithAmount_chargesThatAmount() throws Exception {
        addLesson(LessonStatus.MISSED, "2026-10-18", 60);

        charge(new BigDecimal("20")).execute(model);

        assertEquals(new BigDecimal("20.00"), balance());
    }

    @Test
    public void execute_lessonAlreadyCharged_rejected() throws Exception {
        addLesson(LessonStatus.COMPLETED, "2026-10-18", 90);
        charge(null).execute(model);

        CommandException e = assertThrows(CommandException.class, () -> charge(new BigDecimal("5")).execute(model));

        assertEquals("That lesson already has a charge of $75.00.", e.getMessage());
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_cancelledOrScheduledLesson_rejected() {
        Lesson lesson = addLesson(LessonStatus.CANCELLED, "2026-10-18", 60);
        assertEquals("Cannot charge a cancelled lesson.",
                assertThrows(CommandException.class, () -> charge(null).execute(model)).getMessage());

        model.deleteLesson(lesson);
        addLesson(LessonStatus.SCHEDULED, "2026-10-22", 60);
        assertEquals("Cannot charge a lesson scheduled for Thu 22 Oct 2026.",
                assertThrows(CommandException.class, () -> charge(null).execute(model)).getMessage());
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
    }

    @Test
    public void execute_zeroRateWithoutAmount_explainsHowToCharge() throws Exception {
        Person free = new PersonBuilder().withName("Trial Student").withPhone("90000001").withRate("0").build();
        model.addPerson(free);
        model.addLesson(new LessonBuilder().withStudent(free).withStatus(LessonStatus.COMPLETED).build());
        model.updateFilteredLessonList(lesson -> lesson.isWith(free));

        CommandException e = assertThrows(CommandException.class, () -> charge(null).execute(model));
        assertEquals("This student's rate is $0.00. Specify an amount with amt/ if you want to charge for this "
                + "lesson.", e.getMessage());

        charge(BigDecimal.TEN).execute(model);
        assertEquals(new BigDecimal("10.00"), Ledger.balance(model.getAddressBook(), free));
    }

    @Test
    public void execute_invalidLessonIndex_rejected() {
        assertThrows(CommandException.class, () -> charge(null).execute(model));
        assertThrows(CommandException.class, () -> new ChargeCommand(INDEX_SECOND_PERSON, null).execute(model));
    }

    @Test
    public void reverse_activeCharge_keepsTheHistoryAndClearsTheDebt() throws Exception {
        addLesson(LessonStatus.COMPLETED, "2026-10-18", 90);
        charge(null).execute(model);

        CommandResult result = reverse().execute(model);

        assertEquals("Charge of $75.00 reversed for Priya Kumar.", result.getFeedbackToUser());
        assertEquals(Ledger.ZERO, balance());
        assertEquals(2, model.getAddressBook().getLedgerEntries().size());
        LedgerEntry reversal = model.getAddressBook().getLedgerEntries().getLast();
        assertEquals(LedgerEntry.Kind.CHARGE_REVERSAL, reversal.kind());
        assertEquals(TODAY, reversal.date());
    }

    @Test
    public void reverse_withoutActiveCharge_rejected() throws Exception {
        addLesson(LessonStatus.COMPLETED, "2026-10-18", 90);
        String expected = "That lesson has no active charge; it may already have been reversed.";
        assertEquals(expected, assertThrows(CommandException.class, () -> reverse().execute(model)).getMessage());

        charge(null).execute(model);
        reverse().execute(model);
        assertEquals(expected, assertThrows(CommandException.class, () -> reverse().execute(model)).getMessage());
    }

    @Test
    public void charge_afterReversal_isAllowedAndKeepsOneActiveCharge() throws Exception {
        addLesson(LessonStatus.COMPLETED, "2026-10-18", 90);
        charge(null).execute(model);
        reverse().execute(model);

        charge(new BigDecimal("60")).execute(model);

        assertEquals(new BigDecimal("60.00"), balance());
        assertEquals(3, model.getAddressBook().getLedgerEntries().size());
        assertEquals(new BigDecimal("60.00"), Ledger.activeCharge(model.getAddressBook(),
                model.getFilteredLessonList().get(0).getId()).orElseThrow().amount());
        assertFalse(Ledger.dues(model.getAddressBook(), TODAY).isEmpty());
    }

    @Test
    public void reverse_chargePaidAgainst_leavesTheStudentInCredit() throws Exception {
        addLesson(LessonStatus.COMPLETED, "2026-10-18", 90);
        charge(null).execute(model);
        model.addLedgerEntry(LedgerEntry.payment(student, new BigDecimal("75.00"), TODAY));

        reverse().execute(model);

        assertEquals(new BigDecimal("-75.00"), balance());
        assertTrue(Ledger.dues(model.getAddressBook(), TODAY).isEmpty());
    }

    @Test
    public void equalsHashCodeAndToString() {
        ChargeCommand command = charge(BigDecimal.TEN);
        assertEquals(command, charge(new BigDecimal("10.00")));
        assertEquals(command.hashCode(), charge(new BigDecimal("10.00")).hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals(charge(null)));
        assertFalse(charge(null).equals(reverse()));
        assertFalse(command.equals(new ClearCommand()));
        assertTrue(command.toString().contains("isReversal=false"));
    }
}
