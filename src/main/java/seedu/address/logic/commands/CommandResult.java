package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;
    private final List<Lesson> displayedLessons;
    private final List<Homework> displayedHomework;

    /** Help information should be shown to the user. */
    private final boolean isShowHelp;

    /** The application should exit. */
    private final boolean isExit;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean isShowHelp, boolean isExit) {
        this(feedbackToUser, isShowHelp, isExit, null, null);
    }

    private CommandResult(String feedbackToUser, boolean isShowHelp, boolean isExit, List<Lesson> displayedLessons,
            List<Homework> displayedHomework) {
        this.displayedHomework = displayedHomework == null ? null : List.copyOf(displayedHomework);
        this.displayedLessons = displayedLessons == null ? null : List.copyOf(displayedLessons);
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.isShowHelp = isShowHelp;
        this.isExit = isExit;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /** Returns the text to show to the user. */
    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    /** Returns the lessons to display, if this result has any. */
    public Optional<List<Lesson>> getDisplayedLessons() {
        return Optional.ofNullable(displayedLessons);
    }

    /** Attaches the immutable numbered selection actually rendered in this result. */
    public CommandResult withDisplayedLessons(List<Lesson> lessons) {
        return new CommandResult(feedbackToUser, isShowHelp, isExit, lessons, displayedHomework);
    }

    /** Returns the homework to display, if this result has any. */
    public Optional<List<Homework>> getDisplayedHomework() {
        return Optional.ofNullable(displayedHomework);
    }

    /** Attaches immutable homework rows actually numbered in this result. */
    public CommandResult withDisplayedHomework(List<Homework> homework) {
        return new CommandResult(feedbackToUser, isShowHelp, isExit, displayedLessons, homework);
    }

    /** Replaces feedback while retaining presentation data and command flags. */
    public CommandResult withFeedback(String feedback) {
        return new CommandResult(feedback, isShowHelp, isExit, displayedLessons, displayedHomework);
    }

    /** Returns true if the help window should be shown. */
    public boolean isShowHelp() {
        return isShowHelp;
    }

    /** Returns true if the application should exit. */
    public boolean isExit() {
        return isExit;
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
                && isShowHelp == otherCommandResult.isShowHelp
                && isExit == otherCommandResult.isExit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, isShowHelp, isExit);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", isShowHelp)
                .add("exit", isExit)
                .toString();
    }

}
