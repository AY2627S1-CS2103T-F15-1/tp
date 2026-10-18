package seedu.address.model.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.TypicalPersons;

public class TaskIntegrityTest {
    private final Person student = TypicalPersons.ALICE;

    private FollowUpTask task(FollowUpTask.LinkType type, UUID target) {
        return new FollowUpTask(UUID.randomUUID(), student.getId(), type, target, "Follow up", null, false);
    }

    @Test
    public void root_bulkReplacement_cannotOrphanTaskLinks() {
        AddressBook book = new AddressBook();
        book.addPerson(student);
        Lesson lesson = new LessonBuilder().build().withStudent(student);
        book.addLesson(lesson);
        book.addTask(task(FollowUpTask.LinkType.LESSON, lesson.getId()));
        AddressBook before = new AddressBook(book);
        assertThrows(IllegalArgumentException.class, () -> book.setLessons(List.of()));
        assertThrows(IllegalArgumentException.class, () -> book.setPersons(List.of()));
        assertEquals(before, book);
    }

    @Test
    public void root_invalidReset_validatesBeforeReplacingAnyDomain() {
        AddressBook book = new AddressBook();
        book.addPerson(student);
        book.addTask(task(FollowUpTask.LinkType.STUDENT, student.getId()));
        AddressBook before = new AddressBook(book);
        ReadOnlyAddressBook invalid = new AddressBook() {
            @Override
            public ObservableList<FollowUpTask> getTaskList() {
                return FXCollections.observableArrayList(task(FollowUpTask.LinkType.STUDENT, student.getId()));
            }
        };
        assertThrows(IllegalArgumentException.class, () -> book.resetData(invalid));
        assertEquals(before, book);
    }

    @Test
    public void root_parentCascadesAndTaskEdits_preserveIdentityAndCompleteCopies() {
        AddressBook book = new AddressBook();
        book.addPerson(student);
        Lesson lesson = new LessonBuilder().build().withStudent(student);
        Homework homework = Homework.assign(student.getId(), "Worksheet", LocalDate.now());
        book.addLesson(lesson);
        book.addHomework(homework);
        FollowUpTask lessonTask = task(FollowUpTask.LinkType.LESSON, lesson.getId());
        FollowUpTask homeworkTask = task(FollowUpTask.LinkType.HOMEWORK, homework.getId());
        book.addTask(lessonTask);
        book.addTask(homeworkTask);
        AddressBook before = new AddressBook(book);
        assertThrows(IllegalArgumentException.class, () -> book.addTask(lessonTask));
        assertThrows(IllegalArgumentException.class, () -> book.setTask(lessonTask,
                task(FollowUpTask.LinkType.STUDENT, student.getId())));
        book.setTask(lessonTask, lessonTask.complete().edit("Done preparation", LocalDate.now()));
        assertTrue(book.getTaskList().getFirst().done());
        book.removeLesson(lesson);
        assertEquals(List.of(homeworkTask), book.getTaskList());
        book.removeHomework(homework);
        assertTrue(book.getTaskList().isEmpty());
        book.resetData(before);
        assertEquals(before, book);
        assertEquals(before.hashCode(), book.hashCode());
        book.removePerson(student);
        assertTrue(book.getTaskList().isEmpty());
    }

    @Test
    public void root_wrongStudentAndNonPaymentTargets_rejectWithoutMutation() {
        AddressBook book = new AddressBook();
        book.addPerson(student);
        book.addPerson(TypicalPersons.BOB);
        Lesson lesson = new LessonBuilder().build().withStudent(TypicalPersons.BOB);
        book.addLesson(lesson);
        assertThrows(IllegalArgumentException.class, () ->
                book.addTask(task(FollowUpTask.LinkType.LESSON, lesson.getId())));
        LedgerEntry charge = LedgerEntry.charge(lesson, TypicalPersons.BOB, null);
        book.addLedgerEntry(charge);
        assertThrows(IllegalArgumentException.class, () -> book.addTask(new FollowUpTask(UUID.randomUUID(),
                TypicalPersons.BOB.getId(), FollowUpTask.LinkType.PAYMENT, charge.id(), "Invalid", null, false)));
        assertTrue(book.getTaskList().isEmpty());
    }
}
