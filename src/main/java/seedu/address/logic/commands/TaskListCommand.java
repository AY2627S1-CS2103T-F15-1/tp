package seedu.address.logic.commands;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.task.FollowUpTask;
import seedu.address.model.task.TaskQueries;

/** Numbers a complete task query and freezes that exact selection until the next list. */
public class TaskListCommand extends Command {
    public static final String MESSAGE_USAGE = "task list [all/] [st/INDEX]"
            + "\nShows open tasks by due date; all/ includes completed tasks. Archived history remains available.";
    private final Index student;
    private final boolean all;
    private final LocalDate today;

    /** Creates an open or complete-history query, optionally filtered by a displayed student. */
    public TaskListCommand(Index student, boolean all, LocalDate today) {
        this.student = student;
        this.all = all;
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        UUID studentId = student == null ? null : HomeworkCommandSupport.student(model, student).getId();
        List<FollowUpTask> tasks = TaskQueries.select(model.getAddressBook(), studentId, all);
        StringBuilder text = new StringBuilder("Tasks (" + tasks.size() + ")");
        for (int i = 0; i < tasks.size(); i++) {
            text.append("\n").append(i + 1).append(". ")
                    .append(TaskQueries.format(model.getAddressBook(), tasks.get(i), today));
        }
        model.showTasks(tasks);
        return new CommandResult(text.toString()).withDisplayedTasks(tasks);
    }
}
