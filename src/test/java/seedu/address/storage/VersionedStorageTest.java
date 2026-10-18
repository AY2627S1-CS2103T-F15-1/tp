package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.TypicalPersons;

public class VersionedStorageTest {
    @TempDir
    private Path directory;

    @Test
    public void startup_legacyData_copiesToNewFileWithoutChangingOriginal() throws Exception {
        Path legacy = directory.resolve("addressbook.json");
        Path current = directory.resolve("tutorflow-v14.json");
        new JsonAddressBookStorage(legacy).saveAddressBook(TypicalPersons.getTypicalAddressBook());
        String original = Files.readString(legacy);
        VersionedAddressBookStorage data = new VersionedAddressBookStorage(current, legacy);
        ModelManager model = new ModelManager(data.readAddressBook().orElseThrow(), new UserPrefs());
        assertTrue(data.requiresInitialSave());
        Storage storage = new StorageManager(data, new JsonUserPrefsStorage(directory.resolve("prefs.json")));
        LogicManager logic = new LogicManager(model, storage, "", data.requiresInitialSave());
        assertFalse(logic.hasUnsavedChanges());
        assertTrue(Files.exists(current));
        assertEquals(original, Files.readString(legacy));
        assertEquals(model.getAddressBook(), data.readAddressBook().orElseThrow());
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

}
