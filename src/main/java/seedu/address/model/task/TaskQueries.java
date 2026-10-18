package seedu.address.model.task;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.Money;
import seedu.address.model.person.Person;

/** Read-only task ordering and live linked-history presentation. */
public final class TaskQueries {
    private TaskQueries() {
    }

    /** Sorts by due date, leaving undated tasks last and ties in persisted creation order. */
    public static List<FollowUpTask> select(ReadOnlyAddressBook book, UUID studentId, boolean all) {
        return book.getTaskList().stream().filter(task -> all || !task.done())
                .filter(task -> studentId == null || task.studentId().equals(studentId))
                .sorted(Comparator.comparing(FollowUpTask::dueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /** Formats current linked details without changing any domain's displayed selection. */
    public static String format(ReadOnlyAddressBook book, FollowUpTask task, LocalDate today) {
        String state = task.done() ? "done" : "open";
        if (!task.done() && task.dueDate() != null && task.dueDate().isBefore(today)) {
            state += "; overdue";
        }
        Person student = book.getPersonList().stream().filter(person -> person.getId().equals(task.studentId()))
                .findFirst().orElseThrow();
        String parent = switch (task.linkType()) {
            case STUDENT -> "student";
            case LESSON -> book.getLessonList().stream().filter(lesson -> lesson.getId().equals(task.targetId()))
                    .map(lesson -> "lesson " + lesson.getSubject() + " " + lesson.getDate() + " "
                            + lesson.getTimeRange() + " (" + lesson.getStatus() + ")").findFirst().orElseThrow();
            case HOMEWORK -> book.getHomeworkList().stream().filter(item -> item.getId().equals(task.targetId()))
                    .map(item -> "homework " + item.getDescription() + " due " + item.getDueDate()
                            + " (" + item.getStatus() + ")").findFirst().orElseThrow();
            case PAYMENT -> book.getLedgerEntries().stream().filter(entry -> entry.id().equals(task.targetId()))
                    .map(entry -> "payment " + Money.format(entry.amount()) + " on " + entry.date()
                            + (entry.description() == null ? "" : " - " + entry.description())
                            + (Ledger.isReversed(book, entry.id()) ? " (reversed)" : ""))
                    .findFirst().orElseThrow();
        };
        return "[" + state + "] " + task.description()
                + (task.dueDate() == null ? " (no due date)" : " due " + task.dueDate())
                + " | " + student.getName() + " (" + student.getPhone() + ")"
                + (student.isArchived() ? " [archived]" : "") + " | " + parent;
    }
}
