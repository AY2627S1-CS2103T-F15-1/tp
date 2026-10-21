package seedu.address.logic.view;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.homework.Homework;
import seedu.address.model.person.Person;

/**
 * One row of the homework that the last {@code hw list} showed, as the homework panel draws it. The number is the
 * one that the homework commands use. A row whose homework has been deleted since stays where it is, marked as
 * deleted, so that no number comes to mean another item.
 *
 * @param number the position in the list, counted from 1.
 * @param student the name of the student, with "(archived)" after it if the student is archived.
 * @param description what the student was given to do.
 * @param dueDate when the homework is due.
 * @param status what the row says about progress: the status, or OVERDUE if the work is late.
 * @param isOverdue whether the work is unsubmitted after its due date.
 * @param feedback the feedback the tutor recorded, if any.
 * @param isDeleted whether the homework has been deleted since the list was shown.
 */
public record HomeworkRow(int number, String student, String description, LocalDate dueDate, String status,
        boolean isOverdue, Optional<String> feedback, boolean isDeleted) {

    /** The status that a row shows for homework that was deleted after it was listed. */
    public static final String STATUS_DELETED = "DELETED";

    /** Creates a row. */
    public HomeworkRow {
        requireNonNull(student);
        requireNonNull(description);
        requireNonNull(dueDate);
        requireNonNull(status);
        requireNonNull(feedback);
    }

    /**
     * Returns the rows for {@code shown}, which is the homework that was listed, numbered from 1. Each row says
     * what the homework is in {@code book} now, and a row that is no longer there is marked as deleted.
     */
    public static List<HomeworkRow> listOf(ReadOnlyAddressBook book, List<Homework> shown, LocalDate today) {
        requireNonNull(book);
        requireNonNull(shown);
        requireNonNull(today);
        Map<UUID, Homework> current = book.getHomeworkList().stream()
                .collect(Collectors.toMap(Homework::getId, Function.identity(), (first, second) -> first));
        Map<UUID, Person> students = book.getPersonList().stream()
                .collect(Collectors.toMap(Person::getId, Function.identity(), (first, second) -> first));
        return IntStream.range(0, shown.size()).mapToObj(i -> {
            Homework listed = shown.get(i);
            Homework now = current.get(listed.getId());
            return now == null ? deleted(i + 1, students, listed) : of(i + 1, students, now, today);
        }).toList();
    }

    private static HomeworkRow of(int number, Map<UUID, Person> students, Homework item, LocalDate today) {
        boolean isOverdue = item.isOverdue(today);
        return new HomeworkRow(number, studentOf(students, item), item.getDescription(), item.getDueDate(),
                isOverdue ? "OVERDUE" : item.getStatus().toString(), isOverdue, item.getFeedback(), false);
    }

    private static HomeworkRow deleted(int number, Map<UUID, Person> students, Homework item) {
        return new HomeworkRow(number, studentOf(students, item), item.getDescription(), item.getDueDate(),
                STATUS_DELETED, false, Optional.empty(), true);
    }

    private static String studentOf(Map<UUID, Person> students, Homework item) {
        Person student = students.get(item.getStudentId());
        return student == null ? "" : describe(student);
    }

    private static String describe(Person student) {
        return student.getName() + (student.isArchived() ? " (archived)" : "");
    }
}
