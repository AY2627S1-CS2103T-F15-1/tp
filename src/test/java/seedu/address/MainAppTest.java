package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.UnsupportedSchemaException;
import seedu.address.model.AddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.VersionedAddressBookStorage;

class MainAppTest {
    @TempDir
    private Path directory;

    @Test
    void init_futureSchema_refusesStartupAndLeavesOriginalInPlace() throws Exception {
        assertUnsupportedStartup(true, "{\"schemaVersion\":15,\"persons\":[],\"lessons\":[],\"homework\":[1]}");
    }

    @Test
    void init_futureSchemaWithChangedFields_refusesBeforeDecodingOldModel() throws Exception {
        assertUnsupportedStartup(true, "{\"schemaVersion\":15,\"persons\":{\"newRepresentation\":true}}");
    }

    @Test
    void init_futureSchemaWithoutCurrentFields_refusesBeforeDecodingOldModel() throws Exception {
        assertUnsupportedStartup(true, "{\"schemaVersion\":15,\"newDomain\":[]}");
    }

    @Test
    void init_futureSchemaAtLegacyPath_refusesMigrationAndLeavesOriginalInPlace() throws Exception {
        assertUnsupportedStartup(false, "{\"schemaVersion\":15,\"persons\":[],\"lessons\":[]}");
    }

    @Test
    void init_unsupportedOlderSchema_refusesStartup() throws Exception {
        assertUnsupportedStartup(true, "{\"schemaVersion\":13,\"persons\":[],\"lessons\":[]}");
    }

    private void assertUnsupportedStartup(boolean isCurrent, String json) throws Exception {
        Path current = directory.resolve("tutorflow-v14.json");
        Path legacy = directory.resolve("addressbook.json");
        Path source = isCurrent ? current : legacy;
        byte[] original = (json + "\n").getBytes(StandardCharsets.UTF_8);
        Files.write(source, original);
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

        DataLoadingException error = assertThrows(DataLoadingException.class, app::init);
        assertTrue(error.getCause() instanceof UnsupportedSchemaException);
        assertTrue(error.getMessage().contains("Unsupported data schema version"));
        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        assertThrows(IOException.class, () -> app.storage.saveAddressBook(new AddressBook()));
        assertArrayEquals(original, Files.readAllBytes(source));
        assertFalse(Files.exists(isCurrent ? legacy : current));
        try (var files = Files.list(directory)) {
            assertTrue(files.allMatch(source::equals), "No recovery copy or replacement may be created");
        }
    }
}
