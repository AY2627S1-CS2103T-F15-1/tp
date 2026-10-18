package seedu.address.logic.commands;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;

/** Cancels upcoming scheduled occurrences while retaining past teaching and financial history. */
public class LessonSeriesCancelCommand extends Command {
    public static final String MESSAGE_USAGE = "lesson series cancel LESSON_INDEX confirm"
            + "\nCancels this series' scheduled occurrences dated today or later, including moved occurrences.";
    private final Index index;
    private final Clock clock;

    /** Selects the series through any of its visible occurrences. */
    public LessonSeriesCancelCommand(Index index, Clock clock) {
        this.index = index;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        UUID series = LessonCommandUtil.getLesson(model, index).getSeriesId()
                .orElseThrow(() -> new CommandException("That lesson is not part of a recurring series."));
        List<Lesson> targets = model.getAddressBook().getLessonList().stream()
                .filter(lesson -> lesson.getSeriesId().filter(series::equals).isPresent())
                .filter(lesson -> lesson.isUpcoming(LocalDate.now(clock))).toList();
        if (targets.isEmpty()) {
            throw new CommandException("This series has no scheduled occurrences today or later.");
        }
        AddressBook proposed = new AddressBook(model.getAddressBook());
        for (Lesson lesson : targets) {
            FinanceCommandSupport.requireUncharged(model, lesson);
            proposed.setLesson(lesson, lesson.cancel("Series cancelled"));
        }
        model.setAddressBook(proposed);
        return new CommandResult("Cancelled " + targets.size() + " series occurrences."
                + Messages.formatLessonList(model.getFilteredLessonList()))
                .withDisplayedLessons(model.getFilteredLessonList());
    }
}
