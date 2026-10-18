package seedu.address.logic.commands;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.lesson.TravelWarnings;

/** Sets a persisted global offline travel buffer. */
public class TravelCommand extends Command {
    public static final String MESSAGE_USAGE = "travel buffer/MINUTES (0-180, default 30)."
            + "\nWarns for different physical venues; does not block bookings or calculate routes.";
    private final int minutes;

    /** Creates a settings change; the root model validates its bounds. */
    public TravelCommand(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public CommandResult execute(Model model) {
        AddressBook proposed = new AddressBook(model.getAddressBook());
        proposed.setTravelBufferMinutes(minutes);
        model.setAddressBook(proposed);
        return new CommandResult("Travel buffer set to " + minutes + " minutes."
                + TravelWarnings.format(model.getAddressBook(), model.getFilteredLessonList()));
    }
}
