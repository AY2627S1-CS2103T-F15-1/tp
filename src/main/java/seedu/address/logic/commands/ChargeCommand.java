package seedu.address.logic.commands;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.finance.Money;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/** Records a manual lesson charge or appends its explicit reversal. */
public class ChargeCommand extends Command {
    public static final String MESSAGE_USAGE = "charge l/LESSON_INDEX [amt/AMOUNT] | charge reverse l/LESSON_INDEX";
    private final Index lessonIndex;
    private final BigDecimal amount;
    private final boolean reverse;
    private final Clock clock;

    /** Creates a charge or reversal with an injectable date boundary. */
    public ChargeCommand(Index lessonIndex, BigDecimal amount, boolean reverse, Clock clock) {
        this.lessonIndex = lessonIndex;
        this.amount = amount == null ? null : Money.positive(amount);
        this.reverse = reverse;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Lesson lesson = LessonCommandUtil.getLesson(model, lessonIndex);
        Person student = FinanceCommandSupport.lessonStudent(model, lesson);
        LedgerEntry active = Ledger.activeCharge(model.getAddressBook(), lesson.getId()).orElse(null);
        if (reverse) {
            if (active == null) {
                throw new CommandException("That lesson has no active charge; it may already have been reversed.");
            }
            model.addLedgerEntry(LedgerEntry.reverse(active, LocalDate.now(clock)));
            return new CommandResult("Charge of " + Money.format(active.amount()) + " reversed for "
                    + student.getName() + ".");
        }
        FinanceCommandSupport.requireChargeable(lesson, clock);
        if (active != null) {
            throw new CommandException("That lesson already has a charge of " + Money.format(active.amount()) + ".");
        }
        if (amount == null && student.getRate().value.signum() == 0) {
            throw new CommandException(FinanceCommandSupport.ZERO_RATE);
        }
        if (amount == null && Money.charge(student.getRate().value, lesson.getDuration().value).signum() == 0) {
            throw new CommandException("Computed charge rounds to $0.00. Specify a positive amt/ override.");
        }
        LedgerEntry entry = LedgerEntry.charge(lesson, student, amount);
        model.addLedgerEntry(entry);
        return new CommandResult("Charge of " + Money.format(entry.amount()) + " recorded for " + student.getName()
                + " — " + lesson.getSubject() + ", " + lesson.getDate() + ".");
    }
}
