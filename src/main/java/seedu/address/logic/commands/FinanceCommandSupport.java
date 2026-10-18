package seedu.address.logic.commands;

import java.time.Clock;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;

/** Shared finance selections and lesson charge guards. Archived obligations remain usable. */
final class FinanceCommandSupport {
    static final String ZERO_RATE = "This student's rate is $0.00. Specify an amount with amt/ if you want to charge "
            + "for this lesson.";

    private FinanceCommandSupport() {
    }

    /** Resolves a displayed student without excluding archived history. */
    static Person student(Model model, Index index) throws CommandException {
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.invalidPersonIndex(index, model.getFilteredPersonList().size()));
        }
        return model.getFilteredPersonList().get(index.getZeroBased());
    }

    /** Resolves the lesson's stable student link, including explicit legacy compatibility. */
    static Person lessonStudent(Model model, Lesson lesson) throws CommandException {
        return model.getAddressBook().getPersonList().stream().filter(lesson::isWith).findFirst()
                .orElseThrow(() -> new CommandException("That lesson's student no longer exists."));
    }

    /** Prevents charges for cancelled lessons or lessons after the local calendar day. */
    static void requireChargeable(Lesson lesson, Clock clock) throws CommandException {
        LessonCommandUtil.requireStatusNot(lesson, LessonStatus.CANCELLED, "Cannot charge a cancelled lesson.");
        LessonCommandUtil.requireNotInFuture(lesson, clock, "Cannot charge a lesson scheduled for %1$s.");
    }

    /** Requires explicit financial correction before changing a charged booking. */
    static void requireUncharged(Model model, Lesson lesson) throws CommandException {
        if (Ledger.activeCharge(model.getAddressBook(), lesson.getId()).isPresent()) {
            throw new CommandException("Reverse the active charge with 'charge reverse l/INDEX' before cancelling "
                    + "or rescheduling this lesson.");
        }
    }
}
