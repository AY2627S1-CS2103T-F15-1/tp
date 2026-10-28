package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkQueries;
import seedu.address.model.person.StudentSummaryLabels;

/** Shows a complete or filtered homework history and establishes a stable numbered selection. */
public class HomeworkListCommand extends Command {
    public static final int MAX_DISPLAYED_HOMEWORK = 200;
    public static final String MESSAGE_USAGE = "hw list [st/STUDENT_INDEX] [overdue/|outstanding/]";

    public static final String MESSAGE_FLAGS_EXCLUSIVE = "Choose overdue/ or outstanding/, not both.";
    public static final String MESSAGE_LISTED = "%1$d homework item(s).";
    public static final String MESSAGE_NO_MATCH = "\nNo homework matches those filters.";
    public static final String MESSAGE_NONE_RECORDED = "No homework recorded yet.";
    public static final String MESSAGE_TRUNCATED = "Showing %1$d of %2$d homework item(s). The hidden rows keep "
            + "their numbers for indexed homework commands.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkListCommand.class);

    private final Index studentIndex;
    private final boolean isOverdueOnly;
    private final boolean isOutstandingOnly;
    private final LocalDate today;

    /**
     * Creates a command that lists the homework of the student at {@code studentIndex} of the displayed student
     * list, or of every student if it is null. Overdue and outstanding work cannot be asked for together;
     * {@code today} decides what is overdue.
     */
    public HomeworkListCommand(Index studentIndex, boolean isOverdueOnly, boolean isOutstandingOnly,
            LocalDate today) {
        requireNonNull(today);
        assert !(isOverdueOnly && isOutstandingOnly) : "overdue and outstanding cannot be combined";
        this.studentIndex = studentIndex;
        this.isOverdueOnly = isOverdueOnly;
        this.isOutstandingOnly = isOutstandingOnly;
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        UUID studentId = studentIndex == null ? null : HomeworkCommandSupport.student(model, studentIndex).getId();
        List<Homework> rows = HomeworkQueries.select(model.getAddressBook(), studentId, isOverdueOnly,
                isOutstandingOnly, today);
        model.showHomework(rows);
        logger.fine("Listed " + rows.size() + " homework item(s)");

        if (rows.isEmpty() && model.getAddressBook().getHomeworkList().isEmpty()) {
            return new CommandResult(MESSAGE_NONE_RECORDED).withDisplayedHomework(rows);
        }
        StringBuilder text = new StringBuilder(String.format(MESSAGE_LISTED, rows.size()));
        if (rows.isEmpty()) {
            text.append(MESSAGE_NO_MATCH);
        }
        StudentSummaryLabels labels = new StudentSummaryLabels(model.getAddressBook());
        int shown = Math.min(rows.size(), MAX_DISPLAYED_HOMEWORK);
        for (int i = 0; i < shown; i++) {
            text.append("\n").append(i + 1).append(". ")
                    .append(HomeworkQueries.format(labels, rows.get(i), today));
        }
        if (shown < rows.size()) {
            text.append("\n").append(String.format(MESSAGE_TRUNCATED, shown, rows.size()));
        }
        return new CommandResult(text.toString()).withDisplayedHomework(rows);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkListCommand otherCommand)) {
            return false;
        }

        return Objects.equals(studentIndex, otherCommand.studentIndex) && isOverdueOnly == otherCommand.isOverdueOnly
                && isOutstandingOnly == otherCommand.isOutstandingOnly && today.equals(otherCommand.today);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, isOverdueOnly, isOutstandingOnly, today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("isOverdueOnly", isOverdueOnly)
                .add("isOutstandingOnly", isOutstandingOnly)
                .add("today", today)
                .toString();
    }
}
