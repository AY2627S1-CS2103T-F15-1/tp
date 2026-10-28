package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;

/**
 * Records a charge for a lesson by hand, or reverses the active charge of a lesson.
 *
 * Charges are normally raised by {@code lesson done}. The command covers the manual and corrective cases: billing a
 * missed lesson, an agreed discount, or a lesson of a student whose rate is zero. A charge is never deleted. It is
 * reversed by a further entry, so the account shows that a correction happened.
 */
public class ChargeCommand extends Command {
    public static final String COMMAND_WORD = "charge";
    public static final String REVERSE_KEYWORD = "reverse";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Records a charge for a completed or missed lesson in "
            + "the displayed lesson list. Without amt/ the fee is the student's rate times the lesson's duration.\n"
            + "Parameters: l/LESSON_INDEX [amt/AMOUNT]\n"
            + "Example: " + COMMAND_WORD + " l/1 amt/50\n"
            + "To correct a charge: " + COMMAND_WORD + " " + REVERSE_KEYWORD + " l/LESSON_INDEX";
    public static final String MESSAGE_SUBCOMMANDS =
            "Charge commands: " + COMMAND_WORD + " l/LESSON_INDEX [amt/AMOUNT], "
                    + COMMAND_WORD + " " + REVERSE_KEYWORD + " l/LESSON_INDEX";
    public static final String MESSAGE_SUCCESS = "Charge of %1$s recorded for %2$s — %3$s, %4$s.";
    public static final String MESSAGE_REVERSED = "Charge of %1$s reversed for %2$s.";
    public static final String MESSAGE_ALREADY_CHARGED = "That lesson already has a charge of %1$s.";
    public static final String MESSAGE_CANCELLED_LESSON = "Cannot charge a cancelled lesson.";
    public static final String MESSAGE_NOT_HAPPENED = "Cannot charge a lesson scheduled for %1$s.";
    public static final String MESSAGE_ZERO_RATE = "This student's rate is $0.00. "
            + "Specify an amount with amt/ if you want to charge for this lesson.";
    public static final String MESSAGE_NO_ACTIVE_CHARGE =
            "That lesson has no active charge; it may already have been reversed.";

    private final Index lessonIndex;
    private final BigDecimal amount;
    private final boolean isReversal;
    private final Clock clock;

    /** Charges the lesson at {@code lessonIndex}; a null {@code amount} means the student's fee for the lesson. */
    public ChargeCommand(Index lessonIndex, BigDecimal amount) {
        this(lessonIndex, amount, false, Clock.systemDefaultZone());
    }

    /** Creates a charge command that reverses the active charge instead when {@code isReversal} is true. */
    public ChargeCommand(Index lessonIndex, BigDecimal amount, boolean isReversal, Clock clock) {
        requireNonNull(lessonIndex);
        requireNonNull(clock);
        if (isReversal && amount != null) {
            throw new IllegalArgumentException("A reversal does not take an amount.");
        }
        if (amount != null && (amount.signum() <= 0 || amount.scale() > 2)) {
            throw new IllegalArgumentException(PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        this.lessonIndex = lessonIndex;
        this.amount = amount == null ? null : amount.setScale(2);
        this.isReversal = isReversal;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Lesson lesson = LessonCommandUtil.getLesson(model, lessonIndex);
        Person student = model.getAddressBook().getPersonList().stream().filter(lesson::isWith).findFirst()
                .orElseThrow(() -> new CommandException("This lesson's student no longer exists."));
        return isReversal ? reverse(model, lesson, student) : charge(model, lesson, student);
    }

    private CommandResult charge(Model model, Lesson lesson, Person student) throws CommandException {
        if (lesson.getStatus() == LessonStatus.CANCELLED) {
            throw new CommandException(MESSAGE_CANCELLED_LESSON);
        }
        if (lesson.getStatus() == LessonStatus.SCHEDULED) {
            throw new CommandException(String.format(MESSAGE_NOT_HAPPENED, lesson.getDate()));
        }
        LedgerEntry existing = Ledger.activeCharge(model.getAddressBook(), lesson.getId()).orElse(null);
        if (existing != null) {
            throw new CommandException(String.format(MESSAGE_ALREADY_CHARGED, Ledger.format(existing.amount())));
        }
        BigDecimal fee = amount != null ? amount : LedgerEntry.lessonFee(student, lesson);
        if (fee.signum() == 0) {
            throw new CommandException(MESSAGE_ZERO_RATE);
        }
        try {
            model.addLedgerEntry(LedgerEntry.charge(student, lesson, fee));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, Ledger.format(fee), student.getName(),
                lesson.getSubject(), lesson.getDate()));
    }

    private CommandResult reverse(Model model, Lesson lesson, Person student) throws CommandException {
        LedgerEntry active = Ledger.activeCharge(model.getAddressBook(), lesson.getId())
                .orElseThrow(() -> new CommandException(MESSAGE_NO_ACTIVE_CHARGE));
        try {
            model.addLedgerEntry(LedgerEntry.reversalOf(active, LocalDate.now(clock)));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_REVERSED, Ledger.format(active.amount()), student.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ChargeCommand otherCommand)) {
            return false;
        }
        return lessonIndex.equals(otherCommand.lessonIndex) && Objects.equals(amount, otherCommand.amount)
                && isReversal == otherCommand.isReversal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lessonIndex, amount, isReversal);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("lessonIndex", lessonIndex).add("amount", amount)
                .add("isReversal", isReversal).toString();
    }
}
