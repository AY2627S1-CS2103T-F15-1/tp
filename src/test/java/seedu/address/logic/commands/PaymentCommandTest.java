package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;

public class PaymentCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);

    private static PaymentCommand payment(Index student, BigDecimal amount) {
        return new PaymentCommand(student, amount, null, null, CLOCK);
    }

    @Test
    public void constructor_invalidArguments_rejected() {
        assertThrows(NullPointerException.class, () -> payment(null, BigDecimal.TEN));
        assertThrows(NullPointerException.class, () -> payment(INDEX_FIRST_PERSON, null));
        for (String amount : new String[]{"0", "-1", "1.001"}) {
            assertThrows(IllegalArgumentException.class, () -> payment(INDEX_FIRST_PERSON,
                    new BigDecimal(amount)));
        }
        assertThrows(IllegalArgumentException.class, () -> new PaymentCommand(INDEX_FIRST_PERSON,
                BigDecimal.TEN, TODAY, "line\nbreak", CLOCK));
    }

    @Test
    public void execute_nullModelOrInvalidIndex_doesNotRecordPayment() {
        PaymentCommand command = payment(INDEX_FIRST_PERSON, BigDecimal.TEN);
        assertThrows(NullPointerException.class, () -> command.execute(null));
        Model model = new ModelManager();
        assertThrows(CommandException.class, () -> command.execute(model));
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
    }

    @Test
    public void execute_partialPayment_reducesDebtAndConfirmsNewBalance() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        Lesson lesson = new LessonBuilder().withStudent(ALICE).build();
        model.addLesson(lesson);
        model.addLedgerEntry(new LedgerEntry(UUID.randomUUID(), ALICE.getId(), lesson.getId(),
                LedgerEntry.Kind.CHARGE, new BigDecimal("100.00"), TODAY));

        CommandResult result = payment(INDEX_FIRST_PERSON, new BigDecimal("25.50")).execute(model);

        assertEquals("Payment of $25.50 recorded for " + ALICE.getName() + " on " + TODAY
                + ".\nBalance: OUTSTANDING $74.50.", result.getFeedbackToUser());
        assertEquals(new BigDecimal("74.50"), Ledger.balance(model.getAddressBook(), ALICE));
        assertEquals(LedgerEntry.Kind.PAYMENT, model.getAddressBook().getLedgerEntries().getLast().kind());
        assertEquals(2, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_advancePayment_showsCredit() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        CommandResult result = payment(INDEX_FIRST_PERSON, BigDecimal.TEN).execute(model);
        assertTrue(result.getFeedbackToUser().endsWith("IN CREDIT $10.00."));
        assertEquals(new BigDecimal("-10.00"), Ledger.balance(model.getAddressBook(), ALICE));
    }

    @Test
    public void execute_largeAmount_recordedWithWarning() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        CommandResult result = payment(INDEX_FIRST_PERSON, new BigDecimal("100000000.50")).execute(model);

        assertEquals("Payment of $100000000.50 recorded for " + ALICE.getName() + " on " + TODAY
                + ".\nBalance: IN CREDIT $100000000.50.\n"
                + String.format(PaymentCommand.MESSAGE_LARGE_AMOUNT, "$100000000.50"), result.getFeedbackToUser());
        assertEquals(new BigDecimal("-100000000.50"), Ledger.balance(model.getAddressBook(), ALICE));
    }

    @Test
    public void execute_amountAtWarningThreshold_warnsOnlyFromThreshold() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        String justBelow = payment(INDEX_FIRST_PERSON, new BigDecimal("9999.99")).execute(model)
                .getFeedbackToUser();
        String atThreshold = payment(INDEX_FIRST_PERSON, PaymentCommand.LARGE_AMOUNT).execute(model)
                .getFeedbackToUser();

        assertFalse(justBelow.contains("Warning"), justBelow);
        assertTrue(atThreshold.endsWith(String.format(PaymentCommand.MESSAGE_LARGE_AMOUNT, "$10000.00")),
                atThreshold);
        assertEquals(2, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_twoArgumentConstructor_usesSystemDateAndRecordsPayment() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        CommandResult result = new PaymentCommand(INDEX_FIRST_PERSON, BigDecimal.TEN).execute(model);

        assertTrue(result.getFeedbackToUser().startsWith("Payment of $10.00 recorded for " + ALICE.getName()));
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_rejectedLedgerEntry_reportsCommandErrorWithoutRecordingPayment() {
        Model model = new ModelManager() {
            @Override
            public void addLedgerEntry(LedgerEntry entry) {
                throw new IllegalArgumentException("Ledger student does not exist.");
            }
        };
        model.addPerson(ALICE);
        CommandException error = org.junit.jupiter.api.Assertions.assertThrows(
                CommandException.class, () -> payment(INDEX_FIRST_PERSON, BigDecimal.TEN).execute(model));
        assertEquals("Ledger student does not exist.", error.getMessage());
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
    }

    @Test
    public void execute_futureDate_isRejectedWithoutRecordingPayment() {
        Model model = new ModelManager();
        model.addPerson(ALICE);
        PaymentCommand futurePayment = new PaymentCommand(INDEX_FIRST_PERSON, BigDecimal.TEN,
                TODAY.plusDays(1), null, CLOCK);

        CommandException error = org.junit.jupiter.api.Assertions.assertThrows(
                CommandException.class, () -> futurePayment.execute(model));

        assertEquals(PaymentCommand.MESSAGE_FUTURE_DATE, error.getMessage());
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
    }

    @Test
    public void equals() {
        PaymentCommand command = payment(INDEX_FIRST_PERSON, BigDecimal.TEN);
        assertTrue(command.equals(command));
        assertTrue(command.equals(payment(INDEX_FIRST_PERSON, new BigDecimal("10.00"))));
        assertFalse(command.equals(payment(INDEX_FIRST_PERSON, BigDecimal.ONE)));
        assertFalse(command.equals(payment(INDEX_SECOND_PERSON, BigDecimal.TEN)));
        assertFalse(command.equals(new PaymentCommand(INDEX_FIRST_PERSON, BigDecimal.TEN,
                TODAY.plusDays(1), null, CLOCK)));
        assertFalse(command.equals(new PaymentCommand(INDEX_FIRST_PERSON, BigDecimal.TEN,
                null, "reference", CLOCK)));
        Clock anotherClock = Clock.fixed(Instant.parse("2026-10-20T04:00:00Z"), ZoneId.of("Asia/Singapore"));
        assertFalse(command.equals(new PaymentCommand(INDEX_FIRST_PERSON, BigDecimal.TEN,
                null, null, anotherClock)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new BalanceCommand(INDEX_FIRST_PERSON)));
        assertFalse(command.equals(new PaymentCommand(INDEX_FIRST_PERSON, BigDecimal.TEN,
                TODAY, "different note", CLOCK)));
        assertTrue(command.toString().contains("studentIndex"));
    }
}
