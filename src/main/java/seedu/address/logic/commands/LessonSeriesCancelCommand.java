package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;

/** Cancels every not-yet-started scheduled occurrence of a series as one model update. */
public class LessonSeriesCancelCommand extends Command {
    public static final String MESSAGE_USAGE = "lesson series cancel LESSON_INDEX confirm\n"
            + "Cancels all scheduled occurrences of that series starting now or later; retains past history.";
    public static final String MESSAGE_NOT_SERIES = "That lesson is not part of a recurring series.";
    public static final String MESSAGE_NONE = "This series has no scheduled occurrences starting now or later.";

    private final Index index;
    private final Clock clock;

    /** Uses the local system clock to decide whether an occurrence has started. */
    public LessonSeriesCancelCommand(Index index) {
        this(index, Clock.systemDefaultZone());
    }

    /** Uses an explicit clock for deterministic boundary tests. */
    public LessonSeriesCancelCommand(Index index, Clock clock) {
        this.index = requireNonNull(index);
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Lesson target = LessonCommandUtil.getLesson(model, index);
        String seriesId = target.getSeriesId().orElseThrow(() -> new CommandException(MESSAGE_NOT_SERIES));
        LocalDateTime now = LocalDateTime.now(clock);
        List<Lesson> affected = model.getAddressBook().getLessonList().stream()
                .filter(lesson -> lesson.getSeriesId().filter(seriesId::equals).isPresent())
                .filter(Lesson::isScheduled)
                .filter(lesson -> !LocalDateTime.of(lesson.getDate().value, lesson.getTime().value).isBefore(now))
                .toList();
        if (affected.isEmpty()) {
            throw new CommandException(MESSAGE_NONE);
        }

        // Prepare the complete result first: an error cannot leave a partially cancelled series in the model.
        AddressBook updated = new AddressBook(model.getAddressBook());
        for (Lesson lesson : affected) {
            updated.setLesson(lesson, lesson.cancel("Series cancelled"));
        }
        List<UUID> previousNumbers = LessonCommandUtil.displayedLessonIds(model);
        model.setAddressBook(updated);
        return new CommandResult("Cancelled " + affected.size() + " series occurrences."
                + LessonCommandUtil.getChangedLessonNumbers(model, previousNumbers));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LessonSeriesCancelCommand command
                && index.equals(command.index) && clock.equals(command.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, clock);
    }
}
