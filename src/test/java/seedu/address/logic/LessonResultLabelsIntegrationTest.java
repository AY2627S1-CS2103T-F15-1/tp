package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentSummaryLabels;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

/** Ensures lesson feedback identifies the same students as the agenda, including after a failed save. */
public class LessonResultLabelsIntegrationTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-02T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final String FIRST_LABEL = "Alex (phone ending 4567)";
    private static final String SECOND_LABEL = "Alex (phone ending 5678)";

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private Person first;
    private Person second;
    private boolean failSave;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        first = new PersonBuilder().withName("Alex").withPhone("91234567").withSubjects("Math").build();
        second = new PersonBuilder().withName("Alex").withPhone("92345678").withSubjects("Math").build();
        model.addPerson(first);
        model.addPerson(second);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("data.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (failSave) {
                    throw new IOException("Synthetic save failure");
                }
                super.saveAddressBook(book);
            }
        };
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))), CLOCK);
    }

    @Test
    public void move_renumberedRowsIdentifyStudentsAndKeepCorrectIndices() throws Exception {
        scheduleFutureLessons();
        String feedback = execute("lesson move 2 t/15:00");
        assertTrue(feedback.contains("Rescheduled: " + SECOND_LABEL));
        assertTrue(feedback.contains("\n1. " + SECOND_LABEL));
        assertTrue(feedback.contains("\n2. " + FIRST_LABEL));
        assertEquals(second.getId(), model.getFilteredLessonList().get(0).getStudentId().orElseThrow());
        assertEquals(first.getId(), model.getFilteredLessonList().get(1).getStudentId().orElseThrow());
        assertTrue(execute("lesson prep 1 n/Bring paper").contains("Preparation saved for " + SECOND_LABEL));
    }

    @Test
    public void move_failedSaveStillShowsDisambiguatedRenumbering() throws Exception {
        scheduleFutureLessons();
        failSave = true;
        CommandException failure = assertThrows(CommandException.class, () -> execute("lesson move 2 t/15:00"));
        String feedback = failure.getMessage();
        assertTrue(feedback.contains("\n1. " + SECOND_LABEL));
        assertTrue(feedback.contains("\n2. " + FIRST_LABEL));
        assertTrue(logic.hasUnsavedChanges());
        failSave = false;
        execute("save");
        assertFalse(logic.hasUnsavedChanges());
    }

    @Test
    public void lessonLifecycle_resultsIdentifyTargetWithoutFullPhoneNumbers() throws Exception {
        assertTrue(execute("lesson add st/1 d/today t/10:00").contains("Scheduled: " + FIRST_LABEL));
        assertTrue(execute("lesson add st/2 d/today t/12:00").contains("Scheduled: " + SECOND_LABEL));
        execute("agenda");
        assertTrue(execute("lesson done 1 n/Algebra").contains("Completed: " + FIRST_LABEL));
        assertTrue(execute("lesson done 1 n/Updated notes").contains(FIRST_LABEL));
        assertTrue(execute("lesson missed 2").contains("Marked missed: " + SECOND_LABEL));
        assertTrue(execute("lesson cancel 2").contains(SECOND_LABEL));
    }

    @Test
    public void addAndRepeat_overlapWarningsAndRenumberingIdentifyBothStudents() throws Exception {
        execute("lesson repeat st/1 d/tmr t/10:00 count/2");
        execute("agenda d/tmr");
        String repeated = execute("lesson repeat st/2 d/tmr t/09:30 count/2");
        assertTrue(repeated.contains("overlaps with " + FIRST_LABEL));
        assertTrue(repeated.contains("\n1. " + SECOND_LABEL));
        assertTrue(repeated.contains("\n2. " + FIRST_LABEL));
        String added = execute("lesson add st/1 d/tmr t/09:45");
        assertTrue(added.contains("Scheduled: " + FIRST_LABEL));
        String overlaps = added.substring(added.indexOf("Warning:"), added.indexOf("Lesson numbers have changed"));
        assertTrue(overlaps.contains(FIRST_LABEL), added);
        assertTrue(overlaps.contains(SECOND_LABEL), added);
        assertTrue(added.contains("\n2. " + FIRST_LABEL));
    }

    @Test
    public void travelWarnings_identifyBothStudentsIncludingArchivedOnes() throws Exception {
        execute("lesson add st/1 d/tmr t/10:00 v/Bishan");
        String added = execute("lesson add st/2 d/tmr t/11:15 v/Clementi");
        assertTrue(added.contains("Travel warning: " + FIRST_LABEL + " at Bishan -> "
                + SECOND_LABEL + " at Clementi"));
        execute("archive 2");
        String warning = execute("travel buffer/30");
        assertTrue(warning.contains("Alex (phone ending 5678, archived) at Clementi"));
    }

    @Test
    public void formatting_legacyFallbackAndSameSuffixReferencesArePreserved() {
        Lesson legacy = new LessonBuilder().withStudent(first).build();
        StudentSummaryLabels labels = new StudentSummaryLabels(model.getAddressBook());
        assertEquals(Messages.format(legacy), Messages.format(legacy, labels));
        Person sameSuffix = new PersonBuilder().withName("Alex").withPhone("93454567").build();
        model.addPerson(sameSuffix);
        labels = new StudentSummaryLabels(model.getAddressBook());
        Lesson linked = legacy.snapshotStudent(first);
        String expected = "Alex (phone ending 4567, ref " + first.getId().toString().substring(0, 8) + ")";
        assertTrue(Messages.formatLessonList(List.of(linked), labels).contains("\n1. " + expected));
        assertFalse(Messages.formatOverlaps(List.of(linked), labels).contains(first.getPhone().value));
        String capped = Messages.formatLessonList(Collections.nCopies(31, linked), labels);
        assertTrue(capped.contains("\n30. " + expected));
        assertFalse(capped.contains("\n31. "));
        assertTrue(capped.contains("... and 1 more."));
        Person absent = new PersonBuilder().withName("Absent").build();
        Lesson missingStudent = legacy.snapshotStudent(absent);
        assertEquals(Messages.format(missingStudent), Messages.format(missingStudent, labels));
    }

    private void scheduleFutureLessons() throws Exception {
        execute("lesson add st/1 d/2026-11-10 t/16:00");
        execute("lesson add st/2 d/2026-11-10 t/18:00");
        execute("agenda d/2026-11-10");
    }

    private String execute(String command) throws Exception {
        String feedback = logic.execute(command).getFeedbackToUser();
        assertFalse(feedback.contains(first.getPhone().value), feedback);
        assertFalse(feedback.contains(second.getPhone().value), feedback);
        return feedback;
    }
}
