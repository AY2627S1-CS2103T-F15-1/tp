package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.task.Task;

/**
 * Deletes a student identified using the index shown in the displayed student list.
 * A student with lessons, homework, follow-up tasks or financial events requires confirmation, because deleting
 * a student also deletes all of those records and this cannot be undone.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";
    public static final String CONFIRM_KEYWORD = "confirm";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the student identified by the index number used in the displayed student list.\n"
            + "Parameters: INDEX (must be a positive integer) [" + CONFIRM_KEYWORD + "]\n"
            + "Example: " + COMMAND_WORD + " 1\n"
            + "Linked lessons, homework, tasks, and financial history require '" + CONFIRM_KEYWORD + "', "
            + "e.g. " + COMMAND_WORD + " 1 " + CONFIRM_KEYWORD;

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted student: %1$s";
    public static final String MESSAGE_DELETE_PERSON_WITH_LESSONS_SUCCESS =
            "Deleted student: %1$s, along with %2$d lesson(s).";
    public static final String MESSAGE_CONFIRMATION = "%1$s (phone: %2$s) has %3$d lesson(s). "
            + "These will all be deleted.\n%4$sType '" + COMMAND_WORD + " %5$d " + CONFIRM_KEYWORD + "' to proceed.";
    public static final String MESSAGE_UPCOMING_WARNING = "Warning: this student has %1$d upcoming lesson(s).\n";
    public static final String MESSAGE_BALANCE = "\nBalance: %1$s.";
    public static final String MESSAGE_ARCHIVE_ADVICE =
            "\nUse 'archive INDEX' to retain these obligations and all history instead.";

    private final Index targetIndex;
    private final boolean isConfirmed;

    /**
     * Creates a {@code DeleteCommand} that deletes the student at {@code targetIndex} only if the student
     * has no lessons.
     */
    public DeleteCommand(Index targetIndex) {
        this(targetIndex, false);
    }

    /**
     * Creates a {@code DeleteCommand} that deletes the student at {@code targetIndex}.
     * A student who has lessons, homework or tasks is only deleted if {@code isConfirmed} is true.
     */
    public DeleteCommand(Index targetIndex, boolean isConfirmed) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.invalidPersonIndex(targetIndex, lastShownList.size()));
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        LinkedRecords records = LinkedRecords.of(model, personToDelete);

        if (records.isEmpty()) {
            model.deletePerson(personToDelete);
            return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
        }

        if (!isConfirmed) {
            return new CommandResult(getConfirmationPreview(personToDelete, records));
        }

        model.deletePerson(personToDelete);
        return new CommandResult(getSuccessMessage(personToDelete, records));
    }

    /**
     * Returns what deleting {@code student} would delete along with them, and how to confirm. A student with
     * homework or financial events also gets one sentence counting both, the balance (when there are financial
     * events) and the advice to archive instead. A student with follow-up tasks also gets a separate line with
     * the count of those tasks, placed before the balance.
     */
    private String getConfirmationPreview(Person student, LinkedRecords records) {
        String preview = getConfirmationPrompt(student, records.lessons());
        if (records.hasHomework() || records.financialEvents() > 0) {
            preview += "\n" + describeHistory(records);
        }
        if (records.hasTasks()) {
            preview += "\nThis also deletes " + records.tasks().size() + " task(s).";
        }
        if (records.financialEvents() > 0) {
            preview += String.format(MESSAGE_BALANCE, Ledger.describeBalance(records.balance()));
        }
        return records.hasHomework() || records.financialEvents() > 0 ? preview + MESSAGE_ARCHIVE_ADVICE : preview;
    }

    /**
     * Returns the line that counts the homework and financial events that would be deleted, in the form the
     * specification gives: "This also deletes 0 homework item(s), including 0 upcoming homework item(s), and 1
     * financial event(s)." The financial count is always given, even when it is 0.
     * Feedback is mentioned only when there is some, because it is the part of the history that took thought to
     * write.
     */
    private static String describeHistory(LinkedRecords records) {
        long upcoming = records.homework().stream().filter(Homework::isOutstanding)
                .filter(item -> !item.getDueDate().isBefore(LocalDate.now())).count();
        String line = "This also deletes " + records.homework().size() + " homework item(s), including " + upcoming
                + " upcoming homework item(s), and " + records.financialEvents() + " financial event(s).";
        long withFeedback = records.homework().stream().filter(item -> item.getFeedback().isPresent()).count();
        return withFeedback == 0
                ? line
                : line + " Recorded feedback on " + withFeedback + " homework item(s) will be deleted.";
    }

    /** Returns what was deleted along with {@code student}, listing every kind of record that was present. */
    private static String getSuccessMessage(Person student, LinkedRecords records) {
        if (!records.hasHomework() && !records.hasTasks() && records.financialEvents() == 0) {
            return String.format(MESSAGE_DELETE_PERSON_WITH_LESSONS_SUCCESS, Messages.format(student),
                    records.lessons().size());
        }
        List<String> parts = new ArrayList<>();
        parts.add(records.lessons().size() + " lesson(s)");
        if (records.hasHomework() || records.financialEvents() > 0) {
            parts.add(records.homework().size() + " homework item(s)");
        }
        if (records.hasTasks()) {
            parts.add(records.tasks().size() + " task(s)");
        }
        if (records.financialEvents() > 0) {
            parts.add(records.financialEvents() + " financial event(s)");
        }
        String last = parts.remove(parts.size() - 1);
        String linked = parts.isEmpty() ? last : String.join(", ", parts) + " and " + last;
        return "Deleted student: " + Messages.format(student) + ", along with " + linked + ".";
    }

    /** The records that are deleted together with a student. */
    private record LinkedRecords(List<Lesson> lessons, List<Homework> homework, List<Task> tasks,
            long financialEvents, BigDecimal balance) {
        static LinkedRecords of(Model model, Person student) {
            List<Lesson> lessons = model.getAddressBook().getLessonList().stream()
                    .filter(lesson -> lesson.isWith(student)).toList();
            List<Homework> homework = model.getAddressBook().getHomeworkList().stream()
                    .filter(item -> item.getStudentId().equals(student.getId())).toList();
            List<Task> tasks = model.getAddressBook().getTaskList().stream()
                    .filter(task -> task.isAbout(student)).toList();
            long financialEvents = model.getAddressBook().getLedgerEntries().stream()
                    .filter(entry -> entry.isWith(student)).count();
            return new LinkedRecords(lessons, homework, tasks, financialEvents,
                    Ledger.balance(model.getAddressBook(), student));
        }

        boolean hasHomework() {
            return !homework.isEmpty();
        }

        boolean hasTasks() {
            return !tasks.isEmpty();
        }

        boolean isEmpty() {
            return lessons.isEmpty() && homework.isEmpty() && tasks.isEmpty() && financialEvents == 0;
        }
    }

    /**
     * Returns the message that asks the user to confirm deleting {@code student} and their {@code lessons}.
     * The message shows the name and phone number of the student, so that the user can check that the right
     * student is about to be deleted.
     */
    private String getConfirmationPrompt(Person student, List<Lesson> lessons) {
        LocalDate today = LocalDate.now();
        long upcomingCount = lessons.stream().filter(lesson -> lesson.isUpcoming(today)).count();
        String upcomingWarning = upcomingCount == 0
                ? ""
                : String.format(MESSAGE_UPCOMING_WARNING, upcomingCount);
        return String.format(MESSAGE_CONFIRMATION, student.getName(), student.getPhone(), lessons.size(),
                upcomingWarning, targetIndex.getOneBased());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex)
                && isConfirmed == otherDeleteCommand.isConfirmed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex, isConfirmed);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("isConfirmed", isConfirmed)
                .toString();
    }
}
