package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.BalanceCommand;
import seedu.address.logic.commands.DuesCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class FinanceIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final Person alice = new PersonBuilder().withName("Alice").withRate("50").build();
    private final Person bob = new PersonBuilder().withName("Bob").withPhone("98765432").withRate("80").build();
    private final Model model = new ModelManager();
    private JsonAddressBookStorage storage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model.addPerson(alice);
        model.addPerson(bob);
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void completePayAndReload_balancesRemainCorrect() throws Exception {
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/10:00");
        logic.execute("lesson done 1");
        logic.execute("lesson done 1 n/Updated notes");
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $50.00"));
        model.addLedgerEntry(LedgerEntry.payment(alice, new BigDecimal("20"), LocalDate.now()));
        storage.saveAddressBook(model.getAddressBook());
        String dues = logic.execute("dues").getFeedbackToUser();
        assertTrue(dues.contains("  Alice $30.00 oldest unpaid 0 days"));
        assertTrue(dues.contains("$30.00 total"));
        assertFalse(dues.contains("Bob"));
        assertEquals(new BigDecimal("30.00"), Ledger.balance(storage.readAddressBook().orElseThrow(), alice));
        assertEquals(2, storage.readAddressBook().orElseThrow().getLedgerEntries().size());
    }

    @Test
    public void completeMultipleLessons_addsEachFeeOnceAndPreservesItAfterReload() throws Exception {
        LocalDate today = LocalDate.now();
        logic.execute("lesson add st/1 d/" + today + " t/10:00");
        logic.execute("lesson done 1");
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $50.00"));

        logic.execute("edit 1 r/80");
        logic.execute("lesson add st/1 d/" + today + " t/12:00");
        logic.execute("lesson done 2");
        logic.execute("lesson done 2");
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $130.00"));
        assertTrue(logic.execute("dues").getFeedbackToUser().contains("$130.00 total"));

        Model restored = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic restoredLogic = new LogicManager(restored, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        restoredLogic.execute("lesson done 1 n/Revised notes after restart");
        assertTrue(restoredLogic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $130.00"));
        assertEquals(2, restored.getAddressBook().getLedgerEntries().size());
        assertEquals(new BigDecimal("50.00"), restored.getAddressBook().getLedgerEntries().get(0).amount());
        assertEquals(new BigDecimal("80.00"), restored.getAddressBook().getLedgerEntries().get(1).amount());
        assertEquals(new BigDecimal("130.00"), Ledger.balance(storage.readAddressBook().orElseThrow(), alice));
    }

    @Test
    public void filteredStudentList_balanceUsesDisplayedIndexButDuesIncludesEveryone() throws Exception {
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/10:00");
        logic.execute("lesson done 1");
        logic.execute("lesson add st/2 d/" + LocalDate.now() + " t/12:00");
        logic.execute("lesson done 2");
        logic.execute("find Bob");
        String balance = logic.execute("balance st/1").getFeedbackToUser();
        assertTrue(balance.contains("Balance for Bob"));
        assertTrue(balance.contains("$80.00"));
        String dues = logic.execute("dues").getFeedbackToUser();
        assertTrue(dues.contains("Alice"));
        assertTrue(dues.contains("Bob"));
        assertTrue(dues.contains("$130.00 total"));
        assertEquals(1, model.getFilteredPersonList().size());
        assertThrows(CommandException.class, () -> logic.execute("balance st/2"));
    }

    @Test
    public void emptyAndCreditAccounts_noOutstandingDues() throws Exception {
        assertEquals(DuesCommand.MESSAGE_NO_DUES, logic.execute("dues").getFeedbackToUser());
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $0.00"));
        model.addLedgerEntry(LedgerEntry.payment(alice, new BigDecimal("100"), LocalDate.now()));
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("IN CREDIT $100.00"));
        assertEquals(DuesCommand.MESSAGE_NO_DUES, logic.execute("dues").getFeedbackToUser());
    }

    @Test
    public void invalidArgumentsAndHelp_areHandledByCommandParser() throws Exception {
        for (String command : new String[]{"balance", "balance st/0", "balance st/-1", "balance st/1 extra",
            "balance 1", "balance st/1 st/2", "dues 1"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
        }
        assertEquals(BalanceCommand.MESSAGE_USAGE + "\n" + HelpCommand.MESSAGE_USER_GUIDE,
                logic.execute("help balance").getFeedbackToUser());
        assertEquals(DuesCommand.MESSAGE_USAGE + "\n" + HelpCommand.MESSAGE_USER_GUIDE,
                logic.execute("help dues").getFeedbackToUser());
        assertTrue(logic.execute("help").getFeedbackToUser().contains("balance"));
    }

    @Test
    public void deleteStudentWithOnlyCredit_requiresConfirmationAndRemovesRecords() throws Exception {
        model.addLedgerEntry(LedgerEntry.payment(alice, BigDecimal.TEN, LocalDate.now()));
        assertTrue(logic.execute("delete 1").getFeedbackToUser().contains("charge/payment record(s)"));
        assertEquals(2, model.getAddressBook().getPersonList().size());
        logic.execute("delete 1 confirm");
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        assertTrue(storage.readAddressBook().orElseThrow().getLedgerEntries().isEmpty());
    }

    @Test
    public void payment_recordsPartialFullAndExcessPaymentsAndPersistsThem() throws Exception {
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/10:00");
        logic.execute("lesson done 1");
        assertTrue(logic.execute("payment st/1 a/20").getFeedbackToUser().endsWith("OUTSTANDING $30.00"));
        assertTrue(logic.execute("dues").getFeedbackToUser().contains("$30.00 total"));
        assertTrue(logic.execute("payment st/1 a/30").getFeedbackToUser().endsWith("OUTSTANDING $0.00"));
        assertEquals(DuesCommand.MESSAGE_NO_DUES, logic.execute("dues").getFeedbackToUser());
        assertTrue(logic.execute("payment a/10.25 st/1").getFeedbackToUser().endsWith("IN CREDIT $10.25"));
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().endsWith("IN CREDIT $10.25"));
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertEquals(new BigDecimal("-10.25"), Ledger.balance(saved, alice));
        assertEquals(4, saved.getLedgerEntries().size());
        assertEquals(LedgerEntry.Kind.PAYMENT, saved.getLedgerEntries().getLast().kind());
    }

    @Test
    public void payment_immediatelyAfterCompletion_usesStudentIndexWithoutRefreshingLists() throws Exception {
        // Bob is student 2, but his lesson is lesson 1. No agenda or list command is needed.
        logic.execute("lesson add st/2 d/" + LocalDate.now() + " t/10:00 dur/90");
        assertTrue(logic.execute("lesson done 1").getFeedbackToUser().contains("Charge of $120.00 recorded."));
        String payment = logic.execute("payment st/2 a/120").getFeedbackToUser();

        assertTrue(payment.contains("Recorded payment of $120.00 from Bob"));
        assertTrue(payment.endsWith("OUTSTANDING $0.00"));
        assertEquals(alice, model.getFilteredPersonList().get(0));
        assertEquals(bob, model.getFilteredPersonList().get(1));
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertEquals(Ledger.ZERO, Ledger.balance(saved, bob));
        assertEquals(Ledger.ZERO, Ledger.balance(saved, alice));
        assertEquals(LessonStatus.COMPLETED, saved.getLessonList().getFirst().getStatus());
        assertEquals(2, saved.getLedgerEntries().size());
        assertEquals(bob.getId(), saved.getLedgerEntries().getFirst().studentId());
        assertEquals(bob.getId(), saved.getLedgerEntries().getLast().studentId());
    }

    @Test
    public void payment_immediatelyAfterCompletion_keepsFilteredStudentNumber() throws Exception {
        logic.execute("find Bob");
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/10:00");
        logic.execute("lesson done 1");
        String payment = logic.execute("payment st/1 a/80").getFeedbackToUser();

        assertTrue(payment.contains("Recorded payment of $80.00 from Bob"));
        assertTrue(payment.endsWith("OUTSTANDING $0.00"));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(bob, model.getFilteredPersonList().getFirst());
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertEquals(Ledger.ZERO, Ledger.balance(saved, bob));
        assertEquals(Ledger.ZERO, Ledger.balance(saved, alice));
        assertEquals(2, saved.getLedgerEntries().size());
        assertEquals(bob.getId(), saved.getLedgerEntries().getLast().studentId());
    }

    @Test
    public void payment_afterCompletionHidesLesson_doesNotRequireAnotherAgendaOrList() throws Exception {
        logic.execute("lesson add st/2 d/" + LocalDate.now() + " t/10:00");
        logic.execute("lesson list st/2");
        assertEquals(1, model.getFilteredLessonList().size());

        logic.execute("lesson done 1");
        assertTrue(model.getFilteredLessonList().isEmpty());
        String payment = logic.execute("payment st/2 a/80").getFeedbackToUser();

        assertTrue(payment.contains("Recorded payment of $80.00 from Bob"));
        assertTrue(payment.endsWith("OUTSTANDING $0.00"));
        assertTrue(model.getFilteredLessonList().isEmpty());
        assertEquals(2, model.getFilteredPersonList().size());
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertEquals(Ledger.ZERO, Ledger.balance(saved, bob));
        assertEquals(2, saved.getLedgerEntries().size());
        assertEquals(bob.getId(), saved.getLedgerEntries().getLast().studentId());
    }

    @Test
    public void payment_filteredIndexTargetsDisplayedStudentAndKeepsFilter() throws Exception {
        logic.execute("find Bob");
        logic.execute("payment st/1 a/5");
        assertEquals(new BigDecimal("-5.00"), Ledger.balance(model.getAddressBook(), bob));
        assertEquals(Ledger.ZERO, Ledger.balance(model.getAddressBook(), alice));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(bob, model.getFilteredPersonList().getFirst());
        assertThrows(CommandException.class, () -> logic.execute("payment st/2 a/5"));
        assertThrows(ParseException.class, () -> logic.execute("payment st/1 a/0"));
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void payment_failedSaveThenRetry_doesNotRecordPaymentTwice() throws Exception {
        RetryPaymentStorage retryStorage = new RetryPaymentStorage(temporaryFolder.resolve("retry.json"));
        Logic retryLogic = new LogicManager(model, new StorageManager(retryStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        assertThrows(CommandException.class, () -> retryLogic.execute("payment st/1 a/20"));
        assertTrue(retryLogic.hasUnsavedChanges());
        assertEquals(new BigDecimal("-20.00"), Ledger.balance(model.getAddressBook(), alice));
        retryStorage.failSave = false;
        retryLogic.execute("save");
        assertFalse(retryLogic.hasUnsavedChanges());
        assertEquals(1, retryStorage.readAddressBook().orElseThrow().getLedgerEntries().size());
        assertEquals(new BigDecimal("-20.00"), Ledger.balance(retryStorage.readAddressBook().orElseThrow(), alice));
    }

    @Test
    public void completion_hourlyFeeAndRounding_areConfirmedAndPersisted() throws Exception {
        logic.execute("edit 1 r/60");
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/10:00 dur/90");
        assertTrue(logic.execute("lesson done 1").getFeedbackToUser().contains("Charge of $90.00 recorded."));
        logic.execute("edit 1 r/62.51");
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/12:00 dur/45");
        assertTrue(logic.execute("lesson done 2").getFeedbackToUser().contains("Charge of $46.88 recorded."));
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $136.88"));
        assertEquals(new BigDecimal("136.88"), Ledger.balance(storage.readAddressBook().orElseThrow(), alice));
    }

    @Test
    public void completion_zeroRate_recordsNoChargeAndExplainsWhy() throws Exception {
        logic.execute("edit 1 r/0");
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/10:00");
        String result = logic.execute("lesson done 1").getFeedbackToUser();
        assertTrue(result.contains("No automatic charge: This student's rate is $0.00."));
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        assertEquals(Ledger.ZERO, Ledger.balance(storage.readAddressBook().orElseThrow(), alice));
    }

    @Test
    public void completion_smallFeeRoundingToZero_completesWithoutInvalidCharge() throws Exception {
        logic.execute("edit 1 r/0.01");
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/10:00 dur/15");
        assertTrue(logic.execute("lesson done 1").getFeedbackToUser().contains("calculated fee rounds to $0.00"));
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/12:00 dur/30");
        assertTrue(logic.execute("lesson done 2").getFeedbackToUser().contains("Charge of $0.01 recorded."));
        assertEquals(new BigDecimal("0.01"), Ledger.balance(storage.readAddressBook().orElseThrow(), alice));
    }

    @Test
    public void deleteStudentWithHomeworkAndCredit_warnsAboutBothAndCascades() throws Exception {
        logic.execute("payment st/1 a/10");
        logic.execute("hw add st/1 desc/Algebra due/today");
        String preview = logic.execute("delete 1").getFeedbackToUser();
        assertTrue(preview.contains("homework"));
        assertTrue(preview.contains("charge/payment record(s)"));
        assertTrue(preview.contains("IN CREDIT $10.00"));
        logic.execute("delete 1 confirm");
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertTrue(saved.getLedgerEntries().isEmpty());
        assertTrue(saved.getHomeworkList().isEmpty());
    }

    private static class RetryPaymentStorage extends JsonAddressBookStorage {
        private boolean failSave = true;

        RetryPaymentStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
            if (failSave) {
                throw new IOException("Simulated payment save failure");
            }
            super.saveAddressBook(book);
        }
    }
}
