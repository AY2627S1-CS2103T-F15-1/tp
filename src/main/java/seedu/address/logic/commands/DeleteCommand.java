package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Deletes a student identified using the index shown in the displayed student list.
 * A student who has lessons is only deleted when the command is confirmed, because deleting a student
 * also deletes all of their lessons and this cannot be undone.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";
    public static final String CONFIRM_KEYWORD = "confirm";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the student identified by the index number used in the displayed student list.\n"
            + "Parameters: INDEX (must be a positive integer) [" + CONFIRM_KEYWORD + "]\n"
            + "Example: " + COMMAND_WORD + " 1\n"
            + "A student who has lessons is only deleted if you add '" + CONFIRM_KEYWORD + "', "
            + "e.g. " + COMMAND_WORD + " 1 " + CONFIRM_KEYWORD;

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted student: %1$s";
    public static final String MESSAGE_DELETE_PERSON_WITH_LESSONS_SUCCESS =
            "Deleted student: %1$s, along with %2$d lesson(s).";
    public static final String MESSAGE_CONFIRMATION = "%1$s (phone: %2$s) has %3$d lesson(s). "
            + "These will all be deleted.\n%4$sType '" + COMMAND_WORD + " %5$d " + CONFIRM_KEYWORD + "' to proceed.";
    public static final String MESSAGE_UPCOMING_WARNING = "Warning: this student has %1$d upcoming lesson(s).\n";

    private final Index targetIndex;
    private final boolean isConfirmed;

    /**
     * Creates a {@code DeleteCommand} that deletes the student at {@code targetIndex} only if the student
     * has no lessons.
     */
    public DeleteCommand(Index targetIndex) {
        this(targetIndex, false);
    }

    /**
     * Creates a {@code DeleteCommand} that deletes the student at {@code targetIndex}.
     * A student who has lessons is only deleted if {@code isConfirmed} is true.
     */
    public DeleteCommand(Index targetIndex, boolean isConfirmed) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        List<Lesson> lessonsToDelete = model.getAddressBook().getLessonList().stream()
                .filter(lesson -> lesson.isWith(personToDelete))
                .toList();

        if (lessonsToDelete.isEmpty()) {
            model.deletePerson(personToDelete);
            return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
        }

        if (!isConfirmed) {
            return new CommandResult(getConfirmationPrompt(personToDelete, lessonsToDelete));
        }

        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_WITH_LESSONS_SUCCESS,
                Messages.format(personToDelete), lessonsToDelete.size()));
    }

    /**
     * Returns the message that asks the user to confirm deleting {@code student} and their {@code lessons}.
     * The message shows the name and phone number of the student, so that the user can check that the right
     * student is about to be deleted.
     */
    private String getConfirmationPrompt(Person student, List<Lesson> lessons) {
        LocalDate today = LocalDate.now();
        long upcomingCount = lessons.stream().filter(lesson -> lesson.isUpcoming(today)).count();
        String upcomingWarning = upcomingCount == 0
                ? ""
                : String.format(MESSAGE_UPCOMING_WARNING, upcomingCount);
        return String.format(MESSAGE_CONFIRMATION, student.getName(), student.getPhone(), lessons.size(),
                upcomingWarning, targetIndex.getOneBased());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex)
                && isConfirmed == otherDeleteCommand.isConfirmed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("isConfirmed", isConfirmed)
                .toString();
    }
}
