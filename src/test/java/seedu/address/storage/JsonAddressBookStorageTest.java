package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TaskBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_nullJson_throwsDataLoadingException() throws Exception {
        assertInvalidJsonIsRejected("null");
    }

    @Test
    public void readAddressBook_nullStudent_throwsDataLoadingException() throws Exception {
        assertInvalidJsonIsRejected("{\"persons\":[null]}");
    }

    @Test
    public void readAddressBook_nullLesson_throwsDataLoadingException() throws Exception {
        assertInvalidJsonIsRejected("{\"persons\":[],\"lessons\":[null]}");
    }

    @Test
    public void readAddressBook_nullSubject_throwsDataLoadingException() throws Exception {
        assertInvalidJsonIsRejected("{\"persons\":[{\"subjects\":[null]}]}");
    }

    @Test
    public void isAccessFailure_separatesAFileThatCannotBeOpenedFromInvalidContent() throws Exception {
        Path folder = testFolder.resolve("a-folder.json");
        Files.createDirectory(folder);

        Executable readFolder = () -> new JsonAddressBookStorage(folder).readAddressBook();

        DataLoadingException unopened = Assertions.assertThrows(DataLoadingException.class, readFolder);

        assertTrue(JsonAddressBookStorage.isAccessFailure(unopened));
        for (String content : new String[] {"null", "{ not json", "", "[]", "{\"persons\":[null]}",
            "{\"schemaVersion\":99}"}) {
            Path file = testFolder.resolve("content.json");
            Files.writeString(file, content);
            Executable readFile = () -> new JsonAddressBookStorage(file).readAddressBook();
            DataLoadingException invalid = Assertions.assertThrows(DataLoadingException.class, readFile);
            assertFalse(JsonAddressBookStorage.isAccessFailure(invalid), "content: " + content);
        }
    }

    @Test
    public void saveAddressBook_afterTheFileCouldNotBeOpened_refusesAndLeavesItAlone() throws Exception {
        Path folder = testFolder.resolve("a-folder.json");
        Files.createDirectory(folder);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder);
        assertThrows(DataLoadingException.class, storage::readAddressBook);

        Executable save = () -> storage.saveAddressBook(new AddressBook());

        IOException refused = Assertions.assertThrows(IOException.class, save);

        assertEquals(String.format(DataFileRecovery.MESSAGE_NOT_OPENED_SAVE, folder), refused.getMessage());
        assertTrue(Files.isDirectory(folder));
        assertFalse(Files.exists(testFolder.resolve("a-folder.json.corrupt")));
    }

    private void assertInvalidJsonIsRejected(String invalidJson) throws Exception {
        Path file = testFolder.resolve("invalid.json");
        Files.writeString(file, invalidJson);

        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(file).readAddressBook());

        assertEquals(invalidJson, Files.readString(file));
    }

    @Test
    public void readAddressBook_taskWithAnExistingStudent_loadsTheTask() throws Exception {
        Path file = testFolder.resolve("tasks.json");
        AddressBook original = getTypicalAddressBook();
        original.addTask(new TaskBuilder().withId("t").withStudent(ALICE).build());
        new JsonAddressBookStorage(file).saveAddressBook(original);

        AddressBook read = new AddressBook(new JsonAddressBookStorage(file).readAddressBook().get());

        assertEquals(original, read);
        assertTrue(read.getTaskList().get(0).isAbout(ALICE));
    }

    @Test
    public void readAddressBook_orphanTaskInDataFile_throwDataLoadingExceptionAndKeepsTheFile() throws Exception {
        Path file = testFolder.resolve("orphan.json");
        AddressBook original = getTypicalAddressBook();
        original.addTask(new TaskBuilder().withId("t").withStudent(ALICE).build());
        new JsonAddressBookStorage(file).saveAddressBook(original);

        // the tasks are the last list in the file, so the last use of Alice's id is the one in her task
        String json = Files.readString(file);
        String aliceId = ALICE.getId().toString();
        int inTask = json.lastIndexOf(aliceId);
        assertTrue(inTask > json.indexOf(aliceId));
        String orphanJson = json.substring(0, inTask) + UUID.randomUUID() + json.substring(inTask + aliceId.length());
        Files.writeString(file, orphanJson);

        Executable read = () -> new JsonAddressBookStorage(file).readAddressBook();
        DataLoadingException error = Assertions.assertThrows(DataLoadingException.class, read);

        String reason = error.getCause().getMessage();
        assertTrue(reason.contains(JsonSerializableAddressBook.MESSAGE_TASK_WITHOUT_TARGET), reason);
        assertEquals(orphanJson, Files.readString(file));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_nonAsciiNamesAndSubjects_survivesTheRoundTrip() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withName("\u9648\u4f1f\u660e").withPhone("82223333")
                .withSubjects("\u534e\u6587", "Fran\u00e7ais").build());
        original.addPerson(new PersonBuilder().withName("Jos\u00e9 \u00c1lvarez").withPhone("81112222").build());
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        jsonAddressBookStorage.saveAddressBook(original);

        assertEquals(original, new AddressBook(jsonAddressBookStorage.readAddressBook().get()));
        assertTrue(Files.readString(filePath, StandardCharsets.UTF_8).contains("\u9648\u4f1f\u660e"));
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
