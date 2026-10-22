package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.DataFileRecovery;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks that contact details, fees and notes never reach the log, at any log level. */
public class LogPrivacyTest {
    private static final List<String> NEEDLES = List.of("quillon zephyrine", "93217654", "zephyr@quillon.example",
            "blk 913 quillon rd", "88.77", "needle-remark", "needle-homework", "needle-feedback");

    @TempDir
    public Path temporaryFolder;

    private final List<String> logged = new ArrayList<>();
    private final Handler capture = new Handler() {
        @Override
        public void publish(LogRecord record) {
            StringBuilder text = new StringBuilder(record.getMessage() == null ? "" : record.getMessage());
            for (Object parameter : record.getParameters() == null ? new Object[0] : record.getParameters()) {
                text.append(' ').append(parameter);
            }
            for (Throwable t = record.getThrown(); t != null; t = t.getCause()) {
                text.append(' ').append(t);
            }
            logged.add(text.toString().toLowerCase(Locale.ENGLISH));
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };
    private Logger base;
    private Level originalLevel;
    private boolean shouldFailWrites;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        LogsCenter.getLogger(LogPrivacyTest.class); // makes sure that the logging is configured
        base = Logger.getLogger("ab3");
        originalLevel = base.getLevel();
        base.setLevel(Level.ALL);
        capture.setLevel(Level.ALL);
        base.addHandler(capture);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("data.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (shouldFailWrites) {
                    throw new IOException("Disk full");
                }
                super.saveAddressBook(addressBook);
            }
        };
        logic = new LogicManager(new ModelManager(), new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @AfterEach
    public void tearDown() {
        base.removeHandler(capture);
        base.setLevel(originalLevel);
    }

    private void run(String... commands) {
        for (String command : commands) {
            try {
                logic.execute(command);
            } catch (CommandException | ParseException e) {
                // rejected commands take part in the check as well
                assertFalse(e.getMessage().isEmpty());
            }
        }
    }

    private void assertNothingPrivateWasLogged() {
        assertFalse(logged.isEmpty(), "The check is only meaningful if something was logged");
        for (String entry : logged) {
            for (String needle : NEEDLES) {
                assertFalse(entry.contains(needle), "The log must not contain '" + needle + "': " + entry);
            }
        }
    }

    @Test
    public void session_ofStudentAndHomeworkCommands_logsNoPersonalDetails() {
        run("add n/Quillon Zephyrine p/93217654 e/zephyr@quillon.example l/S3 s/Math r/88.77 v/Blk 913 Quillon Rd",
                "edit 1 r/88.77 e/zephyr@quillon.example", "remark 1 r/needle-remark", "find Quillon", "list",
                "view 1", "hw add st/1 desc/needle-homework due/tmr", "hw list", "hw status 1 s/submitted",
                "hw feedback 1 f/needle-feedback", "hw edit 1 desc/needle-homework due/tmr", "hw delete 1 confirm",
                "archive 1", "list archived/", "restore 1", "today", "save");

        assertNothingPrivateWasLogged();
    }

    @Test
    public void session_ofRejectedCommands_logsNoPersonalDetails() {
        run("add n/Quillon Zephyrine p/93217654 l/S3 s/Math r/88.77", "add n/Quillon Zephyrine p/93217654 l/S3 "
                + "s/Math r/88.77 e/zephyr@quillon.example v/Blk 913 Quillon Rd x/needle-remark",
                "add n/Quillon Zephyrine p/93217654 l/S3 s/Math r/88.77 e/zephyr@quillon.example",
                "edit 9 n/Quillon Zephyrine", "hw add st/9 desc/needle-homework due/tmr",
                "hw feedback 1 f/needle-feedback", "frobnicate 93217654", "find");

        assertNothingPrivateWasLogged();
    }

    @Test
    public void session_whenSavingFails_logsNoPersonalDetails() {
        shouldFailWrites = true;
        run("add n/Quillon Zephyrine p/93217654 e/zephyr@quillon.example l/S3 s/Math r/88.77 v/Blk 913 Quillon Rd",
                "save", "exit");

        assertTrue(logic.hasUnsavedChanges());
        assertNothingPrivateWasLogged();
    }

    @Test
    public void recovery_ofAnUnreadableFile_logsNoFileContents() throws IOException {
        Path dataFile = temporaryFolder.resolve("unreadable.json");
        Files.writeString(dataFile, "{\"name\":\"Quillon Zephyrine\",\"phone\":\"93217654\" oops");

        DataFileRecovery.setAside(dataFile);

        assertNothingPrivateWasLogged();
    }
}
