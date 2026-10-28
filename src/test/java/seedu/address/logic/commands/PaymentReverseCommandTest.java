package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PaymentReverseCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);

    private final Person priya = new PersonBuilder().withName("Priya Kumar").build();
    private Model model;
    private LedgerEntry payment;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(priya);
        payment = LedgerEntry.payment(priya, new BigDecimal("90.00"), TODAY.minusDays(1), null);
        model.addLedgerEntry(payment);
    }

    private PaymentReverseCommand reverse(int oneBased) {
        return new PaymentReverseCommand(Index.fromOneBased(oneBased), CLOCK);
    }

    private void list() throws CommandException {
        new PaymentListCommand(null).execute(model);
    }

    @Test
    public void execute_validIndex_reversesAndKeepsTheOriginalPayment() throws Exception {
        list();

        assertEquals("Payment of $90.00 reversed.", reverse(1).execute(model).getFeedbackToUser());

        List<LedgerEntry> entries = model.getAddressBook().getLedgerEntries();
        assertEquals(2, entries.size());
        assertEquals(payment, entries.get(0));
        assertEquals(LedgerEntry.Kind.PAYMENT_REVERSAL, entries.get(1).kind());
        assertEquals(payment.id(), entries.get(1).reverses());
        assertEquals(TODAY, entries.get(1).date());
        assertEquals(new BigDecimal("0.00"), Ledger.balance(model.getAddressBook(), priya));
    }

    @Test
    public void execute_noListShownYet_throwsInvalidPaymentIndex() {
        CommandException e = assertThrows(CommandException.class, () -> reverse(1).execute(model));
        assertEquals(PaymentReverseCommand.MESSAGE_INVALID_INDEX, e.getMessage());
    }

    @Test
    public void execute_indexOutOfRange_throwsInvalidPaymentIndex() throws Exception {
        list();
        CommandException e = assertThrows(CommandException.class, () -> reverse(2).execute(model));
        assertEquals(PaymentReverseCommand.MESSAGE_INVALID_INDEX, e.getMessage());
    }

    @Test
    public void execute_alreadyReversed_throwsAndAddsNothing() throws Exception {
        list();
        reverse(1).execute(model);

        CommandException e = assertThrows(CommandException.class, () -> reverse(1).execute(model));

        assertEquals(PaymentReverseCommand.MESSAGE_ALREADY_REVERSED, e.getMessage());
        assertEquals(2, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_indicesStayFixedAfterANewPayment() throws Exception {
        list();
        model.addLedgerEntry(LedgerEntry.payment(priya, new BigDecimal("5.00"), TODAY.minusDays(30), null));

        reverse(1).execute(model);

        // row 1 of the list the user saw is still the 90.00 payment, not the older 5.00 one
        assertEquals(payment.id(), model.getAddressBook().getLedgerEntries().getLast().reverses());
    }

    @Test
    public void execute_modelRejectsTheReversal_reportsItAsACommandException() throws Exception {
        Model rejecting = new ModelManager(model.getAddressBook(), new UserPrefs()) {
            @Override
            public void addLedgerEntry(LedgerEntry entry) {
                throw new IllegalArgumentException("The ledger refused this entry.");
            }
        };
        rejecting.setShownPayments(List.of(payment));

        CommandException e = assertThrows(CommandException.class, () -> reverse(1).execute(rejecting));

        assertEquals("The ledger refused this entry.", e.getMessage());
        assertEquals(1, rejecting.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_paymentDisappearedAfterListing_throwsInvalidPaymentIndex() throws Exception {
        list();
        Model cleared = new ModelManager();
        cleared.setShownPayments(model.getShownPayments());

        CommandException e = assertThrows(CommandException.class, () -> reverse(1).execute(cleared));

        assertEquals(PaymentReverseCommand.MESSAGE_INVALID_INDEX, e.getMessage());
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(reverse(1), reverse(1));
        assertEquals(reverse(1).hashCode(), reverse(1).hashCode());
        assertNotEquals(reverse(1), reverse(2));
        assertFalse(reverse(1).equals(null));
        assertFalse(reverse(1).equals(INDEX_FIRST_PERSON));
        assertTrue(reverse(1).toString().contains("paymentIndex"));
        assertEquals(new PaymentReverseCommand(INDEX_SECOND_PERSON), new PaymentReverseCommand(INDEX_SECOND_PERSON));
    }
}
