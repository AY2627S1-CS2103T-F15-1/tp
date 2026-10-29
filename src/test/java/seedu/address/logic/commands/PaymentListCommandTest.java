package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PaymentListCommandTest {
    private static final LocalDate DAY_ONE = LocalDate.of(2026, 10, 18);
    private static final LocalDate DAY_TWO = LocalDate.of(2026, 10, 19);

    private final Person priya = new PersonBuilder().withName("Priya Kumar").build();
    private final Person ben = new PersonBuilder().withName("Ben Tan").withPhone("91234567")
            .withEmail("ben@example.com").build();
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(priya);
        model.addPerson(ben);
    }

    private LedgerEntry pay(Person student, String amount, LocalDate date, String notes) {
        LedgerEntry entry = LedgerEntry.payment(student, new BigDecimal(amount), date, notes);
        model.addLedgerEntry(entry);
        return entry;
    }

    @Test
    public void execute_noPayments_saysSoAndClearsTheNumbering() throws Exception {
        LedgerEntry old = pay(priya, "10.00", DAY_ONE, null);
        model.setShownPayments(List.of(old));
        model = new ModelManager();
        model.addPerson(priya);

        assertEquals(PaymentListCommand.MESSAGE_NO_PAYMENTS,
                new PaymentListCommand(null).execute(model).getFeedbackToUser());
        assertTrue(model.getShownPayments().isEmpty());
    }

    @Test
    public void execute_noPaymentsForStudent_namesTheStudent() throws Exception {
        pay(priya, "10.00", DAY_ONE, null);

        assertEquals("No payments recorded for Ben Tan.",
                new PaymentListCommand(INDEX_SECOND_PERSON).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_listsOldestFirstWithNotesAndReversedMark() throws Exception {
        LedgerEntry later = pay(priya, "50.00", DAY_TWO, null);
        pay(priya, "100.00", DAY_ONE, "PayNow ref 4471");
        model.addLedgerEntry(LedgerEntry.reversalOf(later, DAY_TWO));

        String result = new PaymentListCommand(null).execute(model).getFeedbackToUser();

        assertEquals("PAYMENTS\n"
                + "1. 2026-10-18 Priya Kumar $100.00 — PayNow ref 4471\n"
                + "2. 2026-10-19 Priya Kumar $50.00 [REVERSED]\n"
                + "Use 'pay reverse INDEX' with these rows. Indices stay fixed until the next 'pay list'.", result);
        assertEquals(2, model.getShownPayments().size());
    }

    @Test
    public void execute_severalStudents_eachRowNamesItsOwnStudentAndOnlyReversedRowsAreMarked() throws Exception {
        pay(priya, "10.00", DAY_ONE, null);
        LedgerEntry bens = pay(ben, "20.00", DAY_ONE, "cash");
        pay(priya, "30.00", DAY_TWO, null);
        model.addLedgerEntry(LedgerEntry.reversalOf(bens, DAY_TWO));

        String result = new PaymentListCommand(null).execute(model).getFeedbackToUser();

        assertEquals("PAYMENTS\n"
                + "1. 2026-10-18 Priya Kumar $10.00\n"
                + "2. 2026-10-18 Ben Tan $20.00 — cash [REVERSED]\n"
                + "3. 2026-10-19 Priya Kumar $30.00\n"
                + "Use 'pay reverse INDEX' with these rows. Indices stay fixed until the next 'pay list'.", result);
    }

    @Test
    public void execute_paymentOfStudentNoLongerInBook_failsWithAMessage() {
        AddressBook ledgerHolder = new AddressBook();
        ledgerHolder.addPerson(priya);
        LedgerEntry orphan = LedgerEntry.payment(priya, new BigDecimal("10.00"), DAY_ONE);
        ledgerHolder.addLedgerEntry(orphan);
        // A book whose ledger names a student that its student list lacks, which the real model never allows.
        AddressBook inconsistent = new AddressBook() {
            @Override
            public ObservableList<Person> getPersonList() {
                return FXCollections.observableArrayList();
            }

            @Override
            public ObservableList<LedgerEntry> getLedgerEntries() {
                return ledgerHolder.getLedgerEntries();
            }
        };
        Model badModel = new ModelManager() {
            @Override
            public ReadOnlyAddressBook getAddressBook() {
                return inconsistent;
            }
        };

        PaymentListCommand listAll = new PaymentListCommand(null);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> listAll.execute(badModel));
        assertTrue(error.getMessage().contains(orphan.id().toString()));
        assertTrue(error.getMessage().contains(priya.getId().toString()));
    }

    @Test
    public void execute_chargesAndReversalsAreNotListed() throws Exception {
        LedgerEntry payment = pay(priya, "20.00", DAY_ONE, null);
        model.addLedgerEntry(LedgerEntry.reversalOf(payment, DAY_ONE));

        assertEquals(List.of(payment), listedPayments());
    }

    private List<LedgerEntry> listedPayments() throws CommandException {
        new PaymentListCommand(null).execute(model);
        return model.getShownPayments();
    }

    @Test
    public void execute_studentFilter_listsOnlyThatStudent() throws Exception {
        pay(priya, "20.00", DAY_ONE, null);
        LedgerEntry bens = pay(ben, "30.00", DAY_ONE, null);

        new PaymentListCommand(INDEX_SECOND_PERSON).execute(model);

        assertEquals(List.of(bens), model.getShownPayments());
    }

    @Test
    public void execute_numbersStayFixedUntilTheNextList() throws Exception {
        LedgerEntry first = pay(priya, "20.00", DAY_ONE, null);
        new PaymentListCommand(null).execute(model);
        pay(priya, "5.00", DAY_ONE.minusDays(5), null); // would sort before the first one in a fresh list

        assertEquals(List.of(first), model.getShownPayments());
    }

    @Test
    public void execute_moreThanTwoHundredPayments_showsFirstTwoHundredAndKeepsAllIndices() throws Exception {
        for (int i = 0; i < PaymentListCommand.MAX_DISPLAYED_PAYMENTS + 1; i++) {
            pay(priya, "1.00", DAY_ONE, null);
        }

        String result = new PaymentListCommand(null).execute(model).getFeedbackToUser();

        assertTrue(result.contains("\n200. "));
        assertFalse(result.contains("\n201. "));
        assertTrue(result.contains(String.format(PaymentListCommand.MESSAGE_TRUNCATED,
                PaymentListCommand.MAX_DISPLAYED_PAYMENTS, PaymentListCommand.MAX_DISPLAYED_PAYMENTS + 1)));
        assertEquals(PaymentListCommand.MAX_DISPLAYED_PAYMENTS + 1, model.getShownPayments().size());
    }

    @Test
    public void execute_invalidStudentIndex_throwsCommandException() {
        Index outOfRange = Index.fromOneBased(3);
        PaymentListCommand command = new PaymentListCommand(outOfRange);

        CommandException e = assertThrows(CommandException.class, () -> command.execute(model));

        assertEquals(Messages.invalidPersonIndex(outOfRange, 2), e.getMessage());
    }

    @Test
    public void equalsAndHashCode() {
        PaymentListCommand all = new PaymentListCommand(null);
        assertEquals(all, new PaymentListCommand(null));
        assertEquals(all.hashCode(), new PaymentListCommand(null).hashCode());
        assertEquals(new PaymentListCommand(INDEX_FIRST_PERSON), new PaymentListCommand(INDEX_FIRST_PERSON));
        assertNotEquals(all, new PaymentListCommand(INDEX_FIRST_PERSON));
        assertNotEquals(new PaymentListCommand(INDEX_FIRST_PERSON), new PaymentListCommand(INDEX_SECOND_PERSON));
        assertFalse(all.equals(null));
        assertFalse(all.equals(1));
        assertTrue(all.toString().contains("studentIndex"));
    }
}
