package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Changes a student's archive state without deleting any linked history or commitments. */
public class ArchiveCommand extends Command {
    public static final String COMMAND_WORD_ARCHIVE = "archive";
    public static final String COMMAND_WORD_RESTORE = "restore";

    public static final String MESSAGE_USAGE_ARCHIVE = COMMAND_WORD_ARCHIVE + " INDEX";
    public static final String MESSAGE_USAGE_RESTORE = COMMAND_WORD_RESTORE
            + " INDEX (after 'list archived/' or 'list all/')";

    public static final String MESSAGE_ALREADY_ARCHIVED = "This student is already archived.";
    public static final String MESSAGE_ALREADY_ACTIVE =
            "This student is already active. Use student list archived/ before restoring.";
    public static final String MESSAGE_ARCHIVED = "Archived: %1$s. Lessons and history are retained. "
            + "The student list has refreshed.";
    public static final String MESSAGE_RESTORED = "Restored: %1$s. Lessons and history are retained. "
            + "The student list has refreshed.";

    private static final Logger logger = LogsCenter.getLogger(ArchiveCommand.class);

    private final Index index;
    private final boolean shouldArchive;

    /**
     * Creates a command that archives the student at {@code index} of the displayed student list if
     * {@code shouldArchive} is true, and restores that student otherwise.
     */
    public ArchiveCommand(Index index, boolean shouldArchive) {
        requireNonNull(index);
        this.index = index;
        this.shouldArchive = shouldArchive;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = LessonCommandUtil.getStudent(model, index);
        if (student.isArchived() == shouldArchive) {
            throw new CommandException(shouldArchive ? MESSAGE_ALREADY_ARCHIVED : MESSAGE_ALREADY_ACTIVE);
        }
        model.setPerson(student, student.withArchived(shouldArchive));
        logger.fine(shouldArchive ? "Archived a student" : "Restored a student");
        return new CommandResult(String.format(shouldArchive ? MESSAGE_ARCHIVED : MESSAGE_RESTORED,
                student.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ArchiveCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index) && shouldArchive == otherCommand.shouldArchive;
    }

    @Override
    public int hashCode() {
        return 31 * index.hashCode() + Boolean.hashCode(shouldArchive);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("shouldArchive", shouldArchive)
                .toString();
    }
}
