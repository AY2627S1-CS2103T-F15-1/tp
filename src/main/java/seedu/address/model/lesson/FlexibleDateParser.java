package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the dates that a user types. A date has three parts, a day, a month and a year, that are separated by
 * the same separator, one of {@code /}, {@code -}, {@code .} or a space. Either the day comes first, as in
 * {@code 22/12/2026}, or the year does, as in {@code 2026-12-22}. The month is a number of one or two digits or its
 * English name in full or in its three-letter short form, so {@code 22-Dec-26} and {@code 22 December 2026} are
 * accepted. The year has four digits, or two digits for a year from 2000 to 2099.
 *
 * <p>A year of four digits shows the order of the other parts. When both the first and the last part have at
 * most two digits, as in {@code 26-12-22}, the date is read with the day first, which is 26 December 2022 for that
 * example. It is read with the year first only if the day-first reading is not a real date, as in
 * {@code 35-12-31}, so the same text always means the same date. A month is never written before the day, so
 * {@code 12/22/2026} is not a date. Impossible dates such as 2026-02-30 are never moved to a nearby real date.
 */
final class FlexibleDateParser {

    private static final Pattern THREE_PARTS = Pattern.compile("(?<first>[\\p{L}\\d]+)(?<separator>[/.\\- ])"
            + "(?<middle>[\\p{L}\\d]+)\\k<separator>(?<last>[\\p{L}\\d]+)");
    private static final Pattern DIGITS_OF_DAY = Pattern.compile("\\d{1,2}");
    private static final Pattern DIGITS_OF_MONTH = Pattern.compile("\\d{1,2}");
    private static final Pattern DIGITS_OF_YEAR = Pattern.compile("\\d{2}|\\d{4}");
    private static final int FIRST_YEAR_OF_TWO_DIGIT_YEARS = 2000;

    private FlexibleDateParser() {}

    /**
     * Returns the date that {@code text} stands for, or an empty {@code Optional} if it is not a real date written
     * in one of the accepted ways.
     */
    static Optional<LocalDate> parse(String text) {
        requireNonNull(text);
        Matcher matcher = THREE_PARTS.matcher(text.strip().replaceAll("\\s+", " "));
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String first = matcher.group("first");
        String middle = matcher.group("middle");
        String last = matcher.group("last");

        return of(last, middle, first).or(() -> of(first, middle, last));
    }

    /** Returns the date made of {@code year}, {@code month} and {@code day}, if they are written well and real. */
    private static Optional<LocalDate> of(String year, String month, String day) {
        Optional<Month> parsedMonth = parseMonth(month);
        if (!DIGITS_OF_YEAR.matcher(year).matches() || !DIGITS_OF_DAY.matcher(day).matches()
                || parsedMonth.isEmpty()) {
            return Optional.empty();
        }
        int parsedYear = Integer.parseInt(year);
        if (year.length() == 2) {
            parsedYear += FIRST_YEAR_OF_TWO_DIGIT_YEARS;
        }
        try {
            return Optional.of(LocalDate.of(parsedYear, parsedMonth.get(), Integer.parseInt(day)));
        } catch (DateTimeException e) {
            return Optional.empty();
        }
    }

    private static Optional<Month> parseMonth(String text) {
        if (DIGITS_OF_MONTH.matcher(text).matches()) {
            int number = Integer.parseInt(text);
            return number >= 1 && number <= 12 ? Optional.of(Month.of(number)) : Optional.empty();
        }
        String name = text.toLowerCase(Locale.ENGLISH);
        for (Month month : Month.values()) {
            String full = month.name().toLowerCase(Locale.ENGLISH);
            if (name.equals(full) || name.equals(full.substring(0, 3)) || month == Month.SEPTEMBER
                    && name.equals("sept")) {
                return Optional.of(month);
            }
        }
        return Optional.empty();
    }
}
