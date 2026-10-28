package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTime;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LessonBuilder;

/** Exercises travel warnings, refreshed indices and series metadata through the real command and storage layers. */
public class LessonTravelWorkflowTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private JsonAddressBookStorage storage;
    private LogicManager logic;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(ALICE);
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("data.json"));
        logic = logic(model, storage);
    }

    @Test
    public void execute_bookingMoveAgendaTodayAndBuffer_reportTravelWithoutBlocking() throws Exception {
        logic.execute("lesson add st/1 s/Math d/" + today + " t/10:00 dur/60 v/Bishan");
        String booked = logic.execute("lesson add st/1 s/Math d/" + today
                + " t/11:15 dur/60 v/Toa Payoh").getFeedbackToUser();
        String warning = "Travel warning: Alice Pauline at Bishan -> Alice Pauline at Toa Payoh ("
                + model.getFilteredLessonList().get(1).getDate() + " 11:15): 15 min available; 30 min required.";
        assertTrue(booked.contains("\n" + warning));
        assertEquals(2, model.getAddressBook().getLessonList().size());
        assertTrue(logic.execute("agenda d/" + today).getFeedbackToUser().contains(warning));
        assertTrue(logic.execute("agenda d/" + today + " week/").getFeedbackToUser()
                .contains(warning));
        assertTrue(logic.execute("today").getFeedbackToUser().contains("\n  " + warning));
        assertFalse(logic.execute("travel buffer/10").getFeedbackToUser().contains("Travel warning:"));
        assertEquals("Travel buffer set to 30 minutes.\n" + warning,
                logic.execute("travel buffer/30").getFeedbackToUser());
        logic.execute("agenda d/" + today);
        assertFalse(logic.execute("lesson move 2 v/Online").getFeedbackToUser().contains("Travel warning:"));
        assertTrue(logic.execute("lesson move 2 v/Cafe").getFeedbackToUser().contains("Travel warning:"));
    }

    @Test
    public void execute_bufferPersistsAndInvalidInputDoesNotWrite() throws Exception {
        assertEquals(30, model.getAddressBook().getTravelBuffer());
        logic.execute("travel buffer/180");
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        assertEquals(180, loaded.getTravelBuffer());
        assertEquals(180, new ModelManager(loaded, new UserPrefs()).getAddressBook().getTravelBuffer());
        assertEquals(180, new AddressBook(loaded).getTravelBuffer());
        String before = Files.readString(storage.getAddressBookFilePath());
        assertThrows(ParseException.class, () -> logic.execute("travel buffer/181"));
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(180, model.getAddressBook().getTravelBuffer());
    }

    @Test
    public void execute_failedSaveRetainsBufferAndRetryPersistsIt() throws Exception {
        boolean[] fails = {true};
        JsonAddressBookStorage failing = new JsonAddressBookStorage(temporaryFolder.resolve("retry.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                if (fails[0]) {
                    throw new IOException("Synthetic write failure");
                }
                super.saveAddressBook(data);
            }
        };
        LogicManager failingLogic = logic(model, failing);
        assertThrows(CommandException.class, () -> failingLogic.execute("travel buffer/75"));
        assertEquals(75, model.getAddressBook().getTravelBuffer());
        assertTrue(failingLogic.hasUnsavedChanges());
        fails[0] = false;
        failingLogic.execute("save");
        assertFalse(failingLogic.hasUnsavedChanges());
        assertEquals(75, failing.readAddressBook().orElseThrow().getTravelBuffer());
    }

    @Test
    public void execute_moveReordersLessons_echoesExactNewNumbers() throws Exception {
        Lesson first = lesson("10:00");
        Lesson second = lesson("12:00");
        model.addLesson(first);
        model.addLesson(second);
        logic.execute("agenda d/" + today);
        String result = logic.execute("lesson move 1 t/14:00").getFeedbackToUser();
        assertNumberingOnce(result);
        assertTrue(result.contains("1. " + Messages.format(second)));
        assertTrue(result.contains("2. " + Messages.format(first.reschedule(first.getDate(),
                new LessonTime("14:00"), first.getDuration(), null))));
        logic.execute("lesson cancel 1");
        Lesson restoredFirst = model.getAddressBook().getLessonList().stream()
                .filter(lesson -> lesson.getId().equals(first.getId())).findFirst().orElseThrow();
        assertTrue(restoredFirst.isScheduled());
        assertTrue(model.getAddressBook().getLessonList().stream()
                .filter(lesson -> lesson.getId().equals(second.getId())).findFirst().orElseThrow().isCancelled());
    }

    @Test
    public void execute_statusChangeRemovesFirstFromUpcomingList_echoesRemainingNumber() throws Exception {
        for (String command : List.of("lesson cancel 1", "lesson done 1", "lesson missed 1")) {
            ModelManager fresh = new ModelManager();
            fresh.addPerson(ALICE);
            fresh.addLesson(lesson("10:00"));
            Lesson second = lesson("12:00");
            fresh.addLesson(second);
            LogicManager freshLogic = logic(fresh, storage);
            freshLogic.execute("lesson list st/1");
            String result = freshLogic.execute(command).getFeedbackToUser();
            assertNumberingOnce(result);
            assertTrue(result.contains("1. " + Messages.format(second)), command);
            assertEquals(List.of(second), fresh.getFilteredLessonList());
        }
    }

    @Test
    public void execute_seriesIdentitySurvivesEditsAndRestartAndCanBeCancelled() throws Exception {
        Lesson original = lesson("10:00")
                .withSeries(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .prepare("Bring worksheets");
        Lesson future = new LessonBuilder().withStudent(ALICE).withDate(today.plusDays(7)).withTime("12:00").build()
                .snapshotStudent(ALICE).withSeries(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"));
        model.addLesson(original);
        model.addLesson(future);
        logic.execute("travel buffer/45");
        ModelManager restoredModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager restored = logic(restoredModel, storage);
        restored.execute("lesson list st/1 all/");
        restored.execute("lesson move 2 d/" + today.plusDays(8));
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        Lesson moved = loaded.getLessonList().get(1);
        assertEquals(future.getId(), moved.getId());
        assertEquals(original.getSeriesId(), moved.getSeriesId());
        assertEquals(future.getAcademicLevel(), moved.getAcademicLevel());
        assertEquals("Bring worksheets", loaded.getLessonList().getFirst().getPreparation().orElseThrow());
        String result = restored.execute("lesson series cancel 1 confirm").getFeedbackToUser();
        assertTrue(result.startsWith("Cancelled "));
        // An all/ list keeps cancelled lessons in place, so no number changes and no new numbering is shown
        assertFalse(result.contains("Lesson numbers have changed"), result);
        assertTrue(storage.readAddressBook().orElseThrow().getLessonList().get(1).isCancelled());
        assertEquals(45, storage.readAddressBook().orElseThrow().getTravelBuffer());
    }

    @Test
    public void execute_moveOutsideDayFilter_echoesEmptyListInsteadOfLeavingStaleNumbers() throws Exception {
        model.addLesson(lesson("10:00"));
        logic.execute("agenda d/" + today);
        String result = logic.execute("lesson move 1 d/" + today.plusDays(1)).getFeedbackToUser();
        assertNumberingOnce(result);
        assertTrue(result.contains("No lessons in the current list."));
        assertTrue(model.getFilteredLessonList().isEmpty());
    }

    @Test
    public void execute_weeklyAgenda_excludesTravelWarningsBeforeAndAfterItsWeek() throws Exception {
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (LocalDate date : List.of(monday.minusDays(1), today, monday.plusDays(7))) {
            model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(date).withTime("10:00")
                    .withDuration(60).withVenue("Home").build());
            model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(date).withTime("11:15")
                    .withDuration(60).withVenue("Cafe").build());
        }
        String agenda = logic.execute("agenda d/" + today + " week/").getFeedbackToUser();
        assertEquals(1, agenda.split("Travel warning:", -1).length - 1);
    }

    @Test
    public void execute_moveUnrelatedLessonToAnotherDate_doesNotRepeatExistingWarnings() throws Exception {
        for (LocalDate date : List.of(today, today.plusDays(1), today.plusDays(2))) {
            model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(date).withTime("10:00")
                    .withDuration(60).withVenue("Home").build());
            model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(date).withTime("11:15")
                    .withDuration(60).withVenue("Cafe").build());
        }
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(today).withTime("15:00")
                .withDuration(60).withVenue("Library").build());
        logic.execute("agenda d/" + today);
        String result = logic.execute("lesson move 3 d/" + today.plusDays(1)).getFeedbackToUser();
        assertFalse(result.contains("Travel warning:"));
    }

    @Test
    public void execute_addUnrelatedOnlineLesson_doesNotRepeatExistingWarning() throws Exception {
        logic.execute("lesson add st/1 s/Math d/" + today + " t/10:00 dur/60 v/Home");
        logic.execute("lesson add st/1 s/Math d/" + today + " t/11:15 dur/60 v/Cafe");
        String result = logic.execute("lesson add st/1 s/Math d/" + today
                + " t/15:00 dur/60 v/Online").getFeedbackToUser();
        assertFalse(result.contains("Travel warning:"));
        assertTrue(logic.execute("agenda d/" + today).getFeedbackToUser().contains("Travel warning:"));
    }

    @Test
    public void execute_addBetweenLessons_reportsIncomingAndOutgoingJourneys() throws Exception {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(today).withTime("10:00")
                .withDuration(60).withVenue("Home").build());
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(today).withTime("12:15")
                .withDuration(60).withVenue("School").build());
        String result = logic.execute("lesson add st/1 s/Math d/" + today
                + " t/11:10 dur/60 v/Cafe").getFeedbackToUser();
        assertEquals(2, result.split("Travel warning:", -1).length - 1);
        assertTrue(result.contains("at Home -> Alice Pauline at Cafe"));
        assertTrue(result.contains("at Cafe -> Alice Pauline at School"));
        assertNumberingOnce(result);
    }

    @Test
    public void execute_moveBetweenLessons_reportsIncomingAndOutgoingJourneys() throws Exception {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(today).withTime("10:00")
                .withDuration(60).withVenue("Home").build());
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(today).withTime("12:15")
                .withDuration(60).withVenue("School").build());
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(today).withTime("15:00")
                .withDuration(60).withVenue("Cafe").build());
        logic.execute("agenda d/" + today);
        String result = logic.execute("lesson move 3 t/11:10").getFeedbackToUser();
        assertEquals(2, result.split("Travel warning:", -1).length - 1);
        assertTrue(result.contains("at Home -> Alice Pauline at Cafe"));
        assertTrue(result.contains("at Cafe -> Alice Pauline at School"));
        assertNumberingOnce(result);
    }

    @Test
    public void execute_repeatWarnsForEveryAffectedJourneyAndSeriesSurvivesRestart() throws Exception {
        LocalDate first = today.plusDays(1);
        for (LocalDate date : List.of(first, first.plusWeeks(1))) {
            for (String booking : List.of("t/10:00 dur/60 v/Home", "t/12:20 dur/60 v/School",
                    "t/15:00 dur/60 v/Park", "t/16:10 dur/60 v/Library")) {
                logic.execute("lesson add st/1 s/Math d/" + date + " " + booking);
            }
        }
        String result = logic.execute("lesson repeat st/1 s/Math d/" + first
                + " t/11:10 dur/60 v/Cafe count/2").getFeedbackToUser();
        assertEquals(4, result.split("Travel warning:", -1).length - 1, result);
        assertTrue(result.contains("at Home -> Alice Pauline at Cafe"));
        assertTrue(result.contains("at Cafe -> Alice Pauline at School"));
        assertFalse(result.contains("at Park"));

        ModelManager restoredModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        List<Lesson> series = restoredModel.getAddressBook().getLessonList().stream()
                .filter(lesson -> lesson.getSeriesId().isPresent()).toList();
        assertEquals(2, series.size());
        assertEquals(series.getFirst().getSeriesId(), series.getLast().getSeriesId());
        LogicManager restored = logic(restoredModel, storage);
        restored.execute("agenda d/" + first);
        restored.execute("lesson series cancel 2 confirm");
        ReadOnlyAddressBook saved = storage.readAddressBook().orElseThrow();
        assertEquals(2, saved.getLessonList().stream().filter(Lesson::isCancelled).count());
        for (Lesson original : series) {
            Lesson cancelled = saved.getLessonList().stream()
                    .filter(lesson -> lesson.getId().equals(original.getId())).findFirst().orElseThrow();
            assertEquals(original.getSeriesId(), cancelled.getSeriesId());
        }
    }

    @Test
    public void execute_seriesCancelRemovesUpcomingOccurrences_refreshesNumbersExactlyOnce() throws Exception {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(today.plusDays(1)).withTime("10:00")
                .build().withSeries(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")));
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(today.plusDays(2)).withTime("10:00")
                .build().withSeries(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")));
        logic.execute("lesson list st/1");
        String result = logic.execute("lesson series cancel 1 confirm").getFeedbackToUser();
        assertNumberingOnce(result);
        assertTrue(result.contains("No lessons in the current list."));
        assertTrue(model.getFilteredLessonList().isEmpty());
    }

    private static void assertNumberingOnce(String result) {
        assertEquals(1, result.split("Lesson numbers have changed. Current lesson list:", -1).length - 1, result);
    }

    private Lesson lesson(String time) {
        return new LessonBuilder().withStudent(ALICE).withDate(today).withTime(time).withDuration(60).build()
                .snapshotStudent(ALICE);
    }

    private LogicManager logic(ModelManager target, JsonAddressBookStorage targetStorage) {
        return new LogicManager(target, new StorageManager(targetStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
