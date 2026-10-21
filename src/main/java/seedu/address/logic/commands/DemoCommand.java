package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.util.SampleDataUtil;

/**
 * Lets a user look around with sample students without risking their own data. A demo stands in for the user's data
 * until it ends and is never saved, so closing TutorFlow in the middle of one loses nothing. The command previews the
 * demo, starts it with {@code confirm}, and leaves it with {@code exit}.
 */
public class DemoCommand extends Command {

    public static final String COMMAND_WORD = "demo";
    public static final String CONFIRM = "confirm";
    public static final String EXIT = "exit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " [" + CONFIRM + "|" + EXIT + "]\n"
            + COMMAND_WORD + ": Shows what the demo would do, starts it with " + CONFIRM + ", or leaves it with "
            + EXIT + ". The demo loads sample students for exploration. Your own data is kept safe and nothing "
            + "in the demo is saved.\n"
            + "Examples: " + COMMAND_WORD + ", " + COMMAND_WORD + " " + CONFIRM + ", " + COMMAND_WORD + " " + EXIT;

    public static final String MESSAGE_PREVIEW = "Load %1$d sample students for exploration. "
            + "Type demo confirm to load them.\n"
            + "Your own data is kept safe and nothing in the demo is saved. Type demo exit to go back to it.";
    public static final String MESSAGE_STARTED = "Sample students loaded. Try student list and student view 1.\n"
            + "Demo mode: nothing here is saved, and your own data is safe. Type demo exit to go back to it.";
    public static final String MESSAGE_ENDED = "Demo ended. Your own data is back.";
    public static final String MESSAGE_ALREADY_RUNNING =
            "You are already in the demo. Type demo exit to go back to your own data.";
    public static final String MESSAGE_NO_DEMO = "There is no demo to leave. Type demo to see what it would do.";

    private static final Logger logger = LogsCenter.getLogger(DemoCommand.class);

    /** What the user asked the demo to do. */
    public enum Action { PREVIEW, START, EXIT }

    private final Action action;

    /** Creates a command that starts the demo if {@code isConfirmed} is true, and only previews it otherwise. */
    public DemoCommand(boolean isConfirmed) {
        this(isConfirmed ? Action.START : Action.PREVIEW);
    }

    /** Creates a command that does {@code action}. */
    public DemoCommand(Action action) {
        requireNonNull(action);
        this.action = action;
    }

    /** Returns whether this command would start a demo, which a user with unsaved changes has to be stopped from. */
    public boolean isStart() {
        return action == Action.START;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (action == Action.EXIT) {
            if (!model.isDemoMode()) {
                throw new CommandException(MESSAGE_NO_DEMO);
            }
            model.endDemo();
            logger.fine("Left the demo");
            return new CommandResult(MESSAGE_ENDED);
        }
        if (model.isDemoMode()) {
            throw new CommandException(MESSAGE_ALREADY_RUNNING);
        }
        if (action == Action.PREVIEW) {
            return new CommandResult(String.format(MESSAGE_PREVIEW, SampleDataUtil.getSamplePersons().length))
                    .withConfirmationCommand(COMMAND_WORD + " " + CONFIRM);
        }
        model.startDemo(SampleDataUtil.getSampleAddressBook());
        logger.fine("Started the demo");
        return new CommandResult(MESSAGE_STARTED);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DemoCommand otherCommand)) {
            return false;
        }

        return action == otherCommand.action;
    }

    @Override
    public int hashCode() {
        return action.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("action", action)
                .toString();
    }
}
