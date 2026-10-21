package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.RATE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_MATH;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, Messages.invalidPersonIndex(
                seedu.address.commons.core.index.Index.fromOneBased(9), 0));
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_readOnlyCommands_doesNotCreateDataFile() throws Exception {
        for (String command : new String[] {"help", "list", "today", "find Amy"}) {
            logic.execute(command);
            assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
        }
    }

    @Test
    public void execute_caseOnlyContactAndSubjectEdits_savesExactDisplayValues() throws Exception {
        model.addPerson(new PersonBuilder().withEmail("Alice@Example.com").withSubjects("Math").build());
        JsonAddressBookStorage addressStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        addressStorage.saveAddressBook(model.getAddressBook());
        logic.execute("edit 1 e/alice@example.com");
        assertEquals("alice@example.com", addressStorage.readAddressBook().orElseThrow().getPersonList().getFirst()
                .getEmail().orElseThrow().value);
        logic.execute("edit 1 s/MATH");
        assertEquals("MATH", addressStorage.readAddressBook().orElseThrow().getPersonList().getFirst()
                .getSubjects().iterator().next().value);
        assertFalse(logic.hasUnsavedChanges());
    }

    @Test
    public void execute_caseOnlyEditWriteFailure_retainsDirtyAndGuardsExitUntilSaved() throws Exception {
        for (String edit : new String[] {"edit 1 e/alice@example.com", "edit 1 s/MATH"}) {
            boolean[] fail = {false};
            JsonAddressBookStorage addressStorage = new JsonAddressBookStorage(temporaryFolder.resolve("case.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    if (fail[0]) {
                        throw DUMMY_IO_EXCEPTION;
                    }
                    super.saveAddressBook(addressBook);
                }
            };
            model = new ModelManager();
            model.addPerson(new PersonBuilder().withEmail("Alice@Example.com").withSubjects("Math").build());
            addressStorage.saveAddressBook(model.getAddressBook());
            logic = new LogicManager(model, new StorageManager(addressStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            fail[0] = true;
            assertThrows(CommandException.class, () -> logic.execute(edit));
            assertTrue(logic.hasUnsavedChanges());
            assertThrows(CommandException.class, () -> logic.execute("exit"));
            fail[0] = false;
            assertTrue(logic.execute("exit").isExit());
            Person restored = addressStorage.readAddressBook().orElseThrow().getPersonList().getFirst();
            if (edit.contains("e/")) {
                assertEquals("alice@example.com", restored.getEmail().orElseThrow().value);
            } else {
                assertEquals("MATH", restored.getSubjects().iterator().next().value);
            }
            assertFalse(logic.hasUnsavedChanges());
        }
    }

    @Test
    public void execute_failedSave_retainsDirtyStateUntilExplicitRetry() throws Exception {
        boolean[] fail = {true};
        JsonAddressBookStorage addressStorage = new JsonAddressBookStorage(temporaryFolder.resolve("retry.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (fail[0]) {
                    throw DUMMY_IO_EXCEPTION;
                }
                super.saveAddressBook(addressBook);
            }
        };
        logic = new LogicManager(model, new StorageManager(addressStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        assertThrows(CommandException.class, () -> logic.execute("add" + NAME_DESC_AMY + PHONE_DESC_AMY
                + LEVEL_DESC_AMY + SUBJECT_DESC_MATH + RATE_DESC_AMY));
        assertTrue(logic.hasUnsavedChanges());
        assertTrue(logic.execute("help").getFeedbackToUser().contains("Unsaved changes"));
        assertThrows(CommandException.class, () -> logic.execute("exit"));
        fail[0] = false;
        logic.execute("save");
        assertFalse(logic.hasUnsavedChanges());
        assertEquals(model.getAddressBook(), addressStorage.readAddressBook().orElseThrow());
        assertTrue(logic.execute("exit").isExit());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()) + "\n"
                + LogicManager.UNSAVED_WARNING);
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()) + "\n"
                + LogicManager.UNSAVED_WARNING);
    }

    @Test
    public void getStartupMessage_noWarning_isTheDashboard() {
        assertEquals(logic.getDashboard(), logic.getStartupMessage());
    }

    @Test
    public void getStartupMessage_withWarning_showsWarningBeforeTheDashboard() {
        String warning = "Your data file could not be read.";
        Logic warningLogic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))), warning);

        assertEquals(warning + "\n\n" + warningLogic.getDashboard(), warningLogic.getStartupMessage());
    }

    @Test
    public void getDashboard_showsTodayCommandResult() {
        assertEquals(new TodayCommand(LocalDate.now()).execute(model).getFeedbackToUser(), logic.getDashboard());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + LEVEL_DESC_AMY + SUBJECT_DESC_MATH + RATE_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withoutVenue().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    @Test
    public void markTourSeen_remembersItInTheModel() {
        assertFalse(logic.isTourSeen());

        logic.markTourSeen();

        assertTrue(logic.isTourSeen());
        assertTrue(model.isTourSeen());
    }

    @Test
    public void getFilteredLessonList_isTheListThatLessonCommandsCountFrom() throws Exception {
        model.addPerson(AMY);
        model.addLesson(new LessonBuilder().withStudent(AMY).withDate(LocalDate.now().plusDays(1)).build());
        model.updateFilteredLessonList(Model.PREDICATE_SHOW_ALL_LESSONS);

        assertEquals(model.getFilteredLessonList(), logic.getFilteredLessonList());
        assertEquals(1, logic.getFilteredLessonList().size());
    }

    @Test
    public void getHomeworkRows_followsTheLastHomeworkListAndTheCurrentData() throws Exception {
        model.addPerson(AMY);
        assertEquals(0, logic.getHomeworkRows().size());
        Homework item = new HomeworkBuilder(AMY).withDueDate(LocalDate.now().plusDays(2)).build();
        model.addHomework(item);

        logic.execute("hw list");
        assertEquals(1, logic.getHomeworkRows().size());
        assertEquals("assigned", logic.getHomeworkRows().get(0).status());

        logic.execute("hw status 1 s/submitted");
        assertEquals("submitted", logic.getHomeworkRows().get(0).status());

        logic.execute("hw delete 1");
        assertTrue(logic.getHomeworkRows().get(0).isDeleted());
    }
}
