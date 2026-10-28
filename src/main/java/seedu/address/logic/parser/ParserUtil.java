package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Rate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_MISSING_PARAMETERS = "Missing required parameter(s): %1$s\n%2$s";
    public static final int MAX_SUBJECTS_PER_STUDENT = Person.MAX_SUBJECTS;
    public static final String MESSAGE_TOO_MANY_SUBJECTS = "A student can have at most "
            + MAX_SUBJECTS_PER_STUDENT + " subjects.";
    public static final String MESSAGE_FLAG_TAKES_NO_VALUE = "The '%1$s' flag does not take a value.";

    private static final String KEYWORD_TODAY = "today";
    private static final String KEYWORD_TOMORROW = "tmr";
    /** The word "s/o" ("son of"), which a name may contain although {@code s/} is the subject prefix. */
    private static final Pattern SON_OF = Pattern.compile("(?<=\\s)s/o(?=\\s|$)");
    private static final char SON_OF_MASK = '\uE000';

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses {@code oneBasedIndex} as {@link #parseIndex(String)} does, but reports an invalid index as an invalid
     * command format that shows {@code usage}, as every command that takes an index does.
     *
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex, String usage) throws ParseException {
        try {
            return parseIndex(oneBasedIndex);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage), pe);
        }
    }

    /**
     * Returns the error for a command whose arguments are malformed: the invalid-format line followed by
     * {@code usage}, as required for every known command.
     */
    public static ParseException invalidFormat(String usage) {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        requirePrintable(name, Name.MESSAGE_CONSTRAINTS);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String level} into a {@code Level}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code level} is invalid.
     */
    public static Level parseLevel(String level) throws ParseException {
        requireNonNull(level);
        String trimmedLevel = level.trim();
        if (!Level.isValidLevel(trimmedLevel)) {
            throw new ParseException(Level.MESSAGE_CONSTRAINTS);
        }
        return new Level(trimmedLevel);
    }

    /**
     * Parses a {@code String subject} into a {@code Subject}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code subject} is invalid.
     */
    public static Subject parseSubject(String subject) throws ParseException {
        requireNonNull(subject);
        String trimmedSubject = subject.trim();
        if (!Subject.isValidSubject(trimmedSubject)) {
            throw new ParseException(Subject.MESSAGE_CONSTRAINTS);
        }
        return new Subject(trimmedSubject);
    }

    /**
     * Parses {@code Collection<String> subjects} into a {@code Set<Subject>}.
     *
     * @throws ParseException if any of the given {@code subjects} is invalid, or if there are more than
     *     {@value #MAX_SUBJECTS_PER_STUDENT} different subjects.
     */
    public static Set<Subject> parseSubjects(Collection<String> subjects) throws ParseException {
        requireNonNull(subjects);
        final Set<Subject> subjectSet = new HashSet<>();
        for (String subject : subjects) {
            subjectSet.add(parseSubject(subject));
        }
        if (subjectSet.isEmpty()) {
            throw new ParseException("A student must have at least one subject.");
        }
        if (subjectSet.size() > MAX_SUBJECTS_PER_STUDENT) {
            throw new ParseException(MESSAGE_TOO_MANY_SUBJECTS);
        }
        return subjectSet;
    }

    /**
     * Parses a {@code String rate} into a {@code Rate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code rate} is invalid.
     */
    public static Rate parseRate(String rate) throws ParseException {
        requireNonNull(rate);
        String trimmedRate = rate.trim();
        if (!Rate.isValidRate(trimmedRate)) {
            throw new ParseException(Rate.MESSAGE_CONSTRAINTS);
        }
        return new Rate(trimmedRate);
    }

    /**
     * Parses a {@code String venue} into a {@code Venue}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code venue} is invalid.
     */
    public static Venue parseVenue(String venue) throws ParseException {
        requireNonNull(venue);
        requirePrintable(venue, Venue.MESSAGE_CONSTRAINTS);
        String trimmedVenue = venue.trim();
        if (!Venue.isValidVenue(trimmedVenue)) {
            throw new ParseException(Venue.MESSAGE_CONSTRAINTS);
        }
        return new Venue(trimmedVenue);
    }

    /**
     * Parses a {@code String date} into a {@code LessonDate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code date} is invalid.
     */
    public static LessonDate parseDate(String date) throws ParseException {
        return parseDate(date, LocalDate.now());
    }

    /**
     * Parses a {@code String date} into a {@code LessonDate}, resolving the keywords {@code today} and {@code tmr}
     * (in any letter case) against {@code today}. Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code date} is invalid.
     */
    public static LessonDate parseDate(String date, LocalDate today) throws ParseException {
        requireNonNull(date);
        requireNonNull(today);
        String keyword = date.strip();
        if (keyword.equalsIgnoreCase(KEYWORD_TODAY)) {
            return new LessonDate(today.toString());
        }
        if (keyword.equalsIgnoreCase(KEYWORD_TOMORROW)) {
            return new LessonDate(today.plusDays(1).toString());
        }
        return parseValue(date, LessonDate::isValidDate, LessonDate.MESSAGE_CONSTRAINTS,
                LessonDate::new);
    }

    /**
     * Parses a {@code String time} into a {@code LessonTime}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code time} is invalid.
     */
    public static LessonTime parseTime(String time) throws ParseException {
        return parseValue(time, LessonTime::isValidTime, LessonTime.MESSAGE_CONSTRAINTS, LessonTime::new);
    }

    /**
     * Parses a {@code String minutes} into a {@code LessonDuration}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code minutes} is not a valid duration.
     */
    public static LessonDuration parseDuration(String minutes) throws ParseException {
        requireNonNull(minutes);
        String trimmedMinutes = minutes.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedMinutes)
                || !LessonDuration.isValidDuration(Integer.parseInt(trimmedMinutes))) {
            throw new ParseException(LessonDuration.MESSAGE_CONSTRAINTS);
        }
        return new LessonDuration(Integer.parseInt(trimmedMinutes));
    }

    /**
     * Parses a {@code String reason} for cancelling a lesson.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code reason} is invalid.
     */
    public static String parseReason(String reason) throws ParseException {
        requireNonNull(reason);
        requirePrintable(reason, Lesson.MESSAGE_REASON_CONSTRAINTS);
        return parseValue(reason, Lesson::isValidCancelReason, Lesson.MESSAGE_REASON_CONSTRAINTS,
                value -> value.replaceAll(" {2,}", " "));
    }

    /**
     * Parses teaching notes, trimming the ends and collapsing repeated spaces.
     *
     * @throws ParseException if the notes are empty, too long or contain non-printable characters.
     */
    public static String parseNotes(String notes) throws ParseException {
        requireNonNull(notes);
        requirePrintable(notes, Lesson.MESSAGE_NOTES_CONSTRAINTS);
        String normalisedNotes = notes.trim().replaceAll(" {2,}", " ");
        if (!Lesson.isValidNotes(normalisedNotes)) {
            throw new ParseException(Lesson.MESSAGE_NOTES_CONSTRAINTS);
        }
        return normalisedNotes;
    }

    /**
     * Returns the value of {@code prefix} parsed by {@code valueParser}, or null if the prefix is not given.
     *
     * @throws ParseException if the value is invalid.
     */
    public static <T> T parseOptional(ArgumentMultimap argMultimap, Prefix prefix, ValueParser<T> valueParser)
            throws ParseException {
        Optional<String> value = argMultimap.getValue(prefix);
        return value.isPresent() ? valueParser.parse(value.get()) : null;
    }

    /**
     * Returns true if the flag {@code flag}, a prefix that takes no value, is among the arguments.
     *
     * @throws ParseException if the flag is repeated or is given a value.
     */
    public static boolean parseFlag(ArgumentMultimap argMultimap, Prefix flag) throws ParseException {
        argMultimap.verifyNoDuplicatePrefixesFor(flag);
        Optional<String> value = argMultimap.getValue(flag);
        if (value.isPresent() && !value.get().isBlank()) {
            throw new ParseException(String.format(MESSAGE_FLAG_TAKES_NO_VALUE, flag));
        }
        return value.isPresent();
    }

    /**
     * Checks that every prefix in {@code prefixes} is present and that nothing precedes the first prefix.
     *
     * @param usage the usage message of the command, shown with the error.
     * @throws ParseException if a prefix is missing or there is text before the first prefix.
     */
    public static void requirePrefixesPresent(ArgumentMultimap argMultimap, String usage, Prefix... prefixes)
            throws ParseException {
        requirePrefixesFound(argMultimap, usage, prefixes);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw invalidFormat(usage);
        }
    }

    /**
     * Checks that every prefix in {@code prefixes} is present, and names each one that is not.
     *
     * @param usage the usage message of the command, shown with the error.
     * @throws ParseException if a prefix is missing.
     */
    public static void requirePrefixesFound(ArgumentMultimap argMultimap, String usage, Prefix... prefixes)
            throws ParseException {
        String missing = Stream.of(prefixes)
                .filter(prefix -> argMultimap.getValue(prefix).isEmpty())
                .map(Prefix::toString)
                .collect(Collectors.joining(", "));
        if (!missing.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_MISSING_PARAMETERS, missing, usage));
        }
    }

    /**
     * Returns {@code arguments} with every stand-alone {@code s/o} hidden from the tokenizer, so that a name such as
     * {@code Priya s/o Kumar} can be typed as written. Pass the values of the fields that may contain it through
     * {@link #unmaskSonOf(String)}.
     */
    public static String maskSonOf(String arguments) {
        return SON_OF.matcher(arguments).replaceAll("s" + SON_OF_MASK + "o");
    }

    /** Returns {@code value} with every {@code s/o} hidden by {@link #maskSonOf(String)} written out again. */
    public static String unmaskSonOf(String value) {
        return value.replace(SON_OF_MASK, '/');
    }

    /** Rejects non-printable text before trimming or whitespace normalization hides it. */
    static void requirePrintable(String value, String constraints) throws ParseException {
        if (value.codePoints().anyMatch(character -> Character.isISOControl(character)
                || character == '\u2028' || character == '\u2029')) {
            throw new ParseException(constraints);
        }
    }

    private static <T> T parseValue(String value, Predicate<String> isValid, String constraints,
            Function<String, T> factory) throws ParseException {
        requireNonNull(value);
        String trimmedValue = value.trim();
        if (!isValid.test(trimmedValue)) {
            throw new ParseException(constraints);
        }
        return factory.apply(trimmedValue);
    }

    /**
     * Parses one value of a command into a {@code T}.
     */
    @FunctionalInterface
    public interface ValueParser<T> {
        T parse(String value) throws ParseException;
    }
}
