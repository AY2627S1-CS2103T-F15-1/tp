package seedu.address.logic.commands;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkQueries;

/** Corrects an assignment while preserving its identity, lifecycle, and feedback. */
public class HomeworkEditCommand extends Command {
    public static final String MESSAGE_USAGE = "hw edit INDEX [desc/DESCRIPTION] [due/DATE]";

    private final Index index;
    private final String description;
    private final LocalDate dueDate;
    private final LocalDate today;

    /** Creates an edit with at least one supplied field. */
    public HomeworkEditCommand(Index index, String description, LocalDate dueDate, LocalDate today) {
        this.index = index;
        this.description = description;
        this.dueDate = dueDate;
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
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
        return new CommandResult("Edited homework " + index.getOneBased() + ": "
                + HomeworkQueries.format(model.getAddressBook(), replacement, today));
    }
}
