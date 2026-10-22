package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.VersionedAddressBookStorage;

/** Exercises migration restart after a failure inside the real atomic writer. */
public class MigrationAtomicWriteTest {
    @TempDir
    private Path directory;

    @Test
    public void migrate_interruptedInitialWrite_restartRecoversLegacyRecords() throws Exception {
        Path legacy = directory.resolve("addressbook.json");
        Path current = directory.resolve("new-data/tutorflow-v14.json");
        String original = """
                {"persons":[{"name":"Alice Pauline","phone":"94351253","level":"S3",
                  "subjects":["Math"],"rate":"50"}],"lessons":[]}
                """;
        Files.writeString(legacy, original);
        VersionedAddressBookStorage failingStorage = new VersionedAddressBookStorage(current, legacy) {
            @Override
            protected void writeSnapshot(Path path, String snapshot) throws IOException {
                byte[] bytes = snapshot.getBytes(StandardCharsets.UTF_8);
                InputStream interrupted = new InputStream() {
                    private int position;

                    @Override
                    public int read() throws IOException {
                        if (position == 16) {
                            throw new IOException("Interrupted while copying snapshot bytes");
                        }
                        return bytes[position++] & 0xff;
                    }
                };
                FileUtil.writeToFile(path, interrupted);
            }
        };
        ModelManager firstModel = new ModelManager(failingStorage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager firstLogic = new LogicManager(firstModel, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))), "",
                failingStorage.requiresInitialSave());
        assertTrue(firstLogic.hasUnsavedChanges());
        assertFalse(Files.exists(current), "A failed migration must not publish an empty current file");
        assertEquals(original, Files.readString(legacy));
        try (var files = Files.list(current.getParent())) {
            assertEquals(0, files.count(), "Failed writes must also remove the temporary snapshot");
        }

        VersionedAddressBookStorage restartedStorage = new VersionedAddressBookStorage(current, legacy);
        ModelManager restartedModel = new ModelManager(restartedStorage.readAddressBook().orElseThrow(),
                new UserPrefs());
        assertEquals(firstModel.getAddressBook(), restartedModel.getAddressBook());
        assertTrue(restartedStorage.requiresInitialSave());
        LogicManager restartedLogic = new LogicManager(restartedModel, new StorageManager(restartedStorage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))), "",
                restartedStorage.requiresInitialSave());
        assertFalse(restartedLogic.hasUnsavedChanges());
        assertEquals(original, Files.readString(legacy));
        assertEquals(1, restartedStorage.readAddressBook().orElseThrow().getPersonList().size());
    }
}
