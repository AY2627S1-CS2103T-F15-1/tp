package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.UnsupportedSchemaException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.TaskBuilder;
import seedu.address.testutil.TypicalPersons;

public class VersionedStorageTest {
    /** The schema written by the build before follow-up tasks were added. */
    private static final int VERSION_BEFORE_TASKS = 16;

    @TempDir
    private Path directory;

    @Test
    public void startup_legacyData_copiesToNewFileWithoutChangingOriginal() throws Exception {
        Path legacy = directory.resolve("addressbook.json");
        Path current = directory.resolve("tutorflow-v14.json");
        Files.writeString(legacy, """
                {
                  "persons": [{"name":"Alice Pauline","phone":"94351253","level":"S3",
                    "subjects":["Math"],"rate":"50"}],
                  "lessons": [
                    {"studentName":"Alice Pauline","studentPhone":"94351253","subject":"Math",
                     "date":"2026-10-12","time":"10:00","duration":60,"status":"cancelled"},
                    {"studentName":"Alice Pauline","studentPhone":"94351253","subject":"Math",
                     "date":"2026-10-12","time":"10:00","duration":60,"status":"cancelled"}
                  ]
                }
                """);
        String original = Files.readString(legacy);
        VersionedAddressBookStorage data = new VersionedAddressBookStorage(current, legacy);
        ModelManager model = new ModelManager(data.readAddressBook().orElseThrow(), new UserPrefs());
        assertTrue(data.requiresInitialSave());
        Storage storage = new StorageManager(data, new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        LogicManager logic = new LogicManager(model, storage, "", data.requiresInitialSave());
        assertFalse(logic.hasUnsavedChanges());
        assertTrue(Files.exists(current));
        assertEquals(original, Files.readString(legacy));
        ReadOnlyAddressBook restored = data.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), restored);
        assertEquals(2, restored.getLessonList().size());
        assertNotEquals(restored.getLessonList().get(0).getId(), restored.getLessonList().get(1).getId());
        assertTrue(restored.getLessonList().get(0).getAcademicLevel().isEmpty());
        assertEquals(restored.getPersonList().getFirst().getId(),
                restored.getLessonList().getFirst().getStudentId().orElseThrow());
    }
    @Test
    public void startup_schema16FileWithoutTasks_loadsWithNoTasks() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        AddressBook book = TypicalPersons.getTypicalAddressBook();
        ObjectNode root = (ObjectNode) new ObjectMapper()
                .readTree(JsonUtil.toJsonString(new JsonSerializableAddressBook(book)));
        root.remove("tasks");
        root.put("schemaVersion", VERSION_BEFORE_TASKS);
        Files.writeString(current, root.toString());

        VersionedAddressBookStorage data = new VersionedAddressBookStorage(current,
                directory.resolve("addressbook.json"));
        ReadOnlyAddressBook loaded = data.readAddressBook().orElseThrow();

        assertTrue(loaded.getTaskList().isEmpty());
        assertEquals(book.getPersonList(), loaded.getPersonList());
        assertFalse(data.requiresInitialSave());
    }

    @Test
    public void save_withTasks_writesSchema19AndABuildThatStopsAt18WouldRefuseIt() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        AddressBook book = TypicalPersons.getTypicalAddressBook();
        book.addTask(new TaskBuilder().build());
        new JsonAddressBookStorage(current).saveAddressBook(book);

        JsonNode saved = new ObjectMapper().readTree(Files.readString(current));
        int savedVersion = saved.get("schemaVersion").intValue();

        assertEquals(VERSION_BEFORE_TASKS + 3, StorageSchema.CURRENT_VERSION);
        assertEquals(StorageSchema.CURRENT_VERSION, savedVersion);
        assertEquals(1, saved.get("tasks").size());
        // A v18 build accepts only versions up to 18 (StorageSchema.requireSupported), so it refuses this file
        // instead of loading it without the tasks and then erasing them on its next save.
        assertTrue(savedVersion > VERSION_BEFORE_TASKS);
        assertEquals(book, new VersionedAddressBookStorage(current, directory.resolve("addressbook.json"))
                .readAddressBook().orElseThrow());
    }

    @Test
    public void startup_schemaNewerThanThisBuild_isRefusedAndTheFileIsLeftUnchanged() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        new JsonAddressBookStorage(current).saveAddressBook(TypicalPersons.getTypicalAddressBook());
        String newer = Files.readString(current).replace("\"schemaVersion\" : " + StorageSchema.CURRENT_VERSION,
                "\"schemaVersion\" : " + (StorageSchema.CURRENT_VERSION + 1));
        Files.writeString(current, newer);

        VersionedAddressBookStorage data = new VersionedAddressBookStorage(current,
                directory.resolve("addressbook.json"));
        DataLoadingException failure = assertThrows(DataLoadingException.class, data::readAddressBook);

        assertTrue(failure.getCause() instanceof UnsupportedSchemaException);
        assertEquals(newer, Files.readString(current));
    }

    @Test
    public void startup_failedMigration_retainsDirtyStateAndRetriesBeforeExit() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        class FailingStorage extends JsonAddressBookStorage {
            private boolean fail = true;

            FailingStorage() {
                super(current);
            }

            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (fail) {
                    throw new IOException("Read only");
                }
                super.saveAddressBook(book);
            }
        }
        FailingStorage data = new FailingStorage();
        Storage storage = new StorageManager(data, new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        LogicManager logic = new LogicManager(new ModelManager(TypicalPersons.getTypicalAddressBook(),
                new UserPrefs()), storage, "", true);
        assertTrue(logic.hasUnsavedChanges());
        assertTrue(logic.getStartupMessage().contains("Migration is pending"));
        assertThrows(CommandException.class, () -> logic.execute("exit"));
        assertTrue(logic.hasUnsavedChanges());
        data.fail = false;
        logic.execute("save");
        assertFalse(logic.hasUnsavedChanges());
        assertTrue(Files.exists(current));
    }

    @Test
    public void startup_existingCurrentData_ignoresLegacyAndDoesNotRequestMigration() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        new JsonAddressBookStorage(current).saveAddressBook(new AddressBook());
        Path legacy = directory.resolve("addressbook.json");
        Files.writeString(legacy, "broken legacy file");
        VersionedAddressBookStorage data = new VersionedAddressBookStorage(current, legacy);
        assertTrue(data.readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertFalse(data.requiresInitialSave());
    }

    @Test
    public void startup_unknownCurrentSource_remainsProtectedWhenMetadataRecovers() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        Files.writeString(current, "original inaccessible bytes");
        class UnknownStorage extends VersionedAddressBookStorage {
            private boolean unknown = true;

            UnknownStorage() {
                super(current, directory.resolve("addressbook.json"));
            }

            @Override
            protected SourcePresence sourcePresence(Path path) {
                return unknown && path.equals(current) ? SourcePresence.UNKNOWN : super.sourcePresence(path);
            }

            @Override
            protected void preserveUnreadableFile(Path path) throws IOException {
                throw new IOException("Recovery copy denied");
            }
        }
        UnknownStorage data = new UnknownStorage();
        assertThrows(DataLoadingException.class, data::readAddressBook);
        data.unknown = false;
        assertThrows(IOException.class, () -> data.saveAddressBook(new AddressBook()));
        assertEquals("original inaccessible bytes", Files.readString(current));
    }

    @Test
    public void startup_invalidLegacySource_blocksSaveUntilOriginalIsPreserved() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        Path legacy = directory.resolve("addressbook.json");
        Files.writeString(legacy, "broken legacy bytes");
        VersionedAddressBookStorage data = new VersionedAddressBookStorage(current, legacy) {
            @Override
            protected void preserveUnreadableFile(Path path) throws IOException {
                throw new IOException("Recovery copy denied");
            }
        };
        assertThrows(DataLoadingException.class, data::readAddressBook);
        assertThrows(IOException.class, () -> data.saveAddressBook(new AddressBook()));
        assertEquals(legacy, data.getReadSource());
        assertFalse(Files.exists(current));
        assertEquals("broken legacy bytes", Files.readString(legacy));
    }

    @Test
    public void startup_legacySourceThatCannotBeOpened_isNeverSetAsideAndNothingIsSaved() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        Path legacy = directory.resolve("addressbook.json");
        Files.createDirectory(legacy); // cannot be read as a file, as a valid file that another program holds
        VersionedAddressBookStorage data = new VersionedAddressBookStorage(current, legacy);

        DataLoadingException failure = assertThrows(DataLoadingException.class, data::readAddressBook);
        IOException refused = assertThrows(IOException.class, () -> data.saveAddressBook(new AddressBook()));

        assertTrue(JsonAddressBookStorage.isAccessFailure(failure));
        assertEquals(String.format(DataFileRecovery.MESSAGE_NOT_OPENED_SAVE, legacy), refused.getMessage());
        assertTrue(Files.isDirectory(legacy), "the source stays where it is");
        assertFalse(Files.exists(directory.resolve("addressbook.json.corrupt")), "it is not set aside");
        assertFalse(Files.exists(current), "no data file is written beside it");
    }

    @Test
    public void startup_legacySourceThatCouldNotBeOpenedBefore_isMigratedOnTheNextStart() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        Path legacy = directory.resolve("addressbook.json");
        Files.createDirectory(legacy);
        assertThrows(DataLoadingException.class, () -> new VersionedAddressBookStorage(current, legacy)
                .readAddressBook());
        Files.delete(legacy);
        Files.writeString(legacy, """
                {"persons": [{"name":"Alice Pauline","phone":"94351253","level":"S3",
                  "subjects":["Math"],"rate":"50"}], "lessons": []}
                """);

        VersionedAddressBookStorage next = new VersionedAddressBookStorage(current, legacy);

        assertEquals(1, next.readAddressBook().orElseThrow().getPersonList().size());
        assertTrue(next.requiresInitialSave());
    }

    @Test
    public void startup_currentFileThatCannotBeOpened_isNeverSetAsideAndNothingIsSaved() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        Files.createDirectory(current);
        VersionedAddressBookStorage data = new VersionedAddressBookStorage(current, directory.resolve("legacy.json"));

        DataLoadingException failure = assertThrows(DataLoadingException.class, data::readAddressBook);

        assertTrue(JsonAddressBookStorage.isAccessFailure(failure));
        assertThrows(IOException.class, () -> data.saveAddressBook(new AddressBook()));
        assertTrue(Files.isDirectory(current));
        assertFalse(Files.exists(directory.resolve("tutorflow-v14.json.corrupt")));
    }

    @Test
    public void startup_invalidLegacySource_successfulPreservationAllowsNewDataset() throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        Path legacy = directory.resolve("addressbook.json");
        Files.writeString(legacy, "broken legacy bytes");
        VersionedAddressBookStorage data = new VersionedAddressBookStorage(current, legacy);
        assertThrows(DataLoadingException.class, data::readAddressBook);
        data.saveAddressBook(new AddressBook());
        assertEquals("broken legacy bytes", Files.readString(directory.resolve("addressbook.json.corrupt")));
        assertTrue(new JsonAddressBookStorage(current).readAddressBook().orElseThrow().getPersonList().isEmpty());
    }

}
