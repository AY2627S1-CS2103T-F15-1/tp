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
import seedu.address.model.homework.HomeworkQueries;

/** Corrects an assignment while preserving its identity, lifecycle, and feedback. */
public class HomeworkEditCommand extends Command {
    public static final String MESSAGE_USAGE = "hw edit INDEX [desc/DESCRIPTION] [due/DATE]";

    public static final String MESSAGE_SUCCESS = "Edited homework %1$d: %2$s";

    private static final Logger logger = LogsCenter.getLogger(HomeworkEditCommand.class);

    private final Index index;
    private final String description;
    private final LocalDate dueDate;
    private final LocalDate today;

    /**
     * Creates a command that edits the homework at {@code index} of the last homework list. {@code description} and
     * {@code dueDate} may each be null to keep the current value, but not both. {@code today} bounds a changed due
     * date.
     */
    public HomeworkEditCommand(Index index, String description, LocalDate dueDate, LocalDate today) {
        requireNonNull(index);
        requireNonNull(today);
        assert description != null || dueDate != null : "at least one field must be edited";
        this.index = index;
        this.description = description;
        this.dueDate = dueDate;
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Homework target = HomeworkCommandSupport.target(model, index);
        if (dueDate != null && !dueDate.equals(target.getDueDate())) {
            HomeworkCommandSupport.validateDueDate(dueDate, today);
        }
        Homework replacement = target.edit(description == null ? target.getDescription() : description,
                dueDate == null ? target.getDueDate() : dueDate);
        try {
            model.setHomework(target, replacement);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        logger.fine("Edited a homework item");
        return new CommandResult(String.format(MESSAGE_SUCCESS, index.getOneBased(),
                HomeworkQueries.formatLine(model.getAddressBook(), replacement, today)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkEditCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index) && Objects.equals(description, otherCommand.description)
                && Objects.equals(dueDate, otherCommand.dueDate) && today.equals(otherCommand.today);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, description, dueDate, today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("description", description)
                .add("dueDate", dueDate)
                .add("today", today)
                .toString();
    }
}
