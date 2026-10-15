package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Exercises scheduling, viewing, rescheduling and cancelling lessons through the application's parser, model and
 * real JSON storage, the way a tutor would type the commands.
 */
public class LessonSchedulingIntegrationTest {

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
    public void execute_scheduleViewMoveAndCancel_everyStepIsSavedAndNumbersMatchTheAgenda() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/16:30 dur/90");
        String warning = logic.execute("lesson add st/2 s/Physics d/" + DAY + " t/17:00 dur/60")
                .getFeedbackToUser();
        assertTrue(warning.contains("Warning: this overlaps with " + ALICE.getName()));

        String agenda = logic.execute("agenda d/" + DAY).getFeedbackToUser();
        assertEquals(2, agenda.lines().filter(line -> line.endsWith("overlaps")).count());
        assertTrue(agenda.contains(" 2. 17:00-18:00  " + BENSON.getName()));

        // lesson 2 in the agenda is Benson's, and moving it clears the overlap
        String moved = logic.execute("lesson move 2 t/18:30").getFeedbackToUser();
        assertFalse(moved.contains("Warning"));
        assertEquals(DAY, savedLessons().get(1).getDate().value);
        assertEquals("18:30", savedLessons().get(1).getTime().toString());

        logic.execute("lesson cancel 1 r/Student unwell");
        Lesson cancelled = savedLessons().get(0);
        assertEquals(LessonStatus.CANCELLED, cancelled.getStatus());
        assertEquals("Student unwell", cancelled.getCancelReason().orElseThrow());

        // a restart keeps the lessons, and a cancelled lesson no longer counts as an overlap
        Model restored = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        String restoredAgenda = createLogic(restored).execute("agenda d/" + DAY).getFeedbackToUser();
        assertTrue(restoredAgenda.contains("(1 lesson, 1h)"));
        assertFalse(restoredAgenda.contains("overlaps"));
    }

    @Test
    public void execute_addingALessonAfterTheAgenda_doesNotShiftTheShownNumbers() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/10:00");
        logic.execute("lesson add st/2 s/Physics d/" + DAY + " t/12:00");
        logic.execute("agenda d/" + DAY);

        // a lesson on another day is added, but the agenda on screen is unchanged
        logic.execute("lesson add st/1 d/" + DAY.minusDays(3) + " t/09:00");
        logic.execute("lesson cancel 2");

        assertEquals(LessonStatus.CANCELLED, savedLessons().get(1).getStatus());
        assertEquals(LessonStatus.SCHEDULED, savedLessons().get(2).getStatus());
    }

    @Test
    public void execute_lessonNumberBeforeAnyAgenda_isRejectedAtStartup() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/10:00");
        model.updateFilteredLessonList(unused -> false);

        assertThrows(CommandException.class, () -> logic.execute("lesson cancel 1"));
    }

    @Test
    public void execute_lessonListThenMove_usesTheNumbersOfLessonList() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY.plusDays(2) + " t/10:00");
        logic.execute("lesson add st/1 d/" + DAY + " t/10:00");
        logic.execute("lesson list st/1");

        logic.execute("lesson move 1 v/Online");

        // the earlier lesson is number 1 in the list, whatever order the lessons were added in
        assertEquals(DAY, savedLessons().get(1).getDate().value);
        assertEquals("Online", savedLessons().get(1).getVenue().orElseThrow().value);
    }

    @Test
    public void execute_deleteStudentWithLessons_removesTheirLessonsFromSavedData() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/10:00");
        logic.execute("lesson add st/2 s/Physics d/" + DAY + " t/12:00");

        logic.execute("delete 1 confirm");

        assertEquals(1, savedLessons().size());
        assertEquals(BENSON.getName(), savedLessons().get(0).getStudentName());
    }

    @Test
    public void execute_editStudentName_keepsTheirLessons() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/10:00");

        logic.execute("edit 1 n/Alice Tan");

        assertEquals(1, savedLessons().size());
        assertEquals("Alice Tan", savedLessons().get(0).getStudentName().fullName);
    }

    @Test
    public void execute_bookAgainAfterCancelling_succeedsAndKeepsTheCancelledRecord() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/10:00");
        logic.execute("lesson cancel 1 r/Student unwell");

        logic.execute("lesson add st/1 d/" + DAY + " t/10:00");

        assertEquals(2, savedLessons().size());
        assertEquals(LessonStatus.CANCELLED, savedLessons().get(0).getStatus());
        assertEquals(LessonStatus.SCHEDULED, savedLessons().get(1).getStatus());
        // both are still readable after a restart
        assertEquals(2, addressBookStorage.readAddressBook().orElseThrow().getLessonList().size());
        // but two live bookings of the slot are still rejected
        assertThrows(CommandException.class, () -> logic.execute("lesson add st/1 d/" + DAY + " t/10:00"));
    }

    @Test
    public void execute_rejectedCommands_leaveSavedLessonsUnchanged() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/10:00");
        List<Lesson> before = savedLessons();

        assertThrows(CommandException.class, () -> logic.execute("lesson add st/1 d/" + DAY + " t/10:00"));
        assertThrows(CommandException.class, () -> logic.execute("lesson add st/1 d/2020-01-01 t/10:00"));
        assertThrows(CommandException.class, () -> logic.execute("lesson move 5 t/11:00"));
        assertThrows(CommandException.class, () -> logic.execute("lesson cancel 5"));

        assertEquals(before, savedLessons());
    }

    private List<Lesson> savedLessons() throws Exception {
        return List.copyOf(addressBookStorage.readAddressBook().orElseThrow().getLessonList());
    }

    private Logic createLogic(Model targetModel) {
        return new LogicManager(targetModel, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userprefs.json"))));
    }
}
