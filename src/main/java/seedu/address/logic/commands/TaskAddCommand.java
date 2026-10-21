package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DUE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HOMEWORK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PAYMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.TaskCommandParser;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.task.Task;

/**
 * Adds a follow-up task about a student or a lesson that is shown (user story 31).
 * Exactly one link is given. Links to homework and payments are accepted so that the command already has its final
 * format, but there is nothing for them to point at until the homework and payment lists exist.
 */
public class TaskAddCommand extends Command {

    public static final String COMMAND_WORD = TaskCommandParser.COMMAND_WORD + " add";

    public static final String FORMAT = COMMAND_WORD + " " + PREFIX_STUDENT + "INDEX|" + PREFIX_LESSON + "INDEX|"
            + PREFIX_HOMEWORK + "INDEX|" + PREFIX_PAYMENT + "INDEX " + PREFIX_DESCRIPTION + "TEXT ["
            + PREFIX_DUE + "DATE]";

    public static final String MESSAGE_USAGE = FORMAT + " (choose exactly one link; use hw list or pay list "
            + "before those indices)\n"
            + COMMAND_WORD + ": Adds a follow-up task about a student or a lesson. " + PREFIX_STUDENT
            + " takes a number from the student list, " + PREFIX_LESSON + " a number from the last agenda or "
            + "lesson list. The description is 1-200 characters.\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STUDENT + "1 " + PREFIX_DESCRIPTION + "Call parent "
            + PREFIX_DUE + "2026-12-01";

    public static final String MESSAGE_SUCCESS = "Added task: %1$s. Run 'task list' to display its index.";
    public static final String MESSAGE_INVALID_HOMEWORK_INDEX = "Invalid homework index. "
            + "Run 'hw list' to display homework first.";
    public static final String MESSAGE_INVALID_PAYMENT_INDEX = "Invalid payment index. Run 'pay list' first.";

    private static final Logger logger = LogsCenter.getLogger(TaskAddCommand.class);

    private final Index studentIndex; // exactly one of the four links is not null
    private final Index lessonIndex;
    private final Index homeworkIndex;
    private final Index paymentIndex;
    private final String description;
    private final LocalDate due; // null if the task has no due date

    /**
     * Creates a {@code TaskAddCommand}. Exactly one of the four indices must be given and the others must be
     * null. The {@code description} must be valid and the {@code due} date may be null.
     */
    public TaskAddCommand(Index studentIndex, Index lessonIndex, Index homeworkIndex, Index paymentIndex,
            String description, LocalDate due) {
        requireNonNull(description);
        assert Task.isValidDescription(description) : "The description must be validated by the caller";
        assert countLinks(studentIndex, lessonIndex, homeworkIndex, paymentIndex) == 1
                : "Exactly one link must be given";
        this.studentIndex = studentIndex;
        this.lessonIndex = lessonIndex;
        this.homeworkIndex = homeworkIndex;
        this.paymentIndex = paymentIndex;
        this.description = description.strip();
        this.due = due;
    }

    private static int countLinks(Index... links) {
        int count = 0;
        for (Index link : links) {
            count += link == null ? 0 : 1;
        }
        return count;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Task task = createTask(model);
        model.addTask(task);
        logger.fine("Added task");
        return new CommandResult(String.format(MESSAGE_SUCCESS, task.getDescription()));
    }

    private Task createTask(Model model) throws CommandException {
        if (studentIndex != null) {
            return Task.create(description, due, LessonCommandUtil.getStudent(model, studentIndex), null);
        }
        if (lessonIndex != null) {
            Lesson lesson = LessonCommandUtil.getLesson(model, lessonIndex);
            Person student = model.getAddressBook().getPersonList().stream().filter(lesson::isWith).findFirst()
                    .orElseThrow(() -> new CommandException("The student of that lesson no longer exists."));
            return Task.create(description, due, student, lesson);
        }
        // Homework and payments are not recorded yet, so no number can point at one
        throw new CommandException(homeworkIndex != null ? MESSAGE_INVALID_HOMEWORK_INDEX
                : MESSAGE_INVALID_PAYMENT_INDEX);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TaskAddCommand otherCommand)) {
            return false;
        }

        return Objects.equals(studentIndex, otherCommand.studentIndex)
                && Objects.equals(lessonIndex, otherCommand.lessonIndex)
                && Objects.equals(homeworkIndex, otherCommand.homeworkIndex)
                && Objects.equals(paymentIndex, otherCommand.paymentIndex)
                && description.equals(otherCommand.description)
                && Objects.equals(due, otherCommand.due);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("lessonIndex", lessonIndex)
                .add("homeworkIndex", homeworkIndex)
                .add("paymentIndex", paymentIndex)
                .add("description", description)
                .add("due", due)
                .toString();
    }
}
