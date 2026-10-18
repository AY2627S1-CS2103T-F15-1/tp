package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;

/** Records or replaces feedback on submitted or reviewed homework. */
public class HomeworkFeedbackCommand extends Command {
    public static final String MESSAGE_USAGE = "hw feedback INDEX f/FEEDBACK";

    private final Index index;
    private final String feedback;

    /** Creates a feedback update against a previously displayed index. */
    public HomeworkFeedbackCommand(Index index, String feedback) {
        this.index = index;
        this.feedback = feedback;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Homework target = HomeworkCommandSupport.target(model, index);
        try {
            model.setHomework(target, target.withFeedback(feedback));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult("Feedback recorded for homework " + index.getOneBased() + ": "
                + target.getDescription());
    }
}
