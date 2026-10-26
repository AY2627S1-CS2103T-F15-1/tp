package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.BalanceCommand;
import seedu.address.logic.commands.DuesCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.LessonCancelCommand;
import seedu.address.logic.commands.LessonMoveCommand;
import seedu.address.logic.commands.PaymentCommand;
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
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);

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
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))), CLOCK);
    }

    @Test
    public void completePayAndReload_balancesRemainCorrect() throws Exception {
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00");
        logic.execute("lesson done 1");
        logic.execute("lesson done 1 n/Updated notes");
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $50.00"));
        model.addLedgerEntry(LedgerEntry.payment(alice, new BigDecimal("20"), TODAY));
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
        LocalDate today = TODAY;
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
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))), CLOCK);
        restoredLogic.execute("lesson done 1 n/Revised notes after restart");
        assertTrue(restoredLogic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $130.00"));
        assertEquals(2, restored.getAddressBook().getLedgerEntries().size());
        assertEquals(new BigDecimal("50.00"), restored.getAddressBook().getLedgerEntries().get(0).amount());
        assertEquals(new BigDecimal("80.00"), restored.getAddressBook().getLedgerEntries().get(1).amount());
        assertEquals(new BigDecimal("130.00"), Ledger.balance(storage.readAddressBook().orElseThrow(), alice));
    }

    @Test
    public void filteredStudentList_balanceUsesDisplayedIndexButDuesIncludesEveryone() throws Exception {
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00");
        logic.execute("lesson done 1");
        logic.execute("lesson add st/2 d/" + TODAY + " t/12:00");
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
    public void completedLesson_moveAndCancelNameTheStatusEvenThoughItIsBilled() throws Exception {
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00");
        logic.execute("lesson done 1");

        String completedMove = LessonMoveCommand.MESSAGE_COMPLETED_LESSON;
        assertThrows(CommandException.class, completedMove, () -> logic.execute("lesson move 1 t/11:00"));
        String completedCancel = LessonCancelCommand.MESSAGE_COMPLETED_LESSON;
        assertThrows(CommandException.class, completedCancel, () -> logic.execute("lesson cancel 1"));
    }

    @Test
    public void emptyAndCreditAccounts_noOutstandingDues() throws Exception {
        assertEquals(DuesCommand.MESSAGE_NO_DUES, logic.execute("dues").getFeedbackToUser());
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $0.00"));
        model.addLedgerEntry(LedgerEntry.payment(alice, new BigDecimal("100"), TODAY));
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
        model.addLedgerEntry(LedgerEntry.payment(alice, BigDecimal.TEN, TODAY));
        assertTrue(logic.execute("delete 1").getFeedbackToUser().contains("financial event(s)"));
        assertEquals(2, model.getAddressBook().getPersonList().size());
        logic.execute("delete 1 confirm");
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        assertTrue(storage.readAddressBook().orElseThrow().getLedgerEntries().isEmpty());
    }

    @Test
    public void payment_recordsPartialFullAndExcessPaymentsAndPersistsThem() throws Exception {
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00");
        logic.execute("lesson done 1");
        assertTrue(logic.execute("pay st/1 amt/20").getFeedbackToUser().endsWith("OUTSTANDING $30.00."));
        assertTrue(logic.execute("dues").getFeedbackToUser().contains("$30.00 total"));
        assertTrue(logic.execute("pay st/1 amt/30").getFeedbackToUser().endsWith("OUTSTANDING $0.00."));
        assertEquals(DuesCommand.MESSAGE_NO_DUES, logic.execute("dues").getFeedbackToUser());
        assertTrue(logic.execute("pay amt/10.25 st/1").getFeedbackToUser().contains("IN CREDIT $10.25"));
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("IN CREDIT $10.25"));
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertEquals(new BigDecimal("-10.25"), Ledger.balance(saved, alice));
        assertEquals(4, saved.getLedgerEntries().size());
        assertEquals(LedgerEntry.Kind.PAYMENT, saved.getLedgerEntries().getLast().kind());
    }

    @Test
    public void payment_immediatelyAfterCompletion_usesStudentIndexWithoutRefreshingLists() throws Exception {
        // Bob is student 2, but his lesson is lesson 1. No agenda or list command is needed.
        logic.execute("lesson add st/2 d/" + TODAY + " t/10:00 dur/90");
        assertTrue(logic.execute("lesson done 1").getFeedbackToUser().contains("Charge of $120.00 recorded."));
        String payment = logic.execute("pay st/2 amt/120").getFeedbackToUser();

        assertTrue(payment.contains("Payment of $120.00 recorded for Bob"));
        assertTrue(payment.endsWith("OUTSTANDING $0.00."));
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
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00");
        logic.execute("lesson done 1");
        String payment = logic.execute("pay st/1 amt/80").getFeedbackToUser();

        assertTrue(payment.contains("Payment of $80.00 recorded for Bob"));
        assertTrue(payment.endsWith("OUTSTANDING $0.00."));
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
        logic.execute("lesson add st/2 d/" + TODAY + " t/10:00");
        logic.execute("lesson list st/2");
        assertEquals(1, model.getFilteredLessonList().size());

        logic.execute("lesson done 1");
        assertTrue(model.getFilteredLessonList().isEmpty());
        String payment = logic.execute("pay st/2 amt/80").getFeedbackToUser();

        assertTrue(payment.contains("Payment of $80.00 recorded for Bob"));
        assertTrue(payment.endsWith("OUTSTANDING $0.00."));
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
        logic.execute("pay st/1 amt/5");
        assertEquals(new BigDecimal("-5.00"), Ledger.balance(model.getAddressBook(), bob));
        assertEquals(Ledger.ZERO, Ledger.balance(model.getAddressBook(), alice));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(bob, model.getFilteredPersonList().getFirst());
        assertThrows(CommandException.class, () -> logic.execute("pay st/2 amt/5"));
        assertThrows(ParseException.class, () -> logic.execute("pay st/1 amt/0"));
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void payment_failedSaveThenRetry_doesNotRecordPaymentTwice() throws Exception {
        RetryPaymentStorage retryStorage = new RetryPaymentStorage(temporaryFolder.resolve("retry.json"));
        Logic retryLogic = new LogicManager(model, new StorageManager(retryStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))), CLOCK);
        assertThrows(CommandException.class, () -> retryLogic.execute("pay st/1 amt/20"));
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
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00 dur/90");
        assertTrue(logic.execute("lesson done 1").getFeedbackToUser().contains("Charge of $90.00 recorded."));
        logic.execute("edit 1 r/62.51");
        logic.execute("lesson add st/1 d/" + TODAY + " t/12:00 dur/45");
        assertTrue(logic.execute("lesson done 2").getFeedbackToUser().contains("Charge of $46.88 recorded."));
        assertTrue(logic.execute("balance st/1").getFeedbackToUser().contains("OUTSTANDING $136.88"));
        assertEquals(new BigDecimal("136.88"), Ledger.balance(storage.readAddressBook().orElseThrow(), alice));
    }

    @Test
    public void completion_zeroRate_recordsNoChargeAndExplainsWhy() throws Exception {
        logic.execute("edit 1 r/0");
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00");
        String result = logic.execute("lesson done 1").getFeedbackToUser();
        assertTrue(result.contains("No automatic charge: This student's rate is $0.00. Specify an amount with amt/ "
                + "if you want to charge for this lesson. Use charge with an amount."), result);
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        assertEquals(Ledger.ZERO, Ledger.balance(storage.readAddressBook().orElseThrow(), alice));
    }

    @Test
    public void completion_smallFeeRoundingToZero_completesWithoutInvalidCharge() throws Exception {
        logic.execute("edit 1 r/0.01");
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00 dur/15");
        assertTrue(logic.execute("lesson done 1").getFeedbackToUser().contains("lesson fee is $0.00"));
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        logic.execute("lesson add st/1 d/" + TODAY + " t/12:00 dur/30");
        assertTrue(logic.execute("lesson done 2").getFeedbackToUser().contains("Charge of $0.01 recorded."));
        assertEquals(new BigDecimal("0.01"), Ledger.balance(storage.readAddressBook().orElseThrow(), alice));
    }

    @Test
    public void deleteStudentWithHomeworkAndCredit_warnsAboutBothAndCascades() throws Exception {
        logic.execute("pay st/1 amt/10");
        logic.execute("hw add st/1 desc/Algebra due/today");
        String preview = logic.execute("delete 1").getFeedbackToUser();
        assertTrue(preview.contains("homework"));
        assertTrue(preview.contains("financial event(s)"));
        assertTrue(preview.contains("IN CREDIT $10.00"));
        logic.execute("delete 1 confirm");
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertTrue(saved.getLedgerEntries().isEmpty());
        assertTrue(saved.getHomeworkList().isEmpty());
    }

    @Test
    public void pay_optionalDateAndNotes_persistWithExactSpecifiedFeedback() throws Exception {
        logic.execute("lesson add st/2 d/" + TODAY + " t/10:00");
        logic.execute("lesson done 1");
        assertEquals("Payment of $30.00 recorded for Bob on 2026-10-18.\nBalance: OUTSTANDING $50.00.",
                logic.execute("pay st/2 amt/30 d/18/10/2026 n/PayNow ref 4471").getFeedbackToUser());
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertEquals(TODAY.minusDays(1), saved.getLedgerEntries().getLast().date());
        assertEquals("PayNow ref 4471", saved.getLedgerEntries().getLast().notes());
        assertEquals(new BigDecimal("50.00"), Ledger.balance(saved, bob));
        assertEquals(bob.getId(), saved.getLedgerEntries().getLast().studentId());
        assertEquals(alice, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void pay_futureDateOrInvalidNotes_doesNotRecordOrSavePayment() throws Exception {
        assertThrows(CommandException.class, PaymentCommand.MESSAGE_FUTURE_DATE, () ->
                logic.execute("pay st/1 amt/10 d/tmr"));
        assertThrows(ParseException.class, () -> logic.execute("pay st/1 amt/10 n/" + "a".repeat(201)));
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        assertTrue(storage.readAddressBook().isEmpty());
    }

    @Test
    public void pay_usesClockLocalDateEvenWhenUtcDateIsYesterday() throws Exception {
        Clock midnight = Clock.fixed(Instant.parse("2026-10-18T16:30:00Z"), ZoneId.of("Asia/Singapore"));
        Logic fixedLogic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))), midnight);
        assertEquals("Payment of $10.00 recorded for Alice on 2026-10-19.\nBalance: IN CREDIT $10.00.",
                fixedLogic.execute("pay st/1 amt/10").getFeedbackToUser());
        assertEquals(TODAY, storage.readAddressBook().orElseThrow().getLedgerEntries().getFirst().date());
        assertEquals(PaymentCommand.MESSAGE_USAGE + "\n" + HelpCommand.MESSAGE_USER_GUIDE,
                fixedLogic.execute("help pay").getFeedbackToUser());
    }

    @Test
    public void deleteBilledLesson_isRefusedAndTheSavedBookStillLoads() throws Exception {
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00");
        logic.execute("lesson done 1");
        ReadOnlyAddressBook before = storage.readAddressBook().orElseThrow();
        seedu.address.model.lesson.Lesson lesson = model.getAddressBook().getLessonList().getFirst();
        assertThrows(IllegalArgumentException.class, () -> model.deleteLesson(lesson));
        assertThrows(IllegalArgumentException.class, () -> model.deleteLesson(
                new seedu.address.testutil.LessonBuilder(lesson).build().snapshotStudent(alice)));
        storage.saveAddressBook(model.getAddressBook());
        ReadOnlyAddressBook restored = storage.readAddressBook().orElseThrow();
        assertEquals(before, restored);
        assertEquals(new BigDecimal("50.00"), Ledger.balance(restored, alice));
        assertEquals(lesson.getId(), restored.getLedgerEntries().getFirst().lessonId());
    }

    @Test
    public void deleteUnbilledLesson_stillWorksAndSurvivesReload() throws Exception {
        logic.execute("lesson add st/1 d/" + TODAY + " t/10:00");
        model.deleteLesson(model.getAddressBook().getLessonList().getFirst());
        storage.saveAddressBook(model.getAddressBook());
        assertTrue(storage.readAddressBook().orElseThrow().getLessonList().isEmpty());
        assertTrue(storage.readAddressBook().orElseThrow().getLedgerEntries().isEmpty());
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
