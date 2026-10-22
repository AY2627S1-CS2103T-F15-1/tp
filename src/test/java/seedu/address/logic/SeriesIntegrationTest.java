package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Exercises weekly series (user story 9) and back-dated lessons (user story 8) through the parser, model and real
 * JSON storage, the way a tutor would type the commands.
 */
public class SeriesIntegrationTest {

    private static final LocalDate DAY = LocalDate.now().plusDays(10);

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private JsonAddressBookStorage addressBookStorage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = createLogic(model);
    }

    @Test
    public void execute_weeklySeries_isSavedAndRestoredWithLevelAndSeriesId() throws Exception {
        String result = logic.execute("lesson repeat st/1 d/" + DAY + " t/16:00 count/4").getFeedbackToUser();
        assertTrue(result.startsWith("Scheduled 4 weekly occurrences."));

        ReadOnlyAddressBook saved = saved();
        assertEquals(4, saved.getLessonList().size());
        assertEquals(1, saved.getLessonList().stream().map(l -> l.getSeriesId().orElseThrow()).distinct().count());
        assertTrue(saved.getLessonList().stream()
                .allMatch(l -> l.getAcademicLevel().orElseThrow().value.equals(ALICE.getLevel().value)));

        String list = createLogic(new ModelManager(saved, new UserPrefs())).execute("lesson list st/1")
                .getFeedbackToUser();
        assertTrue(list.contains("[Level at booking: " + ALICE.getLevel().value + "]"));
        assertEquals(4, list.lines().filter(line -> line.contains("scheduled")).count());
    }

    @Test
    public void execute_movingOrCancellingAnOccurrence_keepsEveryOccurrenceInTheSeries() throws Exception {
        logic.execute("lesson repeat st/1 d/" + DAY + " t/16:00 count/3");
        UUID series = saved().getLessonList().get(0).getSeriesId().orElseThrow();

        logic.execute("lesson list st/1");
        logic.execute("lesson move 1 d/" + DAY.plusDays(1));
        logic.execute("lesson list st/1");
        logic.execute("lesson cancel 2 r/Unwell");

        assertEquals(3, saved().getLessonList().size());
        assertTrue(saved().getLessonList().stream()
                .allMatch(lesson -> lesson.getSeriesId().equals(Optional.of(series))));
        assertEquals(1, saved().getLessonList().stream().filter(lesson -> lesson.isCancelled()).count());
    }

    @Test
    public void execute_seriesWithADuplicate_addsNothingAndSavesNothingNew() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY.plusWeeks(2) + " t/16:00");

        CommandException e = assertThrows(CommandException.class, ()
                -> logic.execute("lesson repeat st/1 d/" + DAY + " t/16:00 count/4"));

        assertEquals("This exact lesson already exists. No occurrences were added.", e.getMessage());
        assertEquals(1, saved().getLessonList().size());
    }

    @Test
    public void execute_countOutOfRange_isRejectedBeforeAnythingIsAdded() throws Exception {
        assertThrows(ParseException.class, () -> logic.execute("lesson repeat st/1 d/" + DAY + " t/16:00 count/1"));
        assertThrows(ParseException.class, () -> logic.execute("lesson repeat st/1 d/" + DAY + " t/16:00 count/53"));
        assertTrue(model.getAddressBook().getLessonList().isEmpty());
    }

    @Test
    public void execute_tomorrowAndBackdatedLessons_areAccepted() throws Exception {
        logic.execute("lesson add st/1 d/tmr t/16:00");
        logic.execute("lesson add st/1 d/" + LocalDate.now().minusDays(5) + " t/16:00 past/");

        assertThrows(CommandException.class, ()
                -> logic.execute("lesson add st/1 d/" + LocalDate.now().minusDays(6) + " t/16:00"));
        assertEquals(2, saved().getLessonList().size());
    }

    private ReadOnlyAddressBook saved() throws Exception {
        return addressBookStorage.readAddressBook().orElseThrow();
    }

    private Logic createLogic(Model targetModel) {
        return new LogicManager(targetModel, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userprefs.json"))));
    }
}
