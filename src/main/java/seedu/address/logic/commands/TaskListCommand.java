package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.TaskCommandParser;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.task.Task;

/**
 * Lists the open tasks, by due date, or all tasks if {@code all/} is given, optionally for one student. The numbers
 * it shows are the task indices that the other task commands use, and they stay fixed until the next listing.
 */
public class TaskListCommand extends Command {

    public static final String COMMAND_WORD = TaskCommandParser.COMMAND_WORD + " list";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists the open tasks, earliest due date first, "
            + "or all tasks including completed ones if " + PREFIX_ALL + " is given. "
            + PREFIX_STUDENT + "INDEX limits the list to one student.\n"
            + "Parameters: [" + PREFIX_ALL + "] [" + PREFIX_STUDENT + "INDEX]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_ALL + " " + PREFIX_STUDENT + "1";

    public static final String MESSAGE_HEADER = "Tasks (%1$d)";
    public static final String MESSAGE_NO_TASKS = "No tasks to show. Use 'task add' to create one.";

    /** Orders tasks by due date, with the tasks that have no due date last. The sort is stable. */
    static final Comparator<Task> BY_DUE_DATE = Comparator.comparing((Task task) -> task.getDue()
            .orElse(LocalDate.MAX));

    private final boolean isShowingAll;
    private final Index studentIndex; // null if every student's tasks are listed

    /**
     * Creates a {@code TaskListCommand}. Completed tasks are only listed if {@code isShowingAll} is true, and
     * only the tasks of the student at {@code studentIndex} are listed unless it is null.
     */
    public TaskListCommand(boolean isShowingAll, Index studentIndex) {
        this.isShowingAll = isShowingAll;
        this.studentIndex = studentIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = studentIndex == null ? null : LessonCommandUtil.getStudent(model, studentIndex);

        List<Task> tasks = model.getAddressBook().getTaskList().stream()
                .filter(task -> isShowingAll || !task.isDone())
                .filter(task -> student == null || task.isAbout(student))
                .sorted(BY_DUE_DATE)
                .collect(Collectors.toList());
        model.setShownTasks(tasks);

        if (tasks.isEmpty()) {
            return new CommandResult(MESSAGE_NO_TASKS);
        }
        String rows = IntStream.range(0, tasks.size())
                .mapToObj(i -> (i + 1) + ". " + TaskCommandUtil.format(model, tasks.get(i)))
                .collect(Collectors.joining("\n"));
        return new CommandResult(String.format(MESSAGE_HEADER, tasks.size()) + "\n" + rows);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TaskListCommand otherCommand)) {
            return false;
        }

        return isShowingAll == otherCommand.isShowingAll && Objects.equals(studentIndex, otherCommand.studentIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("isShowingAll", isShowingAll)
                .add("studentIndex", studentIndex)
                .toString();
    }
}
