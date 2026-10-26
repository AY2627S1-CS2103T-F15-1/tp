package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.LessonCommandParser;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;

/**
 * Records completion and optional teaching notes for a lesson in the displayed lesson list.
 */
public class LessonDoneCommand extends Command {

    public static final String COMMAND_WORD = LessonCommandParser.COMMAND_WORD + " done";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Completes the lesson identified by the index "
            + "in the displayed lesson list. Notes are optional; giving notes again replaces the old notes.\n"
            + "Parameters: INDEX (must be a positive integer) [" + PREFIX_NOTES + "NOTES]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_NOTES + "Covered quadratic roots";
    public static final String MESSAGE_SUCCESS = "Completed: %1$s.";
    public static final String MESSAGE_UPDATED_NOTES = "Updated notes for: %1$s.";
    public static final String MESSAGE_ALREADY_COMPLETED = "That lesson is already completed. Existing notes retained.";
    public static final String MESSAGE_CANCELLED_LESSON = "Cannot complete a cancelled lesson.";
    public static final String MESSAGE_NO_CHARGE =
            "No automatic charge: The lesson fee is $0.00. No charge was recorded.";
    public static final String MESSAGE_NO_CHARGE_ZERO_RATE =
            "No automatic charge: This student's rate is $0.00. Specify an amount with amt/ if you want to charge "
                    + "for this lesson. Use charge with an amount.";
    public static final String MESSAGE_FUTURE_LESSON =
            "That lesson is scheduled for %1$s. Mark it complete on or after that date.";

    private static final Logger logger = LogsCenter.getLogger(LessonDoneCommand.class);

    private final Index lessonIndex;
    private final String notes; // null if no notes were given
    private final Clock clock;

    /**
     * Creates a command to complete the lesson at {@code lessonIndex}, using the system date.
     */
    public LessonDoneCommand(Index lessonIndex, String notes) {
        this(lessonIndex, notes, Clock.systemDefaultZone());
    }

    /**
     * Creates a command with an explicit clock so the date check can be tested independently of the system date.
     */
    public LessonDoneCommand(Index lessonIndex, String notes, Clock clock) {
        requireNonNull(lessonIndex);
        requireNonNull(clock);
        this.lessonIndex = lessonIndex;
        this.notes = notes;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Lesson target = LessonCommandUtil.getLesson(model, lessonIndex);
        LessonCommandUtil.requireStatusNot(target, LessonStatus.CANCELLED, MESSAGE_CANCELLED_LESSON);
        LessonCommandUtil.requireNotInFuture(target, clock, MESSAGE_FUTURE_LESSON);
        if (target.getStatus() == LessonStatus.COMPLETED && notes == null) {
            return new CommandResult(MESSAGE_ALREADY_COMPLETED);
        }

        boolean isZeroRate = model.getAddressBook().getPersonList().stream()
                .filter(person -> target.getStudentId().filter(person.getId()::equals).isPresent())
                .anyMatch(person -> person.getRate().value.signum() == 0);
        Optional<LedgerEntry> charge = Optional.empty();
        try {
            if (target.getStatus() == LessonStatus.COMPLETED) {
                model.setLesson(target, target.complete(notes));
            } else {
                charge = model.chargeLesson(target, target.complete(notes));
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Could not complete lesson due to a model validation failure");
            throw new CommandException(e.getMessage(), e);
        }
        logger.fine("Completed lesson");
        String message = target.getStatus() == LessonStatus.COMPLETED ? MESSAGE_UPDATED_NOTES : MESSAGE_SUCCESS;
        String feedback = String.format(message, Messages.format(target));
        if (target.getStatus() != LessonStatus.COMPLETED) {
            String chargeMessage = charge.map(entry -> "Charge of " + Ledger.format(entry.amount()) + " recorded.")
                    .orElse(isZeroRate ? MESSAGE_NO_CHARGE_ZERO_RATE : MESSAGE_NO_CHARGE);
            feedback += "\n" + chargeMessage;
        }
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LessonDoneCommand otherCommand)) {
            return false;
        }
        return lessonIndex.equals(otherCommand.lessonIndex) && Objects.equals(notes, otherCommand.notes)
                && clock.equals(otherCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("lessonIndex", lessonIndex).add("notes", notes).toString();
    }
}
