package seedu.address.logic.commands;

import java.time.LocalDate;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Person;

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
     * Returns the student at {@code studentIndex} in the displayed student list.
     *
     * @throws CommandException if the index is out of range.
     */
    static Person getStudent(Model model, Index studentIndex) throws CommandException {
        List<Person> shownStudents = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= shownStudents.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return shownStudents.get(studentIndex.getZeroBased());
    }

    /**
     * Returns the lesson at {@code lessonIndex} in the displayed lesson list.
     *
     * @throws CommandException if the index is out of range.
     */
    static Lesson getLesson(Model model, Index lessonIndex) throws CommandException {
        List<Lesson> shownLessons = model.getFilteredLessonList();
        if (lessonIndex.getZeroBased() >= shownLessons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }
        return shownLessons.get(lessonIndex.getZeroBased());
    }

    /**
     * Checks that {@code date} is not before today.
     *
     * @param message the message to show if it is.
     * @throws CommandException if the date is in the past.
     */
    static void requireNotInPast(LessonDate date, String message) throws CommandException {
        if (date.value.isBefore(LocalDate.now())) {
            throw new CommandException(message);
        }
    }

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
