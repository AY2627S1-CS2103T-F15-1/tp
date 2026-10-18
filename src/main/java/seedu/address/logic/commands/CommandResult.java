package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.task.FollowUpTask;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final List<FollowUpTask> displayedTasks;
    private final String feedbackToUser;
    private final List<Lesson> displayedLessons;
    private final List<Homework> displayedHomework;
    private final List<LedgerEntry> displayedPayments;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null, null, null, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, List<Lesson> displayedLessons,
            List<Homework> displayedHomework, List<LedgerEntry> displayedPayments, List<FollowUpTask> displayedTasks) {
        this.displayedTasks = displayedTasks == null ? null : List.copyOf(displayedTasks);
        this.displayedPayments = displayedPayments == null ? null : List.copyOf(displayedPayments);
        this.displayedHomework = displayedHomework == null ? null : List.copyOf(displayedHomework);
        this.displayedLessons = displayedLessons == null ? null : List.copyOf(displayedLessons);
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public Optional<List<Lesson>> getDisplayedLessons() {
        return Optional.ofNullable(displayedLessons);
    }

    /** Attaches the immutable numbered selection actually rendered in this result. */
    public CommandResult withDisplayedLessons(List<Lesson> lessons) {
        return new CommandResult(feedbackToUser, showHelp, exit, lessons, displayedHomework, displayedPayments,
                displayedTasks);
    }

    public Optional<List<Homework>> getDisplayedHomework() {
        return Optional.ofNullable(displayedHomework);
    }

    /** Attaches immutable homework rows actually numbered in this result. */
    public CommandResult withDisplayedHomework(List<Homework> homework) {
        return new CommandResult(feedbackToUser, showHelp, exit, displayedLessons, homework, displayedPayments,
                displayedTasks);
    }

    public Optional<List<LedgerEntry>> getDisplayedPayments() {
        return Optional.ofNullable(displayedPayments);
    }

    /** Attaches immutable payment rows actually numbered in this result. */
    public CommandResult withDisplayedPayments(List<LedgerEntry> payments) {
        return new CommandResult(feedbackToUser, showHelp, exit, displayedLessons, displayedHomework, payments,
                displayedTasks);
    }

    public Optional<List<FollowUpTask>> getDisplayedTasks() {
        return Optional.ofNullable(displayedTasks);
    }

    /** Attaches immutable task rows actually numbered in this result. */
    public CommandResult withDisplayedTasks(List<FollowUpTask> tasks) {
        return new CommandResult(feedbackToUser, showHelp, exit, displayedLessons, displayedHomework,
                displayedPayments, tasks);
    }

    /** Replaces feedback while retaining presentation data and command flags. */
    public CommandResult withFeedback(String feedback) {
        return new CommandResult(feedback, showHelp, exit, displayedLessons, displayedHomework, displayedPayments,
                displayedTasks);
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .toString();
    }

}
