package seedu.address.logic.commands;

import java.time.LocalDate;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.person.Person;

/** Shared selection and due-date validation for homework mutations. */
final class HomeworkCommandSupport {
    private HomeworkCommandSupport() {
    }

    /** Resolves the last shown identity against current data without renumbering stale or deleted rows. */
    static Homework target(Model model, Index index) throws CommandException {
        List<Homework> rows = model.getDisplayedHomework();
        if (index.getZeroBased() >= rows.size()) {
            throw new CommandException("Invalid homework index. Run 'hw list' to display homework first.");
        }
        Homework shown = rows.get(index.getZeroBased());
        return model.getAddressBook().getHomeworkList().stream()
                .filter(item -> item.getId().equals(shown.getId())).findFirst()
                .orElseThrow(() -> new CommandException("That homework was deleted. Run 'hw list' to refresh."));
    }

    /** Resolves the current displayed student index. */
    static Person student(Model model, Index index) throws CommandException {
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.invalidPersonIndex(index, model.getFilteredPersonList().size()));
        }
        return model.getFilteredPersonList().get(index.getZeroBased());
    }

    /** Validates dates for new assignments and changed due dates. */
    static void validateDueDate(LocalDate dueDate, LocalDate today) throws CommandException {
        if (dueDate.isBefore(today)) {
            throw new CommandException("Due dates must be today or later.");
        }
        if (dueDate.isAfter(today.plusYears(1))) {
            throw new CommandException("That due date is more than a year away. Check the year.");
        }
    }
}
