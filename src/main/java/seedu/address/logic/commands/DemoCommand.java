package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.util.SampleDataUtil;

/** Loads the existing sample dataset only after confirmation and only into an empty dataset. */
public class DemoCommand extends Command {
    private final boolean confirmed;

    /** Creates a sample-data preview or a confirmed load. */
    public DemoCommand(boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        if (!model.getAddressBook().getPersonList().isEmpty() || !model.getAddressBook().getLessonList().isEmpty()
                || !model.getAddressBook().getHomeworkList().isEmpty()) {
            throw new CommandException("Demo requires an empty dataset. Your records have been retained.");
        }
        if (!confirmed) {
            return new CommandResult("Load " + SampleDataUtil.getSamplePersons().length
                    + " sample students for exploration. Type demo confirm to load them.");
        }
        model.setAddressBook(SampleDataUtil.getSampleAddressBook());
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ACTIVE_PERSONS);
        return new CommandResult("Sample students loaded. Try student list and student view 1.");
    }
}
