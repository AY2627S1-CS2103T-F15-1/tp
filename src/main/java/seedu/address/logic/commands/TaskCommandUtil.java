package seedu.address.logic.commands;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.task.Task;

/**
 * Contains checks and messages that the task commands share.
 */
final class TaskCommandUtil {

    static final String MESSAGE_INVALID_TASK_INDEX = "Invalid task index. Run 'task list' first.";
    static final String MESSAGE_TASK_DELETED = "That task was deleted. Run 'task list' to refresh.";
    static final String MESSAGE_REFRESH_KEPT = "Shown task indices stay unchanged; run 'task list' to refresh.";

    private TaskCommandUtil() {} // prevents instantiation

    /**
     * Returns the current version of the task at {@code index} in the tasks that the last {@code task list}
     * showed. The shown tasks are a snapshot, so the task is looked up again in case it was edited since.
     *
     * @throws CommandException if no tasks are shown, the index is out of range, or the task was deleted.
     */
    static Task getShownTask(Model model, Index index) throws CommandException {
        List<Task> shown = model.getShownTasks();
        if (index.getZeroBased() >= shown.size()) {
            throw new CommandException(MESSAGE_INVALID_TASK_INDEX);
        }
        return model.findTask(shown.get(index.getZeroBased()).getId())
                .orElseThrow(() -> new CommandException(MESSAGE_TASK_DELETED));
    }

    /**
     * Formats {@code task} for the task list: its status, text and due date, the student it is about, and the
     * record it is linked to as that record reads now.
     */
    static String format(Model model, Task task) {
        String due = task.getDue().map(date -> "due " + date).orElse("(no due date)");
        return String.format("[%s] %s %s | %s | %s", task.isDone() ? "done" : "open", task.getDescription(),
                due, describeStudent(model, task), describeLink(model, task));
    }

    private static String describeStudent(Model model, Task task) {
        return model.getAddressBook().getPersonList().stream().filter(task::isAbout).findFirst()
                .map(person -> person.getName() + " (" + person.getPhone() + ")")
                .orElse("unknown student");
    }

    private static String describeLink(Model model, Task task) {
        if (task.getLessonId().isPresent()) {
            return model.getAddressBook().getLessonList().stream().filter(task::isAbout).findFirst()
                    .map(lesson -> String.format("lesson %s %s %s (%s)", lesson.getSubject(), lesson.getDate(),
                            lesson.getTimeRange(), lesson.getStatus()))
                    // Not expected, because a lesson takes its tasks with it when it is deleted
                    .orElse("lesson (no longer exists)");
        }
        if (task.getHomeworkId().isPresent()) {
            return model.getAddressBook().getHomeworkList().stream().filter(task::isAbout).findFirst()
                    .map(item -> String.format("homework %s due %s (%s)", item.getDescription(), item.getDueDate(),
                            item.getStatus()))
                    .orElse("homework (no longer exists)");
        }
        if (task.getPaymentId().isPresent()) {
            return model.getAddressBook().getLedgerEntries().stream().filter(task::isAbout).findFirst()
                    .map(payment -> String.format("payment %s on %s%s", Ledger.format(payment.amount()),
                            payment.date(), payment.notes() == null ? "" : " - " + payment.notes()))
                    .orElse("payment (no longer exists)");
        }
        return "student";
    }
}
