package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Changes a student's archive state without deleting any linked history or commitments. */
public class ArchiveCommand extends Command {
    private final Index index;
    private final boolean archived;

    /** Creates an archive or restore command targeting the currently displayed student list. */
    public ArchiveCommand(Index index, boolean archived) {
        this.index = index;
        this.archived = archived;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person person = LessonCommandUtil.getStudent(model, index);
        if (person.isArchived() == archived) {
            throw new CommandException(archived ? "This student is already archived."
                    : "This student is already active. Use student list archived/ before restoring.");
        }
        model.setPerson(person, person.withArchived(archived));
        return new CommandResult((archived ? "Archived: " : "Restored: ") + person.getName()
                + ". Lessons and history are retained. The student list has refreshed.");
    }
}
