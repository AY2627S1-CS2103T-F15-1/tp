package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.PersonBuilder;

public class RecurrenceStorageTest {
    @TempDir
    public Path directory;

    @Test
    public void restart_movedOccurrence_preservesSeriesAndCancelsTogether() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withSubjects("Math").build());
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("lesson repeat st/1 d/today t/10:00 count/3").execute(model);
        parser.parseCommand("lesson prep 2 n/Review fractions").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("series.json"));
        storage.saveAddressBook(model.getAddressBook());
        ModelManager restored = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(storage.snapshotAddressBook(model.getAddressBook()),
                storage.snapshotAddressBook(restored.getAddressBook()));
        assertTrue(restored.getAddressBook().getLessonList().stream()
                .allMatch(lesson -> lesson.getSeriesId().isPresent()));
        assertEquals(1, restored.getAddressBook().getLessonList().stream()
                .map(lesson -> lesson.getSeriesId().orElseThrow()).distinct().count());
        parser.parseCommand("lesson series cancel 2 confirm").execute(restored);
        assertTrue(restored.getAddressBook().getLessonList().stream().allMatch(lesson -> lesson.isCancelled()));
        storage.saveAddressBook(restored.getAddressBook());
        assertEquals(restored.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
