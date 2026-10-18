package seedu.address.logic.commands;

import java.util.List;
import java.util.UUID;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.task.FollowUpTask;

/** Resolves shown identities against live roots before mutating or linking tasks. */
final class TaskCommandSupport {
    private TaskCommandSupport() {
    }

    /** Resolves a task without shifting stale or deleted indices to another record. */
    static FollowUpTask task(Model model, Index index) throws CommandException {
        List<FollowUpTask> rows = model.getDisplayedTasks();
        if (index.getZeroBased() >= rows.size()) {
            throw new CommandException("Invalid task index. Run 'task list' first.");
        }
        UUID id = rows.get(index.getZeroBased()).id();
        return model.getAddressBook().getTaskList().stream().filter(task -> task.id().equals(id)).findFirst()
                .orElseThrow(() -> new CommandException("That task was deleted. Run 'task list' to refresh."));
    }

    /** Resolves each supported link using that domain's own displayed numbering. */
    static Target target(Model model, FollowUpTask.LinkType type, Index index) throws CommandException {
        return switch (type) {
            case STUDENT -> {
                UUID id = HomeworkCommandSupport.student(model, index).getId();
                yield new Target(id, id);
            }
            case LESSON -> {
                UUID id = LessonCommandUtil.getLesson(model, index).getId();
                Lesson lesson = model.getAddressBook().getLessonList().stream()
                        .filter(item -> item.getId().equals(id)).findFirst()
                        .orElseThrow(() -> new CommandException("That lesson was deleted. Refresh the agenda."));
                yield new Target(FinanceCommandSupport.lessonStudent(model, lesson).getId(), id);
            }
            case HOMEWORK -> {
                Homework item = HomeworkCommandSupport.target(model, index);
                yield new Target(item.getStudentId(), item.getId());
            }
            case PAYMENT -> {
                if (index.getZeroBased() >= model.getDisplayedPayments().size()) {
                    throw new CommandException("Invalid payment index. Run 'pay list' first.");
                }
                UUID id = model.getDisplayedPayments().get(index.getZeroBased()).id();
                LedgerEntry entry = model.getAddressBook().getLedgerEntries().stream()
                        .filter(item -> item.id().equals(id) && item.kind() == LedgerEntry.Kind.PAYMENT).findFirst()
                        .orElseThrow(() -> new CommandException(
                            "That payment was deleted. Run 'pay list' to refresh."));
                yield new Target(entry.studentId(), entry.id());
            }
        };
    }

    /** Current target identity and owning student, validated together before creating a task. */
    record Target(UUID studentId, UUID id) { }
}
