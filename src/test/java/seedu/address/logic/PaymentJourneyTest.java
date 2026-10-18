package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.TypicalPersons;

public class PaymentJourneyTest {
    @TempDir
    public Path directory;
    private final ModelManager model = new ModelManager();

    private LogicManager logic(JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void payments_duplicateCreditReversalRestart_preservesFinancialHistory() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("finance.json"));
        LogicManager logic = logic(storage);
        assertTrue(logic.execute("pay st/1 amt/300 n/PayNow").getFeedbackToUser().contains("IN CREDIT $300.00"));
        logic.execute("pay st/1 amt/300 n/PayNow");
        CommandResult list = logic.execute("pay list st/1");
        List<LedgerEntry> rows = list.getDisplayedPayments().orElseThrow();
        assertEquals(2, rows.size());
        assertNotEquals(rows.getFirst().id(), rows.getLast().id());
        assertEquals(list.getDisplayedPayments(), list.withFeedback("Warning").getDisplayedPayments());
        assertThrows(UnsupportedOperationException.class, rows::clear);
        logic.execute("pay reverse 1");
        assertThrows(CommandException.class, () -> logic.execute("pay reverse 1"));
        assertEquals(new BigDecimal("-300.00"), Ledger.balance(model.getAddressBook(), TypicalPersons.ALICE.getId()));
        ModelManager restored = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook().getLedgerEntries(), restored.getAddressBook().getLedgerEntries());
        assertTrue(restored.getDisplayedPayments().isEmpty());
        assertTrue(logic.execute("pay list").getFeedbackToUser().contains("REVERSED"));
    }

    @Test
    public void selection_backdatedAddRenameAndFailedReverse_neverRetargetsShownRows() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        boolean[] fail = {false};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("failed.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (fail[0]) {
                    throw new IOException("Simulated save failure");
                }
                super.saveAddressBook(book);
            }
        };
        LogicManager logic = logic(storage);
        logic.execute("pay st/1 amt/10");
        logic.execute("pay st/1 amt/20");
        List<LedgerEntry> shown = logic.execute("pay list").getDisplayedPayments().orElseThrow();
        byte[] original = Files.readAllBytes(storage.getAddressBookFilePath());
        fail[0] = true;
        assertThrows(CommandException.class, () -> logic.execute("pay st/1 amt/30 d/2020-01-01"));
        assertThrows(CommandException.class, () -> logic.execute("edit 1 n/Current name"));
        assertThrows(CommandException.class, () -> logic.execute("pay reverse 2"));
        assertTrue(Ledger.isReversed(model.getAddressBook(), shown.get(1).id()));
        assertFalse(Ledger.isReversed(model.getAddressBook(), shown.getFirst().id()));
        assertEquals(shown, model.getDisplayedPayments());
        assertTrue(logic.hasUnsavedChanges());
        assertTrue(java.util.Arrays.equals(original, Files.readAllBytes(storage.getAddressBookFilePath())));
        assertThrows(CommandException.class, () -> logic.execute("exit"));
        fail[0] = false;
        logic.execute("save");
        assertFalse(logic.hasUnsavedChanges());
        assertEquals(model.getAddressBook().getLedgerEntries(),
                storage.readAddressBook().orElseThrow().getLedgerEntries());
        assertTrue(logic.execute("pay list").getFeedbackToUser().contains("Current name"));
    }

    @Test
    public void archivedStudent_paymentsAndCharges_remainUsable() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addLesson(new LessonBuilder().withDate(LocalDate.now()).build().withStudent(TypicalPersons.ALICE));
        LogicManager logic = logic(new JsonAddressBookStorage(directory.resolve("archive.json")));
        logic.execute("archive 1");
        logic.execute("student list archived/");
        logic.execute("charge l/1 amt/100");
        logic.execute("pay st/1 amt/25");
        assertTrue(logic.execute("pay list st/1").getFeedbackToUser().contains("(archived)"));
        assertEquals(new BigDecimal("75.00"), Ledger.balance(model.getAddressBook(), TypicalPersons.ALICE.getId()));
    }

    @Test
    public void payment_invalidAmountsDatesNotesAndPrefixes_rejectBeforeMutation() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        LogicManager logic = logic(new JsonAddressBookStorage(directory.resolve("invalid.json")));
        for (String input : List.of("pay st/1 amt/0", "pay st/1 amt/-1", "pay st/1 amt/1.001",
                "pay st/1 amt/1e2", "pay st/1 amt/1,000", "pay st/1 amt/", "pay st/1 amt/2 amt/3",
                "pay st/1 amt/2 n/", "pay st/1 amt/2 n/" + "x".repeat(201), "pay list extra",
                "pay reverse 1 extra", "pay st/1 amt/2 d/2026-02-30")) {
            assertThrows(ParseException.class, () -> logic.execute(input), input);
        }
        assertThrows(CommandException.class, () -> logic.execute("pay st/1 amt/2 d/tmr"));
        assertThrows(CommandException.class, () -> logic.execute("pay st/2 amt/2"));
        assertThrows(CommandException.class, () -> logic.execute("pay reverse 1"));
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        assertFalse(logic.hasUnsavedChanges());
    }
}
