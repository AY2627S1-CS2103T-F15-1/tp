package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.view.DetailView;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;
    private final List<Lesson> displayedLessons;
    private final List<Homework> displayedHomework;
    private final DetailView detailView;
    private final List<Person> addedStudents;
    private final String confirmationCommand;
    private final boolean isTourRequested;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null, null, null, null, null, false);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, List<Lesson> displayedLessons,
            List<Homework> displayedHomework, DetailView detailView, List<Person> addedStudents,
            String confirmationCommand, boolean isTourRequested) {
        this.isTourRequested = isTourRequested;
        this.confirmationCommand = confirmationCommand;
        this.detailView = detailView;
        this.addedStudents = addedStudents == null ? null : List.copyOf(addedStudents);
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
        return new CommandResult(feedbackToUser, showHelp, exit, lessons, displayedHomework, detailView,
                addedStudents, confirmationCommand, isTourRequested);
    }

    public Optional<List<Homework>> getDisplayedHomework() {
        return Optional.ofNullable(displayedHomework);
    }

    /** Attaches immutable homework rows actually numbered in this result. */
    public CommandResult withDisplayedHomework(List<Homework> homework) {
        return new CommandResult(feedbackToUser, showHelp, exit, displayedLessons, homework, detailView,
                addedStudents, confirmationCommand, isTourRequested);
    }

    /** Returns what the detail pane shows for this result, if the command has something for it. */
    public Optional<DetailView> getDetailView() {
        return Optional.ofNullable(detailView);
    }

    /** Attaches the structured content that the detail pane shows instead of the text of this result. */
    public CommandResult withDetailView(DetailView view) {
        return new CommandResult(feedbackToUser, showHelp, exit, displayedLessons, displayedHomework,
                requireNonNull(view), addedStudents, confirmationCommand, isTourRequested);
    }

    /** Returns the students that this command added, which the student list highlights. */
    public List<Person> getAddedStudents() {
        return addedStudents == null ? List.of() : addedStudents;
    }

    /**
     * Returns the command that carries out what this result only previewed, such as {@code delete 3 confirm}, if
     * the command asked for confirmation. A window offers it as a button, so it never has to read the text.
     */
    public Optional<String> getConfirmationCommand() {
        return Optional.ofNullable(confirmationCommand);
    }

    /** Marks this result as a preview that {@code command} would carry out. */
    public CommandResult withConfirmationCommand(String command) {
        return new CommandResult(feedbackToUser, showHelp, exit, displayedLessons, displayedHomework, detailView,
                addedStudents, requireNonNull(command), isTourRequested);
    }

    /** Returns whether the command asks the window to start the guided tour. */
    public boolean isTourRequested() {
        return isTourRequested;
    }

    /** Marks this result as asking the window to start the guided tour. */
    public CommandResult withTourRequested() {
        return new CommandResult(feedbackToUser, showHelp, exit, displayedLessons, displayedHomework, detailView,
                addedStudents, confirmationCommand, true);
    }

    /** Marks {@code students} as added by this command. */
    public CommandResult withAddedStudents(List<Person> students) {
        return new CommandResult(feedbackToUser, showHelp, exit, displayedLessons, displayedHomework, detailView,
                requireNonNull(students), confirmationCommand, isTourRequested);
    }

    /** Replaces feedback while retaining presentation data and command flags. */
    public CommandResult withFeedback(String feedback) {
        return new CommandResult(feedback, showHelp, exit, displayedLessons, displayedHomework, detailView,
                addedStudents, confirmationCommand, isTourRequested);
    }

    /**
     * Adds {@code text} to the end of the feedback and, if the command has a detail view, also to what the result
     * box says next to it, so that a warning such as unsaved changes is not lost.
     */
    public CommandResult withAppendedText(String text) {
        requireNonNull(text);
        DetailView noted = detailView == null || text.isBlank() ? detailView : detailView.withNote(text.strip());
        return new CommandResult(feedbackToUser + text, showHelp, exit, displayedLessons, displayedHomework, noted,
                addedStudents, confirmationCommand, isTourRequested);
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
