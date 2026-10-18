package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class TravelCommandTest {
    @TempDir
    public Path directory;
    private final ModelManager model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();

    private CommandResult run(String input) throws Exception {
        return parser.parseCommand(input).execute(model);
    }

    private void firstLesson() throws Exception {
        model.addPerson(new PersonBuilder().withSubjects("Math").build());
        run("lesson add st/1 d/" + LocalDate.now() + " t/10:00 dur/60 v/Jurong");
    }

    @Test
    public void travel_gapThresholdAndSetting_warnWithoutBlockingAndPersist() throws Exception {
        firstLesson();
        String result = run("lesson add st/1 d/" + LocalDate.now()
                + " t/11:20 dur/60 v/Tampines").getFeedbackToUser();
        assertTrue(result.contains("Travel warning"));
        assertTrue(result.contains("20 min available; 30 min required"));
        assertEquals(2, model.getAddressBook().getLessonList().size());
        assertTrue(run("agenda").getFeedbackToUser().contains("Travel warning"));
        assertTrue(run("today").getFeedbackToUser().contains("Travel warning"));
        run("travel buffer/20");
        assertFalse(run("agenda").getFeedbackToUser().contains("Travel warning"));
        run("travel buffer/21");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("travel.json"));
        storage.saveAddressBook(model.getAddressBook());
        ModelManager restored = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertTrue(parser.parseCommand("agenda").execute(restored).getFeedbackToUser().contains("21 min required"));
        assertEquals(model.getAddressBook(), restored.getAddressBook());
        assertEquals(model.getAddressBook().hashCode(), restored.getAddressBook().hashCode());
    }

    @Test
    public void travel_sameVenueOnlineOverlapAndMove_handleExceptions() throws Exception {
        firstLesson();
        assertFalse(run("lesson add st/1 d/" + LocalDate.now()
                + " t/11:00 dur/60 v/jurong").getFeedbackToUser().contains("Travel warning"));
        assertFalse(run("lesson move 2 v/Online").getFeedbackToUser().contains("Travel warning"));
        assertTrue(run("lesson move 2 v/Tampines").getFeedbackToUser().contains("Travel warning"));
        String overlap = run("lesson move 2 t/10:30").getFeedbackToUser();
        assertTrue(overlap.contains("overlaps"));
        assertFalse(overlap.contains("Travel warning"));
        run("lesson cancel 1");
        assertFalse(run("agenda").getFeedbackToUser().contains("Travel warning"));
    }

    @Test
    public void travel_invalidBuffer_doesNotMutate() throws Exception {
        run("travel buffer/180");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String value : new String[] {"-1", "181", "1.5", "", "30 buffer/40"}) {
            assertThrows(Exception.class, () -> run("travel buffer/" + value));
            assertEquals(before, model.getAddressBook());
        }
        run("travel buffer/0");
        firstLesson();
        assertFalse(run("lesson add st/1 d/" + LocalDate.now()
                + " t/11:00 v/Tampines").getFeedbackToUser().contains("Travel warning"));
    }
}
