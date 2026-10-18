package seedu.address.model.homework;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/** Shared query and display rules for homework lists, student overviews, and dashboards. */
public final class HomeworkQueries {
    private HomeworkQueries() {
    }

    /** Selects assignments by student and derived lifecycle, with overdue first and due dates ascending. */
    public static List<Homework> select(ReadOnlyAddressBook book, UUID studentId, boolean overdue,
            boolean outstanding, LocalDate today) {
        return book.getHomeworkList().stream()
                .filter(item -> studentId == null || item.getStudentId().equals(studentId))
                .filter(item -> !overdue || item.isOverdue(today))
                .filter(item -> !outstanding || item.isOutstanding())
                .sorted(Comparator.comparing((Homework item) -> !item.isOverdue(today))
                        .thenComparing(Homework::getDueDate)).toList();
    }

    /** Formats live student labels and all feedback without exposing persistent IDs. */
    public static String format(ReadOnlyAddressBook book, Homework item, LocalDate today) {
        Person student = book.getPersonList().stream()
                .filter(person -> person.getId().equals(item.getStudentId())).findFirst().orElseThrow();
        return student.getName() + (student.isArchived() ? " (archived)" : "") + "  " + item.getDescription()
                + "  due " + item.getDueDate() + "  " + (item.isOverdue(today) ? "OVERDUE" : item.getStatus())
                + item.getFeedback().map(feedback -> "\n    Feedback: " + feedback).orElse("");
    }

    /** Formats a non-indexed summary that leaves the command selection unchanged. */
    public static String summary(ReadOnlyAddressBook book, UUID studentId, boolean overdue, LocalDate today) {
        List<Homework> items = select(book, studentId, overdue, !overdue, today);
        String title = overdue ? "OVERDUE HOMEWORK" : "OUTSTANDING HOMEWORK";
        StringBuilder text = new StringBuilder(title + " (" + items.size() + ")");
        if (items.isEmpty()) {
            text.append(overdue ? "\n  No overdue homework." : "\n  No outstanding homework.");
        } else {
            items.forEach(item -> text.append("\n  ").append(format(book, item, today)));
        }
        return text.toString();
    }
}
