package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;

public class WithdrawCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);

    private static WithdrawCommand withdraw(Index student, String amount) {
        return new WithdrawCommand(student, new BigDecimal(amount), null, null, CLOCK);
    }

    @Test
    public void constructor_invalidArguments_rejected() {
        assertThrows(NullPointerException.class, () -> withdraw(null, "10"));
        assertThrows(NullPointerException.class, () -> new WithdrawCommand(INDEX_FIRST_PERSON, null,
                null, null, CLOCK));
        for (String amount : new String[]{"0", "0.00", "-1", "1.001"}) {
            assertThrows(IllegalArgumentException.class, () -> withdraw(INDEX_FIRST_PERSON, amount));
        }
        assertThrows(IllegalArgumentException.class, () -> new WithdrawCommand(INDEX_FIRST_PERSON,
                BigDecimal.TEN, TODAY, "line\nbreak", CLOCK));
    }

    @Test
    public void execute_validWithdrawal_recordsEntryAndReducesCredit() throws Exception {
        Model model = creditModel("20.00");

        CommandResult result = withdraw(INDEX_FIRST_PERSON, "5.25").execute(model);

        assertEquals("Withdrawal of $5.25 recorded for " + ALICE.getName() + " on " + TODAY
                + ".\nBalance: IN CREDIT $14.75.", result.getFeedbackToUser());
        assertEquals(new BigDecimal("-14.75"), Ledger.balance(model.getAddressBook(), ALICE));
        assertEquals(LedgerEntry.Kind.WITHDRAWAL, model.getAddressBook().getLedgerEntries().getLast().kind());
    }

    @Test
    public void execute_withdrawAllCredit_leavesZeroBalance() throws Exception {
        Model model = creditModel("20.00");

        withdraw(INDEX_FIRST_PERSON, "20").execute(model);

        assertEquals(Ledger.ZERO, Ledger.balance(model.getAddressBook(), ALICE));
    }

    @Test
    public void execute_moreThanAvailableCredit_rejectedWithoutRecording() {
        Model model = creditModel("20.00");

        CommandException exception = assertThrows(CommandException.class, () ->
                withdraw(INDEX_FIRST_PERSON, "20.01").execute(model));

        assertEquals("The withdrawal exceeds " + ALICE.getName() + "'s available credit of $20.00.",
                exception.getMessage());
        assertEquals(new BigDecimal("-20.00"), Ledger.balance(model.getAddressBook(), ALICE));
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_debtOrZeroCredit_rejectedWithoutRecording() {
        for (String balance : new String[]{"0", "10.00"}) {
            Model model = new ModelManager();
            model.addPerson(ALICE);
            if (!balance.equals("0")) {
                Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(TODAY).build();
                model.addLesson(lesson);
                model.addLedgerEntry(LedgerEntry.charge(ALICE, lesson, new BigDecimal(balance)));
            }

            CommandException exception = assertThrows(CommandException.class, () ->
                    withdraw(INDEX_FIRST_PERSON, "1").execute(model));

            assertTrue(exception.getMessage().startsWith(ALICE.getName() + " has no credit to withdraw."));
            assertEquals("0".equals(balance) ? 0 : 1, model.getAddressBook().getLedgerEntries().size());
        }
    }

    @Test
    public void execute_futureDate_rejectedWithoutRecording() {
        Model model = creditModel("20.00");
        WithdrawCommand command = new WithdrawCommand(INDEX_FIRST_PERSON, BigDecimal.TEN,
                TODAY.plusDays(1), null, CLOCK);

        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));

        assertEquals(WithdrawCommand.MESSAGE_FUTURE_DATE, exception.getMessage());
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_invalidIndexOrNullModel_rejectedWithoutRecording() {
        Model model = creditModel("20.00");
        assertThrows(NullPointerException.class, () -> withdraw(INDEX_FIRST_PERSON, "1").execute(null));
        assertThrows(CommandException.class, () -> withdraw(INDEX_SECOND_PERSON, "1").execute(model));
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_archivedStudentWithCredit_allowed() throws Exception {
        Model model = creditModel("20.00");
        model.setPerson(ALICE, ALICE.withArchived(true));
        model.updateFilteredPersonList(person -> true);

        withdraw(INDEX_FIRST_PERSON, "10").execute(model);

        assertEquals(new BigDecimal("-10.00"), Ledger.balance(model.getAddressBook(), ALICE));
    }

    @Test
    public void equals() {
        WithdrawCommand command = withdraw(INDEX_FIRST_PERSON, "10");
        assertTrue(command.equals(command));
        assertTrue(command.equals(withdraw(INDEX_FIRST_PERSON, "10.00")));
        assertFalse(command.equals(withdraw(INDEX_FIRST_PERSON, "11")));
        assertFalse(command.equals(withdraw(INDEX_SECOND_PERSON, "10")));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new PaymentCommand(INDEX_FIRST_PERSON, BigDecimal.TEN)));
        assertTrue(command.toString().contains("studentIndex"));
    }

    private static Model creditModel(String amount) {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        model.addLedgerEntry(LedgerEntry.payment(ALICE, new BigDecimal(amount), TODAY));
        return model;
    }
}
