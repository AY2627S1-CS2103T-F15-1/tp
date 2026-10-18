package seedu.address.logic.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.lesson.TravelWarnings;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/** Creates a finite recurring series only after validating the entire proposed batch. */
public class LessonRepeatCommand extends Command {
    public static final String MESSAGE_USAGE = "lesson repeat st/INDEX [s/SUBJECT] d/START t/TIME "
            + "[dur/MINUTES] [v/VENUE] count/COUNT [every/WEEKS]"
            + "\nCOUNT is 2-52; WEEKS is 1-12 (default 1). All occurrences must be within two years.";

    /** Number of overlap or travel warnings listed before the remainder is only counted. */
    private static final int MAX_SHOWN_WARNINGS = 5;

    private final Index studentIndex;
    private final Subject subject;
    private final LessonDate date;
    private final LessonTime time;
    private final LessonDuration duration;
    private final Venue venue;
    private final int count;
    private final int weeks;

    /** Stores the finite recurrence parameters. */
    public LessonRepeatCommand(Index studentIndex, Subject subject, LessonDate date, LessonTime time,
            LessonDuration duration, Venue venue, int count, int weeks) {
        this.studentIndex = studentIndex;
        this.subject = subject;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.venue = venue;
        this.count = count;
        this.weeks = weeks;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        if (count < 2 || count > 52 || weeks < 1 || weeks > 12) {
            throw new CommandException(MESSAGE_USAGE);
        }
        Person student = LessonCommandUtil.getStudent(model, studentIndex);
        if (student.isArchived()) {
            throw new CommandException("Restore this archived student before scheduling a new lesson.");
        }
        AddressBook proposed = new AddressBook(model.getAddressBook());
        UUID series = UUID.randomUUID();
        List<Lesson> added = new ArrayList<>();
        for (int occurrence = 0; occurrence < count; occurrence++) {
            LessonDate occurrenceDate = new LessonDate(date.value.plusWeeks((long) occurrence * weeks).toString());
            Lesson lesson = new LessonAddCommand(studentIndex, subject, occurrenceDate, time, duration, venue)
                    .createLesson(student).withSeriesId(series);
            if (proposed.hasLesson(lesson)) {
                throw new CommandException(LessonCommandUtil.MESSAGE_DUPLICATE_LESSON
                        + " No occurrences were added.");
            }
            proposed.addLesson(lesson);
            added.add(lesson);
        }
        model.setAddressBook(proposed);
        StringBuilder feedback = new StringBuilder("Scheduled " + count + " weekly occurrences.");
        appendOverlapWarnings(feedback, model, added);
        feedback.append(summarize(TravelWarnings.format(model.getAddressBook(), added)));

        List<Lesson> shown = model.getFilteredLessonList();
        if (added.stream().noneMatch(shown::contains)) {
            feedback.append("\nThe new lessons are not in the displayed list. Run 'agenda' or 'lesson list st/")
                    .append(studentIndex.getOneBased()).append("' to number them.");
            return new CommandResult(feedback.toString());
        }
        feedback.append(Messages.formatLessonList(shown));
        return new CommandResult(feedback.toString()).withDisplayedLessons(shown);
    }

    /** Adds the first few overlap warnings and counts the rest, so a long series cannot bury the result. */
    private static void appendOverlapWarnings(StringBuilder feedback, Model model, List<Lesson> added) {
        int shownWarnings = 0;
        int hiddenWarnings = 0;
        for (Lesson lesson : added) {
            String warning = LessonCommandUtil.getOverlapWarning(model, lesson);
            if (warning.isEmpty()) {
                continue;
            }
            if (shownWarnings < MAX_SHOWN_WARNINGS) {
                feedback.append(warning);
                shownWarnings++;
            } else {
                hiddenWarnings++;
            }
        }
        if (hiddenWarnings > 0) {
            feedback.append("\n... and ").append(hiddenWarnings)
                    .append(hiddenWarnings == 1 ? " more occurrence overlaps existing lessons."
                            : " more occurrences overlap existing lessons.");
        }
    }

    /** Keeps the first few lines of a multi-line warning block and counts the remainder. */
    private static String summarize(String warnings) {
        String[] lines = warnings.split("\n", -1);
        if (lines.length <= MAX_SHOWN_WARNINGS + 1) {
            return warnings;
        }
        String kept = String.join("\n", Arrays.copyOfRange(lines, 0, MAX_SHOWN_WARNINGS + 1));
        return kept + "\n... and " + (lines.length - MAX_SHOWN_WARNINGS - 1) + " more travel warnings.";
    }
}
