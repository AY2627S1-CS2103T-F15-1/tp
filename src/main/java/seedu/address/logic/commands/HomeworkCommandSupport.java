package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.person.Person;

/** Shared selection and due-date validation for homework commands. */
public final class HomeworkCommandSupport {
    public static final String MESSAGE_INVALID_INDEX =
            "Invalid homework index. Run 'hw list' to display homework first.";
    public static final String MESSAGE_DELETED = "That homework was deleted. Run 'hw list' to refresh.";
    public static final String MESSAGE_DUE_BEFORE_TODAY = "Due dates must be today or later.";
    public static final String MESSAGE_DUE_TOO_FAR = "That due date is more than a year away. Check the year.";

    private HomeworkCommandSupport() {
    }

    /**
     * Returns the homework that the last {@code hw list} showed at {@code index}, as it is now. A number never
     * selects another item: a row deleted since the list was shown is reported as deleted.
     *
     * @throws CommandException if {@code index} is outside the last list or its homework no longer exists.
     */
    static Homework target(Model model, Index index) throws CommandException {
        requireNonNull(model);
        requireNonNull(index);
        List<Homework> rows = model.getDisplayedHomework();
        if (index.getZeroBased() >= rows.size()) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
        Homework shown = rows.get(index.getZeroBased());
        return model.getAddressBook().getHomeworkList().stream()
                .filter(item -> item.getId().equals(shown.getId())).findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_DELETED));
    }

    /**
     * Returns the student at {@code index} of the displayed student list.
     *
     * @throws CommandException if {@code index} is outside the displayed list.
     */
    static Person student(Model model, Index index) throws CommandException {
        requireNonNull(model);
        requireNonNull(index);
        List<Person> students = model.getFilteredPersonList();
        if (index.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.invalidPersonIndex(index, students.size()));
        }
        return students.get(index.getZeroBased());
    }

    /**
     * Checks a due date given for a new assignment or a changed due date: it must be today or later, and at most a
     * year ahead.
     *
     * @throws CommandException if {@code dueDate} is outside those bounds.
     */
    static void validateDueDate(LocalDate dueDate, LocalDate today) throws CommandException {
        requireNonNull(dueDate);
        requireNonNull(today);
        if (dueDate.isBefore(today)) {
            throw new CommandException(MESSAGE_DUE_BEFORE_TODAY);
        }
        if (dueDate.isAfter(today.plusYears(1))) {
            throw new CommandException(MESSAGE_DUE_TOO_FAR);
        }
    }
}
