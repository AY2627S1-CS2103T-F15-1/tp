package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageSchema;
import seedu.address.storage.VersionedAddressBookStorage;

class MainAppTest {
    @TempDir
    private Path directory;

    @Test
    void init_futureSchema_refusesStartupAndLeavesOriginalInPlace() throws Exception {
        assertUnsupportedStartup(true,
                "{\"schemaVersion\":" + (StorageSchema.CURRENT_VERSION + 1)
                        + ",\"persons\":[],\"lessons\":[],\"homework\":[1]}");
    }

    @Test
    void init_futureSchemaWithChangedFields_refusesBeforeDecodingOldModel() throws Exception {
        assertUnsupportedStartup(true,
                "{\"schemaVersion\":" + (StorageSchema.CURRENT_VERSION + 1)
                        + ",\"persons\":{\"newRepresentation\":true}}");
    }

    @Test
    void init_futureSchemaWithoutCurrentFields_refusesBeforeDecodingOldModel() throws Exception {
        assertUnsupportedStartup(true,
                "{\"schemaVersion\":" + (StorageSchema.CURRENT_VERSION + 1) + ",\"newDomain\":[]}");
    }

    @Test
    void init_futureSchemaAtLegacyPath_refusesMigrationAndLeavesOriginalInPlace() throws Exception {
        assertUnsupportedStartup(false,
                "{\"schemaVersion\":" + (StorageSchema.CURRENT_VERSION + 1)
                        + ",\"persons\":[],\"lessons\":[]}");
    }

    @Test
    void init_unsupportedOlderSchema_refusesStartup() throws Exception {
        assertUnsupportedStartup(true, "{\"schemaVersion\":13,\"persons\":[],\"lessons\":[]}");
    }

    @Test
    void init_futureSchema_saysWhichVersionsThisBuildSupports() throws Exception {
        int future = StorageSchema.CURRENT_VERSION + 1;
        Files.writeString(current(), "{\"schemaVersion\":" + future + ",\"persons\":[],\"lessons\":[]}");

        MainApp app = start();

        assertEquals("Unsupported data schema version: " + future + ". This build supports versions "
                + StorageSchema.MINIMUM_VERSION + " through " + StorageSchema.CURRENT_VERSION + ". "
                + "Open with a compatible TutorFlow build or restore a matching backup. "
                + "The original data file has not been changed.", app.startupRefusal);
    }

    @Test
    void init_noDataFile_startsEmptyWithTheWelcomeAndWritesNothing() throws Exception {
        MainApp app = start();

        assertEquals("Welcome to TutorFlow. Add a student or type demo to preview sample data.",
                app.startupWarning);
        assertTrue(app.logic.getStartupMessage().startsWith(app.startupWarning + "\n\n"));
        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        try (var files = Files.list(directory)) {
            assertEquals(0, files.count(), "Starting must not create a data file");
        }
    }

    @Test
    void init_dataFileDeletedAfterUse_startsEmptyAgainInsteadOfRefillingWithSamples() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(AMY);
        new JsonAddressBookStorage(current()).saveAddressBook(book);
        assertEquals(1, start().model.getAddressBook().getPersonList().size());

        Files.delete(current());
        MainApp app = start();

        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertTrue(app.startupWarning.startsWith("Welcome to TutorFlow."));
    }

    @Test
    void init_malformedDataFile_setsItAsideAndStartsEmpty() throws Exception {
        Files.writeString(current(), "{ this is not json");

        MainApp app = start();

        Path setAside = directory.resolve("tutorflow-v14.json.corrupt");
        assertEquals("Your data file at " + current() + " could not be read and has been set aside as " + setAside
                + ". TutorFlow has started with an empty list. The original is never overwritten in place.",
                app.startupWarning);
        assertEquals("{ this is not json", Files.readString(setAside));
        assertFalse(Files.exists(current()));
        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertTrue(app.logic.getStartupMessage().startsWith(app.startupWarning + "\n\n"));
    }

    @Test
    void init_malformedDataFileWhenAnEarlierOneWasSetAside_numbersTheNewCopy() throws Exception {
        Path first = directory.resolve("tutorflow-v14.json.corrupt");
        Files.writeString(first, "first");
        Files.writeString(current(), "second");

        MainApp app = start();

        assertEquals("first", Files.readString(first));
        assertEquals("second", Files.readString(directory.resolve("tutorflow-v14.json.corrupt.1")));
        assertTrue(app.startupWarning.contains("set aside as " + directory.resolve("tutorflow-v14.json.corrupt.1")));
    }

    @Test
    void init_dataFileWithInvalidValues_isSetAsideRatherThanLoadedPartially() throws Exception {
        Files.writeString(current(), "{\"persons\":[{\"name\":\"\",\"phone\":\"1\"}],\"lessons\":[]}");

        MainApp app = start();

        assertTrue(app.startupWarning.contains("has been set aside as"));
        assertTrue(Files.exists(directory.resolve("tutorflow-v14.json.corrupt")));
        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
    }

    private Path current() {
        return directory.resolve("tutorflow-v14.json");
    }

    private MainApp start() throws Exception {
        Path current = current();
        Path legacy = directory.resolve("addressbook.json");
        MainApp app = new MainApp() {
            @Override
            protected VersionedAddressBookStorage createAddressBookStorage() {
                return new VersionedAddressBookStorage(current, legacy);
            }

            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                return new UserPrefs();
            }
        };
        app.init();
        return app;
    }

    private void assertUnsupportedStartup(boolean isCurrent, String json) throws Exception {
        Path current = current();
        Path legacy = directory.resolve("addressbook.json");
        Path source = isCurrent ? current : legacy;
        byte[] original = (json + "\n").getBytes(StandardCharsets.UTF_8);
        Files.write(source, original);
        MainApp app = start();

        // start() shows this text in a window, so it must be the plain message without a Java class name
        assertTrue(app.startupRefusal.startsWith("Unsupported data schema version: "), app.startupRefusal);
        assertTrue(app.startupRefusal.endsWith("The original data file has not been changed."), app.startupRefusal);
        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        app.stop(); // JavaFX calls stop() after the refusal, when there is no model to take preferences from
        assertThrows(IOException.class, () -> app.storage.saveAddressBook(new AddressBook()));
        assertArrayEquals(original, Files.readAllBytes(source));
        assertFalse(Files.exists(isCurrent ? legacy : current));
        try (var files = Files.list(directory)) {
            assertTrue(files.allMatch(source::equals), "No recovery copy or replacement may be created");
        }
    }
}
