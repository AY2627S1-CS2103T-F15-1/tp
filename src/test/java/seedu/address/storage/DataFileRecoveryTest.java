package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;

public class DataFileRecoveryTest {

    private static final String UNREADABLE_CONTENT = "{ this is not a data file";

    @TempDir
    public Path testFolder;

    @Test
    public void setAside_existingFile_movesItToCorruptFileAndSaysSo() throws Exception {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, UNREADABLE_CONTENT);
        Path setAsideFile = testFolder.resolve("addressbook.json.corrupt");

        String warning = DataFileRecovery.setAside(dataFile);

        assertEquals(String.format(DataFileRecovery.MESSAGE_SET_ASIDE, dataFile, setAsideFile), warning);
        assertFalse(Files.exists(dataFile));
        assertEquals(UNREADABLE_CONTENT, Files.readString(setAsideFile));
    }

    @Test
    public void setAside_corruptFileAlreadyExists_keepsTheEarlierOneToo() throws Exception {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(testFolder.resolve("addressbook.json.corrupt"), "first");
        Files.writeString(dataFile, "second");

        String warning = DataFileRecovery.setAside(dataFile);

        Path secondCopy = testFolder.resolve("addressbook.json.corrupt.1");
        assertTrue(warning.contains(secondCopy.toString()));
        assertEquals("first", Files.readString(testFolder.resolve("addressbook.json.corrupt")));
        assertEquals("second", Files.readString(secondCopy));
    }

    @Test
    public void setAside_fileCannotBeMoved_warnsToCopyItFirst() {
        Path missingFile = testFolder.resolve("missing.json");

        String warning = DataFileRecovery.setAside(missingFile);

        assertTrue(warning.startsWith("Your data file at " + missingFile + " could not be read, and it could not "
                + "be set aside"));
        assertTrue(warning.contains("Copy that file somewhere safe"));
    }

    @Test
    public void startAfreshAfterUnreadableFile_savingDoesNotDestroyTheOriginal() throws Exception {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, UNREADABLE_CONTENT);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        assertThrows(DataLoadingException.class, storage::readAddressBook);

        DataFileRecovery.setAside(dataFile);
        storage.saveAddressBook(getTypicalAddressBook());

        assertEquals(UNREADABLE_CONTENT, Files.readString(testFolder.resolve("addressbook.json.corrupt")));
        assertEquals(getTypicalAddressBook(), new AddressBook(
                storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void save_failedRecoveryThenReleasedLock_preservesOriginalBytesBeforeRetry() throws Exception {
        Path dataFile = testFolder.resolve("protected.json");
        Files.writeString(dataFile, UNREADABLE_CONTENT);
        boolean[] locked = {true};
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile) {
            @Override
            protected void preserveUnreadableFile(Path path) throws IOException {
                if (locked[0]) {
                    throw new IOException("File is locked");
                }
                super.preserveUnreadableFile(path);
            }
        };
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertEquals(UNREADABLE_CONTENT, Files.readString(dataFile));
        locked[0] = false;
        storage.saveAddressBook(getTypicalAddressBook());
        assertEquals(UNREADABLE_CONTENT, Files.readString(testFolder.resolve("protected.json.corrupt")));
        assertEquals(getTypicalAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void save_windowsDeleteLockReleased_preservesLockedOriginal() throws Exception {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return; // File sharing locks are Windows-specific; the portable failure/retry test also runs.
        }
        Path dataFile = testFolder.resolve("locked.json");
        Files.writeString(dataFile, UNREADABLE_CONTENT);
        String script = "$s = [System.IO.File]::Open('" + dataFile.toString().replace("'", "''")
                + "', 'Open', 'Read', 'ReadWrite'); [Console]::WriteLine('locked'); "
                + "[Console]::ReadLine() | Out-Null; $s.Dispose()";
        Process lock = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", script).start();
        try {
            assertEquals("locked", new BufferedReader(new InputStreamReader(lock.getInputStream())).readLine());
            JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertTrue(DataFileRecovery.setAside(dataFile).contains("could not be set aside"));
            assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
            lock.getOutputStream().write('\n');
            lock.getOutputStream().flush();
            assertTrue(lock.waitFor(10, java.util.concurrent.TimeUnit.SECONDS));
            storage.saveAddressBook(getTypicalAddressBook());
            assertEquals(UNREADABLE_CONTENT, Files.readString(testFolder.resolve("locked.json.corrupt")));
        } finally {
            lock.destroyForcibly();
        }
    }
}
