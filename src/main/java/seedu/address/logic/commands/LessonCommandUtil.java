package seedu.address.logic.commands;

import java.time.LocalDate;
import java.util.List;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;

/**
 * Contains checks and messages that the commands which schedule or move a lesson share.
 */
final class LessonCommandUtil {

    static final String MESSAGE_DATE_TOO_FAR = "That date is more than 2 years away. Check the year.";
    static final String MESSAGE_DUPLICATE_LESSON = "This exact lesson already exists.";
    static final String MESSAGE_OVERLAP_WARNING = "\nWarning: this overlaps with %1$s";

    private static final int MAX_YEARS_AHEAD = 2;

    private LessonCommandUtil() {} // prevents instantiation

    /**
     * Checks that {@code date} is not so far ahead that it is probably a mistyped year.
     *
     * @throws CommandException if the date is more than 2 years from today.
     */
    static void requireNearFuture(LessonDate date) throws CommandException {
        if (date.value.isAfter(LocalDate.now().plusYears(MAX_YEARS_AHEAD))) {
            throw new CommandException(MESSAGE_DATE_TOO_FAR);
        }
    }

    /**
     * Checks that a lesson that starts at {@code time} and lasts for {@code duration} ends on the same day.
     *
     * @throws CommandException if the lesson would run past midnight.
     */
    static void requireEndsBeforeMidnight(LessonTime time, LessonDuration duration) throws CommandException {
        if (!Lesson.isValidTimeRange(time, duration)) {
            throw new CommandException(Lesson.MESSAGE_CONSTRAINTS);
        }
    }

    /**
     * Returns a warning that names the lessons that {@code lesson} overlaps with, or an empty string if it
     * overlaps with none. An overlap only warns, because the tutor may double book on purpose.
     */
    static String getOverlapWarning(Model model, Lesson lesson) {
        List<Lesson> overlappingLessons = model.getConflictingLessons(lesson);
        if (overlappingLessons.isEmpty()) {
            return "";
        }
        return String.format(MESSAGE_OVERLAP_WARNING, Messages.formatOverlaps(overlappingLessons));
    }
}
