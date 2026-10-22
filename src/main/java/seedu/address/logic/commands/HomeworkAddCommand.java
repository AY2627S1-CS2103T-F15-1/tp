package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.person.Person;

/** Assigns homework to an active student without changing previously displayed homework indices. */
public class HomeworkAddCommand extends Command {
    public static final String MESSAGE_USAGE = "hw add st/STUDENT_INDEX desc/DESCRIPTION due/DATE";

    public static final String MESSAGE_SUCCESS = "Homework assigned to %1$s: %2$s, due %3$s\n"
            + "Run 'hw list' to display the new assignment's index.";
    public static final String MESSAGE_NO_UPCOMING_LESSON =
            "\nNote: %1$s has no upcoming lessons. The homework is still recorded.";
    public static final String MESSAGE_STUDENT_ARCHIVED = "Restore this student before assigning new homework.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkAddCommand.class);

    private final Index studentIndex;
    private final String description;
    private final LocalDate dueDate;
    private final LocalDate today;

    /**
     * Creates a command that assigns homework with {@code description}, due on {@code dueDate}, to the student at
     * {@code studentIndex} of the displayed student list. {@code today} bounds the due date.
     */
    public HomeworkAddCommand(Index studentIndex, String description, LocalDate dueDate, LocalDate today) {
        requireNonNull(studentIndex);
        requireNonNull(description);
        requireNonNull(dueDate);
        requireNonNull(today);
        this.studentIndex = studentIndex;
        this.description = description;
        this.dueDate = dueDate;
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = HomeworkCommandSupport.student(model, studentIndex);
        if (student.isArchived()) {
            throw new CommandException(MESSAGE_STUDENT_ARCHIVED);
        }
        HomeworkCommandSupport.validateDueDate(dueDate, today);
        try {
            model.addHomework(Homework.assign(student.getId(), description, dueDate));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        logger.fine("Assigned homework to a student");

        boolean hasUpcomingLesson = model.getAddressBook().getLessonList().stream()
                .anyMatch(lesson -> lesson.isWith(student) && lesson.isUpcoming(today));
        String message = String.format(MESSAGE_SUCCESS, student.getName(), description, dueDate);
        if (!hasUpcomingLesson) {
            message += String.format(MESSAGE_NO_UPCOMING_LESSON, student.getName());
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkAddCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex) && description.equals(otherCommand.description)
                && dueDate.equals(otherCommand.dueDate) && today.equals(otherCommand.today);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, description, dueDate, today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("description", description)
                .add("dueDate", dueDate)
                .add("today", today)
                .toString();
    }
}
