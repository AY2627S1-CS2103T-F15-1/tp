package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.HomeworkCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class HomeworkJourneyTest {
    @TempDir
    public Path directory;

    private ModelManager model() {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        return new ModelManager(book, new UserPrefs());
    }

    private LogicManager logic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void journey_assignOverdueSubmitFeedbackReviewEditRestart_preservesHistory() throws Exception {
        ModelManager model = model();
        HomeworkCommandParser yesterday = new HomeworkCommandParser(Clock.fixed(
                Instant.now().minusSeconds(86400), ZoneOffset.UTC));
        yesterday.parse("add st/1 desc/Algebra due/today").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("nested/data.json"));
        storage.saveAddressBook(model.getAddressBook());
        LogicManager logic = logic(model, storage);
        CommandResult overdue = logic.execute("hw list overdue/");
        assertTrue(overdue.getFeedbackToUser().contains("OVERDUE"));
        Homework original = overdue.getDisplayedHomework().orElseThrow().getFirst();
        logic.execute("hw status 1 s/submitted");
        logic.execute("hw feedback 1 f/Revisit negative signs");
        logic.execute("hw status 1 s/reviewed");
        logic.execute("hw edit 1 desc/Algebra correction");
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager afterRestart = logic(restarted, storage);
        assertThrows(CommandException.class, () -> afterRestart.execute("hw status 1 s/assigned"));
        CommandResult history = afterRestart.execute("hw list st/1");
        Homework restored = history.getDisplayedHomework().orElseThrow().getFirst();
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getDueDate(), restored.getDueDate());
        assertEquals(HomeworkStatus.REVIEWED, restored.getStatus());
        assertEquals("Revisit negative signs", restored.getFeedback().orElseThrow());
        assertEquals("Algebra correction", restored.getDescription());
        assertFalse(afterRestart.hasUnsavedChanges());
        assertThrows(UnsupportedOperationException.class, () -> history.getDisplayedHomework().orElseThrow().clear());
        assertEquals(history.getDisplayedHomework(), history.withFeedback("Warning").getDisplayedHomework());
    }

    @Test
    public void mutations_filteredSelectionNeverRetargets_evenAfterSaveFailure() throws Exception {
        ModelManager model = model();
        LocalDate today = LocalDate.now();
        Homework first = Homework.assign(TypicalPersons.ALICE.getId(), "First", today.minusDays(2));
        Homework second = Homework.assign(TypicalPersons.ALICE.getId(), "Second", today.minusDays(1));
        model.addHomework(first);
        model.addHomework(second);
        boolean[] fail = {false};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("failed.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (fail[0]) {
                    throw new IOException("Simulated failure");
                }
                super.saveAddressBook(addressBook);
            }
        };
        storage.saveAddressBook(model.getAddressBook());
        byte[] original = Files.readAllBytes(storage.getAddressBookFilePath());
        LogicManager logic = logic(model, storage);
        logic.execute("hw list outstanding/");
        fail[0] = true;
        assertThrows(CommandException.class, () -> logic.execute("hw add st/1 desc/Added due/today"));
        assertThrows(CommandException.class, () -> logic.execute("hw edit 1 due/tmr"));
        assertThrows(CommandException.class, () -> logic.execute("hw status 1 s/reviewed"));
        assertThrows(CommandException.class, () -> logic.execute("hw feedback 1 f/Retained in session"));
        assertThrows(CommandException.class, () -> logic.execute("edit 1 n/Renamed student"));
        assertEquals(List.of(first.getId(), second.getId()), model.getDisplayedHomework().stream()
                .map(Homework::getId).toList());
        assertThrows(CommandException.class, () -> logic.execute("hw status 2 s/submitted"));
        assertEquals(HomeworkStatus.SUBMITTED, model.getAddressBook().getHomeworkList().get(1).getStatus());
        assertThrows(CommandException.class, () -> logic.execute("hw delete 1 confirm"));
        assertThrows(CommandException.class, () -> logic.execute("hw status 1 s/assigned"));
        assertThrows(CommandException.class, () -> logic.execute("hw status 2 s/reviewed"));
        assertTrue(logic.hasUnsavedChanges());
        assertTrue(java.util.Arrays.equals(original, Files.readAllBytes(storage.getAddressBookFilePath())));
        assertThrows(CommandException.class, () -> logic.execute("exit"));
        fail[0] = false;
        logic.execute("save");
        assertFalse(logic.hasUnsavedChanges());
        ReadOnlyAddressBook restored = storage.readAddressBook().orElseThrow();
        assertEquals(2, restored.getHomeworkList().size());
        assertEquals(second.getId(), restored.getHomeworkList().getFirst().getId());
        assertEquals(HomeworkStatus.REVIEWED, restored.getHomeworkList().getFirst().getStatus());
        assertEquals("Renamed student", restored.getPersonList().getFirst().getName().fullName);
        assertTrue(logic.execute("hw list").getFeedbackToUser().contains("Renamed student"));
    }

    @Test
    public void archiveAndRename_historyUsesCurrentIdentityAndRetainsLink() throws Exception {
        ModelManager model = model();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("archive.json"));
        LogicManager logic = logic(model, storage);
        logic.execute("hw add st/1 desc/Archive work due/today");
        UUID studentId = model.getFilteredPersonList().getFirst().getId();
        Person renamed = new PersonBuilder(TypicalPersons.ALICE).withName("Current name").build();
        model.setPerson(TypicalPersons.ALICE, renamed);
        logic.execute("archive 1");
        logic.execute("student list archived/");
        CommandResult history = logic.execute("hw list st/1");
        assertTrue(history.getFeedbackToUser().contains("Current name (archived)"));
        assertEquals(studentId, history.getDisplayedHomework().orElseThrow().getFirst().getStudentId());
        assertThrows(CommandException.class, () -> logic.execute("hw add st/1 desc/New due/today"));
        logic.execute("hw status 1 s/submitted");
        logic.execute("hw feedback 1 f/Archived history");
        assertEquals("Archived history", storage.readAddressBook().orElseThrow().getHomeworkList().getFirst()
                .getFeedback().orElseThrow());
    }

    @Test
    public void help_allHomeworkCommands_documentsUsage() throws Exception {
        LogicManager logic = logic(model(), new JsonAddressBookStorage(directory.resolve("help.json")));
        for (String operation : new String[] {"add", "list", "edit", "status", "feedback", "delete"}) {
            assertTrue(logic.execute("help hw " + operation).getFeedbackToUser().contains("hw " + operation));
        }
        assertFalse(Files.exists(directory.resolve("help.json")));
    }
}
