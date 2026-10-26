package seedu.address.logic.commands;

import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.lesson.TravelGap;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * Contains checks and messages that the commands which schedule or move a lesson share.
 */
final class LessonCommandUtil {

    static final String MESSAGE_DATE_TOO_FAR = "That date is more than 2 years away. Check the year.";
    static final String MESSAGE_DATE_TOO_FAR_PAST = "That date is more than 2 years in the past. Check the year.";
    static final String MESSAGE_SUBJECT_NOT_TAKEN = "%1$s is not recorded as taking %2$s. "
            + "Add the subject with 'edit' first.";
    static final String MESSAGE_SUBJECT_REQUIRED = "%1$s takes more than one subject (%2$s). "
            + "Specify one with " + PREFIX_SUBJECT + "SUBJECT.";
    static final String MESSAGE_DUPLICATE_LESSON = "This exact lesson already exists.";
    static final String MESSAGE_CHARGED_LESSON = "Cannot change a billed lesson. "
            + "Its financial history must be retained.";
    static final String MESSAGE_OVERLAP_WARNING = "\nWarning: this overlaps with %1$s";
    static final String MESSAGE_TRAVEL_WARNING = "Travel warning: %s at %s -> %s at %s (%s %s): "
            + "%d min available; %d min required.";

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
            throw new CommandException(Messages.invalidPersonIndex(studentIndex, shownStudents.size()));
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
            throw new CommandException(Messages.invalidLessonIndex(lessonIndex, shownLessons.size()));
        }
        return shownLessons.get(lessonIndex.getZeroBased());
    }

    /** Prevents changing a lesson once its charge has been recorded. */
    static void requireNotCharged(Model model, Lesson lesson) throws CommandException {
        boolean isCharged = model.getAddressBook().getLedgerEntries().stream()
                .anyMatch(entry -> entry.kind() == LedgerEntry.Kind.CHARGE
                        && lesson.getId().equals(entry.lessonId()));
        if (isCharged) {
            throw new CommandException(MESSAGE_CHARGED_LESSON);
        }
    }

    /**
     * Checks that {@code lesson} does not have the given {@code status}.
     *
     * @param message the message to show if it does.
     * @throws CommandException if the lesson has the status.
     */
    static void requireStatusNot(Lesson lesson, LessonStatus status, String message) throws CommandException {
        if (lesson.getStatus() == status) {
            throw new CommandException(message);
        }
    }

    /**
     * Checks that {@code lesson} is dated today or earlier, by the date of {@code clock}.
     *
     * @param messageFormat the message to show if it is not, where %1$s stands for the date of the lesson.
     * @throws CommandException if the lesson is dated after today.
     */
    static void requireNotInFuture(Lesson lesson, Clock clock, String messageFormat) throws CommandException {
        if (lesson.getDate().value.isAfter(LocalDate.now(clock))) {
            throw new CommandException(String.format(messageFormat, lesson.getDate()));
        }
    }

    /**
     * Checks that {@code date} is not before today.
     *
     * @param message the message to show if it is.
     * @throws CommandException if the date is in the past.
     */
    static void requireNotInPast(LessonDate date, String message) throws CommandException {
        requireNotInPast(date, message, Clock.systemDefaultZone());
    }

    /**
     * Checks that {@code date} is not before today, by the date of {@code clock}.
     *
     * @param message the message to show if it is.
     * @throws CommandException if the date is in the past.
     */
    static void requireNotInPast(LessonDate date, String message, Clock clock) throws CommandException {
        if (date.value.isBefore(LocalDate.now(clock))) {
            throw new CommandException(message);
        }
    }

    /**
     * Checks that {@code date} is today or earlier, by the date of {@code clock}.
     *
     * @param message the message to show if it is later.
     * @throws CommandException if the date is after today.
     */
    static void requireNotAfterToday(LessonDate date, String message, Clock clock) throws CommandException {
        if (date.value.isAfter(LocalDate.now(clock))) {
            throw new CommandException(message);
        }
    }

    /**
     * Checks that {@code date} is not so far in the past that it is probably a mistyped year.
     *
     * @throws CommandException if the date is more than 2 years before today.
     */
    static void requireNotTooFarPast(LessonDate date) throws CommandException {
        requireNotTooFarPast(date, Clock.systemDefaultZone());
    }

    /**
     * Checks that {@code date} is not so far in the past that it is probably a mistyped year, by the date of
     * {@code clock}.
     *
     * @throws CommandException if the date is more than 2 years before today.
     */
    static void requireNotTooFarPast(LessonDate date, Clock clock) throws CommandException {
        if (date.value.isBefore(LocalDate.now(clock).minusYears(MAX_YEARS_AHEAD))) {
            throw new CommandException(MESSAGE_DATE_TOO_FAR_PAST);
        }
    }

    /**
     * Returns the subject of {@code student} that the user asked for, or the only subject of {@code student} if
     * none was asked for. The lesson uses the student's own spelling, so {@code math} is shown as {@code Math}.
     *
     * @param requested the subject given by the user, or null if none was given.
     * @throws CommandException if no subject was given for a student with several, or the student does not take it.
     */
    static Subject chooseSubject(Person student, Subject requested) throws CommandException {
        if (requested == null && student.getSubjects().size() == 1) {
            return student.getSubjects().iterator().next();
        }
        if (requested == null) {
            String subjects = student.getSubjects().stream().map(Subject::toString)
                    .sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.joining(", "));
            throw new CommandException(String.format(MESSAGE_SUBJECT_REQUIRED, student.getName(), subjects));
        }
        return student.getSubjects().stream().filter(requested::equals).findFirst()
                .orElseThrow(() -> new CommandException(
                        String.format(MESSAGE_SUBJECT_NOT_TAKEN, student.getName(), requested)));
    }

    /**
     * Checks that {@code date} is not so far ahead that it is probably a mistyped year.
     *
     * @throws CommandException if the date is more than 2 years from today.
     */
    static void requireNearFuture(LessonDate date) throws CommandException {
        requireNearFuture(date, Clock.systemDefaultZone());
    }

    /**
     * Checks that {@code date} is not so far ahead that it is probably a mistyped year, by the date of
     * {@code clock}.
     *
     * @throws CommandException if the date is more than 2 years from today.
     */
    static void requireNearFuture(LessonDate date, Clock clock) throws CommandException {
        if (date.value.isAfter(LocalDate.now(clock).plusYears(MAX_YEARS_AHEAD))) {
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

    /** Reports travel warnings for the selected destination lessons without changing any displayed numbers. */
    static String getTravelWarnings(Model model, Predicate<Lesson> destinations) {
        return getTravelWarnings(model, destinations, "");
    }

    /** Reports destination warnings with the indentation used by their containing view. */
    static String getTravelWarnings(Model model, Predicate<Lesson> destinations, String indentation) {
        return formatTravelWarnings(model, gap -> destinations.test(gap.next()), indentation);
    }

    /** Reports only journeys into or out of the added or moved lesson, not unrelated warnings on that date. */
    static String getTravelWarningsForLesson(Model model, Lesson changed) {
        return getTravelWarningsForLessons(model, List.of(changed));
    }

    /** Reports each incoming or outgoing journey affected by a batch of new occurrences once. */
    static String getTravelWarningsForLessons(Model model, List<Lesson> changed) {
        return formatTravelWarnings(model, gap -> changed.stream().anyMatch(lesson ->
                gap.previous().getId().equals(lesson.getId()) || gap.next().getId().equals(lesson.getId())), "");
    }

    private static String formatTravelWarnings(Model model, Predicate<TravelGap> included, String indentation) {
        return TravelGap.find(model.getAddressBook().getLessonList(), model.getAddressBook().getTravelBuffer())
                .stream().filter(included)
                .map(gap -> "\n" + indentation + String.format(MESSAGE_TRAVEL_WARNING,
                        gap.previous().getStudentName(), Messages.formatVenue(gap.previous()),
                        gap.next().getStudentName(), Messages.formatVenue(gap.next()),
                        gap.next().getDate(), gap.next().getTime(), gap.minutes(), gap.buffer()))
                .collect(Collectors.joining());
    }
}
