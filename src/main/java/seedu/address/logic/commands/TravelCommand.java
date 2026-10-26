package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.lesson.TravelGap;

/** Configures the offline travel buffer and reports the journeys that are now too short. */
public class TravelCommand extends Command {
    public static final String COMMAND_WORD = "travel";
    public static final String MESSAGE_USAGE = "travel buffer/MINUTES (0-180, default 30).\n"
            + "Warns for different physical venues; does not block bookings or calculate routes.\n"
            + "Example: travel buffer/30";

    private final int minutes;

    /** Creates a buffer update after validating its inclusive bounds. */
    public TravelCommand(int minutes) {
        if (!TravelGap.isValidBuffer(minutes)) {
            throw new IllegalArgumentException(TravelGap.MESSAGE_CONSTRAINTS);
        }
        this.minutes = minutes;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setTravelBuffer(minutes);
        return new CommandResult("Travel buffer set to " + minutes + (minutes == 1 ? " minute." : " minutes.")
                + LessonCommandUtil.getTravelWarnings(model, lesson -> true));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TravelCommand command && minutes == command.minutes;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(minutes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("minutes", minutes).toString();
    }
}
