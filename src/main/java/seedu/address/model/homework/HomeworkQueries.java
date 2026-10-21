package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;

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

    /**
     * Selects the assignments of the student with {@code studentId}, or of every student if it is null. Overdue
     * ones come first, then due dates ascend, and assignments due on the same day stay in the order they were added.
     *
     * @param isOverdueOnly whether to keep only unsubmitted work whose due date has passed.
     * @param isOutstandingOnly whether to keep only work that is assigned or submitted.
     */
    public static List<Homework> select(ReadOnlyAddressBook book, UUID studentId, boolean isOverdueOnly,
            boolean isOutstandingOnly, LocalDate today) {
        requireNonNull(book);
        requireNonNull(today);
        return book.getHomeworkList().stream()
                .filter(item -> studentId == null || item.getStudentId().equals(studentId))
                .filter(item -> !isOverdueOnly || item.isOverdue(today))
                .filter(item -> !isOutstandingOnly || item.isOutstanding())
                .sorted(Comparator.comparing((Homework item) -> !item.isOverdue(today))
                        .thenComparing(Homework::getDueDate)).toList();
    }

    /** Formats the student, description, due date and status of an item on one line, without its feedback. */
    public static String formatLine(ReadOnlyAddressBook book, Homework item, LocalDate today) {
        requireNonNull(book);
        requireNonNull(item);
        requireNonNull(today);
        Person student = book.getPersonList().stream()
                .filter(person -> person.getId().equals(item.getStudentId())).findFirst().orElseThrow();
        return student.getName() + (student.isArchived() ? " (archived)" : "") + " " + item.getDescription()
                + " due " + item.getDueDate() + " " + (item.isOverdue(today) ? "OVERDUE" : item.getStatus());
    }

    /** Formats an item as {@link #formatLine} does and adds its feedback, if any, on the next line. */
    public static String format(ReadOnlyAddressBook book, Homework item, LocalDate today) {
        return formatLine(book, item, today)
                + item.getFeedback().map(feedback -> "\n    Feedback: " + feedback).orElse("");
    }

    /**
     * A non-indexed summary of unsubmitted or outstanding work that leaves the command selection unchanged.
     *
     * @param heading the title with the number of items, such as {@code OVERDUE HOMEWORK (1)}.
     * @param hasItems whether there is any homework to list.
     * @param lines one entry for each item, or the sentence that says there are none.
     */
    public record Summary(String heading, boolean hasItems, List<String> lines) {

        /** Returns the summary as text, with its entries indented below the heading. */
        public String toText() {
            StringBuilder text = new StringBuilder(heading);
            lines.forEach(line -> text.append("\n  ").append(line));
            return text.toString();
        }
    }

    /** Summarises the overdue homework if {@code isOverdueSummary} is true, and the outstanding homework otherwise. */
    public static Summary summaryOf(ReadOnlyAddressBook book, UUID studentId, boolean isOverdueSummary,
            LocalDate today) {
        List<Homework> items = select(book, studentId, isOverdueSummary, !isOverdueSummary, today);
        String title = isOverdueSummary ? "OVERDUE HOMEWORK" : "OUTSTANDING HOMEWORK";
        List<String> lines = items.isEmpty()
                ? List.of(isOverdueSummary ? "No overdue homework." : "No outstanding homework.")
                : items.stream().map(item -> format(book, item, today)).toList();
        return new Summary(title + " (" + items.size() + ")", !items.isEmpty(), lines);
    }

    /** Formats a non-indexed summary that leaves the command selection unchanged. */
    public static String summary(ReadOnlyAddressBook book, UUID studentId, boolean isOverdueSummary,
            LocalDate today) {
        return summaryOf(book, studentId, isOverdueSummary, today).toText();
    }
}
