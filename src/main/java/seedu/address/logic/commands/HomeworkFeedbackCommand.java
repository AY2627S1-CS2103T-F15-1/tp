package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;

/** Records or replaces feedback on submitted or reviewed homework. */
public class HomeworkFeedbackCommand extends Command {
    public static final String MESSAGE_USAGE = "hw feedback INDEX f/FEEDBACK";

    public static final String MESSAGE_SUCCESS = "Feedback recorded for homework %1$d: %2$s";

    private static final Logger logger = LogsCenter.getLogger(HomeworkFeedbackCommand.class);

    private final Index index;
    private final String feedback;

    /** Creates a command that records {@code feedback} on the homework at {@code index} of the last list. */
    public HomeworkFeedbackCommand(Index index, String feedback) {
        requireNonNull(index);
        requireNonNull(feedback);
        this.index = index;
        this.feedback = feedback;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Homework target = HomeworkCommandSupport.target(model, index);
        if (target.getStatus() == HomeworkStatus.ASSIGNED) {
            throw new CommandException(Homework.MESSAGE_FEEDBACK_NOT_SUBMITTED);
        }
        model.setHomework(target, target.withFeedback(feedback));
        logger.fine("Recorded feedback on a homework item");
        return new CommandResult(String.format(MESSAGE_SUCCESS, index.getOneBased(), target.getDescription()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkFeedbackCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index) && feedback.equals(otherCommand.feedback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, feedback);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("feedback", feedback)
                .toString();
    }
}
